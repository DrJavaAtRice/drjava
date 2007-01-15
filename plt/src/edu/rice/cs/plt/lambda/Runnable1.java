package edu.rice.cs.plt.lambda;

/**
 * A lambda that doesn't have a return value (relying instead on side effects).
 * 
 * @param T  the argument type
 *
 * @see Runnable
 * @see Runnable2
 * @see Runnable3
 * @see Runnable4
 * @see Lambda
 */
public interface Runnable1<T> {
  
  public void run(T arg);
  
  /** A Runnable that does nothing */
  public static final Runnable1<Object> EMPTY = 
    new Runnable1<Object>() { public void run(Object arg) {} };

}
