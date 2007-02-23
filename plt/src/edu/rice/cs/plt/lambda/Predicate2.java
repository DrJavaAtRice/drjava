package edu.rice.cs.plt.lambda;

/** An arbitrary binary predicate for values of type T1 and T2 */
public interface Predicate2<T1, T2> extends Lambda2<T1, T2, Boolean> {
  public Boolean value(T1 arg1, T2 arg2);  
}
