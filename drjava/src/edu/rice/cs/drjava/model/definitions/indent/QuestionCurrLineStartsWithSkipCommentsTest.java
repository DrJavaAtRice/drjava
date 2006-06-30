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
 * Test class according to the JUnit protocol. Tests the proper functionality
 * of the class QuestionCurrLineStartsWithSkipComments.
 * @version $Id$
 */
public final class QuestionCurrLineStartsWithSkipCommentsTest extends IndentRulesTestCase
{
  private String _text;
    
  private IndentRuleQuestion _rule;
  
  public void testNoPrefix() throws BadLocationException
  {
    _text =
      "class A                 \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    // one line comment \n" + /*  50 */
      "    int method1         \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    int method()        \n" + /* 175 */
      "    {                   \n" + /* 200 */
      "    }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */                  \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);

    IndentRuleQuestion rule = new QuestionCurrLineStartsWithSkipComments("", null, null);

    // This rule should always apply, unless the entire line is inside a comment.
    
    assertTrue("At DOCSTART.", rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("At start of block.", rule.applyRule(_doc, 25, Indenter.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 54, Indenter.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 60, Indenter.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 104, Indenter.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 110, Indenter.OTHER));
    assertTrue("Line inside javadoc comment.", !rule.applyRule(_doc, 130, Indenter.OTHER));
    assertTrue("Line closes javadoc comment.", rule.applyRule(_doc, 150, Indenter.OTHER));
    assertTrue("START is free.", rule.applyRule(_doc, 180, Indenter.OTHER));
    assertTrue("START is free.", rule.applyRule(_doc, 230, Indenter.OTHER));
    assertTrue("START starts multi-line comment.", rule.applyRule(_doc, 260, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 275, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300, Indenter.OTHER));
    assertTrue("Line closes multi-line comment.", rule.applyRule(_doc, 399, Indenter.OTHER));
    assertTrue("START is free.", rule.applyRule(_doc, 400, Indenter.OTHER));
    assertTrue("At end of document.", rule.applyRule(_doc, 401, Indenter.OTHER));
  }

  public void testOpenBracePrefix() throws BadLocationException
  {
    _text =
      "class A extends         \n" + /*   0 */
      "B {                     \n" + /*  25 */
      "    // {        }       \n" + /*  50 */
      "    int field;          \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * {        }       \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    int method() /*     \n" + /* 175 */
      " */ {                   \n" + /* 200 */
      "    }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "/**stuff*/   {  // stuff\n" + /* 325 */
      "             }          \n" + /* 350 */
      "                        \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);

    _rule = new QuestionCurrLineStartsWithSkipComments("{", null, null);

    assertTrue("At DOCSTART - line doesn't start with an open brace.",      !_rule.applyRule(_doc,   0, Indenter.OTHER));
    assertTrue("Line starts a block, but not the start of the line.",       !_rule.applyRule(_doc,  25, Indenter.OTHER));
    assertTrue("Inside block - line starts with an alphanumeric character.",!_rule.applyRule(_doc,  30, Indenter.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  54, Indenter.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  60, Indenter.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc,  80, Indenter.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 104, Indenter.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 110, Indenter.OTHER));
    assertTrue("Line inside javadoc comment.",                              !_rule.applyRule(_doc, 130, Indenter.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc, 180, Indenter.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 201, Indenter.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 221, Indenter.OTHER));
    assertTrue("At end of block - line starts with a close brace.",         !_rule.applyRule(_doc, 225, Indenter.OTHER));
    assertTrue("Line starts a multi-line comment.",                         !_rule.applyRule(_doc, 260, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 275, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 300, Indenter.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 325, Indenter.OTHER));
    assertTrue("Line starts with a close brace.",                           !_rule.applyRule(_doc, 355, Indenter.OTHER));
    assertTrue("Empty line.",                                               !_rule.applyRule(_doc, 390, Indenter.OTHER));
    assertTrue("At last character - line starts with a close brace.",       !_rule.applyRule(_doc, 400, Indenter.OTHER));
    assertTrue("At end of document - line starts with a close brace.",      !_rule.applyRule(_doc, 401, Indenter.OTHER));
  }
    
  public void testCloseBracePrefix() throws BadLocationException
  {
    _text =
      "class A                 \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    // }         }      \n" + /*  50 */
      "    int field;          \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */   }             \n" + /* 150 */
      "    int method()        \n" + /* 175 */
      "/**/}                   \n" + /* 200 */
      "/ * }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "*/ / }                  \n" + /* 350 */
      "   * }                  \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);
    
    _rule = new QuestionCurrLineStartsWithSkipComments("}", null, null);

    assertTrue("At DOCSTART - line doesn't start with a close brace.",      !_rule.applyRule(_doc,   0, Indenter.OTHER));
    assertTrue("At start of block - line starts with an open brace.",       !_rule.applyRule(_doc,  25, Indenter.OTHER));
    assertTrue("Inside block - line starts with an open brace.",            !_rule.applyRule(_doc,  30, Indenter.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  54, Indenter.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  60, Indenter.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc,  80, Indenter.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 104, Indenter.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 110, Indenter.OTHER));
    assertTrue("Line inside javadoc comment.",                              !_rule.applyRule(_doc, 130, Indenter.OTHER));
    assertTrue("Line closes multi-line comment, it follows a close brace.",  _rule.applyRule(_doc, 150, Indenter.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc, 180, Indenter.OTHER));
    assertTrue("Line starts with a comment, it follows a close brace.",      _rule.applyRule(_doc, 221, Indenter.OTHER));
    assertTrue("At end of block - line starts with a slash.",               !_rule.applyRule(_doc, 225, Indenter.OTHER));
    assertTrue("Line starts a multi-line comment.",                         !_rule.applyRule(_doc, 260, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 275, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 300, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 325, Indenter.OTHER));
    assertTrue("Line closes multi-line comment, it follows a slash.",       !_rule.applyRule(_doc, 355, Indenter.OTHER));
    assertTrue("Line starts with a star.",                                  !_rule.applyRule(_doc, 376, Indenter.OTHER));
    assertTrue("At last character - line starts with a close brace.",        _rule.applyRule(_doc, 400, Indenter.OTHER));
    assertTrue("At end of document - line starts with a close brace.",       _rule.applyRule(_doc, 401, Indenter.OTHER));
  }
}
