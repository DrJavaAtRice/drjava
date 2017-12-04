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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import edu.rice.cs.plt.collect.ListenerSet;
import edu.rice.cs.plt.collect.SnapshotSynchronizedSet;
import edu.rice.cs.plt.lambda.ResolvingThunk;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.tuple.Option;

/**
 * <p>Provides access to a concurrent task that produces a value.  Extends standard Future behavior
 * with an initial non-running state; also provides a variety of additional methods.</p>
 * 
 * <p>To implement a concrete instance, a subclass must provide {@link #doStart}, {@link #doStop},
 * and, optionally, {@link #discard}.  The {@link #state} field and related {@link State} class hierarchy
 * is protected, rather than private, in order to facilitate subclasses that modify the set and
 * behavior of internal states; these should be treated as private by most subclasses.</p>
 */
public abstract class TaskController<R> implements ResolvingThunk<R>, Future<R> {
  
  /** Current internal state; should only be modified with {@link AtomicReference#compareAndSet}. */
  protected final AtomicReference<State> state;
  private final CompletionMonitor _done; // signal means get() will not block on current state
  private volatile ListenerSet<R> _finishListeners;
  
  protected TaskController() {
    state = new AtomicReference<State>(new FreshState());
    _done = new CompletionMonitor();
    // use a SnapshotSynchronizedSet for listeners, which is thread-safe and supports efficient writes
    _finishListeners = new ListenerSet<R>(SnapshotSynchronizedSet.<Runnable1<? super R>>makeLinkedHash());
  }
  
  /** Get the current status. */
  public Status status() { return state.get().status(); }
  
  /** Check whether computation has completed &mdash; the state is either {@code FINISHED} or {@code CANCELED}. */
  public boolean isDone() { Status s = status(); return s == Status.FINISHED || s == Status.CANCELED; }
  /** Check whether the task has produced a value &mdash; the status is {@code FINISHED}. */
  public boolean hasValue() { return status() == Status.FINISHED; }
  /** Check whether the task has been canceled before completion &mdash; the status is {@code CANCELED}. */
  public boolean isCanceled() { return status() == Status.CANCELED; }
  /** Check whether the task has been canceled before completion &mdash; the status is {@code CANCELED}. */
  public boolean isCancelled() { return status() == Status.CANCELED; }
  
  /**
   * Returns {@code true} if the status is {@code FINISHED} <em>and</em> {@code value()} will return a result
   * (rather than throwing an exception).
   */
  public boolean isResolved() {
    // cast to workaround limitation in Eclipse compiler
    return ((Object) state.get()) instanceof TaskController.CleanlyFinishedState;
  }
  
  /**
   * Request that the task be run.  If the task is {@code RUNNING} or {@code FINISHED}, has no effect.
   * There may be a delay between this invocation's return and a state change to {@code RUNNING}.
   * @throws CancellationException  If the task is {@code CANCELED}.
   */
  public void start() { state.get().start(); }
  
  /**
   * Request that the task be abandoned, and that any resources associated with it be disposed.
   * This convenience method sets {@code stopRunning} to {@code true}.
   * @return  {@code true} if the task has not yet finished.
   */
  public boolean cancel() { return state.get().cancel(true); }
  
  /**
   * Request that the task be abandoned, and that any resources associated with it be disposed.  
   * {@code PAUSED} tasks are always canceled immediately.  If {@code stopRunning}, a {@code RUNNING}
   * task will also be asked to terminate, but there may be a delay between this invocation's return
   * and a state change to {@code CANCELED}; and if the task completes successfully in the interim,
   * the cancel request is ignored.  (If {@code stopRunning} is {@code false}, {@code RUNNING} tasks are
   * allowed to run to completion.)
   * @param stopRunning  Whether a task that has begun running should be canceled.
   * @return {@code true} if the task has not yet started, or if {@code stopRunning} is {@code true}
   *         and the task has not yet finished.
   */
  public boolean cancel(boolean stopRunning) { return state.get().cancel(stopRunning); }
  
  /**
   * Get the result.  If {@link #isDone} is false, ensure that the task is {@code RUNNING} and block until it
   * finishes.
   * @throws WrappedException  Wraps any exception encountered, as documented by {@link #get()}.  For simplicity,
   *                           all exceptions, including RuntimeExceptions, are wrapped.
   */
  public R value() {
    try { return state.get().get(); }
    catch (Exception e) { throw new WrappedException(e); }
  }
  
  /**
   * Get the result.  If {@link #isDone} is false, ensure that the task is {@code RUNNING} and block until it
   * finishes.
   * @throws InterruptedException  If the current thread is interrupted while waiting.
   * @throws ExecutionException  If the running task terminated early with an exception.
   * @throws CancellationException  If the final state is {@code CANCELED} rather than {@code FINISHED}.
   * @throws RuntimeException  Any other exception that occurs in the controller implementation.
   */
  public R get() throws InterruptedException, ExecutionException { return state.get().get(); }
  
  /**
   * Get the result.  If {@link #isDone} is false, ensure that the task is {@code RUNNING} and block until it
   * finishes, or until a timeout is reached.
   * @param timeout  Maximum wait time, in milliseconds.
   * @throws InterruptedException  If the current thread is interrupted while waiting.
   * @throws TimeoutException  If the current thread times out while waiting.
   * @throws ExecutionException  If the running task terminated early with an exception.
   * @throws CancellationException  If the final state is {@code CANCELED} rather than {@code FINISHED}.
   * @throws RuntimeException  Any other exception that occurs in the controller implementation.
   */
  public R get(long timeout) throws InterruptedException, ExecutionException, TimeoutException {
    return get(timeout, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Get the result.  If {@link #isDone} is false, ensure that the task is {@code RUNNING} and block until it
   * finishes, or until a timeout is reached.
   * @param timeout  Maximum wait time, in {@code unit} units.
   * @param unit  Units for {@code timeout}.
   * @throws InterruptedException  If the current thread is interrupted while waiting.
   * @throws TimeoutException  If the current thread times out while waiting.
   * @throws ExecutionException  If the running task terminated early with an exception.
   * @throws CancellationException  If the final state is {@code CANCELED} rather than {@code FINISHED}.
   * @throws RuntimeException  Any other exception that occurs in the controller implementation.
   */
  public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return state.get().get(timeout, unit);
  }
  
  /**
   * Try to get the result within the given amount of time.  If {@link #isDone} is false, ensure that the task
   * is {@code RUNNING} and block until it finishes, or until a timeout is reached.
   * @param timeout  Maximum wait time, in milliseconds.
   * @return The wrapped result, if it is available before an interrupt or timeout occurs; otherwise, "none".
   * @throws ExecutionException  If the running task terminated early with an exception.
   * @throws CancellationException  If the final state is {@code CANCELED} rather than {@code FINISHED}.
   * @throws RuntimeException  Any other exception that occurs in the controller implementation.
   */
  public Option<R> attemptGet(long timeout) throws ExecutionException {
    try { return Option.some(get(timeout, TimeUnit.MILLISECONDS)); }
    catch (InterruptedException e) { return Option.none(); }
    catch (TimeoutException e) { return Option.none(); }
  }
  
  /**
   * Try to get the result within the given amount of time.  If {@link #isDone} is false, ensure that the task
   * is {@code RUNNING} and block until it finishes, or until a timeout is reached.
   * @param timeout  Maximum wait time, in {@code unit} units.
   * @param unit  Units for {@code timeout}.
   * @return The wrapped result, if it is available before an interrupt or timeout occurs; otherwise, "none".
   * @throws ExecutionException  If the running task terminated early with an exception.
   * @throws CancellationException  If the final state is {@code CANCELED} rather than {@code FINISHED}.
   * @throws RuntimeException  Any other exception that occurs in the controller implementation.
   */
  public Option<R> attemptGet(long timeout, TimeUnit unit) throws ExecutionException {
    try { return Option.some(get(timeout, unit)); }
    catch (InterruptedException e) { return Option.none(); }
    catch (TimeoutException e) { return Option.none(); }
  }

  /**
   * Access the ListenerSet responding to the completion of computation.  Registered listeners will only be
   * run if the task completes cleanly (without throwing an exception) and the registration occurs before
   * the task completes. 
   */
  public ListenerSet<R>.Sink finishListeners() { return _finishListeners.sink(); }
  
  /**
   * Begin computation and call {@link #started}.  If starting does not occur immediately (for example,
   * blocking occurs first), the {@code started()} call may occur in a different thread.
   */
  protected abstract void doStart();

  /**
   * Terminate computation and call {@link #stopped}.  Never called before {@code started()} has been
   * invoked.  If termination does not occur immediately (for example, blocking occurs first), the
   * {@code stopped()} call may occur in a different thread.
   */
  protected abstract void doStop();
  
  /**
   * Clean up after the task has completed.  Called whenever the task enters a canceled or finished
   * state (for example, when {@code finishedCleanly()} has been called, or when {@code cancel()} is
   * invoked on a {@code PAUSED} controller).  By default, does nothing, but can be overridden to close 
   * connections or throw away unnecessary objects.  (Where TaskControllers live far beyond their computation
   * life span (as simple wrappers for a value), this allows objects related to the computation to be
   * garbage-collected in the interim.)
   */
  protected void discard() {}
  
  /**
   * Called by by the constructor (or a thread spawned by the constructor) when startup is complete.
   * Must be invoked before any of the completion methods ({@code stopped()}, {@code finishedCleanly()},
   * etc.)
   */
  protected final void started() {
    boolean kept = false;
    State current = state.get();
    State next = runningState();
    // cast to workaround limitation in Eclipse compiler
    while (((Object) current) instanceof TaskController.StartingState && !kept) {
      // must loop because a FreshStartingState->CanceledStartingState transition could occur concurrently
      // can use weakCompareAndSet since we're already in a while loop
      kept = state.weakCompareAndSet(current, next);
      if (kept) { ((StartingState) current).started(); }
      else { current = state.get(); }
    }
  }
  
  /**
   * Called by an invocation of {@link #doStop} when stopping is complete.  May also be invoked if
   * the task terminates in a canceled-like state, even if {@code stop()} was never invoked.
   */
  protected final void stopped() { finished(new CanceledState()); }
  
  /** Called when running has completed and produced a value. */
  protected final void finishedCleanly(R result) {
    finished(new CleanlyFinishedState(result));
    _finishListeners.run(result);
    _finishListeners.clear(); // discard listeners, since they won't be used again
  }
  
  /** Called when the running task terminates early with an exception. */
  protected final void finishedWithTaskException(Exception e) {
    finished(new ExecutionExceptionState(new ExecutionException(e)));
  }
  
  /** Called when the runner implementation encounters an exception. */
  protected final void finishedWithImplementationException(RuntimeException e) {
    finished(new InternalExceptionState(e));
  }
  
  private final void finished(State finishedState) {
    State current = state.get();
    boolean changed = false;
    while (!changed && current.status() != Status.CANCELED && current.status() != Status.FINISHED) {
      // can use weakCompareAndSet since we're already in a while loop
      changed = state.weakCompareAndSet(current, finishedState);
      if (changed) { _done.signal(); discard(); }
      else { current = state.get(); }
    }
  }

  /** Produce a running state.  May be overridden to change the way RunningStates are implemented. */
  protected RunningState runningState() { return new RunningState(); }
  
  
  public static enum Status {
    /** The task is not currently running, but can be started. */
    PAUSED,
    /** The task is currently executing. */
    RUNNING,
    /** The task has completed, and the result is available. */
    FINISHED,
    /** The task has been stopped and cannot be restarted.  No result is available. */
    CANCELED;
  }
  
  
  /** An internal state for the controller, implementing the controller's core behavior. */
  protected abstract class State {
    public abstract Status status();
    public abstract void start();
    public abstract boolean cancel(boolean stopRunning);
    public abstract R get() throws InterruptedException, ExecutionException;
    public abstract R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException;
  }
  
  /** Any state in which the task must be started before it can complete. */
  protected abstract class WaitingState extends State {
    public Status status() { return Status.PAUSED; }
    public boolean cancel(boolean stopRunning) {
      if (state.compareAndSet(this, new CanceledState())) {
        _done.signal();
        discard();
        return true;
      }
      else { return state.get().cancel(stopRunning); }
    }
    public R get() throws InterruptedException, ExecutionException {
      start(); return state.get().get();
    }
    public R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException {
      start(); return state.get().get(timeout, u);
    }
  }
  
  /** Any state in which the task has already been asked to compute a result. */
  protected abstract class ComputingState extends State {
    public R get() throws InterruptedException, ExecutionException {
      _done.ensureSignaled();
      return state.get().get();
    }
    public R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException {
      _done.ensureSignaled(timeout, u);
      return state.get().get(); // will not block, so can't timeout
    }
  }
  
  /** Initial state. */
  protected class FreshState extends WaitingState {
    public void start() {
      if (state.compareAndSet(this, new FreshStartingState())) { doStart(); }
      else { state.get().start(); }
    }
  }
  
  /** {@code FreshState.start()} has been invoked, but startup is not yet complete. */ 
  protected abstract class StartingState extends ComputingState {
    public Status status() { return Status.PAUSED; }
    public void start() {}
    public boolean cancel(boolean stopRunning) {
      if (stopRunning) {
        if (state.compareAndSet(this, new CanceledStartingState())) { return true; }
        else { return state.get().cancel(stopRunning); }
      }
      else { return false; }
    }
    /** Operation to perform when starting is complete */
    public abstract void started();
  }
  
  /** Simple instance of StartingState. */
  protected class FreshStartingState extends StartingState {
    public void started() {}
  }
  
  /** A StartingState that has been canceled while waiting for startup to complete. */
  protected class CanceledStartingState extends StartingState {
    public boolean cancel(boolean stopRunning) { return stopRunning; }
    public void started() { state.get().cancel(true); }
  }
  
  /** Startup has completed and we're waiting for a result. */
  protected class RunningState extends ComputingState {
    public Status status() { return Status.RUNNING; }
    public void start() {}
    public boolean cancel(boolean stopRunning) {
      if (stopRunning) {
        if (state.compareAndSet(this, new CancelingState())) { doStop(); return true; }
        else { return state.get().cancel(stopRunning); }
      }
      else { return false; }
    }
  }
  
  /** Canceled while running; waiting for termination to complete. */
  protected class CancelingState extends ComputingState {
    public Status status() { return Status.RUNNING; }
    public void start() {}
    public boolean cancel(boolean stopRunning) { return stopRunning; }
  }
  
  /** Any state for a task that has finished. */
  protected abstract class FinishedState extends State {
    public Status status() { return Status.FINISHED; }
    public void start() {}
    public boolean cancel(boolean stopRunning) { return false; }
  }
  
  /**
   * Finished with a result.  Only states with this type are "resolved" -- others should either
   * require additional computation or throw an exception.
   */
  protected class CleanlyFinishedState extends FinishedState {
    private R _result;
    public CleanlyFinishedState(R result) { _result = result; }
    public R get() { return _result; }
    public R get(long timeout, TimeUnit u) { return _result; }
  }
  
  /** Finished with an ExecutionException. */
  protected class ExecutionExceptionState extends FinishedState {
    private ExecutionException _e;
    public ExecutionExceptionState(ExecutionException e) { _e = e; }
    public R get() throws ExecutionException { throw _e; }
    public R get(long timeout, TimeUnit u) throws ExecutionException { throw _e; }
  }
  
  /** Finished with a RuntimeException. */
  protected class InternalExceptionState extends FinishedState {
    private RuntimeException _e;
    public InternalExceptionState(RuntimeException e) { _e = e; }
    public R get() { throw _e; }
    public R get(long timeout, TimeUnit u) throws ExecutionException { throw _e; }
  }
  
  /** Has been successfully canceled. */
  protected class CanceledState extends State {
    public Status status() { return Status.CANCELED; }
    public void start() { throw new CancellationException("Task is canceled"); }
    public boolean cancel(boolean stopRunning) { return false; }
    public R get() { throw new CancellationException("Task is canceled"); }
    public R get(long timeout, TimeUnit u) { throw new CancellationException("Task is canceled"); }
  }
  
}
