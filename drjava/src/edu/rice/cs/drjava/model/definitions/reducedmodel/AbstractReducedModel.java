package edu.rice.cs.drjava;

import gj.util.Stack;
import gj.util.Vector;

/**
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
    _cursor = _tokens.getIterator();
    // we should be pointing to the head of the list
    _cursor.setBlockOffset(0);
  }
 
  int getBlockOffset() {
    return _cursor.getBlockOffset();
  }
  
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
    TokenList.Iterator it = _cursor.copy();
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
    
    TokenList.Iterator it = _tokens.getIterator();
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
  
  public abstract void insertChar(char ch);
  
  /**
   * Inserts a block of non-brace text into the reduced model.
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
  public abstract void _insertGap(int length);

  public TokenList.Iterator makeCopyCursor() {
    return _cursor.copy();
  }
  
  /**
   * Wrapper for TokenList.Iterator.getStateAtCurrent that returns the current 
   * state for some iterator.
   * Convenience method to return the current state in the cursor iterator.
   */
  protected ReducedModelState getStateAtCurrent() {
    return _cursor.getStateAtCurrent();
  }

  
  protected boolean _gapToRight() {
    // Before using, make sure not at last, or tail.
    return (!_tokens.isEmpty() && !_cursor.atEnd() &&
            !_cursor.atLastItem() && _cursor.nextItem().isGap());
  }
  
  /**
   * Returns true if there is a gap immediately to the left. 
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

  protected ReducedToken current() {
    return _cursor.current();
  }
  protected void  next() {
    _cursor.next();
  }
  protected void prev() {
    _cursor.prev();
  }
 
 
}