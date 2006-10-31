/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.drjava.config;

import java.util.Vector;
import java.util.StringTokenizer;
/**
 * Abstract class defining behavior shared by all
 * configuration options with values of type
 * Vector<T>.
 * @version $Id$
 */
public class VectorOption<T> extends Option<Vector<T>> {

  protected ParseStrategy<T> parser;
  protected FormatStrategy<T> formatter;
  public final String header;
  public final char delim;
  public final String footer;

  /**
   * @param key The name of this option.
   * @param parser the parsing strategy for an element in this option
   * @param formatter the formatting strategy for an element in this option
   */
  private VectorOption(String key, ParseStrategy<T> parser, FormatStrategy<T> formatter,
                       String header, char delim, String footer, Vector<T> def) {
    super(key,def);
    this.parser = parser;
    this.formatter = formatter;
    this.header = header;
    this.delim = delim;
    this.footer = footer;
  }

  public VectorOption(String key, Option<T> strategy, String header,
                      char delim, String footer, Vector<T> def) {
    this(key, strategy, strategy, header, delim, footer,def);
  }

  /**
   * Defaults the "header", "footer", and "delim" fields
   * to open bracket, close bracket, and comma, repsectively.
   * @param key The name of this option.
   * @param option The object that knows how to parse and format
   * an element of type T.
   */
  public VectorOption(String key, Option<T> option, Vector<T> def) {
    this(key,option,option,"[",',',"]",def);
  }

  /**
   * @param s The String to be parsed.
   * @return An instance of Vector<T> represented by "s".
   * @exception IllegalArgumentException if "s" is not formatted
   * according to the method Vector<T>.toString().
   */
  public Vector<T> parse(String s) {
    s= s.trim();
    int startFirstElement = header.length();
    int startFooter = s.length() - footer.length();

    if (startFooter < startFirstElement ||
        !s.startsWith(header) ||
        !s.endsWith(footer)) {
      throw new OptionParseException(name, s,
                                     "Value must start with "+header+" and end "+
                                     "with "+footer+" to be a valid vector.");
    }
    s = s.substring(startFirstElement, startFooter);
    String d = String.valueOf(delim);
    StringTokenizer st = new StringTokenizer(s,d,true);

    Vector<T> res = new Vector<T>();
    boolean sawDelim = st.hasMoreTokens();

    while(st.hasMoreTokens()) {
      String token = st.nextToken();
      boolean isDelim = token.equals(d);

      if (!isDelim) {
        res.add(parser.parse(token));
      } else if (sawDelim) { // isDelim & sawDelim (two delims in a row)
        throw new OptionParseException(name, s,
                                       "Argument contains delimiter with no preceding list element.");
      }
      sawDelim = isDelim;
    }
    if (sawDelim) {
      throw new OptionParseException(name, s,
                                     "Value shouldn't end with a delimiter.");
    }
    return res;
  }

  /** Formats the Vector v.  The overall String format is determined by the method Vector<T>.tString(), but each 
    * element of the vector is formatted by calling formatElement().
    * @param v The Vector to be formatted.
    * @return A String representing "v". 
    */
  public String format(Vector<T> v) {
    final StringBuilder res = new StringBuilder(header);

    int size = v.size();
    int i = 0;
    while (i < size) {
      res.append(formatter.format(v.get(i)));
      i++;
      if (i < size) res.append(delim);
    }
    return res.append(footer).toString();
  }
}

