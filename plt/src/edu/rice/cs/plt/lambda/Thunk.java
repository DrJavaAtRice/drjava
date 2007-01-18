package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code that returns a value with the provided type.
 * 
 * @see Lambda
 * @see Lambda2
 * @see Lambda3
 * @see Lambda4
 * @see Runnable
 */
public interface Thunk<R> {
  public R value();
}
