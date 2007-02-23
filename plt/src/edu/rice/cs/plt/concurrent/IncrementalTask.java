package edu.rice.cs.plt.concurrent;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Thunk;

/**
 * A thunk that performs incremental computation.  Clients will repeatedly invoke {@link #step}
 * until {@link #isFinished} is {@code true}, signifying that the result has been computed.
 * {@link #value} need only be supported <em>after</em> the task has completed.
 * @param I  The type of the incremental result (may be {@link Void} if there is no useful
 *           intermediate result)
 * @param R  The type of the final result (may be {@link Void} if the task has no useful final result)
 */
public interface IncrementalTask<I, R> extends Thunk<R> {
  /**
   * Whether the final result is ready. As long as this returns {@code false}, {@link #step} will
   * be invoked; after returning {@code true}, only {@link #value} will be invoked.
   */
  public boolean isFinished();
  
  /** Perform a step in the computation.  Undefined when {@link #isFinished} is {@code true}. */
  public I step();
  
  /** Produce the final result of the task.  Undefined when {@link #isFinished} is {@code false}. */
  public R value();
}
