/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

import edu.rice.cs.plt.iter.IterUtil;

/** 
 * A log that sends messages to all the logs it contains.  This allows, for example, logging to be
 * viewed at runtime while, at the same time, being recorded to a file.
 */
public class LogSplitter implements Log {
  
  private final Iterable<Log> _logs;
  
  /** Create a log that will send its messages to each of {@code logs} */
  public LogSplitter(Log... logs) {
    _logs = IterUtil.asIterable(logs);
  }
  
  public LogSplitter(Iterable<? extends Log> logs) {
    _logs = IterUtil.snapshot(logs);
  }
  
  public void log() {
    for (Log l : _logs) { l.log(); }
  }
  
  public void log(String message) {
    for (Log l : _logs) { l.log(message); }
  }
    
  public void log(Throwable t) {
    for (Log l : _logs) { l.log(t); }
  }
  
  public void log(String message, Throwable t) {
    for (Log l : _logs) { l.log(message, t); }
  }
  
  public void logStart() {
    for (Log l : _logs) { l.logStart(); }
  }
  
  public void logStart(String message) {
    for (Log l : _logs) { l.logStart(message); }
  }
  
  public void logEnd() {
    for (Log l : _logs) { l.logEnd(); }
  }
  
  public void logEnd(String message) {
    for (Log l : _logs) { l.logEnd(message); }
  }
  
  public void logStack() {
    for (Log l : _logs) { l.logStack(); }
  }
  
  public void logStack(String message) {
    for (Log l : _logs) { l.logStack(message); }
  }
  
  public void logValue(String name, Object value) {
    for (Log l : _logs) { l.logValue(name, value); }
  }
  
  public void logValue(String message, String name, Object value) {
    for (Log l : _logs) { l.logValue(message, name, value); }
  }
  
  public void logValues(String[] names, Object... values) {
    for (Log l : _logs) { l.logValues(names, values); }
  }
  
  public void logValues(String message, String[] names, Object... values) {
    for (Log l : _logs) { l.logValues(message, names, values); }
  }
  
}
