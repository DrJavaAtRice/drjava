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
    
    if ((!info.braceType.equals(IndentInfo.openSquiggly)) ||
        (info.distToBrace < 0)) {
      // Precondition not met: we should have a brace
      return false;
    }
    int bracePos = lineStart - info.distToBrace;
    
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
