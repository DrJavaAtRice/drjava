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

/** Tests the proper functionality of the class QuestionCurrLineStartsWithSkipComments.
  * @version $Id$
  */
public final class QuestionCurrLineStartsWithSkipCommentsTest extends IndentRulesTestCase {
  private String _text;
    
  private IndentRuleQuestion _rule;
  
  public void testNoPrefix() throws BadLocationException {
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

    IndentRuleQuestion rule = new QuestionCurrLineStartsWithSkipComments(new char[] {}, null, null);

    // This rule should always return false
    
    assertTrue("At 0.", ! rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At start of block.", ! rule.testApplyRule(_doc, 25, Indenter.IndentReason.OTHER));
//    System.err.println("****** Starting test that fails ******");
    assertTrue("START starts one-line comment.", ! rule.testApplyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("START starts one-line comment.", ! rule.testApplyRule(_doc, 60, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", ! rule.testApplyRule(_doc, 104, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", ! rule.testApplyRule(_doc, 110, Indenter.IndentReason.OTHER));
    assertTrue("Line inside javadoc comment.", ! rule.testApplyRule(_doc, 130, Indenter.IndentReason.OTHER));
    assertTrue("Line closes javadoc comment.", ! rule.testApplyRule(_doc, 150, Indenter.IndentReason.OTHER));
    assertTrue("START is free.", ! rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));
    assertTrue("START is free.", ! rule.testApplyRule(_doc, 230, Indenter.IndentReason.OTHER));
    assertTrue("START starts multi-line comment.", ! rule.testApplyRule(_doc, 260, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", ! rule.testApplyRule(_doc, 275, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", ! rule.testApplyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment.", ! rule.testApplyRule(_doc, 399, Indenter.IndentReason.OTHER));
    assertTrue("START is free.", ! rule.testApplyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document.", ! rule.testApplyRule(_doc, 401, Indenter.IndentReason.OTHER));
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

    _rule = new QuestionCurrLineStartsWithSkipComments(new char[] {'{'}, null, null);

    assertTrue("At 0 - line doesn't start with an open brace.",      !_rule.testApplyRule(_doc,   0, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a block, but not the start of the line.",       !_rule.testApplyRule(_doc,  25, Indenter.IndentReason.OTHER));
    assertTrue("Inside block - line starts with an alphanumeric character.",!_rule.testApplyRule(_doc,  30, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.testApplyRule(_doc,  54, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.testApplyRule(_doc,  60, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.testApplyRule(_doc,  80, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.testApplyRule(_doc, 104, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.testApplyRule(_doc, 110, Indenter.IndentReason.OTHER));
    assertTrue("Line inside javadoc comment.",                              !_rule.testApplyRule(_doc, 130, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",            !_rule.testApplyRule(_doc, 201, Indenter.IndentReason.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",            !_rule.testApplyRule(_doc, 221, Indenter.IndentReason.OTHER));
    assertTrue("At end of block - line starts with a close brace.",         !_rule.testApplyRule(_doc, 225, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a multi-line comment.",                         !_rule.testApplyRule(_doc, 260, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.testApplyRule(_doc, 275, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.testApplyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes comment. It follows an open brace.",            !_rule.testApplyRule(_doc, 325, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with a close brace.",                           !_rule.testApplyRule(_doc, 355, Indenter.IndentReason.OTHER));
    assertTrue("Empty line.",                                               !_rule.testApplyRule(_doc, 390, Indenter.IndentReason.OTHER));
    assertTrue("At last character - line starts with a close brace.",       !_rule.testApplyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document - line starts with a close brace.",      !_rule.testApplyRule(_doc, 401, Indenter.IndentReason.OTHER));
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
    
    _rule = new QuestionCurrLineStartsWithSkipComments(new char[] {'}'}, null, null);

    assertTrue("At 0 - line doesn't start with a close brace.",      !_rule.testApplyRule(_doc,   0, Indenter.IndentReason.OTHER));
    assertTrue("At start of block - line starts with an open brace.",       !_rule.testApplyRule(_doc,  25, Indenter.IndentReason.OTHER));
    assertTrue("Inside block - line starts with an open brace.",            !_rule.testApplyRule(_doc,  30, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.testApplyRule(_doc,  54, Indenter.IndentReason.OTHER));
    System.err.println("Should return false: " + _rule.testApplyRule(_doc,  60, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a one-line comment.",                           !_rule.testApplyRule(_doc,  60, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.testApplyRule(_doc,  80, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.testApplyRule(_doc, 104, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a javadoc comment.",                            !_rule.testApplyRule(_doc, 110, Indenter.IndentReason.OTHER));
    assertTrue("Line inside javadoc comment.",                              !_rule.testApplyRule(_doc, 130, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment, it follows a close brace.", !_rule.testApplyRule(_doc, 150, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with a comment, it follows a close brace.",     !_rule.testApplyRule(_doc, 221, Indenter.IndentReason.OTHER));
    assertTrue("At end of block - line starts with a slash.",               !_rule.testApplyRule(_doc, 225, Indenter.IndentReason.OTHER));
    assertTrue("Line starts a multi-line comment.",                         !_rule.testApplyRule(_doc, 260, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.testApplyRule(_doc, 275, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.testApplyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.",                           !_rule.testApplyRule(_doc, 325, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment, it follows a slash.",       !_rule.testApplyRule(_doc, 355, Indenter.IndentReason.OTHER));
    assertTrue("Line starts with a star.",                                  !_rule.testApplyRule(_doc, 376, Indenter.IndentReason.OTHER));
    assertTrue("At last character - line starts with a close brace.",       !_rule.testApplyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document - line starts with a close brace.",      !_rule.testApplyRule(_doc, 401, Indenter.IndentReason.OTHER));
  }
}
