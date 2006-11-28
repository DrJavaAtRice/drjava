package edu.rice.cs.plt.lambda;

/**
 * A thunk that doesn't have a return value (relying instead on side effects).
 *
 * @see Command1
 * @see Command2
 * @see Command3
 * @see Command4
 * @see Thunk
 */
public interface Command extends Runnable {
  
  public void run();
  
  /** A Command that does nothing */
  public static final Command EMPTY = new Command() { public void run() {} };

}
