package edu.rice.cs.plt.io;

/** A stream that discards all data written to it. */
public class VoidOutputStream extends DirectOutputStream {
  public static final VoidOutputStream INSTANCE = new VoidOutputStream();
  protected VoidOutputStream() {} // allow for subclassing, if desired
  @Override public void close() {}
  @Override public void flush() {}
  @Override public void write(byte[] bbuf) {}
  @Override public void write(byte[] bbuf, int offset, int len) {}
  @Override public void write(int b) {}
}
