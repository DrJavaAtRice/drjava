package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code parameterized by four arguments.
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param T3  the third argument type
 * @param T4  the fourth argument type
 * @param R  the return type
 *
 * @see Thunk
 * @see Lambda
 * @see Lambda2
 * @see Lambda3
 * @see Command4
 */
public interface Lambda4<T1, T2, T3, T4, R> {
  public R value(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
}
