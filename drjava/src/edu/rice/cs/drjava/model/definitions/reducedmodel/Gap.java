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

/** A subclass of ReducedToken that represents sequences of non-special characters.
  * @version $Id: Gap.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
class Gap extends ReducedToken {
  private volatile int _size;
  
  /** Creates a new Gap.
    * @param size the size of the gap
    * @param state the state of the reduced model
    */
  Gap(int size, ReducedModelState state) {
    super(state);
    _size = size;
  }
  
  /** Gets the size of this gap.
    * @return _size
    */
  public int getSize() { return _size; }
  
  /** Gets the token type.
    * @return the empty string
    */
  public String getType() { return ""; }
  
  /** Blows up.  The type of a Gap cannot be set.
    * @param type the type to set to
    * @throws RuntimeException always
    */
  public void setType(String type) { throw new RuntimeException("Can't set type on Gap!"); }
  
  /** Blows up.  A Gap cannot be flipped.
    * @throws RuntimeException always
    */
  public void flip() { throw  new RuntimeException("Can't flip a Gap!"); }
  
  /** Increases the size of the gap.
    * @param delta the amount by which the gap is augmented.
    */
  public void grow(int delta) { if (delta >= 0) _size += delta; }
  
  /** Decreases the size of the gap.
    * @param delta the amount by which the gap is diminished.
    */
  public void shrink(int delta) { if (delta <= _size && delta >= 0) _size -= delta; }
  
  /** Converts a Brace to a String.  Used for debugging.
    * @return the String representation of the Gap
    */
  public String toString() {
//    final StringBuilder val = new StringBuilder();
//    int i;
//    for (i = 0; i < _size; i++) val.append(" _");
//    return val.toString();
    return "Gap<" + _size + ">";
  }
  
  /** Determines that this is not a multi-char brace.
    * @return <code>false</code>
    */
  public boolean isMultipleCharBrace() { return false; }
  
  /** Determines that this is a gap.
    * @return <code>true</code>
    */
  public boolean isGap() { return true; }
  
  /** Determines that this is not a line comment.
    * @return <code>false</code>
    */
  public boolean isLineComment() { return false; }
  
  /** Determines that this is not the start of a block comment.
    * @return <code>false</code>
    */
  public boolean isBlockCommentStart() { return false; }
  
  /** Determines that this is not the end of a block comment.
    * @return <code>false</code>
    */
  public boolean isBlockCommentEnd() { return false; }
  
  /** Determines that this is not a newline.
    * @return <code>false</code>
    */
  public boolean isNewline() { return false; }
  
  /** Determines that this is not a /.
    * @return <code>false</code>
    */
  public boolean isSlash() { return false; }
  
  /** Determines that this is not a *.
    * @return <code>false</code>
    */
  public boolean isStar() { return false; }
  
  /** Determines that this is not a ".
    * @return <code>false</code>
    */
  public boolean isDoubleQuote() { return false; }
  
  /** Determines that this is not a '.
    * @return <code>false</code>
    */
  public boolean isSingleQuote() { return false; }
  
  /** Determines that this is not a double escape sequence.
    * @return <code>false</code>
    */
  public boolean isDoubleEscapeSequence() { return false; }
  
  /** Determines that this is not a double escape.
    * @return <code>false</code>
    */
  public boolean isDoubleEscape() { return false; }
  
  /** Determines that this is not a \'.
    * @return <code>false</code>
    */
  public boolean isEscapedSingleQuote() { return false; }
  
  /** Determines that this is not a \".
    * @return <code>false</code>
    */
  public boolean isEscapedDoubleQuote() { return false; }
  
  /** Determines that this is not open.
    * @return <code>false</code>
    */
  public boolean isOpen() { return false; }
  
  /** Determines that this is not closed. */
  public boolean isClosed() { return false; }
  
  /** Determines that this is not a match.
    * @param other the token to compare to
    * @return <code>false</code>
    */
  public boolean isMatch(Brace other) { return false; }
  
  /** Determines that this ReducedToken is not matchable (one of "{", "}", "(", ")", "[", "]") */
  public boolean isMatchable() { return false; }
  
  /** Determines that this is not an open brace. */
  public boolean isOpenBrace() { return false; }
  
  /** Determines that this is not a closed brace. */
  public boolean isClosedBrace() { return false; }
}
