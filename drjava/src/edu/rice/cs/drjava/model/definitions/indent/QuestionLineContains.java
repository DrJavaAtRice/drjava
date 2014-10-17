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

/**
 * Question rule in the indentation decision tree.  Determines if the 
 * current line contains the given character. Does not check
 * for the character inside comments or quotes.
 * <p>
 * <b>Does not work if character being searched for is a '/' or a '*'</b>
 *
 * @version $Id: QuestionLineContains.java 5668 2012-08-15 04:58:30Z rcartwright $
 */
public class QuestionLineContains extends IndentRuleQuestion {
  /** Character to search for
   */
  private char _findChar;
  
  /** Constructs a new rule which determines if the line
   * contains the given character.
   * @param findChar Character to search for
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionLineContains(char findChar, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _findChar = findChar;
  }
  
  /** Determines if the given character exists on the current line.
   * Does not search in quotes or comments.
   * <p>
   * <b>Does not work if character being searched for is a '/' or a '*'</b>
   * @param doc AbstractDJDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    int charPos = doc.findCharOnLine(doc.getCurrentLocation(), _findChar);
    if (charPos == -1) {
      return false;
    } else {
      return true;
    }
  }
}

