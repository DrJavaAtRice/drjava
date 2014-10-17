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
  * next non-whitespace character is a star '*'.  This is useful for determining
  * if we are at the beginning of a previously ill-formed block comment.
  * <p>
  * <b>Does not work if character being searched for is a '/' or a '*'</b>
  *
  * @version $Id: QuestionFollowedByStar.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class QuestionFollowedByStar extends IndentRuleQuestion {
  
  /** Constructs a new rule which determines if the current position is
    * immediately followed by a star '*'.
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public QuestionFollowedByStar(IndentRule yesRule, IndentRule noRule) { super(yesRule, noRule); }
  
  /** Determines if the next non WS character is '*'. Does not search in quotes or comments. <p>
    * <b>Does not work if character being searched for is a '/' or a '*'</b>
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    try {
      int charPos = doc.getFirstNonWSCharPos(doc.getCurrentLocation(), true);
      return (charPos != -1) && doc.getText(charPos, 1).equals("*");
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }
}

