package edu.rice.cs.plt.lambda;

/**
 * An arbitrary piece of code parameterized by two arguments.
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param R  the return type
 *
 * @see Thunk
 * @see Lambda
 * @see Lambda3
 * @see Lambda4
 * @see Runnable2
 */
public interface Lambda2<T1, T2, R> {
  public R value(T1 arg1, T2 arg2);
  
  /** Add two integers */
  public static final Lambda2<Integer, Integer, Integer> ADD_INT = 
    new Lambda2<Integer, Integer, Integer>() {
      public Integer value(Integer x, Integer y) { return x+y; }
    };
  
  /** Subtract two integers */
  public static final Lambda2<Integer, Integer, Integer> SUBTRACT_INT = 
    new Lambda2<Integer, Integer, Integer>() {
      public Integer value(Integer x, Integer y) { return x-y; }
    };
  
  /** Multiply two integers */
  public static final Lambda2<Integer, Integer, Integer> MULTIPLY_INT = 
    new Lambda2<Integer, Integer, Integer>() {
      public Integer value(Integer x, Integer y) { return x*y; }
    };
  
  /** Divide two integers */
  public static final Lambda2<Integer, Integer, Integer> DIVIDE_INT = 
    new Lambda2<Integer, Integer, Integer>() {
      public Integer value(Integer x, Integer y) { return x/y; }
    };
    
}
