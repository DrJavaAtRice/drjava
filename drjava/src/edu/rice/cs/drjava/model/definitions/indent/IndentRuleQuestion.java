/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;

/**
 * A question node in the decision tree for the indentation system.
 * Calls to <code>indentLine</code> on an IndentRuleQuestion will
 * make a decision based on context and call the same method on
 * one of its children.  The leaves of the tree are represented
 * by IndentRuleAction objects.
 * @version $Id$
 */
public abstract class IndentRuleQuestion extends IndentRuleWithTrace {
  /**
   * Node in decision tree to use if the rule holds in this context.
   */
  private final IndentRule _yesRule;

  /**
   * Node in decision tree to use if the rule does not hold in this context.
   */
  private final IndentRule _noRule;

  /**
   * Constructs a new Question indent rule using the two given children.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public IndentRuleQuestion(final IndentRule yesRule, final IndentRule noRule) {
    _yesRule = yesRule;
    _noRule = noRule;
  }

  /**
   * Determines if the given rule holds in this context.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param reason - The reason that indentation was initiated, specified in Indenter
   * @return true if this node's rule holds.
   */
  abstract boolean applyRule(DefinitionsDocument doc, int reason);

  /**
   * Determines if the given rule holds in this context.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param pos Position within line to be indented.
   * @param reason - The reason that indentation was initiated, specified in Indenter
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc, int pos, int reason) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    boolean result = applyRule(doc, reason);
    if (oldPos > doc.getLength()) {
      oldPos = doc.getLength();
    }
    doc.setCurrentLocation(oldPos);
    return result;
  }

  /**
   * Determines if the given rule holds in this context and calls
   * the same method on one of its child nodes.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param reason - The reason that indentation was initiated, specified in Indenter
   * @return true if the caller should update the current location itself,
   * false if the indenter has already handled this
   */
  public boolean indentLine(DefinitionsDocument doc, int reason)
  {
    if (applyRule(doc, reason)) {
      _addToIndentTrace(getRuleName(), YES, false);
      return _yesRule.indentLine(doc, reason);
    }
    else {
      _addToIndentTrace(getRuleName(), NO, false);
      return _noRule.indentLine(doc, reason);
    }
  }

}





