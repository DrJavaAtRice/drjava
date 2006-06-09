package edu.rice.cs.plt.lambda;

/**
 * An arbitrary predicate for values of type T.  Implementations should return {@code true}
 * iff some property holds for {@code arg}.
 */
public interface Predicate<T> extends Lambda<T, Boolean> {
  public Boolean value(T arg);
  
  /** A predicate whose result is always {@code true} */
  public static final Predicate<Object> TRUE = new Predicate<Object>() {
    public Boolean value(Object arg) { return true; }
  };

  /** A predicate whose result is always {@code false} */
  public static final Predicate<Object> FALSE = new Predicate<Object>() {
    public Boolean value(Object arg) { return false; }
  };
  
  /** A predicate that returns {@code true} iff the argument is {@code null} */
  public static final Predicate<Object> IS_NULL = new Predicate<Object>() {
    public Boolean value(Object arg) { return arg == null; }
  };
  
  /** A predicate that returns {@code true} iff the argument is not {@code null} */
  public static final Predicate<Object> NOT_NULL = LambdaUtil.negate(IS_NULL);

}
