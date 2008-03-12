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
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.WrappedException;

/** 
 * A log that writes tagged, indented text to an output stream.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class OutputStreamLog extends TextLog {
   
  private final String _charset; // may be null, indicating the default
  private volatile BufferedWriter _writer;
    
  /**
   * Create a log to the given stream without filtering, using the platform's default charset.
   * The provided stream will not be closed.
   */
  public OutputStreamLog(OutputStream out) {
    super();
    _charset = null;
    _writer = new BufferedWriter(new OutputStreamWriter(out));
  }
  
  /**
   * Create a log to the given stream without filtering, using the specified charset.
   * The provided stream will not be closed.
   */
  public OutputStreamLog(OutputStream out, String charsetName) throws UnsupportedEncodingException {
    super();
    _charset = charsetName;
    _writer = new BufferedWriter(new OutputStreamWriter(out, charsetName));
  }
  
  /** Create a log to the given stream with the given filter, using the platform's default charset. */
  public OutputStreamLog(OutputStream out, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _charset = null;
    _writer = new BufferedWriter(new OutputStreamWriter(out));
  }
  
  /** Create a log to the given stream with the given filter, using the specified charset. */
  public OutputStreamLog(OutputStream out, String charsetName,
                         Predicate2<? super Thread, ? super StackTraceElement> filter)
                         throws UnsupportedEncodingException {
    super(filter);
    _charset = charsetName;
    _writer = new BufferedWriter(new OutputStreamWriter(out, charsetName));
  }
  
  /** Change the output destination to a different stream. */
  public void switchStream(OutputStream newOut) {
    // we've guaranteed (by using writeText) that the old stream has already been flushed
    if (_charset == null) { _writer = new BufferedWriter(new OutputStreamWriter(newOut)); }
    else {
      try { _writer = new BufferedWriter(new OutputStreamWriter(newOut, _charset)); }
      catch (UnsupportedEncodingException e) {
        // Should never happen, because we already used this encoding.
        throw new WrappedException(e);
      }
    }
  }
    
  
  protected synchronized void write(Date time, Thread thread, StackTraceElement location, 
                                    SizedIterable<? extends String> messages) {
    writeText(_writer, time, thread, location, messages);
  }
  
}
