package edu.rice.cs.drjava.model.compiler;

import java.io.File;

/**
 * A CompilerInterface implementation for signifying that no compiler is
 * available.
 *
 * @version $Id$
 */
public class NoCompilerAvailable implements CompilerInterface {
  public static final CompilerInterface ONLY = new NoCompilerAvailable();
  private static final String MESSAGE = "No compiler is available.";

  private NoCompilerAvailable() {}

  public CompilerError[] compile(File sourceRoot, File[] files) {
    CompilerError error = new CompilerError(files[0].getName(),
                                            -1,
                                            -1,
                                            MESSAGE,
                                            false);

    return new CompilerError[] { error };
  }

  public boolean isAvailable() { return true; }

  public String getName() {
    return "(no compiler available)";
  }

  public String toString() {
    return getName();
  }
}



