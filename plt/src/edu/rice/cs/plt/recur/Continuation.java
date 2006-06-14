package edu.rice.cs.plt.recur;

import edu.rice.cs.plt.lambda.Thunk;

/**
 * <p>A thunk enabling iterative evaluation of a recursive function.  If a function with
 * a continuation return type can immediately produce the result value, it wraps the value
 * in a simple continuation; otherwise, it wraps a recursive computation in a continuation
 * that, when resolved, will produce the result.</p>
 * 
 * <p>For example, this recursive function:<code>
 * boolean isEven(int x) {
 *   if (x == 0) { return true; }
 *   if (x == 1) { return false; }
 *   else { return isEven(x - 2); }
 * }
 * </code>
 * Could be written using continuations, as follows:<code>
 * Continuation<Boolean> isEven(int x) {
 *   if (x == 0) { return ValueContinuation.make(true); }
 *   if (x == 1) { return ValueContinuation.make(false); }
 *   else {
 *     return new TailContinuation<Boolean>() {
 *       public Continuation<? extends Boolean> step() { return isEven(x - 2); }
 *     };
 *   }
 * }
 * </code>
 * While evaluation of the original {@code isEven} function might lead to a stack overflow
 * on modestly large inputs, evaluation of the second will not.</p>
 * 
 * <p>To produce a value from a continuation, clients may either invoke {@link #value}
 * or iteratively invoke {@link #step} until {@link #isResolved} on the result is {@code true}.</p>
 * 
 * TODO: Implement continuations for non-tail recursion.
 */
public interface Continuation<T> extends Thunk<T> {
  
  /** Resolve the continuation to a value */
  public T value();
  
  /** @return  {@code true} iff the continuation has been resolved to a value */
  public boolean isResolved();
  
  /**
   * @return  A continuation representing the next step of compuation
   * @throws IllegalStateException  If {@code isResolved()} is {@code true}
   */
  public Continuation<? extends T> step();
}
