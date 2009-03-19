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

import java.math.BigInteger;

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
  public LongLiteral(String rep) throws NumberFormatException {
    this(rep, SourceInfo.NONE);
  }
  
  /**
   * Initializes a literal
   * @param rep the representation of the literal
   */
  public LongLiteral(String rep, SourceInfo si) throws NumberFormatException {
    super(rep,
          parse(rep.substring(0, rep.length())),
          long.class,
          si);
  }
  
  /**
   * Parse the representation of an integer
   */
  private static Long parse(String s) throws NumberFormatException {
    int radix = 10;
    int start = 0;
    boolean negate = false;
    int end = s.length();
    if (s.endsWith("l") || s.endsWith("L")) { end--; }
    // only consider 0x or 0 or - if this doesn't make the string empty
    if ((end-start>1) && (s.startsWith("-"))) { start++; negate = true; }
    if ((end-start>2) && (s.startsWith("0x",start))) { radix = 16; start += 2; }
    else if ((end-start>1) && (s.startsWith("0",start)) && (s.length() > 1)) { radix = 8; start++; }
    // BigInteger can parse hex numbers representing negative longs; Long can't
    BigInteger val = new BigInteger(s.substring(start, end), radix);
    if (negate) { val = val.negate(); }
    long result = val.longValue();
    if (val.bitLength() > 64 || (radix == 10 && !val.equals(BigInteger.valueOf(result)))) {
      throw new NumberFormatException("Literal is out of range: "+s);
    }
    return result;
  }
  
}
