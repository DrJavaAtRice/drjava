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
 * Test class according to the JUnit protocol. Tests the question
 * that determines whether or not the last block or expression list 
 * opened previous to the start of the current line was opened 
 * by the character '{'. 
 * This questions corresponds to rule 15 in our decision tree.
 * @version $Id: QuestionBraceIsCurlyTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class QuestionBraceIsCurlyTest extends IndentRulesTestCase {
  // PRE: we are not inside a multiline comment
  // PRE: the current block or expression list was *not* 
  //      opened by '[' or '('.
  
  private String _text;
  
  private final IndentRuleQuestion _rule = new QuestionBraceIsCurly(null, null);
  
  public void testWithParen() throws BadLocationException {
    int i;
    
    /* (1) */
    
    _text = "boolean method(int[] a, String b) {}";
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", ! _rule.testApplyRule(_doc, i, Indenter.IndentReason.OTHER));
      
    /* (2) */
    
    _text = 
      "boolean method() {\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", ! _rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is curly brace.", _rule.testApplyRule(_doc, 19, Indenter.IndentReason.OTHER));
    
    /* (3) */
    
    _text = 
      "boolean method(\n" +
      "    int[] a, String b)\n" +
      "{}";
    
    _setDocText(_text);
    
    assertTrue("START is open curly brace.", ! _rule.testApplyRule(_doc, _text.length() - 2, Indenter.IndentReason.OTHER));
    assertTrue("START is open curly brace.", ! _rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
    
    /* (4) */
    
    _text = 
      "if (<cond>) {\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    assertTrue("START's brace is open curly brace.", _rule.testApplyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is open curly brace.", _rule.testApplyRule(_doc, 22, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open paren.", !_rule.testApplyRule(_doc, 23, Indenter.IndentReason.OTHER));
    
    /* (5) */
    
    _text = 
      "array[\n" +
      "    new Listener() {\n" +
      "        method() {\n" +
      "        }\n" +
      "    }]";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is open bracket.", !_rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, 28, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, 30, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
  }
  
  public void testOnlyCurly() throws BadLocationException {
    /* (1) */
    
    _text =
      "{ /* block1 */ }\n" +
      "{ /* block2 */ }\n" +
      "{ /* block3 */ }";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 28, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 30, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
    
    /* (2) */
    
    _text =
      "{\n" +
      "    {\n" +
      "        {}\n" +
      "    }\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, 18, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, 19, Indenter.IndentReason.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
    
    /* (3) */
    
    _text =
      "class Foo {\n" +
      "}";
    _setDocText(_text);
    
    assertTrue("Close brace immediately after open brace.", _rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    
    /* (4) */
    
    _text =
      "class Foo {\n" +
      "  method m()\n" +
      "  {\n" +
      "  }\n" +
      "}";
    _setDocText(_text);
    
    assertTrue("Close brace immediately after open brace.", _rule.testApplyRule(_doc, 29, Indenter.IndentReason.OTHER));
  }
  
   public void testEmbeddedBraceForms() throws BadLocationException {
    /* (1) */
    
    _text =
      "Foo f1 = x,\n" +
      "  f2 = new int[]{1, 2, 3},\n" +
      "  f3 = y;";
     
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, 22, Indenter.IndentReason.OTHER));
//    assertTrue("START has brace.", _rule.applyRule(_doc, 32, Indenter.IndentReason.OTHER));
//    assertTrue("START has brace.", _rule.applyRule(_doc, 40, Indenter.IndentReason.OTHER));
    assertTrue("START has no brace.", !_rule.testApplyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER));
    
    /* (2) */
    
  }
}

