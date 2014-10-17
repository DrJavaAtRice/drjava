/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2014, JavaPLT group at Rice University (drjava@rice.edu)
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
 * Tests the indention rule which detects whether the current line starts
 * a new parenthesized phrase.  (ie. Previous line ends in comma, semicolon,
 * open paren, or open bracket.)
 * @version $Id: QuestionNewParenPhraseTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class QuestionNewParenPhraseTest extends IndentRulesTestCase {

  /** Tests hitting start of document.
   */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("second line", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having no paren phrase delimiters on prev line.
   */
  public void testNoParenDelims() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // No paren delimiters
    _setDocText("foo\nbar.\ny");
    assertTrue("second line", !rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("third line", !rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having delimiter on prev line, with text preceding. */
  public void testParenDelimsWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Lines ending in delimiter, each with preceding text
    _setDocText("new Foo(\nx,\ny;\na[\nbar])\n{");
    assertTrue("line after paren", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertTrue("line after comma", rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("line after semicolon", rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
    assertTrue("line after bracket", rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("line after close paren", !rule.testApplyRule(_doc, 24, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having delimiter on prev line, with no text preceding. */
  public void testParenDelimsNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Paren delims with no leading text
    _setDocText("(\n,\n;\n[\nfoo\nbar");
    assertTrue("line after paren", rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("line after comma", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("line after semicolon", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after bracket", rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after text", !rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a comment after the delimiter
   */
  public void testParenDelimsWithComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, with comment before
    _setDocText("for (int i; // comment\ni < 2; /** comment */\ni++) {");
    assertTrue("// comment", rule.testApplyRule(_doc, 23, Indenter.IndentReason.OTHER));
    assertTrue("/* */ comment", rule.testApplyRule(_doc, 45, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a paren delimiter several lines back, with only
   * whitespace inbetween.
   */
  public void testMultipleBlankLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Blank lines between
    _setDocText("for(\n\nint i;\n\n\ni > 0;;\n)");
    assertTrue("line after open paren", rule.testApplyRule(_doc, 5, Indenter.IndentReason.OTHER));
    assertTrue("two lines after open paren", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after semicolon", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("two lines after semicolon", rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a paren delimiter several lines back, with only
   * blank space and comments inbetween.
   */
  public void testMultipleCommentLinesBack() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Comments between
    _setDocText("for(\n//\n/** foo * /int i;\n\n// bar\ni > 0;;\n)");
    assertTrue("line after open paren", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("two lines after open paren", rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("line after semicolon", rule.testApplyRule(_doc, 25, Indenter.IndentReason.OTHER));
    assertTrue("two lines after semicolon", rule.testApplyRule(_doc, 28, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having text on a line after the delimiter.
   */
  public void testDoesNotEndWithParenDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(bar.\nx,y\n)");
    assertTrue("line after paren", !rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertTrue("line after comma", !rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having an operator as a delimiter.
   */
  public void testOperatorDelim() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x +\ny\n)");
    assertTrue("line after operator", rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after comma", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }
  
  /** Tests ignoring delims on line.
   */
  public void testIgnoreDelimsOnLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionNewParenPhrase(null, null);
    
    // Delim in text, not ending line
    _setDocText("foo(x.\ny()\n)");
    assertTrue("after paren, but not new phrase", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }

}
