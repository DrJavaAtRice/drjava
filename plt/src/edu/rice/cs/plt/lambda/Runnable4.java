package edu.rice.cs.plt.lambda;

/**
 * A quaternary lambda that doesn't have a return value (relying instead on side effects).
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param T3  the third argument type
 * @param T4  the fourth argument type
 *
 * @see Runnable
 * @see Runnable1
 * @see Runnable2
 * @see Runnable3
 * @see Lambda4
 */
public interface Runnable4<T1, T2, T3, T4> {
  
  public void run(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
  
  /** A Runnable4 that does nothing */
  public static final Runnable4<Object, Object, Object, Object> EMPTY = 
    new Runnable4<Object, Object, Object, Object>() { 
      public void run(Object a1, Object a2, Object a3, Object a4) {}
    };

}
