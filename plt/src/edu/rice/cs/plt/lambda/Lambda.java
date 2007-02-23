package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code parameterized by an argument.
 * 
 * @param T  the argument type
 * @param R  the return type
 * 
 * @see Thunk
 * @see Lambda2
 * @see Lambda3
 * @see Lambda4
 * @see Runnable1
 */
public interface Lambda<T, R> {
  public R value(T arg);
}
