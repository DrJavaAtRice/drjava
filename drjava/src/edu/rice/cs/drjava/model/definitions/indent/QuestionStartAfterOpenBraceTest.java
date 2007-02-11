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
 * Test class according to the JUnit protocol.
 * Tests the question determining whether or not the closest non-whitespace character
 * previous to the start of the current line (excluding any characters
 * inside comments or strings) is an open brace.
 *
 * @version $Id$
 */
public final class QuestionStartAfterOpenBraceTest extends IndentRulesTestCase 
{
  private String _text;
  
  private IndentRuleQuestion _rule = new QuestionStartAfterOpenBrace(null, null);
  
  public void testNoBrace() throws BadLocationException
  {
    _text = "method(\nint[] a, String b) {}";
    _setDocText(_text);
    assertTrue("START has no preceding brace.", !_rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open paren, not a brace.", !_rule.applyRule(_doc, 8, Indenter.IndentReason.OTHER));
    assertTrue("START immediately follows an open paren, not a brace.", !_rule.applyRule(_doc, _text.length()-1, Indenter.IndentReason.OTHER));
  }
  
  public void testRightAfterBrace() throws BadLocationException 
  {
    
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
  
  public void testWSAfterBrace() throws BadLocationException 
  {
    
    _text = 
      "if (<cond>) {\n" +
      "\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    assertTrue("START immediatly follows an open brace.", _rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between open brace and START.", _rule.applyRule(_doc, 15, Indenter.IndentReason.OTHER));
    assertTrue("Only WS between open brace and START.", _rule.applyRule(_doc, 23, Indenter.IndentReason.OTHER));
    assertTrue("START immediatly follows an open paren.", !_rule.applyRule(_doc, 25, Indenter.IndentReason.OTHER));
    
  }
  
  public void testCommentsAfterBrace() throws BadLocationException 
  {
    
    _text = 
      "class Foo {   \n" +
      "              \n" +
      "  /*          \n" +
      "   *          \n" +
      "   */         \n" +
      "  int field;  \n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START = DOCSTART.", !_rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("START = DOCSTART.", !_rule.applyRule(_doc, 14, Indenter.IndentReason.OTHER));
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

