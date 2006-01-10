/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.parser.wrapper;

import koala.dynamicjava.parser.impl.Token;
import koala.dynamicjava.tree.IdentifierToken;

/**
 * This class represents the token managed by the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/06/11
 */

public class TreeToken implements IdentifierToken {
  /**
   * The implementation
   */
  private Token token;
  
  /**
   * Creates a new tree token
   * @param t the parser token
   */
  public TreeToken(Token t) {
    token = t;
  }
  
  /**
   * Returns the underlying token
   */
  public Token getToken() {
    return token;
  }
  
  /**
   * Returns the representation of the identifier
   */
  public String image() {
    return token.image;
  }
  
  /**
   * Returns the line number where the beginning of the token
   * was found in the source file
   */
  public int beginLine() {
    return token.beginLine;
  }
  
  /**
   * Returns the line number where the end of the token
   * was found in the source file
   */
  public int endLine() {
    return token.endLine;
  }
  
  /**
   * Returns the column number where the beginning of the token
   * was found in the source file
   */
  public int beginColumn() {
    return token.beginColumn;
  }
  
  /**
   * Returns the column number where the end of the token
   * was found in the source file
   */
  public int endColumn() {
    return token.endLine;
  }
  
  public String toString() {
    return image();
  }
}
