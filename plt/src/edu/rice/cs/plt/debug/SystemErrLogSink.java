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

import java.io.*;
import java.nio.charset.Charset;

/**
 * A log sink that writes tagged, indented text to {@link System#err}.  Automatically changes the destination
 * stream after updates are made to {@code System.err}.
 */
public class SystemErrLogSink extends IndentedTextLogSink {
  
  private final Charset _charset;
  private volatile BufferedWriter _writer;
  private volatile PrintStream _currentStream; // the OutputStream on which _writer is based
  
  /** Create a log sink to {@code System.err} using the platform's default charset. */
  public SystemErrLogSink() {
    super();
    _charset = Charset.defaultCharset();
    _writer = null;
    _currentStream = null;
  }
  
  /** Create a log sink to {@code System.err} using the given charset. */
  public SystemErrLogSink(String charsetName) throws UnsupportedEncodingException {
    super();
    _charset = Charset.forName(charsetName);
    _currentStream = null;
  }
  
  /** Create a log sink to {@code System.err} using the platform's default charset and the given line width. */
  public SystemErrLogSink(int idealLineWidth) {
    super(idealLineWidth);
    _charset = Charset.defaultCharset();
    _writer = null;
    _currentStream = null;
  }
  
  /** Create a log to {@code System.err} using the given charset and line width. */
  public SystemErrLogSink(String charsetName, int idealLineWidth) throws UnsupportedEncodingException {
    super(idealLineWidth);
    _charset = Charset.forName(charsetName);
    _writer = null;
    _currentStream = null;
  }
  
  @Override protected BufferedWriter writer(Message m) {
    // Make sure we reflect changes to System.err (via System.setErr())
    PrintStream err = System.err;
    if (_currentStream != err) {
      // the update isn't atomic, but that's okay as long as _currentStream is updated last
      _writer = new BufferedWriter(new OutputStreamWriter(err, _charset));
      _currentStream = err;
    }
    return _writer;
  }
  
  public void close() throws IOException { _writer.close(); }
  
}
