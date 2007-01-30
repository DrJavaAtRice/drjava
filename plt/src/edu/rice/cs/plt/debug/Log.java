package edu.rice.cs.plt.debug;

/** 
 * A log allows messages and other information to be recorded during program execution.  Implementations should
 * be thread-safe.
 */
public interface Log {
  
  /** Record the current execution point (may include the current time, thread, code location, etc.) */
  public void log();
  
  /** Record the given message */
  public void log(String message);
  
  /** Record the given exception (or other throwable) */
  public void log(Throwable t);
  
  /** Record the given exception (or other throwable) with a descriptive message */
  public void log(String message, Throwable t);
  
  /**
   * Record the beginning of an execution phase.  This is useful, for example, when a method's execution
   * begins.  This call should always be followed by a corresponding invocation of {@link #logEnd()} or 
   * {@link #logEnd(String)}.
   */
  public void logStart();
  
  /**
   * Record the beginning of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution begins.  This call should always be followed by a corresponding invocation of 
   * {@link #logEnd()} or {@link #logEnd(String)}.
   */
  public void logStart(String message);
  
  /**
   * Record the end of an execution phase.  This is useful, for example, when a method's execution ends.  This
   * call should always be preceded by a corresponding invocation of {@link #logStart()} or {@link #logStart(String)}.
   */
  public void logEnd();

  /**
   * Record the end of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution ends.  This call should always be preceded by a corresponding invocation of 
   * {@link #logStart()} or {@link #logStart(String)}.
   */
  public void logEnd(String message);
  
  /** Record the current thread's stack trace */
  public void logStack();
  
  /** Record the current thread's stack trace with a descriptive message */
  public void logStack(String message);
  
  /** Record the name and value of some variable or expression */
  public void logValue(String name, Object value);

  /**
   * Record the names and values of a list of variables or expressions.  The two arrays are assumed
   * to have the same length, although implementations are encouraged to handle violations of this 
   * assumption cleanly by, for example, logging an error message.
   */
  public void logValues(String[] names, Object... values);
}
