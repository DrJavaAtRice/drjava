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

import javax.swing.text.*;

import edu.rice.cs.drjava.model.AbstractDJDocument;

import edu.rice.cs.util.UnexpectedException;

/** @version $Id$
  * Asks whether the current line is empty (contains only whitespace), or indentation was
  * spawned by pressing enter.
  * 
  * TODO: eliminate duplicated code with QuestionCurrentLineEmpty
  */
class QuestionCurrLineEmptyOrEnterPress extends IndentRuleQuestion {
  
  QuestionCurrLineEmptyOrEnterPress(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }

  /** Determines if the current line in the document is empty.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is being done
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    if (reason == Indenter.IndentReason.ENTER_KEY_PRESS) return true;
    try {
      // Determine if there are only whitespace chars by seeing if the first non-WS char is the endOfLine
      int here = doc.getCurrentLocation();
      int endOfLine = doc.getLineEndPos(here);
      int firstNonWS = doc.getLineFirstCharPos(here);
      return (endOfLine == firstNonWS);
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
