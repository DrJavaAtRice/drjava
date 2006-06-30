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

/**
 * Test class for an indent rule with a really long name.  :-)
 * Inherits from ActionStartPrevLinePlusTest, since this rule's functionality
 * should be a strict extension of ActionStartPrevLinePlus.
 * @version $Id$
 */
public class ActionStartPrevLinePlusBackupTest extends ActionStartPrevLinePlusTest {
  
  /**
   * Factory to enable reuse of methods from ActionStartPrevLinePlusTest.
   * This creates an action that should behave identically to an instance of
   * ActionStartPrevLinePlus.
   * @param suffix the text to be added by this rule after indent padding
   * @see ActionStartPrevLinePlus#ActionStartPrevLinePlus(String)
   */
  private IndentRuleAction makeAction(String suffix) {
    return new ActionStartPrevLinePlusBackup(suffix, suffix.length());
  }
  
  /**
   * Factory to enable reuse of methods from ActionStartPrevLinePlusBackupTest.
   * This works similarly to {@link #makeAction(String)}.
   * @param suffix the text to be added by this rule after indent padding
   * @param position the character within the suffix string before which to
   * place the cursor
   * @see ActionStartPrevLinePlusBackup#ActionStartPrevLinePlusBackup(String, int)
   */
  private IndentRuleAction makeBackupAction(String suffix, int position) {
    return new ActionStartPrevLinePlusBackup(suffix, position);
  }
  
  private String _noIndent = "foo\nbar";
  private String _evenIndent = "  foo\n  bar";
  private String _unevenIndent = "  foo\nbar";
  private String _noIndentRes = "foo\nabc bar";
  private String _evenIndentRes = "  foo\n  abc bar";
  private String _unevenIndentRes = "  foo\n  abc bar";
  
  /**
   * Attempts to move current location to the start of the suffix.
   */
  public void testMoveToStart() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 7, 0, 4);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 11, 0, 8);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 9, 0, 8);
  }
  
  /**
   * Attempts to move current location to the end of the suffix.
   */
  public void testMoveToEnd() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 4, 4, 8);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 6, 4, 12);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 6, 4, 12);
  }
  
  /**
   * Attempts to move current location to the middle of the suffix.
   */
  public void testMoveToMiddle() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 4, 2, 6);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 6, 2, 10);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 6, 2, 10);
  }
  
  /**
   * Helper method for "MoveTo" tests.
   * @param text the test text
   * @param result the result text
   * @param deltaLen the change in text length
   * @param before location to set before indenting
   * @param position param to pass to makeBackupAction
   * @param after location to expect after indenting
   */
  private void moveTestHelper(String text, String result, int deltaLen,
                              int before, int position, int after)
      throws javax.swing.text.BadLocationException {
    _setDocText(text);
    _doc.setCurrentLocation(before);  // end of bar line
    
    String suffix = "abc ";
    makeBackupAction(suffix, position).indentLine(_doc, Indenter.OTHER);
    assertEquals("text length",
                 text.length() + deltaLen + suffix.length(),
                 _doc.getLength());
    assertEquals("text contents", result, _doc.getText());
    assertEquals("location", after, _doc.getCurrentLocation());
  }
}
