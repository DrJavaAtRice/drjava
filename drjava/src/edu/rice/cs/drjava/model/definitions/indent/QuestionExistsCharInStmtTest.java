/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

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
public class QuestionExistsCharInStmtTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionExistsCharInStmtTest(String name) {
    super(name);
  }

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
	       !rule.applyRule(_doc));
    _doc.setCurrentLocation(16);
    assertTrue("after newline (colon not in ternary op, one line stmt, no '?')",
	       !rule.applyRule(_doc));

    // Colon in ternary op, one line stmt
    _setDocText("foo();\nreturn test ? x : y;\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in ternary op, same line", 
	       rule.applyRule(_doc));
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
    _setDocText("foo();\nreturn test ? x : y; case default: break;\n");
    _doc.setCurrentLocation(7);
    assertTrue("colon in ternary op, two stmts on one line",
	       rule.applyRule(_doc));
    
    _setDocText("foo();\ncase default: break; return test ? x : y;\n");
    // Colon not in ternary op, two stmts on one line
    _doc.setCurrentLocation(7);
    assertTrue("colon not in ternary op, two stmts on one line",
	       !rule.applyRule(_doc));
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
	       rule.applyRule(_doc));
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
	       rule.applyRule(_doc));

    // Colon in ternary op, ignores single line comments
    _setDocText("foo();\nreturn test ? //{\n//case 1: bar();\nx() : y();\n");
    _doc.setCurrentLocation(42);
    assertTrue("colon in ternary op, ignores single line comments",
	       rule.applyRule(_doc));

    // Colon in ternary op, ignores multi-line comments
    _setDocText("foo();\nreturn test ? /* {\ncase 1 : bar();*/\nx() : y();\n");
    _doc.setCurrentLocation(44);
    assertTrue("colon in ternary op, ignores multi-line comments",
	       rule.applyRule(_doc));
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
	       !rule.applyRule(_doc));

    // Colon not in ternary op, ignores '?' in multi-line comments
    _setDocText("foo();\nreturn test; /* huh? okay */\ncase default: break;\n");
    _doc.setCurrentLocation(36);
    assertTrue("colon not in ternary op, ignores '?' in multi-line comments",
	       !rule.applyRule(_doc));

    // Colon not in quotes, ignores '?' in quotes
    _setDocText("foo();\nreturn str + \"?\";\ncase default: break;\n");
    _doc.setCurrentLocation(25);
    assertTrue("colon not in ternary op, ignores '?' in quotes",
	       !rule.applyRule(_doc));

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
	       !rule.applyRule(_doc));

    // Colon not in ternary op, multi-line stmt, 
    // same line as end of ternary op
    _setDocText("foo()\nreturn test ? x :\ny; case default: break;\n");
    _doc.setCurrentLocation(24);
    assertTrue("colon not in ternary op, multi-line stmt, same line as end of ternary op",
	       !rule.applyRule(_doc));
  }
}


