package edu.rice.cs.plt.debug;

import java.util.Date;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A log that triggers an assertion failure whenever it is written to.  If necessary, a filter can be used so that
 * only logging that occurs in a certain location or thread leads to a failure.  (If assertions are disabled, no 
 * failures will occur, and this degenerates into a slightly more-expensive {@link VoidLog}.)
 */
public class AssertEmptyLog extends AbstractLog {
  
  public AssertEmptyLog() { super(); }
  
  public AssertEmptyLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
  }

  /** Trigger an assertion failure with a descriptive message */
  protected void write(Date time, Thread thread, StackTraceElement location, SizedIterable<? extends String> messages) {
    assert false : makeMessage(time, thread, location, messages);
  }
  
  private String makeMessage(Date time, Thread thread, StackTraceElement location, 
                             SizedIterable<? extends String> messages) {
    String first = "[" + formatLocation(location) + " - " + formatThread(thread) + " - " + formatTime(time) + "]";
    return IterUtil.multilineToString(IterUtil.compose(first, messages));
  }
  
  /** Do nothing */
  protected void push() {}
  
  /** Do nothing */
  protected void pop() {}

}
