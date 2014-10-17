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

import java.util.ArrayList;

import javax.swing.text.BadLocationException;

/**
 * This class does almost all the work for keeping an indent tree trace.  IndentRuleQuestion
 * also does some of the work, and any subclass may substitute its own version of getRuleName()
 * This test is broken in DrScala.  TODO: fix it.
 * @version $Id: IndentRuleWithTraceTest.java 5751 2013-02-06 10:32:04Z rcartwright $
 */
public final class IndentRuleWithTraceTest extends IndentRulesTestCase {

  public void xtestTrace() throws BadLocationException{
    IndentRuleWithTrace.setRuleTraceEnabled(true);
    IndentRule
      rule4 = new ActionBracePlus(2),
      rule3 = new QuestionBraceIsCurly(rule4, rule4),
      rule2 = new QuestionBraceIsBracket(rule3, rule3);
    IndentRuleQuestion 
      rule1 = new QuestionInsideComment(rule2, rule2);
    String text =
      "class foo {\n" +
      "/**\n" +
      " * This method does nothing\n" + 
      " */\n" +
      "def method1(){\n" +
      "}\n" +
      "}\n";

    
    _setDocText(text);
    rule1.indentLine(_doc, 23, Indenter.IndentReason.OTHER);
    rule1.indentLine(_doc, 75, Indenter.IndentReason.OTHER);

    String[] expected = {"edu.rice.cs.drjava.model.definitions.indent.QuestionInsideComment No",
    "edu.rice.cs.drjava.model.definitions.indent.QuestionBraceIsParenOrBracket No",
    "edu.rice.cs.drjava.model.definitions.indent.QuestionBraceIsCurly Yes",
    "edu.rice.cs.drjava.model.definitions.indent.ActionBracePlus "};

    ArrayList<String> actual = IndentRuleWithTrace.getTrace();
//    System.err.println("Trace is: " + actual);
    assertEquals("steps in trace", 4, actual.size());
    for(int x = 0; x < actual.size(); x++) {
      assertEquals("check trace step " + x, expected[x], actual.get(x));
    }
  }
}
