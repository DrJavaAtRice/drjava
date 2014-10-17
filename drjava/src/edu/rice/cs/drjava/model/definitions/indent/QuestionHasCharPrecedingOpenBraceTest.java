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
 * Test class according to the JUnit protocol. Tests the proper functionality
 * of the class QuestionHasCharPrecedingOpenBrace.
 * @version $Id: QuestionHasCharPrecedingOpenBraceTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class QuestionHasCharPrecedingOpenBraceTest extends IndentRulesTestCase
{
  private String _text;
    
//  private IndentRuleQuestion _rule;

  public void testIsIn1DArray() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a =            \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    a, //  line comment \n" + /*  50 */
      "    int b,              \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /*  {  multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At 0.", ! rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of array.", !rule.testApplyRule(_doc, 25, Indenter.IndentReason.OTHER));
    assertTrue("START starts one-line comment.", rule.testApplyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("START starts one-line comment.", rule.testApplyRule(_doc, 60, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", rule.testApplyRule(_doc, 104, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", rule.testApplyRule(_doc, 110, Indenter.IndentReason.OTHER));
    assertTrue("Line inside javadoc comment.", rule.testApplyRule(_doc, 130, Indenter.IndentReason.OTHER));
    assertTrue("Line closes javadoc comment.", rule.testApplyRule(_doc, 150, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));
    assertTrue("Second pseudo array element.", ! rule.testApplyRule(_doc, 230, Indenter.IndentReason.OTHER));
    assertTrue("Start of multi-line comment.", !rule.testApplyRule(_doc, 260, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.testApplyRule(_doc, 275, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.testApplyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.testApplyRule(_doc, 399, Indenter.IndentReason.OTHER));
    assertTrue("Last close brace", !rule.testApplyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document.", !rule.testApplyRule(_doc, 401, Indenter.IndentReason.OTHER));
  }
  public void testIsIn2DArray() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a =            \n" + /*   0 */
      "{                       \n" + /*  25 */
      "  {                     \n" + /*  50 */
      "    a, //  line comment \n" + /*  75 */
      "    int b,              \n" + /* 100 */
      "    /**                 \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /* = { multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}"                          + /* 400 */
      "";

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At 0.", ! rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of outer array", !rule.testApplyRule(_doc, 25, Indenter.IndentReason.OTHER));

    assertTrue("Before start of inner array", rule.testApplyRule(_doc, 50, Indenter.IndentReason.OTHER));

    assertTrue("Same line as inner {.", rule.testApplyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("Line after inner {.", !rule.testApplyRule(_doc, 75, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", !rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));

    assertTrue("Second pseudo array element.",  rule.testApplyRule(_doc, 230, Indenter.IndentReason.OTHER));
    assertTrue("In multi-line comment.", ! rule.testApplyRule(_doc, 260, Indenter.IndentReason.OTHER));

    assertTrue("multi-line comment w/ = {.",  ! rule.testApplyRule(_doc, 275, Indenter.IndentReason.OTHER));

    assertTrue("Line inside multi-line comment.", !rule.testApplyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.testApplyRule(_doc, 399, Indenter.IndentReason.OTHER));

    assertTrue("Last close brace",  rule.testApplyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document.",  rule.testApplyRule(_doc, 401, Indenter.IndentReason.OTHER));
  }
  public void testNoEquals() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a             \n" + /*   0 */
      "{                       \n" + /*  25 */
      "  {                     \n" + /*  50 */
      "    a, //  line comment \n" + /*  75 */
      "    int b,              \n" + /* 100 */
      "    /**                 \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /* = { multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}"                          + /* 400 */
      "";

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At 0.",    ! rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of outer array", !rule.testApplyRule(_doc, 25, Indenter.IndentReason.OTHER));

    assertTrue("Before start of inner array", ! rule.testApplyRule(_doc, 50, Indenter.IndentReason.OTHER));
    assertTrue("Same line as inner {.", !rule.testApplyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("Line after inner {.", !rule.testApplyRule(_doc, 75, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", !rule.testApplyRule(_doc, 180, Indenter.IndentReason.OTHER));

  }
}
