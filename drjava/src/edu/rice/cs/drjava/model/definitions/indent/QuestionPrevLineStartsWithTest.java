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
 * Tests the indention rule which detects whether the immediately previous line
 * starts with a particular string.
 * @version $Id: QuestionPrevLineStartsWithTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class QuestionPrevLineStartsWithTest extends IndentRulesTestCase {

  /** Tests not having the prefix in the text. */
  public void testNoPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("}\nfoo();\n}\n");
    assertTrue("line after close brace (no open brace)", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("line after text (no open brace)", !rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertTrue("line after text (no open brace)", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    
    // Star
    rule = new QuestionPrevLineStartsWith("*", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no star", !rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    
  }
  
  /** Tests hitting start of document. */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("{", null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("second line", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
  }
  
  /** Tests prefix on current line. */
  public void testPrefixOnCurrLine() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("}", null, null);
    
    // Prefix at start of current line
    _setDocText("} foo();");
    assertTrue("before brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("after brace", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    
    // Prefix in middle of current line
    _setDocText("foo();\n bar(); } foo();");
    assertTrue("before brace", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("after brace", !rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having prev line start with prefix, with text following */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("}bar();\nfoo();\nbar();");
    assertTrue("line of brace (no space)", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (no space)", rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("two lines after brace (no space)", !rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
    
    // Prefix plus text (with space)
    rule = new QuestionPrevLineStartsWith("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("just before star (with space)", !rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star (with space)", !rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star (with space)", rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having prev line start with prefix, with no text following */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n*\nbar();\n}");
    assertTrue("line of star (no space)", !rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after star (no space)", rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("two lines after star (no space)", !rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   * \nbar();\n{");
    assertTrue("line of star (with space)", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after star (with space)", !rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("line after star (with space)", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a multiple character prefix. */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("* ", null, null);
    
    // Multi-char prefix
    _setDocText("*\n *\n * \n * foo\nbar");
    assertTrue("star", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("space star", !rule.testApplyRule(_doc, 5, Indenter.IndentReason.OTHER));
    assertTrue("space star space", rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("space star space text", rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a commented prefix. */
  public void testCommentedPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n*/");
    assertTrue("just before star", !rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star", !rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("line after star", !rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /** Tests a prefix that begins a comment. */
  public void testCommentPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("/**", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n*/");
    assertTrue("just before star", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("line after star", !rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having text on a line before the prefix. */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in text, not starting line
    _setDocText("foo(); *\nbar();\n");
    assertTrue("line after star", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }


}
