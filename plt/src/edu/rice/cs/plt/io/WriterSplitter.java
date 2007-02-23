package edu.rice.cs.plt.io;

import java.io.Writer;
import java.io.IOException;
import edu.rice.cs.plt.iter.IterUtil;

/** A writer that allows sending the same data to an arbitrary number of writers. */
public class WriterSplitter extends DirectWriter {
  
  private final Iterable<? extends Writer> _writers;
  
  public WriterSplitter(Writer... writers) { _writers = IterUtil.asIterable(writers); }
  
  public WriterSplitter(Iterable<? extends Writer> writers) { _writers = writers; }
  
  @Override public void close() throws IOException {
    for (Writer w : _writers) { w.close(); }
  }
  
  @Override public void flush() throws IOException {
    for (Writer w : _writers) { w.flush(); }
  }
  
  @Override public void write(char[] cbuf) throws IOException {
    for (Writer w : _writers) { w.write(cbuf); }
  }
  
  @Override public void write(char[] cbuf, int off, int len) throws IOException {
    for (Writer w : _writers) { w.write(cbuf, off, len); }
  }
  
  @Override public void write(int c) throws IOException {
    for (Writer w : _writers) { w.write(c); }
  }
  
  @Override public void write(String s) throws IOException {
    for (Writer w : _writers) { w.write(s); }
  }
  
  @Override public void write(String s, int off, int len) throws IOException {
    for (Writer w : _writers) { w.write(s, off, len); }
  }
  
}
