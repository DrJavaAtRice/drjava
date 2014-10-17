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

/** A refactoring of the common code between ReducedModelComment and ReducedModelBrace.  Both of the refactored classes
  * extend this class.
  * @version $Id: AbstractReducedModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  * @author JavaPLT
  */
public abstract class AbstractReducedModel implements ReducedModelStates {
  
  /** The character that represents the cursor in toString(). @see #toString() */
  public static final char PTR_CHAR = '#';
  
  /** The reduced model for a document is a list of ReducedTokens (braces and gaps). */
  volatile TokenList _tokens;
  
  /** Keeps track of cursor position in document. */
  TokenList.Iterator _cursor;
  
  /** Constructor.  Creates a new reduced model with the cursor at the start of a blank "page." */
  public AbstractReducedModel() {
    _tokens = new TokenList();
    _cursor = _tokens.getIterator();
    // we should be pointing to the head of the list
    _cursor.setBlockOffset(0);
  }
  
  /** Get the offset into the current ReducedToken.
    * @return the number of characters into the token where the cursor sits
    */
  int getBlockOffset() { return _cursor.getBlockOffset(); }
  
  /** Change the offset into the current ReducedToken.
    * @param offset the number of characters into the token to set the cursor
    */
  void setBlockOffset(int offset) { _cursor.setBlockOffset(offset); }
  
  /** Absolute offset for testing purposes. We don't keep track of absolute offset as it causes too much confusion
    * and trouble.
    */
  public int absOffset() { return absOffset(_cursor); }
  
  /** Absolute offset of the specified iterator.  Inefficient so only used for testing purposes. */
  public int absOffset(TokenList.Iterator cursor) {
    int off = cursor.getBlockOffset();
    TokenList.Iterator it = cursor.copy();
    if (! it.atStart()) it.prev();
    
    while (! it.atStart()) {
      off += it.current().getSize();
      it.prev();
    }
    it.dispose();
    return off;
  }
  
  public int getLength() {
    TokenList.Iterator it = _tokens.getIterator();
    it.next();
    if (it.atEnd()) return 0;
    int len = 0;
    while (! it.atEnd()) {
      len += it.current().getSize();
      it.next();
    }
    it.dispose();
    return len;
  }
  
  /* @return the shadowing state of _cursor; only makes sense for ReducedModelComment. */
  public ReducedModelState getState() { return _cursor.getStateAtCurrent(); }
  
  /** A toString() replacement for testing - easier to read. */
  public String simpleString() {
    final StringBuilder val = new StringBuilder();
    ReducedToken tmp;
    
    TokenList.Iterator it = _tokens.getIterator();
    it.next(); // since we start at the head, which has no current item
    
    if (_cursor.atStart())  val.append(PTR_CHAR).append(_cursor.getBlockOffset());
    
    while (!it.atEnd()) {
      tmp = it.current();
      
      if (!_cursor.atStart() && !_cursor.atEnd() && (tmp == _cursor.current())) {
        val.append(PTR_CHAR).append(_cursor.getBlockOffset());
      }
      
      val.append('|').append(tmp).append('|').append("    ");
      it.next();
    }
    
    if (_cursor.atEnd()) val.append(PTR_CHAR).append(_cursor.getBlockOffset());
    
    val.append("|end|");
    it.dispose();
    return val.toString();
  }
  
  /** Inserts a character into the reduced model. A method to be implemented in each specific reduced sub-model. */
  public abstract void insertChar(char ch);
  
  /** Inserts a block of text into the reduced model which has no
    * special consideration in the reduced model.
    * <OL>
    *  <li> atStart: if gap to right, augment first gap, else insert
    *  <li> atEnd: if gap to left, augment left gap, else insert
    *  <li> inside a gap: grow current gap, move offset by length
    *  <li> inside a multiple character brace:
    *   <ol>
    *    <li> break current brace
    *    <li> insert new gap
    *   </ol>
    *  <li> gap to left: grow that gap and set offset to zero
    *  <li> gap to right: this case handled by inside gap (offset invariant)
    *  <li> between two braces: insert new gap
    * @param length the length of the inserted text
    */
  public void _insertGap( int length ) {
    if (_cursor.atStart()) {
      if (_gapToRight()) {
        _cursor.next();
        _augmentCurrentGap(length); //increases gap and moves offset
      }
      else _insertNewGap(length);//inserts gap and goes to next item
    }
    else if (_cursor.atEnd()) {
      if (_gapToLeft()) {
        _augmentGapToLeft(length);
        //increases the gap to the left and
        //cursor to next item in list leaving offset 0
      }
      else _insertNewGap(length); //inserts gap and moves to next item
    }
    // should we insert a Gap in between the characters of a multiple char brace
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace())
      insertGapBetweenMultiCharBrace(length);
    // inserting inside a Gap
    else if (_cursor.current().isGap()) {
      _cursor.current().grow(length);
      _cursor.setBlockOffset(_cursor.getBlockOffset() + length);
    }
    else if (!_cursor.atFirstItem() && _cursor.prevItem().isGap())
      //already pointing to next item
      _cursor.prevItem().grow(length);
    else  //between two braces
      _insertNewGap(length); //inserts a gap and goes to the next item
    return;
  }
  
  /** Inserts a gap between a multiple character brace.
   * Because ReducedModelBrace does not keep track of multiple character
   * braces, only (),{}, and [], it differed in its implementation of
   * inserGap(int) from ReducedModelComment's.  To pull out the otherwise
   * identical code and place it here, we created this function to do
   * something meaningful in ReducedModelComment and to throw an exception
   * in ReducedModelBrace.
   */
  protected abstract void insertGapBetweenMultiCharBrace(int length);
  
  /** Make a copy of the token list's iterator. */
  public TokenList.Iterator makeCopyCursor() { return _cursor.copy(); }
  
  /** Determines if there is a Gap immediately to the right of the cursor. */
  protected boolean _gapToRight() {
    // Before using, make sure not at last, or tail.
    return (! _tokens.isEmpty() && ! _cursor.atEnd() && ! _cursor.atLastItem() && _cursor.nextItem().isGap());
  }
  
  /** Determines if there is a gap immediately to the left of the cursor. */
  protected boolean _gapToLeft() {
    // Before using, make sure not at first or head.
    return (! _tokens.isEmpty() && ! _cursor.atStart() && ! _cursor.atFirstItem() && _cursor.prevItem().isGap());
  }
  
  /** Assuming there is a gap to the left, this function increases the size of that gap.
    * @param length the amount of increase
    */
  protected void _augmentGapToLeft(int length) { _cursor.prevItem().grow(length); }
  
  /** Assuming there is a gap to the right, this function increases the size of that gap.
    * @param length the amount of increase
    */
  protected void _augmentCurrentGap(int length) {
    _cursor.current().grow(length);
    _cursor.setBlockOffset(length);
  }
  
  /** Helper function for _insertGap. Performs the actual insert and marks the offset appropriately.
    * @param length size of gap to insert
    */
  protected void _insertNewGap(int length) {
    _cursor.insert(new Gap(length, _cursor.getStateAtCurrent()));
    _cursor.next();
    _cursor.setBlockOffset(0);
  }
  
  /** Returns the state at the relLocation, where relLocation is the location relative to the walker.
    * @param relLocation distance from walker to get state at.
    */
  protected abstract ReducedModelState moveWalkerGetState(int relLocation);
  
  /** Resets the walker to the current position in document. */
  protected abstract void resetWalkerLocationToCursor();
  
  /** Get the ReducedToken currently pointed at by the cursor.
    * @return the current token
    */
  protected ReducedToken current() { return _cursor.current(); }
  
  /** Move to the token immediately right. This function forwards its responsibilities to the cursor.
    * If the cursor is at the end, it will throw an exception.
    */
  protected void next() { _cursor.next(); }
  
  /** Move to the token immediately left. This function forwards its responsibilities to the TokenList iterator.  If the
    * cursor is at the start, it will throw an exception.
    */
  protected void prev() { _cursor.prev(); }
  
  // Never used
//  /** Determines whether the char at index pos with text is the start of a comment:  "/*" or "//" */
//  public static boolean isStartOfComment(String text, int pos) {
//    char currChar = text.charAt(pos);
//    if (currChar == '/') {
//      try {
//        char afterCurrChar = text.charAt(pos + 1);
//        if ((afterCurrChar == '/') || (afterCurrChar == '*'))  return true;
//      } catch (StringIndexOutOfBoundsException e) { }
//    }
//    return false;
//  }
}
