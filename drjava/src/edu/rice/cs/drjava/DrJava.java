package  edu.rice.cs.drjava;

import  java.io.PrintStream;

import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.ui.MainFrame;

import edu.rice.cs.drjava.model.compiler.GJv6Compiler;

/** 
 * Main class for DrJava. 
 * @version $Id$
 */
public class DrJava {
  // Pre-initialize them in case we use them before calling main
  private static PrintStream _consoleOut = System.out;
  private static PrintStream _consoleErr = System.err;

  public static void main(String[] args) {
    // Save pointers to stdout and stderr
    _consoleOut = System.out;
    _consoleErr = System.err;
    try {
      MainFrame mf = new MainFrame();
      mf.show();
    } catch (Exception ex) {
      _consoleErr.println(ex.getClass().getName() + ": " + ex.getMessage());
      ex.printStackTrace(_consoleErr);
      // System.exit();
    }
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
