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
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import static edu.rice.cs.drjava.model.AbstractDJDocument.ERROR_INDEX;

/** Question rule in the indentation decision tree.  Determines if the 
  * line previous to the current position starts with the specified character.
  * @version $Id: QuestionPrevLineStartsWith.java 3490 2006-01-07 19:22:33Z dlsmith $
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
  boolean applyRule(AbstractDJDocument doc, int reason) {

    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc.getLineStartPos(here);
      
      if (startLine <= AbstractDJDocument.DOCSTART) return false;  // on first line
      
      // Find start of previous line
      int endPrevLine = startLine - 1;
      int startPrevLine = doc.getLineStartPos(endPrevLine);
      int firstChar = doc.getLineFirstCharPos(startPrevLine);
      
      // Compare prefix
      String actualPrefix = doc.getText(firstChar, 3);
      if (! actualPrefix.equals("/**")) return false;
      int nextNonWSChar = doc.getFirstNonWSCharPos(firstChar + 3, true);
      return nextNonWSChar != ERROR_INDEX && nextNonWSChar <= endPrevLine;     
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
