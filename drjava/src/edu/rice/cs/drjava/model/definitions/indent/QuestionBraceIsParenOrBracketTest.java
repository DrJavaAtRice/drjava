/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

/**
 * Test class according to the JUnit protocol. Tests the question
 * that determines whether or not the last block or expression list
 * opened previous to the start of the current line was opened
 * by one of the characters '(' or '['.
 * This questions corresponds to rule 11 in our decision tree.
 * @version $Id: QuestionBraceIsParenOrBracketTest.java 5175 2010-01-20 08:46:32Z mgricken $
 */
public final class QuestionBraceIsParenOrBracketTest extends IndentRulesTestCase {
  // PRE: We are not inside a multiline comment.
  
  private String _text;
  
  private final IndentRuleQuestion _rule = new QuestionBraceIsParenOrBracket(null, null);
  
  public void testParen() throws BadLocationException {
    int i;
    
    /* (1) */
    _text = "boolean method(int[] a, String b)";
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (2) */
    _text =
      "boolean method\n" +
      "    (int[] a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (3) */
    _text =
      "boolean method(\n" +
      "    int[] a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < 16; i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    // For any position past the '\n' character, the rule applies:
    
    for (i = 16; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (4) */
    
    _text =
      "if (<cond>) {\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    for (i = 0; i < 23; i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    // For any position past the second '\n' character, the rule applies:
    
    for (i = 23; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (5) */
    
    _text =
      "method(\n" +
      "       array1, foo(array1[x]))\n" +
      " <other stuff>";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace", !_rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, 30, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
  }
  
  public void testBracket() throws BadLocationException
  {
    int i;
    
    /* (1) */
    
    _text =
      "boolean method(int[\n" +
      "                   ELTS]\n" +
      "               a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < 20; i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    // For any position past the first '\n' character, the rule applies:
    
    for (i = 20; i < 29; i++)
      assertTrue("START's brace is an open bracket.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    for (i = 29; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (2) */
    
    _text = "array1[i]\n" +
      "       [j]";
    
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    /* (3) */
    
    _text =
      "array1[\n" +
      "           i][\n" +
      "              j]";
    
    _setDocText(_text);
    
    assertTrue("START's paren is an open bracket.", _rule.testApplyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("START's paren is an open bracket.", _rule.testApplyRule(_doc, 22, Indenter.IndentReason.OTHER));
    assertTrue("START's paren is an open bracket.", _rule.testApplyRule(_doc, 23, Indenter.IndentReason.OTHER));
  }
  
  public void testCurly() throws BadLocationException
  {
    /* (1) */
    
    _text =
      "class X extends Base\n" +
      "{\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 20, Indenter.IndentReason.OTHER));
    assertTrue("START is curly brace.", !_rule.testApplyRule(_doc, 21, Indenter.IndentReason.OTHER));
    assertTrue("START is close brace.", !_rule.testApplyRule(_doc, 23, Indenter.IndentReason.OTHER));
    
    /* (2) */
    
    _text =
      "class X extends Base\n" +
      "{\n" +
      "    int bla() { return 44; }\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 20, Indenter.IndentReason.OTHER));
    assertTrue("START is curly brace.", !_rule.testApplyRule(_doc, 21, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is curly brace.", !_rule.testApplyRule(_doc, 23, Indenter.IndentReason.OTHER));
    assertTrue("START is close curly brace.", !_rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
    
    /* (3) */
    
    _text =
      "class X extends Base\n" +
      "{}\n" +
      "class Y extends Base\n" +
      "{}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 20, Indenter.IndentReason.OTHER));
    assertTrue("START is open curly brace.", !_rule.testApplyRule(_doc, 21, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 24, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is open curly brace.", !_rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
  }
  
  public void testBracketWithArrayComprehension() throws BadLocationException {
    int i;
    
    /* (1) */
    
    _text =
      "f(int[\n" +
      "                   ] {1, 2, 3},\n" +
      "               a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < 7; i++)
      assertTrue("START has no brace.", !_rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    // For any position past the first '\n' character, the rule applies:
    
    for (i = 7; i < 27; i++)
      assertTrue("START's brace is an open bracket.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
    
    for (i = 27; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
  }
}






