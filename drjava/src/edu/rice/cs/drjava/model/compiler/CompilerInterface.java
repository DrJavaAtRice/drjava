package  edu.rice.cs.drjava;

import  java.io.File;


/**
 * The minimum interface that a compiler must meet to be used by DrJava.
 * @version $Id$
 */
public interface CompilerInterface {

  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  CompilerError[] compile(File sourceRoot, File[] files);

  /*
   * Indicates whether this compiler is actually available.
   * As in: Is it installed and located?
   * This method should load the compiler class, which should
   * hopefully prove whether the class can load.
   * If this method returns true, the {@link #compile} method
   * should not fail due to class not being found.
   */
  //boolean isAvailable();

  /*
   * Returns the name of this compiler, appropriate to show to the user.
   */
  //String getName();
}



