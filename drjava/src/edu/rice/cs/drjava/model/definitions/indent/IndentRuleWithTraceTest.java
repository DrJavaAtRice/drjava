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

import java.util.Vector;
import javax.swing.text.BadLocationException;

/**
 * This class does almost all the work for keeping an indent tree trace.  IndentRuleQuestion
 * also does some of the work, and any subclass may substitute its own version of getRuleName()
 * @version $Id$
 */
public final class IndentRuleWithTraceTest extends IndentRulesTestCase{

  public void testTrace() throws BadLocationException{
    IndentRuleWithTrace.setRuleTraceEnabled(true);
    IndentRule
      rule4 = new ActionBracePlus("  "),
      rule3 = new QuestionBraceIsCurly(rule4, rule4),
      rule2 = new QuestionBraceIsParenOrBracket(rule3, rule3);
    IndentRuleQuestion
      rule1 = new QuestionInsideComment(rule2, rule2);
    String text =
      "public class foo {\n" +
      "/**\n" +
      " * This method does nothing\n" + 
      " */\n" +
      "public void method1(){\n" +
      "}\n" +
      "}\n";

    _setDocText(text);
    rule1.indentLine(_doc, 23, Indenter.OTHER);
    rule1.indentLine(_doc, 75, Indenter.OTHER);

    String[] expected = {"edu.rice.cs.drjava.model.definitions.indent.QuestionInsideComment No",
    "edu.rice.cs.drjava.model.definitions.indent.QuestionBraceIsParenOrBracket No",
    "edu.rice.cs.drjava.model.definitions.indent.QuestionBraceIsCurly Yes",
    "edu.rice.cs.drjava.model.definitions.indent.ActionBracePlus "};

    Vector<String> actual = IndentRuleWithTrace.getTrace();
    assertEquals("steps in trace", 4, actual.size());
    for(int x = 0; x < actual.size(); x++) {
      assertEquals("check trace step " + x, expected[x], actual.get(x));
    }
  }
}
