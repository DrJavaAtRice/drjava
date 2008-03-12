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

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import edu.rice.cs.plt.io.VoidOutputStream;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;

/** 
 * A log that writes tagged, indented text to {@link System#out}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class SystemOutLog extends OutputStreamLog {
  
  private OutputStream _currentOutput;
  
  /** Create a log to {@code System.out} without filtering, using the platform's default charset. */
  public SystemOutLog() {
    // binding to System.out is delayed to be thread safe (using "System.out" both in the super call
    // and the field initialization raises the possibility that its value changes in between the two refs)
    super(VoidOutputStream.INSTANCE);
    _currentOutput = VoidOutputStream.INSTANCE;
  }
  
  /** Create a log to {@code System.out} without filtering, using the given charset. */
  public SystemOutLog(String charsetName) throws UnsupportedEncodingException {
    super(VoidOutputStream.INSTANCE, charsetName);
    _currentOutput = VoidOutputStream.INSTANCE;
  }
  
  /** Create a log to {@code System.out} with the given filter, using the platform's default charset. */
  public SystemOutLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(VoidOutputStream.INSTANCE, filter);
    _currentOutput = VoidOutputStream.INSTANCE;
  }
  
  /** Create a log to {@code System.out} with the given filter, using the given charset. */
  public SystemOutLog(String charsetName, Predicate2<? super Thread, ? super StackTraceElement> filter)
      throws UnsupportedEncodingException{
    super(VoidOutputStream.INSTANCE, charsetName, filter);
    _currentOutput = VoidOutputStream.INSTANCE;
  }
  
  protected synchronized void write(Date time, Thread thread, StackTraceElement location, 
                                    SizedIterable<? extends String> messages) {
    // Make sure we reflect changes to System.out (via System.setOut())
    OutputStream out = System.out;
    if (_currentOutput != out) { switchStream(out); _currentOutput = out; }
    super.write(time, thread, location, messages);
  }
  
}
