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
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("foo();\n}\n");
    assertTrue("no open brace", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("line of close brace (no open brace)", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("line after close brace (no open brace)", !rule.applyRule(_doc, 8, Indenter.OTHER));
    
    // Close brace
    rule = new QuestionCurrLineStartsWith("}", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no close brace", !rule.applyRule(_doc, 0, Indenter.OTHER));
  }
  
  /**
   * Tests having a line start with prefix, with text following
   */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("foo();\n}bar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 9, Indenter.OTHER));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 15, Indenter.OTHER));
    
    // Prefix plus text (with space)
    rule = new QuestionCurrLineStartsWith("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("line before star (with space)", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before star (with space)", rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star (with space)", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star (with space)", !rule.applyRule(_doc, 15, Indenter.OTHER));
  }
  
  /**
   * Tests having a line start with prefix, with no text following
   */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n{\nbar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 10, Indenter.OTHER));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   {\nbar();\n");
    assertTrue("line before brace (with space)", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before brace (with space)", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after brace (with space)", rule.applyRule(_doc, 11, Indenter.OTHER));
    assertTrue("line after brace (with space)", !rule.applyRule(_doc, 14, Indenter.OTHER));
  }
  
  /**
   * Tests having a multiple character prefix.
   */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith(".*.", null, null);
    
    // Multi-char prefix
    _setDocText("*\n.*\n.*.\n.*.foo");
    assertTrue("star", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("dot star", !rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("dot star dot", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("dot star dot text", rule.applyRule(_doc, 9, Indenter.OTHER));
  }
    
  /**
   * Tests having a commented prefix without searching in comments.
   */
  public void testCommentedPrefixDontSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace in comment
    _setDocText("foo();\n// {\nbar();\n");
    assertTrue("just before brace", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after brace", !rule.applyRule(_doc, 11, Indenter.OTHER));
    assertTrue("line after brace", !rule.applyRule(_doc, 12, Indenter.OTHER));
  }

  /**
   * Tests having a commented prefix with searching in comments.
   */
  public void testCommentedPrefixSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n");
    assertTrue("line before star", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before star", rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 7, Indenter.OTHER));
  }
  
  /**
   * Tests having text on a line before the prefix.
   */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    // Close brace in text, not starting line
    _setDocText("foo(); }\nbar();\n");
    assertTrue("before brace", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("just before brace", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after brace", !rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("line after brace", !rule.applyRule(_doc, 10, Indenter.OTHER));
  }

  /**
   * Prefix appears at the end of a document.
   */
  public void testPrefixAtEnd() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    _setDocText("void foo() {\n}");
    assertTrue("first line", !rule.applyRule(_doc, 3, Indenter.OTHER));
    assertTrue("end of first line", !rule.applyRule(_doc, 12, Indenter.OTHER));
    assertTrue("beginning of second line", rule.applyRule(_doc, 13, Indenter.OTHER));
    assertTrue("end of second line", rule.applyRule(_doc, 14, Indenter.OTHER));
  }
  
  /**
   * Tests multiple-character prefix.
   */
  public void testMultCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("abcdefg", null, null);
    
    _setDocText("   abcdefghij\n  abcde");
    assertTrue("first line, beginning", rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("first line, mid", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("first line, end", rule.applyRule(_doc, 13, Indenter.OTHER));
    assertTrue("second line, beginning", !rule.applyRule(_doc, 14, Indenter.OTHER));
    assertTrue("second line, mid", !rule.applyRule(_doc, 18, Indenter.OTHER));
    assertTrue("second line, end", !rule.applyRule(_doc, 21, Indenter.OTHER));
  }
}