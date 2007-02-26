package edu.rice.cs.plt.debug;

import java.util.Date;
import java.util.Collection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.BinaryMappedIterable;
import edu.rice.cs.plt.iter.SkipFirstIterable;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.recur.RecurUtil;
import edu.rice.cs.plt.recur.RecurUtil.ArrayStringMode;
import edu.rice.cs.plt.text.TextUtil;

/**
 * An abstract implementation of the {@code Log} interface that provides much of the front-end
 * logic for processing logging requests.  Subclasses should be implemented with the following in
 * mind:
 * <ul>
 * <li>Log requests coming from certain threads or code locations may be ignored by invoking the
 * {@code AbstractLog} constructor with a filtering predicate.</li>
 * <li>All log requests are translated into invocations of the class's abstract methods: {@link #write},
 * {@link #push}, and {@link #pop}.  Messages that span multiple lines are broken into lists of strings
 * for simplified handling by the subclass.</li>
 * <li>The following methods are also provided as services to subclasses: {@link #formatTime}, {@link #formatThread},
 * and {@link #formatLocation}.
 * <li>All logs should be thread safe.  Subclasses are responsible for supporting concurrent access to their 
 * resources; the effects of each invocation of {@link #write} should be atomic (this can be trivially accomplished
 * by declaring the {@code write} method {@code synchronized}).</li>
 * </ul>
 */
public abstract class AbstractLog implements Log {
  
  private static final SizedIterable<String> EMPTY_MESSAGE = IterUtil.singleton("");
  private static final SizedIterable<String> START_MESSAGE = IterUtil.singleton("Starting");
  private static final SizedIterable<String> END_MESSAGE = IterUtil.singleton("Ending");
  private static final int IDEAL_LINE_WIDTH = 120;
  private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("H:mm:ss.SSS");
  
  protected final Predicate2<? super Thread, ? super StackTraceElement> _filter;
  
  /** Create a log that performs no filtering */
  protected AbstractLog() {
    _filter = LambdaUtil.TRUE;
  }
  
  /** Create a log that only records messages for which the given filter returns {@code true}. */
  protected AbstractLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    _filter = filter;
  }
  
  public void log() {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) { write(new Date(), th, e, EMPTY_MESSAGE); }
  }
  
  public void log(String message) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) { write(new Date(), th, e, processText(message)); }
  }
  
  /** Record the given throwable's message, stack trace, and cause (which is processed recursively). */
  public void log(Throwable t) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) { write(new Date(), th, e, processThrowable(t)); }
  }
  
  /** Record the given throwable's message, stack trace, and cause (which is processed recursively). */
  public void log(String message, Throwable t) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      SizedIterable<String> messages = IterUtil.compose(processText(message), processThrowable(t));
      write(new Date(), th, e, messages);
    }
  }
  
  /** Perform a {@link #push}, followed by a default starting message */
  public void logStart() {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, START_MESSAGE);
      push();
    }
  }
  
  /** Perform a {@link #push}, followed by the given message */
  public void logStart(String message) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, processText("Start " + message));
      push();
    }
  }
  
  /** Write a default ending message, followed by a {@link #pop} */
  public void logEnd() {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      pop();
      write(new Date(), th, e, END_MESSAGE);
    }
  }
  
  /** Write the given message, followed by a {@link #pop} */
  public void logEnd(String message) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      pop();
      write(new Date(), th, e, processText("End " + message));
    }
  }
  
  public void logStack() {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      SizedIterable<String> messages = IterUtil.compose("Current stack:", processCurrentStack());
      write(new Date(), th, e, messages);
    }
  }
  
  public void logStack(String message) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      SizedIterable<String> messages = IterUtil.compose(processText(message), processCurrentStack());
      write(new Date(), th, e, messages);
    }
  }
  
  /**
   * Record a message like {@code "x: 23"}.  Values are converted to strings using {@link TextUtil#toString}.
   * If the value is an array or an iterable that doesn't fit nicely on a line, each element will be printed on a 
   * separate line.
   */
  public void logValue(String name, Object value) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, processValue(name, value));
    }
  }
  
  public void logValue(String message, String name, Object value) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, IterUtil.compose(processText(message), processValue(name, value)));
    }
  }
  
  /**
   * Record a message containing a line for each name-value pair.  Each line will look like {@code "x: 23"}.  Values
   * are converted to strings using {@link TextUtil#toString}.  If the value is an array or an iterable that doesn't 
   * fit nicely on a line, each element will be printed on a separate line.  If the lengths of the given arrays
   * do not match, an error message will be logged.
   */
  public void logValues(String[] names, Object... values) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, processValues(names, values));
    }
  }
  
  public void logValues(String message, String[] names, Object... values) {
    Thread th = Thread.currentThread();
    StackTraceElement e = DebugUtil.getCaller();
    if (_filter.value(th, e)) {
      write(new Date(), th, e, IterUtil.compose(processText(message), processValues(names, values)));
    }
  }
  

  private static SizedIterable<String> processText(String text) {
    SizedIterable<String> result = TextUtil.getLines(text);
    if (result.size() == 0) { return EMPTY_MESSAGE; }
    else { return result; }
  }
  
  private static SizedIterable<String> processThrowable(Throwable t) {
    return processThrowable(t, false);
  }
  
  private static SizedIterable<String> processThrowable(Throwable t, boolean asCause) {
    if (t == null) { return IterUtil.singleton("null"); }
    SizedIterable<String> result;
    if (asCause) { result = IterUtil.make("", "Caused by " + t, "at"); }
    else { result = IterUtil.make(t.toString(), "at"); }
    result = IterUtil.compose(result, processStack(IterUtil.asIterable(t.getStackTrace())));
    if (t.getCause() != null) {
      result = IterUtil.compose(result, processThrowable(t.getCause()));
    }
    return result;
  }
  
  private static SizedIterable<String> processCurrentStack() {
    StackTraceElement[] stackArray = new Throwable().getStackTrace();
    // Skip two entries: one for this method, and one for the calling log method
    SizedIterable<StackTraceElement> stack = IterUtil.skipFirst(IterUtil.skipFirst(IterUtil.asIterable(stackArray)));
    return processStack(stack);
  }
  
  private static SizedIterable<String> processStack(SizedIterable<StackTraceElement> stack) {
    SizedIterable<String> result = IterUtil.empty();
    for (StackTraceElement e : stack) {
      result = IterUtil.compose(result, e.toString());
    }
    return result;
  }
  
  private static SizedIterable<String> processValue(String name, Object value) {
    SizedIterable<String> valStrings = processText(RecurUtil.safeToString(value));
    if (valStrings.size() > 1 || IterUtil.first(valStrings).length() > IDEAL_LINE_WIDTH) {
      // if this is an array or Iterable, print values on separate lines
      if (value instanceof Iterable<?>) {
        valStrings = processText(IterUtil.multilineToString((Iterable<?>) value));
      }
      // this check is necessary for Retroweaver compatibility -- the Iterable check fails at run time
      // (because java.lang.Iterable becomes com.rc.retroweaver.runtime.Iterable_)
      else if (value instanceof Collection<?>) {
        valStrings = processText(IterUtil.multilineToString((Collection<?>) value));
      }
      else if (value instanceof Object[]) {
        valStrings = processText(RecurUtil.arrayToString((Object[]) value, ArrayStringMode.SHALLOW_MULTILINE));
      }
    }
    
    if (valStrings.size() == 1) {
      return IterUtil.singleton(name + ": " + IterUtil.first(valStrings));
    }
    else {
      return IterUtil.compose(name + ":", valStrings);
    }
  }
  
  private static final Lambda2<String, Object, SizedIterable<String>> PROCESS_VALUE =
    new Lambda2<String, Object, SizedIterable<String>>() {
    public SizedIterable<String> value(String name, Object val) { return processValue(name, val); }
  };
  
  private static SizedIterable<String> processValues(String[] names, Object... values) {
    if (names.length == values.length) {
      Iterable<String> namesIter = IterUtil.asIterable(names);
      Iterable<Object> valuesIter = IterUtil.asIterable(values);
      SizedIterable<SizedIterable<String>> messages = BinaryMappedIterable.make(namesIter, valuesIter, PROCESS_VALUE);
      return IterUtil.collapse(messages);
    }
    else {
      String err = "Invalid invocation of logValues() with " + names.length + " names and "  + values.length + " values";
      return IterUtil.singleton(err);
    }
  }
  
  /** Convert a time to a string of the form {@code "12:23:03.013"} */
  protected String formatTime(Date time) {
    return TIME_FORMATTER.format(time);
  }
  
  /**
   * Convert a thread to a string of the form {@code "Custom Thread@1440523542"}.  The number is an identification
   * code corresponding to the thread's identity hash code.
   */
  protected String formatThread(Thread thread) {
    // Ideally, we should use the thread's ID, but that is only available since Java 5.
    return thread.getName() + " " + System.identityHashCode(thread);
  }
  
  /**
   * Convert a location to a string of the form
   * {@code "edu.rice.cs.plt.debug.AbstractLog.formatLocation(247)"}
   */
  protected String formatLocation(StackTraceElement location) {
    StringBuilder result = new StringBuilder();
    result.append(location.getClassName());
    result.append(".");
    result.append(location.getMethodName());
    result.append("(");
    int line = location.getLineNumber();
    if (line >= 0) { result.append(line); }
    else if (location.isNativeMethod()) { result.append("native"); }
    else { result.append("unknown"); }
    result.append(")");
    return result.toString();
  }
  
  /**
   * Record the given message, which occured at a certain time while running in a certain thread at the given location
   * in the code.  Each element of {@code messages} is a line of text.  Subclasses should ensure that the effects of
   * this method are atomic (at least within the context of concurrent invocations of this method).  This can be 
   * trivially achieved by declaring the method {@code synchronized}.
   */
  protected abstract void write(Date time, Thread thread, StackTraceElement location, SizedIterable<? extends String> messages);

  /** Record the fact that a "start" has been logged */
  protected abstract void push();
  
  /** Record the fact that an "end" has been logged */
  protected abstract void pop();
}
