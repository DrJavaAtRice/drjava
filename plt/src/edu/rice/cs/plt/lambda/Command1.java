package edu.rice.cs.plt.lambda;

/**
 * A lambda that doesn't have a return value (relying instead on side effects).
 * 
 * @param T  the argument type
 *
 * @see Command
 * @see Command2
 * @see Command3
 * @see Command4
 * @see Lambda
 */
public interface Command1<T> {
  
  public void run(T arg);
  
  /** A Command that does nothing */
  public static final Command1<Object> EMPTY = 
    new Command1<Object>() { public void run(Object arg) {} };

}
