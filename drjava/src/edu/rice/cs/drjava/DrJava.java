/* $Id$ */

package edu.rice.cs.drjava;

import java.io.PrintStream;

/** Main class for DrJava. */
public class DrJava {
  // Generated automatically when you check out with tag name!
  public static final String DRJAVA_BUILD = "$Name$";

  private static PrintStream _consoleOut;
  private static PrintStream _consoleErr;

  /** Compiler to use everywhere. */
  public static final CompilerInterface compiler = new GJv6Compiler();

  public static void main(String[] args) {
    // Save pointers to stdout and stderr
    _consoleOut = System.out;
    _consoleErr = System.err;

    MainFrame mf = new MainFrame();
		mf.show();
  }

  public static PrintStream consoleErr() { return _consoleErr; }
  public static PrintStream consoleOut() { return _consoleOut; }
}
