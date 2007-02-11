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
 * of the class QuestionHasCharPrecedingOpenBrace.
 * @version $Id$
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

    assertTrue("At DOCSTART.", ! rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of array.", !rule.applyRule(_doc, 25, Indenter.IndentReason.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 60, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 104, Indenter.IndentReason.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 110, Indenter.IndentReason.OTHER));
    assertTrue("Line inside javadoc comment.", rule.applyRule(_doc, 130, Indenter.IndentReason.OTHER));
    assertTrue("Line closes javadoc comment.", rule.applyRule(_doc, 150, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", rule.applyRule(_doc, 180, Indenter.IndentReason.OTHER));
    assertTrue("Second pseudo array element.", ! rule.applyRule(_doc, 230, Indenter.IndentReason.OTHER));
    assertTrue("Start of multi-line comment.", !rule.applyRule(_doc, 260, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 275, Indenter.IndentReason.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.applyRule(_doc, 399, Indenter.IndentReason.OTHER));
    assertTrue("Last close brace", !rule.applyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document.", !rule.applyRule(_doc, 401, Indenter.IndentReason.OTHER));
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

    assertTrue("At DOCSTART.", ! rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of outer array", !rule.applyRule(_doc, 25, Indenter.IndentReason.OTHER));

    assertTrue("Before start of inner array", rule.applyRule(_doc, 50, Indenter.IndentReason.OTHER));

    assertTrue("Same line as inner {.", rule.applyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("Line after inner {.", !rule.applyRule(_doc, 75, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", !rule.applyRule(_doc, 180, Indenter.IndentReason.OTHER));

    assertTrue("Second pseudo array element.",  rule.applyRule(_doc, 230, Indenter.IndentReason.OTHER));
    assertTrue("In multi-line comment.", ! rule.applyRule(_doc, 260, Indenter.IndentReason.OTHER));

    assertTrue("multi-line comment w/ = {.",  ! rule.applyRule(_doc, 275, Indenter.IndentReason.OTHER));

    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300, Indenter.IndentReason.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.applyRule(_doc, 399, Indenter.IndentReason.OTHER));

    assertTrue("Last close brace",  rule.applyRule(_doc, 400, Indenter.IndentReason.OTHER));
    assertTrue("At end of document.",  rule.applyRule(_doc, 401, Indenter.IndentReason.OTHER));
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

    assertTrue("At DOCSTART.",    ! rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
    assertTrue("At start of outer array", !rule.applyRule(_doc, 25, Indenter.IndentReason.OTHER));

    assertTrue("Before start of inner array", ! rule.applyRule(_doc, 50, Indenter.IndentReason.OTHER));
    assertTrue("Same line as inner {.", !rule.applyRule(_doc, 54, Indenter.IndentReason.OTHER));
    assertTrue("Line after inner {.", !rule.applyRule(_doc, 75, Indenter.IndentReason.OTHER));
    assertTrue("START is stil in first.", !rule.applyRule(_doc, 180, Indenter.IndentReason.OTHER));

  }
}
