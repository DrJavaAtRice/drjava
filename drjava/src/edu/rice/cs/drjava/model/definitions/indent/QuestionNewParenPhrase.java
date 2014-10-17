/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2014, JavaPLT group at Rice University (drjava@rice.edu)
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
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import java.util.Arrays;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.util.swing.Utilities;

/** Question rule in the indentation decision tree.  Determines if the current line starts a new "phrase" within a 
  * parenthesized expression.  Specifically, this rule determines if the previous line ends in a comma, semicolon, 
  * open paren, or open bracket.  Note that whitespace, blank lines, and comments are disregarded.
  * 
 * @version $Id: QuestionNewParenPhrase.java 5611 2012-07-25 15:03:33Z rcartwright $
 */
public class QuestionNewParenPhrase extends IndentRuleQuestion {
  
  /* Array of delimiters; must be sorted!  This sort is performed in the constructor. */
  
  public static final char[] LOCAL_DELIMS =
    {'%', '&', '(', '*', '+', ',', '-', '/', ';', '<', '=', '>', '[', '|', '}'};  /* Sorted! */

  /** Constructs a new rule to determine if the current line starts new paren phrase.
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public QuestionNewParenPhrase(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
 
  /** Determines if the previous line ends in a comma, semicolon,
    * open paren, open bracket, operator, or comparator.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {

    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc._getLineStartPos(here);
//      System.err.println("QuestionNewParenPhrase.applyRule called. here = " + here + " startLine = " + startLine);
      
      if (startLine > 0) {
        // Find previous delimiter (looking in paren phrases)
        int prevDelim = doc.findPrevDelimiter(startLine, LOCAL_DELIMS, false);
        if (prevDelim == -1) {
          return false;
        }
        
        // Make sure the delim is the previous non-WS char
        int nextNonWS = doc.getFirstNonWSCharPos(prevDelim + 1);
        if (nextNonWS == -1) {
          nextNonWS = startLine;
        }
        boolean result = nextNonWS >= startLine;
//        if (! result) Utilities.show("QuestionNewParenPhrase is returning false at pos " + here);
        return result;
      }
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
    // On first line
    return false;
  }
}
