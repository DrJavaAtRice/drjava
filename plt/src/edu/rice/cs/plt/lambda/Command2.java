package edu.rice.cs.plt.lambda;

/**
 * A binary lambda that doesn't have a return value (relying instead on side effects).
 * 
 * @param T1  the first argument type
 * @param T2  the second argument type
 *
 * @see Command
 * @see Command1
 * @see Command3
 * @see Command4
 * @see Lambda2
 */
public interface Command2<T1, T2> {
  
  public void run(T1 arg1, T2 arg2);
  
  /** A Command that does nothing */
  public static final Command2<Object, Object> EMPTY = 
    new Command2<Object, Object>() { public void run(Object a1, Object a2) {} };

}
