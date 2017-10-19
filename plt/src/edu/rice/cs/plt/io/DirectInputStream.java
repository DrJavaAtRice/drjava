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

/**
 * <p>An {@code InputStream} that supports reading directly into an {@code OutputStream}.  This class
 * provides default implementations defined in terms of {@code InputStream} and {@code OutputStream}
 * methods.  Subclasses can override (at least) {@link #readAll(OutputStream, byte[])} and
 * {@link #read(OutputStream, int, byte[])} to provide better implementations (by, for example,
 * not invoking {@link InputStream#read(byte[])}).</p>
 * 
 * <p>Also guarantees that, consistent with the {@link Reader} class, all read operations are
 * by default defined in terms of the (declared abstract) {@link #read(byte[], int, int)} method.</p>
 * 
 * @see DirectReader
 * @see DirectOutputStream
 * @see DirectWriter
 */
public abstract class DirectInputStream extends InputStream {
  
  protected static final int DEFAULT_BUFFER_SIZE = 1024;

  /** Delegate to the more general {@link #read(byte[], int, int)} method */
  @Override public int read() throws IOException {
    byte[] bbuf = new byte[1];
    int readResult = read(bbuf, 0, 1);
    if (readResult == -1) { return readResult; }
    else if (readResult == 1) { return bbuf[0]; }
    else { throw new IOException("Unexpected read result: " + readResult); }
  }
  
  /** Delegate to the more general {@link #read(byte[], int, int)} method */
  @Override public int read(byte[] bbuf) throws IOException { return read(bbuf, 0, bbuf.length); }
  
  /** Subclasses are, at a minimum, required to implement this method. */
  @Override public abstract int read(byte[] bbuf, int offset, int bytes) throws IOException;
      
  /**
   * Read some number of bytes from this stream, sending them to the provided {@code OutputStream}.
   * The default implementation invokes {@link #read(OutputStream, int, int)} with the minimum of 
   * {@code bytes} and {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the size of this stream's 
   * remaining contents, or that do not rely on a buffer in {@link #read(OutputStream, int, byte[])}, 
   * should override this method.
   * 
   * @param out  A stream to be written to
   * @param bytes  The number of bytes to read
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read
   * @throws IOException  If an error occurs during reading or writing
   */
  public int read(OutputStream out, int bytes) throws IOException {
    return read(out, bytes, (bytes < DEFAULT_BUFFER_SIZE) ? bytes : DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Read some number of bytes from this stream, sending them to the provided {@code OutputStream}.
   * The default implementation invokes {@link #read(OutputStream, int, byte[])} with a newly-allocated array 
   * of the given size.  Subclasses that do not rely on a buffer in {@link #read(OutputStream, int, byte[])}
   * should override this method.
   * 
   * @param out  A stream to be written to
   * @param bytes  The number of bytes to read
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance of large streams
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int read(OutputStream out, int bytes, int bufferSize) throws IOException {
    return read(out, bytes, new byte[bufferSize]);
  }
  
  /**
   * Read some number of bytes from this stream, sending them to the provided {@code OutputStream}.
   * The given buffer is useful in repeated {@code read} invocations to avoid unnecessary
   * memory allocation.  The default implementation repeatedly fills the given buffer via a 
   * {@link InputStream#read(byte[], int, int)} operation, then writes it via 
   * {@link OutputStream#write(byte[], int, int)}.  Subclasses that do not require an external buffer 
   * should override this method.
   * 
   * @param out  A stream to be written to
   * @param bytes  The number of bytes to read
   * @param buffer  A buffer used to copy bytes from this stream to the output stream.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int read(OutputStream out, int bytes, byte[] buffer) throws IOException {
    return IOUtil.doWriteFromInputStream(this, out, bytes, buffer);
  }
  
  /**
   * Read the full contents of this stream, sending the bytes to the provided {@code OutputStream}.
   * The method will block until an end-of-file is reached.  The default implementation invokes 
   * {@link #readAll(OutputStream, int)} with {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the 
   * size of this stream's remaining contents, or that do not rely on a buffer in 
   * {@link #readAll(OutputStream, byte[])}, should override this method.
   * 
   * @param out  A stream to be written to
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   */
  public int readAll(OutputStream out) throws IOException { return readAll(out, DEFAULT_BUFFER_SIZE); }
  
  /**
   * Read the full contents of this stream, sending the bytes to the provided {@code OutputStream}.
   * The method will block until an end-of-file is reached.  The default implementation invokes 
   * {@link #readAll(OutputStream, byte[])} with a newly-allocated array of the given size.  Subclasses 
   * that do not rely on a buffer in {@link #readAll(OutputStream, byte[])} should override this method.
   * 
   * @param out  A stream to be written to
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance of large streams
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int readAll(OutputStream out, int bufferSize) throws IOException {
    return readAll(out, new byte[bufferSize]);
  }

  /**
   * Read the full contents of this stream, sending the bytes to the provided {@code OutputStream}.
   * The given buffer is useful in repeated {@code readAll} invocations to avoid unnecessary
   * memory allocation.  The method will block until an end-of-file is reached.  The default 
   * implementation repeatedly fills the given buffer via a {@link InputStream#read(byte[])} operation, 
   * then writes it via {@link OutputStream#write(byte[])}.  Subclasses that do not require an external buffer 
   * should override this method.
   * 
   * @param out  A stream to be written to
   * @param buffer  A buffer used to copy bytes from this stream to the output stream.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if this stream is at the end of file; otherwise, the number of bytes read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int readAll(OutputStream out, byte[] buffer) throws IOException {
    return IOUtil.doCopyInputStream(this, out, buffer);
  }  
  
}
