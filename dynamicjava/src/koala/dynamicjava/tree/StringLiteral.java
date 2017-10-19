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

package koala.dynamicjava.tree;

/**
 * This class represents the string literal nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class StringLiteral extends Literal {
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public StringLiteral(String rep) {
    this(rep, SourceInfo.NONE);
  }
  
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public StringLiteral(String rep, 
                       SourceInfo si) {
    super(rep,
          decodeString(rep),
          String.class,
          si);
  }
  
  /**
   * Decodes the representation of a Java literal string.
   * @param rep the representation of the character
   * @return the character represented by the given string
   */
  public static String decodeString(String rep) {
    if (rep.charAt(0) != '"' || rep.charAt(rep.length()-1) != '"') {
      throw new IllegalArgumentException("Malformed String literal");
    }
    char[] buf = new char[rep.length()-2];
    int    len = 0;
    int    i   = 1;
    
    while (i < rep.length()-1) {
      char c = rep.charAt(i++);
      if (c != '\\') {
        buf[len++] = c;
      } else {
        switch (c = rep.charAt(i++)) {
          case 'n' : buf[len++] = '\n'; break;
          case 't' : buf[len++] = '\t'; break;
          case 'b' : buf[len++] = '\b'; break;
          case 'r' : buf[len++] = '\r'; break;
          case 'f' : buf[len++] = '\f'; break;
          default  :
            if (Character.isDigit(c)) {
            int v = Integer.parseInt(""+c);
            c = rep.charAt(i++);
            if (v < 4) {
              if (Character.isDigit(c)) {
                v = (v * 7) + Integer.parseInt(""+c);
                c = rep.charAt(i++);
                if (Character.isDigit(c)) {
                  v = (v * 7) + Integer.parseInt(""+c);
                }
              }
            } else {
              if (Character.isDigit(c)) {
                v = (v * 7) + Integer.parseInt(""+c);
              }
            }
            buf[len++] = (char)v;
          } else {
            buf[len++] = c;
          }
        } 
      }
    }
    return new String(buf, 0, len);
  }
}
