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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rice.cs.plt.collect.ListenerSet;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * <p>Provides access to a concurrent task that produces incremental results. Adds
 * a {@link #pause} method, more responsive cancellation, and access to intermediate computation
 * results via {@link #steps} and {@link #intermediateQueue}.</p>
 * 
 * <p>To implement a concrete instance, a subclass must provide {@link #doStart}, {@link #doPause},
 * {@link #doStop}, and, optionally, {@link #discard}.</p>
 */
public abstract class IncrementalTaskController<I, R> extends TaskController<R> {
  
  private final boolean _ignoreIntermediate;
  private final AtomicInteger _steps;
  private final BlockingQueue<I> _intermediateQueue;
  private final ListenerSet<I> _intermediateListeners;
  
  /** Sets {@code ignoreIntermediate} to {@code false}. */
  protected IncrementalTaskController() { this(false); }

  /**
   * If {@code ignoreIntermediate}, intermediate results will not be enqueued.  (They will,
   * however, be passed to any listeners and counted by {@link #steps}.)
   */
  protected IncrementalTaskController(boolean ignoreIntermediate) {
    _ignoreIntermediate = ignoreIntermediate;
    _steps = new AtomicInteger(0);
    _intermediateQueue = _ignoreIntermediate ? null : new LinkedBlockingQueue<I>();
    _intermediateListeners = new ListenerSet<I>();
  }
  
  /**
   * Get the number of intermediate steps the task has taken.  If {@code storeIntermediate}, at
   * least this many values have been added to the {@code intermediateQueue}.  
   */
  public int steps() { return _steps.get(); }
  
  /**
   * <p>Get the queue for storing intermediate results.  Throws an exception if {@code storeIntermediate}
   * is {@code false}; otherwise each intermediate result is added to this queue.  Clients can check
   * the controller's status to determine if no additional results will be enqueued.  However, there
   * is no guarantee (in general) that a running task will produce an intermediate result before completing.
   * (The task being run may follow some convention for indicating termination via the queue.)  While the
   * result is intended to be used only for read operations, given the lack of a nice interface for
   * separating queue reads from queue writes, the result allows write access to the queue as well.</p>
   * 
   * <p>IncrementalTaskController implementations should invoke {@link #stepped} to add items to
   * the queue, rather than doing so directly.</p>
   * @throws IllegalStateException  If {@code storeIntermediate} is {@code false}.
   */
  public BlockingQueue<I> intermediateQueue() {
    if (_ignoreIntermediate) { throw new IllegalStateException("No queue is maintained"); }
    else { return _intermediateQueue; }
  }
  
  /**
   * Access the ListenerSet responding to the availability of intermediate results.  Registered listeners
   * will be run for each intermediate result that becomes available.
   */
  public ListenerSet<I>.Sink intermediateListeners() {
    return _intermediateListeners.sink();
  }
  
  /**
   * Request that the task be paused.  After pausing, the task will not continue executing unless 
   * {@link #start} is invoked.  If the the task is {@code PAUSED} or {@code FINISHED}, has no effect.
   * @throws CancellationException  If the task is {@code CANCELED}.
   */
  public void pause() {
    // ideally, this would be implemented as part of the state, but we can't do that without
    // redefining the entire hierarchy of state classes
    boolean success = false;
    do {
      State s = state.get();
      Object sObj = s; // workaround for Eclipse compiler limitations
      if (sObj instanceof TaskController.RunningState) {
        success = state.compareAndSet(s, new FreshPausingState());
        if (success) { doPause(); }
      }
      else if (sObj instanceof TaskController.FreshStartingState) {
        success = state.compareAndSet(s, new PausedStartingState());
      }
      else if (sObj instanceof IncrementalTaskController.StartedPausingState) {
        success = state.compareAndSet(s, new FreshPausingState());
      }
      else if (sObj instanceof TaskController.CanceledState) {
        throw new CancellationException("Task is canceled");
      }
      else { // ignore other fresh, paused, finished, pausing, or canceling states
        success = true;
      }
    } while (!success);
  }
  
  /**
   * Pause computation and call {@link #paused}.  If pausing does not occur immediately, the {@code paused()}
   * call may occur in a different thread.  Will only be called after {@code started()} has been invoked.
   * When execution should resume again, {@code doStart()} will be invoked (but only after {@code paused()}
   * has been called). In order to support responsive canceling, a call to {@code doStop()} may occur
   * concurrently, or before {@code paused()} is called.
   */
  protected abstract void doPause();
  
  /**
   * Resume computation (after a pause) and call {@link #started}.  If starting does not occur immediately
   * (for example, blocking occurs first), the {@code started()} call may occur in a different thread.
   */
  protected abstract void doResume();
  
  @SuppressWarnings("unchecked")
  protected void paused() {
    boolean kept = false;
    State current = state.get();
    State next = new PausedState();
    // cast to Object as workaround for Eclipse compiler limitation
    while (current instanceof IncrementalTaskController.PausingState && ! kept) {
      // must loop because a transition between PausingStates could occur concurrently
      // can use weakCompareAndSet since we're already in a while loop
      kept = state.weakCompareAndSet(current, next);
      if (kept) { ((PausingState) current).paused(); }
      else { current = state.get(); }
    }
  }
  
  /** Record an intermediate result.  Should only be called while the controller is in a running state. */
  protected void stepped(I intermediateResult) {
    if (!_ignoreIntermediate) {
      try { _intermediateQueue.put(intermediateResult); }
      // shouldn't block in the current implementation, but if that changes, we should throw the
      // InterruptedException -- it may have been caused by a doCancel() implementation.
      catch (InterruptedException e) { throw new WrappedException(e); }
    }
    _steps.incrementAndGet();
    _intermediateListeners.run(intermediateResult);
  }
  
  /** The tasked has been started and then paused. */
  protected class PausedState extends WaitingState {
    public final void start() {
      if (state.compareAndSet(this, new FreshStartingState())) { doResume(); }
      else { state.get().start(); }
    }
  }
  
  /** pause() has been invoked on a RunningState, but the task has not yet paused. */
  protected abstract class PausingState extends ComputingState {
    public Status status() { return Status.RUNNING; }
    public boolean cancel(boolean stopRunning) {
      if (stopRunning) {
        if (state.compareAndSet(this, new CanceledPausingState())) { doStop(); return true; }
        else { return state.get().cancel(stopRunning); }
      }
      else { return false; }
    }
    /** Operation to perform when pausing is complete */
    public abstract void paused();
  }
  
  /** Simple instance of PausingState. */
  protected class FreshPausingState extends PausingState {
    public void start() {
      if (!state.compareAndSet(this, new StartedPausingState())) { state.get().start(); }
    }
    public R get() throws InterruptedException, ExecutionException {
      start(); return state.get().get();
    }
    public R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException {
      start(); return state.get().get(timeout, u);
    }
    public void paused() {}
  }
  
  /** A PausingState that has been canceled while waiting for the pause to complete. */
  protected class CanceledPausingState extends PausingState {
    public void start() {} // we're already committed to canceling
    public boolean cancel(boolean stopRunning) { return stopRunning; }
    public void paused() { state.get().cancel(true); }
  }
  
  /** A PausingState that has been started while waiting for the pause to complete. */
  protected class StartedPausingState extends PausingState {
    public void start() {}
    public void paused() { state.get().start(); }
  }
  
  /** A StartingState that has been paused while waiting for startup to complete. */
  protected class PausedStartingState extends StartingState {
    public void start() {
      if (!state.compareAndSet(this, new FreshStartingState())) { state.get().start(); }
    }
    public void started() { IncrementalTaskController.this.pause(); }
  }
  
}
