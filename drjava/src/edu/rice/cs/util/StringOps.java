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

package edu.rice.cs.util;

import edu.rice.cs.plt.tuple.Pair;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * A class to provide some convenient String operations as static methods.
 * It's abstract to prevent (useless) instantiation, though it can be subclassed
 * to provide convenient namespace importation of its methods.
 * @version $Id$
 */

public abstract class StringOps {
  /**
   * Takes theString fullString and replaces all instances of toReplace with
   * replacement
   */
  public static String replace (String fullString, String toReplace, String replacement) {
    int index = 0;
    int pos;
    int fullStringLength = fullString.length();
    int toReplaceLength = toReplace.length();
    if (toReplaceLength > 0) {
      int replacementLength = replacement.length();
      StringBuilder buff;
      while (index < fullStringLength && 
             ((pos = fullString.indexOf(toReplace, index)) >= 0)) {      
        buff = new StringBuilder(fullString.substring(0, pos));
        buff.append(replacement);
        buff.append(fullString.substring(pos + toReplaceLength, fullStringLength));
        index = pos + replacementLength;
        fullString = buff.toString();
        fullStringLength = fullString.length();
      }
    }
    return fullString;
  }
  
  /**
   * Converts the given string to a valid Java string literal.
   * All back slashes, quotes, new-lines, and tabs are converted
   * to their escap character form, and the sourounding quotes 
   * are added.
   * @param s the normal string to turn into a string literal
   * @return the valid Java string literal
   */
  public static String convertToLiteral(String s) {
    String output = s;
    output = replace(output, "\\", "\\\\"); // convert \ to \\
    output = replace(output, "\"", "\\\""); // convert " to \"
    output = replace(output, "\t", "\\t");  // convert [tab] to \t
    output = replace(output, "\n", "\\n");  // convert [newline] to \n
    return "\"" + output + "\"";
  }
  
  /**
   * Verifies that (startRow, startCol) occurs before (endRow, endCol).
   * @throws IllegalArgumentException if end is before start
   */
  private static void _ensureStartBeforeEnd(int startRow, int startCol,
                                            int endRow, int endCol) {
    if (startRow > endRow) {
      throw new IllegalArgumentException("end row before start row: " +
                                         startRow + " > " + endRow);
    }
    else if (startRow == endRow && startCol > endCol) {
      throw new IllegalArgumentException("end before start: (" +
                                         startRow + ", " + startCol +
                                         ") > (" + endRow + ", " + endCol + ")");
    }
  }

  /**
   * Verifies that the given column position is within the row at rowStartIndex
   * in the given String.
   * @param fullString the string in which to check the column
   * @param col the column index that should be within the row
   * @param rowStartIndex the first index of the row within fullString that col should be in
   * @throws IllegalArgumentException if col is after the end of the given row
   */
  private static void _ensureColInRow(String fullString, int col, int rowStartIndex) {
    int endOfLine = fullString.indexOf("\n",rowStartIndex);
    if (endOfLine == -1) {
      endOfLine = fullString.length();
    }
    if (col > (endOfLine - rowStartIndex)) {
      throw new IllegalArgumentException("the given column is past the end of its row");
    }
  }

  /**
   * Gets the offset and length equivalent to the given pairs start and end row-col.
   * @param fullString the string in which to compute the offset/length
   * @param startRow the row on which the error starts, starting at one for the first row
   * @param startCol the col on which the error starts, starting at one for the first column
   * @param endRow the row on which the error ends.  Equals the startRow for one-line errors
   * @param endCol the character position on which the error ends.
   *               Equals the startCol for one-character errors
   * @return a Pair of which the first is the offset, the second is the length
   */
  public static Pair<Integer,Integer> getOffsetAndLength(String fullString, int startRow,
                                                         int startCol, int endRow, int endCol) {
    _ensureStartBeforeEnd(startRow, startCol, endRow, endCol);

    // find the offset
    int currentChar = 0;
    int linesSeen = 1;
    while (startRow > linesSeen) {
      currentChar = fullString.indexOf("\n",currentChar);
      if (currentChar == -1) {
        throw new IllegalArgumentException("startRow is beyond the end of the string");
      }
      // Must move past the newline
      currentChar++;
      linesSeen++;
    }
    
    _ensureColInRow(fullString, startCol, currentChar);
    int offset = currentChar + startCol - 1;  // offset is zero-based

    // find the length
    while (endRow > linesSeen) {
      currentChar = fullString.indexOf("\n",currentChar);
      if (currentChar == -1) {
        throw new IllegalArgumentException("endRow is beyond the end of the string");
      }
      currentChar++;
      linesSeen++;
    }

    _ensureColInRow(fullString, endCol, currentChar);
    int length = currentChar + endCol - offset;

    // ensure the length is in bounds
    if (offset + length > fullString.length()) {
      throw new IllegalArgumentException("Given positions beyond the end of the string");
    }
    return new Pair<Integer,Integer>(new Integer(offset), new Integer(length));
  }

  /**
   * Gets the stack trace of the given Throwable as a String.
   * @param t the throwable object for which to get the stack trace
   * @return the stack trace of the given Throwable
   */
  public static String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }
  
  /**
   * Gets the stack trace of the current code. Does not include this method.
   * @return the stack trace for the current code
   */
  public static String getStackTrace() {
    try { throw new Exception(); } // Thread.getStackTrace() might be more efficient, but is new in Java 5.0
    catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      StackTraceElement[] stes = e.getStackTrace();
      int skip = 1;
      for(StackTraceElement ste: stes) {
        if (skip>0) { --skip; } else { pw.print("at "); pw.println(ste); }
      }
      return sw.toString();
    }
  }
  
  /**
   * Character.isDigit answers <tt>true</tt> to some non-ascii
   * digits.  This one does not.
   */
  public static boolean isAsciiDigit(char c) {
    return '0' <= c && c <= '9';
  }
  
  /**
   * Returns true if the class is an anonymous inner class.
   * This works just like Class.isAnonymousClass() in Java 5.0 but is not version-specific.
   * @param c class to check
   * @return true if anonymous inner class
   */
  public static boolean isAnonymousClass(Class c) {
    String simpleName = c.getName();
    int idx = simpleName.lastIndexOf('$');
    if (idx >= 0) {
      // see if we have just numbers after the $
      for (int pos=idx+1; pos < simpleName.length(); ++pos) {
        if (!isAsciiDigit(simpleName.charAt(pos))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  /**
   * Returns true if the class is a member class.
   * This works just like Class.isMemberClass() in Java 5.0 but is not version-specific.
   * @param c class to check
   * @return true if member class
   */
  public static boolean isMemberClass(Class c) {
    String simpleName = c.getName();
    int idx = simpleName.lastIndexOf('$');
    if (idx == -1) {
      return false;
    }
    return !isAnonymousClass(c);
  }
  
  /**
   * Returns the simple class name.
   * This works just like Class.getSimpleName() in Java 5.0 but is not version-specific.
   * @param c class for which to get the simple name
   * @return simple name
   */
  public static String getSimpleName(Class c) {
    if (c.isArray())
      return getSimpleName(c.getComponentType())+"[]";

    if (isAnonymousClass(c)) {
      return "";
    }
    
    String simpleName = c.getName();
    int idx = Math.max(simpleName.lastIndexOf('.'), 
                       simpleName.lastIndexOf('$'));
    return simpleName.substring(idx + 1); // strip the package name
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(long[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(int[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(short[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(char[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(byte[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(boolean[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(float[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(double[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    buf.append('[');
    buf.append(a[0]);
    
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
    
    buf.append("]");
    return buf.toString();
  }
  
  /**
   * This works just like java.util.Arrays.toString in Java 5.0 but is not version-specific.
   */
  public static String toString(Object[] a) {
    if (a == null)
      return "null";
    if (a.length == 0)
      return "[]";
    
    final StringBuilder buf = new StringBuilder();
    
    for (int i = 0; i < a.length; i++) {
      if (i == 0)
        buf.append('[');
      else
        buf.append(", ");
      
      buf.append(String.valueOf(a[i]));
    }
    
    buf.append("]");
    return buf.toString();
  }

  /**
   * Encode &, <, > and newlines as HTML entities.
   * @param s string to encode
   * @return encoded string
   */
  public static String encodeHTML(String s) {
    s = StringOps.replace(s, "&", "&amp;");
    s = StringOps.replace(s, "<", "&lt;");
    s = StringOps.replace(s, ">", "&gt;");
    s = StringOps.replace(s, System.getProperty("line.separator"),"<br>");
    s = StringOps.replace(s, "\n","<br>");
    return s;
  }

  /**
   * Return a string representing the approximate amount of memory specified in bytes.
   * @param l memory in bytes
   * @return string approximating the amount of memory
   */
  public static String memSizeToString(long l) {
    String[] sizes = new String[] { "byte", "kilobyte", "megabyte", "gigabyte" };
    double d = l;
    int i = 0;
    while((d >= 1024) && (i<sizes.length)) {
      ++i;
      d /= 1024;
    }
    if (i>=sizes.length) { i = sizes.length-1; }
    StringBuilder sb = new StringBuilder();
    long whole = (long)d;
    if (whole==d) {
      if (whole==1) {
        sb.append(whole);
        sb.append(' ');
        sb.append(sizes[i]);
      }
      else {
        sb.append(whole);
        sb.append(' ');
        sb.append(sizes[i]);
        sb.append('s');
      }
    }
    else {
      // two decimal digits
      DecimalFormat df = new DecimalFormat("#.00");
      sb.append(df.format(d));
      sb.append(' ');
      sb.append(sizes[i]);
      sb.append('s');
    }
    return sb.toString();
  }
}
