/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *  
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.sexp;

public class TextAtom implements Atom {
  protected String _text;
  
  public TextAtom(String text) { _text = text; }
  
  public String getText() { return _text; }
  
  /**
   * Visitor hook for the TextAtom
   * @param the visitor
   * @return result of the given algorithm
   */
  public <Ret> Ret accept(SExpVisitor<Ret> v){
    return v.forTextAtom(this);
  }
  
  /**
   * If the given text was a quoted string, the text returned
   * excludes the quotes around the string.
   * @return the text that went into making this atom.
   */
  public String toString() { return _text; }  
}

/**
 * this type of text atom is mostly like its super class
 * except its string representation includes the sourrounding 
 * quotes and the instances of the characters: \ " etc are turned
 * into their corresponding escape character sequences.
 */
class QuotedTextAtom extends TextAtom {
  
  public QuotedTextAtom(String text) { super(text); }
  
  public String toString() { 
    String output = _text;
    output = replaceAll(output, "\\", "\\\\"); // convert \ to \\
    output = replaceAll(output, "\"", "\\\""); // convert " to \"
    output = replaceAll(output, "\t", "\\t");  // convert [tab] to \t
    output = replaceAll(output, "\n", "\\n");  // convert [newline] to \n
    return "\"" + output + "\"";
  }
  
  /**
   * replaces all occurrences of the given a string with a new string.
   * This method was reproduced here to remove any dependencies on the
   * java v1.4 api.
   * @param str the string in which the replacements should occur
   * @param toReplace the substring to replace
   * @param replacement the substring to put in its place
   * @return the new changed string
   */
  private static String replaceAll(String str, String toReplace, String replacement) {
    String result = str;
    int i = result.indexOf(toReplace); 
    while (i >= 0) {
      result = result.substring(0,i) + replacement + result.substring(i+1);
      i = result.indexOf(toReplace, i + replacement.length());
    }
    return result;
  }
}