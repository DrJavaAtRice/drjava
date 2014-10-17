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

/** Tests the rule QuestionCurrLineStartsWith which determines whether the current line
  * starts with a particular string.
  * @version $Id: QuestionCurrLineStartsWithTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class QuestionCurrLineStartsWithTest extends IndentRulesTestCase {

  /** Tests not having the prefix in the text. */
  public void testNoPrefix() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("{", null, null);
    
    // Open brace
    _setDocText("foo();\n}\n");
    assertTrue("no open brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("line of close brace (no open brace)", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("line after close brace (no open brace)", !rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    
    // Close brace
    rule = QuestionCurrLineStartsWith.newQuestion("}", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no close brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a line start with prefix, with text following */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("foo();\n}bar();\n");
    assertFalse("beginning of text", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (no space)", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("end-of-line after brace (no space)", rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertFalse("line after brace (no space)", rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
    
    // Prefix plus text (with space)
    rule = QuestionCurrLineStartsWith.newQuestion("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertFalse("beginning of text", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("before star (with space)", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just before star", rule.testApplyRule(_doc, 5, Indenter.IndentReason.OTHER));
    assertTrue("just after star", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertFalse("line after star (with space)", rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a line start with prefix, with no text following */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("{", null, null);
    
    // Prefix plus no text (no space)
    _setDocText("foo();\n{\nbar();\n");
    assertTrue("line before brace (no space)", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (no space)", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (no space)", rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (no space)", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    
    // Prefix plus no text (with space)
    _setDocText("foo();\n   {\nbar();\n");
    assertTrue("line before brace (with space)", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (with space)", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (with space)", rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (with space)", !rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a multiple character prefix. */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion(".*.", null, null);
    
    // Multi-char prefix
    _setDocText("*\n.*\n.*.\n.*.foo");
    assertTrue("star", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("dot star", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot text", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
  }
    
  /** Tests having a commented prefix without searching in comments. */
  public void testCommentedPrefixDontSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestionSkipComments("{", null, null);
    
    // Open brace in comment
    _setDocText("foo();\n// {\nbar();\n{");
    assertFalse("just before brace", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertFalse("just after brace", rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertFalse("line after brace", rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("second line after brace", rule.testApplyRule(_doc, 19, Indenter.IndentReason.OTHER));
  }

  /** Tests having a commented prefix with searching in comments. */
  public void testCommentedPrefixSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n");
//    System.err.println("*** Text is: '" + _doc.getText() + "'");
    assertFalse("line before star", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("just before first star", rule.testApplyRule(_doc, 1, Indenter.IndentReason.OTHER));
    assertTrue("just before star at beginning of line 2", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    
    assertTrue("just after star", rule.testApplyRule(_doc, 5, Indenter.IndentReason.OTHER));
    assertFalse("line after star", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having text on a line before the prefix. */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("}", null, null);
    
    // Close brace in text, not starting line
    _setDocText("foo(); }\nbar();\n  }");
    assertFalse("before brace", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("one space before brace", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertFalse("just before brace", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertFalse("just after brace", rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertFalse("line after brace", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertFalse("line after brace", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertFalse("end of line before 2nd brace", rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
    assertTrue("end of line before 2nd brace", rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }

  /** Prefix appears at the end of a document. */
  public void testPrefixAtEnd() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("}", null, null);
    
    _setDocText("void foo() {\n}\n");
//    System.err.println("text = \n" + _doc.getText());
    assertFalse("first line", rule.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
    assertFalse("end of first line", rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("beginning of second line", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("end of second line", rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertFalse("after second line", rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /** Tests multiple-character prefix. */
  public void testMultCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = QuestionCurrLineStartsWith.newQuestion("abcdefg", null, null);
    
    _setDocText("   abcdefghij\n  abcde");
    assertTrue("first line, beginning", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("first line after 'c'", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("first line, end", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertFalse("second line, beginning", rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertFalse("second line after 'b'", rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertFalse("second line, end", rule.testApplyRule(_doc, 21, Indenter.IndentReason.OTHER));
  }
}