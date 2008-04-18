/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/



package edu.rice.cs.drjava.model.definitions.indent;

/**
 * Test class for an indent rule with a really long name.  :-)
 * Inherits from ActionStartPrevLinePlusTest, since this rule's functionality
 * should be a strict extension of ActionStartPrevLinePlus.
 * @version $Id$
 */
public class ActionStartPrevLinePlusBackupTest extends ActionStartPrevLinePlusTest {
  
  /** Factory to enable reuse of methods from ActionStartPrevLinePlusTest.
   * This creates an action that should behave identically to an instance of
   * ActionStartPrevLinePlus.
   * @param suffix the text to be added by this rule after indent padding
   * @see ActionStartPrevLinePlus#ActionStartPrevLinePlus(String)
   */
  private IndentRuleAction makeAction(String suffix) {
    return new ActionStartPrevLinePlusBackup(suffix, suffix.length());
  }
  
  /** Factory to enable reuse of methods from ActionStartPrevLinePlusBackupTest.
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
  
  /** Attempts to move current location to the start of the suffix.
   */
  public void testMoveToStart() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 7, 0, 4);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 11, 0, 8);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 9, 0, 8);
  }
  
  /** Attempts to move current location to the end of the suffix.
   */
  public void testMoveToEnd() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 4, 4, 8);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 6, 4, 12);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 6, 4, 12);
  }
  
  /** Attempts to move current location to the middle of the suffix.
   */
  public void testMoveToMiddle() throws javax.swing.text.BadLocationException {
    moveTestHelper(_noIndent, _noIndentRes, 0, 4, 2, 6);
    moveTestHelper(_evenIndent, _evenIndentRes, 0, 6, 2, 10);
    moveTestHelper(_unevenIndent, _unevenIndentRes, 2, 6, 2, 10);
  }
  
  /** Helper method for "MoveTo" tests.
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
    makeBackupAction(suffix, position).indentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals("text length",
                 text.length() + deltaLen + suffix.length(),
                 _doc.getLength());
    assertEquals("text contents", result, _doc.getText());
    assertEquals("location", after, _doc.getCurrentLocation());
  }
}
