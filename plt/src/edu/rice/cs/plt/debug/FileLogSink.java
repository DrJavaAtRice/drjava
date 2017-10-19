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
import java.util.Date;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.text.TextUtil;

/**
 * A log sink that writes tagged, indented text to a file.  The file is not opened until required for logging,
 * and is closed on program exit.
 */
public class FileLogSink extends IndentedTextLogSink {
  
  private final Thunk<BufferedWriter> _writer;
  private volatile boolean _active;
  
  public FileLogSink(String filename) { this(new File(filename), null, true); }
  
  public FileLogSink(File f) { this(f, null, true); }
  
  public FileLogSink(File f, int idealLineWidth) { this(f, null, idealLineWidth, true); }
  
  public FileLogSink(File f, boolean closeOnExit) { this(f, null, closeOnExit); }
  
  public FileLogSink(File f, int idealLineWidth, boolean closeOnExit) {
    this(f, null, idealLineWidth, closeOnExit);
  }
  
  public FileLogSink(File f, String charset) { this(f, charset, true); }

  public FileLogSink(File f, String charset, boolean closeOnExit) {
    super();
    _writer = initWriter(f, charset);
    _active = false;
    if (closeOnExit) { IOUtil.closeOnExit(this); }
  }
  
  public FileLogSink(File f, String charset, int idealLineWidth) {
    this(f, charset, idealLineWidth, true);
  }
  
  public FileLogSink(File f, String charset, int idealLineWidth, boolean closeOnExit) {
    super(idealLineWidth);
    _writer = initWriter(f, charset);
    _active = false;
    if (closeOnExit) { IOUtil.closeOnExit(this); }
  }
  
  /** @param charset  {@code null} if the default should be used */
  private Thunk<BufferedWriter> initWriter(final File f, final String charset) {
    return LazyThunk.make(new Thunk<BufferedWriter>() {
      public BufferedWriter value() {
        try {
          OutputStream out = new FileOutputStream(f, true);
          try {
            Writer w = (charset == null) ? new OutputStreamWriter(out) : new OutputStreamWriter(out, charset);
            BufferedWriter result = new BufferedWriter(w);
            IOUtil.closeOnExit(result);
            String stars = TextUtil.repeat('*', 40);
            result.write(stars);
            result.newLine();
            result.write("Opened log file " + formatTime(new Date()));
            result.newLine();
            result.write(stars);
            result.newLine();
            result.newLine();
            result.flush();
            _active = true;
            return result;
          }
          finally { if (!_active) { out.close(); } }
        }
        catch (IOException e) { throw new WrappedException(e); }
      }
    });
  }
  
  @Override protected BufferedWriter writer(Message m) { return _writer.value(); }
  
  /** Close the file stream. */
  public void close() throws IOException {
    if (_active) { _writer.value().close(); _active = false; }
  }
    
}
