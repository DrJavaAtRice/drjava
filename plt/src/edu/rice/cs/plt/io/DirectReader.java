package edu.rice.cs.plt.io;

import java.io.*;

/**
 * A {@code Reader} that supports reading directly into a {@code Writer}.  This class
 * provides default implementations defined in terms of {@code Reader} and {@code Writer}
 * methods.  Subclasses can override (at least) {@link #readAll(Writer, char[])} and
 * {@link #read(Writer, int, char[])} to provide better implementations (by, for example,
 * not invoking {@link Reader#read(char[])}).
 * 
 * @see DirectInputStream
 * @see DirectWriter
 * @see DirectOutputStream
 */
public abstract class DirectReader extends Reader {
  
  protected static final int DEFAULT_BUFFER_SIZE = 1024;

  /**
   * Read some number of characters from this reader, sending them to the provided {@code Writer}.
   * The default implementation invokes {@link #read(Writer, int, int)} with the minimum of 
   * {@code chars} and {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the size of this reader's 
   * remaining contents, or that do not rely on a buffer in {@link #read(Writer, int, char[])}, 
   * should override this method.
   * 
   * @param w  A writer to be written to
   * @param chars  The number of characters to read
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read
   * @throws IOException  If an error occurs during reading or writing
   */
  public int read(Writer w, int chars) throws IOException {
    return read(w, chars, (chars < DEFAULT_BUFFER_SIZE) ? chars : DEFAULT_BUFFER_SIZE);
  }
  
  /**
   * Read some number of characters from this reader, sending them to the provided {@code Writer}.
   * The default implementation invokes {@link #read(Writer, int, char[])} with a newly-allocated array 
   * of the given size.  Subclasses that do not rely on a buffer in {@link #read(Writer, int, char[])}
   * should override this method.
   * 
   * @param w  A writer to be written to
   * @param chars  The number of characters to read
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance of large readers
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int read(Writer w, int chars, int bufferSize) throws IOException {
    return read(w, chars, new char[bufferSize]);
  }
  
  /**
   * Read some number of characters from this reader, sending them to the provided {@code Writer}.
   * The given buffer is useful in repeated {@code read} invocations to avoid unnecessary
   * memory allocation.  The default implementation repeatedly fills the given buffer via a 
   * {@link Reader#read(char[], int, int)} operation, then writes it via 
   * {@link Writer#write(char[], int, int)}.  Subclasses that do not require an external buffer 
   * should override this method.
   * 
   * @param w  A writer to be written to
   * @param chars  The number of characters to read
   * @param buffer  A buffer used to copy characters from this reader to the writer.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int read(Writer w, int chars, char[] buffer) throws IOException {
    return IOUtil.doWriteFromReader(this, w, chars, buffer);
  }
  
  /**
   * Read the full contents of this reader, sending the characters to the provided {@code Writer}.
   * The method will block until an end-of-file is reached.  The default implementation invokes 
   * {@link #readAll(Writer, int)} with {@link #DEFAULT_BUFFER_SIZE}.  Subclasses that know the 
   * size of this reader's remaining contents, or that do not rely on a buffer in 
   * {@link #readAll(Writer, char[])}, should override this method.
   * 
   * @param w  A writer to be written to
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   */
  public int readAll(Writer w) throws IOException { return readAll(w, DEFAULT_BUFFER_SIZE); }
  
  /**
   * Read the full contents of this reader, sending the characters to the provided {@code Writer}.
   * The method will block until an end-of-file is reached.  The default implementation invokes 
   * {@link #readAll(Writer, char[])} with a newly-allocated array of the given size.  Subclasses 
   * that do not rely on a buffer in {@link #readAll(Writer, char[])} should override this method.
   * 
   * @param w  A writer to be written to
   * @param bufferSize  The size of buffer to use (if necessary).  Smaller values may reduce the amount of
   *                    memory required; larger values may increase performance of large readers
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code bufferSize <= 0}
   */
  public int readAll(Writer w, int bufferSize) throws IOException { return readAll(w, new char[bufferSize]); }

  /**
   * Read the full contents of this reader, sending the characters to the provided {@code Writer}.
   * The given buffer is useful in repeated {@code readAll} invocations to avoid unnecessary
   * memory allocation.  The method will block until an end-of-file is reached.  The default 
   * implementation repeatedly fills the given buffer via a {@link Reader#read(char[])} operation, 
   * then writes it via {@link Writer#write(char[])}.  Subclasses that do not require an external buffer 
   * should override this method.
   * 
   * @param w  A writer to be written to
   * @param buffer  A buffer used to copy characters from this reader to the writer.  Note that this is only
   *                used to avoid unnecessary memory allocation.  No assumptions are made about the buffer's
   *                contents (which may be overwritten), and no assumptions should be made about the contents
   *                of the buffer after the method invocation.
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  public int readAll(Writer w, char[] buffer) throws IOException {
    return IOUtil.doCopyReader(this, w, buffer);
  }  
  
}
