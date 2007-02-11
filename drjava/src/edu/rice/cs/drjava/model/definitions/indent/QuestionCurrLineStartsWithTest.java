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
 * Tests the indention rule which detects whether the current line
 * starts with a particular string.
 * @version $Id$
 */
public final class QuestionCurrLineStartsWithTest extends IndentRulesTestCase {

  /** Tests not having the prefix in the text. */
  public void testNoPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("foo();\n}\n");
    assertTrue("no open brace", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("line of close brace (no open brace)", !rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("line after close brace (no open brace)", !rule.applyRule(_doc, 8, Indenter.IndentReason.OTHER));
    
    // Close brace
    rule = new QuestionCurrLineStartsWith("}", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no close brace", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
  }
  
  /**
   * Tests having a line start with prefix, with text following
   */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("foo();\n}bar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 15, Indenter.IndentReason.OTHER));
    
    // Prefix plus text (with space)
    rule = new QuestionCurrLineStartsWith("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("line before star (with space)", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before star (with space)", rule.applyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star (with space)", rule.applyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star (with space)", !rule.applyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /**
   * Tests having a line start with prefix, with no text following
   */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n{\nbar();\n");
    assertTrue("line before brace (no space)", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (no space)", rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (no space)", rule.applyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (no space)", !rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   {\nbar();\n");
    assertTrue("line before brace (with space)", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (with space)", rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (with space)", rule.applyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (with space)", !rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
  }
  
  /**
   * Tests having a multiple character prefix.
   */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith(".*.", null, null);
    
    // Multi-char prefix
    _setDocText("*\n.*\n.*.\n.*.foo");
    assertTrue("star", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("dot star", !rule.applyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot", rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot text", rule.applyRule(_doc, 9, Indenter.IndentReason.OTHER));
  }
    
  /**
   * Tests having a commented prefix without searching in comments.
   */
  public void testCommentedPrefixDontSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace in comment
    _setDocText("foo();\n// {\nbar();\n");
    assertTrue("just before brace", !rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace", !rule.applyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("line after brace", !rule.applyRule(_doc, 12, Indenter.IndentReason.OTHER));
  }

  /**
   * Tests having a commented prefix with searching in comments.
   */
  public void testCommentedPrefixSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n");
    assertTrue("line before star", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before star", rule.applyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star", rule.applyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star", !rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  
  /**
   * Tests having text on a line before the prefix.
   */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    // Close brace in text, not starting line
    _setDocText("foo(); }\nbar();\n");
    assertTrue("before brace", !rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace", !rule.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace", !rule.applyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after brace", !rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }

  /**
   * Prefix appears at the end of a document.
   */
  public void testPrefixAtEnd() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    _setDocText("void foo() {\n}");
    assertTrue("first line", !rule.applyRule(_doc, 3, Indenter.IndentReason.OTHER));
    assertTrue("end of first line", !rule.applyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("beginning of second line", rule.applyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("end of second line", rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
  }
  
  /**
   * Tests multiple-character prefix.
   */
  public void testMultCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("abcdefg", null, null);
    
    _setDocText("   abcdefghij\n  abcde");
    assertTrue("first line, beginning", rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("first line, mid", rule.applyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("first line, end", rule.applyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("second line, beginning", !rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("second line, mid", !rule.applyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("second line, end", !rule.applyRule(_doc, 21, Indenter.IndentReason.OTHER));
  }
}