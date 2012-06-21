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
  
  /** Ensures that a colon that is part of a ternary operator is detected.
   * Tests that this rule works for one line statements.
   */
  public void testColonInTernaryOpOneLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);
   
    // Colon not in ternary op, one line stmt, no '?'
    _setDocText("case 1: foo()\ncase default: break;\n");
    _doc.setCurrentLocation(0);
    assertTrue("colon not in ternary op, one line stmt, no '?'",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(16);
    assertTrue("after newline (colon not in ternary op, one line stmt, no '?')",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, one line stmt
    _setDocText("foo();\nreturn (test ? x : y;)\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in ternary op, same line", 
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a colon that is part of a ternary operator is detected.
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
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    
    _setDocText("foo();\ncase default: break; return test ? x : y;\n");
    // Colon not in ternary op, two stmts on one line
    _doc.setCurrentLocation(7);
    assertTrue("colon not in ternary op, two stmts on one line",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a colon that is part of a ternary operator is detected.
   * Tests that a colon in a multi-line ternary op statement is detected.
   */
  public void testColonInTernaryOpMultiLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon in ternary op, multi-line stmt
    _setDocText("foo();\nreturn test ?\nx : y;\n");
    _doc.setCurrentLocation(22);
    assertTrue("colon in ternary op, multi-line stmt",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a colon that is part of a ternary operator is detected.
   * Tests that whitespace, single-line comments and multi-line comments
   * in between the ':' character and the '?' character are ignored.
   */
  public void testColonInTernaryOpIgnoreWhitespaceAndComments() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon in ternary op, ignores whitespace
    _setDocText("foo;\nreturn test ?\n    \n \t \nx : y;\n");
    _doc.setCurrentLocation(28);
    assertTrue("colon in ternary op, multi-line stmt, ignores whitespace",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, ignores single line comments
    _setDocText("foo();\nreturn test ? //{\n//case 1: bar();\nx() : y();\n");
    _doc.setCurrentLocation(42);
    assertTrue("colon in ternary op, ignores single line comments",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon in ternary op, ignores multi-line comments
    _setDocText("foo();\nreturn test ? /* {\ncase 1 : bar();*/\nx() : y();\n");
    _doc.setCurrentLocation(44);
    assertTrue("colon in ternary op, ignores multi-line comments",
        rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /** Ensures that a colon that is part of a ternary operator is detected.
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
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in ternary op, ignores '?' in multi-line comments
    _setDocText("foo();\nreturn test; /* huh? okay */\ncase default: break;\n");
    _doc.setCurrentLocation(36);
    assertTrue("colon not in ternary op, ignores '?' in multi-line comments",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in quotes, ignores '?' in quotes
    _setDocText("foo();\nreturn str + \"?\";\ncase default: break;\n");
    _doc.setCurrentLocation(25);
    assertTrue("colon not in ternary op, ignores '?' in quotes",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

  }

  /** Ensures that a colon that is part of a ternary operator is detected.
   * Tests that a colon that is part of a multi-line statement is 
   * not falsely identified as belonging to a ternary op.
   */
  public void testColonNotInTernaryOpMultiLineStmts() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionExistsCharInStmt('?', ':', null, null);

    // Colon not in ternary op, multi-line stmt
    _setDocText("return test ? x : y;\ncase 1\n: foo();\n");
    _doc.setCurrentLocation(28);
    assertTrue("colon not in ternary op, multi-line stmt",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));

    // Colon not in ternary op, multi-line stmt, 
    // same line as end of ternary op
    _setDocText("foo()\nreturn test ? x :\ny; case default: break;\n");
    _doc.setCurrentLocation(24);
    assertTrue("colon not in ternary op, multi-line stmt, same line as end of ternary op",
        !rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
  }
}


