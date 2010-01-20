/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.text.Bracket;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Pair;
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
  
  static { initializeLogs(); }
  
  /**
   * Initialize {@link #debug} and {@link #error} based on the descriptors appearing in system properties
   * {@code plt.debug.log} and {@code plt.error.log}.  This method is run automatically when the
   * {@code DebugUtil} class is loaded.  If desired, it may be re-invoked at any time.
   * @see #makeLog
   * @see #makeLogSink
   */
  public static void initializeLogs() {
    String debugProp = System.getProperty("plt.debug.log");
    debug = (debugProp == null) ? VoidLog.INSTANCE : makeLog(debugProp, "Debug");
    String errorProp = System.getProperty("plt.error.log");
    error = (errorProp == null) ? VoidLog.INSTANCE : makeLog(errorProp, "Error");
  }
  
  /**
   * Produce a Log corresponding to the given descriptor.  If the descriptor is a valid sink descriptor,
   * a {@link StandardLog} is returned; otherwise, a {@link VoidLog} is returned.  ({@code "void"} is a
   * convenient special case of an invalid sink descriptor.)
   * @see #makeLogSink
   */
  public static Log makeLog(String descriptor, String defaultName) {
    LogSink sink = makeLogSink(descriptor, defaultName);
    if (sink == null) { return VoidLog.INSTANCE; }
    else { return new StandardLog(sink); }
  }
  
  /**
   * <p>Produce a LogSink corresponding to the given descriptor.  The descriptor is a sink type, with additional
   * support for asynchronous sinks, string parameters, filters, and splitting.
   * <ul>
   * <li>{@code <descriptor> := <descriptor>, <descriptor> | [~] <single-descriptor> <filter>}</li>
   * <li>{@code <single-descriptor> := <type> [:<parameter>] | (<descriptor>)}</li>  
   * <li>{@code <type> := System.out | stdout | System.err | stderr | file | assert | popup | tree}</li>
   * <li>{@code <parameter> := <an arbitrary string parameter for the given sink type>}</li>
   * <li>{@code <filter> := ( +<loc-filter> | -<loc-filter> | +'<thread-filter>' | -'<thread-filter>' )* }</li>
   * <li>{@code <loc-filter> := <the prefix of fully-qualified method names to include or exclude>}</li>
   * <li>{@code <thread-filter> := <a substring of thread names to include or exclude>}</li>
   * </ul>
   * Examples:
   * <ul>
   * <li>{@code "stdout, stderr"}: Log to both {@code System.out} and {@code System.err}.</li>
   * <li>{@code "tree, ~file"}: Log synchronously to a {@link #remoteTreeLogSink} and
   *     asynchronously to a file (named after {@code defaultName} and with location determined by the
   *     {@code plt.log.working.dir} and {@code user.dir} properties).</li>
   * <li>{@code "file:pkg1.txt +pkg1, file:pkg2.txt +pkg2"}: Log messages from package {@code pkg1} to file
   *     {@code pkg1.txt} and messages from package {@code pkg2} to file {@code pkg2.txt}.</li>
   * <li>{@code "(stdout:'UTF-8', stderr:'UTF-8') -'Foo'"}: Log all messages <em>except</em> those coming from
   *     threads with {@code "Foo"} in their names to both {@code System.out} and {@code System.err}, encoded as
   *     UTF-8 text.  Note that apostrophes may be used to prevent interpreting delimiters appearing within
   *     the sink's argument string.</li>
   * <li>{@code "assert +com -com.pkg1 -com.pkg2"}:  Assert that no logging messages come from locations in
   *     subpackages of {@code "com"}, but allowing an exception for messages from {@code "com.pkg1"} and
   *     {@code "com.pkg2"}.</li>
   * </ul></p>
   * 
   * <p>The set of supported types can be extended by defining the property {@code plt.log.factory} to point to
   * a static method.  The method must have a signature matching the following (any method name can be used):
   * {@code LogSink factoryMethod(String type, String arg, String defaultName)}.  The {@code arg} parameter
   * will be an empty string if none is provided by the descriptor.  This factory method is invoked <em>before</em>
   * reverting to the default interpretations; to delegate to the default behavior, the method should return
   * {@code null}.</p>
   */
  public static LogSink makeLogSink(String descriptor, String defaultName) {
    String[] split = TextUtil.split(descriptor, ",", Bracket.PARENTHESES, Bracket.APOSTROPHES).array();
    List<LogSink> sinks = new ArrayList<LogSink>(split.length);
    for (String s : split) {
      LogSink sink = makeFilteredLogSink(s.trim(), defaultName);
      if (sink != null) { sinks.add(sink); }
    }
    if (sinks.isEmpty()) { return null; }
    else if (sinks.size() == 1) { return sinks.get(0); }
    else { return new SplitLogSink(sinks); }
  }
  
  private static LogSink makeFilteredLogSink(String descriptor, String defaultName) {
    TextUtil.SplitString split = TextUtil.split(descriptor, "\\+|-", Bracket.PARENTHESES, Bracket.APOSTROPHES);
    String desc;
    SizedIterable<Pair<String, String>> filters;
    if (split.splits().isEmpty()) { desc = descriptor; filters = IterUtil.empty(); }
    else {
      desc = split.splits().get(0).trim();
      Iterable<String> filterText = IterUtil.compose(IterUtil.skipFirst(split.splits()), split.rest());
      filters = IterUtil.zip(split.delimiters(), filterText);
    }
    
    LogSink result;
    if (desc.startsWith("(")) {
      if (desc.endsWith(")")) { result = makeLogSink(desc.substring(1, desc.length()-1), defaultName); }
      else { return null; } // malformed descriptor
    }
    else if (desc.startsWith("~")) {
      result = new AsynchronousLogSink(makeAtomicLogSink(desc.substring(1), defaultName));
    }
    else {
      result = makeAtomicLogSink(desc, defaultName);
    }
    
    if (!filters.isEmpty()) {
      List<String> whiteListLocs = new ArrayList<String>();
      List<String> blackListLocs = new ArrayList<String>();
      List<String> whiteListThreads = new ArrayList<String>();
      List<String> blackListThreads = new ArrayList<String>();
      for (Pair<String, String> p : filters) {
        String text = p.second().trim();
        boolean thread = text.startsWith("'");
        if (thread) {
          if (text.endsWith("'")) { text = text.substring(1, text.length()-1); }
          else { return null; } // malformed descriptor
        }
        if (p.first().equals("+")) {
          (thread ? whiteListThreads : whiteListLocs).add(text);
        }
        else if (p.first().equals("-")) {
          (thread ? blackListThreads : blackListLocs).add(text);
        }
        else { throw new RuntimeException("Bad delimiter from TextUtil.split: " + p.first()); }
      }
      if (!blackListLocs.isEmpty()) {
        result = FilteredLogSink.byLocationBlackList(result, IterUtil.toArray(blackListLocs, String.class));
      }
      if (!blackListThreads.isEmpty()) {
        result = FilteredLogSink.byThreadBlackList(result, IterUtil.toArray(blackListThreads, String.class));
      }
      if (!whiteListLocs.isEmpty()) {
        result = FilteredLogSink.byLocationWhiteList(result, IterUtil.toArray(whiteListLocs, String.class));
      }
      if (!whiteListThreads.isEmpty()) {
        result = FilteredLogSink.byThreadWhiteList(result, IterUtil.toArray(whiteListThreads, String.class));
      }
    }
    return result;
  }
  
  private static LogSink makeAtomicLogSink(String descriptor, String defaultName) {
    String[] split = TextUtil.split(descriptor, ":", 2, Bracket.PARENTHESES, Bracket.APOSTROPHES).array();
    String name = split[0].trim();
    String arg = (split.length > 1) ? split[1].trim() : ""; 
    if (arg.length() >= 2 && arg.startsWith("'") && arg.endsWith("'")) { arg = arg.substring(1, arg.length()-1); }
    LogSink result = null;
    String factoryName = System.getProperty("plt.log.factory");
    if (factoryName != null) {
      int dot = factoryName.lastIndexOf('.');
      if (dot >= 0) {
        String className = factoryName.substring(0, dot);
        String methodName = factoryName.substring(dot+1);
        try {
          result = (LogSink) ReflectUtil.invokeStaticMethod(className, methodName, name, arg, defaultName);
        }
        catch (ReflectException e) {
          System.err.println("Unable to invoke plt.log.factory: " + e.getCause());
        }
        catch (ClassCastException e) {
          System.err.println("Unable to invoke plt.log.factory: " + e);
        }
      }
    }
    if (result == null) {
      try {
        if (name.equals("System.out") || name.equals("stdout")) {
          if (arg.equals("")) { result = new SystemOutLogSink(); }
          else { result = new SystemOutLogSink(arg); }
        }
        else if (name.equals("System.err") || name.equals("stderr")) {
          if (arg.equals("")) { result = new SystemErrLogSink(); }
          else { result = new SystemErrLogSink(arg); }
        }
        else if (name.equals("file")) {
          if (arg.equals("")) { arg = defaultName.toLowerCase().replace(' ', '-') + "-log.txt"; }
          String workingDir = System.getProperty("plt.log.working.dir");
          if (workingDir == null) { result = new FileLogSink(arg); }
          else { result = new FileLogSink(new File(workingDir, arg)); }
        }
        else if (name.equals("assert")) {
          result = AssertEmptyLogSink.INSTANCE;
        }
        else if (name.equals("popup")) {
          if (arg.equals("")) { arg = defaultName; }
          result = new PopupLogSink(arg);
        }
        else if (name.equals("tree")) {
          if (arg.equals("")) { arg = defaultName; }
          result = remoteTreeLogSink(arg);
        }
        // else result remains null
      }
      catch (Exception e) { /* ignore; result is null */ }
    }
    return result;
  }
  
  public static Log voidLog() { return VoidLog.INSTANCE; }
  public static Log assertEmptyLog() { return new StandardLog(AssertEmptyLogSink.INSTANCE); }
  public static Log systemOutLog() { return new StandardLog(new SystemOutLogSink()); }
  public static Log systemOutLog(String charsetName) throws UnsupportedEncodingException {
    return new StandardLog(new SystemOutLogSink(charsetName));
  }
  public static Log systemErrLog() { return new StandardLog(new SystemErrLogSink()); }
  public static Log systemErrLog(String charsetName) throws UnsupportedEncodingException {
    return new StandardLog(new SystemErrLogSink(charsetName));
  }
  public static Log fileLog(String filename) { return new StandardLog(new FileLogSink(filename)); }
  public static Log fileLog(File f) { return new StandardLog(new FileLogSink(f)); }
  public static Log popupLog(String name) { return new StandardLog(new PopupLogSink(name)); }
  public static Log remoteTreeLog(String name) { return new StandardLog(remoteTreeLogSink(name)); }

  /**
   * Create an RMILogSink that passes messages to a remote TreeLogSink with {@code exitOnClose} set
   * to {@code true}.
   */ 
  public static LogSink remoteTreeLogSink(String name) {
    return new RMILogSink(TreeLogSink.factory(name, true), false);
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
   * @throws IllegalStateException  If the stack information is not available
   */
  public static StackTraceElement getCaller() {
    // Index 0 is this location; index 1 is the caller; index 2 is the caller's caller
    // (we avoid Thread.getStackTrace() because it includes itself and unspecified implementation frames
    // in the result)
    try { return new Throwable().getStackTrace()[2]; }
    catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalStateException("Stack trace information for caller is not available");
    }
  }
  

  /** A lazily-instantiated Timer for scheduling logging events. */ 
  private static final Thunk<Timer> LOG_TIMER = new LazyThunk<Timer>(new Thunk<Timer>() {
    public Timer value() {
      return new Timer("Delayed Log Timer", true);
    }
  });
  
  /**
   * Log the status (name, state, stack, etc.), of the current thread after a delay.  Invokes {@link Log#logValues}
   * on the given log.  Delayed logging events are scheduled on a daemon thread, and so may not run if
   * program execution terminates first.
   * @param log  Log to write to.
   * @param delays  A set of delays (in milliseconds) after which the thread should be logged.  If empty, the
   *                status is logged immediately.  Otherwise, the status is logged after each specified delay.
   */
  public static void logThreadStatus(Log log, long... delays) {
    logThreadStatus(log, Thread.currentThread(), delays);
  }
  
  /**
   * Log the status (name, state, stack, etc.), of the given thread after a delay.  Invokes {@link Log#logValues}
   * on the given log.  Delayed logging events are scheduled on a daemon thread, and so may not run if
   * program execution terminates first.
   * @param thread  The thread to observe.
   * @param log  Log to write to.
   * @param delays  A set of delays (in milliseconds) after which the thread should be logged.  If empty, the
   *                status is logged immediately.  Otherwise, the status is logged after each specified delay.
   */
  public static void logThreadStatus(final Log log, final Thread thread, long... delays) {
    // LogTask can't be a singleton because each scheduled instance has distinct state for scheduling
    class LogTask extends TimerTask {
      public void run() {
        log.logValues(new String[]{ "thread", "state", "stack" },
                      thread, thread.getState(), thread.getStackTrace());
      }
    }
    if (delays.length == 0) { new LogTask().run(); }
    for (final long delay : delays) {
      LOG_TIMER.value().schedule(new LogTask(), delay);
    }
  }
  
  /** Wrap a Runnable in a try-catch block that logs all caught {@code RuntimeException}s. */
  public static Runnable logExceptions(Log l, Runnable r) {
    return new LogExceptionRunnable(l, r);
  }
  
  private static final class LogExceptionRunnable implements Runnable, Serializable {
    private final Log _log;
    private final Runnable _r;
    public LogExceptionRunnable(Log log, Runnable r) { _log = log; _r = r; }
    public void run() {
      try { _r.run(); }
      catch (RuntimeException e) { _log.log(e); }
    }
  }
  
  /** Wrap a Runnable in a try-catch block that logs all caught {@code Throwable}s. */
  public static Runnable logThrowables(Log l, Runnable r) {
    return new LogThrowableRunnable(l, r);
  }
  
  private static final class LogThrowableRunnable implements Runnable, Serializable {
    private final Log _log;
    private final Runnable _r;
    public LogThrowableRunnable(Log log, Runnable r) { _log = log; _r = r; }
    public void run() {
      try { _r.run(); }
      catch (Throwable t) { _log.log(t); }
    }
  }
  
  /** Wrap a Runnable1 in a try-catch block that logs all caught {@code RuntimeException}s. */
  public static <T> Runnable1<T> logExceptions(Log l, Runnable1<? super T> r) {
    return new LogExceptionRunnable1<T>(l, r);
  }
  
  private static final class LogExceptionRunnable1<T> implements Runnable1<T>, Serializable {
    private final Log _log;
    private final Runnable1<? super T> _r;
    public LogExceptionRunnable1(Log log, Runnable1<? super T> r) { _log = log; _r = r; }
    public void run(T arg) {
      try { _r.run(arg); }
      catch (RuntimeException e) { _log.log(e); }
    }
  }
  
  /** Wrap a Runnable1 in a try-catch block that logs all caught {@code Throwable}s. */
  public static <T> Runnable1<T> logThrowables(Log l, Runnable1<? super T> r) {
    return new LogThrowableRunnable1<T>(l, r);
  }
  
  private static final class LogThrowableRunnable1<T> implements Runnable1<T>, Serializable {
    private final Log _log;
    private final Runnable1<? super T> _r;
    public LogThrowableRunnable1(Log log, Runnable1<? super T> r) { _log = log; _r = r; }
    public void run(T arg) {
      try { _r.run(arg); }
      catch (Throwable t) { _log.log(t); }
    }
  }
  
  /** Wrap a Runnable2 in a try-catch block that logs all caught {@code RuntimeException}s. */
  public static <T1, T2> Runnable2<T1, T2> logExceptions(Log l, Runnable2<? super T1, ? super T2> r) {
    return new LogExceptionRunnable2<T1, T2>(l, r);
  }
  
  private static final class LogExceptionRunnable2<T1, T2> implements Runnable2<T1, T2>, Serializable {
    private final Log _log;
    private final Runnable2<? super T1, ? super T2> _r;
    public LogExceptionRunnable2(Log log, Runnable2<? super T1, ? super T2> r) { _log = log; _r = r; }
    public void run(T1 arg1, T2 arg2) {
      try { _r.run(arg1, arg2); }
      catch (RuntimeException e) { _log.log(e); }
    }
  }
  
  /** Wrap a Runnable2 in a try-catch block that logs all caught {@code Throwable}s. */
  public static <T1, T2> Runnable2<T1, T2> logThrowables(Log l, Runnable2<? super T1, ? super T2> r) {
    return new LogThrowableRunnable2<T1, T2>(l, r);
  }
  
  private static final class LogThrowableRunnable2<T1, T2> implements Runnable2<T1, T2>, Serializable {
    private final Log _log;
    private final Runnable2<? super T1, ? super T2> _r;
    public LogThrowableRunnable2(Log log, Runnable2<? super T1, ? super T2> r) { _log = log; _r = r; }
    public void run(T1 arg1, T2 arg2) {
      try { _r.run(arg1, arg2); }
      catch (Throwable t) { _log.log(t); }
    }
  }
  
  /** Wrap a Runnable3 in a try-catch block that logs all caught {@code RuntimeException}s. */
  public static <T1, T2, T3>
  Runnable3<T1, T2, T3> logExceptions(Log l, Runnable3<? super T1, ? super T2, ? super T3> r) {
    return new LogExceptionRunnable3<T1, T2, T3>(l, r);
  }
  
  private static final class LogExceptionRunnable3<T1, T2, T3> implements Runnable3<T1, T2, T3>, Serializable {
    private final Log _log;
    private final Runnable3<? super T1, ? super T2, ? super T3> _r;
    public LogExceptionRunnable3(Log log, Runnable3<? super T1, ? super T2, ? super T3> r) { _log = log; _r = r; }
    public void run(T1 arg1, T2 arg2, T3 arg3) {
      try { _r.run(arg1, arg2, arg3); }
      catch (RuntimeException e) { _log.log(e); }
    }
  }
  
  /** Wrap a Runnable3 in a try-catch block that logs all caught {@code Throwable}s. */
  public static <T1, T2, T3>
  Runnable3<T1, T2, T3> logThrowables(Log l, Runnable3<? super T1, ? super T2, ? super T3> r) {
    return new LogThrowableRunnable3<T1, T2, T3>(l, r);
  }
  
  private static final class LogThrowableRunnable3<T1, T2, T3> implements Runnable3<T1, T2, T3>, Serializable {
    private final Log _log;
    private final Runnable3<? super T1, ? super T2, ? super T3> _r;
    public LogThrowableRunnable3(Log log, Runnable3<? super T1, ? super T2, ? super T3> r) { _log = log; _r = r; }
    public void run(T1 arg1, T2 arg2, T3 arg3) {
      try { _r.run(arg1, arg2, arg3); }
      catch (Throwable t) { _log.log(t); }
    }
  }
  
  /** Wrap a Runnable4 in a try-catch block that logs all caught {@code RuntimeException}s. */
  public static <T1, T2, T3, T4>
  Runnable4<T1, T2, T3, T4> logExceptions(Log l, Runnable4<? super T1, ? super T2, ? super T3, ? super T4> r) {
    return new LogExceptionRunnable4<T1, T2, T3, T4>(l, r);
  }
  
  private static final class LogExceptionRunnable4<T1, T2, T3, T4> implements Runnable4<T1, T2, T3, T4>, Serializable {
    private final Log _log;
    private final Runnable4<? super T1, ? super T2, ? super T3, ? super T4> _r;
    public LogExceptionRunnable4(Log log, Runnable4<? super T1, ? super T2, ? super T3, ? super T4> r) {
      _log = log; _r = r;
    }
    public void run(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
      try { _r.run(arg1, arg2, arg3, arg4); }
      catch (RuntimeException e) { _log.log(e); }
    }
  }
  
  /** Wrap a Runnable4 in a try-catch block that logs all caught {@code Throwable}s. */
  public static <T1, T2, T3, T4>
  Runnable4<T1, T2, T3, T4> logThrowables(Log l, Runnable4<? super T1, ? super T2, ? super T3, ? super T4> r) {
    return new LogThrowableRunnable4<T1, T2, T3, T4>(l, r);
  }
  
  private static final class LogThrowableRunnable4<T1, T2, T3, T4> implements Runnable4<T1, T2, T3, T4>, Serializable {
    private final Log _log;
    private final Runnable4<? super T1, ? super T2, ? super T3, ? super T4> _r;
    public LogThrowableRunnable4(Log log, Runnable4<? super T1, ? super T2, ? super T3, ? super T4> r) {
      _log = log; _r = r;
    }
    public void run(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
      try { _r.run(arg1, arg2, arg3, arg4); }
      catch (Throwable t) { _log.log(t); }
    }
  }
  
}
