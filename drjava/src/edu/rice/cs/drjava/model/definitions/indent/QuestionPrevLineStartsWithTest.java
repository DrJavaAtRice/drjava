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
 * Tests the indention rule which detects whether the immediately previous line
 * starts with a particular string.
 * @version $Id$
 */
public class QuestionPrevLineStartsWithTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionPrevLineStartsWithTest(String name) {
    super(name);
  }

  /**
   * Tests not having the prefix in the text.
   */
  public void testNoPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("}\nfoo();\n}\n");
    assertTrue("line after close brace (no open brace)", !rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("line after text (no open brace)", !rule.applyRule(_doc, 9, Indenter.OTHER));
    assertTrue("line after text (no open brace)", !rule.applyRule(_doc, 10, Indenter.OTHER));
    
    // Star
    rule = new QuestionPrevLineStartsWith("*", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no star", !rule.applyRule(_doc, 6, Indenter.OTHER));
    
  }
  
  /**
   * Tests hitting start of document.
   */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("{", null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("second line", !rule.applyRule(_doc, 2, Indenter.OTHER));
  }
  
  /**
   * Tests prefix on current line.
   */
  public void testPrefixOnCurrLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("}", null, null);
    
    // Prefix at start of current line
    _setDocText("} foo();");
    assertTrue("before brace", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("after brace", !rule.applyRule(_doc, 2, Indenter.OTHER));
    
    // Prefix in middle of current line
    _setDocText("foo();\n bar(); } foo();");
    assertTrue("before brace", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("after brace", !rule.applyRule(_doc, 18, Indenter.OTHER));
  }
  
  /**
   * Tests having prev line start with prefix, with text following
   */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("}bar();\nfoo();\nbar();");
    assertTrue("line of brace (no space)", !rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("line after brace (no space)", rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("two lines after brace (no space)", !rule.applyRule(_doc, 16, Indenter.OTHER));
    
    // Prefix plus text (with space)
    rule = new QuestionPrevLineStartsWith("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("just before star (with space)", !rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star (with space)", !rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star (with space)", rule.applyRule(_doc, 16, Indenter.OTHER));
  }
  
  /**
   * Tests having prev line start with prefix, with no text following
   */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n*\nbar();\n}");
    assertTrue("line of star (no space)", !rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("line after star (no space)", rule.applyRule(_doc, 10, Indenter.OTHER));
    assertTrue("two lines after star (no space)", !rule.applyRule(_doc, 16, Indenter.OTHER));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   * \nbar();\n{");
    assertTrue("line of star (with space)", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("just after star (with space)", !rule.applyRule(_doc, 11, Indenter.OTHER));
    assertTrue("line after star (with space)", rule.applyRule(_doc, 13, Indenter.OTHER));
  }
  
  /**
   * Tests having a multiple character prefix.
   */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("* ", null, null);
    
    // Multi-char prefix
    _setDocText("*\n *\n * \n * foo\nbar");
    assertTrue("star", !rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("space star", !rule.applyRule(_doc, 5, Indenter.OTHER));
    assertTrue("space star space", rule.applyRule(_doc, 11, Indenter.OTHER));
    assertTrue("space star space text", rule.applyRule(_doc, 16, Indenter.OTHER));
  }
  
  /**
   * Tests having a commented prefix.
   */
  public void testCommentedPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n*/");
    assertTrue("just before star", !rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star", !rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 15, Indenter.OTHER));
  }
  
  /**
   * Tests having text on a line before the prefix.
   */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in text, not starting line
    _setDocText("foo(); *\nbar();\n");
    assertTrue("line after star", !rule.applyRule(_doc, 10, Indenter.OTHER));
  }


}
