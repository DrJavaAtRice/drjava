package  edu.rice.cs.drjava;

import  java.awt.Color;

/**
 * @version $Id$
 */
abstract class ReducedToken implements ReducedModelStates {
  protected int _state;

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract int getSize();

  /**
   * put your documentation comment here
   * @return 
   */
  public abstract String getType();

  /**
   * put your documentation comment here
   * @param type
   */
  public abstract void setType(String type);

  /**
   * put your documentation comment here
   */
  public abstract void flip();

  /**
   * put your documentation comment here
   * @param other
   * @return 
   */
  public abstract boolean isMatch(ReducedToken other);

  /**
   * put your documentation comment here
   * @return 
   */
  public int getState() {
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
  public void setState(int state) {
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



