package  edu.rice.cs.drjava;

import  java.io.File;


/**
 * The minimum interface that a compiler must meet to be used by DrJava.
 * @version $Id$
 */
public interface CompilerInterface {

  /**
   * Returns array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  CompilerError[] compile(File[] files);
}



