package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code parameterized by three arguments.
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param T3  the third argument type
 * @param R  the return type
 *
 * @see Thunk
 * @see Lambda
 * @see Lambda2
 * @see Lambda4
 * @see Command3
 */
public interface Lambda3<T1, T2, T3, R> {
  public R value(T1 arg1, T2 arg2, T3 arg3);
}
