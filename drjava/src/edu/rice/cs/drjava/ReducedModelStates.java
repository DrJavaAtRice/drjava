package edu.rice.cs.drjava;

/**
 * @version $Id$
 * This interface contains the various constants used by the reduced model to
 * represent the states of various blocks in the document.
 */
public interface ReducedModelStates {
  
  public static final int FREE = 0;
  
  public static final int INSIDE_SINGLE_QUOTE = 1;
  public static final int INSIDE_DOUBLE_QUOTE = 2;

  public static final int INSIDE_LINE_COMMENT = 3;
  public static final int INSIDE_BLOCK_COMMENT = 4;
}
