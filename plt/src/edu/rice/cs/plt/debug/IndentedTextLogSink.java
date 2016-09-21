/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.debug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.WeakHashMap;

import edu.rice.cs.plt.collect.TotalMap;
import edu.rice.cs.plt.concurrent.LockMap;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * An abstract log that records messages as indented text.  Subclasses are responsible for providing a
 * BufferedWriter as needed via {@link #writer}.
 */
public abstract class IndentedTextLogSink extends TextLogSink {

  private static final String HANGING_INDENT = "    ";
  
  private static final Lambda<Long, Indenter> MAKE_INDENTER = new Lambda<Long, Indenter>() {
    public Indenter value(Long l) { return new Indenter(); }
  };
  
  private final TotalMap<Long, Indenter> _indenters;
  private final LockMap<BufferedWriter> _locks;
  private final WeakHashMap<BufferedWriter, Long> _lastThreads; // used to insert breaks when the log switches threads
  
  protected IndentedTextLogSink() {
    super();
    _indenters = new TotalMap<Long, Indenter>(MAKE_INDENTER, true);
    _locks = new LockMap<BufferedWriter>(5);
    _lastThreads = new WeakHashMap<BufferedWriter, Long>(5);
  }
  
  protected IndentedTextLogSink(int idealLineWidth) {
    super(idealLineWidth);
    _indenters = new TotalMap<Long, Indenter>(MAKE_INDENTER, true);
    _locks = new LockMap<BufferedWriter>(5);
    _lastThreads = new WeakHashMap<BufferedWriter, Long>(5);
  }

  /** Get a BufferedWriter for outputting the given message. */
  protected abstract BufferedWriter writer(Message m);
   
  protected void write(Message m, SizedIterable<String> text) {
    BufferedWriter w = writer(m);
    doWrite(w, m, text);
  }
  
  protected void writeStart(StartMessage m, SizedIterable<String> text) {
    write(m, text);
    synchronized(_indenters) { _indenters.get(m.thread().getId()).push(); }
  }

  protected void writeEnd(EndMessage m, SizedIterable<String> text) {
    synchronized(_indenters) { _indenters.get(m.thread().getId()).pop(); }
    write(m, text);
  }
      
  private void doWrite(BufferedWriter w, Message m, SizedIterable<String> text) {
    Long threadId = m.thread().getId();
    String indentString;
    synchronized(_indenters) { indentString = _indenters.get(threadId).indentString(); }
    Runnable unlock = _locks.lock(w);
    try {
      if (_lastThreads.containsKey(w)) {
        Long prevId = _lastThreads.get(w);
        if (!prevId.equals(threadId)) { w.newLine(); _lastThreads.put(w, threadId); }
      }
      else { _lastThreads.put(w, threadId); }
      w.write(indentString);
      w.write("[" + formatLocation(m.caller()) + " - " + formatThread(m.thread()) +" - " +
              formatTime(m.time()) + "]");
      w.newLine();
      for (String s : text) {
        w.write(indentString);
        w.write(HANGING_INDENT);
        w.write(s);
        w.newLine();
      }
      w.flush();
    }
    catch (IOException e) {
      // Throw an exception, because otherwise the lack of anything in the log will be interpreted as evidence
      // that the calling line of code did not execute.
      throw new WrappedException(e);
    }
    finally { unlock.run(); }
  }
  
}
