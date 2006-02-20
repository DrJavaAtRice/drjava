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

/**
 * This class acts as the representation of a brace in the reduced
 * view.  It also includes information about the gap of plaintext
 * preceding the actual brace before the previous brace or the start
 * of the file.
 * @version $Id$
 */
class Brace extends ReducedToken implements ReducedModelStates {
  /**
   * An array of the special characters that signify areas of text other than gaps.
   * This really isn't the best datastructure to hold this information; there is
   * more structure to the elements than a flat array. Specifically, matching
   * characters are placed next to each other (except for the trailing elements,
   * which have no matches). Notice that single and double quotes match themselves.
   * @see String
   */
  public static final String[] braces =  {
    "{", "}", "(", ")", "[", "]", "/*", "*/", "//", "\n", "/", "*", "\"", "\"",
    "'", "'", "\\\\", "\\", "\\'", "\\\"", ""
  };
  public static final String BLK_CMT_BEG = "/*";
  public static final String BLK_CMT_END = "*/";
  public static final String EOLN = "\n";
  public static final String LINE_CMT = "//";
  public static final String SINGLE_QUOTE = "'";
  public static final String DOUBLE_QUOTE = "\"";
  public static final String STAR = "*";
  public static final String SLASH = "/";

  /** the type of the brace */
  protected int _type;

  /**
   * Virtual constructor.
   * @param type the brace text
   * @param state whether the brace is shadwowed by a comment, quote etc
   * @return a new Brace if type is valid, otherwise null
   * @throws BraceException if the given type is not a valid brace type.
   */
  public static Brace MakeBrace(String type, ReducedModelState state) {
    int index = findBrace(type);
    if (index == braces.length) {
      throw new BraceException("Invalid brace type \"" + type + "\"");
    }
    else {
      return new Brace(index, state);
    }
  }

  /**
   * Constructor.
   * @param type the brace type
   * @param state the state of the reduced model
   */
  private Brace(int type, ReducedModelState state) {
    super(state);
    _type = type;
  }

  /**
   * Get the text of the brace.
   * @return the text of the Brace
   */
  public String getType() {
    return (_type == braces.length) ? "!" : braces[_type];
  }

  /**
   * @return the size of the brace and its preceding gap
   */
  public int getSize() {
    return getType().length();
  }

  /**
   * Converts a Brace to a String.
   * Used for debugging.
   * @return the string representation of the Brace.
   */
  public String toString() {
    //String val = "Brace(size: "+ getSize() +"): ";
    StringBuffer val = new StringBuffer();
    int i;
    for (i = 0; i < getSize(); i++) {
      val.append(' ');
      val.append(getType().charAt(i));
    }
    return val.toString();
  }

  /** Flips the orientation of the brace.
   *  Useful for updating quote information.
   */
  public void flip() {
    if (isOpen()) _type += 1;
    else if (_type < braces.length - 1) _type -= 1;
  }

  /**
   * Indicates whether this is an opening brace.
   * @return true if the brace is an opening brace.
   */
  public boolean isOpen() {
    return (((_type%2) == 0) && (_type < braces.length - 1));
  }

  /**
   * @return true if this is {|(|[
   */
  public boolean isOpenBrace() {
    return ((_type == 0) || (_type == 2) || (_type == 4));
  }

  /**
   * @return true if this is }|)|]
   */
  public boolean isClosedBrace() {
    return ((_type == 1) || (_type == 3) || (_type == 5));
  }

  /**
   * Indicates whether this is a closing brace.
   * @return true if the brace is a closing brace.
   */
  public boolean isClosed() {
    return !isOpen();
  }

  /**
   * Reset the type of this brace.
   * @param type the new String type for the brace
   */
  public void setType(String type) {
    int index = findBrace(type);
    if (index == braces.length) {
      throw new BraceException("Invalid brace type \"" + type + "\"");
    }
    else {
      _type = index;
    }
  }

  /**
   * Determine the brace type of a given String.
   * The integer value returned is only used internally.
   * Externally, the brace shows the text as its "type".
   * @param type the text of the brace
   * @return an integer indicating the type of brace
   */
  protected static int findBrace(String type) {
    int i;
    for (i = 0; i < braces.length; i++) {
      if (type.equals(braces[i]))
        break;
    }
    return  i;
  }

  /**
   * Check if two braces match.
   * @param other the brace to compare
   * @return true if this is a match for other.
   */
  public boolean isMatch(ReducedToken other) {
    if (this.getType().equals("")) {
      return false;
    }
    int off = (this.isOpen()) ? 1 : -1;
    return (braces[_type + off].equals(other.getType()));
  }

  /**
   * @return true if this is a quote
   */
  public boolean isDoubleQuote() {
    return this.getType().equals(DOUBLE_QUOTE);
  }

  public boolean isSingleQuote() {
    return this.getType().equals(SINGLE_QUOTE);
  }


  /**
   * @return true if this is a line comment delimiter
   */
  public boolean isLineComment() {
    return this.getType().equals(LINE_CMT);
  }

  /**
   * @return true if this is a block comment open delimiter
   */
  public boolean isBlockCommentStart() {
    return this.getType().equals(BLK_CMT_BEG);
  }

  /**7
   * @return true if this is a block comment close delimiter
   */
  public boolean isBlockCommentEnd() {
    return this.getType().equals(BLK_CMT_END);
  }

  /** @return true if this is a newline delimiter
   */
  public boolean isNewline() {
    return this.getType().equals(EOLN);
  }

  /** @return true if this is a multiple character brace
   */
  public boolean isMultipleCharBrace() {
    return isLineComment() || isBlockCommentStart() ||
           isBlockCommentEnd() || isDoubleEscapeSequence();
  }

  /**
   * @return true if this is \\ or \"
   */
  public boolean isDoubleEscapeSequence() {
    return  isDoubleEscape() || isEscapedDoubleQuote() ||
      isEscapedSingleQuote();
  }

  /**
   * @return true if this is \\
   */
  public boolean isDoubleEscape() {
    return  this.getType().equals("\\\\");
  }

  /**
   * @return true if this is \"
   */
  public boolean isEscapedDoubleQuote() {
    return  this.getType().equals("\\\"");
  }

  /**
   * @return true if this is \'
   */
  public boolean isEscapedSingleQuote() {
    return this.getType().equals("\\'");
  }

  /**
   * Implementation of abstract function.
   * Braces, of course, are never Gaps.
   * @return false
   */
  public boolean isGap() {
    return  false;
  }

  /**
   * @return true if this is /
   */
  public boolean isSlash() {
    return this.getType().equals(SLASH);
  }

  /**
   * @return true if this is *
   */
  public boolean isStar() {
    return this.getType().equals(STAR);
  }

  /**
   * Braces can't grow.
   * @throws RuntimeException
   */
  public void grow(int delta) {
    throw new RuntimeException("Braces can't grow.");
  }

  /**
   * Braces can't shrink.
   * @throws RuntimeException
   */
  public void shrink(int delta) {
    throw new RuntimeException("Braces can't shrink.");
  }

}


/**
 * An exception class used by methods in this class.
 */
class BraceException extends RuntimeException {

  /**
   * Creates a new BraceException
   * @param s the message
   */
  public BraceException(String s) {
    super(s);
  }
}



