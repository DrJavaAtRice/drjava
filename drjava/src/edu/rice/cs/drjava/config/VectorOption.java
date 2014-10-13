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

package edu.rice.cs.drjava.config;

import java.util.Vector;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

/**
 * Abstract class defining behavior shared by all
 * configuration options with values of type
 * Vector<T>.
 * VectorOption<String> now allows empty strings, i.e. "[,]" is a vector of two empty strings.
 * "[]" will be interpreted as a vector of one empty string, and "" is an empty vector.
 * @version $Id: VectorOption.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class VectorOption<T> extends Option<Vector<T>> {

  protected ParseStrategy<T> parser;
  protected FormatStrategy<T> formatter;
  public final String header;
  public final char delim;
  public final String footer;

  /** @param key The name of this option.
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

  /** Defaults the "header", "footer", and "delim" fields
   * to open bracket, close bracket, and comma, repsectively.
   * @param key The name of this option.
   * @param option The object that knows how to parse and format
   * an element of type T.
   */
  public VectorOption(String key, Option<T> option, Vector<T> def) {
    this(key,option,option,"[",',',"]",def);
  }

  /** @param s The String to be parsed.
   * @return An instance of Vector<T> represented by "s".
   * @exception IllegalArgumentException if "s" is not formatted
   * according to the method Vector<T>.toString().
   */
  public Vector<T> parse(String s) {
    s= s.trim();
    Vector<T> res = new Vector<T>();
    if (s.equals("")) { return res; }

    int startFirstElement = header.length();
    int startFooter = s.length() - footer.length();

    if (!s.startsWith(header) && !s.endsWith(footer)) {
      // not formatted as a vector, try parsing this as a singleton vector
      res.add(parser.parse(s));
      return res;
    }
    if (startFooter < startFirstElement || !s.startsWith(header) || ! s.endsWith(footer)) {
      throw new OptionParseException(name, s, "Value must start with " + header + " and end " + "with " + footer + 
                                     " to be a valid vector.");
    }
    s = s.substring(startFirstElement, startFooter);
    if (s.equals("")) {
      res.add(parser.parse(""));
      return res;
    }
    
//    String d = String.valueOf(delim);

    StreamTokenizer st = new StreamTokenizer(new StringReader(s));
    st.resetSyntax();
    st.wordChars(0,255);
    st.ordinaryChar('|');
    st.ordinaryChar(delim);
    try {
      int tok = st.nextToken();
      int prevtok = -4;
      StringBuilder sb = new StringBuilder();
      while (tok!=StreamTokenizer.TT_EOF) {
        if (tok=='|') {
          if (prevtok=='|') {
            // second pipe in a row, append a pipe to string builder
            sb.append('|');
            prevtok = tok = -4;
          }
          else {
            // first pipe, next token decides
            prevtok = tok;
          }
        }
        else if (tok==delim) {
          if (prevtok=='|') {
            // pipe followed by delimiter --> escaped delimiter
            // append delimiter to string builder
            sb.append(delim);
            prevtok = tok = -4;
          }
          else {
            // no preceding pipe --> real delimiter
            res.add(parser.parse(sb.toString()));
            sb.setLength(0); // clear string builder
            prevtok = tok;
          }
        }
        else {
          // not a pipe or delimiter
          if (prevtok=='|') {
            // backslash followed by neither a backslash nor a delimiter
            // invalid
            throw new OptionParseException(name, s, "A pipe | was discovered before the token '" + st.sval +
                                           "'. A pipe is only allowed in front of another pipe " +
                                           "or the delimiter " + delim + ".");
          }
          sb.append(st.sval);
          prevtok = tok;
        }
        
        tok = st.nextToken();
      }
      
      res.add(parser.parse(sb.toString()));      
    }
    catch(IOException ioe) {
      throw new OptionParseException(name, s, "An IOException occurred while parsing a vector.");
    }

    return res;
  }

  /** Formats the Vector v.  The overall String format is determined by the method Vector<T>.tString(), but each 
    * element of the vector is formatted by calling formatElement().
    * @param v The Vector to be formatted.
    * @return A String representing "v". 
    */
  public String format(Vector<T> v) {
    if (v.size() == 0) { return ""; }
    
//    String d = String.valueOf(delim);
    final StringBuilder res = new StringBuilder(header);

    int size = v.size();
    int i = 0;
    while (i < size) {
      String str = formatter.format(v.get(i));
      str = str.replaceAll("\\|","||");
      str = str.replaceAll(",","|,");
      res.append(str);
      i++;
      if (i < size) res.append(delim);
    }
    String str = res.append(footer).toString();
    return str;
  }
}

