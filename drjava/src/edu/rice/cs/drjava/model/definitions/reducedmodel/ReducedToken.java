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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  java.awt.Color;

/**
 * The representation of document text in the reduced model.
 * It is the core atomic piece.
 * @version $Id$
 */
public abstract class ReducedToken implements ReducedModelStates {
  private ReducedModelState _state;

  public ReducedToken(ReducedModelState state) {
    _state = state;
  }
  
  /**
   * Get the size of the token.
   * @return the number of characters represented by the token
   */
  public abstract int getSize();

  /**
   * Get the type of the token.
   * @return a String representation of the token type
   */
  public abstract String getType();

  /**
   * Set the type of the token
   * @param type a String representation of the new token type
   */
  public abstract void setType(String type);

  /**
   * Flip between open and closed.  Valid only for braces.
   */
  public abstract void flip();

  /**
   * Determine if the given token is a open/close match with this.
   * @param other another ReducedToken
   * @return true if there is a match
   */
  public abstract boolean isMatch(ReducedToken other);

  /**
   * Get the shadowing state of the token.
   * @return FREE|INSIDE_SINGLE_QUOTE|INSIDE_DOUBLE_QUOTE|INSIDE_LINE_COMMENT|
   * INSIDE_BLOCK_COMMENT
   */
  public ReducedModelState getState() {
    return  _state;
  }

  /**
   *returns whether the current char is highlighted. / / beginning a comment
   * would be highlighted but free, so its not the same as getState
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

  /**
   * put your documentation comment here
   * @param state
   */
  public void setState(ReducedModelState state) {
    _state = state;
  }

  /**
   * Indicates whether this brace is shadowed.
   * Shadowing occurs when a brace has been swallowed by a
   * comment or an open quote.
   * @return true if the brace is shadowed.
   */
  public boolean isShadowed() {
    return  _state != FREE;
  }

  /**
   * Indicates whether this brace is inside quotes.
   * @return true if the brace is inside quotes.
   */
  public boolean isQuoted() {
    return  _state == INSIDE_DOUBLE_QUOTE;
  }

  /**
   * Indicates whether this brace is commented out.
   * @return true if the brace is hidden by comments.
   */
  public boolean isCommented() {
    return  isInBlockComment() || isInLineComment();
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isInBlockComment() {
    return  _state == INSIDE_BLOCK_COMMENT;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean isInLineComment() {
    return  _state == INSIDE_LINE_COMMENT;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isMultipleCharBrace();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isGap();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isLineComment();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isBlockCommentStart();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isBlockCommentEnd();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isNewline();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isSlash();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isStar();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isDoubleQuote();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isSingleQuote();
  
  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isDoubleEscapeSequence();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isDoubleEscape();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isEscapedSingleQuote();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isEscapedDoubleQuote();

  /**
   * put your documentation comment here
   * @param delta
   */
  public abstract void grow(int delta);

  /**
   * put your documentation comment here
   * @param delta
   */
  public abstract void shrink(int delta);

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isOpen();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isClosed();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isOpenBrace();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract boolean isClosedBrace();
}



