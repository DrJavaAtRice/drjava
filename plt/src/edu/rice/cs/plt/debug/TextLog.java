/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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

import java.util.Date;
import java.io.BufferedWriter;
import java.io.IOException;

import edu.rice.cs.plt.collect.TotalMap;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.iter.SizedIterable;

/**
 * An abstract log that records messages as plain text.  This class takes responsibility for converting
 * {@link #write} invocations to blocks of properly-indented and tagged text.  Subclasses are responsible for managing
 * the {@code BufferedWriter}(s) that will be used for output, and for calling {@link #writeText} as appropriate.
 */
public abstract class TextLog extends AbstractLog {
  
  private static final String HANGING_INDENT = "    ";
  
  private static final Lambda<Thread, Indenter> MAKE_INDENTER = new Lambda<Thread, Indenter>() {
    public Indenter value(Thread t) { return new Indenter(); }
  };
  
  private final TotalMap<Thread, Indenter> _indenters;
  
  /** Create a log without a filter */
  protected TextLog() {
    super();
    _indenters = new TotalMap<Thread, Indenter>(MAKE_INDENTER, true);
  }
  
  /** Create a log using the given filter */
  protected TextLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _indenters = new TotalMap<Thread, Indenter>(MAKE_INDENTER, true);
  }
  
  /** 
   * Write a text message based on the given parameters to {@code w}.  At the end of execution,
   * {@link BufferedWriter#newLine} and {@link java.io.Writer#flush} will be invoked.  Subclasses
   * are responsible for invoking this method as needed.
   * @throws WrappedException  If an {@code IOException} occurs during writing
   */
  protected void writeText(BufferedWriter w, Date time, Thread thread, StackTraceElement location, 
                           SizedIterable<? extends String> messages) {
    try {
      Indenter indent = _indenters.get(thread);
      w.write(indent.indentString());
      w.write("[" + formatLocation(location) + " - " + formatThread(thread) + " - " + formatTime(time) + "]");
      w.newLine();
      for (String s : messages) {
        w.write(indent.indentString());
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
  }
  
  protected void push() { _indenters.get(Thread.currentThread()).push(); }
  
  protected void pop() { _indenters.get(Thread.currentThread()).pop(); }
  
}
