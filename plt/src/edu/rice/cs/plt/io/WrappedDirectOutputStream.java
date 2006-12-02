package edu.rice.cs.plt.io;

import java.io.*;

/**
 * An implementation of {@code DirectOutputStream} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code OutputStream}.
 */
public class WrappedDirectOutputStream extends DirectOutputStream {
  private OutputStream _stream;
  
  public WrappedDirectOutputStream(OutputStream stream) { _stream = stream; }
  
  @Override public void close() throws IOException { _stream.close(); }
  
  @Override public void flush() throws IOException { _stream.flush(); }
  
  @Override public void write(int b) throws IOException { _stream.write(b); }
  
  @Override public void write(byte[] bbuf) throws IOException { _stream.write(bbuf); }
  
  @Override public void write(byte[] bbuf, int offset, int length) throws IOException {
    _stream.write(bbuf, offset, length);
  }
  
  /** 
   * If the input is a {@code DirectOutputStream}, cast it; otherwise, create a
   * {@code WrappedDirectOutputStream}.
   */
  public static DirectOutputStream makeDirect(OutputStream stream) {
    if (stream instanceof DirectOutputStream) { return (DirectOutputStream) stream; }
    else { return new WrappedDirectOutputStream(stream); }
  }

}
