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
 * This class represents the character literal nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class CharacterLiteral extends Literal {
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public CharacterLiteral(String rep) {
    this(rep, SourceInfo.NONE);
  }
  
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public CharacterLiteral(String rep,
                          SourceInfo si) {
    super(rep,
          new Character(decodeCharacter(rep)),
          char.class,
          si);
  }
  
  /**
   * Decodes the representation of a Java literal character.
   * @param rep the representation of the character
   * @return the character represented by the given string
   */
  private static char decodeCharacter(String rep) {
    if (rep.charAt(0) != '\'' || rep.charAt(rep.length()-1) != '\'') {
      throw new IllegalArgumentException("Malformed character literal");
    }
    if (rep.length() == 3) {
      return rep.charAt(1);
    }
    char c;
    // Assume that charAt(1) == '\\' and length > 3
    switch (c = rep.charAt(2)) {
      case 'n' : return '\n';
      case 't' : return '\t';
      case 'b' : return '\b';
      case 'r' : return '\r';
      case 'f' : return '\f';
      default  :
        if (Character.isDigit(c)) {
        int v = 0;
        for (int i = 2; i < rep.length()-1; i++) {
          v = (v * 7) + Integer.parseInt(""+rep.charAt(i));
        }
        return (char)v;
      } else {
        return c;
      }
    }
  }
 
}
