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
 * Tests the indention rule which detects whether the current line
 * starts with a particular string.
 * @version $Id$
 */
public class QuestionCurrLineStartsWithTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionCurrLineStartsWithTest(String name) {
    super(name);
  }

  /**
   * Tests not having the prefix in the text.
   */
  public void testNoPrefix() throws BadLocationException {
    /**
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("foo();\n}\n");
    assertTrue("no open brace", !rule.applyRule(_doc, 0));
    assertTrue("line of close brace (no open brace)", !rule.applyRule(_doc, 7));
    assertTrue("line after close brace (no open brace)", !rule.applyRule(_doc, 8));
    
    // Close brace
    rule = new QuestionCurrLineStartsWith("}", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no close brace", !rule.applyRule(_doc, 0));
    */
  }
  
  /**
   * Tests having a line start with prefix, with text following
   *
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("foo();\n}bar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 9));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 15));
    
    // Prefix plus text (with space)
    rule = new QuestionCurrLineStartsWith("*", true, null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("line before star (with space)", !rule.applyRule(_doc, 0));
    assertTrue("just before star (with space)", rule.applyRule(_doc, 4));
    assertTrue("just after star (with space)", rule.applyRule(_doc, 6));
    assertTrue("line after star (with space)", !rule.applyRule(_doc, 15));
  }*/
  
  /**
   * Tests having a line start with prefix, with no text following
   *
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n{\nbar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 8));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 10));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   {\nbar();\n");
    assertTrue("line before brace (with space)", !rule.applyRule(_doc, 0));
    assertTrue("just before brace (with space)", rule.applyRule(_doc, 7));
    assertTrue("just after brace (with space)", rule.applyRule(_doc, 11));
    assertTrue("line after brace (with space)", !rule.applyRule(_doc, 14));
  }*/
  
  /**
   * Tests having a multiple character prefix.
   *
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith(" * ", true, null, null);
    
    // Multi-char prefix
    _setDocText("*\n *\n * \n * foo");
    assertTrue("star", !rule.applyRule(_doc, 0));
    assertTrue("space star", !rule.applyRule(_doc, 2));
    assertTrue("space star space", rule.applyRule(_doc, 7));
    assertTrue("space star space text", rule.applyRule(_doc, 9));
  }*/
  
  /**
   * Tests having a comment before the prefix.
   *
  public void testStartsWithPrefixWithComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    // Prefix in text, with comment before
    _setDocText("foo();\n/** comment * /}\nbar();\n");
    assertTrue("line before brace", !rule.applyRule(_doc, 0));
    assertTrue("just before brace", rule.applyRule(_doc, 7));
    assertTrue("just after brace", rule.applyRule(_doc, 22));
    assertTrue("line after brace", !rule.applyRule(_doc, 23));
  }*/
  
  /**
   * Tests having a commented prefix without searching in comments.
   *
  public void testCommentedPrefixDontSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace in comment
    _setDocText("foo();\n// {\nbar();\n");
    assertTrue("just before brace", !rule.applyRule(_doc, 7));
    assertTrue("just after brace", !rule.applyRule(_doc, 11));
    assertTrue("line after brace", !rule.applyRule(_doc, 12));
    
    // Close brace in comment
    rule = new QuestionCurrLineStartsWith("}", false, null, null);
    _setDocText("/**\n}\n* /\n");
    assertTrue("line before brace", !rule.applyRule(_doc, 0));
    assertTrue("just before brace", !rule.applyRule(_doc, 4));
    assertTrue("just after brace", !rule.applyRule(_doc, 5));
    assertTrue("line after brace", !rule.applyRule(_doc, 7));

  }*/

  /**
   * Tests having a commented prefix with searching in comments.
   *
  public void testCommentedPrefixSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("*", true, null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n");
    assertTrue("line before star", !rule.applyRule(_doc, 0));
    assertTrue("just before star", rule.applyRule(_doc, 4));
    assertTrue("just after star", rule.applyRule(_doc, 6));
    assertTrue("line after star", !rule.applyRule(_doc, 7));
  }*/
  
  /**
   * Tests having text on a line before the prefix.
   *
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    // Close brace in text, not starting line
    _setDocText("foo(); }\nbar();\n");
    assertTrue("before brace", !rule.applyRule(_doc, 0));
    assertTrue("just before brace", !rule.applyRule(_doc, 7));
    assertTrue("just after brace", !rule.applyRule(_doc, 8));
    assertTrue("line after brace", !rule.applyRule(_doc, 10));
  }*/


}
