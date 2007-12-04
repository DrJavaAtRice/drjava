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
 * This class represents the long literal nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class LongLiteral extends Literal {
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public LongLiteral(String rep) {
    this(rep, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   * @param fn  the filename
   * @param bl  the begin line
   * @param bc  the begin column
   * @param el  the end line
   * @param ec  the end column
   */
  public LongLiteral(String rep, String fn, int bl, int bc, int el, int ec) {
    super(rep,
          parse(rep.substring(0, rep.length())), //corrected bug, was rep.length()-1
          long.class,
          fn, bl, bc, el, ec);
  }
  
  /**
   * Parse the representation of an integer
   */
  private static Long parse(String s) {
    int radix = 10;
    int start = 0;
    int end = s.length();
    if (s.endsWith("l") || s.endsWith("L")) { end--; }
    if (s.startsWith("0x")) { radix = 16; start += 2; }
    else if (s.startsWith("0") && s.length() > 1) { radix = 8; start++; }
    return Long.valueOf(s.substring(start, end), radix);
  }
  
}
