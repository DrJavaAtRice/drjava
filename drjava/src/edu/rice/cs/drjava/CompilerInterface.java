/* $Id$ */

package edu.rice.cs.drjava;

import java.io.File;

public interface CompilerInterface {
  /** Returns true if succeeds and false if not. */
  boolean compile(File[] files);
}
