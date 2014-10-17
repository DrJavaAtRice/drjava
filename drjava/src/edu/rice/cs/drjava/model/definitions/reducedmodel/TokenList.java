/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/** A list of reduced model tokens.  Uses ModelList as its base.
  * @version $Id: TokenList.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class TokenList extends ModelList<ReducedToken> implements /*imports*/ ReducedModelStates {
  
  /** Gets a TokenList.Iterator for this list.  Overrides the weaker method in ModelList<ReducedToken>.Iterator. */
  public Iterator getIterator() { return new Iterator(); }
  
  public class Iterator extends ModelIterator {
    
    private int _offset;
    
    public Iterator() {
      super();
      _offset = 0;
    }
    
    private Iterator(Iterator that) {
      super(that);
      _offset = that.getBlockOffset();
    }
    
    /** Makes a fresh copy of this TokenList.Iterator.  copy() returns a ModelList<ReducedToken>.Iterator copy which is
      * more restrictive than TokenList.Iterator.  An underscore differentiates the two methods.  This differentiation 
      * was easiest since it allowed us to keep TokenList.Iterator extending ModelList<ReducedToken>.Iterator.
      */
    public Iterator copy() { return new Iterator(this); }
    
    public void setTo(Iterator that) {
      super.setTo(that);
      _offset = that.getBlockOffset();
    }
    
    public int getBlockOffset() { return _offset; }
    
    public void setBlockOffset(int offset) { _offset = offset; }
    
    /** Returns the current commented/quoted state at the cursor.
      * @return FREE | INSIDE_BLOCK_COMMENT | INSIDE_LINE_COMMENT | INSIDE_SINGLE_QUOTE | INSIDE_DOUBLE_QUOTE
      */
    public ReducedModelState getStateAtCurrent() {
      if (atFirstItem() || atStart() || TokenList.this.isEmpty())  return FREE;
      else if (prevItem().isLineComment() || (prevItem().getState() == INSIDE_LINE_COMMENT))
        return INSIDE_LINE_COMMENT;
      else if (prevItem().isBlockCommentStart() || (prevItem().getState() == INSIDE_BLOCK_COMMENT))
        return INSIDE_BLOCK_COMMENT;
      else if ((prevItem().isDoubleQuote() && prevItem().isOpen() && (prevItem().getState() == FREE)) ||
               (prevItem().getState() == INSIDE_DOUBLE_QUOTE))
        return INSIDE_DOUBLE_QUOTE;
      else if ((prevItem().isSingleQuote() && prevItem().isOpen() && (prevItem().getState() == FREE)) ||
               (prevItem().getState() == INSIDE_SINGLE_QUOTE))
        return INSIDE_SINGLE_QUOTE;
      else return FREE;
    }
    
    
    /** Handles the details of the case where a brace is inserted into a gap. Assumes the current token is a gap!  
      * Assumes that the reduced lock is already held. 
      */
    void insertBraceToGap(String text) {
      current().shrink(getBlockOffset());
      insert(Brace.MakeBrace(text, getStateAtCurrent()));
      // add a new gap to account for the remainder from the split gap
      // if block offset is zero, do NOT add a Gap of size 0.
      if (getBlockOffset() > 0) {
        insert(new Gap(getBlockOffset(), getStateAtCurrent()));
        next(); //now point at new brace
      }
      next(); // now pointing at second half of gap
      setBlockOffset(0);
    }
    
    /** Helper function to _insertBrace. Handles the details of the case where brace is inserted between two
      * reduced tokens.  No destructive action is taken.  Assume that the reduced lock is already held.
      */
    void insertNewBrace(String text) {
      insert(Brace.MakeBrace(text, getStateAtCurrent()));
      next();
      setBlockOffset(0);
    }
    
    /** Splits the current brace if it is a multiple character brace and fulfills certain conditions.  If the current 
      * brace is a // or /*, split it into two braces.  Do the same for star-slash (end comment block) if the parameter
      * splitClose is true.  Do the same for \\ and \" if splitEscape is true.  If a split was performed, the first of
      * the two Braces will be the current one when we're done.  The offset is not changed.  The two new Braces will 
      * have the same quoted/commented status as the one they were split from.
      */
    void _splitCurrentIfCommentBlock(boolean splitClose, boolean splitEscape) {
      String type = current().getType();
      if (type.equals("//") || type.equals("/*") ||
          (splitClose && type.equals("*/")) ||
          (splitEscape && type.equals("\\\\")) ||
          (splitEscape && type.equals("\\\"")) ||
          (splitEscape && type.equals("\\'"))) {
        String first = type.substring(0, 1);
        String second = type.substring(1, 2);
        // change current Brace to only be first character
        current().setType(first);
        ReducedModelState oldState = current().getState();
        
        // then put a new brace after the current one
        next();
        insert(Brace.MakeBrace(second, oldState));
        // Move back to make the first brace we inserted current
        prev();
      }
    }
    
    /** Walks along the list on which ReducedModel is based from the current cursor position.  Which path it takes
      * depends on the return value of getStateAtCurrent() at the start of the walk.  Assumes reduced
      * lock is already held.
      */
    void updateBasedOnCurrentState() {
      if (atStart()) next();
      
      // If there's no text after here, nothing to update!
      if (atEnd()) return;
      
      ReducedModelState curState = getStateAtCurrent();
      // Free if at the beginning
      while (! atEnd()) { curState = curState.update(this); }
    }
    
    /** Updates the BraceReduction to reflect cursor movement. Negative values move left from the cursor, positive
      * values move right.  ASSUMES that count is within range, i.e. that the move will not push cursor past start
      * or end.
      * @param count indicates the direction and magnitude of cursor movement
      */
    public void move(int count) { _offset = _move(count, _offset); }
    
    /** Helper function for move(int).  Assumes that count is in range!
      * @param count  the number of chars to move.  Negative values move back, positive values move forward.
      * @param currentOffset the current offset for copyCursor
      * @return the updated offset
      */
    private int _move(int count, int currentOffset) {
      if (count == 0) return currentOffset;
      Iterator it = this;
      
      //make copy of cursor and return new iterator?
      if (count > 0) return it._moveRight(count, currentOffset);
      return it._moveLeft(- count, currentOffset);  // count < 0
    }
    
    /** Helper function that moves cursor ([iterator pos, count]) forward by count chars.  Assumes that count > 0 and
      * is in range.  Returns the new count.
      * <ol>
      * <li> at head && count > 0:  next
      * <li> LOOP:<BR>
      * if atEnd and count == 0, stop<BR>
      * if atEnd and count > 0, throw boundary exception<BR>
      * if count < size of current token, offset = count, stop<BR>
      * otherwise, reduce count by size of current token and go to
      * the next token, continuing the loop.
      * </ol>
      */
    private int _moveRight(int count, int currentOffset) {
      // Standardize initial position
      if (atStart()) {
        currentOffset = 0;
        next();
      }
      if (atEnd()) throw new IllegalArgumentException("At end");
      
      // Initialize loop variables
      int size = current().getSize();
      count = count + currentOffset;
      
      // Process tokens moving forward
      while (count >= size) { // advance one token
        count = count - size;
        next();
        if (atEnd()) {
          if (count == 0) break;
          else throw new IllegalArgumentException("At end");
        }
        size = current().getSize();
      }
      return count; // returns the offset in the current token
    }
    
    /** Helper function that moves cursor ([iterator pos, count]) backward by count chars.  Assumes that count > 0 and
      * is in range.  Returns the new count.
      * <ol>
      * <li> atEnd && count > 0:  prev
      * <li> LOOP:<BR>
      * if atStart and count == 0, stop<BR>
      * if atStart and count > 0, throw boundary exception<BR>
      * if count < size of current token, offset = size - count, stop<BR>
      * otherwise, reduce count by size of current token and go to
      * the previous token, continuing the loop.
      * </ol>
      */
    private int _moveLeft(int count, int currentOffset) {
      
      // Standardize initial position, eliminating 0 offset
      if (atEnd()) {
        assert currentOffset == 0;
        prev();
        if (atStart()) throw new IllegalArgumentException("At Start");  
        currentOffset = current().getSize(); // ! atStart() is precondition for calling current()
      }
      else if (atStart()) throw new IllegalArgumentException("At Start");
      
      while (count > currentOffset) {
        count = count - currentOffset;
        prev();
        
        if (atStart()) throw new IllegalArgumentException("At Start");  // count > 0
        currentOffset = current().getSize();
      }
      return currentOffset - count;  // Note: returned offset can be 0
    }
    
    /** <P>Update the BraceReduction to reflect text deletion.</P>
      * @param count  A number specifying the size and direction of text deletion. Negative values delete text to the 
      *               left of the cursor; positive values delete text to the right. Assumes deletion is within range!
      */
    public void delete(int count) {
      if (count == 0) return;
      Iterator copyCursor = copy();
      // from = this iterator
      // to = this iterator's copy
      _offset = _delete(count, copyCursor);
      copyCursor.dispose();
      return;
    }
    
    /** Helper function for delete. If deleting forward, move delTo the distance forward and call
      * deleteRight.<BR> If deleting backward, move delFrom the distance back and call deleteRight.
      * @param count      size of deletion
      * @param copyCursor cursor iterator
      * @return new offset after deletion
      */
    private int _delete(int count, Iterator copyCursor) {
      // Guarrantees that it's possible to delete count characters
      try {
        if (count > 0) copyCursor.move(count);
        else move(count); // count <= 0
        return deleteRight(copyCursor);
      }
      catch (Exception e) { throw new IllegalArgumentException("Trying to delete past end of file."); }
    }
    
    /** Gets rid of extra text.  Because collapse cannot get rid of all deletion text as some may be
      * only partially spanning a token, we need to make sure that
      * this partial span into the non-collapsed token on the left is removed.
      */
    void clipLeft() {
      if (atStart())  return;
      else if (getBlockOffset() == 0) remove();
      else if (current().isGap()) {
        int size = current().getSize();
        current().shrink(size - getBlockOffset());
      }
      else if (current().isMultipleCharBrace()) {
        if (getBlockOffset() != 1) throw new IllegalArgumentException("Offset incorrect");
        else {
          String type = current().getType();
          String first = type.substring(0, 1);
          current().setType(first);
        }
      }
      else throw new IllegalArgumentException("Cannot clip left.");
    }
    
    /** Gets rid of extra text. Because collapse cannot get rid of all deletion text as some may only partially span a
      * token, we must remove this partial span into the non-collapsed token on the right.
      */
    void clipRight() {
      if (atEnd()) return;
      else if (getBlockOffset() == 0) return;
      else if (getBlockOffset() == current().getSize()) remove();
      else if (current().isGap()) current().shrink(getBlockOffset());
      else if (current().isMultipleCharBrace()) {
        if (getBlockOffset() != 1) throw new IllegalArgumentException("Offset incorrect");
        else {
          String type = current().getType();
          String second = type.substring(1, 2);
          current().setType(second);
        }
      }
      else throw new IllegalArgumentException("Cannot clip left.");
    }
    
    /** Deletes from offset in this to endOffset in delTo. Uses ModelList.collapse to perform quick deletion. */
    int deleteRight(Iterator delTo) {
      collapse(delTo);
      
      // if both pointing to same item, and it's a gap
      if (eq(delTo) && current().isGap()) {
        // inside gap
        current().shrink(delTo.getBlockOffset() - getBlockOffset());
        return getBlockOffset();
      }
      
      
      //if brace is multiple char it must be a comment because the above if
      //test guarrantees it can't be a gap.
      if (! eq(delTo)) clipLeft();
      delTo.clipRight();
      
      if (! atStart()) prev();
      int delToSizeCurr;
      String delToTypeCurr;
      if (delTo.atEnd()) {
        setTo(delTo);
        return 0;
      }
      else {
        delToSizeCurr = delTo.current().getSize();
        delToTypeCurr = delTo.current().getType();
      }
      
      //get info on previous item.
      delTo.prev(); //get stats on previous item
      
      int delToSizePrev;
      String delToTypePrev;
      if (delTo.atStart()) { //no previous item, can't be at end
        delTo.next();
        setTo(delTo);
        return 0;
      }
      else {
        delToSizePrev = delTo.current().getSize();
        delToTypePrev = delTo.current().getType();
      }
      delTo.next(); //put delTo back on original node
      
      int temp = _calculateOffset(delToSizePrev, delToTypePrev,
                                  delToSizeCurr, delToTypeCurr,
                                  delTo);
      this.setTo(delTo);
      return temp;
    }
    
    
    /** By comparing the delTo token after the walk to what it was before the walk we can see how it has changed and 
      * where the offset should go.<p/>
      * Prev is the item previous to the current cursor. Curr is the current token. delTo is where current is pointing
      * at this moment in time.
      */
    private int _calculateOffset(int delToSizePrev, String delToTypePrev, int delToSizeCurr, String delToTypeCurr,
                                 Iterator delTo) {
      int offset;
      int delToSizeChange = delTo.current().getSize();
//      String delToTypeChange = delTo.current().getType();
      
      //1)if there was a gap previous to the gap at delTo delTo should be
      //augmented by its size, and that size is the offset.
      //2)if the gap was not preceeded by a gap then it would not need to
      //be shrunk
      if (delTo.atEnd()) throw new IllegalArgumentException("Shouldn't happen");
      if (delTo.current().isGap()) return delToSizeChange - delToSizeCurr;
      
      //this means that the item at the end formed a double brace with the
      //item that the delete left preceeding it. /dddddd*
      
      //the final item shrunk. This can only happen if the starting item
      //stole one of its braces: /ddddd*/
      //or if it was a double brace that had to get broken because it was
      //now commented or no longer has an open block
      
      //EXAMPLES: /*___*/  becoming */
      //          /*___*/  delete the first star, through the spaces to get
      //                   /*/
      //         //*__\n// becoming //*__//, the // is broken
      //         //*__\n// becoming ////   , the // is broken
      //THIS MUST HAVE THE previous items size and type passed in from
      //before the update. This way we know how it's changing too.
      
      // In this if clause, special characters are initially separated by some text
      // (represented here as ellipses), and when the text is deleted, the special
      // characters come together.  Sometimes, this breaks up the second token if
      // it is a multiple character brace.  Each in-line comment demonstrates
      // the individual case that occurs and for which we check with this if.
      // In this branch, both the cursor is off and the offset is also not correct.
      if ((delToTypePrev.equals("/") &&
           // /.../* => //-*
           ((delToTypeCurr.equals("/*") && _checkPrevEquals(delTo, "//")) ||
            // /...// => //-/
            (delToTypeCurr.equals("//") && _checkPrevEquals(delTo, "//")))) ||
          
          (delToTypePrev.equals("*") &&
           // *.../* => */-*
           ((delToTypeCurr.equals("/*") && _checkPrevEquals(delTo, "*/")) ||
            // *...// => */-/
            (delToTypeCurr.equals("//") && _checkPrevEquals(delTo, "*/")))) ||
          
          (delToTypePrev.equals("\\") &&
           // \...\\ => \\-\
           ((delToTypeCurr.equals("\\\\") && _checkPrevEquals(delTo, "\\")) ||
            // \...\' => \\-'
            (delToTypeCurr.equals("\\'") && _checkPrevEquals(delTo, "'")) ||
            // \...\" => \\-"
            (delToTypeCurr.equals("\\\"") && _checkPrevEquals(delTo, "\""))))) {
        delTo.prev();
        offset = 1;
      }
      // In this branch, the cursor is on the right token, but the offset is not correct.
      else if ((delToTypePrev.equals("/") &&
                // /-*/
                ((delToTypeCurr.equals("*/") && delTo.current().getType().equals("/*")) ||
                 (delToTypeCurr.equals("*") && delTo.current().getType().equals("/*")) ||
                 (delToTypeCurr.equals("/") && delTo.current().getType().equals("//")))) ||
               
               (delToTypePrev.equals("*") &&
                delToTypeCurr.equals("/") && delTo.current().getType().equals("*/")) ||
               
               (delToTypePrev.equals("\\") &&
                ((delToTypeCurr.equals("\\") && delTo.current().getType().equals("\\\\")) ||
                 (delToTypeCurr.equals("'") && delTo.current().getType().equals("\\'")) ||
                 (delToTypeCurr.equals("\"") && delTo.current().getType().equals("\\\""))))) {
        offset = 1;
      }
      // Otherwise, we're on the right token and our offset is correct because no recombinations occurred
      else offset = 0;
      return offset;
    }
    
    /** Checks if the previous token is of a certain type.
      * @param delTo the cursor for calling prevItem on
      * @param match the type we want to check
      * @return true if the previous token is of type match
      */
    private boolean _checkPrevEquals(Iterator delTo, String match) {
      if (delTo.atFirstItem() || delTo.atStart()) return false;
      return delTo.prevItem().getType().equals(match);
    }
    
    public String toString() { return "" + current(); }
    
  }
}
