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

/**
 * A subclass of ReducedToken that represents non-special characters.
 * @version $Id$
 */
class Gap extends ReducedToken {
  private int _size;

  /**
   * put your documentation comment here
   * @param     int size
   * @param     ReducedModelState state
   */
  Gap(int size, ReducedModelState state) {
    super(state);
    _size = size;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int getSize() {
    return  _size;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String getType() {
    return  "";
  }

  /**
   * put your documentation comment here
   * @param type
   */
  public void setType(String type) {
    throw  new RuntimeException("Can't set type on Gap!");
  }

  /**
   * put your documentation comment here
   */
  public void flip() {
    throw  new RuntimeException("Can't flip a Gap!");
  }

  /**
   * Increases the size of the gap.
   * @param delta the amount by which the gap is augmented.
   */
  public void grow(int delta) {
    if (delta >= 0)
      _size += delta;
  }

  /**
   * Decreases the size of the gap.
   * @param delta the amount by which the gap is diminished.
   */
  public void shrink(int delta) {
    if ((delta <= _size) && (delta >= 0))
      _size -= delta;
  }

  /**
   * Converts a Brace to a String.
   * Used for debugging.
   * @return the string representation of the Brace.
   */
  public String toString() {
    String val = "";
    int i;
    for (i = 0; i < _size; i++) {
      val += " _";
    }
    return  val;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isMultipleCharBrace() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isGap() {
    return  true;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isLineComment() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isBlockCommentStart() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isBlockCommentEnd() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isNewline() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isSlash() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isStar() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isDoubleQuote() {
    return  false;
  }
  
  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isSingleQuote() {
    return false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isDoubleEscapeSequence() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isDoubleEscape() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isEscapedSingleQuote() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isEscapedDoubleQuote() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isOpen() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isClosed() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @param other
   * @return 
   */
  public boolean isMatch(ReducedToken other) {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isOpenBrace() {
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isClosedBrace() {
    return  false;
  }
}



