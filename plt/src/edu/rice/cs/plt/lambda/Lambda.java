package edu.rice.cs.plt.lambda;

import edu.rice.cs.plt.recur.RecurUtil;

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
 * @see Command1
 */
public interface Lambda<T, R> {
  public R value(T arg);
  
  /** Calls {@link RecurUtil#safeToString(Object)} on the input */
  public static final Lambda<Object, String> TO_STRING = new Lambda<Object, String>() {
    public String value(Object obj) { return RecurUtil.safeToString(obj); }
  };
  
  /** Calls {@link RecurUtil#safeHashCode(Object)} on the input */
  public static final Lambda<Object, Integer> HASH_CODE = new Lambda<Object, Integer>() {
    public Integer value(Object obj) { return RecurUtil.safeHashCode(obj); }
  };
  
}
