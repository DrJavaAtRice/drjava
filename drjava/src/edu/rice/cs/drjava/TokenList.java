package edu.rice.cs.drjava;

/**
 * A list of reduced model tokens.  Uses ModelList as its base.
 * @version$Id$
 */
public class TokenList extends ModelList<ReducedToken> 
                       implements /*imports*/ ReducedModelStates
{
  public Iterator getIterator() {
    return new Iterator();
  }
  
  public class Iterator extends ModelList<ReducedToken>.Iterator {
    
    public Iterator() {
      super();
    }
    
    Iterator(Iterator that) {
      super(that);
    }
    
    public Iterator copy() {
      return new Iterator(this);
    }
    
    /**
    * Returns the current commented/quoted state at the cursor.
    * @return FREE|INSIDE_BLOCK_COMMENT|INSIDE_LINE_COMMENT|INSIDE_SINGLE_QUOTE|
    * INSIDE_DOUBLE_QUOTE
    */
    public ReducedModelState getStateAtCurrent() {
      
      ReducedModelState state = FREE;
      
      if (atFirstItem() || atStart() || TokenList.this.isEmpty()) {
        state = FREE;
      }
      else if ( prevItem().isLineComment() ||
               (prevItem().getState() ==
                INSIDE_LINE_COMMENT))
      {
        state = INSIDE_LINE_COMMENT;
      }
      else if ( prevItem().isBlockCommentStart() ||
               (prevItem().getState() ==
                INSIDE_BLOCK_COMMENT)) 
      {
        state = INSIDE_BLOCK_COMMENT;
      }
      else if ( (prevItem().isDoubleQuote() &&
                 prevItem().isOpen() &&
                 (prevItem().getState() == FREE)) ||
               (prevItem().getState() ==
                INSIDE_DOUBLE_QUOTE))
      {
        state = INSIDE_DOUBLE_QUOTE;
      }
      else if ( (prevItem().isSingleQuote() &&
                 prevItem().isOpen() &&
                 (prevItem().getState() == FREE)) ||
               (prevItem().getState() ==
                INSIDE_SINGLE_QUOTE))
      {
        state = INSIDE_SINGLE_QUOTE;
      }
      else {
        state = FREE;
      }
      return state;
    }
    
  /**
   * Splits the current brace if it is a multiple character brace and
   * fulfills certain conditions.
   * If the current brace is a // or /*, split it into two braces.
   *  Do the same for star-slash (end comment block) if
   *  the parameter splitClose is true.
   *  Do the same for \\ and \" if splitEscape is true.
   *  If a split was performed, the first of the two Braces
   *  will be the current one when we're done.
   *  The offset is not changed.
   *  The two new Braces will have the same quoted/commented status
   *  as the one they were split from.
   */
  void _splitCurrentIfCommentBlock(boolean splitClose,
                                   boolean splitEscape)
    {
      String type = current().getType();
      if (type.equals("//") ||
          type.equals("/*") ||
          (splitClose && type.equals("*/")) ||
          (splitEscape && type.equals("\\\\")) ||
          (splitEscape && type.equals("\\\"")) ||
          (splitEscape && type.equals("\\'")))
      {
        String first = type.substring(0, 1);
        String second = type.substring(1, 2);
        // change current Brace to only be first character
        current().setType(first);
        ReducedModelState oldState = current().getState();
        
        // then put a new brace after the current one
        next();
        insert( Brace.MakeBrace(second, oldState) );
        // Move back to make the first brace we inserted current
        prev();
      }
    }
  }
}