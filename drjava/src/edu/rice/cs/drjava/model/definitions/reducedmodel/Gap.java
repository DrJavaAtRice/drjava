/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
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
    //String val = "Gap(size: "+_size+"): ";
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



