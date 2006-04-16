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

/** The representation of document text in the reduced model.
 *  It is the core atomic piece.
 *  @version $Id$
 */
public abstract class ReducedToken implements ReducedModelStates {
  private ReducedModelState _state;

  public ReducedToken(ReducedModelState state) {
    _state = state;
  }

  /** Get the size of the token.
   *  @return the number of characters represented by the token
   */
  public abstract int getSize();

  /** Get the type of the token.
   *  @return a String representation of the token type
   */
  public abstract String getType();

  /** Set the type of the token
   *  @param type a String representation of the new token type
   */
  public abstract void setType(String type);

  /** Flip between open and closed.  Valid only for braces. */
  public abstract void flip();

  /** Determine if the given token is a open/close match with this.
   *  @param other another ReducedToken
   *  @return true if there is a match
   */
  public abstract boolean isMatch(ReducedToken other);

  /** Get the shadowing state of the token.
   *  @return FREE | INSIDE_SINGLE_QUOTE | INSIDE_DOUBLE_QUOTE | INSIDE_LINE_COMMENT| INSIDE_BLOCK_COMMENT
   */
  public ReducedModelState getState() { return  _state; }

  /** Returns whether the current char is highlighted. / / beginning a comment
   *  would be highlighted but free, so its not the same as getState
   */
  public int getHighlightState() {
    String type = getType();
    if (type.equals("//") || (_state == INSIDE_LINE_COMMENT) || type.equals("/*")
        || type.equals("*/") || (_state == INSIDE_BLOCK_COMMENT)) {
      return  HighlightStatus.COMMENTED;
    }
    if ((type.equals("'") && (_state == FREE)) || (_state == INSIDE_SINGLE_QUOTE)) {
      return  HighlightStatus.SINGLE_QUOTED;
    }
    if ((type.equals("\"") && (_state == FREE)) || (_state == INSIDE_DOUBLE_QUOTE)) {
      return  HighlightStatus.DOUBLE_QUOTED;
    }
    return  HighlightStatus.NORMAL;
  }

  /** Set the shadowing state of the token.
   *  @param state
   */
  public void setState(ReducedModelState state) {
    _state = state;
  }

  /** Indicates whether this brace is shadowed. Shadowing occurs when a brace has been swallowed by a
   *  comment or an open quote.
   *  @return true if the brace is shadowed.
   */
  public boolean isShadowed() { return  _state != FREE; }

  /** Indicates whether this brace is inside quotes.
   *  @return true if the brace is inside quotes.
   */
  public boolean isQuoted() {
    return  _state == INSIDE_DOUBLE_QUOTE;
  }

  /** Indicates whether this brace is commented out.
   *  @return true if the brace is hidden by comments.
   */
  public boolean isCommented() { return  inBlockComment() || inLineComment(); }

  /** Determines whether the current location is inside a block comment.
   *  @return true or false
   */
  public boolean inBlockComment() { return  _state == INSIDE_BLOCK_COMMENT; }

  /** Determines whether the current location is inside a line comment.
   *  @return true or false
   */
  public boolean inLineComment() { return  _state == INSIDE_LINE_COMMENT; }

  /** Determines whether the current location is part of a multiple char brace.
   *  @return true or false
   */
  public abstract boolean isMultipleCharBrace();

  /** Determines whether the current location is within in gap.
   *  @return true or false
   */
  public abstract boolean isGap();

  /** Determines whether the current location is a line comment
   *  @return true or false
   */
  public abstract boolean isLineComment();

  /** Determines if current location is the beginning of a block comment
   * @return true or false
   */
  public abstract boolean isBlockCommentStart();

  /** Determines whether the current location is the end of a block comment
   *  @return boolean
   */
  public abstract boolean isBlockCommentEnd();

  /**
   * Determines whether the current location is a new line.
   * @return boolean
   */
  public abstract boolean isNewline();

  /** Returns whether the current location is a slash
   * @return boolean
   */
  public abstract boolean isSlash();

  /** Returns whether this is a star
   *  @return boolean
   */
  public abstract boolean isStar();

  /** Returns whether this is a double quote
   *  @return boolean
   */
  public abstract boolean isDoubleQuote();

  /** Returns whether this is a single quote
   *  @return boolean
   */
  public abstract boolean isSingleQuote();

  /** Returns whether this is a double escape sequence
   *  @return boolean
   */
  public abstract boolean isDoubleEscapeSequence();

  /** Returns whether this is a double escape
   *  @return boolean
   */
  public abstract boolean isDoubleEscape();

  /** Returns whether this is an escaped single quote
   *  @return boolean
   */
  public abstract boolean isEscapedSingleQuote();

  /** Return whether this is an escaped double quote
   *  @return boolean
   */
  public abstract boolean isEscapedDoubleQuote();

  /** Increases the size of the gap.
   * @param delta
   */
  public abstract void grow(int delta);

  /** Decreases the size of the gap.
   *  @param delta
   */
  public abstract void shrink(int delta);

  /** Determines whether the current location is an opening parenthesis.
   *  @return boolean
   */
  public abstract boolean isOpen();

  /** Determines whether the current location is a closing parenthesis.
   *  @return boolean
   */
  public abstract boolean isClosed();

  /** Determines whether the current location is an open brace.
   * @return boolean
   */
  public abstract boolean isOpenBrace();

  /** Determines whether the current location is a closed brace.
   *  @return boolean
   */
  public abstract boolean isClosedBrace();
}



