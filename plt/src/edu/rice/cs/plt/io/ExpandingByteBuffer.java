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
import java.io.InterruptedIOException;

/**
 * <p>A byte buffer of arbitrary size to be used with InputStreams and OutputStreams.  The
 * buffer is a FIFO queue of bytes.  It provides a {@link DirectOutputStream} for adding
 * bytes to the end and a {@link DirectInputStream} for pulling bytes from the front.
 * This allows behavior similar to that of a {@link java.io.PipedWriter} and {@link java.io.PipedReader},
 * but without any assumptions about access from different threads, without any restrictions on
 * the size of the buffer (so writing will never block), and <em>with</em> support for multiple
 * readers or writers connected to the same source.  (If access is restricted to a single thread, 
 * care must be taken to never read when the buffer is empty.)</p>
 * 
 * <p>While an attempt at thread safety has been made, at least one exception is evident:
 * if the result of {@code outputStream()} attempts to write from the result of {@code inputStream()},
 * and the inputStream blocks, a write from another thread will be necessary to unblock the inputStream.
 * At that point, the original {@code write()} will have already instructed the inputStream to copy
 * its data into an incorrect location.  In general, connecting a inputStream and a outputStream from the same
 * buffer is not recommended.</p>
 */
public class ExpandingByteBuffer extends ExpandingBuffer<byte[]> {

  /** True iff {@link #end()} has been invoked */
  private boolean _eof;
  
  public ExpandingByteBuffer() {
    super();
    _eof = false;
  }
  
  /**
   * Place an "end of file" at the end of the buffer.  No further writes will be allowed,
   * and when the buffer is emptied, reads will see an end of file.
   */
  public synchronized void end() { _eof = true; notifyAll(); }
  
  public synchronized boolean isEnded() { return _eof; }
  
  protected byte[] allocateBuffer(int size) { return new byte[size]; }
  
  /**
   * Create an output stream providing write access to the buffer.  Invocations of {@code write} will 
   * atomically add bytes directly to the buffer.  {@link OutputStream#close()} will have no effect.
   */
  public DirectOutputStream outputStream() {
    return new DirectOutputStream() {
      public void close() {}
      
      public void flush() {}
      
      @Override public void write(int b) throws IOException {
        synchronized (ExpandingByteBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          allocate();
          lastBuffer()[lastIndex()] = (byte) b;
          recordWrite(1);
          ExpandingByteBuffer.this.notifyAll();
        }
      }
      
      @Override public void write(byte[] bbuf) throws IOException { write(bbuf, 0, bbuf.length); }
      
      @Override public void write(byte[] bbuf, int off, int bytes) throws IOException {
        synchronized (ExpandingByteBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          while (bytes > 0) {
            int space = allocate();
            int toWrite = (space > bytes) ? bytes : space;
            System.arraycopy(bbuf, off, lastBuffer(), lastIndex(), toWrite);
            recordWrite(toWrite);
            bytes -= toWrite;
          }
          ExpandingByteBuffer.this.notifyAll();
        }
      }
      
      @Override public int write(InputStream in, int bytes) throws IOException {
        if (bytes == 0) { return 0; }
        synchronized (ExpandingByteBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          int bytesRead = 0;
          int totalRead = 0;
          while (bytes > 0 && bytesRead >= 0) {
            int space = allocate();
            bytesRead = in.read(lastBuffer(), lastIndex(), space);
            if (bytesRead >= 0) {
              recordWrite(bytesRead);
              bytes -= bytesRead;
              totalRead += bytesRead;
            }
          }
          ExpandingByteBuffer.this.notifyAll();
          if (totalRead == 0) { return -1; }
          else { return totalRead; }
        }
      }
      
      @Override public int write(InputStream in, int bytes, int bufferSize) throws IOException {
        return write(in, bytes);
      }
      
      @Override public int write(InputStream in, int bytes, byte[] buffer) throws IOException {
        return write(in, bytes);
      }
      
      @Override public int writeAll(InputStream in) throws IOException {
        synchronized (ExpandingByteBuffer.this) {
          int bytesRead;
          long totalRead = 0;
          do {
            int space = allocate();
            bytesRead = in.read(lastBuffer(), lastIndex(), space);
            if (bytesRead >= 0) {
              recordWrite(bytesRead);
              totalRead += bytesRead;
            }
          } while (bytesRead >= 0);
          ExpandingByteBuffer.this.notifyAll();
          
          if (totalRead == 0) { return -1; }
          else if (totalRead > Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
          else { return (int) totalRead; }
        }
      }
      
      @Override public int writeAll(InputStream in, int bufferSize) throws IOException {
        return writeAll(in);
      }
      
      @Override public int writeAll(InputStream in, byte[] buffer) throws IOException {
        return writeAll(in);
      }
      
    };
  }
  
  
  /**
   * Create an input stream providing read access to the buffer.  Invocations of {@code read} will 
   * atomically remove bytes from the buffer.  {@link InputStream#close()} will have no effect.
   */
  public DirectInputStream inputStream() {
    return new DirectInputStream() {
      @Override public void close() {}
      
      @Override public int available() {
        long result = size();
        return result < Integer.MAX_VALUE ? (int) result : Integer.MAX_VALUE;
      }

      @Override public int read() throws IOException {
        synchronized (ExpandingByteBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            byte result = firstBuffer()[firstIndex()];
            recordRead(1);
            deallocate();
            return result;
          }
        }
      }
      
      @Override public int read(byte[] bbuf) throws IOException { return read(bbuf, 0, bbuf.length); }
      
      @Override public int read(byte[] bbuf, int offset, int bytes) throws IOException {
        if (bytes <= 0) { return 0; }
        synchronized (ExpandingByteBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            int totalRead = 0;
            while (bytes > 0 && !isEmpty()) {
              int inFirstBuffer = elementsInFirstBuffer();
              int toRead = (inFirstBuffer > bytes) ? bytes : inFirstBuffer;
              System.arraycopy(firstBuffer(), firstIndex(), bbuf, offset, toRead);
              recordRead(toRead);
              bytes -= toRead;
              totalRead += toRead;
              deallocate();
            }
            return totalRead;
          }
        }
      }
      
      @Override public int read(OutputStream out, int bytes) throws IOException {
        if (bytes <= 0) { return 0; }
        synchronized (ExpandingByteBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            int totalRead = 0;
            while (bytes > 0 && !isEmpty()) {
              int inFirstBuffer = elementsInFirstBuffer();
              int toRead = (inFirstBuffer > bytes) ? bytes : inFirstBuffer;
              out.write(firstBuffer(), firstIndex(), toRead);
              recordRead(toRead);
              bytes -= toRead;
              totalRead += toRead;
              deallocate();
            }
            return totalRead;
          }
        }
      }
      
      @Override public int read(OutputStream out, int bytes, int bufferSize) throws IOException {
        return read(out, bytes);
      }
      
      @Override public int read(OutputStream out, int bytes, byte[] buffer) throws IOException {
        return read(out, bytes);
      }
      
      @Override public int readAll(OutputStream out) throws IOException {
        synchronized (ExpandingByteBuffer.this) {
          long totalRead = 0;
          do {
            waitForInput();
            while (!isEmpty()) {
              int toRead = elementsInFirstBuffer();
              out.write(firstBuffer(), firstIndex(), toRead);
              recordRead(toRead);
              totalRead += toRead;
              deallocate();
            }
          } while (!_eof);
          
          if (totalRead == 0) { return -1; }
          else if (totalRead > Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
          else { return (int) totalRead; }
        }
      }
      
      @Override public int readAll(OutputStream out, int bufferSize) throws IOException {
        return readAll(out);
      }
      
      @Override public int readAll(OutputStream out, byte[] buffer) throws IOException {
        return readAll(out);
      }

      @Override public long skip(long bytes) throws IOException {
        if (bytes <= 0) { return 0; }
        synchronized (ExpandingByteBuffer.this) {
          waitForInput();
          long size = size();
          if (bytes > size) { bytes = size; }
          recordRead(bytes);
          while (deallocate()) {}
          return bytes;
        }
      }
      
      /**
       * Guarantees that either {@code !isEmpty()} or {@code _eof} (or both) holds.  If 
       * neither is true, the thread will wait until it becomes true.  Assumes that calling code
       * is synchronized on the {@code ExpandingByteBuffer} object. 
       * @throws InterruptedIOException  If this thread is interrupted while waiting for input
       */
      private void waitForInput() throws InterruptedIOException {
        while (!_eof && isEmpty()) {
          try { ExpandingByteBuffer.this.wait(); }
          catch (InterruptedException e) { throw new InterruptedIOException(); }
        }
      }

    };
  }

}
