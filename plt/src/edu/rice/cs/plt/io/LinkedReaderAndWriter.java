package edu.rice.cs.plt.io;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

/**
 * Coordinates reading and writing from the same source.  Insures
 * that the given Writer will never write past the location of the
 * Reader, potentially corrupting its contents.  When the Writer needs
 * to write more characters than have been read, a buffer is used to
 * store the characters in between.  In order to take advantage of these
 * linking features, all interaction with the user-provided Reader and Writer
 * must subsequently take place via the results of {@link #reader()} and {@link #writer()}, 
 * respectively.
 */
public class LinkedReaderAndWriter {
  
  private final DirectReader _linkedReader;
  private final DirectWriter _linkedWriter;
  private long _readIndex; // Must always be >= _writeIndex
  private boolean _eof;
  private long _writeIndex;
  
  /**
   * Link {@code r} and {@code w}.  Assumes that {@code r} and {@code w} are newly created, or at 
   * least that their underlying cursors point to the same place.  Also assumes that no further 
   * direct interaction with {@code r} or {@code w} will occur.
   */
  public LinkedReaderAndWriter(final Reader r, final Writer w) {
    _readIndex = 0;
    _writeIndex = 0;
    _eof = false;
    final ExpandingCharBuffer buffer = new ExpandingCharBuffer();
    final Reader fromBuffer = buffer.reader();
    final DirectWriter toBuffer = buffer.writer();
    
    _linkedReader = new DirectReader() {
      public int read(char[] cbuf, int offset, int chars) throws IOException {
        int read = 0;
        
        if (!buffer.isEmpty()) {
          int bufferReadResult = fromBuffer.read(cbuf, offset, chars);
          if (bufferReadResult < 0) {
            throw new IllegalStateException("Unexpected negative result from ExpandingCharBuffer read");
          }
          else if (bufferReadResult > 0) { read += bufferReadResult; chars -= bufferReadResult; }
        }
        
        if (buffer.isEmpty() && chars >= 0) {
          int readResult = r.read(cbuf, offset + read, chars);
          if (readResult < 0) {
            _eof = true;
            return read > 0 ? read : readResult;
          }
          else {
            read += readResult;
            chars -= readResult;
            _readIndex += readResult;
          }
        }
        return read;
      }
      
      public void close() throws IOException { r.close(); }
      
      public boolean ready() throws IOException { return r.ready(); }
    };
    
    _linkedWriter = new DirectWriter() {
      public void write(char[] cbuf, int offset, int chars) throws IOException {
        long newIndex = _writeIndex + chars;
        while (newIndex > _readIndex) {
          // If we've reached the eof, we don't try to buffer anything
          if (_eof) { _readIndex = newIndex; }
          else {
            int bufferWriteResult = toBuffer.write(r, (int) (newIndex - _readIndex));
            if (bufferWriteResult < 0) { _eof = true; }
            else { _readIndex += bufferWriteResult; }
          }
        }
        w.write(cbuf, offset, chars);
        _writeIndex = newIndex;
      }
      
      public void close() throws IOException { w.close(); }
      
      public void flush() throws IOException { w.flush(); }
    };
  }
  
  public DirectReader reader() { return _linkedReader; }
  
  public DirectWriter writer() { return _linkedWriter; }
    
}
