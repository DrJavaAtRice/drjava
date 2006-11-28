package edu.rice.cs.plt.io;

import java.io.*;

/**
 * An implementation of {@code DirectWriter} that inherits that class's default implementations
 * and delegates all other operations to the wrapped {@code Writer}.
 */
public class WrappedDirectWriter extends DirectWriter {
  private Writer _writer;
  
  public WrappedDirectWriter(Writer writer) { _writer = writer; }
  
  @Override public void close() throws IOException { _writer.close(); }
  
  @Override public void flush() throws IOException { _writer.flush(); }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
  //@Override public Writer append(char c) throws IOException { _writer.append(c); return this; }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
//  @Override public Writer append(CharSequence csq) throws IOException {
//    _writer.append(csq);
//    return this;
//  }
  
  // Removed for now to preserve compatibility with Java 1.4 APIs.
//  @Override public Writer append(CharSequence csq, int start, int end) throws IOException {
//    _writer.append(csq, start, end);
//    return this;
//  }
  
  @Override public void write(int c) throws IOException { _writer.write(c); }
  
  @Override public void write(char[] cbuf) throws IOException { _writer.write(cbuf); }
  
  @Override public void write(char[] cbuf, int offset, int length) throws IOException {
    _writer.write(cbuf, offset, length);
  }
  
  @Override public void write(String s) throws IOException { _writer.write(s); }
  
  @Override public void write(String s, int offset, int length) throws IOException {
    _writer.write(s, offset, length);
  }

  /** 
   * If the input is a {@code DirectWriter}, cast it; otherwise, create a
   * {@code WrappedDirectWriter}.
   */
  public static DirectWriter makeDirect(Writer writer) {
    if (writer instanceof DirectWriter) { return (DirectWriter) writer; }
    else { return new WrappedDirectWriter(writer); }
  }

}
