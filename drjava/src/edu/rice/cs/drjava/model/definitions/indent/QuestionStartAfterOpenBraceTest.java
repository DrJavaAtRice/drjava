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

/** Tests whether the closest non-whitespace character preceding the start of the current line (excluding any 
  * characters inside comments or strings) is an open brace (paren or curly).
  * @version $Id: QuestionStartAfterOpenBraceTest.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
public final class QuestionStartAfterOpenBraceTest extends IndentRulesTestCase {
  private String _text;
  
  private IndentRuleQuestion _rule = new QuestionStartAfterOpenBrace(null, null);
  
  public void testNoBrace() throws BadLocationException {
    _text = "def method(\na: Int, b: String) {\n}\n";
    _setDocText(_text);
    assertTrue("START has no preceding brace.", ! _rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open paren", 
               _rule.applyRule(_doc, 12, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open brace.", 
               _rule.applyRule(_doc, 33, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows a closed curly brace.", 
               ! _rule.applyRule(_doc, _text.length(), Indenter.IndentReason.OTHER));
  }
  
  public void testRightAfterBrace() throws BadLocationException {
    
    _text = 
      "boolean method() {\n" +
      "}";
    
    _setDocText(_text);
    assertTrue("START immediately follows an open brace.", _rule.applyRule(_doc, 19, Indenter.IndentReason.OTHER));
    
    _text = 
      "boolean method(\n" +
      "    int[] a, String b)\n" +
      "{\n" +
      "}";
    
    _setDocText(_text); 
    assertTrue("START immediately follows an open paren.", !_rule.applyRule(_doc, 40, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open brace.", _rule.applyRule(_doc, 41, Indenter.IndentReason.OTHER));
  }
  
  public void testWSAfterBrace() throws BadLocationException {
    
    _text = 
      "if (<cond>) {\n" +
      "\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    assertTrue("START immediatly follows an open brace.", _rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between open brace and START.", _rule.applyRule(_doc, 15, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open paren", _rule.applyRule(_doc, 23, Indenter.IndentReason.OTHER));
    assertTrue("START still in shadow of first open brace", 
               _rule.applyRule(_doc, _text.length() - 1, Indenter.IndentReason.OTHER)); 
  }
  
  public void testCommentsAfterBrace() throws BadLocationException {
    
    _text = 
      "class Foo {   \n" +  // 15 chars
      "              \n" +  // 30 chars
      "  /*          \n" +  // 45 chars
      "   *          \n" +
      "   */         \n" +
      "  int field;  \n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START = 0.", !_rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START = 0.", !_rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 15, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 30, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 44, Indenter.IndentReason.OTHER));
    assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 45, Indenter.IndentReason.OTHER));
    assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 60, Indenter.IndentReason.OTHER));
    assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 77, Indenter.IndentReason.OTHER));
  }
  
  public void testBraceLastCharOnLine() throws BadLocationException {
    _setDocText("{\n");
    assertTrue("Brace only char on line.", _rule.applyRule(_doc, 2, Indenter.IndentReason.OTHER));
    
    _setDocText("void foo() {\n");
    assertTrue("Brace last char on line.", _rule.applyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
  
  public void testTextAfterBrace() throws BadLocationException {
    _setDocText("{ hello\n  foo();");
    assertTrue("Text on line after brace.", _rule.applyRule(_doc, 8, Indenter.IndentReason.OTHER));
  }
}

