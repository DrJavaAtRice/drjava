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

import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;


/** Determines whether or not the current line in the document starts with "//" 
 *  @version $Id$
 */
public class QuestionCurrLineIsWingComment extends IndentRuleQuestion {
  
  /** @param yesRule The decision subtree for the case that this rule applies in the current context.
   *  @param noRule The decision subtree for the case that this rule does not apply in the current context.
   */
  public QuestionCurrLineIsWingComment(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
  
  /** Determines whether or not the current line in the document starts with the wing comment prefix "//".
   *  Does not test to determine if this line is embedded in a block comment.  
   *  @param doc The AbstractDJDocument containing the current line.
   *  @param reason The reason that the indentation is being done
   *  @return true iff the current line is a wing comment.
   *  @pre current line is not embedded in a block comment
   */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    try {
      // Find the first non-whitespace character on the current line.
      
      int currentPos = doc.getCurrentLocation();
      int startPos   = doc.getLineStartPos(currentPos);
      int maxPos     = doc.getLength();
      int diff       = maxPos - startPos;
      
      if (diff < 2) return false;
      
      String text = doc.getText(startPos, 2);
      
      return text.equals("//");
    }
    catch (BadLocationException e) { throw new UnexpectedException();
    }
  }
  
}