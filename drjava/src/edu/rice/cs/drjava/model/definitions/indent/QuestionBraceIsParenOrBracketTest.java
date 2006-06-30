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
 * by one of the characters '(' or '['.
 * This questions corresponds to rule 11 in our decision tree.
 * @version $Id$
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
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (2) */
    _text =
      "boolean method\n" +
      "    (int[] a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (3) */
    _text =
      "boolean method(\n" +
      "    int[] a, String b)";
    
    _setDocText(_text);
    
    for (i = 0; i < 16; i++)
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    // For any position past the '\n' character, the rule applies:
    
    for (i = 16; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (4) */
    
    _text =
      "if (<cond>) {\n" +
      "    if (\n" +
      "        <cond>) { ... }}";
    
    _setDocText(_text);
    
    for (i = 0; i < 23; i++)
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    // For any position past the second '\n' character, the rule applies:
    
    for (i = 23; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (5) */
    
    _text =
      "method(\n" +
      "       array1, foo(array1[x]))\n" +
      " <other stuff>";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace", !_rule.applyRule(_doc, 7, Indenter.OTHER));
    assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, 30, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
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
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    // For any position past the first '\n' character, the rule applies:
    
    for (i = 20; i < 29; i++)
      assertTrue("START's brace is an open bracket.", _rule.applyRule(_doc, i, Indenter.OTHER));
    
    for (i = 29; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (2) */
    
    _text = "array1[i]\n" +
      "       [j]";
    
    _setDocText(_text);
    
    for (i = 0; i < _text.length(); i++)
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    /* (3) */
    
    _text =
      "array1[\n" +
      "           i][\n" +
      "              j]";
    
    _setDocText(_text);
    
    assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 8, Indenter.OTHER));
    assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 22, Indenter.OTHER));
    assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 23, Indenter.OTHER));
  }
  
  public void testCurly() throws BadLocationException
  {
    /* (1) */
    
    _text =
      "class X extends Base\n" +
      "{\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 20, Indenter.OTHER));
    assertTrue("START is curly brace.", !_rule.applyRule(_doc, 21, Indenter.OTHER));
    assertTrue("START is close brace.", !_rule.applyRule(_doc, 23, Indenter.OTHER));
    
    /* (2) */
    
    _text =
      "class X extends Base\n" +
      "{\n" +
      "    int bla() { return 44; }\n" +
      "}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 20, Indenter.OTHER));
    assertTrue("START is curly brace.", !_rule.applyRule(_doc, 21, Indenter.OTHER));
    assertTrue("START's brace is curly brace.", !_rule.applyRule(_doc, 23, Indenter.OTHER));
    assertTrue("START is close curly brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
    
    /* (3) */
    
    _text =
      "class X extends Base\n" +
      "{}\n" +
      "class Y extends Base\n" +
      "{}";
    
    _setDocText(_text);
    
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 20, Indenter.OTHER));
    assertTrue("START is open curly brace.", !_rule.applyRule(_doc, 21, Indenter.OTHER));
    assertTrue("START has no brace.", !_rule.applyRule(_doc, 24, Indenter.OTHER));
    assertTrue("START's brace is open curly brace.", !_rule.applyRule(_doc, _text.length() - 1, Indenter.OTHER));
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
      assertTrue("START has no brace.", !_rule.applyRule(_doc, i, Indenter.OTHER));
    
    // For any position past the first '\n' character, the rule applies:
    
    for (i = 7; i < 27; i++)
      assertTrue("START's brace is an open bracket.", _rule.applyRule(_doc, i, Indenter.OTHER));
    
    for (i = 27; i < _text.length(); i++)
      assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i, Indenter.OTHER));
  }
}






