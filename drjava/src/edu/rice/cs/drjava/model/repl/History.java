package edu.rice.cs.drjava.model.repl;

import  gj.util.Vector;


/**
 * Keeps track of what was typed in the interactions pane.
 * @version $Id$
 */
public class History {
  private Vector<String> _vector = new Vector<String>();
  private int _cursor = -1;

  /**
   * Adds an item to the history and moves the cursor to point
   * to the place after it.
   * Thus, to access the newly inserted item, you must movePrevious first.
   */
  public void add(String item) {
    _vector.addElement(item);
    moveEnd();
  }

  /**
   * Move the cursor to just past the end. Thus, to access the last element,
   * you must movePrevious.
   */
  public void moveEnd() {
    _cursor = _vector.size();
  }

  /** Moves cursor back 1, or throws exception if there is none. */
  public void movePrevious() {
    if (!hasPrevious()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    _cursor--;
  }

  /** Moves cursor forward 1, or throws exception if there is none. */
  public void moveNext() {
    if (!hasNext()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    _cursor++;
  }

  /** Returns whether moveNext() would succeed right now. */
  public boolean hasNext() {
    return  _cursor < (_vector.size() - 1);
  }

  /** Returns whether movePrevious() would succeed right now. */
  public boolean hasPrevious() {
    return  _cursor > 0;
  }

  /**
   * Returns item in history at current position, or throws exception if none.
   */
  public String getCurrent() {
    return  _vector.elementAt(_cursor);
  }
}



