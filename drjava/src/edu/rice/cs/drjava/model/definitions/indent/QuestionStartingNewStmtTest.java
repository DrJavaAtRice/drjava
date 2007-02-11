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

import javax.swing.text.BadLocationException;

/**
 * Tests the question rule which determines if the current line
 * is starting a new statement.
 *
 * @version $Id$
 */
public final class QuestionStartingNewStmtTest extends IndentRulesTestCase {

  /**
   * Ensures that the current line is the first line of a statement.
   * This is done by testing if the previous character is one of
   * the following: docstart, ';', '{', '}'
   * These characters are here-on refered to as 'end-characters'.
   */
  public void testStartOfStmtCheckForEndCharacters() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);

    // Starting new stmt, prev char docstart
    _setDocText("import java.util.Vector;\n");
    _doc.setCurrentLocation(4);
    assertTrue("starting new stmt, prev char docstart",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, prev char ';'
    _setDocText("foo();\nbar();\n");
    _doc.setCurrentLocation(7);
    assertTrue("starting new stmt, prev char ';'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    
    // Starting new stmt, prev char '{'
    _setDocText("public void foo() {\nfoo()\n");
    _doc.setCurrentLocation(20);
    assertTrue("starting new stmt, prev char '{'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, prev char '}'
    _setDocText("x();\n}\nfoo()\n");
    _doc.setCurrentLocation(7);
    assertTrue("starting new stmt, prev char '}'",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }  

  /**
   * Ensures that the current line is the first line of a statement.
   * Tests that whitespace, single-line and multi-line comments
   * are ignored when searching for the end-characters.
   */
  public void testStartOfStmtIgnoreWhiteSpaceAndCommentsInBetween() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);
  
    // Starting new stmt, ignore whitespace in between
    _setDocText("bar();\n\t   \n  foo();");
    _doc.setCurrentLocation(12);
    assertTrue("starting new stmt, ignore whitespace in between",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, ignore single line comments
    _setDocText("} // note:\n//please ignore me\nfoo();\n");
    _doc.setCurrentLocation(30);
    assertTrue("starting new stmt, ignore single line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Starting new stmt, ignore multi-line comments
    _setDocText("{ /* in a comment\nstill in a comment\ndone */\nfoo();");
    _doc.setCurrentLocation(45);
    assertTrue("starting new stmt, ignore multi-line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    _setDocText("bar();\n/* blah */ foo();\n");
    _doc.setCurrentLocation(18);
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

  /**
   * Ensures that the current line is the first line of a statement.
   * Tests that end characters in single-line comments, multi-line
   * comments or quotes are ignored.
   */
  public void testNotStartOfStmtDueToEndCharactersInCommentsOrQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionStartingNewStmt(null, null);

    // Not starting new stmt, ignore end chars in quotes
    _setDocText("x = bar + \";\" + \"}\" + \"{\"\n+ foo;\n");
    _doc.setCurrentLocation(26);
    assertTrue("not starting new stmt, ignore end chars in quotes",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Not starting new stmt, ignore end chars in single-line comments
    _setDocText("x = bar.//;{}\nfoo();\n");
    _doc.setCurrentLocation(14);
    assertTrue("not starting new stmt, ignore end chars in single-line comments",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Not starting new stmt, ignore end chars in multi-line comments
    _setDocText("x = bar./*;\n{\n}\n*/\nfoo();\n");
    _doc.setCurrentLocation(19);
    assertTrue("not starting new stmt, ignore end chars in multi-line comments",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }
}
