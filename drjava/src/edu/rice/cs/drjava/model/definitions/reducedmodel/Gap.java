package  edu.rice.cs.drjava;

/**
 * @version $Id$
 */
class Gap extends ReducedToken {
  private int _size;

  /**
   * put your documentation comment here
   * @param     int size
   * @param     int state
   */
  Gap(int size, int state) {
    _size = size;
    _state = state;
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
  public boolean isEscapedQuote() {
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



