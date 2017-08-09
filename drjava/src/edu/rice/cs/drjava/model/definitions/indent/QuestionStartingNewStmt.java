/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/** * Determines if the current line is starting a new statement by
 * searching backwards to see if the previous line was the end
 * of a statement. Specifically,  checks if the previous
 * non-whitespace character not on this line is one of the
 * following: ';', '{', '}', or 0.
 * <p>
 * Note that characters in comments and quotes are disregarded. 
 *
 * @version $Id$
 */
public class QuestionStartingNewStmt extends IndentRuleQuestion {
  
  /** Constructs a new rule to determine if the current line is the start of a new statement.
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public QuestionStartingNewStmt(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
 
  /** Determines if the previous non-whitespace character not on this line was one of the following: ';', '{', '}' 
    * or nothing.  Ignores characters in quotes and comments.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    
    char[] delims = {';', '{', '}'};
    int lineStart = doc._getLineStartPos(doc.getCurrentLocation());
    int prevDelimiterPos;
    
    try {
      prevDelimiterPos = doc.findPrevDelimiter(lineStart, delims);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    
    // If no previous delimited exists, imaginary delimiter at position -1
    
    // Delimiter must be at the end of its line (ignoring whitespace & comments)
    int firstNonWSAfterDelimiter;
    try {
      firstNonWSAfterDelimiter = doc.getFirstNonWSCharPos(prevDelimiterPos + 1);
      // will return ERROR_INDEX (-1) if we hit the end of the document
    } 
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // If the first non-WS character is after the beginning of the line
    // or we reached the end of the document, then we are starting a new statement.
    return (firstNonWSAfterDelimiter >= lineStart || firstNonWSAfterDelimiter == -1);
  }
}

