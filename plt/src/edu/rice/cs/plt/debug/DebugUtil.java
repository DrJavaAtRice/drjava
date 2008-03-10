/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.debug;

import java.io.File;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.reflect.ReflectException;

/** A collection of utility fields and methods to facilitate code-embedded debugging and logging */
public final class DebugUtil {
  
  /** Prevents instance creation */
  private DebugUtil() {}
  
  /**
   * A globally-accessible debugging log, declared in the spirit of {@link System#out}.  This log is intended to 
   * record information that would be useful in a debugging session, but that is too detailed for typical program 
   * executions.  By default, its value is a {@link VoidLog}; logging behavior may be changed by setting the system
   * property {@code plt.debug.log}, or by directly setting this field (it is declared {@code volatile} so that 
   * changes take immediate effect in all threads).
   * 
   * @see #initializeLogs
   */
  public static volatile Log debug;

  /**
   * A globally-accessible error log, declared in the spirit of {@link System#out}.  This log is intended to 
   * record errors that should be noted, but that do not require the propagation of an exception.  By default, 
   * its value is a {@link VoidLog}; logging behavior may be changed by setting the system
   * property {@code plt.error.log}, or by directly setting this field (it is declared 
   * {@code volatile} so that changes take immediate effect in all threads).
   * 
   * @see #initializeLogs
   */
  public static volatile Log error;
  
  private static final File DEFAULT_DEBUG_FILE = new File("debug-log.txt");
  private static final File DEFAULT_ERROR_FILE = new File("error-log.txt");
  
  static { initializeLogs(); }
  
  /**
   * <p>Initialize the logs based on the settings of system properties {@code plt.debug.log} and {@code plt.error.log}.
   * The following property values, describing log types, are supported:
   * <ul>
   * <li>{@code System.out}: A {@link SystemOutLog}</li>
   * <li>{@code stdout}: An alias for {@code System.out}</li>
   * <li>{@code System.err}: A {@link SystemErrLog}</li>
   * <li>{@code stderr}: An alias for {@code System.err}</li>
   * <li>{@code file}: A {@link FileLog}, by default writing to {@code debug-log.txt} or 
   *     {@code error-log.txt}; specific files can be specified, as well: {@code file:my-log.txt}.  A working
   *     directory can be set with {@code plt.log.working.dir}.</li>
   * <li>{@code assert}: An {@link AssertEmptyLog}</li>
   * <li>{@code popup}: A {@link PopupLog}</li>
   * <li>{@code tree}: A {@link TreeLog}</li>
   * <li>{@code void}: A {@link VoidLog}</li>
   * </ul>
   * The property may also be a comma-delimited list of types, for which a {@link LogSplitter} will be created.
   * If the system property for a log is not set or is unrecognized, a {@code VoidLog} is used.</p>
   * 
   * <p>The property {@code plt.log.factory} may also be used to override or extend the default set of properties.
   * The property must be the name of a static method taking two {@code String} arguments: a {@code type},
   * which is a name such as those listed above, and a {@code tag}, which is one of {@code "Debug"} or 
   * {@code "Error"}.  Comma-delimited lists will be processed before invoking the method.
   * The return value should be a {@code Log}.  The method can delegate to the default behavior by returning 
   * {@code null}.</p>
   * 
   * <p>This method is run automatically when the {@code DebugUtil} class is loaded.  If desired, it may be
   * re-invoked at any time.</p>
   */
  public static void initializeLogs() {
    String debugProp = System.getProperty("plt.debug.log");
    debug = (debugProp == null) ? VoidLog.INSTANCE : makeLog(debugProp, "Debug");
    String errorProp = System.getProperty("plt.error.log");
    error = (errorProp == null) ? VoidLog.INSTANCE : makeLog(errorProp, "Error");
  }
  
  /** Produce a log corresponding to the given type string. */
  private static Log makeLog(String type, String tag) {
    if (type != null && TextUtil.contains(type, ',')) {
      String[] types = type.split(",");
      if (types.length == 0) { return makeLog(null, tag); }
      else {
        Log[] logs = new Log[types.length];
        for (int i = 0; i < logs.length; i++) { logs[i] = makeLog(types[i], tag); }
        return new LogSplitter(logs);
      }
    }
    else {
      Log result = null;
      String factoryName = System.getProperty("plt.log.factory");
      if (factoryName != null) {
        int dot = factoryName.lastIndexOf('.');
        if (dot >= 0) {
          String className = factoryName.substring(0, dot);
          String methodName = factoryName.substring(dot+1);
          try { result = (Log) ReflectUtil.invokeStaticMethod(className, methodName); }
          catch (ReflectException e) {
            System.err.println("Unable to invoke plt.log.factory: " + e.getCause());
          }
          catch (ClassCastException e) {
            System.err.println("Unable to invoke plt.log.factory: " + e);
          }
        }
      }
      if (result == null) {
        if (type.equals("void")) { result = VoidLog.INSTANCE; }
        else if (type.equals("System.out")) { result = new SystemOutLog(); }
        else if (type.equals("stdout")) { result = new SystemOutLog(); }
        else if (type.equals("System.err")) { result = new SystemErrLog(); }
        else if (type.equals("stderr")) { result = new SystemErrLog(); }
        else if (type.equals("file")) { result = makeFileLog(tag.toLowerCase() + "-log.txt"); }
        else if (type.startsWith("file:")) { result = makeFileLog(type.substring(5)); }
        else if (type.equals("assert")) { result = new AssertEmptyLog(); }
        else if (type.equals("popup")) { result = new PopupLog(tag + " Log"); }
        else if (type.equals("tree")) { result = new TreeLog(tag + " Log"); }
        else { result = VoidLog.INSTANCE; }
      }
      return result;
    }
  }
  
  private static Log makeFileLog(String name) {
    String workingDir = System.getProperty("plt.log.working.dir");
    if (workingDir == null) { return new FileLog(new File(name)); }
    else { return new FileLog(new File(workingDir, name)); }
  }
  
  /**
   * Create a filter for use with logging that will reject all calls made from a location starting with
   * one of the given prefixes (that is, {@code (className + "." + methodName).startsWith(prefix)}).
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and(Predicate2, Predicate2)},
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
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and(Predicate2, Predicate2)},
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
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and(Predicate2, Predicate2)},
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
   * More complex filters may be produced using {@link edu.rice.cs.plt.lambda.LambdaUtil#and(Predicate2, Predicate2)},
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
