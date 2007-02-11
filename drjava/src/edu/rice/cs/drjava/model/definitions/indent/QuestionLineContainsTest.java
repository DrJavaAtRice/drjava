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
 * Tests the question rule which determines if the current line
 * in the document contains the given character.
 * <p>
 * All tests check for the ':' character on the current line.
 *
 * @version $Id$
 */
public final class QuestionLineContainsTest extends IndentRulesTestCase {

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line of text containing a colon is detected.
   */
  public void testLineContainsColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // Colon in text
    _setDocText("return test ? x : y;\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("colon in text (after startdoc)",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(10);
    assertTrue("colon in text (after newline)",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(25);
    assertTrue("colon in text (after colon on line)",
        rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }    
  
  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line does not contain a colon.
   */
  public void testLineDoesNotContainColon() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
    
    // No colon in text
    _setDocText("foo();\nreturn test ? x : y;\n}\n");
    _doc.setCurrentLocation(6);
    assertTrue("no colon", !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(28);
    assertTrue("line of close brace (no colon in text)", !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line containing a commented out colon is identified as a
   * line that does not contain a colon.
   */
  public void testLineDoesNotContainColonDueToComments() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);

    // No colon, single line comment
    _setDocText("//case 1:\nreturn test; //? x : y\n}\n");
    _doc.setCurrentLocation(0);
    assertTrue("entire line with colon in comment (no colon, single line comment)",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    _doc.setCurrentLocation(10);
    assertTrue("part of line with colon in comment (no colon, single line comment)",
        !rule.applyRule(_doc, Indenter.IndentReason.OTHER));

    // No colon, multi-line comment
    _setDocText("foo();\nreturn test; /*? x : y*/\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in multi-line comment", !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }

  /**
   * Ensures that a line containing a colon is detected.
   * Tests that a line containing a colon in quotes is identified as a
   * line that does not contain a colon.
   */
  public void testLineDoesNotContainColonDueToQuotes() throws BadLocationException {
    IndentRuleQuestion rule = new QuestionLineContains(':', null, null);
  
    // No colon, quotes
    _setDocText("foo();\nreturn \"total: \" + sum\n}\n");
    _doc.setCurrentLocation(7);
    assertTrue("no colon, colon in quotes", !rule.applyRule(_doc, Indenter.IndentReason.OTHER));
  }
}
