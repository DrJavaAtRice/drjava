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

package edu.rice.cs.plt.io;

import java.io.*;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An implementation of {@code DirectReader} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code Reader}.
 */
public class WrappedDirectReader extends DirectReader implements Composite {
  private Reader _reader;
  
  public WrappedDirectReader(Reader reader) { _reader = reader; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_reader) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_reader) + 1; }
  
  @Override public void close() throws IOException { _reader.close(); }
  
  @Override public void mark(int readAheadLimit) throws IOException { _reader.mark(readAheadLimit); }
  
  @Override public boolean markSupported() { return _reader.markSupported(); }
  
  @Override public int read() throws IOException { return _reader.read(); }
  
  @Override public int read(char[] cbuf) throws IOException { return _reader.read(cbuf); }
  
  @Override public int read(char[] cbuf, int offset, int length) throws IOException {
    return _reader.read(cbuf, offset, length);
  }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
  //@Override public int read(CharBuffer target) throws IOException { return _reader.read(target); }
  
  @Override public boolean ready() throws IOException { return _reader.ready(); }
  
  @Override public void reset() throws IOException { _reader.reset(); }
  
  @Override public long skip(long n) throws IOException { return _reader.skip(n); }

  /** 
   * If the input is a {@code DirectReader}, cast it; otherwise, create a
   * {@code WrappedDirectReader}.
   */
  public static DirectReader makeDirect(Reader reader) {
    if (reader instanceof DirectReader) { return (DirectReader) reader; }
    else { return new WrappedDirectReader(reader); }
  }

}
