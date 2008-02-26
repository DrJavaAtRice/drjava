/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import javax.swing.text.BadLocationException;


/**
 * Determines whether or not the last '{' was immediately preceded 
 * by _prefix So when _prefix='=', effectivily, we are looking for "={"
 * This questions corresponds to rule 22 in our decision tree.
 * @version $Id$
 */
public class QuestionHasCharPrecedingOpenBrace extends IndentRuleQuestion {
  private char[] _prefix;

  /**
   * @param yesRule The decision subtree for the case that this rule applies
   * in the current context.
   * @param noRule The decision subtree for the case that this rule does not
   * apply in the current context.
   */
  public QuestionHasCharPrecedingOpenBrace(char[] prefix, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /** @param doc The AbstractDJDocument containing the current line.
   *  @return true iff the last block/expression-list opened before the start of the current line begins with '{'. 
   */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    // PRE: We are inside a {.
    
    int origin = doc.getCurrentLocation();
    int lineStart = doc.getLineStartPos(origin);
    
    // Get brace for start of line
    doc.move(lineStart - origin);
    IndentInfo info = doc.getIndentInformation();
    doc.move(origin - lineStart);
    
    if (! info.lineEnclosingBraceType.equals(IndentInfo.OPEN_CURLY) || info.distToLineEnclosingBrace < 0) {
      // Precondition not met: we should have a brace
      return false;
    }
    int bracePos = lineStart - info.distToLineEnclosingBrace;
    
    // Get position of previous non-WS char (not in comments)
    int prevNonWS = -1;
    try {
      prevNonWS = doc.findPrevNonWSCharPos(bracePos);
      char c = doc.getText(prevNonWS,1).charAt(0);
      for (char pchar: _prefix) if (c == pchar) return true;
    }
    catch (BadLocationException e) {
    }    
    return false;
  }
}
