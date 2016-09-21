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
 * An implementation of {@code DirectWriter} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code Writer}.
 */
public class WrappedDirectWriter extends DirectWriter implements Composite {
  private Writer _writer;
  
  public WrappedDirectWriter(Writer writer) { _writer = writer; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_writer) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_writer) + 1; }
  
  @Override public void close() throws IOException { _writer.close(); }
  
  @Override public void flush() throws IOException { _writer.flush(); }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
  //@Override public Writer append(char c) throws IOException { _writer.append(c); return this; }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
//  @Override public Writer append(CharSequence csq) throws IOException {
//    _writer.append(csq);
//    return this;
//  }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
//  @Override public Writer append(CharSequence csq, int start, int end) throws IOException {
//    _writer.append(csq, start, end);
//    return this;
//  }
  
  @Override public void write(int c) throws IOException { _writer.write(c); }
  
  @Override public void write(char[] cbuf) throws IOException { _writer.write(cbuf); }
  
  @Override public void write(char[] cbuf, int offset, int length) throws IOException {
    _writer.write(cbuf, offset, length);
  }
  
  @Override public void write(String s) throws IOException { _writer.write(s); }
  
  @Override public void write(String s, int offset, int length) throws IOException {
    _writer.write(s, offset, length);
  }

  /** 
   * If the input is a {@code DirectWriter}, cast it; otherwise, create a
   * {@code WrappedDirectWriter}.
   */
  public static DirectWriter makeDirect(Writer writer) {
    if (writer instanceof DirectWriter) { return (DirectWriter) writer; }
    else { return new WrappedDirectWriter(writer); }
  }

}
