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

/** Question rule in the indentation decision tree.  Determines if the current line starts with the specified string.
  * @version $Id$
  */
public class QuestionCurrLineStartsWith extends IndentRuleQuestion {
  private volatile String _prefix;
  private volatile boolean _acceptComments;
  
  /** Constructs a new rule for the given prefix string. Does not look inside comments.
    * @param prefix String to search for
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    */
  public QuestionCurrLineStartsWith(String prefix, boolean acceptComments, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _prefix = prefix;
    _acceptComments = acceptComments;
  }
  
  /** Convenience constructor for case wlineStart acceptComments is true */
  public QuestionCurrLineStartsWith(String prefix, IndentRule yesRule, IndentRule noRule) {
    this(prefix, true, yesRule, noRule);
  }
  /** Determines if the current line in the document starts with the specified prefix, ignoring whitespace.
    * If the prefix is null or empty, returns true.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is being done
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    if (_prefix == null) return true;
    int len = _prefix.length();
    if (len == 0) return true;
    
    try {
      // Find start of line
      int lineStart = doc._getLineStartPos();
      int firstCharPos, lineEndPos;
      
      if (_acceptComments) {
        firstCharPos = doc._getLineFirstCharPos(lineStart);
        lineEndPos = doc._getLineEndPos(lineStart);
      }
      else {
        firstCharPos = doc.getFirstNonWSCharPos(lineStart);
        lineEndPos = doc._getLineEndPos(firstCharPos);
      }
      
      // If prefix would run off the end of the line, the answer is obvious.
      if (firstCharPos + len > lineEndPos) {
        return false;
      }
      
      // Compare prefix
      String actualPrefix = doc.getText(firstCharPos, len);
      return _prefix.equals(actualPrefix);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }  // Shouldn't happen
  }
}
