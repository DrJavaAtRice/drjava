/* $Id$ */

package edu.rice.cs.drjava;

import java.io.PrintStream;

/** Main class for DrJava. */
public class DrJava {
  // Generated automatically when you check out with tag name!
  public static final String DRJAVA_BUILD = "$Name$";

  // Pre-initialize them in case we use them before calling main
  private static PrintStream _consoleOut = System.out;
  private static PrintStream _consoleErr = System.err;

  /** Compiler to use everywhere. */
  public static final CompilerInterface compiler = new GJv6Compiler();

  public static void main(String[] args) {
    // Save pointers to stdout and stderr
    _consoleOut = System.out;
    _consoleErr = System.err;

    try {
	MainFrame mf = new MainFrame();
	mf.show();
    }
    catch (Exception ex) {
	_consoleErr.println(ex.getClass().getName() + ": " + ex.getMessage());
	ex.printStackTrace(_consoleErr);
	// System.exit();
    }

  }

  public static PrintStream consoleErr() { return _consoleErr; }
  public static PrintStream consoleOut() { return _consoleOut; }
}
