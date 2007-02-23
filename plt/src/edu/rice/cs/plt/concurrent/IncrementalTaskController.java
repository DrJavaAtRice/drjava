package edu.rice.cs.plt.concurrent;

import java.util.List;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * Provides access to a concurrent task that produces incremental results. In addition to adding
 * a {@link #pause} method and an {@link #intermediateValues} method to {@code TaskController}, 
 * implementations should support cancellation of a running task.
 */
public abstract class IncrementalTaskController<I, R> extends TaskController<R> {
  
  /**
   * Request that the task be paused.  After pausing, the task will not continue executing unless 
   * {@link #start} is invoked.  If the the task is {@code PAUSED} or {@code FINISHED}, has no effect.
   * @throws IllegalStateException  If the task is {@code CANCELLED}.
   */
  public void pause() {
    switch (_status) {
      case RUNNING: doPause(); break;
      case CANCELLED: throw new IllegalStateException("Task is cancelled");
    }
  }
  
  /**
   * Get a list of intermediate results.  Every intermediate result will be available via this method exactly 
   * once.  If no results are currently available (and the task is not finished), run the
   * task and block until an intermediate or final result is available.
   * @throws IllegalStateException  If the task has been {@code CANCELLED}.
   * @throws WrappedException  Wraps an {@link InterruptedException} if the current thread is interrupted
   *                           while waiting for a result.  (Other errors that occur during execution will
   *                           instead be thrown by {@link #value}.)
   */
  public List<I> intermediateValues() {
    if (_status != Status.CANCELLED) {
      try { return getIntermediateValues(); }
      catch (InterruptedException e) { throw new WrappedException(e); }
    }
    else { throw new IllegalStateException("Task is cancelled"); }
  }
  
  /** Check whether the task has an intermediate value available to return. */
  public abstract boolean hasIntermediateValue();
  
  /**
   * Implementation for pausing the task.  Should cause the status to be updated to {@code PAUSED}
   * (but need not do so directly).  May assume the current status is {@code RUNNING}.
   */
  protected abstract void doPause();
  
  /**
   * Implementation for accessing intermediate results.  Should block until the task is finished or at least
   * 1 result is available.  May assume the current status is not {@code CANCELLED}.
   */
  protected abstract List<I> getIntermediateValues() throws InterruptedException;
  
}
