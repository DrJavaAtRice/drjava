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
 * An implementation of {@code DirectOutputStream} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code OutputStream}.
 */
public class WrappedDirectOutputStream extends DirectOutputStream implements Composite {
  private OutputStream _stream;
  
  public WrappedDirectOutputStream(OutputStream stream) { _stream = stream; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_stream) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_stream) + 1; }
  
  @Override public void close() throws IOException { _stream.close(); }
  
  @Override public void flush() throws IOException { _stream.flush(); }
  
  @Override public void write(int b) throws IOException { _stream.write(b); }
  
  @Override public void write(byte[] bbuf) throws IOException { _stream.write(bbuf); }
  
  @Override public void write(byte[] bbuf, int offset, int length) throws IOException {
    _stream.write(bbuf, offset, length);
  }
  
  /** 
   * If the input is a {@code DirectOutputStream}, cast it; otherwise, create a
   * {@code WrappedDirectOutputStream}.
   */
  public static DirectOutputStream makeDirect(OutputStream stream) {
    if (stream instanceof DirectOutputStream) { return (DirectOutputStream) stream; }
    else { return new WrappedDirectOutputStream(stream); }
  }

}
