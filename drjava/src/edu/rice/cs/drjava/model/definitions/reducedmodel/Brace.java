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

/** This class acts as the representation of a brace in the reduced view.  It also includes information about the gap
  * of plaintext preceding the actual brace before the previous brace or the start of the file.
  * WARNING: the code in this class critically depends on the fact that literal strings are interned.
  * @version $Id: Brace.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
class Brace extends ReducedToken implements ReducedModelStates {
  
  /** An array of the special characters that signify areas of text other than gaps.  NOTE: this data structure is NOT
    * simply a flat array. Matching characters are placed next to each other (except for the trailing elements,
    * which have no matches). Notice that single and double quotes match themselves.
    * @see String
    */
  public static final String[] braces =  {
    "{", "}", "(", ")", "[", "]", "/*", "*/", "//", "\n", "/", "*", "\"", "\"", "'", "'", "\\\\", "\\", "\\'", "\\\"", ""
  };
  
  public static final int BRACES_LENGTH = braces.length;
  public static final int LAST_BRACE_INDEX = braces.length - 1;
  
  public static final int BLK_CMT_BEG_TYPE = findBrace("/*");
  public static final int BLK_CMT_END_TYPE = findBrace("*/");
  public static final int EOLN_TYPE = findBrace("\n");
  public static final int LINE_CMT_TYPE = findBrace("//");
  public static final int SINGLE_QUOTE_TYPE = findBrace("'");
  public static final int DOUBLE_QUOTE_TYPE = findBrace("\"");
  public static final int STAR_TYPE = findBrace("*");
  public static final int SLASH_TYPE = findBrace("/");
  public static final int DOUBLE_ESCAPE_TYPE = findBrace("\\\\");
  public static final int ESCAPED_SINGLE_QUOTE_TYPE = findBrace("\\'");
  public static final int ESCAPED_DOUBLE_QUOTE_TYPE = findBrace("\\\"");

  
  private volatile int _type;  /** the type of this brace, which is MUTABLE via flip and setType */
  private volatile int _size;  /** the size of this brace */

  /** Virtual constructor.
    * @param type the brace text
    * @param state whether the brace is shadwowed by a comment, quote etc
    * @return a new Brace if type is valid, otherwise null
    * @throws BraceException if the given type is not a valid brace type.
    */
  public static Brace MakeBrace(String type, ReducedModelState state) {
    int index = findBrace(type.intern());
    if (index == BRACES_LENGTH) throw new BraceException("Invalid brace type \"" + type + "\"");
    else return new Brace(index, state);
  }

  /** Constructor.
    * @param type the brace type
    * @param state the state of the reduced model
    */
  private Brace(int type, ReducedModelState state) {
    super(state);
    _type = type;
    _size = getType().length();
  }

  /** Get the text of the brace.
    * @return the text of the Brace
    */
  public String getType() { return (_type == BRACES_LENGTH) ? "!" : braces[_type]; }

  /** @return the size of the brace and its preceding gap. */
  public int getSize() { return _size; }

  /** Converts a Brace to a String.
    * Used for debugging.
    * @return the string representation of the Brace.
    */
  public String toString() {
    //String val = "Brace(size: " +  getSize()  + "): ";
    final StringBuilder val = new StringBuilder("Brace<");
//    int i;
//    for (i = 0; i < getSize(); i++) {
//      val.append(getType().charAt(i));
//    }
//    val.append("' ");
    val.append(getType());
    return val.append('>').toString();
  }

  /** Flips the orientation of the brace. Useful for updating quote information. Does not change _size. */
  public void flip() {
    if (isOpen()) _type += 1;
    else if (_type < braces.length - 1) _type -= 1;
  }

  /** Indicates whether this is an opening brace.
    * @return true if the brace is an opening brace.
    */
  public boolean isOpen() { return (((_type % 2) == 0) && (_type < braces.length - 1)); }

  /** @return true if this is {|(|[ */
  public boolean isOpenBrace() { return ((_type == 0) || (_type == 2) || (_type == 4)); }

  /** @return true if this is }|)|] */
  public boolean isClosedBrace() { return ((_type == 1) || (_type == 3) || (_type == 5)); }

  /** Indicates whether this is a closing brace.
    * @return true if the brace is a closing brace.
    */
  public boolean isClosed() { return ! isOpen(); }

  /** Reset the type of this brace.
    * @param type the new String type for the brace
    */
  public void setType(String type) {
    type = type.intern();
    int index = findBrace(type);
    if (index == braces.length) throw new BraceException("Invalid brace type \"" + type + "\"");
    _type = index;
    _size = getType().length();
  }

  /** Determine the brace _type of the given String. The integer value returned is only used internally.
    * ASSUMES that the given String is interned!  Externally, the brace shows the text as its "type".
    * @param type the text of the brace
    * @return an integer indicating the type of brace
    */
  private static int findBrace(String type) {
    assert type == type.intern();
    int i;
    for (i = 0; i < braces.length; i++) {
      if (type == braces[i]) break;
    }
    return  i;
  }

  /** Check if two braces match.
    * @param other the brace to compare
    * @return true if this is a match for other.
    */
  public boolean isMatch(Brace other) {
    int off = isOpen() ? 1 : -1;
    return _type + off == other._type;
  }
  
  /** Determines if this Brace is matchable (one of "{", "}", "(", ")", "[", "]"). */
  public boolean isMatchable() { return _type < BLK_CMT_BEG_TYPE; }

  /** @return true if this is a double quote */
  public boolean isDoubleQuote() { return _type == DOUBLE_QUOTE_TYPE; }

  /** @return true if this is a single quote */
  public boolean isSingleQuote() { return _type == SINGLE_QUOTE_TYPE; }

  /** @return true if this is a line comment delimiter */
  public boolean isLineComment() { return _type == LINE_CMT_TYPE; }

  /** @return true if this is a block comment open delimiter */
  public boolean isBlockCommentStart() { return _type == BLK_CMT_BEG_TYPE; }

  /** @return true if this is a block comment close delimiter */
  public boolean isBlockCommentEnd() { return _type == BLK_CMT_END_TYPE; }

  /** @return true if this is a newline delimiter */
  public boolean isNewline() { return _type == EOLN_TYPE; }

  /** @return true if this is a multiple character brace */
  public boolean isMultipleCharBrace() {
    return isLineComment() || isBlockCommentStart() || isBlockCommentEnd() || isDoubleEscapeSequence();
  }

  /** @return true if this is \\ or \" */
  public boolean isDoubleEscapeSequence() { 
    return isDoubleEscape() || isEscapedDoubleQuote() || isEscapedSingleQuote(); 
  }

  /** @return true if this is \\ */
  public boolean isDoubleEscape() { return _type == DOUBLE_ESCAPE_TYPE; }

  /** @return true if this is \" */
  public boolean isEscapedDoubleQuote() {return _type == ESCAPED_DOUBLE_QUOTE_TYPE; }

  /** @return true if this is \' */
  public boolean isEscapedSingleQuote() { return _type == ESCAPED_SINGLE_QUOTE_TYPE; }

  /** Implementation of abstract function. Braces, of course, are never Gaps. */
  public boolean isGap() { return false; }

  /** @return true if this is / */
  public boolean isSlash() { return _type == SLASH_TYPE; }

  /** @return true if this is * */
  public boolean isStar() { return _type == STAR_TYPE; }

  /** Braces can't grow.
    * @throws RuntimeException
    */
  public void grow(int delta) { throw new BraceException("Braces can't grow."); }

  /** Braces can't shrink.
    * @throws RuntimeException
    */
  public void shrink(int delta) { throw new BraceException("Braces can't shrink."); }
}





