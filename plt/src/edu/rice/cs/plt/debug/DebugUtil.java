package edu.rice.cs.plt.debug;

import edu.rice.cs.plt.lambda.Predicate2;

/** A collection of utility fields and methods to facilitate code-embedded debugging and logging */
public final class DebugUtil {
  
  /** Prevents instance creation */
  private DebugUtil() {}
  
  /**
   * A globally-accessible debugging log, declared in the spirit of {@link System#out}.  This log is intended to 
   * record information that would be useful in a debugging session, but that is too detailed for typical program 
   * executions.  By default, its value is a {@link VoidLog}; logging behavior may be changed by setting this 
   * field (it is declared {@code volatile} so that changes take immediate effect in all threads).
   */
  public static volatile Log debug = VoidLog.INSTANCE;

  /**
   * A globally-accessible error log, declared in the spirit of {@link System#out}.  This log is intended to 
   * record errors that should be noted, but that do not require the propagation of an exception.  By default, 
   * its value is a {@link VoidLog}; logging behavior may be changed by setting this field (it is declared 
   * {@code volatile} so that changes take immediate effect in all threads).
   */
  public static volatile Log error = VoidLog.INSTANCE;
  
  /**
   * Create a filter for use with logging that will reject all calls made from a location starting with
   * one of the given prefixes (that is, {@code (className + "." + methodName).startsWith(prefix)}).
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and2(Predicate2, Predicate2)},
   * etc.
   */
  public static Predicate2<Thread, StackTraceElement> blackListLocationFilter(final String... prefixes) {
    return new Predicate2<Thread, StackTraceElement>() {
      public Boolean value(Thread thread, StackTraceElement location) {
        String caller = location.getClassName() + "." + location.getMethodName();
        for (String pre : prefixes) {
          if (caller.startsWith(pre)) { return false; }
        }
        return true;
      }
    };
  }
  
  /**
   * Create a filter for use with logging that will reject all calls <emph>not</emph> made from a location starting 
   * with one of the given prefixes (that is, {@code (className + "." + methodName).startsWith(prefix)}).
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and2(Predicate2, Predicate2)},
   * etc.
   */
  public static Predicate2<Thread, StackTraceElement> whiteListLocationFilter(final String... prefixes) {
    return new Predicate2<Thread, StackTraceElement>() {
      public Boolean value(Thread thread, StackTraceElement location) {
        String caller = location.getClassName() + "." + location.getMethodName();
        for (String pre : prefixes) {
          if (caller.startsWith(pre)) { return true; }
        }
        return false;
      }
    };
  }
  
  /**
   * Create a filter for use with logging that will reject all calls made from the given thread(s).
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and2(Predicate2, Predicate2)},
   * etc.
   */
  public static Predicate2<Thread, StackTraceElement> blackListThreadFilter(final Thread... threads) {
    return new Predicate2<Thread, StackTraceElement>() {
      public Boolean value(Thread thread, StackTraceElement location) {
        for (Thread t : threads) {
          if (thread.equals(t)) { return false; }
        }
        return true;
      }
    };
  }
  
  /**
   * Create a filter for use with logging that will reject all calls <emph>not</emph> made from the given thread(s).
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and2(Predicate2, Predicate2)},
   * etc.
   */
  public static Predicate2<Thread, StackTraceElement> whiteListThreadFilter(final Thread... threads) {
    return new Predicate2<Thread, StackTraceElement>() {
      public Boolean value(Thread thread, StackTraceElement location) {
        for (Thread t : threads) {
          if (thread.equals(t)) { return true; }
        }
        return false;
      }
    };
  }
  
  
  /**
   * An alternative to the built-in {@code assert} statement that treats the assertion as an expression
   * rather than a statement.  If assertions are enabled and the argument is {@code false}, this method will fail;
   * in any case, the value of the argument is returned.  This allows code to be conditionally
   * executed when assertions are disabled.  For example: {@code if (DebugUtil.check(x != null)) x.useMethod()}.
   * (Of course, unlike built-in assertions, calls to {@code check()} will <em>always</em> be executed, with
   * their associated overhead, whether assertions are enabled or not.)
   */
  public static boolean check(boolean assertion) {
    assert assertion;
    return assertion;
  }
  
  /**
   * Get the stack trace element representing the method immediately preceding the current method
   * on the stack.  This gives methods the (dangerous, but useful for debugging) ability to "see"
   * who is calling them.
   */
  public static StackTraceElement getCaller() {
    // We could use Thread.getStackTrace(), but it's new in Java 5.
    // Index 0 is this location; index 1 is the caller; index 2 is the caller's caller
    try { return new Throwable().getStackTrace()[2]; }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalStateException("Stack trace information for caller is not available");
    }
  }
  
}
