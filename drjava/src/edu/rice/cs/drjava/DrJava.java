package edu.rice.cs.drjava;

import java.io.PrintStream;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.PreventExitSecurityManager;

/** 
 * Main class for DrJava. 
 * @version $Id$
 */
public class DrJava {
  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  private static PreventExitSecurityManager _manager
    = PreventExitSecurityManager.activate();

  public static void main(String[] args) {
    try {
      enableSecurityManager();
      MainFrame mf = new MainFrame();
      mf.show();
    } catch (Exception ex) {
      _consoleErr.println(ex.getClass().getName() + ": " + ex.getMessage());
      ex.printStackTrace(_consoleErr);
    }
  }

  public static PreventExitSecurityManager getSecurityManager() {
    return _manager;
  }

  public static void enableSecurityManager() {
    if (System.getSecurityManager() != _manager) {
      System.setSecurityManager(_manager);
    }
  }

  public static void disableSecurityManager() {
    _manager.deactivate();
  }

  /**
   * Get the actual System.err stream.
   * @return System.err
   */
  public static PrintStream consoleErr() {
    return  _consoleErr;
  }

  /**
   * Get the actual System.out stream.
   * @return System.out
   */
  public static PrintStream consoleOut() {
    return  _consoleOut;
  }
}
