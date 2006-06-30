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
 * Tests the indention rule which detects whether the immediately previous line
 * starts with a particular string.
 * @version $Id$
 */
public final class QuestionPrevLineStartsWithTest extends IndentRulesTestCase {

  /** Tests not having the prefix in the text. */
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
  
  /** Tests hitting start of document. */
  public void testStartOfDocument() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("{", null, null);
    
    // Hits docstart
    _setDocText("\nfoo();");
    assertTrue("first line", !rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("second line", !rule.applyRule(_doc, 2, Indenter.OTHER));
  }
  
  /** Tests prefix on current line. */
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
  
  /** Tests having prev line start with prefix, with text following */
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
  
  /** Tests having prev line start with prefix, with no text following */
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
  
  /** Tests having a multiple character prefix. */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("* ", null, null);
    
    // Multi-char prefix
    _setDocText("*\n *\n * \n * foo\nbar");
    assertTrue("star", !rule.applyRule(_doc, 2, Indenter.OTHER));
    assertTrue("space star", !rule.applyRule(_doc, 5, Indenter.OTHER));
    assertTrue("space star space", rule.applyRule(_doc, 11, Indenter.OTHER));
    assertTrue("space star space text", rule.applyRule(_doc, 16, Indenter.OTHER));
  }
  
  /** Tests having a commented prefix. */
  public void testCommentedPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n*/");
    assertTrue("just before star", !rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star", !rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star", rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 15, Indenter.OTHER));
  }
  
  /** Tests a prefix that begins a comment. */
  public void testCommentPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("/**", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n*/");
    assertTrue("just before star", rule.applyRule(_doc, 4, Indenter.OTHER));
    assertTrue("just after star", rule.applyRule(_doc, 6, Indenter.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 15, Indenter.OTHER));
  }
  
  /** Tests having text on a line before the prefix. */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionPrevLineStartsWith("*", null, null);
    
    // Star in text, not starting line
    _setDocText("foo(); *\nbar();\n");
    assertTrue("line after star", !rule.applyRule(_doc, 10, Indenter.OTHER));
  }


}
