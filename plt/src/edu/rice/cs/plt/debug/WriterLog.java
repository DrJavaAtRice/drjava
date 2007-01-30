package edu.rice.cs.plt.debug;

import java.io.Writer;
import java.io.BufferedWriter;
import java.util.Date;

import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.io.IOUtil;

/** 
 * A log that writes tagged, indented text to the given {@code Writer}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class WriterLog extends TextLog {
  
  private final BufferedWriter _w;
  
  /** Create a writer-based log without filtering */
  public WriterLog(Writer w) {
    super();
    _w = IOUtil.makeBuffered(w);
  }
  
  /** Create a writer-based log with the given filter */
  public WriterLog(Writer w, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _w = IOUtil.makeBuffered(w);
  }
  
  protected synchronized void write(Date time, Thread thread, StackTraceElement location, 
                                    SizedIterable<? extends String> messages) {
    writeText(_w, time, thread, location, messages);
  }
  
}
