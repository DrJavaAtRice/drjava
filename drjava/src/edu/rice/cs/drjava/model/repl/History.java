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

package edu.rice.cs.drjava.model.repl;

import  gj.util.Vector;


/**
 * Keeps track of what was typed in the interactions pane.
 * @version $Id$
 */
public class History {
  
  private static final int MAX_SIZE = 500;
  
  private Vector<String> _vector = new Vector<String>();
  private int _cursor = -1;

  /**
   * Adds an item to the history and moves the cursor to point
   * to the place after it.
   * Note: Items are not inserted if they would duplicate the last item,
   * or if they are empty. (This is in accordance with bug #522123 and
   * feature #522213.)
   *
   * Thus, to access the newly inserted item, you must movePrevious first.
   */
  public void add(String item) {
    
    if (item.trim().length() > 0) {
      if (_vector.isEmpty() || ! _vector.lastElement().equals(item)) {
        _vector.addElement(item);
        
        // If adding the new element has filled _vector to beyond max 
        // capacity, spill the oldest element out of the History.
        if (_vector.size() > MAX_SIZE) {
          _vector.removeElementAt(0);
        }
      }
      moveEnd();
    }
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
    return  _cursor < (_vector.size());
  }

  /** Returns whether movePrevious() would succeed right now. */
  public boolean hasPrevious() {
    return  _cursor > 0;
  }

  /**
   * Returns item in history at current position, or throws exception if none.
   */
  public String getCurrent() {
    if (hasNext()) {
      return  _vector.elementAt(_cursor);
    }
    else {
      return "";
    }
  }
  
  /**
   * Returns the number of items in this History.
   */
  public int size() {
    return _vector.size();
  }
}



