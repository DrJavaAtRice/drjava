package edu.rice.cs.plt.recur;

/** 
 * A continuation representing a tail call in a method &mdash; the result
 * of the method can be described as the result of some other method.
 * {@code TailContinuation}s may be created by defining a local class that
 * implements the {@link #step()} method; the body of {@code step} should
 * contain the delayed method call.
 */
public abstract class TailContinuation<T> implements Continuation<T> {
  
  /** Iteratively resolve the continuation, returning the final result */
  public T value() {
    Continuation<? extends T> k = this;
    while (!k.isResolved()) { k = k.step(); }
    return k.value();
  }
  
  /** @return  {@code false} */
  public boolean isResolved() { return false; }
  
  /**
   * Defines the next step of the continuation, which is generally a recursive invocation
   * of the current method (or a related method)
   */
  public abstract Continuation<? extends T> step();
  
}
