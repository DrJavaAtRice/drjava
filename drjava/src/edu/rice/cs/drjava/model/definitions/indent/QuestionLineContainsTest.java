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
 * in the document contains the given character.
 * <p>
 * All tests check for the ':' character on the current line.
 *
 * @version $Id$
 */
public class QuestionLineContainsTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionLineContainsTest(String name) {
    super(name);
  }

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line of text containing a colon is detected.
   */
  public void testLineContainsColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // Colon in text
    _setDocText("return test ? x : y;\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("colon in text (after startdoc)",
	       rule.applyRule(_doc));
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in text (after newline)",
	       rule.applyRule(_doc));
    _doc.setCurrentLocation(25);
    assertTrue("colon in text (after colon on line)",
	       rule.applyRule(_doc));
  }    
  
  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line does not contain a colon.
   */
  public void testLineDoesNotContainColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
    
    // No colon in text
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(6);
    assertTrue("no colon", !rule.applyRule(_doc));
    _doc.setCurrentLocation(28);
    assertTrue("line of close brace (no colon in text)", !rule.applyRule(_doc));    
  }

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line containing a commented out colon is identified as a
   * line that does not contain a colon.
   */
  public void testLineDoesNotContainColonDueToComments() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // No colon, single line comment
    _setDocText("//case 1:\nreturn test; //? x : y\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("entire line with colon in comment (no colon, single line comment)",
	       !rule.applyRule(_doc));
    _doc.setCurrentLocation(10);
    assertTrue("part of line with colon in comment (no colon, single line comment)",
	       !rule.applyRule(_doc));

    // No colon, multi-line comment
    _setDocText("foo();\nreturn test; /*? x : y*/\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in multi-line comment", !rule.applyRule(_doc));
  }

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line containing a colon in quotes is identified as a
   * line that does not contain a colon.
   */
  public void testLineDoesNotContainColonDueToQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
  
    // No colon, quotes
    _setDocText("foo();\nreturn \"total: \" + sum\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in quotes", !rule.applyRule(_doc));
  }
}
