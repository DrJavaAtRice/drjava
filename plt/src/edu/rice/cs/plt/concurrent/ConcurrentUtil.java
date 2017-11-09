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

package edu.rice.cs.plt.concurrent;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.io.VoidOutputStream;

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
    private long junk = 1;
    public void run(Long delay) {
      long finished = System.currentTimeMillis() + delay;
      while (System.currentTimeMillis() < finished) {
        if (Thread.interrupted()) break;
        // if the bound on i is too small, strange JIT effects seem to mess up interrupt detection...
        for (int i = 0; i < 10000; i++) { junk = junk * (delay+1); }
      }
    }
  }
  
  /** Perform useless computation for the given amount of time (milliseconds), or until an interrupt occurs. */
  public static void work(long delay) { WORKING_RUNNABLE.run(delay); }
  
  /**
   * Get the expected value of {@link System#nanoTime} after the given period has passed.  Negative values
   * for {@code time} return a time from the past.  Note that, since an overflow wrap-around may occur
   * at any time in the system's nanosecond clock, comparisons between the current time and this method's
   * result are non-trivial.  For example, to test whether the result time {@code future} has passed:
   * {@code System.nanoTime() - future > 0}.  (This checks that the current time is within a Long.MAX_VALUE range
   * <em>after</em> {@code future}, regardless of the absolute numeric values.  We can infer that (most likely)
   * {@code future} is less than 292 years in the past, or (unlikely) future is more than 292 years in the future.)
   */
  public static long futureTimeNanos(long time, TimeUnit unit) {
    return System.nanoTime() + unit.toNanos(time);
  }
  
  /**
   * Get the expected value of {@link System#currentTimeMillis} after the given period of time has passed.
   * Negative values for {@code time} return a time from the past.  While the notes about overflow in
   * {@link #futureTimeNanos} apply in principal here, an overflow of the 64-bit millisecond clock happens
   * once every 600 million years, with the year 1970 at 0.  So it's safe to use simple operators to make
   * comparisons between the current time and this method's result.
   */
  public static long futureTimeMillis(long time, TimeUnit unit) {
    return System.currentTimeMillis() + unit.toMillis(time);
  }
  
  /**
   * If the given time (based on {@link System#currentTimeMillis}) has passed, throw a TimeoutException.
   * Otherwise, invoke {@link Object#wait(long)} on the given object, which may return due to
   * a {@code notify()} call, the timeout being reached, or a spurious wake-up.  To distinguish between
   * these possibilities, clients should wrap this call in a while loop:
   * {@code long t = futureTimeMillis(...); while (!condition) waitUntilMillis(lock, t);}
   * This loop either completes if the condition is satisfied or throws an appropriate exception
   * due to an interrupt or timeout.
   * @param obj  Object whose {@code wait()} method will be invoked.  Must be locked by the current thread.
   * @param futureTime  A millisecond time value based on {@code System.currentTimeMillis()} after which
   *                    this method should no longer invoke {@code obj.wait()}.
   * @throws InterruptedException  If the wait is interrupted.
   * @throws TimeoutException  If, at invocation time, {@code futureTime} is in the past.
   * @see #futureTimeMillis
   */
  public static void waitUntilMillis(Object obj, long futureTime) throws InterruptedException, TimeoutException {
    long delta = futureTime - System.currentTimeMillis();
    if (delta > 0) { obj.wait(delta); }
    else { throw new TimeoutException(); }
  }
  
  /**
   * If the given time (based on {@link System#nanoTime}) has passed, throw a TimeoutException.
   * Otherwise, invoke {@link Object#wait(long, int)} on the given object, which may return due to
   * a {@code notify()} call, the timeout being reached, or a spurious wake-up.  To distinguish between
   * these possibilities, clients should wrap this call in a while loop:
   * {@code long t = futureTimeNanos(...); while (!condition) waitUntilNanos(lock, t);}
   * This loop either completes if the condition is satisfied or throws an appropriate exception
   * due to an interrupt or timeout.
   * @param obj  Object whose {@code wait()} method will be invoked.  Must be locked by the current thread.
   * @param futureTime  A nanosecond time value based on {@code System.nanoTime()} after which
   *                    this method should no longer invoke {@code obj.wait()}.
   * @throws InterruptedException  If the wait is interrupted.
   * @throws TimeoutException  If, at invocation time, {@code futureTime} is in the past.
   * @see #futureTimeNanos
   */
  public static void waitUntilNanos(Object obj, long futureTime) throws InterruptedException, TimeoutException {
    long delta = futureTime - System.nanoTime();
    if (delta > 0) { TimeUnit.NANOSECONDS.timedWait(obj, delta); }
    else { throw new TimeoutException(); }
  }
  
  /** Wrap a thunk in a Callable interface.  The {@code call()} method will not throw checked exceptions. */
  public static <T> Callable<T> asCallable(Thunk<? extends T> thunk) {
    return new ThunkCallable<T>(thunk);
  }
  
  private static final class ThunkCallable<T> implements Callable<T>, Serializable {
    private final Thunk<? extends T> _thunk;
    public ThunkCallable(Thunk<? extends T> thunk) { _thunk = thunk; }
    public T call() { return _thunk.value(); }
  }
  
  /**
   * Wrap a Future in a TaskController interface (which is also a Thunk).  The state of the controller
   * corresponds to the state of the Future; since Futures have no notion of "starting," the result is
   * automatically "running" &mdash; it is never in a "paused" state.
   */
  public static <T> TaskController<T> asTaskController(Future<? extends T> future) {
    TaskController<T> result = new FutureTaskController<T>(LambdaUtil.valueLambda(future));
    result.start();
    return result;
  }
  
  /**
   * Wrap a Future produced by a Thunk in a TaskController interface (which is also a Thunk).  "Starting"
   * the resulting controller corresponds to invoking {@code futureThunk}; subsequently, the state of
   * the controller corresponds to the state of the Future.
   */
  public static <T> TaskController<T> asTaskController(Thunk<? extends Future<? extends T>> futureThunk) {
    return new FutureTaskController<T>(futureThunk);
  }
  
  /**
   * A simple Executor that creates a new thread for each task; threads are identified by the name
   * {@code THREAD_EXECUTOR-n} for some n.
   */
  public static final Executor THREAD_EXECUTOR = new Executor() {
    private int count = 0;
    public void execute(Runnable r) {
      new Thread(r, "THREAD_EXECUTOR-" + (++count)).start();
    }
  };
  
  /** A trivial Executor that simply runs each task directly.  {@code execute()} blocks until the task completes. */
  public static final Executor DIRECT_EXECUTOR = new Executor() {
    public void execute(Runnable r) { r.run(); }
  };
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a
   * convenience method that sets {@code start} to {@code true}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @see ExecutorTaskController
   */
  public static TaskController<Void> runInThread(Runnable task) {
    return computeWithExecutor(LambdaUtil.asThunk(task), THREAD_EXECUTOR, true);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ExecutorTaskController
   */
  public static TaskController<Void> runInThread(Runnable task, boolean start) {
    return computeWithExecutor(LambdaUtil.asThunk(task), THREAD_EXECUTOR, start);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.  This is a
   * convenience method that sets {@code start} to {@code true}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @see ExecutorTaskController
   */
  public static <R> TaskController<R> computeInThread(Thunk<? extends R> task) {
    return computeWithExecutor(task, THREAD_EXECUTOR, true);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its result.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ExecutorTaskController
   */
  public static <R> TaskController<R> computeInThread(Thunk<? extends R> task, boolean start) {
    return computeWithExecutor(task, THREAD_EXECUTOR, start);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its results.  This is a convenience method
   * that sets {@code start} to {@code true} and {@code ignoreIntermediate} to {@code false}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R> IncrementalTaskController<I, R> computeInThread(IncrementalTask<? extends I, ? extends R> task) {
    return computeWithExecutor(task, THREAD_EXECUTOR, true, false);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its results.  This is a convenience method
   * that sets {@code ignoreIntermediate} to {@code false}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInThread(IncrementalTask<? extends I, ? extends R> task, boolean start) {
    return computeWithExecutor(task, THREAD_EXECUTOR, start, false);
  }
  
  /**
   * Execute the given task in a separate thread, and provide access to its results.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @param ignoreIntermediate  If {@code true}, all intermediate results will be immediately discarded.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R>
    IncrementalTaskController<I, R> computeInThread(IncrementalTask<? extends I, ? extends R> task, 
                                                    boolean start, boolean ignoreIntermediate) {
    return computeWithExecutor(task, THREAD_EXECUTOR, start, ignoreIntermediate);
  }
  
  /**
   * Execute the given task with {@code exec} and provide access to its result.  This is a convenience
   * method that sets {@code start} to {@code true}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param exec  An executor which is given the task to run when the controller is started.
   * @see ExecutorTaskController
   */
  public static <R> TaskController<R> computeWithExecutor(Thunk<? extends R> task, Executor exec) {
    return computeWithExecutor(task, exec, true);
  }
    
  /**
   * Execute the given task with {@code exec} and provide access to its result.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param exec  An executor which is given the task to run when the controller is started.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ExecutorTaskController
   */
  public static <R> TaskController<R> computeWithExecutor(Thunk<? extends R> task, Executor exec, boolean start) {
    ExecutorTaskController<R> result = new ExecutorTaskController<R>(exec, task);
    if (start) { result.start(); }
    return result;
  }
  
  /**
   * Execute the given task with {@code exec} and provide access to its result.  This is a convenience method
   * that sets {@code start} to {@code true} and {@code ignoreIntermediate} to {@code false}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param exec  An executor which is given the task to run when the controller is started.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeWithExecutor(IncrementalTask<? extends I, ? extends R> task,
                                                          Executor exec) {
    return computeWithExecutor(task, exec, true, false);
  }
  
  /**
   * Execute the given task with {@code exec} and provide access to its result.  This is a convenience method
   * that sets {@code ignoreIntermediate} to {@code false}.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param exec  An executor which is given the task to run when the controller is started.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeWithExecutor(IncrementalTask<? extends I, ? extends R> task, 
                                                          Executor exec, boolean start) {
    return computeWithExecutor(task, exec, start, false);
  }
  
  /**
   * Execute the given task with {@code exec} and provide access to its result.
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param exec  An executor which is given the task to run when the controller is started.
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @param ignoreIntermediate  If {@code true}, all intermediate results will be immediately discarded.
   * @see ExecutorIncrementalTaskController
   */
  public static <I, R>
    IncrementalTaskController<I, R> computeWithExecutor(IncrementalTask<? extends I, ? extends R> task, 
                                                        Executor exec, boolean start, boolean ignoreIntermediate) {
    IncrementalTaskController<I, R> result =
      new ExecutorIncrementalTaskController<I, R>(exec, task, ignoreIntermediate);
    if (start) { result.start(); }
    return result;
  }
  
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the
   * return value must be serializable.  Typically, the subprocess terminates when the TaskController enters a
   * finished state.  However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that uses {@link JVMBuilder#DEFAULT} and sets {@code start} to {@code true}.</p>
   * @param task  A task to perform.  Will be abruptly terminated with the process if canceled while running.
   * @see ProcessTaskController
   */
  public static <R> TaskController<R> computeInProcess(Thunk<? extends R> task) {
    return computeInProcess(task, JVMBuilder.DEFAULT, true);
  }

  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the return
   * value must be serializable. Typically, the subprocess terminates when the TaskController enters a
   * finished state. However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that uses {@link JVMBuilder#DEFAULT}.</p>
   * 
   * @param task  A task to perform.  Will be abruptly terminated with the process if canceled while running.
   * @param start If {@code true}, the task will be started before returning; otherwise, the client should
   *              invoke {@link TaskController#start} on the returned controller.
   * @see ProcessTaskController
   */
  public static <R> TaskController<R> computeInProcess(Thunk<? extends R> task, boolean start) {
    return computeInProcess(task, JVMBuilder.DEFAULT, start);
  }
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the return
   * value must be serializable. Typically, the subprocess terminates when the TaskController enters a
   * finished state. However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * @param task  A task to perform.  Will be abruptly terminated with the process if canceled while running.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the task's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @see ProcessTaskController
   */
  public static <R> TaskController<R> computeInProcess(Thunk<? extends R> task, JVMBuilder jvmBuilder) {
    return computeInProcess(task, jvmBuilder, true);
  }
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the
   * return value must be serializable.  Typically, the subprocess terminates when the TaskController enters a
   * finished state.  However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * @param task  A task to perform.  Will be abruptly terminated with the process if canceled while running.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the task's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ProcessTaskController
   */
  public static <R> TaskController<R> computeInProcess(Thunk<? extends R> task, JVMBuilder jvmBuilder,
                                                       boolean start) {
    jvmBuilder = jvmBuilder.addDefaultProperties(getProperties("plt."));
    ProcessTaskController<R> controller = new ProcessTaskController<R>(jvmBuilder, THREAD_EXECUTOR, task);
    if (start) { controller.start(); }
    return controller;
  }
   
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the
   * return value must be serializable.  Typically, the subprocess terminates when the TaskController enters a
   * finished state.  However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that uses {@link JVMBuilder#DEFAULT} and sets {@code start} to {@code true}
   * and {@code ignoreIntermediate} to {@code false}.</p>
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @see ProcessIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInProcess(IncrementalTask<? extends I, ? extends R> task) {
    return computeInProcess(task, JVMBuilder.DEFAULT, true, false);
  }

  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the return
   * value must be serializable. Typically, the subprocess terminates when the TaskController enters a
   * finished state. However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that uses {@link JVMBuilder#DEFAULT} and sets {@code ignoreIntermediate}
   * to {@code false}.</p>
   * 
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param start If {@code true}, the task will be started before returning; otherwise, the client should
   *              invoke {@link TaskController#start} on the returned controller.
   * @see ProcessIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInProcess(IncrementalTask<? extends I, ? extends R> task, boolean start) {
    return computeInProcess(task, JVMBuilder.DEFAULT, start, false);
  }
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the return
   * value must be serializable. Typically, the subprocess terminates when the TaskController enters a
   * finished state. However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that sets {@code start} to {@code true} and {@code ignoreIntermediate}
   * to {@code false}.</p>
   * 
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the task's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @see ProcessIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInProcess(IncrementalTask<? extends I, ? extends R> task,
                                                       JVMBuilder jvmBuilder) {
    return computeInProcess(task, jvmBuilder, true, false);
  }
  
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the
   * return value must be serializable.  Typically, the subprocess terminates when the TaskController enters a
   * finished state.  However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * <p>This is a convenience method that sets {@code ignoreIntermediate} to {@code false}.</p>
   * 
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the task's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @see ProcessIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInProcess(IncrementalTask<? extends I, ? extends R> task,
                                                       JVMBuilder jvmBuilder, boolean start) {
    return computeInProcess(task, jvmBuilder, start, false);
  }
   
  /**
   * <p>Execute the given task in a separate process and provide access to its result.  The task and the
   * return value must be serializable.  Typically, the subprocess terminates when the TaskController enters a
   * finished state.  However, if {@code task} spawns additional threads and no exceptions are thrown by the
   * controller's {@code value()} method, the subprocess may remain alive indefinitely; the remaining threads
   * are responsible for process termination.</p>
   * 
   * @param task  A task to perform.  Should respond to an interrupt by throwing an {@link InterruptedException}
   *              wrapped in a {@link WrappedException}.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the task's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @param start  If {@code true}, the task will be started before returning; otherwise, the client should invoke
   *               {@link TaskController#start} on the returned controller.
   * @param ignoreIntermediate  If {@code true}, all intermediate results will be immediately discarded.
   * @see ProcessIncrementalTaskController
   */
  public static <I, R>
      IncrementalTaskController<I, R> computeInProcess(IncrementalTask<? extends I, ? extends R> task,
                                                       JVMBuilder jvmBuilder, boolean start,
                                                       boolean ignoreIntermediate) {
    jvmBuilder = jvmBuilder.addDefaultProperties(getProperties("plt."));
    ProcessIncrementalTaskController<I, R> controller =
      new ProcessIncrementalTaskController<I, R>(jvmBuilder, THREAD_EXECUTOR, task, ignoreIntermediate);
    if (start) { controller.start(); }
    return controller;
  }
   
  
  
  /**
   * Export the given RMI object in a new process and return the exported stub.  If any exception occurs,
   * the process is destroyed.  This convenience method uses {@link JVMBuilder#DEFAULT} to start the new process.
   * @param factory  A thunk to evaluate in the remote JVM, producing an object that can be exported via
   *                 {@link UnicastRemoteObject#exportObject(Remote, int)}.  The factory must be serializable.
   * @return  An RMI proxy that can be cast to the remote interface type of the object returned by
   *          {@code factory}.  (See {@link Remote} for the definition of "remote interface.")
   * @throws IOException  If a problem occurs in starting the new process or serializing {@code factory}.
   * @throws ExecutionException  If an exception occurs in {@code factory} or while exporting the result.
   * @throws InterruptedException  If this thread is interrupted while waiting for the result to be produced.
   */
  public static Remote exportInProcess(Thunk<? extends Remote> factory)
      throws InterruptedException, ExecutionException, IOException {
    return exportInProcess(factory, JVMBuilder.DEFAULT, null);
  }
  
  /**
   * Export the given RMI object in a new process and return the exported stub.  If any exception occurs,
   * the process is destroyed.
   * @param factory  A thunk to evaluate in the remote JVM, producing an object that can be exported via
   *                 {@link UnicastRemoteObject#exportObject(Remote, int)}.  The factory must be serializable.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the factory's class, ConcurrentUtil, and their dependencies.
   * @return  An RMI proxy that can be cast to the remote interface type of the object returned by
   *          {@code factory}.  (See {@link Remote} for the definition of "remote interface.")
   * @throws IOException  If a problem occurs in starting the new process or serializing {@code factory}.
   * @throws ExecutionException  If an exception occurs in {@code factory} or while exporting the result.
   * @throws InterruptedException  If this thread is interrupted while waiting for the result to be produced.
   */
  public static Remote exportInProcess(Thunk<? extends Remote> factory, JVMBuilder jvmBuilder)
      throws InterruptedException, ExecutionException, IOException {
    return exportInProcess(factory, jvmBuilder, null);
  }
  
  /**
   * Export the given RMI object in a new process and return the exported stub.  If any exception occurs,
   * the process is destroyed.
   * @param factory  A thunk to evaluate in the remote JVM, producing an object that can be exported via
   *                 {@link UnicastRemoteObject#exportObject(Remote, int)}.  The factory must be serializable.
   * @param jvmBuilder  A JVMBuilder set up with the necessary subprocess parameters.  The class path must include
   *                    the factory's class, ConcurrentUtil, and their dependencies.  If the current JVM has
   *                    property values for {@code plt.*}, those values will be added to {@code jvmBuilder}
   *                    (unless they're already set to something else).
   * @param onExit  Code to execute when the process exits, assuming a result is successfully returned.  May be
   *                {@code null}, indicating that nothing should be run.  If an exception occurs here, the process is
   *                destroyed immediately and this listener will not be invoked.
   * @return  An RMI proxy that can be cast to the remote interface type of the object returned by
   *          {@code factory}.  (See {@link Remote} for the definition of "remote interface.")
   * @throws IOException  If a problem occurs in starting the new process or serializing {@code factory}.
   * @throws ExecutionException  If an exception occurs in {@code factory} or while exporting the result.
   * @throws InterruptedException  If this thread is interrupted while waiting for the result to be produced.
   */
  public static Remote exportInProcess(Thunk<? extends Remote> factory, JVMBuilder jvmBuilder,
                                       Runnable1<? super Process> onExit)
      throws InterruptedException, ExecutionException, IOException {
    Thunk<Remote> task = new ExportRemoteTask(factory);
    // no need to spawn a thread if we don't need to wait for the process to quit
    Executor exec = (onExit == null) ? DIRECT_EXECUTOR : THREAD_EXECUTOR;
    // use localhost to avoid issues with changing IPs and firewalls
    jvmBuilder = jvmBuilder.addDefaultProperty("java.rmi.server.hostname", "127.0.0.1");
    jvmBuilder = jvmBuilder.addDefaultProperties(getProperties("plt."));
    try { return new ProcessTaskController<Remote>(jvmBuilder, exec, task, onExit).get(); }
    // an interrupt on this thread translates into a "cancel" because DIRECT_EXECUTOR runs the task on this thread
    catch (CancellationException e) { throw new InterruptedException(); }
    catch (WrappedException e) {
      if (e.getCause() instanceof IOException) { throw (IOException) e.getCause(); }
      else { throw e; }
    }
  }
  
  private static class ExportRemoteTask implements Thunk<Remote>, Serializable {
    private final Thunk<? extends Remote> _factory;
    // The result must be stored statically to prevent garbage-collection.  (It's not clear whether
    // the lack of a strong reference from the RMI code is specified behavior or a bug...)
    private static final List<Remote> _cache = new ArrayList<Remote>(1);
    public ExportRemoteTask(Thunk<? extends Remote> factory) { _factory = factory; }
    public Remote value() {
      Remote server = _factory.value();
      _cache.add(server);
      try { return UnicastRemoteObject.exportObject(server, 0); }
      catch (RemoteException e) { throw new WrappedException(e); }
    }
  }


  /** Test whether the given process has terminated. */
  public static boolean processIsTerminated(Process p) {
    try { p.exitValue(); return true; }
    catch (IllegalThreadStateException e) { return false; }
  }
  
  /**
   * Create a daemon thread that will invoke {@link Process#waitFor} on the given process, then run the given
   * listener.  If the thread is interrupted while blocked on {@code waitFor()}, the listener will not be run. 
   */
  public static void onProcessExit(final Process p, final Runnable1<? super Process> listener) {
    Thread t = new Thread("ConcurrentUtil.onProcessExit") {
      public void run() {
        try { p.waitFor(); listener.run(p); }
        catch (InterruptedException e) { /* terminate early */ }
      }
    };
    t.setDaemon(true);
    t.start();
  }
  
  /**
   * Create two threads to continually discard the contents of the given process's output and error streams.
   * If, instead, the streams are simply ignored, the system buffers may fill up, causing the process to block (see 
   * the class documentation for {@link Process}; experimentation under Java 5 shows the buffer size to be
   * approximately 4 KB).
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
   * approximately 4 KB).
   * @return  The thread performing the discard operation, already started.
   */
  public static Thread discardProcessOut(Process p) { return copyProcessOut(p, VoidOutputStream.INSTANCE); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's standard output to another
   * output stream.  This is a convenience method that sets {@code close} to {@code true}.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessOut(Process p, OutputStream out) { return copyProcessOut(p, out, true); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's standard output to another
   * output stream.  Processing continues until the end-of-file is reached.  If {@code close} is {@code true},
   * {@code out} will then be closed.
   * @return  The thread performing the copy operation, already started.
   */
  public static Thread copyProcessOut(Process p, OutputStream out, boolean close) {
    Thread result = new Thread(new CopyStream(p.getInputStream(), out, close), "ConcurrentUtil.copyProcessOut");
    result.setDaemon(true); // this thread should not keep the JVM from exiting
    result.start();
    return result;
  }

  /**
   * Create a task providing access to the given process's standard output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.  A separate
   * non-daemon thread performs this buffering.
   */
  public static TaskController<String> processOutAsString(Process p) {
    return computeInThread(new StreamToString(p.getInputStream()));
  }
  
  /**
   * Create a task providing access to the given process's standard output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.  A task
   * passed to {@code exec} performs this buffering.
   */
  public static TaskController<String> processOutAsString(Process p, Executor exec) {
    return computeWithExecutor(new StreamToString(p.getInputStream()), exec);
  }
  
  /**
   * Create a thread that will continually discard the contents of the given process's standard output.
   * If, instead, the stream is simply ignored, the system buffer may fill up, causing the process to block (see 
   * the class documentation for {@link Process}; experimentation under Java 5 shows the buffer size to be
   * approximately 4 KB).
   * @return  The thread performing the discard operation, already started.
   */
  public static Thread discardProcessErr(Process p) { return copyProcessErr(p, VoidOutputStream.INSTANCE); }
  
  /**
   * Create a thread that will continually copy the contents of the given process's error output to another
   * output stream.  This is a convenience method that sets {@code close} to {@code false}.
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
    Thread result = new Thread(new CopyStream(p.getErrorStream(), err, close), "ConcurrentUtil.copyProcessErr");
    result.setDaemon(true); // this thread should not keep the JVM from exiting
    result.start();
    return result;
  }
  
  /**
   * Create a task providing access to the given process's error output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.  A separate
   * non-daemon thread performs this buffering.
   */
  public static TaskController<String> processErrAsString(Process p) {
    return computeInThread(new StreamToString(p.getErrorStream()));
  }
  
  /**
   * Create a task providing access to the given process's error output as a string.  The result is not 
   * available until an end-of-file is reached, but it is buffered locally to prevent blocking.  A task
   * passed to {@code exec} performs this buffering.
   */
  public static TaskController<String> processErrAsString(Process p, Executor exec) {
    return computeWithExecutor(new StreamToString(p.getErrorStream()), exec);
  }
  
  /** Shared code for copying and closing a stream. */
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

  /** Shared code for reading a stream as a string. */
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
  
  /**
   * Get a subset of the system properties for names that match at least one of the given prefixes.
   * Returns a String map; in the unusual case that a value is not a String, it is converted 
   * via {@code toString()}.
   * @throws SecurityException  As in {@link System#getProperties}.
   */
  public static Map<String, String> getPropertiesAsMap(String... prefixes) {
    Map<String, String> result = new HashMap<String, String>();
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      for (String prefix : prefixes) {
        if (entry.getKey() instanceof String && ((String) entry.getKey()).startsWith(prefix)) {
          result.put((String) entry.getKey(), entry.getValue().toString());
          break;
        }
      }
    }
    return result;
  }
  
}
