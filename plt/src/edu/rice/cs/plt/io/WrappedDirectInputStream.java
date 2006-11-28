package edu.rice.cs.plt.io;

import java.io.*;

/**
 * An implementation of {@code DirectInputStream} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code InputStream}.
 */
public class WrappedDirectInputStream extends DirectInputStream {
  private InputStream _stream;
  
  public WrappedDirectInputStream(InputStream stream) { _stream = stream; }
  
  @Override public int available() throws IOException { return _stream.available(); }
  
  @Override public void close() throws IOException { _stream.close(); }
  
  @Override public void mark(int readAheadLimit) { _stream.mark(readAheadLimit); }
  
  @Override public boolean markSupported() { return _stream.markSupported(); }
  
  @Override public int read() throws IOException { return _stream.read(); }
  
  @Override public int read(byte[] bbuf) throws IOException { return _stream.read(bbuf); }
  
  @Override public int read(byte[] bbuf, int offset, int length) throws IOException {
    return _stream.read(bbuf, offset, length);
  }
  
  @Override public void reset() throws IOException { _stream.reset(); }
  
  @Override public long skip(long n) throws IOException { return _stream.skip(n); }
  
  /** 
   * If the input is a {@code DirectInputStream}, cast it; otherwise, create a
   * {@code WrappedDirectInputStream}.
   */
  public static DirectInputStream makeDirect(InputStream stream) {
    if (stream instanceof DirectInputStream) { return (DirectInputStream) stream; }
    else { return new WrappedDirectInputStream(stream); }
  }

}
