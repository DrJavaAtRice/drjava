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
 * Tests the indention rule which detects whether the current line starts
 * a new parenthesized phrase.  (ie. Previous line ends in comma, semicolon,
 * open paren, or open bracket.)
 * @version $Id$
 */
public class QuestionNewParenPhraseTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   */
  public QuestionNewParenPhraseTest(String name) {
    super(name);
  }

  /**
   * Tests hitting start of document.
   */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.applyRule(_doc, 0));
    assertTrue("second line", !rule.applyRule(_doc, 2));
  }
  
  /**
   * Tests having no paren phrase delimiters on prev line.
   */
  public void testNoParenDelims() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // No paren delimiters
    _setDocText("foo\nbar.\ny");
    assertTrue("second line", !rule.applyRule(_doc, 4));
    assertTrue("third line", !rule.applyRule(_doc, 9));
  }
  
  /**
   * Tests having delimiter on prev line, with text preceding
   */
  public void testParenDelimsWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
        
    // Lines ending in delimiter, each with preceding text
    _setDocText("new Foo(\nx,\ny;\na[\nbar])\n{");
    assertTrue("line after paren", rule.applyRule(_doc, 9));
    assertTrue("line after comma", rule.applyRule(_doc, 12));
    assertTrue("line after semicolon", rule.applyRule(_doc, 15));
    assertTrue("line after bracket", rule.applyRule(_doc, 18));
    assertTrue("line after close paren", !rule.applyRule(_doc, 24));
  }
  
  /**
   * Tests having delimiter on prev line, with no text preceding
   */
  public void testParenDelimsNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Paren delims with no leading text
    _setDocText("(\n,\n;\n[\nfoo\nbar");
    assertTrue("line after paren", rule.applyRule(_doc, 2));
    assertTrue("line after comma", rule.applyRule(_doc, 4));
    assertTrue("line after semicolon", rule.applyRule(_doc, 6));
    assertTrue("line after bracket", rule.applyRule(_doc, 8));
    assertTrue("line after text", !rule.applyRule(_doc, 12));
  }
  
  /**
   * Tests having a comment after the delimiter
   */
  public void testParenDelimsWithComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, with comment before
    _setDocText("for (int i; // comment\ni < 2; /** comment */\ni++) {");
    assertTrue("// comment", rule.applyRule(_doc, 23));
    assertTrue("/* */ comment", rule.applyRule(_doc, 45));
  }
  
  /**
   * Tests having a paren delimiter several lines back, with only
   * whitespace inbetween.
   */
  public void testMultipleBlankLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Blank lines between
    _setDocText("for(\n\nint i;\n\n\ni > 0;;\n)");
    assertTrue("line after open paren", rule.applyRule(_doc, 5));
    assertTrue("two lines after open paren", rule.applyRule(_doc, 6));
    assertTrue("line after semicolon", rule.applyRule(_doc, 13));
    assertTrue("two lines after semicolon", rule.applyRule(_doc, 16));
  }
  
  /**
   * Tests having a paren delimiter several lines back, with only
   * blank space and comments inbetween.
   */
  public void testMultipleCommentLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Comments between
    _setDocText("for(\n//\n/** foo * /int i;\n\n// bar\ni > 0;;\n)");
    assertTrue("line after open paren", rule.applyRule(_doc, 7));
    assertTrue("two lines after open paren", rule.applyRule(_doc, 18));
    assertTrue("line after semicolon", rule.applyRule(_doc, 25));
    assertTrue("two lines after semicolon", rule.applyRule(_doc, 28));
  }
  
  /**
   * Tests having text on a line after the delimiter.
   */
  public void testDoesNotEndWithParenDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(bar.\nx,y\n)");
    assertTrue("line after paren", !rule.applyRule(_doc, 9));
    assertTrue("line after comma", !rule.applyRule(_doc, 13));
  }
  
  /**
   * Tests having an operator as a delimiter.
   */
  public void testOperatorDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x +\ny\n)");
    assertTrue("line after operator", rule.applyRule(_doc, 8));
    assertTrue("line after comma", !rule.applyRule(_doc, 10));
  }
  
  /**
   * Tests ignoring delims on line.
   */
  public void testIgnoreDelimsOnLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x.\ny()\n)");
    assertTrue("after paren, but not new phrase", !rule.applyRule(_doc, 10));
  }

}
