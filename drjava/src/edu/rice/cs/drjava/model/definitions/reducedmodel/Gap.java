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
   * Creates a new Gap.
   * @param size the size of the gap
   * @param state the state of the reduced model
   */
  Gap(int size, ReducedModelState state) {
    super(state);
    _size = size;
  }

  /**
   * Gets the size of this gap.
   * @return _size
   */
  public int getSize() {
    return _size;
  }

  /**
   * Gets the token type.
   * @return the empty string
   */
  public String getType() {
    return "";
  }

  /**
   * Blows up.  The type of a Gap cannot be set.
   * @param type the type to set to
   * @throws RuntimeException always
   */
  public void setType(String type) {
    throw new RuntimeException("Can't set type on Gap!");
  }

  /**
   * Blows up.  A Gap cannot be flipped.
   * @throws RuntimeException always
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
   * Converts a Brace to a String.  Used for debugging.
   * @return the String representation of the Brace
   */
  public String toString() {
//    String val = "Gap(size: "+_size+"): ";
    String val = "";
    int i;
    for (i = 0; i < _size; i++) {
      val += " _";
    }
    return  val;
  }

  /**
   * Determines that this is not a multi-char brace.
   * @return <code>false</code>
   */
  public boolean isMultipleCharBrace() {
    return false;
  }

  /**
   * Determines that this is a gap.
   * @return <code>true</code>
   */
  public boolean isGap() {
    return true;
  }

  /**
   * Determines that this is not a line comment.
   * @return <code>false</code>
   */
  public boolean isLineComment() {
    return false;
  }

  /**
   * Determines that this is not the start of a block comment.
   * @return <code>false</code>
   */
  public boolean isBlockCommentStart() {
    return false;
  }

  /**
   * Determines that this is not the end of a block comment.
   * @return <code>false</code>
   */
  public boolean isBlockCommentEnd() {
    return false;
  }

  /**
   * Determines that this is not a newline.
   * @return <code>false</code>
   */
  public boolean isNewline() {
    return false;
  }

  /**
   * Determines that this is not a /.
   * @return <code>false</code>
   */
  public boolean isSlash() {
    return false;
  }

  /**
   * Determines that this is not a *.
   * @return <code>false</code>
   */
  public boolean isStar() {
    return false;
  }

  /**
   * Determines that this is not a ".
   * @return <code>false</code>
   */
  public boolean isDoubleQuote() {
    return false;
  }

  /**
   * Determines that this is not a '.
   * @return <code>false</code>
   */
  public boolean isSingleQuote() {
    return false;
  }

  /**
   * Determines that this is not a double escape sequence.
   * @return <code>false</code>
   */
  public boolean isDoubleEscapeSequence() {
    return false;
  }

  /**
   * Determines that this is not a double escape.
   * @return <code>false</code>
   */
  public boolean isDoubleEscape() {
    return false;
  }

  /**
   * Determines that this is not a \'.
   * @return <code>false</code>
   */
  public boolean isEscapedSingleQuote() {
    return false;
  }

  /**
   * Determines that this is not a \".
   * @return <code>false</code>
   */
  public boolean isEscapedDoubleQuote() {
    return false;
  }

  /**
   * Determines that this is not open.
   * @return <code>false</code>
   */
  public boolean isOpen() {
    return false;
  }

  /**
   * Determines that this is not closed.
   * @return <code>false</code>
   */
  public boolean isClosed() {
    return false;
  }

  /**
   * Determines that this is not a match.
   * @param other the token to compare to
   * @return <code>false</code>
   */
  public boolean isMatch(ReducedToken other) {
    return false;
  }

  /**
   * Determines that this is not an open brace.
   * @return <code>false</code>
   */
  public boolean isOpenBrace() {
    return false;
  }

  /**
   * Determines that this is not a closed brace.
   * @return <code>false</code>
   */
  public boolean isClosedBrace() {
    return false;
  }
}
