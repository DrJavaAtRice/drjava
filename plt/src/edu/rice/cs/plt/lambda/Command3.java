package edu.rice.cs.plt.lambda;

/**
 * A ternary lambda that doesn't have a return value (relying instead on side effects).
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 * @param T3  the third argument type
 *
 * @see Command
 * @see Command1
 * @see Command2
 * @see Command4
 * @see Lambda3
 */
public interface Command3<T1, T2, T3> {
  
  public void run(T1 arg1, T2 arg2, T3 arg3);
  
  /** A Command that does nothing */
  public static final Command3<Object, Object, Object> EMPTY = 
    new Command3<Object, Object, Object>() { public void run(Object a1, Object a2, Object a3) {} };

}
