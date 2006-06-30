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
 * Tests the action rule which does nothing to the given text.
 * @version $Id$
 */
public final class ActionDoNothingTest extends IndentRulesTestCase {


  /**
   * Tests empty string
   */
  public void testEmptyString() throws BadLocationException {
    IndentRuleAction rule = new ActionDoNothing();
    String text = "";
    
    _setDocText(text);
    rule.indentLine(_doc, 0, Indenter.OTHER);
    _assertContents(text);
  }
  
  /**
   * Tests no leading spaces
   */
  public void testNoLeadingSpaces() throws BadLocationException {
    IndentRuleAction rule = new ActionDoNothing();
    String text = "foo();\nbar();";
    
    // First line
    _setDocText(text);
    rule.indentLine(_doc, 0, Indenter.OTHER);
    _assertContents(text);
    
    // Second line
    rule.indentLine(_doc, 9, Indenter.OTHER);
    _assertContents(text);
  }

  /**
   * Tests leading spaces
   */
  public void testLeadingSpaces() throws BadLocationException {
    IndentRuleAction rule = new ActionDoNothing();
    String text = "  foo();\n     bar();";
    
    // First line
    _setDocText(text);
    rule.indentLine(_doc, 0, Indenter.OTHER);
    _assertContents(text);
    
    // Second line
    rule.indentLine(_doc, 10, Indenter.OTHER);
    _assertContents(text);
  }

}
