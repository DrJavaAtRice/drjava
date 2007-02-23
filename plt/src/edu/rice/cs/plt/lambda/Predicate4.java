package edu.rice.cs.plt.lambda;

/** An arbitrary quaternary predicate for values of type T1, T2, T3, and T4 */
public interface Predicate4<T1, T2, T3, T4> extends Lambda4<T1, T2, T3, T4, Boolean> {
  public Boolean value(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
}
