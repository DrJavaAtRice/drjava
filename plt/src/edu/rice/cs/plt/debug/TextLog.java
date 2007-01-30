package edu.rice.cs.plt.debug;

import java.util.Date;
import java.io.BufferedWriter;
import java.io.IOException;

import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.iter.SizedIterable;

/**
 * An abstract log that records messages as plain text.  This class takes responsibility for converting
 * {@link #write} invocations to blocks of properly-indented and tagged text.  Subclasses are responsible for managing
 * the {@code BufferedWriter}(s) that will be used for output, and for calling {@link #writeText} as appropriate.
 */
public abstract class TextLog extends AbstractLog {
  
  private static final String HANGING_INDENT = "    ";
  
  private final Indenter _indent;
  
  /** Create a log without a filter */
  protected TextLog() {
    super();
    _indent = new Indenter();
  }
  
  /** Create a log using the given filter */
  protected TextLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _indent = new Indenter();
  }
  
  /** 
   * Write a text message based on the given parameters to {@code w}.  At the end of execution,
   * {@link BufferedWriter#newLine} and {@link java.io.Writer#flush} will be invoked.  Subclasses
   * are responsible for invoking this method as needed.
   * @throws WrappedException  If an {@code IOException} occurs during writing
   */
  protected void writeText(BufferedWriter w, Date time, Thread thread, StackTraceElement location, 
                           SizedIterable<? extends String> messages) {
    try {
      w.write(_indent.indentString());
      w.write("[" + formatLocation(location) + " - " + formatThread(thread) + " - " + formatTime(time) + "]");
      w.newLine();
      for (String s : messages) {
        w.write(_indent.indentString());
        w.write(HANGING_INDENT);
        w.write(s);
        w.newLine();
      }
      w.flush();
    }
    catch (IOException e) {
      // Throw an exception, because otherwise the lack of anything in the log will be interpreted as evidence
      // that the calling line of code did not execute.
      throw new WrappedException(e);
    }
  }
  
  protected void push() { _indent.push(); }
  
  protected void pop() { _indent.pop(); }
  
}
