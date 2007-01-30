package edu.rice.cs.plt.debug;

import java.io.OutputStreamWriter;
import edu.rice.cs.plt.lambda.Predicate2;

/** 
 * A log that writes tagged, indented text to {@link System#err}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class SystemErrLog extends WriterLog {
  
  /** Create a log to {@code System.err} without filtering */
  public SystemErrLog() { super(new OutputStreamWriter(System.err)); }
  
  /** Create a log to {@code System.err} with the given filter */
  public SystemErrLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(new OutputStreamWriter(System.err), filter);
  }
  
}
