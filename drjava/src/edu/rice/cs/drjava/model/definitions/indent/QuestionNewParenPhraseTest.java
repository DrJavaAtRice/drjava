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
 * Tests the indention rule which detects whether the current line starts
 * a new parenthesized phrase.  (ie. Previous line ends in comma, semicolon,
 * open paren, or open bracket.)
 * @version $Id$
 */
public final class QuestionNewParenPhraseTest extends IndentRulesTestCase {

  /**
   * Tests hitting start of document.
   */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("second line", !rule.applyRule(_doc, 2, Indenter.OTHER));
  }
  
  /**
   * Tests having no paren phrase delimiters on prev line.
   */
  public void testNoParenDelims() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // No paren delimiters
    _setDocText("foo\nbar.\ny");
    assertTrue("second line", !rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("third line", !rule.applyRule(_doc, 9, Indenter.OTHER));
  }
  
  /**
   * Tests having delimiter on prev line, with text preceding
   */
  public void testParenDelimsWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
        
    // Lines ending in delimiter, each with preceding text
    _setDocText("new Foo(\nx,\ny;\na[\nbar])\n{");
    assertTrue("line after paren", rule.applyRule(_doc, 9, Indenter.OTHER));
    assertTrue("line after comma", rule.applyRule(_doc, 12, Indenter.OTHER));
    assertTrue("line after semicolon", rule.applyRule(_doc, 15, Indenter.OTHER));
    assertTrue("line after bracket", rule.applyRule(_doc, 18, Indenter.OTHER));
    assertTrue("line after close paren", !rule.applyRule(_doc, 24, Indenter.OTHER));
  }
  
  /**
   * Tests having delimiter on prev line, with no text preceding
   */
  public void testParenDelimsNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Paren delims with no leading text
    _setDocText("(\n,\n;\n[\nfoo\nbar");
    assertTrue("line after paren", rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("line after comma", rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("line after semicolon", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after bracket", rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("line after text", !rule.applyRule(_doc, 12, Indenter.OTHER));
  }
  
  /**
   * Tests having a comment after the delimiter
   */
  public void testParenDelimsWithComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, with comment before
    _setDocText("for (int i; // comment\ni < 2; /** comment */\ni++) {");
    assertTrue("// comment", rule.applyRule(_doc, 23, Indenter.OTHER));
    assertTrue("/* */ comment", rule.applyRule(_doc, 45, Indenter.OTHER));
  }
  
  /**
   * Tests having a paren delimiter several lines back, with only
   * whitespace inbetween.
   */
  public void testMultipleBlankLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Blank lines between
    _setDocText("for(\n\nint i;\n\n\ni > 0;;\n)");
    assertTrue("line after open paren", rule.applyRule(_doc, 5, Indenter.OTHER));
    assertTrue("two lines after open paren", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after semicolon", rule.applyRule(_doc, 13, Indenter.OTHER));
    assertTrue("two lines after semicolon", rule.applyRule(_doc, 16, Indenter.OTHER));
  }
  
  /**
   * Tests having a paren delimiter several lines back, with only
   * blank space and comments inbetween.
   */
  public void testMultipleCommentLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Comments between
    _setDocText("for(\n//\n/** foo * /int i;\n\n// bar\ni > 0;;\n)");
    assertTrue("line after open paren", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("two lines after open paren", rule.applyRule(_doc, 18, Indenter.OTHER));
    assertTrue("line after semicolon", rule.applyRule(_doc, 25, Indenter.OTHER));
    assertTrue("two lines after semicolon", rule.applyRule(_doc, 28, Indenter.OTHER));
  }
  
  /**
   * Tests having text on a line after the delimiter.
   */
  public void testDoesNotEndWithParenDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(bar.\nx,y\n)");
    assertTrue("line after paren", !rule.applyRule(_doc, 9, Indenter.OTHER));
    assertTrue("line after comma", !rule.applyRule(_doc, 13, Indenter.OTHER));
  }
  
  /**
   * Tests having an operator as a delimiter.
   */
  public void testOperatorDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x +\ny\n)");
    assertTrue("line after operator", rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("line after comma", !rule.applyRule(_doc, 10, Indenter.OTHER));
  }
  
  /**
   * Tests ignoring delims on line.
   */
  public void testIgnoreDelimsOnLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x.\ny()\n)");
    assertTrue("after paren, but not new phrase", !rule.applyRule(_doc, 10, Indenter.OTHER));
  }

}
