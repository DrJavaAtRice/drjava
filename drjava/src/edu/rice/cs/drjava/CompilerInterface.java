/* $Id$ */

package edu.rice.cs.drjava;

import java.io.File;

public interface CompilerInterface {
  /**
   * Returns array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  CompilerError[] compile(File[] files);
}
