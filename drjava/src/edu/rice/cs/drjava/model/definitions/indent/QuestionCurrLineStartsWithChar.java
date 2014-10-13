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

package edu.rice.cs.drjava.model.definitions.indent;

import java.util.Arrays;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;
import static edu.rice.cs.drjava.model.AbstractDJDocument.*;

/** Determines whether the current line in the document starts with starts with a delimiter char specified by the 
  * sorted char[] _delimiters field, skipping over any comments and leading whitespace on that line. The character 
  * sequence is passed to the constructor of the class as a String argument.
  * @version
  */
public class QuestionCurrLineStartsWithChar extends IndentRuleQuestion {
  /** The String to be matched. This String may not contain whitespace characters or comment-delimiting characters. */
  private final char[] _delimiters;  // must be in sorted order
  private final boolean _acceptComments;
  
  /** @param yesRule The decision subtree for the case that this rule applies in the current context.
    * @param noRule The decision subtree for the case that this rule does not apply in the current context.
    */
  public QuestionCurrLineStartsWithChar(char[] delimiters, boolean acceptComments, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _delimiters = delimiters;
    _acceptComments = acceptComments;
  }
  
  public static QuestionCurrLineStartsWithChar newQuestionSkipComments(char[] delimiters, IndentRule yesRule, IndentRule noRule) {
    return new QuestionCurrLineStartsWithChar(delimiters, false, yesRule, noRule);
  }
  
  public static QuestionCurrLineStartsWithChar newQuestion(char[] delimiters, IndentRule yesRule, IndentRule noRule) {
    return new QuestionCurrLineStartsWithChar(delimiters, true, yesRule, noRule);
  }
  
  /** Determines whether or not the current line in the document starts with a delimiter char specified by the char[]
    * field _delimiters, skipping over any comments and leading whitespace on that line.  Will not match '/' or
    * default whitespace chars.  Only runs in event thread.
    * @param doc The AbstractDJDocument containing the current line.
    * @return True iff the current line in the document starts with the
    * character sequence specified by the String field _prefix.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    // Find the first non-whitespace character on the current line.
    int orig = doc.getCurrentLocation();
    if (orig == ERROR_INDEX) return false;
//    System.err.println("applyRule called ");
    int endPos = doc._getLineEndPos(orig);
//    System.err.print(" endPos = " + endPos);
    if (endPos == ERROR_INDEX)
//      System.err.println("Returning false because location is invalid");
      return false;
    
//    char prevChar = '\0';
    try {
      int firstNonWSCharPos = doc.getFirstNonWSCharPos(orig, false);  // skip over comments
//      System.err.print(" firstCharPos = " + firstNonWSCharPos);
      if (firstNonWSCharPos == ERROR_INDEX || firstNonWSCharPos >= endPos) return false;
      char firstNonWSChar = doc.getText(firstNonWSCharPos, 1).charAt(0);
//      System.err.print(" firstChar = '" + firstNonWSChar + "'");
//      System.err.println("");
      return Arrays.binarySearch(_delimiters, firstNonWSChar) >= 0; // non-negative only for _delimiters
    }
    catch(BadLocationException ble) { /* throw new UnexpectedException("Should never happen"); */ return false; }
  }
}
