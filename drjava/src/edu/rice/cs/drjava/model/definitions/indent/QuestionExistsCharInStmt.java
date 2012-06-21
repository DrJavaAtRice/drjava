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

/** Determines if the given search character is found between the start of the current statement and the end character.
  * Accomplishes this by searching backwards from the end character, for the search character until one of the 
  * following characters is found: '}', '{', ';', 0.
  * <b>The given end character must exist on the current line and not be part of a quote or comment.</b> If there is 
  * more than end character on the given line, then the first end character is used.
  * <p>This question is useful for determining if, when a colon is found on a line, it is part of a ternary operator 
  * or not (construct this question with '?' for search character and ':' for end character).
  * <p>It can also be used to determine if a statement contains a particular character by constructing it with the 
  * desired character as a search character and the end character as ';'.
  * <p>Note that characters in comments and quotes are disregarded. 
  *
  * @version $Id$
  */
public class QuestionExistsCharInStmt extends IndentRuleQuestion {
  /** The character to search for
   */
  private char _findChar;
  
  /** The character which marks the end of the search
   * space. i.e. search from the start of the statment
   * to the end char.
   */
  private char _endChar;
  
  /** Constructs a rule to determine if findChar exists
   * between the start of the current statement and endChar.
   *
   * @param findChar Character to search for from the start of the
   * statement to endChar
   * @param endChar Character that marks the end of the search space. Must
   * exist on the current line and not be in quotes or comments.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionExistsCharInStmt(char findChar, char endChar, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _findChar = findChar;
    _endChar = endChar;
  }
  
  /** Searches backwards from endChar to the start of the statement looking for findChar. Ignores characters in 
    * comments and quotes. Start of the statement is the point right after when one of the following characters 
    * is found: ';', '{', '}', 0.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    
    // Find the position of endChar on the current line
    int endCharPos = doc.findCharOnLine(doc.getCurrentLocation(), _endChar);
    return doc.findCharInStmtBeforePos(_findChar, endCharPos);
  }
}
