package edu.rice.cs.plt.debug;

import java.util.Date;
import edu.rice.cs.plt.swing.SwingUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.iter.IterUtil;

public class PopupLog extends AbstractLog {
  
  private String _name;
  
  public PopupLog(String name) {
    super();
    _name = name;
  }
  
  public PopupLog(String name, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _name = name;
  }

  /** Create a pop-up dialog with the given message */
  protected void write(Date time, Thread thread, StackTraceElement location, SizedIterable<? extends String> messages) {
    String first = "[" + formatLocation(location) + " - " + formatThread(thread) + " - " + formatTime(time) + "]";
    String text = IterUtil.multilineToString(IterUtil.compose(first, messages));
    SwingUtil.showPopup(_name, text);
  }
  
  /** Do nothing */
  protected void push() {}
  
  /** Do nothing */
  protected void pop() {}

}
