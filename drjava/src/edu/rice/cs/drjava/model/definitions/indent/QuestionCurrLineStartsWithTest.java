/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.BadLocationException;

/** * Tests the indention rule which detects whether the current line
 * starts with a particular string.
 * @version $Id$
 */
public final class QuestionCurrLineStartsWithTest extends IndentRulesTestCase {

  /** Tests not having the prefix in the text.  
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testNoPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace
    _setDocText("foo();\n}\n");
    assertTrue("no open brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("line of close brace (no open brace)", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("line after close brace (no open brace)", !rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    
    // Close brace
    rule = new QuestionCurrLineStartsWith("}", null, null);
    _setDocText("{\nfoo();");
    assertTrue("no close brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a line start with prefix, with text following
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testStartsWithPrefixWithText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
        
    // Prefix plus text (no space)
    _setDocText("foo();\n}bar();\n");
    assertTrue("line before brace (no space)", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace (no space)", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace (no space)", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
    assertTrue("line after brace (no space)", !rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
    
    // Prefix plus text (with space)
    rule = new QuestionCurrLineStartsWith("*", null, null);
    _setDocText("foo\n * comment\nbar");
    assertTrue("line before star (with space)", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before star (with space)", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star (with space)", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star (with space)", !rule.testApplyRule(_doc, 15, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having a line start with prefix, with no text following
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testStartsWithPrefixNoText() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
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
  
  /** Tests having a multiple character prefix.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testMultipleCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith(".*.", null, null);
    
    // Multi-char prefix
    _setDocText("*\n.*\n.*.\n.*.foo");
    assertTrue("star", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("dot star", !rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot", rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("dot star dot text", rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
  }
    
  /** Tests having a commented prefix without searching in comments.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testCommentedPrefixDontSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("{", null, null);
    
    // Open brace in comment
    _setDocText("foo();\n// {\nbar();\n");
    assertTrue("just before brace", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace", !rule.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
    assertTrue("line after brace", !rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
  }

  /** Tests having a commented prefix with searching in comments.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testCommentedPrefixSearchComment() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("*", null, null);
    
    // Star in comment
    _setDocText("/**\n* \ncomment\n");
    assertTrue("line before star", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before star", rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("just after star", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("line after star", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  
  /** Tests having text on a line before the prefix.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testDoesNotStartWithPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    // Close brace in text, not starting line
    _setDocText("foo(); }\nbar();\n");
    assertTrue("before brace", !rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("just before brace", !rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("just after brace", !rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("line after brace", !rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }

  /** Prefix appears at the end of a document.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testPrefixAtEnd() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("}", null, null);
    
    _setDocText("void foo() {\n}");
    assertTrue("first line", !rule.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
    assertTrue("end of first line", !rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("beginning of second line", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("end of second line", rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
  }
  
  /** Tests multiple-character prefix.
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public void testMultCharPrefix() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionCurrLineStartsWith("abcdefg", null, null);
    
    _setDocText("   abcdefghij\n  abcde");
    assertTrue("first line, beginning", rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("first line, mid", rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
    assertTrue("first line, end", rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
    assertTrue("second line, beginning", !rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("second line, mid", !rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("second line, end", !rule.testApplyRule(_doc, 21, Indenter.IndentReason.OTHER));
  }
}
