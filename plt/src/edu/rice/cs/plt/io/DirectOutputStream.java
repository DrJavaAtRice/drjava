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
 * <p>An {@code OutputStream} that supports writing directly from an {@code InputStream}.  This class
 * provides default implementations defined in terms of {@code InputStream} and {@code OutputStream}
 * methods.  Subclasses can override (at least) {@link #writeAll(InputStream, byte[])} and
 * {@link #write(InputStream, int, byte[])} to provide better implementations (by, for example,
 * not invoking {@link OutputStream#write(byte[])}).</p>
 * 
 * <p>Also guarantees that, consistent with the {@link Writer} class, all write operations are
 * by default defined in terms of the (declared abstract) {@link #write(byte[], int, int)} method.</p>
 * 
 * @see DirectWriter
 * @see DirectInputStream
 * @see DirectReader
 */
public abstract class DirectOutputStream extends OutputStream {
  
  protected static final int DEFAULT_BUFFER_SIZE = 1024;

  /** Delegate to the more general {@link #write(byte[], int, int)} method */
  @Override public void write(int b) throws IOException { write(new byte[]{ (byte) b }, 0, 1); }
  
  /** Delegate to the more general {@link #write(byte[], int, int)} method */
  @Override public void write(byte[] bbuf) throws IOException { write(bbuf, 0, bbuf.length); }
      
  /** Subclasses are, at a minimum, required to implement this method. */
  @Override public abstract void write(byte[] bbuf, int offset, int bytes) throws IOException;

  /**
   * Write some number of bytes, using the provided {@code InputStream} as input.  Fewer bytes
   * may be written if the input stream has fewer available.  The default implementation 
   * invokes {@link #write(InputStream, int, int)} with the minimum of {@code bytes} and 
   * {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that do not rely on a buffer in 
   * {@link #write(InputStream, int, byte[])} should override this method.
   * 
   * @param in  A stream to be read from
   * @param bytes  The number of bytes to write
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written
   * @throws IOException  If an error occurs during reading or writing
   */
  public int write(InputStream in, int bytes) throws IOException {
    return write(in, bytes, (bytes < DEFAULT_BUFFER_SIZE) ? bytes : DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Write some number of bytes, using the provided {@code InputStream} as input.  Fewer bytes
   * may be written if the input stream has fewer available.  The default implementation 
   * invokes {@link #write(InputStream, int, byte[])} with a newly-allocated array of the given size.  
   * Subclasses that do not rely on a buffer in {@link #write(InputStream, int, byte[])}
   * should override this method.
   * 
   * @param in  A stream to be read from
   * @param bytes  The number of bytes to write
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance on large readers
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int write(InputStream in, int bytes, int bufferSize) throws IOException {
    return write(in, bytes, new byte[bufferSize]);
  }
  
  /**
   * Write some number of bytes, using the provided {@code InputStream} as input.  Fewer bytes
   * may be written if the input stream has fewer available.  The given buffer is useful
   * in repeated {@code write} invocations to avoid unnecessary memory allocation.  The default 
   * implementation repeatedly fills the given buffer via a
   * {@link InputStream#read(byte[], int, int)} operation, then writes it via 
   * {@link OutputStream#write(byte[], int, int)}.  Subclasses that do not require an external buffer 
   * should override this method.
   * 
   * @param in  A stream to be read from
   * @param bytes  The number of bytes to write
   * @param buffer  A buffer used to copy bytes from the input stream to this stream.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int write(InputStream in, int bytes, byte[] buffer) throws IOException {
    return IOUtil.doWriteFromInputStream(in, this, bytes, buffer);
  }
  
  /**
   * Write the full contents of the given {@code InputStream} to this stream.  The method will block 
   * until an end-of-file is reached.  The default implementation invokes 
   * {@link #writeAll(InputStream, int)} with {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the 
   * size of this stream's remaining contents, or that do not rely on a buffer in 
   * {@link #writeAll(InputStream, byte[])}, should override this method.
   * 
   * @param in  A stream to be read from
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   */
  public int writeAll(InputStream in) throws IOException {
    return writeAll(in, DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Write the full contents of the given {@code InputStream} to this stream.  The method will block 
   * until an end-of-file is reached.  The default implementation invokes 
   * {@link #writeAll(InputStream, byte[])} with a newly-allocated array of the given size.  Subclasses 
   * that do not rely on a buffer in {@link #writeAll(InputStream, byte[])} should override this method.
   * 
   * @param in  A stream to be read from
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance on large readers
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int writeAll(InputStream in, int bufferSize) throws IOException {
    return writeAll(in, new byte[bufferSize]);
  }

  /**
   * Write the full contents of the given {@code InputStream} to this stream.  The given buffer is useful 
   * in repeated {@code writeAll} invocations to avoid unnecessary memory allocation.  The method will 
   * block until an end-of-file is reached.  The default implementation repeatedly fills the given buffer 
   * via a {@link InputStream#read(byte[])} operation, then writes it via {@link OutputStream#write(byte[])}.  
   * Subclasses that do not require an external buffer should override this method.
   * 
   * @param in  A stream to be read from
   * @param buffer  A buffer used to copy bytes from the input stream to this stream.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if the input stream is at the end of file; otherwise, the number of characters 
   *          written (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int writeAll(InputStream in, byte[] buffer) throws IOException {
    return IOUtil.doCopyInputStream(in, this, buffer);
  }  
  
}
