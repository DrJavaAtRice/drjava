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

/** Determines if the given search character is found between the start of the current statement and the end character.
 *  Accomplishes this by searching backwards from the end character, for the search character until one of the 
 *  following characters is found: '}', '{', ';', DOCSTART.
 *  <b>The given end character must exist on the current line and not be part of a quote or comment.</b> If there is 
 *  more than end character on the given line, then the first end character is used.
 *  <p>This question is useful for determining if, when a colon is found on a line, it is part of a ternary operator 
 *  or not (construct this question with '?' for search character and ':' for end character).
 *  <p>It can also be used to determine if a statement contains a particular character by constructing it with the 
 *  desired character as a search character and the end character as ';'.
 *  <p>Note that characters in comments and quotes are disregarded. 
 *
 * @version $Id$
 */
public class QuestionExistsCharInStmt extends IndentRuleQuestion {
  /**
   * The character to search for
   */
  private char _findChar;
  
  /**
   * The character which marks the end of the search
   * space. i.e. search from the start of the statment
   * to the end char.
   */
  private char _endChar;

  /**
   * Constructs a rule to determine if findChar exists
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
   *  comments and quotes. Start of the statement is the point right after when one of the following characters 
   *  is found: ';', '{', '}', DOCSTART.
   *
   *  @param doc AbstractDJDocument containing the line to be indented.
   *  @return true if this node's rule holds.
   */
  boolean applyRule(AbstractDJDocument doc, int reason) {

   // Find the position of endChar on the current line
    int endCharPos = doc.findCharOnLine(doc.getCurrentLocation(), _endChar);
    return doc.findCharInStmtBeforePos(_findChar, endCharPos);
  }
}
