package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * This interface contains the various constants used by the reduced model to
 * represent the states of various blocks in the document.
 * @version $Id$
 */
public interface ReducedModelStates {
  
  public static final Free FREE = Free.ONLY;
  public static final Stutter STUTTER = Stutter.ONLY;
  
  public static final InsideSingleQuote INSIDE_SINGLE_QUOTE =  
    InsideSingleQuote.ONLY;
  
  public static final InsideDoubleQuote INSIDE_DOUBLE_QUOTE =  
    InsideDoubleQuote.ONLY;

  public static final InsideLineComment INSIDE_LINE_COMMENT  = 
    InsideLineComment.ONLY;
  
  public static final InsideBlockComment INSIDE_BLOCK_COMMENT = 
    InsideBlockComment.ONLY;
}
