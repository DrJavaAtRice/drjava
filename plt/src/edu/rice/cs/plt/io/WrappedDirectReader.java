package edu.rice.cs.plt.io;

import java.io.*;
import java.nio.CharBuffer;

/**
 * An implementation of {@code DirectReader} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code Reader}.
 */
public class WrappedDirectReader extends DirectReader {
  private Reader _reader;
  
  public WrappedDirectReader(Reader reader) { _reader = reader; }
  
  @Override public void close() throws IOException { _reader.close(); }
  
  @Override public void mark(int readAheadLimit) throws IOException { _reader.mark(readAheadLimit); }
  
  @Override public boolean markSupported() { return _reader.markSupported(); }
  
  @Override public int read() throws IOException { return _reader.read(); }
  
  @Override public int read(char[] cbuf) throws IOException { return _reader.read(cbuf); }
  
  @Override public int read(char[] cbuf, int offset, int length) throws IOException {
    return _reader.read(cbuf, offset, length);
  }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
  //@Override public int read(CharBuffer target) throws IOException { return _reader.read(target); }
  
  @Override public boolean ready() throws IOException { return _reader.ready(); }
  
  @Override public void reset() throws IOException { _reader.reset(); }
  
  @Override public long skip(long n) throws IOException { return _reader.skip(n); }

  /** 
   * If the input is a {@code DirectReader}, cast it; otherwise, create a
   * {@code WrappedDirectReader}.
   */
  public static DirectReader makeDirect(Reader reader) {
    if (reader instanceof DirectReader) { return (DirectReader) reader; }
    else { return new WrappedDirectReader(reader); }
  }

}
