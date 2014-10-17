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

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/** Given the start of the current line is inside a block comment, asks whether the comment begins on the previous 
  * line.  Note tha the beginning of the block comment does not necessarily open the previous line.
  * 
  * @version $Id: QuestionPrevLineStartsComment.java 5668 2012-08-15 04:58:30Z rcartwright $
  */
class QuestionPrevLineStartsComment extends IndentRuleQuestion {
  
  QuestionPrevLineStartsComment(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
  
  /** Determines if the previous line in the document starts a block comment.  Assumes that the current line is
    * in the same block comment. Only runs in event thread. We know that the current line is in a block comment. 
    * Therefore, if the start of the previous line is not inside of a block comment, then the  previous line must
    * have started the comment. 
    * <p>
    * There is an exception to this reasoning, which is addressed. Consider
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

    int orgPos = doc.getCurrentLocation();

    // Move back to start of current line
    int pos = doc._getLineStartPos(orgPos);
    
    /* If the start of the current line is the start of the document, there was no previous line and so this line must 
     * have started the comment. */
    if (pos == 0) return false;
    
    /* Get the start of the previous line */
    pos = doc._getLineStartPos(pos-1);
    /* Return true iff the start of the prev line is not inside a block comment. */
    return ! doc.isShadowed(pos);
   
//    doc.setCurrentLocation(pos);
//    try {
//      return doc.getStateAtCurrent().equals(ReducedModelStates.INSIDE_BLOCK_COMMENT);
//    }
//    finally { doc.setCurrentLocation(orgPos); }
  }
}
 
