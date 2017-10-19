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

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * <p>A character buffer of arbitrary size to be used with Readers and Writers.  The
 * buffer is a FIFO queue of characters.  It provides a {@link DirectWriter} for adding
 * characters to the end and a {@link DirectReader} for pulling characters from the front.
 * This allows behavior similar to that of a {@link java.io.PipedWriter} and {@link java.io.PipedReader},
 * but without any assumptions about access from different threads, without any restrictions on
 * the size of the buffer (so writing will never block), and <em>with</em> support for multiple
 * readers or writers connected to the same source.  (If access is restricted to a single thread, 
 * care must be taken to never read when the buffer is empty.)</p>
 * 
 * <p>While an attempt at thread safety has been made, at least one exception is evident:
 * if the result of {@code writer()} attempts to write from the result of {@code reader()},
 * and the reader blocks, a write from another thread will be necessary to unblock the reader.
 * At that point, the original {@code write()} will have already instructed the reader to copy
 * its data into an incorrect location.  In general, connecting a reader and a writer from the same
 * buffer is not recommended.</p>
 */
public class ExpandingCharBuffer extends ExpandingBuffer<char[]> {

  /** True iff {@link #end()} has been invoked */
  private boolean _eof;
  
  public ExpandingCharBuffer() {
    super();
    _eof = false;
  }
  
  /**
   * Place an "end of file" at the end of the buffer.  No further writes will be allowed,
   * and when the buffer is emptied, reads will see an end of file.
   */
  public synchronized void end() { _eof = true; notifyAll(); }
  
  public synchronized boolean isEnded() { return _eof; }
  
  protected char[] allocateBuffer(int size) { return new char[size]; }
  
  /**
   * Create a writer providing write access to the buffer.  Invocations of {@code write} will atomically
   * add characters directly to the buffer.  {@link Writer#close()} will have no effect.
   */
  public DirectWriter writer() {
    return new DirectWriter() {
      @Override public void close() {}
      
      @Override public void flush() {}
      
      @Override public void write(int c) throws IOException {
        synchronized (ExpandingCharBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          allocate();
          lastBuffer()[lastIndex()] = (char) c;
          recordWrite(1);
          ExpandingCharBuffer.this.notifyAll();
        }
      }
      
      @Override public void write(char[] cbuf) throws IOException { write(cbuf, 0, cbuf.length); }
      
      @Override public void write(char[] cbuf, int off, int chars) throws IOException {
        if (chars == 0) { return; }
        synchronized (ExpandingCharBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          while (chars > 0) {
            int space = allocate();
            int toWrite = (space > chars) ? chars : space;
            System.arraycopy(cbuf, off, lastBuffer(), lastIndex(), toWrite);
            recordWrite(toWrite);
            chars -= toWrite;
          }
          ExpandingCharBuffer.this.notifyAll();
        }
      }
      
      @Override public int write(Reader r, int chars) throws IOException {
        if (chars == 0) { return 0; }
        synchronized (ExpandingCharBuffer.this) {
          if (_eof) { throw new IOException("Buffer has been ended"); }
          int charsRead = 0;
          int totalRead = 0;
          while (chars > 0 && charsRead >= 0) {
            int space = allocate();
            charsRead = r.read(lastBuffer(), lastIndex(), space);
            if (charsRead >= 0) {
              recordWrite(charsRead);
              chars -= charsRead;
              totalRead += charsRead;
            }
          }
          ExpandingCharBuffer.this.notifyAll();
          if (totalRead == 0) { return -1; }
          else { return totalRead; }
        }
      }
      
      @Override public int write(Reader r, int chars, int bufferSize) throws IOException {
        return write(r, chars);
      }
      
      @Override public int write(Reader r, int chars, char[] buffer) throws IOException {
        return write(r, chars);
      }
      
      @Override public int writeAll(Reader r) throws IOException {
        synchronized (ExpandingCharBuffer.this) {
          int charsRead;
          long totalRead = 0;
          do {
            int space = allocate();
            charsRead = r.read(lastBuffer(), lastIndex(), space);
            if (charsRead >= 0) {
              recordWrite(charsRead);
              totalRead += charsRead;
            }
          } while (charsRead >= 0);
          ExpandingCharBuffer.this.notifyAll();
          
          if (totalRead == 0) { return -1; }
          else if (totalRead > Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
          else { return (int) totalRead; }
        }
      }
      
      @Override public int writeAll(Reader r, int bufferSize) throws IOException {
        return writeAll(r);
      }
      
      @Override public int writeAll(Reader r, char[] buffer) throws IOException {
        return writeAll(r);
      }
      
    };
  }
  
  
  /**
   * Create a reader providing read access to the buffer.  Invocations of {@code read} will atomically
   * remove characters from the buffer.  {@link Reader#close()} will have no effect.
   */
  public DirectReader reader() {
    return new DirectReader() {
      @Override public void close() {}
      
      @Override public boolean ready() { return !isEmpty(); }

      @Override public int read() throws IOException {
        synchronized (ExpandingCharBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            char result = firstBuffer()[firstIndex()];
            recordRead(1);
            deallocate();
            return result;
          }
        }
      }
      
      @Override public int read(char[] cbuf) throws IOException { return read(cbuf, 0, cbuf.length); }
      
      @Override public int read(char[] cbuf, int offset, int chars) throws IOException {
        if (chars <= 0) { return 0; }
        synchronized (ExpandingCharBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            int totalRead = 0;
            while (chars > 0 && !isEmpty()) {
              int inFirstBuffer = elementsInFirstBuffer();
              int toRead = (inFirstBuffer > chars) ? chars : inFirstBuffer;
              System.arraycopy(firstBuffer(), firstIndex(), cbuf, offset, toRead);
              recordRead(toRead);
              chars -= toRead;
              totalRead += toRead;
              deallocate();
            }
            return totalRead;
          }
        }
      }
      
      @Override public int read(Writer w, int chars) throws IOException {
        if (chars <= 0) { return 0; }
        synchronized (ExpandingCharBuffer.this) {
          waitForInput();
          if (isEmpty()) { return -1; }
          else {
            int totalRead = 0;
            while (chars > 0 && !isEmpty()) {
              int inFirstBuffer = elementsInFirstBuffer();
              int toRead = (inFirstBuffer > chars) ? chars : inFirstBuffer;
              w.write(firstBuffer(), firstIndex(), toRead);
              recordRead(toRead);
              chars -= toRead;
              totalRead += toRead;
              deallocate();
            }
            return totalRead;
          }
        }
      }
      
      @Override public int read(Writer w, int chars, int bufferSize) throws IOException {
        return read(w, chars);
      }
      
      @Override public int read(Writer w, int chars, char[] buffer) throws IOException {
        return read(w, chars);
      }
      
      @Override public int readAll(Writer w) throws IOException {
        synchronized (ExpandingCharBuffer.this) {
          long totalRead = 0;
          do {
            waitForInput();
            while (!isEmpty()) {
              int toRead = elementsInFirstBuffer();
              w.write(firstBuffer(), firstIndex(), toRead);
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
      
      @Override public int readAll(Writer w, int bufferSize) throws IOException { return readAll(w); }
      
      @Override public int readAll(Writer w, char[] buffer) throws IOException { return readAll(w); }

      @Override public long skip(long chars) throws IOException {
        if (chars <= 0) { return 0; }
        synchronized (ExpandingCharBuffer.this) {
          waitForInput();
          long size = size();
          if (chars > size) { chars = size; }
          recordRead(chars);
          while (deallocate()) {}
          return chars;
        }
      }
      
      /**
       * Guarantees that either {@code !isEmpty()} or {@code _eof} (or both) holds.  If 
       * neither is true, the thread will wait until it becomes true.  Assumes that calling code
       * is synchronized on the {@code ExpandingCharBuffer} object. 
       * @throws InterruptedIOException  If this thread is interrupted while waiting for input
       */
      private void waitForInput() throws InterruptedIOException {
        while (!_eof && isEmpty()) {
          try { ExpandingCharBuffer.this.wait(); }
          catch (InterruptedException e) { throw new InterruptedIOException(); }
        }
      }

    };
  }

}
