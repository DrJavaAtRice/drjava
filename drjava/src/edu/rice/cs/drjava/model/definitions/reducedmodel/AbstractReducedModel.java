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
}