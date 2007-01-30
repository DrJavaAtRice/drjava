package edu.rice.cs.plt.debug;

import java.io.OutputStreamWriter;
import edu.rice.cs.plt.lambda.Predicate2;

/** 
 * A log that writes tagged, indented text to {@link System#out}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class SystemOutLog extends WriterLog {
  
  /** Create a log to {@code System.out} without filtering */
  public SystemOutLog() { super(new OutputStreamWriter(System.out)); }
  
  /** Create a log to {@code System.out} with the given filter */
  public SystemOutLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(new OutputStreamWriter(System.out), filter);
  }
  
}
