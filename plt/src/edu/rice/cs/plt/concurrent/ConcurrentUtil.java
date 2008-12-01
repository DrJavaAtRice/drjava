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

package edu.rice.cs.plt.concurrent;

import java.io.File;
import java.io.IOException;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.InvocationTargetException;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.io.VoidOutputStream;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.reflect.ReflectUtil;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Utility methods for executing code in concurrent threads or processes. */
public final class ConcurrentUtil {
  
  private ConcurrentUtil() {}
  
  /** A runnable that simply sleeps for the specified amount of time (milliseconds), or until an interrupt occurs. */
  public static final Runnable1<Long> SLEEPING_RUNNABLE = new SleepingRunnable();
  
  private static final class SleepingRunnable implements Runnable1<Long>, Serializable {
    public void run(Long delay) {
      try { Thread.sleep(delay); }
      catch (InterruptedException e) { /* discard */ }
    }
  }

  /** Sleep for the given amount of time (milliseconds), or until an interrupt occurs. */
  public static void sleep(long delay) { SLEEPING_RUNNABLE.run(delay); }

  /** A runnable that performs useless computation for the specified amount of time (milliseconds). */
  public static final Runnable1<Long> WORKING_RUNNABLE = new WorkingRunnable();
  
  private static final class WorkingRunnable implements Runnable1<Long>, Serializable {
    public void run(Long delay) {
      long finished = System.currentTimeMillis() + delay;
      while (System.currentTimeMillis() < finished) {
        if (Thread.interrupted()) break;
        for (int i = 0; i < 1000; i++) { @SuppressWarnings("unused") long junk = delay * (delay - i); }
      }
    }
  }
  
  /** Perform useless computation for the given amount of time (millisconds), or until an interrupt occurs. */
  public static void work(long delay) { WORKING_RUNNABLE.run(delay); }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a
   * convenience method that sets {@code start} to {@code true}.
   */
  public static TaskController<Void> runInThread(Runnable task) {
    return computeInThread(LambdaUtil.asThunk(task), true);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static TaskController<Void> runInThread(Runnable task, boolean start) {
    return computeInThread(LambdaUtil.asThunk(task), start);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a
   * convenience method that sets {@code start} to {@code true}.
   */
  public static <R> TaskController<R> computeInThread(Thunk<? extends R> task) {
    return computeInThread(task, true);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R> TaskController<R> computeInThread(final Thunk<? extends R> task, boolean start) {
    final ThreadController<R> controller = new ThreadController<R>();
    Runnable runner = new Runnable() {
      public void run() {
        R result = null;
        Throwable exception = null; // *Only* exceptions thrown by the task
        try { result = task.value(); }
        catch (Throwable e) { exception = e; }
        controller.done(result, exception);
      }
    };
    controller.setThread(new Thread(runner, "ConcurrentUtil task"));
    if (start) { controller.start(); }
    return controller;
  }
  
  // Declared statically to allow cancel to discard the parameters.
  // Designed to be thread-safe when accessed *only* by the task thread and a single controlling thread.
  private static class ThreadController<R> extends TaskController<R> {
    private Thread _t;
    private volatile R _result; // set in the task thread
    private volatile Throwable _exception; // set in the task thread
    
    public ThreadController() {}
    
    // allows for circular dependency between thread and controller
    protected void setThread(Thread t) { _t = t; }
    
    // invoked by the task thread
    protected void done(R result, Throwable exception) {
      // may occur after a cancel()
      if (_status != Status.CANCELLED) {
        _result = result;
        _exception = exception;
        _status = Status.FINISHED;
      }
    }
    
    protected void doStart() { _t.start(); _status = Status.RUNNING; }
    
    protected void doCancel() {
      _status = Status.CANCELLED;
      _t.interrupt();
      _t = null;
      _result = null;
      _exception = null;
    }
    
    protected R getValue() throws InterruptedException, InvocationTargetException {
      start(); // make sure the thread is running
      _t.join(); // guarantees that the result and exception are set
      if (_exception != null) { throw new InvocationTargetException(_exception); }
      else { return _result; }
    }
    
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a convenience method
   * that sets {@code start} to {@code true} and {@code ignoreIntermediateResults} to {@code false}.
   */
  public static <I, R> IncrementalTaskController<I, R> computeInThread(IncrementalTask<? extends I, ? extends R> task) {
    return computeInThread(task, true, false);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a convenience method
   * that sets {@code ignoreIntermediateResults} to {@code false}.
   */
  public static <I, R>
    IncrementalTaskController<I, R> computeInThread(IncrementalTask<? extends I, ? extends R> task, boolean start) {
    return computeInThread(task, start, false);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @param ignoreIntermediateResults  If {@code true}, all intermediate results will be immediately discarded.
   */
  public static <I, R>
    IncrementalTaskController<I, R> computeInThread(final IncrementalTask<? extends I, ? extends R> task, 
                                                    boolean start, final boolean ignoreIntermediateResults) {
    final IncrementalThreadController<I, R> controller = new IncrementalThreadController<I, R>(start);
    Runnable runner = new Runnable() {
      public void run() {
        R result = null;
        I intermediate = null;
        Throwable exception = null; // *Only* exceptions thrown by the task
        try {
          while (exception == null && !task.isFinished()) {
            controller.authorizeContinue();
            try { intermediate = task.step(); }
            catch (Throwable e) { exception = e; }
            if (exception == null && !ignoreIntermediateResults) { controller.addResult(intermediate); }
          }
          if (exception == null) {
            controller.authorizeContinue();
            try { result = task.value(); }
            catch (Throwable e) { exception = e; }
          }
          controller.done(result, exception);
        }
        catch (InterruptedException e) { /* Task has been cancelled.*/ controller.aborting(); }
      }
    };
    Thread t = new Thread(runner, "ConcurrentUtil task");
    controller.setThread(t);
    t.start();
    return controller;
  }
  
  // Declared statically to allow cancel to discard the parameters.
  // Designed to be thread-safe when accessed *only* by the task thread and a single controlling thread.
  private static class IncrementalThreadController<I, R> extends IncrementalTaskController<I, R> {
    private Thread _t; // only accessed by controlling thread
    private CompletionMonitor _continueMonitor; // only changed when cancelling
    // only changed when cancelling; accesses should lock (to prevent modifying the list after it's returned)
    private Box<List<I>> _intermediateResults;
    private volatile R _result;
    private volatile Throwable _exception;
    
    public IncrementalThreadController(boolean start) {
      _continueMonitor = new CompletionMonitor(start);
      _intermediateResults = new ConcurrentBox<List<I>>(new LinkedList<I>());
      if (start) { _status = Status.RUNNING; }
    }
    
    // allows for circular dependency between thread and controller
    protected void setThread(Thread t) { _t = t; }
    
    protected void authorizeContinue() throws InterruptedException {
      if (!_continueMonitor.isSignalled()) {
        _status = Status.PAUSED;
        debug.log("Waiting for signal to continue");
        _continueMonitor.ensureSignalled();
        debug.log("Received signal to continue");
        _status = Status.RUNNING;
      }
    }
    
    protected void addResult(I result) {
      synchronized (_intermediateResults) {
        _intermediateResults.value().add(result);
        _intermediateResults.notifyAll();
      }
    }
    
    // invoked by the task thread
    protected void done(R result, Throwable exception) {
      _result = result;
      _exception = exception;
      _status = Status.FINISHED;
      synchronized (_intermediateResults) {
        _intermediateResults.notifyAll(); // in case we're blocking for an intermediate result
      }
    }
    
    // Assumes that the task thread will no longer access the controller
    protected void aborting() {
      _status = Status.CANCELLED;
      _t = null;
      _continueMonitor = null;
      _intermediateResults = null;
      _result = null;
      _exception = null;
    }
    
    protected void doStart() { _continueMonitor.signal(); }
    protected void doPause() { _continueMonitor.reset(); }
    
    protected void doCancel() {
      if (_status == Status.FINISHED) {
        // thread is finished and won't call aborting()
        aborting();
      }
      else {
        _continueMonitor.reset();
        _t.interrupt();
      }
    }
    
    protected List<I> getIntermediateValues() throws InterruptedException {
      synchronized (_intermediateResults) {
        while (_intermediateResults.value().isEmpty() && _status != Status.FINISHED) {
          _continueMonitor.signal(); // make sure we're not permanently paused
          debug.log("Waiting for intermediate results");
          _intermediateResults.wait();
          debug.log("Done waiting for intermediate results");
        }
        List<I> result = _intermediateResults.value();
        _intermediateResults.set(new LinkedList<I>());
        return result;
      }
    }
    
    public boolean hasIntermediateValue() {
      synchronized (_intermediateResults) { return ! _intermediateResults.value().isEmpty(); }
    }
    
    protected R getValue() throws InterruptedException, InvocationTargetException {
      _continueMonitor.signal(); // make sure we're not permanently paused
      _t.join(); // guarantees that the result and exception are set; assumes no concurrent cancel
      if (_exception != null) { throw new InvocationTargetException(_exception); }
      else { return _result; }
    }
    
  }
  
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.  This is a convenience method that sets {@code start} to {@code true}.  Default 
   * parameters for the process are as documented in {@link #runJavaProcess(String, String[])}.
   */
  public static <R> TaskController<R> computeInProcess(final Thunk<? extends R> task) {
    return computeInProcess(task, true);
  }
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.  Default parameters for the process are as documented in 
   * {@link #runJavaProcess(String, String[])}.
   * 
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R> TaskController<R> computeInProcess(final Thunk<? extends R> task, boolean start) {
    ProcessController<R> controller = new ProcessController<R>(task, new LazyThunk<Process>(new Thunk<Process>() {
      public Process value() {
        try { return runJavaProcess(TaskProcess.class.getName()); }
        catch (IOException e) { throw new WrappedException(e); }
      }
    }));
    if (start) { controller.start(); }
    return controller;
  }
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.  Default parameters for the process are as documented in 
   * {@link #runJavaProcess(String, Iterable, Iterable)}.
   * 
   * @param classPath  The class path of the new JVM
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R> TaskController<R>
    computeInProcess(final Thunk<? extends R> task,final Iterable<? extends File> classPath, boolean start) {
    ProcessController<R> controller = new ProcessController<R>(task, new LazyThunk<Process>(new Thunk<Process>() {
      public Process value() {
        try { return runJavaProcess(TaskProcess.class.getName(), IterUtil.<String>empty(), classPath); }
        catch (IOException e) { throw new WrappedException(e); }
      }
    }));
    if (start) { controller.start(); }
    return controller;
  }
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.  Default parameters for the process are as documented in 
   * {@link #runJavaProcess(String, Iterable, File)}.
   * 
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working directory)
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R> TaskController<R> computeInProcess(final Thunk<? extends R> task, final File workingDir, 
                                                       boolean start) {
    ProcessController<R> controller = new ProcessController<R>(task, new LazyThunk<Process>(new Thunk<Process>() {
      public Process value() {
        try { return runJavaProcess(TaskProcess.class.getName(), IterUtil.<String>empty(), workingDir); }
        catch (IOException e) { throw new WrappedException(e); }
      }
    }));
    if (start) { controller.start(); }
    return controller;
  }
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.  Default parameters for the process are as documented in 
   * {@link #runJavaProcess(String, Iterable, Iterable, File, Properties, Iterable)}.
   * 
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working directory)
   * @param properties  Properties to set in the new JVM
   * @param jvmParams  Additional command-line parameters to pass to the new JVM
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R> TaskController<R>
    computeInProcess(final Thunk<? extends R> task, final Iterable<? extends File> classPath, final File workingDir, 
                     final Properties properties, final Iterable<? extends String> jvmParams, boolean start) {
    ProcessController<R> controller = new ProcessController<R>(task, new LazyThunk<Process>(new Thunk<Process>() {
      public Process value() {
        try {
          return runJavaProcess(TaskProcess.class.getName(), IterUtil.<String>empty(), classPath, workingDir, 
                                properties, jvmParams);
        }
        catch (IOException e) { throw new WrappedException(e); }
      }
    }));
    if (start) { controller.start(); }
    return controller;
  }
  
  /**
   * Execute the given task in a separate process, and provide access to its result.  The task and the return 
   * value must be serializable.
   * 
   * @param javaCommand  Location of the {@code java} command &mdash; either the executable or a directory locating
   *                     the executable (the paths {@code ../bin}, {@code bin}, and {@code .} are searched)
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working directory)
   * @param properties  Properties to set in the new JVM
   * @param jvmParams  Additional command-line parameters to pass to the new JVM
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   */
  public static <R>
    TaskController<R> computeInProcess(final Thunk<? extends R> task, final File javaCommand, 
                                       final Iterable<? extends File> classPath, final File workingDir, 
                                       final Properties properties, final Iterable<? extends String> jvmParams, 
                                       boolean start) {
    ProcessController<R> controller = new ProcessController<R>(task, new LazyThunk<Process>(new Thunk<Process>() {
      public Process value() {
        try {
          return runJavaProcess(javaCommand, TaskProcess.class.getName(), IterUtil.<String>empty(), classPath,
                                workingDir, properties, jvmParams);
        }
        catch (IOException e) { throw new WrappedException(e); }
      }
    }));
    if (start) { controller.start(); }
    return controller;
  }
  
  // Declared statically to allow cancel to discard the parameters.
  private static class ProcessController<R> extends TaskController<R> {
    private Thunk<? extends R> _task;
    private Thunk<Process> _process;
    private Exception _exception; // allows an exception in doStart() to be stored
    
    public ProcessController(Thunk<? extends R> task, Thunk<Process> process) {
      _task = task;
      _process = process;
      _exception = null;
    }
    
    protected void doStart() {
      _status = Status.RUNNING;
      try {
        _process.value(); /* initialize the process */
        discardProcessErr(_process.value()); /* prevent the err buffer from filling up */
      }
      catch (WrappedException e) {
        if (e.getCause() instanceof IOException) {
          _exception = (IOException) e.getCause();
          _status = Status.FINISHED;
        }
        else { throw e; }
      }
      
      if (_exception == null) {
        try {
          ObjectOutputStream objOut = new ObjectOutputStream(_process.value().getOutputStream());
          try { objOut.writeObject(_task); }
          finally { objOut.close(); }
        }
        catch (IOException e) {
          _exception = e;
          _process.value().destroy();
          _status = Status.FINISHED;
        }
      }
    }
    
    protected void doCancel() {
      if (_status == Status.RUNNING) { _process.value().destroy(); }
      _task = null;
      _process = null;
      _exception = null;
      _status = Status.CANCELLED;
    }
    
    protected R getValue() throws Exception {
      start(); // make sure the process is running
      R result = null;
      if (_exception == null) {
        try {
          ObjectInputStream objIn = new ObjectInputStream(_process.value().getInputStream());
          try {
            @SuppressWarnings("unchecked") R serializedResult = (R) objIn.readObject();
            result = serializedResult;
            _exception = (Exception) objIn.readObject();
          }
          finally { objIn.close(); }
        }
        catch (InterruptedIOException e) {
          _exception = new InterruptedException(e.getMessage());
          _exception.setStackTrace(e.getStackTrace());
        }
        catch (EOFException e) {
          if (processIsTerminated(_process.value())) {
            _exception = new IOException("Unable to run process; class path may need to be adjusted");
          }
          else { _exception = e; }
        }
        catch (IOException e) { _exception = e; }
      }
      _status = Status.FINISHED;
      if (_exception != null) { throw _exception; }
      else { return result; }
    }
    
  }

  
  private static class TaskProcess {
    public static void main(String... args) {
      try {
        ObjectOutputStream objOut = new ObjectOutputStream(System.out);
        try {
          Object result = null;
          Exception exception = null;
          
          try {
            IOUtil.replaceSystemOut(VoidOutputStream.INSTANCE);
            try {
              ObjectInputStream objIn = new ObjectInputStream(System.in);
              try {
                Thunk<?> task = (Thunk<?>) objIn.readObject();
                try { result = task.value(); }
                catch (Throwable e) { exception = new InvocationTargetException(e); }
              }
              finally { objIn.close(); }
            }
            catch (Exception e) { exception = e; } // problem with objIn
          }
          finally { IOUtil.revertSystemOut(); }
          
          objOut.writeObject(result);
          objOut.writeObject(exception);
        }
        finally { objOut.close(); }
      }
      catch (IOException e) { error.log("Can't create or write to ObjectOutputStream", e); }
    }
  }
  
  // TODO: Incremental tasks in a separate process
  
  
  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.  This is a convenience
   * method providing the following defaults:
   * <ul>
   * <li>{@code javaCommand} is the {@code java.home} system property</li>
   * <li>{@code classPath} is {@link ReflectUtil#SYSTEM_CLASS_PATH}</li>
   * <li>{@code workingDir} is the {@code user.dir} system property</li>
   * <li>{@code properties} is empty</li>
   * <li>{@code jvmParams} is empty</li>
   * </ul>
   * 
   * @param mainClass  Name of the class to run
   * @param classParams  Parameters to pass to the main class
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(String mainClass, String... classParams) throws IOException {
    File javaCommand = new File(System.getProperty("java.home", ""));
    Iterable<File> classPath = ReflectUtil.SYSTEM_CLASS_PATH;
    File workingDir = new File(System.getProperty("user.dir", ""));
    Properties properties = new Properties();
    Iterable<String> jvmParams = IterUtil.empty();
    return runJavaProcess(javaCommand, mainClass, IterUtil.asIterable(classParams), classPath, workingDir, 
                          properties, jvmParams);
  }

  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.  This is a convenience
   * method providing the following defaults:
   * <ul>
   * <li>{@code javaCommand} is the {@code java.home} system property</li>
   * <li>{@code classPath} is {@link ReflectUtil#SYSTEM_CLASS_PATH}</li>
   * <li>{@code workingDir} is the {@code user.dir} system property</li>
   * <li>{@code properties} is empty</li>
   * <li>{@code jvmParams} is empty</li>
   * </ul>
   * 
   * @param mainClass  Name of the class to run
   * @param classParams  Parameters to pass to the main class
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(String mainClass, Iterable<? extends String> classParams) throws IOException {
    File javaCommand = new File(System.getProperty("java.home", ""));
    Iterable<File> classPath = ReflectUtil.SYSTEM_CLASS_PATH;
    File workingDir = new File(System.getProperty("user.dir", ""));
    Properties properties = new Properties();
    Iterable<String> jvmParams = IterUtil.empty();
    return runJavaProcess(javaCommand, mainClass, classParams, classPath, workingDir, properties, jvmParams);
  }
  
  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.  This is a convenience
   * method providing the following defaults:
   * <ul>
   * <li>{@code javaCommand} is the {@code java.home} system property</li>
   * <li>{@code workingDir} is the {@code user.dir} system property</li>
   * <li>{@code properties} is empty</li>
   * <li>{@code jvmParams} is empty</li>
   * </ul>
   * 
   * @param mainClass  Name of the class to run
   * @param classParams  Parameters to pass to the main class
   * @param classPath  The class path of the new JVM (interpreted relative to {@code workingDir})
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(String mainClass, Iterable<? extends String> classParams, 
                                       Iterable<? extends File> classPath) throws IOException {
    File javaCommand = new File(System.getProperty("java.home", ""));
    File workingDir = new File(System.getProperty("user.dir", ""));
    Properties properties = new Properties();
    Iterable<String> jvmParams = IterUtil.empty();
    return runJavaProcess(javaCommand, mainClass, classParams, classPath, workingDir, properties, jvmParams);
  }
  
  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.  This is a convenience
   * method providing the following defaults:
   * <ul>
   * <li>{@code javaCommand} is the {@code java.home} system property</li>
   * <li>{@code classPath} is {@link ReflectUtil#SYSTEM_CLASS_PATH}, converted to an absolute form</li>
   * <li>{@code properties} is empty</li>
   * <li>{@code jvmParams} is empty</li>
   * </ul>
   * 
   * @param mainClass  Name of the class to run
   * @param classParams  Parameters to pass to the main class
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working 
   *                    directory)
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(String mainClass, Iterable<? extends String> classParams, File workingDir) 
    throws IOException {
    File javaCommand = new File(System.getProperty("java.home", ""));
    Iterable<File> classPath = IOUtil.attemptAbsoluteFiles(ReflectUtil.SYSTEM_CLASS_PATH);
    Properties properties = new Properties();
    Iterable<String> jvmParams = IterUtil.empty();
    return runJavaProcess(javaCommand, mainClass, classParams, classPath, workingDir, properties, jvmParams);
  }
  
  
  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.  This is a convenience
   * method that uses the {@code java.home} system property as {@code javaCommand}.
   * 
   * @param mainClass  Name of the class to run
   * @param classParams  Parameters to pass to the main class
   * @param classPath  The class path of the new JVM (interpreted relative to {@code workingDir})
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working 
   *                    directory)
   * @param properties  Properties to set in the new JVM
   * @param jvmParams  Additional command-line parameters to pass to the new JVM
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(String mainClass, Iterable<? extends String> classParams, 
                                       Iterable<? extends File> classPath, File workingDir,
                                       Properties properties, Iterable<? extends String> jvmParams) 
    throws IOException {
    File javaCommand = new File(System.getProperty("java.home", ""));
    return runJavaProcess(javaCommand, mainClass, classParams, classPath, workingDir, properties, jvmParams);
  }
  
  /**
   * Create a new process by invoking the {@code java} command with the specified parameters.
   * 
   * @param javaCommand  Location of the {@code java} command &mdash; either the executable or a directory locating
   *                     the executable (the paths {@code ../bin}, {@code bin}, and {@code .} are searched)
   * @param mainClass  Class to run
   * @param classParams  Parameters to pass to the main class
   * @param classPath  The class path of the new JVM (interpreted relative to {@code workingDir})
   * @param workingDir  The working directory of the new JVM (interpreted relative to the current JVM's working 
   *                    directory)
   * @param properties  Properties to set in the new JVM
   * @param jvmParams  Additional command-line parameters to pass to the new JVM
   * 
   * @throws IOException  If the invocation of {@link Runtime#exec(String[], String[], File)} fails.
   */
  public static Process runJavaProcess(File javaCommand, String mainClass, Iterable<? extends String> classParams, 
                                       Iterable<? extends File> classPath, File workingDir,
                                       Properties properties, Iterable<? extends String> jvmParams) 
    throws IOException {
    List<String> propArgs = new LinkedList<String>();
    for (Map.Entry<Object, Object> prop : properties.entrySet()) {
      propArgs.add("-D" + prop.getKey() + "=" + prop.getValue());
    }
    
    Iterable<String> args = IterUtil.singleton(_findJavaExecutable(javaCommand));
    args = IterUtil.compose(args, IterUtil.make("-classpath", IOUtil.pathToString(classPath)));
    args = IterUtil.compose(args, propArgs);
    args = IterUtil.compose(args, jvmParams);
    args = IterUtil.compose(args, IterUtil.compose(mainClass, classParams));
    
    return Runtime.getRuntime().exec(IterUtil.toArray(args, String.class), null, workingDir);
  }

  /** Find the java executable command.  (This logic originally came from Ant.) */
  private static String _findJavaExecutable(File f) {
    if (IOUtil.attemptIsFile(f)) { return f.getPath(); }
    else {
      f = IOUtil.attemptAbsoluteFile(f);
      String os = System.getProperty("os.name", "");
      File[] candidates = new File[]{ new File(f, "../bin"), new File(f, "bin"), f };
      
      if (TextUtil.containsIgnoreCase(os, "netware")) {
        return "java"; // based on comments from Ant's code
      }
      else if (TextUtil.containsIgnoreCase(os, "windows")) {
        for (File dir : candidates) {
          File result = new File(dir, "javaw.exe");
          if (IOUtil.attemptExists(result)) { return result.getPath(); }
          result = new File(dir, "java.exe");
          if (IOUtil.attemptExists(result)) { return result.getPath(); }
        }
      }
      else {
        for (File dir : candidates) {
          File result = new File(dir, "java");
          if (IOUtil.attemptExists(result)) { return result.getPath(); }
        }
      }
      
      // If nothing works, use the system path
      return "java";
    }
  }
  
  /** Test whether the given process has terminated. */
  public static boolean processIsTerminated(Process p) {
    try { p.exitValue(); return true; }
    catch (IllegalThreadStateException e) { return false; }
  }
  
  /**
   * Create two threads to continually discard the contents of the given process's output and error streams.
   * If, instead, the streams are simply ignored, the system buffers may fill up, causing the process to block (see 
   * the class documentation for {@link Process}; experimentation under Java 5 shows the buffer size to be
   * approximately 4 Kb).
   */
  public static void discardProcessOutput(Process p) {
    copyProcessOut(p, VoidOutputStream.INSTANCE);
    copyProcessErr(p, VoidOutputStream.INSTANCE);
  }
  
  /**
   * Create two threads to continually copy the contents of the given process's output and error streams to 
   * the given destinations.  This is a convenience method that invokes {@link #copyProcessOut(Process, OutputStream)}
   * and {@link #copyProcessErr(Process, OutputStream)}.
   */
  public static void copyProcessOutput(Process p, OutputStream out, OutputStream err) {
    copyProcessOut(p, out);
    copyProcessErr(p, err);
  }
  
  /**
   * Create a thread that will continually discard the contents of the given process's standard output.
   * If, instead, the stream is simply ignored, the system buffer may fill up, causing the process to block (see 
   * the class documentation for {@link Process}; experimentation under Java 5 shows the buffer size to be
   * approximately 4 Kb).
   */
  public static void discardProcessOut(Process p) { copyProcessOut(p, VoidOutputStream.INSTANCE); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's standard output to another
   * output stream.  This is a convenience method that sets {@code closeStream} to {@code false}.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessOut(Process p, OutputStream out) { return copyProcessOut(p, out, false); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's standard output to another
   * output stream.  Processing continues until the end-of-file is reached.  If {@code close} is {@code true},
   * {@code out} will then be closed.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessOut(Process p, OutputStream out, boolean close) {
    Thread result = new Thread(new CopyStream(p.getInputStream(), out, close));
    result.setDaemon(true); // this thread should not keep the JVM from exiting
    // TODO: If the parent process quits, can the child get stuck when its buffer fills?
    result.start();
    return result;
  }

  /**
   * Create a task providing access to the given process's standard output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.
   */
  public static TaskController<String> processOutAsString(Process p) {
    return computeInThread(new StreamToString(p.getInputStream()));
  }
  
  /**
   * Create a thread that will continually discard the contents of the given process's standard output.
   * If, instead, the stream is simply ignored, the system buffer may fill up, causing the process to block (see 
   * the class documentation for {@link Process}; experimentation under Java 5 shows the buffer size to be
   * approximately 4 Kb).
   */
  public static void discardProcessErr(Process p) { copyProcessErr(p, VoidOutputStream.INSTANCE); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's error output to another
   * output stream.  This is a convenience method that sets {@code closeStream} to {@code false}.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessErr(Process p, OutputStream err) { return copyProcessErr(p, err, false); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's error output to another
   * output stream.  Processing continues until the end-of-file is reached.  If {@code close} is {@code true},
   * {@code out} will then be closed.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessErr(Process p, OutputStream err, boolean close) {
    Thread result = new Thread(new CopyStream(p.getErrorStream(), err, close));
    result.setDaemon(true); // this thread should not keep the JVM from exiting
    // TODO: If the parent process quits, can the child get stuck when its buffer fills?
    result.start();
    return result;
  }
  
  /**
   * Create a task providing access to the given process's error output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.
   */
  public static TaskController<String> processErrAsString(Process p) {
    return computeInThread(new StreamToString(p.getErrorStream()));
  }
  
  /** Shared code for copying and closing a stream */
  private static final class CopyStream implements Runnable, Serializable {
    private final InputStream _in;
    private final OutputStream _out;
    private final boolean _close;
    public CopyStream(InputStream in, OutputStream out, boolean close) { _in = in; _out = out; _close = close; }
    public void run() {
      try {
        try { IOUtil.copyInputStream(_in, _out); }
        finally { if (_close) _out.close(); }
      }
      catch (IOException e) { error.log(e); }
    }
  }

  /** Shared code for reading a stream as a string */
  private static final class StreamToString implements Thunk<String> {
    private final InputStream _stream;
    public StreamToString(InputStream stream) { _stream = stream; }
    public String value() {
      try { return IOUtil.toString(new InputStreamReader(_stream)); }
      catch (IOException e) { throw new WrappedException(e); }
    }
  }
    
    
  /**
   * Get a subset of the system properties for names that match at least one of the given prefixes.
   * @throws SecurityException  As in {@link System#getProperties}.
   */
  public static Properties getProperties(String... prefixes) {
    Properties result = new Properties();
    // Properties should be a Map<String, String>, but it's not defined that way.  Depending on the
    // implementation, it may even allow clients to put non-string entries.
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      for (String prefix : prefixes) {
        if (entry.getKey() instanceof String && ((String) entry.getKey()).startsWith(prefix)) {
          result.put(entry.getKey(), entry.getValue());
          break;
        }
      }
    }
    return result;
  }
  
}
