package edu.rice.cs.plt.lambda;

import edu.rice.cs.plt.recur.RecurUtil;

/** An arbitrary binary predicate for values of type T1 and T2 */
public interface Predicate2<T1, T2> extends Lambda2<T1, T2, Boolean> {
  public Boolean value(T1 arg1, T2 arg2);
  
  /** A predicate whose result is always {@code true} */
  public static final Predicate2<Object, Object> TRUE = new Predicate2<Object, Object>() {
    public Boolean value(Object arg1, Object arg2) { return true; }
  };

  /** A predicate whose result is always {@code false} */
  public static final Predicate2<Object, Object> FALSE = new Predicate2<Object, Object>() {
    public Boolean value(Object arg1, Object arg2) { return false; }
  };
  
  /**
   * A predicate that evaluates to {@link RecurUtil#safeEquals(Object, Object)}
   * applied to the arguments
   */
  public static final Predicate2<Object, Object> EQUAL = new Predicate2<Object, Object>() {
    public Boolean value(Object arg1, Object arg2) {
      if (arg1 == null) { return arg2 == null; }
      else if (arg2 == null) { return false; }
      else { return arg1.equals(arg2); }
    }
  };
  
  /**
   * A predicate that evaluates to the opposite of {@link RecurUtil#safeEquals(Object, Object)}
   * applied to the arguments
   */
  public static final Predicate2<Object, Object> NOT_EQUAL = LambdaUtil.negate2(EQUAL);
  
  /** A predicate that returns {@code true} iff {@code arg1 == arg2} */
  public static final Predicate2<Object, Object> IDENTICAL = new Predicate2<Object, Object>() {
    public Boolean value(Object arg1, Object arg2) { return arg1 == arg2; }
  };
  
  /** A predicate that returns {@code true} iff {@code arg1 != arg2} */
  public static final Predicate2<Object, Object> NOT_IDENTICAL = LambdaUtil.negate2(IDENTICAL);

}
