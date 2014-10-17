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

import javax.swing.text.BadLocationException;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.drjava.model.AbstractDJDocument.ERROR_INDEX;

/** Determines whether or not the closest non-whitespace character preceding the start of the current line (excluding 
  * any characters inside comments or strings) is on the same line as the enclosing brace (curly or paren).
  *
  * @version $Id: QuestionStartAfterOpenBrace.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
public class QuestionStartAfterOpenBrace extends IndentRuleQuestion {
  /** @param yesRule The decision subtree for the case that this rule applies in the current context.
    * @param noRule The decision subtree for the case that this rule does not apply in the current context.
    */
  public QuestionStartAfterOpenBrace(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
  
  /** Only runs in the event thread.
    * @param doc The AbstractDJDocument containing the current line.
    * @return True the closest non-whitespace character before the start of the current line (excluding any 
    * characters inside comments or strings) is an open brace.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason)  {
    int origin = doc.getCurrentLocation();
    int lineStart = doc._getLineStartPos(origin);
    
//    System.err.println("***** QSAOB.applyRule called with origin = " + origin + " and lineStart = " + lineStart);
//    System.err.println("      Current line = '" + doc._getCurrentLine() + "'");
    
    if (lineStart <= 1) return false;  // lineStart < 1 => No preceding line exists!
    
    try {  // must restore current location
      
      // Get brace for start of line;
      int bracePos = doc.findEnclosingScalaBracePosWithEquals(lineStart);
      
//      System.err.println("[QSAOB] Enclosing brace pos = " + bracePos);
      
      if (bracePos == ERROR_INDEX) return false;
      
//      System.err.println("[QSAOB] Enclosing brace is '" + doc._getText(bracePos, 1).charAt(0) + "'");
      
    // Get brace's end of line
      int braceEndLinePos = doc._getLineEndPos(bracePos);
      
      // Get position of next non-WS char (not in comments)
      int nextNonWS = doc.getFirstNonWSCharPos(braceEndLinePos + 1);  // 
//      System.err.println("[QSAOB] Next NonWS pos after brace = " + nextNonWS);
      
      // return true if no NonWS character appears between brace and beginning of curr line
      boolean result = nextNonWS == ERROR_INDEX || nextNonWS >= lineStart;
//      System.err.println("QSAOB.applyRule returning " + result);

      return result;  
    }
    catch (BadLocationException e) { return false; } 
  }
}
