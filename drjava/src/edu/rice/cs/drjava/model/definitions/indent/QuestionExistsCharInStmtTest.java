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
 * Tests the question rule which determines if the given findChar
 * is found between the start of the statement and the endChar,
 * which must exist on the current line.
 * <p>
 * This is done in the context of determining if a colon that
 * was found on the current line is part of a ternary operator.
 * Hence, we use endChar=':' and findChar='?'.
 *
 * @version $Id$
 */
public final class QuestionExistsCharInStmtTest extends IndentRulesTestCase {
  
  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that this rule works for one line statements.
   */
  public void testColonInTernaryOpOneLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);
   
    // Colon not in ternary op, one line stmt, no '?'
    _setDocText("case 1: foo()\ncase default: break;\n");
    _doc.setCurrentLocation(0);
    assertTrue("colon not in ternary op, one line stmt, no '?'",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(16);
    assertTrue("after newline (colon not in ternary op, one line stmt, no '?')",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, one line stmt
    _setDocText("foo();\nreturn (test ? x : y;)\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in ternary op, same line", 
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that this rule works when there are two statements on the same line.
   * Essentially, that it uses the first colon that it finds on the line
   * as the endChar.
   */
  public void testColonInTernaryOpTwoStmtsOnOneLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon in ternary op, two stmts on one line
    _setDocText("foo();\nreturn (test ? x : y); case default: break;\n");
    _doc.setCurrentLocation(7);
    assertTrue("colon in ternary op, two stmts on one line",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    
    _setDocText("foo();\ncase default: break; return test ? x : y;\n");
    // Colon not in ternary op, two stmts on one line
    _doc.setCurrentLocation(7);
    assertTrue("colon not in ternary op, two stmts on one line",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that a colon in a multi-line ternary op statement is detected.
   */
  public void testColonInTernaryOpMultiLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon in ternary op, multi-line stmt
    _setDocText("foo();\nreturn test ?\nx : y;\n");
    _doc.setCurrentLocation(22);
    assertTrue("colon in ternary op, multi-line stmt",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that whitespace, single-line comments and multi-line comments
   * in between the ':' character and the '?' character are ignored.
   */
  public void testColonInTernaryOpIgnoreWhitespaceAndComments() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon in ternary op, ignores whitespace
    _setDocText("foo;\nreturn test ?\n    \n \t \nx : y;\n");
    _doc.setCurrentLocation(28);
    assertTrue("colon in ternary op, multi-line stmt, ignores whitespace",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, ignores single line comments
    _setDocText("foo();\nreturn test ? //{\n//case 1: bar();\nx() : y();\n");
    _doc.setCurrentLocation(42);
    assertTrue("colon in ternary op, ignores single line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, ignores multi-line comments
    _setDocText("foo();\nreturn test ? /* {\ncase 1 : bar();*/\nx() : y();\n");
    _doc.setCurrentLocation(44);
    assertTrue("colon in ternary op, ignores multi-line comments",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that a '?' in quotes or single-line comments or multi-line
   * comments is not detected - and hence that a colon is not party of
   * a ternary op.
   */
  public void testColonNotInTernaryOpDueToQuestionMarkInCommentsOrQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon not in ternary op, ignores '?' in single-line comments
    _setDocText("foo();\nreturn test; //?\ncase default: break;\n");
    _doc.setCurrentLocation(38);
    assertTrue("colon not in ternary op, ignores '?' in single-line comments",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in ternary op, ignores '?' in multi-line comments
    _setDocText("foo();\nreturn test; /* huh? okay */\ncase default: break;\n");
    _doc.setCurrentLocation(36);
    assertTrue("colon not in ternary op, ignores '?' in multi-line comments",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in quotes, ignores '?' in quotes
    _setDocText("foo();\nreturn str + \"?\";\ncase default: break;\n");
    _doc.setCurrentLocation(25);
    assertTrue("colon not in ternary op, ignores '?' in quotes",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

  }

  /**
   * Ensures that a colon that is part of a ternary operator is detected.
   * Tests that a colon that is part of a multi-line statement is 
   * not falsely identified as belonging to a ternary op.
   */
  public void testColonNotInTernaryOpMultiLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon not in ternary op, multi-line stmt
    _setDocText("return test ? x : y;\ncase 1\n: foo();\n");
    _doc.setCurrentLocation(28);
    assertTrue("colon not in ternary op, multi-line stmt",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in ternary op, multi-line stmt, 
    // same line as end of ternary op
    _setDocText("foo()\nreturn test ? x :\ny; case default: break;\n");
    _doc.setCurrentLocation(24);
    assertTrue("colon not in ternary op, multi-line stmt, same line as end of ternary op",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }
}


