package edu.rice.cs.plt.lambda;

/** An arbitrary ternary predicate for values of type T1, T2, and T3 */
public interface Predicate3<T1, T2, T3> extends Lambda3<T1, T2, T3, Boolean> {
  public Boolean value(T1 arg1, T2 arg2, T3 arg3);
  
  /** A predicate whose result is always {@code true} */
  public static final Predicate3<Object, Object, Object> TRUE = 
    new Predicate3<Object, Object, Object>() {
      public Boolean value(Object arg1, Object arg2, Object arg3) { return true; }
    };

  /** A predicate whose result is always {@code false} */
  public static final Predicate3<Object, Object, Object> FALSE = 
    new Predicate3<Object, Object, Object>() {
      public Boolean value(Object arg1, Object arg2, Object arg3) { return false; }
    };

}
