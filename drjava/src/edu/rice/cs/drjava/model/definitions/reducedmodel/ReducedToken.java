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

/** The representation of document text in the reduced model. ReducedToken :: = Brace | Gap
  * @version $Id: ReducedToken.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class ReducedToken implements ReducedModelStates {
  private volatile ReducedModelState _state;
  
  public ReducedToken(ReducedModelState state) {
    _state = state;
  }
  
  /** Get the size of the token.
    * @return the number of characters represented by the token
    */
  public abstract int getSize();
  
  /** Get the type of the token.
    * @return a String representation of the token type
    */
  public abstract String getType();
  
  /** Set the type of the token
    * @param type a String representation of the new token type
    */
  public abstract void setType(String type);
  
  /** Return the opposite of this ReducedToken in the same state. Valid only for braces. */
  public abstract void flip();
  
  /** Determine if the given token is a open/close match with this.
    * @param other another ReducedToken
    * @return true if there is a match
    */
  public abstract boolean isMatch(Brace other);
  
  /** Return true iff this ReducedToken is a matchable, i.e. is one of "{", "}", "(", ")", "[", "]" */
  public abstract boolean isMatchable();
  
  /** Get the shadowing state of the token.
    * @return FREE | INSIDE_SINGLE_QUOTE | INSIDE_DOUBLE_QUOTE | INSIDE_LINE_COMMENT| INSIDE_BLOCK_COMMENT
    */
  public ReducedModelState getState() { return  _state; }
  
  /** Returns whether the current char is highlighted. / / beginning a comment would be highlighted but free, so its not
    * the same as getState.
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
    * @param state
    */
  public void setState(ReducedModelState state) { _state = state; }
  
  /** Increases the size of the gap.
    * @param delta
    */
  public abstract void grow(int delta);
  
  /** Decreases the size of the gap.
    * @param delta
    */
  public abstract void shrink(int delta);
  
  /** Indicates whether this brace is shadowed. Shadowing occurs when a brace has been swallowed by a
    * comment or an open quote.
    * @return true if the brace is shadowed.
    */
  public boolean isShadowed() { return  _state != FREE; }
  
  /** Indicates whether this brace is inside quotes.
    * @return true if the brace is inside quotes.
    */
  public boolean isQuoted() { return  _state == INSIDE_DOUBLE_QUOTE; }
  
  /** Indicates whether this brace is commented out.  Package visible for testing purposes.
    * @return true if the brace is hidden by comments.
    */
  boolean isCommented() { return  insideBlockComment() || inLineComment(); }
  
  /** Determines whether the current location is inside a block comment.  (Excludes opening "brace"!)
    * @return true or false
    */
  private boolean insideBlockComment() { return  _state == INSIDE_BLOCK_COMMENT; }
  
  /** Determines whether the current location is inside a line comment.  (Excludes opening "brace"!)
    * @return true or false
    */
  private boolean inLineComment() { return  _state == INSIDE_LINE_COMMENT; }
  
  /** Determines whether the current location is part of a multiple char brace.
    * @return true or false
    */
  public abstract boolean isMultipleCharBrace();
  
  /** Determines whether the current location is within in gap.
    * @return true or false
    */
  public abstract boolean isGap();
  
  /** Determines whether the current location is a line comment
    * @return true or false
    */
  public abstract boolean isLineComment();
  
  /** Determines if current location is the beginning of a block comment
    * @return true or false
    */
  public abstract boolean isBlockCommentStart();
  
  /** Determines whether the current location is the end of a block comment
    * @return boolean
    */
  public abstract boolean isBlockCommentEnd();
  
  /** Determines whether the current location is a new line.
    * @return boolean
    */
  public abstract boolean isNewline();
  
  /** Returns whether the current location is a slash
    * @return boolean
    */
  public abstract boolean isSlash();
  
  /** Returns whether this is a star
    * @return boolean
    */
  public abstract boolean isStar();
  
  /** Returns whether this is a double quote
    * @return boolean
    */
  public abstract boolean isDoubleQuote();
  
  /** Returns whether this is a single quote
    * @return boolean
    */
  public abstract boolean isSingleQuote();
  
  /** Returns whether this is a double escape sequence
    * @return boolean
    */
  public abstract boolean isDoubleEscapeSequence();
  
  /** Returns whether this is a double escape
    * @return boolean
    */
  public abstract boolean isDoubleEscape();
  
  /** Returns whether this is an escaped single quote
    * @return boolean
    */
  public abstract boolean isEscapedSingleQuote();
  
  /** Return whether this is an escaped double quote
    * @return boolean
    */
  public abstract boolean isEscapedDoubleQuote();
  
  /** Determines whether the current location is an opening parenthesis.
    * @return boolean
    */
  public abstract boolean isOpen();
  
  /** Determines whether the current location is a closing parenthesis.
    * @return boolean
    */
  public abstract boolean isClosed();
  
  /** Determines whether the current location is an open brace.
    * @return boolean
    */
  public abstract boolean isOpenBrace();
  
  /** Determines whether the current location is a closed brace.
    * @return boolean
    */
  public abstract boolean isClosedBrace();
  
  /** Determine whether this token is a comment start "brace" ("//" or "/*') */
  public boolean isCommentStart() { return isBlockCommentStart() || isLineComment(); }
}



