package edu.rice.cs.plt.lambda;

/** An arbitrary ternary predicate for values of type T1, T2, and T3 */
public interface Predicate3<T1, T2, T3> extends Lambda3<T1, T2, T3, Boolean> {
  public Boolean value(T1 arg1, T2 arg2, T3 arg3);
}
