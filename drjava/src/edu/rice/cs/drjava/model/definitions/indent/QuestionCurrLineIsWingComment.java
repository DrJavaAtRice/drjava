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


/** Determines whether or not the current line in the document starts with "//" 
  * @version $Id$
  */
public class QuestionCurrLineIsWingComment extends IndentRuleQuestion {
  
  /** @param yesRule The decision subtree for the case that this rule applies in the current context.
    * @param noRule  The decision subtree for the case that this rule does not apply in the current context.
    */
  public QuestionCurrLineIsWingComment(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
  
  /** Determines whether or not the current line in the document starts with the wing comment prefix "//". Does NOT
    * check if this line is embedded in a block comment.  Only runs in event thread.
    * @param doc The AbstractDJDocument containing the current line.
    * @param reason The reason that the indentation is being done
    * @return true iff the current line is a wing comment.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    try {
      // Find the first non-whitespace character on the current line.
      
      int currentPos = doc.getCurrentLocation();
      int startPos   = doc._getLineStartPos(currentPos);
      int maxPos     = doc.getLength();
      int diff       = maxPos - startPos;
      
      if (diff < 2) return false;
      
      String text = doc.getText(startPos, 2);
      
      return text.equals("//");
    }
    catch (BadLocationException e) { throw new UnexpectedException(); }
  }
}