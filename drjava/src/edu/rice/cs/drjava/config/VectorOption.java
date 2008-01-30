/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

    if (startFooter < startFirstElement || !s.startsWith(header) || ! s.endsWith(footer)) {
      throw new OptionParseException(name, s, "Value must start with " + header + " and end " + "with " + footer + 
                                     " to be a valid vector.");
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
        throw new OptionParseException(name, s, "Argument contains delimiter with no preceding list element.");
      }
      sawDelim = isDelim;
    }
    if (sawDelim) throw new OptionParseException(name, s, "Value shouldn't end with a delimiter.");
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

