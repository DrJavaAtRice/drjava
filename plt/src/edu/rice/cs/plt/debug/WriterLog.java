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

import java.io.Writer;
import java.io.BufferedWriter;
import java.util.Date;

import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.io.IOUtil;

/** 
 * A log that writes tagged, indented text to the given {@code Writer}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class WriterLog extends TextLog {
  
  private final BufferedWriter _w;
  
  /** Create a writer-based log without filtering */
  public WriterLog(Writer w) {
    super();
    _w = IOUtil.asBuffered(w);
  }
  
  /** Create a writer-based log with the given filter */
  public WriterLog(Writer w, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _w = IOUtil.asBuffered(w);
  }
  
  protected synchronized void write(Date time, Thread thread, StackTraceElement location, 
                                    SizedIterable<? extends String> messages) {
    writeText(_w, time, thread, location, messages);
  }
  
}
