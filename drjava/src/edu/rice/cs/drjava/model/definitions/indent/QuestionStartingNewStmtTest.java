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

import javax.swing.text.BadLocationException;

/** Tests the question rule which determines if the current line is starting a new statement.
  * @version $Id: QuestionStartingNewStmtTest.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
public final class QuestionStartingNewStmtTest extends IndentRulesTestCase {

  /** Ensures that the current line is the first line of a statement. This is done by testing if the previous 
    * character is one of several possibilities.
    * TODO: run all of these tests in the event-handling thread.
    */
  public void testStartOfStmtCheckForEndCharacters() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);

    // Starting new stmt, prev char docstart
    _setDocText("import java.util.Vector\n");
    _doc.setCurrentLocation(4);
    assertTrue("starting new stmt, prev char docstart",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, prev char '.' number
    _setDocText("val x = 4.\nbar()\nfoo\n");
    _doc.setCurrentLocation(11);
    assertTrue("starting new stmt, prev char '.' in number",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    // Adjust cursor so prev char is ')'
    _doc.setCurrentLocation(17);
    assertTrue("starting new stmt, prev char ')'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    
    // Starting new stmt, prev char '{'
    _setDocText("def foo() {\nfoo()\n");
    _doc.setCurrentLocation(12);
    assertTrue("starting new stmt, prev char '{'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, prev char '}'
    _setDocText("x()\n}\nfoo()\n");
    _doc.setCurrentLocation(6);
    assertTrue("starting new stmt, prev char '}'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }  

  /** Ensures that the current line is the first line of a statement.
   * Tests that whitespace, single-line and multi-line comments
   * are ignored when searching for the end-characters.
   */
  public void testStartOfStmtIgnoreWhiteSpaceAndCommentsInBetween() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);
  
    // Starting new stmt, ignore whitespace in between
    _setDocText("bar()\n\t   \n  foo()");
    _doc.setCurrentLocation(11);
    assertTrue("starting new stmt, ignore whitespace in between",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, ignore single line comments
    _setDocText("} // note:\n//please ignore me\nfoo()\n");
    _doc.setCurrentLocation(30);
    assertTrue("starting new stmt, ignore single line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, ignore multi-line comments
    _setDocText("{ /* in a comment\nstill in a comment\ndone */\nfoo()");
    _doc.setCurrentLocation(45);
    assertTrue("starting new stmt, ignore multi-line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    _setDocText("bar()\n/* blah + */ foo()\n");
    _doc.setCurrentLocation(17);
    assertTrue("starting new stmt, ignore multi-line comment on same " +
        "line as new stmt",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    _setDocText("method foo() {\n" +
  "}\n" +
  "     ");
    _doc.setCurrentLocation(17);
    assertTrue("Blank line with no non-WS after",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    _setDocText("method foo() {\n" +
  "}\n" +
  "     \n" +
  "// comment");
    _doc.setCurrentLocation(17);
    assertTrue("Blank line with comments after, but no non-WS",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that the current line is the first line of a statement.
   * Tests that end characters in single-line comments, multi-line
   * comments or quotes are ignored.
   */
  public void testNotStartOfStmtDueToEndCharactersInCommentsOrQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);

    // Not starting new stmt, ignore end chars in quotes
    _setDocText("x = bar + \";\" + \"}\" + \"{\" + \n\"{\" foo\n");
    _doc.setCurrentLocation(32);  // point at space before terminating 'foo'
    assertTrue("not starting new stmt, ignore end chars in quotes",
        ! rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Not starting new stmt, ignore end chars in single-line comments
    _setDocText("x = bar.//;{}\nfoo();\n");
    _doc.setCurrentLocation(14);
    assertTrue("not starting new stmt, ignore end chars in single-line comments",
        ! rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Not starting new stmt, ignore end chars in multi-line comments
    _setDocText("x = bar./*;\n{\n}\n*/\nfoo();\n");
    _doc.setCurrentLocation(19);
    assertTrue("not starting new stmt, ignore end chars in multi-line comments",
        ! rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }
}
