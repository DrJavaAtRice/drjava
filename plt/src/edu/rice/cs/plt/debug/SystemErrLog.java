package edu.rice.cs.plt.debug;

import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.Date;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;

/** 
 * A log that writes tagged, indented text to {@link System#err}.  If needed, log messages coming from a certain
 * thread or code location may be ignored by providing a filter predicate.
 */
public class SystemErrLog extends TextLog {
  
  /** Create a log to {@code System.err} without filtering */
  public SystemErrLog() { super(); }
  
  /** Create a log to {@code System.err} with the given filter */
  public SystemErrLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
  }
  
  protected synchronized void write(Date time, Thread thread, StackTraceElement location, 
                                    SizedIterable<? extends String> messages) {
    // We create the writer on each invocation so that we can reflect changes to System.err (via System.setErr())
    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.err));
    writeText(w, time, thread, location, messages);
  }
  
}
