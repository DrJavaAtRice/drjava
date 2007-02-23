package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code parameterized by two arguments.
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param R  the return type
 *
 * @see Thunk
 * @see Lambda
 * @see Lambda3
 * @see Lambda4
 * @see Runnable2
 */
public interface Lambda2<T1, T2, R> {
  public R value(T1 arg1, T2 arg2);
}
