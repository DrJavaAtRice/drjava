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
 * Test class according to the JUnit protocol. Tests the question
 * that determines whether or not the last block or expression list 
 * opened previous to the start of the current line was opened 
 * by the character '{'. 
 * This questions corresponds to rule 15 in our decision tree.
 * @version $Id$
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
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
      
    /* (2) */
    
    _text = 
      "boolean method() {\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 18, Indenter.OTHER));
    assertTrue("START's brace is curly brace.", _rule.applyRule(_doc, 19, Indenter.OTHER));
    
    /* (3) */
    
    _text = 
      "boolean method(\n" +
      "    int[] a, String b)\n" +
      "{}";
    
    _setDocText(_text);
    
    assertTrue("START is open curly brace.", !_rule.applyRule(_doc, _text.length() - 2, Indenter.OTHER));
    assertTrue("START is open curly brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
    
    /* (4) */
    
    _text = 
      "if (<cond>) {\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    assertTrue("START's brace is open curly brace.", _rule.applyRule(_doc, 14, Indenter.OTHER));
    assertTrue("START's brace is open curly brace.", _rule.applyRule(_doc, 22, Indenter.OTHER));
    assertTrue("START's brace is an open paren.", !_rule.applyRule(_doc, 23, Indenter.OTHER));
    
    /* (5) */
    
    _text = 
      "array[\n" +
      "    new Listener() {\n" +
      "        method() {\n" +
      "        }\n" +
      "    }]";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START's brace is open bracket.", !_rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, 28, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, 30, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
  }
  
  public void testOnlyCurly() throws BadLocationException {
    /* (1) */
    
    _text =
      "{ /* block1 */ }\n" +
      "{ /* block2 */ }\n" +
      "{ /* block3 */ }";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 28, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 30, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
    
    /* (2) */
    
    _text =
      "{\n" +
      "    {\n" +
      "        {}\n" +
      "    }\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, 18, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, 19, Indenter.OTHER));
    assertTrue("START's brace is an open curly brace.", _rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
    
    /* (3) */
    
    _text =
      "class Foo {\n" +
      "}";
    _setDocText(_text);
    
    assertTrue("Close brace immediately after open brace.", _rule.applyRule(_doc, 12, Indenter.OTHER));
    
    /* (4) */
    
    _text =
      "class Foo {\n" +
      "  method m()\n" +
      "  {\n" +
      "  }\n" +
      "}";
    _setDocText(_text);
    
    assertTrue("Close brace immediately after open brace.", _rule.applyRule(_doc, 29, Indenter.OTHER));
  }
  
   public void testEmbeddedBraceForms() throws BadLocationException {
    /* (1) */
    
    _text =
      "Foo f1 = x,\n" +
      "  f2 = new int[]{1, 2, 3},\n" +
      "  f3 = y;";
     
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 12, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 22, Indenter.OTHER));
//    assertTrue("START has brace.", _rule.applyRule(_doc, 32, Indenter.OTHER));
//    assertTrue("START has brace.", _rule.applyRule(_doc, 40, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
    
    /* (2) */
    
  }
}

