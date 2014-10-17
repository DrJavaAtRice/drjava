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

/** A question node in the decision tree for the indentation system. Calls to <code>indentLine</code> on an 
  * IndentRuleQuestion will make a decision based on context and call the same method on one of its children.  
  * The leaves of the tree are represented by IndentRuleAction objects.
  * @version $Id: IndentRuleQuestion.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class IndentRuleQuestion extends IndentRuleWithTrace {
  /** Node in decision tree to use if the rule holds in this context. */
  private final IndentRule _yesRule;
  
  /** Node in decision tree to use if the rule does not hold in this context. */
  private final IndentRule _noRule;
  
  /** Constructs a new Question indent rule using the two given children.
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public IndentRuleQuestion(final IndentRule yesRule, final IndentRule noRule) {
    _yesRule = yesRule;
    _noRule = noRule;
  }
  
  /** Determines if the given rule holds in this context.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that indentation was initiated, specified in Indenter
    * @return true if this node's rule holds.
    */
  abstract boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason);
  
  /** Determines if the given rule holds in this context.
    * @param doc  The AbstractDJDocument containing the line to be indented.
    * @param pos  The Position within line to be indented.
    * @param reason  The reason that indentation was initiated, specified in Indenter
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, int pos, Indenter.IndentReason reason) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    boolean result = applyRule(doc, reason);
    if (oldPos > doc.getLength()) oldPos = doc.getLength();
    doc.setCurrentLocation(oldPos);
    return result;
  }
  
  /** Determines if the given rule holds in this context and calls the same method on one of its child nodes.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that indentation was initiated, specified in Indenter
    * @return true if the caller should update the current location itself, false if the indenter has already handled this
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    if (applyRule(doc, reason)) {
      _addToIndentTrace(getRuleName(), YES, false);
      return _yesRule.indentLine(doc, reason);
    }
    else {
      _addToIndentTrace(getRuleName(), NO, false);
      return _noRule.indentLine(doc, reason);
    }
  }
  
  /** Convenience method that formerly wrapped calls on applyRule in read locks. Only used in testing. */
  boolean testApplyRule(AbstractDJDocument doc, Indenter.IndentReason reason) { return applyRule(doc, reason); }
  
  /** Convenience method that formerly wrapped calls on applyRule in read locks. Only used in testing. */
  boolean testApplyRule(AbstractDJDocument doc, int pos, Indenter.IndentReason reason) {
    return applyRule(doc, pos, reason); 
  }
  
  /** Convenience method that formelry wrapped calls on indentLine in write locks. Only used in testing. */
  public boolean testIndentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    return indentLine(doc, reason); 
  }
}





