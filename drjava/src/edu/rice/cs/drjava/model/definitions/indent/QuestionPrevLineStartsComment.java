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

/** Given the start of the current line is inside a block comment, asks
  * whether the comment begins on the "previous line," ignoring white space.
  * 
  * @version $Id$
  */
class QuestionPrevLineStartsComment extends IndentRuleQuestion {
  
  QuestionPrevLineStartsComment(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
  
  /** Determines if the previous line in the document starts a block comment.
    * We know that the current line is in a block comment. Therefore, if the
    * start of the previous line is not inside of a block comment, then the
    * previous line must have started the comment. 
    * <p>
    * There is an exception to this; however, it is handled adequately. Consider
    * the case when the previous line contains the following code:
    * <code>*&#47; bar(); &#47;*</code>
    * <p>
    * Our approach will say that since the beginning of the previous line is
    * inside of a comment, the previous line did not start the comment. This
    * is acceptable because we think of the previous line as a continuation
    * of a larger commented out region.
    * @param  doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {

      int cursor;

    // Move back to start of current line
    cursor = doc.getLineStartPos(doc.getCurrentLocation());
    
    /* If the start of the current line is the start of the document, there was no previous line and so this line must 
     * have started the comment. */
    if (cursor == AbstractDJDocument.DOCSTART) return false;
    
    // Move the cursor to the previous line
    cursor = cursor - 1;
    
    // Move it to the start of the previous line
    cursor = doc.getLineStartPos(cursor);
    
    // Return if the start of the previous line is in a comment.
//    BraceReduction reduced = doc.getReduced();
    doc.resetReducedModelLocation();
    ReducedModelState state = doc.stateAtRelLocation(cursor - doc.getCurrentLocation());
    return !state.equals(ReducedModelStates.INSIDE_BLOCK_COMMENT);
  }
}

