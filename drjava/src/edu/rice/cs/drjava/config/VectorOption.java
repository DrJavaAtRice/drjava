/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import gj.util.Vector;
import java.util.StringTokenizer;
/**
 * Abstract class defining behavior shared by all
 * configuration options with values of type
 * Vector<T>.
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
        int startFirstElement = header.length();
        int startFooter = s.length() - footer.length(); 
 
        if (startFooter < startFirstElement || 
            !s.startsWith(header) || 
            !s.endsWith(footer)) {
            throw new IllegalArgumentException("String argument does not match format " +
                                               "specified by this VectorOption and its " +
                                               "delimiters.");
        }
        s = s.substring(startFirstElement, startFooter);
        String d = String.valueOf(delim);
        StringTokenizer st = new StringTokenizer(s,d,true);
 
        Vector<T> res = new Vector<T>();
        boolean sawDelim = st.hasMoreTokens();

        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            boolean isDelim = token.equals(d);
     
            if(!isDelim) {
                res.addElement(parser.parse(token));
            } else if(sawDelim) { // isDelim & sawDelim (two delims in a row)
                throw new IllegalArgumentException(" String argument contains delimiter with no preceding list element.");
            } 
            sawDelim = isDelim;
        }
        if(sawDelim) {
            throw new IllegalArgumentException("String argument ends with delimiter.");
        }
        return res;
    }
    
    /**
     * @param v The Vector to be formatted.
     * @return A String representing "v". The overall String
     * format is determined by the method Vector<T>.tString(),
     * but each element of the vector is formatted by calling
     * formatElement().
     */
    public String format(Vector<T> v) {
        StringBuffer res = new StringBuffer(header);

        int size = v.size();
        int i = 0;
        while (i < size) {
            res.append(formatter.format(v.elementAt(i)));
            i++;
            if (i < size) res.append(delim);
        }
        return res.append(footer).toString();
    }
}




