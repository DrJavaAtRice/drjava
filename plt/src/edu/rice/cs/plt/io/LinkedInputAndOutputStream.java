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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Coordinates reading and writing from the same source.  Ensures
 * that the given OutputStream will never write past the location of the
 * InputStream, potentially corrupting its contents.  When the OutputStream needs
 * to write more bytes than have been read, a buffer is used to
 * store the bytes in between.  In order to take advantage of these
 * linking features, all interaction with the user-provided InputStream and OutputStream
 * must subsequently take place via the results of {@link #inputStream()} and 
 * {@link #outputStream()}, respectively.
 */
public class LinkedInputAndOutputStream {
  
  private final DirectInputStream _linkedInputStream;
  private final DirectOutputStream _linkedOutputStream;
  private long _readIndex; // Must always be >= _writeIndex
  private boolean _eof;
  private long _writeIndex;
  
  /**
   * Link {@code in} and {@code out}.  Assumes that {@code in} and {@code out} are newly created, or at 
   * least that their underlying cursors point to the same place.  Also assumes that no further 
   * direct interaction with {@code in} or {@code out} will occur.
   */
  public LinkedInputAndOutputStream(final InputStream in, final OutputStream out) {
    _readIndex = 0;
    _writeIndex = 0;
    _eof = false;
    final ExpandingByteBuffer buffer = new ExpandingByteBuffer();
    final InputStream fromBuffer = buffer.inputStream();
    final DirectOutputStream toBuffer = buffer.outputStream();
    
    _linkedInputStream = new DirectInputStream() {
      
      public int read(byte[] bbuf, int offset, int bytes) throws IOException {
        int read = 0;
        
        if (!buffer.isEmpty()) {
          int bufferReadResult = fromBuffer.read(bbuf, offset, bytes);
          if (bufferReadResult < 0) {
            throw new IllegalStateException("Unexpected negative result from ExpandingByteBuffer read");
          }
          else if (bufferReadResult > 0) { read += bufferReadResult; bytes -= bufferReadResult; }
        }
        
        if (buffer.isEmpty() && bytes >= 0) {
          int readResult = in.read(bbuf, offset + read, bytes);
          if (readResult < 0) {
            _eof = true;
            return read > 0 ? read : readResult;
          }
          else {
            read += readResult;
            bytes -= readResult;
            _readIndex += readResult;
          }
        }
        return read;
      }
      
      public void close() throws IOException { in.close(); }
      
      public int available() throws IOException { return in.available(); }
    };
    
    _linkedOutputStream = new DirectOutputStream() {
      public void write(byte[] bbuf, int offset, int bytes) throws IOException {
        long newIndex = _writeIndex + bytes;
        while (newIndex > _readIndex) {
          // If we've reached the eof, we don't try to buffer anything
          if (_eof) { _readIndex = newIndex; }
          else {
            int bufferWriteResult = toBuffer.write(in, (int) (newIndex - _readIndex));
            if (bufferWriteResult < 0) { _eof = true; }
            else { _readIndex += bufferWriteResult; }
          }
        }
        out.write(bbuf, offset, bytes);
        _writeIndex = newIndex;
      }
      
      public void close() throws IOException { out.close(); }
      
      public void flush() throws IOException { out.flush(); }
    };
  }
  
  public DirectInputStream inputStream() { return _linkedInputStream; }
  
  public DirectOutputStream outputStream() { return _linkedOutputStream; }
    
}
