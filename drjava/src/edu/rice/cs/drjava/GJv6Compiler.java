/* $Id$ */

package edu.rice.cs.drjava;

import java.io.File;

import gjc.v6.JavaCompiler;

import gjc.v6.util.Hashtable;
import gjc.v6.util.List;
import gjc.v6.util.Log;

public class GJv6Compiler implements CompilerInterface {
  private JavaCompiler _compiler;
  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */ 
  private Log _compilerLog;

  public GJv6Compiler() {
  }

  private void _initCompiler() {
    _compilerLog = new Log();

    // To use the GJ compiler, we build up the GJ options hashtable.
    Hashtable<String, String> options = Hashtable.make();

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    _compiler = JavaCompiler.make(_compilerLog, options);
  }

  /** Returns true if succeeds and false if not. */
  public boolean compile(File[] files) {
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    _initCompiler();
    List<String> filesToCompile = new List<String>();

    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }

    try {
      _compiler.compile(filesToCompile);
    }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      System.err.println("Compile error: " + t);
      return false; // it failed!
    }

    return (_compilerLog.nerrors == 0);
  }
}
