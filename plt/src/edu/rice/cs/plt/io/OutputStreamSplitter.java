package edu.rice.cs.plt.io;

import java.io.OutputStream;
import java.io.IOException;
import edu.rice.cs.plt.iter.IterUtil;

/** An output stream that allows sending the same data to an arbitrary number of streams. */
public class OutputStreamSplitter extends DirectOutputStream {
  
  private final Iterable<? extends OutputStream> _streams;
  
  public OutputStreamSplitter(OutputStream... streams) { _streams = IterUtil.asIterable(streams); }
  
  public OutputStreamSplitter(Iterable<? extends OutputStream> streams) { _streams = streams; }
  
  @Override public void close() throws IOException {
    for (OutputStream s : _streams) { s.close(); }
  }
  
  @Override public void flush() throws IOException {
    for (OutputStream s : _streams) { s.flush(); }
  }
  
  @Override public void write(byte[] bytes) throws IOException {
    for (OutputStream s : _streams) { s.write(bytes); }
  }
  
  @Override public void write(byte[] bytes, int off, int len) throws IOException {
    for (OutputStream s : _streams) { s.write(bytes, off, len); }
  }
  
  @Override public void write(int b) throws IOException {
    for (OutputStream s : _streams) { s.write(b); }
  }
  
}
