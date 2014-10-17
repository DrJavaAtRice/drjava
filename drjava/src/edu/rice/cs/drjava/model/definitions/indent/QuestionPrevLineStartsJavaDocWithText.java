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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Question rule in the indentation decision tree.  Determines if the 
  * line previous to the current position starts with the specified character.
  * @version $Id: QuestionPrevLineStartsJavaDocWithText.java 5668 2012-08-15 04:58:30Z rcartwright $
  */
public class QuestionPrevLineStartsJavaDocWithText extends IndentRuleQuestion {
  
  /** Constructs a rule that looks for "/**" as first non-whitespace followed by some additional non-whitespace text
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public QuestionPrevLineStartsJavaDocWithText(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
  
  /** Determines if the previous line starts with "/**", ignoring whitespace, followed by more non-whitespace text
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc._getLineStartPos(here);
      
      if (startLine <= 0) return false;  // on first line
      
      // Find start of previous line
      int endPrevLine = startLine - 1;
      int startPrevLine = doc._getLineStartPos(endPrevLine);
      int firstChar = doc._getLineFirstCharPos(startPrevLine);
      
      // Compare prefix
      String actualPrefix = doc.getText(firstChar, 3);
      if (! actualPrefix.equals("/**")) return false;
      int nextNonWSChar = doc.getFirstNonWSCharPos(firstChar + 3, true);
      return nextNonWSChar != -1 && nextNonWSChar <= endPrevLine;     
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
