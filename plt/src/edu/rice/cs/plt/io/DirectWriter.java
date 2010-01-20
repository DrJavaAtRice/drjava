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
 * A {@code Writer} that supports writing directly from a {@code Reader}.  This class
 * provides default implementations defined in terms of {@code Reader} and {@code Writer}
 * methods.  Subclasses can override (at least) {@link #writeAll(Reader, char[])} and
 * {@link #write(Reader, int, char[])} to provide better implementations (by, for example,
 * not invoking {@link Writer#write(char[])}).
 * 
 * @see DirectOutputStream
 * @see DirectReader
 * @see DirectInputStream
 */
public abstract class DirectWriter extends Writer {
  
  protected static final int DEFAULT_BUFFER_SIZE = 1024;

  /**
   * Write some number of characters, using the provided {@code Reader} as input.  Fewer characters
   * may be written if the reader has fewer available.  The default implementation 
   * invokes {@link #write(Reader, int, int)} with the minimum of {@code chars} and 
   * {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that do not rely on a buffer in 
   * {@link #write(Reader, int, char[])} should override this method.
   * 
   * @param r  A reader to be read from
   * @param chars  The number of characters to write
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written
   * @throws IOException  If an error occurs during reading or writing
   */
  public int write(Reader r, int chars) throws IOException {
    return write(r, chars, (chars < DEFAULT_BUFFER_SIZE) ? chars : DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Write some number of characters, using the provided {@code Reader} as input.  Fewer characters
   * may be written if the reader has fewer available.  The default implementation 
   * invokes {@link #write(Reader, int, char[])} with a newly-allocated array of the given size.  
   * Subclasses that do not rely on a buffer in {@link #write(Reader, int, char[])} should override 
   * this method.
   * 
   * @param r  A reader to be read from
   * @param chars  The number of characters to write
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance on large readers
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int write(Reader r, int chars, int bufferSize) throws IOException {
    return write(r, chars, new char[bufferSize]);
  }
  
  /**
   * Write some number of characters, using the provided {@code Reader} as input.  Fewer characters
   * may be written if the reader has fewer available.  The given buffer is useful in repeated 
   * {@code write} invocations to avoid unnecessary memory allocation.  The default implementation 
   * repeatedly fills the given buffer via a {@link Reader#read(char[], int, int)} operation, then 
   * writes it via {@link Writer#write(char[], int, int)}.  Subclasses that do not require an external 
   * buffer should override this method.
   * 
   * @param r  A reader to be read from
   * @param chars  The number of characters to write
   * @param buffer  A buffer used to copy characters from the reader to this writer.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int write(Reader r, int chars, char[] buffer) throws IOException {
    return IOUtil.doWriteFromReader(r, this, chars, buffer);
  }
  
  /**
   * Write the full contents of the given {@code Reader} to this writer.  The method will block 
   * until an end-of-file is reached.  The default implementation invokes 
   * {@link #writeAll(Reader, int)} with {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the 
   * size of this reader's remaining contents, or that do not rely on a buffer in 
   * {@link #writeAll(Reader, char[])}, should override this method.
   * 
   * @param r  A reader to be read from
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   */
  public int writeAll(Reader r) throws IOException { 
    return writeAll(r, DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Write the full contents of the given {@code Reader} to this writer.  The method will block 
   * until an end-of-file is reached.  The default implementation invokes 
   * {@link #writeAll(Reader, char[])} with a newly-allocated array of the given size.  Subclasses 
   * that do not rely on a buffer in {@link #writeAll(Reader, char[])} should override this method.
   * 
   * @param r  A reader to be read from
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance on large readers
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int writeAll(Reader r, int bufferSize) throws IOException { 
    return writeAll(r, new char[bufferSize]);
  }

  /**
   * Write the full contents of the given {@code Reader} to this writer.  The given buffer is useful 
   * in repeated {@code writeAll} invocations to avoid unnecessary memory allocation.  The method will 
   * block until an end-of-file is reached.  The default implementation repeatedly fills the given buffer 
   * via a {@link Reader#read(char[])} operation, then writes it via {@link Writer#write(char[])}.  
   * Subclasses that do not require an external buffer should override this method.
   * 
   * @param r  A reader to be read from
   * @param buffer  A buffer used to copy characters from the reader to this writer.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters written 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int writeAll(Reader r, char[] buffer) throws IOException {
    return IOUtil.doCopyReader(r, this, buffer);
  }
  
}
