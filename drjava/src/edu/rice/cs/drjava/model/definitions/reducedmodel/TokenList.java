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
    
    private int _offset;
  
    public Iterator() {
      super();
      _offset = 0;
    }
    
    Iterator(Iterator that) {
      super(that);
      _offset = that.getBlockOffset();
    }
    
    public Iterator copy() {
      return new Iterator(this);
    }
    
    public void setTo(Iterator that) {
      super.setTo(that);
      _offset = that.getBlockOffset();
    }
    
    public int getBlockOffset() {
      return _offset;
    }
    
    public void setBlockOffset(int offset) {
      _offset = offset;
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
    
    /**
    * Updates the BraceReduction to reflect cursor movement.
    * Negative values move left from the cursor, positive values move
    * right.
    * @param count indicates the direction and magnitude of cursor movement
    */
    public void move(int count) {
      _offset = _move(count, _offset);
    }
    
    /**
    * Helper function for move(int).
    * @param count the number of chars to move.  Negative values move back,
    * positive values move forward.
    * @param copyCursor the cursor being moved
    * @param currentOffset the current offset for copyCursor
    * @return the updated offset
    */
    private int _move(int count, int currentOffset)
    {
      int retval = currentOffset;
      TokenList.Iterator it = this.copy();
      
      if (count == 0) {
        it.dispose();
        return retval;
      }
      //make copy of cursor and return new iterator?
      else if (count > 0) {
        retval = it._moveRight(count, currentOffset);
      }
      else {
        retval = it._moveLeft(Math.abs(count), currentOffset);
      }
      this.setTo(it);
      it.dispose();
      return retval;
    }
    
    /**
    * Helper function that performs forward moves.
    * <ol>
    *  <li> at head && count>0:  next
    *  <li> LOOP:<BR>
    *     if atEnd and count == 0, stop<BR>
    *     if atEnd and count > 0, throw boundary exception<BR>
    *     if count < size of current token, offset = count, stop<BR>
    *     otherwise, reduce count by size of current token and go to
    *     the next token, continuing the loop.
    * </ol>
    */
    private int _moveRight(int count, int currentOffset)
    {
      if (this.atStart()) {
        currentOffset = 0;
        this.next();
      }
      if (this.atEnd()) {
        throw new IllegalArgumentException("At end");
      }
      while (count >= this.current().getSize() - currentOffset) {
        count = count - this.current().getSize() + currentOffset;
        this.next();
        currentOffset = 0;
        if (this.atEnd()) {
          if (count == 0) {
            break;
          }
          else {throw new IllegalArgumentException("Moved into tail");}
        }
      }
      return count + currentOffset; //returns the offset
    }
    
    /**
    * Helper function that performs forward moves.
    * <ol>
    *  <li> atEnd && count>0:  prev
    *  <li> LOOP:<BR>
    *     if atStart and count == 0, stop<BR>
    *     if atStart and count > 0, throw boundary exception<BR>
    *     if count < size of current token, offset = size - count, stop<BR>
    *     otherwise, reduce count by size of current token and go to
    *     the previous token, continuing the loop.
    * </ol>
    */
    private int _moveLeft(int count, int currentOffset)
    {
      if (this.atEnd()) {
        this.prev();
        if (!this.atStart()) //make sure list not empty
          currentOffset = this.current().getSize();
      }
      
      if (this.atStart()) {
        throw new IllegalArgumentException("At Start");
      }
      while (count > currentOffset) {
        count = count - currentOffset;
        this.prev();
        
        if (this.atStart()) {
          if (count > 0) {
            throw new IllegalArgumentException("At Start");
          }
          else {
            this.next();
            currentOffset = 0;
          }
        }
        else {
          currentOffset = this.current().getSize();
        }
      }
      return currentOffset - count;
    }     
  }
  
}