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

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;
import edu.rice.cs.plt.tuple.IdentityWrapper;

/** 
 * A log sink that sends messages to all the sinks it contains.  This allows, for example, logging to be
 * viewed at runtime while, at the same time, being recorded to a file.  The set of sinks can be
 * modified at any time.  (Concurrent logging is supported, but concurrent {@code add} or {@code remove}
 * invocations are not: only one thread should manage the set of sinks at a time.)
 */
public class SplitLogSink implements LogSink, Composite {
  
  private final Set<IdentityWrapper<LogSink>> _sinkSet;
  // a snapshot of _sinkSet that doesn't require synchronized access (direct iteration, in contrast, would)
  private volatile Iterable<LogSink> _sinks;
  
  /** Create a log that will send its messages to each of {@code logs} */
  public SplitLogSink(LogSink... sinks) { this(IterUtil.asIterable(sinks)); }
  
  public SplitLogSink(Iterable<? extends LogSink> sinks) {
    _sinkSet = new HashSet<IdentityWrapper<LogSink>>();
    add(sinks);
  }
  
  public void add(LogSink... toAdd) {
    for (LogSink s : toAdd) { _sinkSet.add(IdentityWrapper.make(s)); }
    refreshSinks();
  }

  public void add(Iterable<? extends LogSink> toAdd) {
    for (LogSink s : toAdd) { _sinkSet.add(IdentityWrapper.make(s)); }
    refreshSinks();
  }
  
  public void remove(LogSink... toRemove) {
    for (LogSink s : toRemove) { _sinkSet.remove(IdentityWrapper.make(s)); }
    refreshSinks();
  }
  
  public void remove(Iterable<? extends LogSink> toRemove) {
    for (LogSink s : toRemove) { _sinkSet.remove(IdentityWrapper.make(s)); }
    refreshSinks();
  }
  
  private void refreshSinks() {
    _sinks = IterUtil.snapshot(IterUtil.valuesOf(_sinkSet));
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_sinks) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_sinks) + 1; }
  
  public void close() throws IOException {
    IOException exception = null;
    for (LogSink s : _sinks) {
      try { s.close(); }
      catch (IOException e) { exception = e; }
    }
    if (exception != null) { throw exception; }
  }
  
  public void log(StandardMessage m) {
    for (LogSink s : _sinks) { s.log(m); }
  }
  
  public void logStart(StartMessage m) {
    for (LogSink s : _sinks) { s.logStart(m); }
  }
  
  public void logEnd(EndMessage m) {
    for (LogSink s : _sinks) { s.logEnd(m); }
  }

  public void logError(ErrorMessage m) {
    for (LogSink s : _sinks) { s.logError(m); }
  }

  public void logStack(StackMessage m) {
    for (LogSink s : _sinks) { s.logStack(m); }
  }
  
}
