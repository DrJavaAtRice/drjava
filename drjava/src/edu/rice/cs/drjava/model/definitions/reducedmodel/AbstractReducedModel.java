/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import gj.util.Stack;
import gj.util.Vector;

/**
 * A refactoring of the common code between ReducedModelComment and 
 * ReducedModelBrace.  Both of the refactored classes extend this class.
 * @version $Id$
 * @author JavaPLT
 */
public abstract class AbstractReducedModel implements ReducedModelStates {
    
  /**
   * The character that represents the cursor in toString().
   * @see #toString()
   */
  public static final char PTR_CHAR = '#';
  
  /**
   * A list of ReducedTokens (braces and gaps).
   * @see ModelList
   */
  TokenList _tokens;
  /**
   * keeps track of cursor position in document
   * @see ModelList.Iterator
   */
  TokenList.Iterator _cursor;
  
  /**
   * Constructor.  Creates a new reduced model with the cursor
   * at the start of a blank "page."
   */
  public AbstractReducedModel() {
    _tokens = new TokenList();
    _cursor = _tokens._getIterator();
    // we should be pointing to the head of the list
    _cursor.setBlockOffset(0);
  }
 
  /**
   * Get the offset into the current ReducedToken.
   * @return the number of characters into the token where the cursor sits
   */
  int getBlockOffset() {
    return _cursor.getBlockOffset();
  }
  
  /**
   * Change the offset into the current ReducedToken.
   * @param offset the number of characters into the token to set the cursor
   */
  void setBlockOffset(int offset) {
    _cursor.setBlockOffset(offset);
  }
  
  /**
   * Package private absolute offset for tests.
   * We don't keep track of absolute offset as it causes too much confusion
   * and trouble.
   */
  int absOffset() {
    int off = _cursor.getBlockOffset();
    TokenList.Iterator it = _cursor._copy();
    if (!it.atStart())
      it.prev();
    
    while (!it.atStart()) {
      off += it.current().getSize();
      it.prev();
    }
    it.dispose();
    return off;
  }
  
  /**
   * A toString replacement for testing - easier to read.
   */
  public String simpleString() {
    String val = "";
    ReducedToken tmp;
    
    TokenList.Iterator it = _tokens._getIterator();
    it.next(); // since we start at the head, which has no current item
    
    
    if (_cursor.atStart()) {
      val += PTR_CHAR;
      val += _cursor.getBlockOffset();
    }
    
    while(!it.atEnd()) {
      tmp = it.current();
      
      if (!_cursor.atStart() && !_cursor.atEnd() && (tmp == _cursor.current())) {
        val += PTR_CHAR;
        val += _cursor.getBlockOffset();
      }
      
      val += "|";
      val += tmp;
      val += "|\t";
      
      it.next();
    }
    
    if (_cursor.atEnd()) {
      val += PTR_CHAR;
      val += _cursor.getBlockOffset();
    }
    
    val += "|end|";
    it.dispose();
    return val;
  }
  
  /**
   * Inserts a character into the reduced model.
   * A method to be implemented in each specific reduced sub-model.
   */
  public abstract void insertChar(char ch);
  
  /**
   * Inserts a block of text into the reduced model which has no
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
      else {
        _insertNewGap(length);//inserts gap and goes to next item
      }
    }
    else if (_cursor.atEnd()) {
      if (_gapToLeft()) {
        _augmentGapToLeft(length);
        //increases the gap to the left and
        //cursor to next item in list leaving offset 0
      }
      else {
        _insertNewGap(length); //inserts gap and moves to next item
      }
    }
    // should we insert a Gap in between the characters of a multiple char brace
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      insertGapBetweenMultiCharBrace(length);
    }
    // inserting inside a Gap
    else if (_cursor.current().isGap()) {
      _cursor.current().grow(length);
      _cursor.setBlockOffset(_cursor.getBlockOffset() + length);
    }
    else if (!_cursor.atFirstItem() && _cursor.prevItem().isGap()) {
      //already pointing to next item
      _cursor.prevItem().grow(length);
    }
    else { //between two braces
      _insertNewGap(length); //inserts a gap and goes to the next item
    }
    return;
  }

  /**
   * Inserts a gap between a multiple character brace.
   * Because ReducedModelBrace does not keep track of multiple character
   * braces, only (),{}, and [], it differed in its implementation of
   * inserGap(int) from ReducedModelComment's.  To pull out the otherwise
   * identical code and place it here, we created this function to do
   * something meaningful in ReducedModelComment and to throw an exception
   * in ReducedModelBrace.
   */
  protected abstract void insertGapBetweenMultiCharBrace(int length);
  
  /**
   * Make a copy of the token list's iterator.
   * Be sure to dispose of the result of this method after you are
   * finished with it, or there will be memory leaks as long as
   * this ReducedModel is not garbage collected.
   */
  public TokenList.Iterator makeCopyCursor() {
    return _cursor._copy();
  }
  
  /**
   * Wrapper for TokenList.Iterator.getStateAtCurrent that returns the current 
   * state for some iterator.
   * Convenience method to return the current state in the cursor iterator.
   */
  protected ReducedModelState getStateAtCurrent() {
    return _cursor.getStateAtCurrent();
  }

  /**
   * Determines there is a Gap immediately to the right of the cursor.
   */
  protected boolean _gapToRight() {
    // Before using, make sure not at last, or tail.
    return (!_tokens.isEmpty() && !_cursor.atEnd() &&
            !_cursor.atLastItem() && _cursor.nextItem().isGap());
  }
  
  /**
   * Determines if there is a gap immediately to the left of the cursor.
   */
  protected boolean _gapToLeft() {
    // Before using, make sure not at first or head.
    return (!_tokens.isEmpty() && !_cursor.atStart() &&
            !_cursor.atFirstItem() &&_cursor.prevItem().isGap());
  }
  
  /**
   * Assuming there is a gap to the left, this function increases
   * the size of that gap.
   * @param length the amount of increase
   */
  protected void _augmentGapToLeft(int length) {
    _cursor.prevItem().grow(length);
  }
  
  /**
   * Assuming there is a gap to the right, this function increases
   * the size of that gap.
   * @param length the amount of increase
   */
  protected void _augmentCurrentGap(int length) {
    _cursor.current().grow(length);
    _cursor.setBlockOffset(length);
  }
  
  /**
   * Helper function for _insertGap.
   * Performs the actual insert and marks the offset appropriately.
   * @param length size of gap to insert
   */
  protected void _insertNewGap(int length) {
    _cursor.insert(new Gap(length, getStateAtCurrent()));
    _cursor.next();
    _cursor.setBlockOffset(0);
  }
  
  /**
   * Returns the state at the relLocation, where relLocation is the location
   * relative to the walker
   * @param relLocation distance from walker to get state at.
   */
  protected abstract ReducedModelState stateAtRelLocation(int relLocation);
  
  /**
   * Resets the walker to the current position in document
   */
  protected abstract void resetLocation();

  /**
   * Get the ReducedToken currently pointed at by the cursor.
   * @return the current token
   */
  protected ReducedToken current() {
    return _cursor.current();
  }
  
  /**
   * Move to the token immediately right.
   * This function forwards its responsibilities to the TokenList
   * iterator.  If the cursor is at the end, it will throw an
   * exception.
   */
  protected void  next() {
    _cursor.next();
  }

  /**
   * Move to the token immediately left.
   * This function forwards its responsibilities to the TokenList
   * iterator.  If the cursor is at the start, it will throw an
   * exception.
   */
  protected void prev() {
    _cursor.prev();
  }

}