/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.javalanglevels;

/**
 * A class full of static methods for escaping/unescaping characters.
 * It's abstract because it should not be instantiated.
 */
public abstract class CharConverter {
  /**
   * Escapes the given char to be suitable for writing out in
   * a Java char or String literal.
   *
   * @param c  Character to escape
   * @return  A string consisting of c, properly escaped for use
   *          inside a Java char or String literal. There are no
   *          quotation marks around the result.
   */
  public static String escapeChar(char c) {
    StringBuffer buf = new StringBuffer();
    escapeChar(c, buf);
    return buf.toString();
  }

  /**
   * Escapes the given char to be suitable for writing out in
   * a Java char or String literal.
   *
   * @param c  Character to escape
   * @param buf  StringBuffer where the result is written out
   */
  public static void escapeChar(char c, StringBuffer buf) {
    switch (c) {
      case '\n': buf.append("\\n"); break;
      case '\t': buf.append("\\t"); break;
      case '\b': buf.append("\\b"); break;
      case '\r': buf.append("\\r"); break;
      case '\f': buf.append("\\f"); break;
      case '\\': buf.append("\\\\"); break;
      case '\'': buf.append("\\\'"); break;
      case '\"': buf.append("\\\""); break;
      default:
        // unicode escape all non-ascii
        if ((c < 32) || (c > 127)) {
          String hex = Integer.toHexString(c);
          buf.append("\\u");

          for (int i = hex.length(); i < 4; i++) {
            buf.append('0');
          }

          buf.append(hex);
        }
        else {
          buf.append(c);
        }
        break;
    }
  }

  /**
   * Escapes the given String to be suitable for writing out as a
   * Java String literal.
   *
   * @param s  String to escape
   * @return  A string consisting of s, properly escaped for use
   *          inside a Java String literal. There are no
   *          quotation marks around the result.
   */
  public static String escapeString(String s) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < s.length(); i++) {
      escapeChar(s.charAt(i), buf);
    }

    return buf.toString();
  }

  /**
   * Unescapes the given string, escaped as it would be in Java source,
   * to a single char. Note that this does not handle unicode escapes
   * of the form \ uXXXX; these are expected to have already been processed
   * out of the input string. The only escapes that are handled are octal escapes
   * and \n \t \b \r \f \\ \' \".
   *
   * @param in  String containing (possibly) escaped character, without quotes
   * @return  char value of the input string, unescaped.
   */
  public static char unescapeChar(String in) {
    StringBuffer buf = new StringBuffer();
    int endPos = unescapeString(in, 0, buf);
    if (endPos < in.length()) {
      throw new IllegalArgumentException((in.length() - endPos) + " trailing" +
                                         " characters at the end of character"+
                                         " literal '" + in + "'");
    }

    return buf.charAt(0);
  }

  /**
   * Unescapes the given string, escaped as it would be in Java source,
   * to a String. Note that this does not handle unicode escapes
   * of the form \ uXXXX; these are expected to have already been processed
   * out of the input string. The only escapes that are handled are octal escapes
   * and \n \t \b \r \f \\ \' \".
   *
   * @param in  String containing (possibly) escaped characters, without quotes
   * @return  Unescaped string value of the input string
   */
  public static String unescapeString(String in) {
    // short circuit "" to allow the rest to assume != "".
    if (in.length() == 0) {
      return in;
    }

    StringBuffer buf = new StringBuffer();
    int nextStart = 0;
    
    while (nextStart < in.length()) {
      nextStart = unescapeString(in, nextStart, buf);
    }

    return buf.toString();
  }

  /*
  public static char unescapeChar(String in) {
    if (in.length() == 1) {
      return s.charAt(0);
    }
    else if ((in.length() > 1) && (in.charAt(0) == '\\')) {
      switch (in.charAt(1)) {
        case 'n': return '\n';
        case 't': return '\t';
        case 'b': return '\b';
        case 'r': return '\r';
        case 'f': return '\f';
        case '\\': return '\\';
        case '\'': return '\'';
        case '\"': return '\"';
      }
      
      // deal with octal escapes
      String afterBackslash = in.substring(1);
      try {
        int charValue = Integer.parseInt(afterBackslash, 8);
        if ((charValue > Character.MAX_VALUE) || (charValue < Character.MIN_VALUE)) {
          throw new RuntimeException("octal escaped character out of range: " + in);
        }

        return (char) charValue;
      }
      catch (NumberFormatException e) {
        throw new RuntimeException("multi-char char literal invalid! value=" + in);
      }
    }
    else {
      throw new RuntimeException("multi-character char literal doesn't start with \\! value=" + in);
    }
  }
  */

  /**
   * Unescapes one character in the given string, escaped as it would be in
   * Java source, to a String. Note that this does not handle unicode escapes
   * of the form \ uXXXX; these are expected to have already been processed
   * out of the input string. The only escapes that are handled are octal escapes
   * and \n \t \b \r \f \\ \' \".
   *
   * @param in  String containing (possibly) escaped characters
   * @param startPos  Starting position of the next character to unescape
   * @param out  StringBuffer to write out the unescaped character to.
   *
   * @return  Position after the end of the parsed character
   */
  public static int unescapeString(final String in,
                                   final int startPos,
                                   final StringBuffer out)
  {
    char first = in.charAt(startPos);

    if (first != '\\') {
      out.append(first);
      return startPos + 1;
    }

    char second = in.charAt(startPos + 1);

    switch (second) {
      case 'n': out.append('\n'); return startPos + 2;
      case 't': out.append('\t'); return startPos + 2;
      case 'b': out.append('\b'); return startPos + 2;
      case 'r': out.append('\r'); return startPos + 2;
      case 'f': out.append('\f'); return startPos + 2;
      case '\\': out.append('\\'); return startPos + 2;
      case '\'': out.append('\''); return startPos + 2;
      case '\"': out.append('\"'); return startPos + 2;
    }

    // The only cases left to deal with are octal escapes or invalid.
    if (_isOctalDigit(second)) {
      // If the first digit is < 4, there could be three octal digits.
      // Otherwise there can be only two.
      int maxDigits;
      if (second < '4') {
        maxDigits = 3;
      }
      else {
        maxDigits = 2;
      }

      StringBuffer octal = new StringBuffer(maxDigits);
      octal.append(second);

      int nextDigitPos = startPos + 2;
      while ((octal.length() < maxDigits) && (nextDigitPos < in.length())) {
        char nextChar = in.charAt(nextDigitPos);
        if (_isOctalDigit(nextChar)) {
          octal.append(nextChar);
          nextDigitPos++;
        }
        else { // not an octal digit, so our work here is done.
          break;
        }
      }

      try {
        int charValue = Integer.parseInt(octal.toString(), 8);
        if ((charValue > Character.MAX_VALUE) || (charValue < Character.MIN_VALUE)) {
          throw new IllegalArgumentException("Octal escape beginning at " +
                                             "position " + startPos + " out of range: " + in);
        }

        out.append((char) charValue);
        return nextDigitPos;
      }
      catch (NumberFormatException e) {
        throw new RuntimeException("Impossible to occur, but number format exception in octal escape!");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid escape sequence at position "+
                                         startPos + ": " + in);
    }
  }

  private static boolean _isOctalDigit(char c) {
    return ((c >= '0') && (c <= '7'));
  }
}
