/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.BadLocationException;

/** * Tests the question rule which determines if the current line
 * in the document contains the given character.
 * <p>
 * All tests check for the ':' character on the current line.
 *
 * @version $Id$
 */
public final class QuestionLineContainsTest extends IndentRulesTestCase {

  /** Ensures that a line containing a colon is detected.
   * Tests that a line of text containing a colon is detected.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testLineContainsColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // Colon in text
    _setDocText("return test ? x : y;\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("colon in text (after startdoc)",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in text (after newline)",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(25);
    assertTrue("colon in text (after colon on line)",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }    
  
  /** Ensures that a line containing a colon is detected.
   * Tests that a line does not contain a colon.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testLineDoesNotContainColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
    
    // No colon in text
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(6);
    assertTrue("no colon", !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(28);
    assertTrue("line of close brace (no colon in text)", !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a line containing a colon is detected.
   * Tests that a line containing a commented out colon is identified as a
   * line that does not contain a colon.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testLineDoesNotContainColonDueToComments() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // No colon, single line comment
    _setDocText("//case 1:\nreturn test; //? x : y\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("entire line with colon in comment (no colon, single line comment)",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(10);
    assertTrue("part of line with colon in comment (no colon, single line comment)",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // No colon, multi-line comment
    _setDocText("foo();\nreturn test; /*? x : y*/\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in multi-line comment", !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a line containing a colon is detected.
   * Tests that a line containing a colon in quotes is identified as a
   * line that does not contain a colon.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testLineDoesNotContainColonDueToQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
  
    // No colon, quotes
    _setDocText("foo();\nreturn \"total: \" + sum\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in quotes", !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }
}
