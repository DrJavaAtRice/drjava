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
 * Tests the question rule which determines if the current line
 * is starting a new statement.
 *
 * @version $Id$
 */
public class QuestionStartingNewStmtTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionStartingNewStmtTest(String name) {
    super(name);
  }

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
	       rule.applyRule(_doc));

    // Starting new stmt, prev char ';'
    _setDocText("foo();\nbar();\n");
    _doc.setCurrentLocation(7);
    assertTrue("starting new stmt, prev char ';'",
	       rule.applyRule(_doc));
    
    // Starting new stmt, prev char '{'
    _setDocText("public void foo() {\nfoo()\n");
    _doc.setCurrentLocation(20);
    assertTrue("starting new stmt, prev char '{'",
	       rule.applyRule(_doc));

    // Starting new stmt, prev char '}'
    _setDocText("x();\n}\nfoo()\n");
    _doc.setCurrentLocation(7);
    assertTrue("starting new stmt, prev char '}'",
	       rule.applyRule(_doc));
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
	       rule.applyRule(_doc));

    // Starting new stmt, ignore single line comments
    _setDocText("} // note:\n//please ignore me\nfoo();\n");
    _doc.setCurrentLocation(30);
    assertTrue("starting new stmt, ignore single line comments",
	       rule.applyRule(_doc));

    // Starting new stmt, ignore multi-line comments
    _setDocText("{ /* in a comment\nstill in a comment\ndone */\nfoo();");
    _doc.setCurrentLocation(45);
    assertTrue("starting new stmt, ignore multi-line comments",
	       rule.applyRule(_doc));

    _setDocText("bar();\n/* blah */ foo();\n");
    _doc.setCurrentLocation(18);
    assertTrue("starting new stmt, ignore multi-line comment on same " +
	       "line as new stmt",
	       rule.applyRule(_doc));

    _setDocText("method foo() {\n" +
		"}\n" +
		"     ");
    _doc.setCurrentLocation(17);
    assertTrue("Blank line with no non-WS after",
	       rule.applyRule(_doc));

    _setDocText("method foo() {\n" +
		"}\n" +
		"     \n" +
		"// comment");
    _doc.setCurrentLocation(17);
    assertTrue("Blank line with comments after, but no non-WS",
	       rule.applyRule(_doc));
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
	       !rule.applyRule(_doc));

    // Not starting new stmt, ignore end chars in single-line comments
    _setDocText("x = bar.//;{}\nfoo();\n");
    _doc.setCurrentLocation(14);
    assertTrue("not starting new stmt, ignore end chars in single-line comments",
	       !rule.applyRule(_doc));

    // Not starting new stmt, ignore end chars in multi-line comments
    _setDocText("x = bar./*;\n{\n}\n*/\nfoo();\n");
    _doc.setCurrentLocation(19);
    assertTrue("not starting new stmt, ignore end chars in multi-line comments",
	       !rule.applyRule(_doc));
  }
}
