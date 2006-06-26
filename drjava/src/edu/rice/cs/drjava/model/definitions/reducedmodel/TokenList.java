/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * A list of reduced model tokens.  Uses ModelList as its base.
 *
 * @version $Id$
 */
public class TokenList extends ModelList<ReducedToken>
    implements /*imports*/ ReducedModelStates {
  /**
   * Gets a TokenList.Iterator for this list.
   * getIterator() returns a ModelList<ReducedToken>.Iterator
   * which is not as fully featured as a TokenList.Iterator.
   * The underscore differentiates between the two.  This
   * differentiation was easiest since it allowed us to keep
   * TokenList.Iterator extending ModelList<ReducedToken>.Iterator.
   */
  public TokenList.Iterator _getIterator() {
    return new TokenList.Iterator();
  }

  public class Iterator extends ModelList<ReducedToken>.Iterator {

    private int _offset;

    public Iterator() {
      ((ModelList<ReducedToken>) TokenList.this).
      super();
      _offset = 0;
    }

    Iterator(Iterator that) {
      ((ModelList<ReducedToken>) TokenList.this).
      super(that);
      _offset = that.getBlockOffset();
    }

    /**
     * Makes a fresh copy of this TokenList.Iterator.
     * copy() returns a ModelList<ReducedToken>.Iterator copy
     * which is not as fully featured as a TokenList.Iterator.
     * The underscore differentiates between the two.  This
     * differentiation was easiest since it allowed us to keep
     * TokenList.Iterator extending ModelList<ReducedToken>.Iterator.
     */
    public TokenList.Iterator _copy() { return new Iterator(this); }

    public void setTo(TokenList.Iterator that) {
      super.setTo(that);
      _offset = that.getBlockOffset();
    }

    public int getBlockOffset() { return _offset; }

    public void setBlockOffset(int offset) { _offset = offset; }

    /**
     * Returns the current commented/quoted state at the cursor.
     *
     * @return FREE|INSIDE_BLOCK_COMMENT|INSIDE_LINE_COMMENT|INSIDE_SINGLE_QUOTE|
     *         INSIDE_DOUBLE_QUOTE
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


    /** Handles the details of the case where a brace is inserted into a gap.
     *  Do not call this unless the current token is a gap!
     */
    void insertBraceToGap(String text) {
      this.current().shrink(this.getBlockOffset());
      this.insert(Brace.MakeBrace(text, getStateAtCurrent()));
      // add a new gap to account for the remainder from the split gap
      // if block offset is zero, do NOT add a Gap of size 0.
      if (this.getBlockOffset() > 0) {
        this.insert(new Gap(this.getBlockOffset(), getStateAtCurrent()));
        this.next(); //now point at new brace
      }
      this.next(); // now pointing at second half of gap
      this.setBlockOffset(0);
    }

    /** Helper function to _insertBrace.
     *  Handles the details of the case where brace is inserted between two
     *  reduced tokens.  No destructive action is taken.
     */
    void insertNewBrace(String text) {
      this.insert(Brace.MakeBrace(text, getStateAtCurrent()));
      this.next();
      this.setBlockOffset(0);
    }

    /** Splits the current brace if it is a multiple character brace and fulfills certain conditions.  If the current 
     *  brace is a // or /*, split it into two braces.  Do the same for star-slash (end comment block) if the parameter
     *  splitClose is true.  Do the same for \\ and \" if splitEscape is true.  If a split was performed, the first of
     *  the two Braces will be the current one when we're done.  The offset is not changed.  The two new Braces will 
     *  have the same quoted/commented status as the one they were split from.
     */
    void _splitCurrentIfCommentBlock(boolean splitClose,
                                     boolean splitEscape) {
      String type = current().getType();
      if (type.equals("//") ||
          type.equals("/*") ||
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

    /** The walk function.
     *  Walks along the list on which ReducedModel is based from the current
     *  cursor position.  Which path it takes depends on the
     *  return value of getStateAtCurrent() at the start of the walk.
     */
    void updateBasedOnCurrentState() {
      if (this.atStart()) this.next();

      // If there's no text after here, nothing to update!
      if (this.atEnd()) return;

      ReducedModelState curState = this.getStateAtCurrent();
      // Free if at the beginning
      while (! this.atEnd()) {
        curState = curState.update(this);
      }
    }

    /** Updates the BraceReduction to reflect cursor movement. Negative values move left from the cursor, positive
     *  values move right.
     *  @param count indicates the direction and magnitude of cursor movement
     */
    public void move(int count) { _offset = _move(count, _offset); }

    /** Helper function for move(int).
     *
     *  @param count         the number of chars to move.  Negative values move back,
     *                       positive values move forward.
     *  @param currentOffset the current offset for copyCursor
     *  @return the updated offset
     */
    private int _move(int count, int currentOffset) {
      int retval = currentOffset;
      if (count == 0)  return retval;

      TokenList.Iterator it = this._copy();

      //make copy of cursor and return new iterator?
      if (count > 0) {
        retval = it._moveRight(count, currentOffset);
      }
      else {
        retval = it._moveLeft(Math.abs(count), currentOffset);
      }
      this.setTo(it);
      it.dispose();
      return retval;
    }

    /** Helper function that performs forward moves.
     * <ol>
     * <li> at head && count>0:  next
     * <li> LOOP:<BR>
     * if atEnd and count == 0, stop<BR>
     * if atEnd and count > 0, throw boundary exception<BR>
     * if count < size of current token, offset = count, stop<BR>
     * otherwise, reduce count by size of current token and go to
     * the next token, continuing the loop.
     * </ol>
     */
    private int _moveRight(int count, int currentOffset) {
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
          if (count == 0) break;
          else {
            throw new IllegalArgumentException("At end");
          }
        }
      }
      return count + currentOffset; //returns the offset
    }

    /**
     * Helper function that performs forward moves.
     * <ol>
     * <li> atEnd && count>0:  prev
     * <li> LOOP:<BR>
     * if atStart and count == 0, stop<BR>
     * if atStart and count > 0, throw boundary exception<BR>
     * if count < size of current token, offset = size - count, stop<BR>
     * otherwise, reduce count by size of current token and go to
     * the previous token, continuing the loop.
     * </ol>
     */
    private int _moveLeft(int count, int currentOffset) {
      if (this.atEnd()) {
        this.prev();
        if (!this.atStart()) //make sure list not empty
        {
          currentOffset = this.current().getSize();
        }
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


    /**
     * <P>Update the BraceReduction to reflect text deletion.</P>
     *
     * @param count indicates the size and direction of text deletion.
     *              Negative values delete text to the left of the cursor, positive
     *              values delete text to the right.
     *              Always move count spaces to make sure we can delete.
     */
    public void delete(int count) {
      if (count == 0) return;
      TokenList.Iterator copyCursor = this._copy();
      // from = this iterator
      // to = this iterator's copy
      _offset = _delete(count, copyCursor);
      copyCursor.dispose();
      return;
    }

    /**
     * Helper function for delete.
     * If deleting forward, move delTo the distance forward and call
     * deleteRight.<BR>
     * If deleting backward, move delFrom the distance back and call
     * deleteRight.
     *
     * @param count      size of deletion
     * @param copyCursor cursor iterator
     * @return new offset after deletion
     */
    private int _delete(int count, TokenList.Iterator copyCursor) {
      // Guarrantees that it's possible to delete count characters
      try {
        if (count > 0) {
          copyCursor.move(count);
        }
        else { // count <= 0
          this.move(count);
        }
        return deleteRight(copyCursor);
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Trying to delete past end of file.");
      }
    }

    /**
     * Gets rid of extra text.
     * Because collapse cannot get rid of all deletion text as some may be
     * only partially spanning a token, we need to make sure that
     * this partial span into the non-collapsed token on the left is removed.
     */
    void clipLeft() {
      if (atStart()) {
        return;
      }
      else if (getBlockOffset() == 0) {
        remove();
      }
      else if (current().isGap()) {
        int size = current().getSize();
        this.current().shrink(size - getBlockOffset());
      }
      else if (current().isMultipleCharBrace()) {
        if (getBlockOffset() != 1) {
          throw new IllegalArgumentException("Offset incorrect");
        }
        else {
          String type = current().getType();
          String first = type.substring(0, 1);
          this.current().setType(first);
        }
      }
      else {
        throw new IllegalArgumentException("Cannot clip left.");
      }
    }

    /**
     * Gets rid of extra text.
     * Because collapse cannot get rid of all deletion text as some may be
     * only partially spanning a token, we need to make sure that
     * this partial span into the non-collapsed token on the right is removed.
     */
    void clipRight() {
      if (this.atEnd()) {
        return;
      }
      else if (this.getBlockOffset() == 0) {
        return;
      }
      else if (this.getBlockOffset() == this.current().getSize()) {
        this.remove();
      }
      else if (this.current().isGap()) {
        this.current().shrink(this.getBlockOffset());
      }
      else if (this.current().isMultipleCharBrace()) {
        if (this.getBlockOffset() != 1) {
          throw new IllegalArgumentException("Offset incorrect");
        }
        else {
          String type = this.current().getType();
          String second = type.substring(1, 2);
          this.current().setType(second);
        }
      }
      else {
        throw new IllegalArgumentException("Cannot clip left.");
      }
    }

    /** Deletes from offset in delFrom to endOffset in delTo.
     *  Uses ModelList's collapse function to facilitate quick deletion.
     */
    int deleteRight(TokenList.Iterator delTo) {
      this.collapse(delTo);

      // if both pointing to same item, and it's a gap
      if (this.eq(delTo) && this.current().isGap()) {
        // inside gap
        this.current().shrink(delTo.getBlockOffset() - this.getBlockOffset());
        return this.getBlockOffset();
      }


      //if brace is multiple char it must be a comment because the above if
      //test guarrantees it can't be a gap.
      if (!this.eq(delTo)) {
        this.clipLeft();
      }
      delTo.clipRight();

      if (!this.atStart()) {
        this.prev();
      }
      int delToSizeCurr;
      String delToTypeCurr;
      if (delTo.atEnd()) {
        this.setTo(delTo);
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
        this.setTo(delTo);
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


    /**
     * By contrasting the delTo token after the walk to what it was before the
     * walk we can see how it has changed and where the offset should go.
     * <p/>
     * Prev is the item previous to the current cursor
     * Current is what the current cursor
     * delTo is where current is pointing at this moment in time.
     */
    private int _calculateOffset(int delToSizePrev, String delToTypePrev,
                                 int delToSizeCurr, String delToTypeCurr,
                                 TokenList.Iterator delTo) {
      int offset;
      int delToSizeChange = delTo.current().getSize();
//      String delToTypeChange = delTo.current().getType();

      //1)if there was a gap previous to the gap at delTo delTo should be
      //augmented by its size, and that size is the offset.
      //2)if the gap was not preceeded by a gap then it would not need to
      //be shrunk
      if (delTo.atEnd()) {
        throw new IllegalArgumentException("Shouldn't happen");
      }
      if (delTo.current().isGap()) {
        return delToSizeChange - delToSizeCurr;
      }
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
      if (((delToTypePrev.equals("/")) &&
           // /.../* => //-*
           ((delToTypeCurr.equals("/*") &&
             _checkPrevEquals(delTo, "//")) ||
            // /...// => //-/
            (delToTypeCurr.equals("//") &&
             _checkPrevEquals(delTo, "//")))) ||

          ((delToTypePrev.equals("*")) &&
           // *.../* => */-*
           ((delToTypeCurr.equals("/*") &&
             _checkPrevEquals(delTo, "*/")) ||
            // *...// => */-/
            (delToTypeCurr.equals("//") &&
             _checkPrevEquals(delTo, "*/")))) ||

          ((delToTypePrev.equals("\\")) &&
           // \...\\ => \\-\
           ((delToTypeCurr.equals("\\\\") &&
             _checkPrevEquals(delTo, "\\")) ||
            // \...\' => \\-'
            (delToTypeCurr.equals("\\'") &&
             _checkPrevEquals(delTo, "'")) ||
            // \...\" => \\-"
            (delToTypeCurr.equals("\\\"") &&
             _checkPrevEquals(delTo, "\""))))) {
        delTo.prev();
        offset = 1;
      }
      // In this branch, the cursor is on the right token, but the offset is not correct.
      else if (((delToTypePrev.equals("/")) &&
                // /-*/
                ((delToTypeCurr.equals("*/") &&
                  delTo.current().getType().equals("/*")) ||
                 (delToTypeCurr.equals("*") &&
                  delTo.current().getType().equals("/*")) ||
                 (delToTypeCurr.equals("/") &&
                  delTo.current().getType().equals("//")))) ||

               ((delToTypePrev.equals("*")) &&
                ((delToTypeCurr.equals("/") &&
                  delTo.current().getType().equals("*/")))) ||

               ((delToTypePrev.equals("\\")) &&
                ((delToTypeCurr.equals("\\") &&
                  delTo.current().getType().equals("\\\\")) ||
                 (delToTypeCurr.equals("'") &&
                  delTo.current().getType().equals("\\'")) ||
                 (delToTypeCurr.equals("\"") &&
                  delTo.current().getType().equals("\\\""))))) {
        offset = 1;
      }
      // otherwise, we're on the right token and our offset is correct
      // because no recombinations occurred
      else {
        offset = 0;
      }
      return offset;
    }

    /**
     * Checks if the previous token is of a certain type.
     *
     * @param delTo the cursor for calling prevItem on
     * @param match the type we want to check
     * @return true if the previous token is of type match
     */
    private boolean _checkPrevEquals(TokenList.Iterator delTo,
                                     String match) {
      if (delTo.atFirstItem() || delTo.atStart()) return false;
      return delTo.prevItem().getType().equals(match);
    }

    public String toString() { return "" + this.current(); }

  }
}
