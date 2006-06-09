package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code that returns a value with the provided type.
 * 
 * @see Lambda
 * @see Lambda2
 * @see Lambda3
 * @see Lambda4
 * @see Command
 */
public interface Thunk<T> {
  public T value();
}
