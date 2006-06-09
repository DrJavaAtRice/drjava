package edu.rice.cs.plt.lambda;

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
  
  public static final Lambda<Object, String> TO_STRING = new Lambda<Object, String>() {
    public String value(Object obj) { return obj.toString(); }
  };
  
  public static final Lambda<Object, Integer> HASH_CODE = new Lambda<Object, Integer>() {
    public Integer value(Object obj) { return obj.hashCode(); }
  };
  
}
