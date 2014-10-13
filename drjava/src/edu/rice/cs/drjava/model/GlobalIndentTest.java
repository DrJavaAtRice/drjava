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

package edu.rice.cs.drjava.model;

import javax.swing.text.BadLocationException;
import java.util.List;

import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.util.OperationCanceledException;

/**
 * Tests the indenting functionality on the level of the GlobalModel.
 * Not only are we testing that the document turns out right, but also
 * that the cursor position in the document is consistent with a standard.
 * @version $Id: GlobalIndentTest.java 5709 2012-08-30 05:11:09Z rcartwright $
 */
public final class GlobalIndentTest extends GlobalModelTestCase {
  private static final String FOO_EX_1 = "class Foo {\n";
  private static final String FOO_EX_2 = "val foo: Int\n";
  private static final String BAR_CALL_1 = "bar(monkey,\n";
  private static final String BAR_CALL_2 = "banana)\n";
//  private static final String BEAT_1 = "void beat(Horse dead,\n";
//  private static final String BEAT_2 = "          Stick pipe)\n";
  
  /** Tests that an indent increases the size of the tab when the cursor is at the start of the line.  If the cursor is
    * in the whitespace before the first word on a line, indent always moves the cursor up to the beginning of the first non-whitespace
    * character.
    * @throws BadLocationException
    */
  public void testIndentGrowTabAtStart() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.setCurrentLocation(FOO_EX_1.length());
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(FOO_EX_1.length() + 2, openDoc);
  }
  
  /** Tests indent that increases the size of the tab when the cursor is in the middle of the line.  
    * The cursor stays in the same place.
    * @throws BadLocationException
    */
  public void testIndentGrowTabAtMiddle() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.setCurrentLocation(FOO_EX_1.length() + 5);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(FOO_EX_1.length() + 6, openDoc);
  }
  
  /** Tests that an indent increases the size of the tab when the cursor is at the end of the line.  The cursor stays
    * in the same place.
    * @throws BadLocationException
    */
  public void testIndentGrowTabAtEnd() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.setCurrentLocation(openDoc.getLength() - 1);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(openDoc.getLength() - 1, openDoc);
  }
  
  /** Tests that an indent increases the size of the tab when the cursor is at the start of the line.  If the cursor
    * is in  whitespace before the first word on a line, an indent moves the cursor to the beginning of the first 
    * non-whitespace character.
    * @throws BadLocationException
    */
  public void testIndentShrinkTabAtStart() throws BadLocationException, OperationCanceledException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.setCurrentLocation(FOO_EX_1.length());
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(FOO_EX_1.length() + 2, openDoc);
  }
  
  /** Tests that an indent increases the size of the tab when the cursor is in the middle of the line.  The cursor stays
    * in the same place.
    * @throws BadLocationException
    */
  public void testIndentShrinkTabAtMiddle() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.setCurrentLocation(FOO_EX_1.length() + 5);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(FOO_EX_1.length() + 4, openDoc);
  }
  
  /** Tests that an indent increases the size of the tab when the cursor is at the end of the line.  The cursor stays
    * in the same place.
    * @throws BadLocationException
    */
  public void testIndentShrinkTabAtEnd()
    throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_1, null);
    openDoc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.setCurrentLocation(openDoc.getLength() - 1);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, openDoc);
    _assertLocation(openDoc.getLength() - 1, openDoc);
  }
  
  /** Tests that an indent matches up with the indent on the line above. The cursor is at the start of the line.
    * @exception BadLocationException
    */
  public void testIndentSameAsLineAboveAtStart() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_2, null);
    openDoc.insertString(FOO_EX_2.length(), "   " + FOO_EX_2, null);
    openDoc.setCurrentLocation(FOO_EX_2.length());
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_2 + FOO_EX_2, openDoc);
    _assertLocation(FOO_EX_2.length(), openDoc);
  }
  
  /** Tests that an indent matches up with the indent on the line above. The cursor is at the end of the line.
    * @exception BadLocationException
    */
  public void testIndentSameAsLineAboveAtEnd() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_2, null);
    openDoc.insertString(FOO_EX_2.length(), "   " + FOO_EX_2, null);
    openDoc.setCurrentLocation(openDoc.getLength() - 1);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_2 + FOO_EX_2, openDoc);
    _assertLocation(openDoc.getLength() - 1, openDoc);
  }
  
  /** Do an indent that follows the behavior in line with parentheses.
   * The cursor is at the start of the line.
   * @exception BadLocationException
   */
  public void testIndentInsideParenAtStart() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, BAR_CALL_1, null);
    openDoc.insertString(BAR_CALL_1.length(), BAR_CALL_2, null);
    openDoc.setCurrentLocation(BAR_CALL_1.length());
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(BAR_CALL_1 + "    " + BAR_CALL_2, openDoc);
    _assertLocation(BAR_CALL_1.length() + 4, openDoc);
  }
  
  /** Do an indent that follows the behavior in line with parentheses. The cursor is at the end of the line.
    * @exception BadLocationException
    */
  public void testIndentInsideParenAtEnd() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, BAR_CALL_1, null);
    openDoc.insertString(BAR_CALL_1.length(), BAR_CALL_2, null);
    openDoc.setCurrentLocation(openDoc.getLength() - 1);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(BAR_CALL_1 + "    " + BAR_CALL_2, openDoc);
    _assertLocation(openDoc.getLength() - 1, openDoc);
  }
  
  /** Indent does nothing to change the document when everything is in place. */
  public void testIndentDoesNothing() throws BadLocationException, OperationCanceledException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    
    openDoc.insertString(0, FOO_EX_2 + FOO_EX_2, null);
    openDoc.setCurrentLocation(openDoc.getLength() - 1);
    int loc = openDoc.getCurrentLocation();
    openDoc.indentLines(loc, loc, Indenter.IndentReason.OTHER, null);
    _assertContents(FOO_EX_2 + FOO_EX_2, openDoc);
    _assertLocation(openDoc.getLength() - 1, openDoc);
  }
  
  
  /** The quintessential "make the curly go to the start, even though
   * method arguments extend over two lines" test.  This behavior is not
   * correctly followed yet, so until it is, leave this method commented.
   * @exception BadLocationException
   *
   public void testIndentCurlyAfterTwoLines()
   throws BadLocationException, OperationCanceledException {
   OpenDefinitionsDocument openDoc = _getOpenDoc();
   
   openDoc.insertString(0, BEAT_1, null);
   openDoc.insertString(BEAT_1.length(), BEAT_2, null);
   openDoc.insertString(openDoc.getLength(), "{", null);
   int loc = openDoc.getCurrentLocation();
   openDoc.indentLines(loc, loc);
   _assertContents(BEAT_1 + BEAT_2 + "{", openDoc);
   _assertLocation(openDoc.getLength(), openDoc);
   }
   */
  
  /** Indents block comments with stars as they should.
   * Uncomment this method when the correct functionality is implemented.
   */
//  public void testIndentBlockCommentStar()
//      throws BadLocationException, OperationCanceledException {
//    OpenDefinitionsDocument openDoc = _getOpenDoc();
//    openDoc.insertString(0, "/*\n*\n*/\n " + FOO_EX_2, null);
//    int loc = openDoc.getCurrentLocation();
//    openDoc.indentLines(0, openDoc.getLength());
//    _assertContents("/*\n *\n */\n" + FOO_EX_2, openDoc);
//    _assertLocation(openDoc.getLength(), openDoc);
//  }
  
  /** Get the only open definitions document. */
  private OpenDefinitionsDocument _getOpenDoc() {
    _assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = _model.newFile();
    doc.setIndent(2);
    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
    _assertNumOpenDocs(2);
    return docs.get(0);
  }
  
  private void _assertNumOpenDocs(int num) {
    assertEquals("number of open documents", num, _model.getOpenDefinitionsDocuments().size());
  }
  
  private void _assertContents(String expected, OpenDefinitionsDocument document) throws BadLocationException {
    assertEquals("document contents", expected, document.getText());
  }
  
  private void _assertLocation(int loc, OpenDefinitionsDocument openDoc) {
    assertEquals("current def'n loc", loc, openDoc.getCurrentLocation());
  }
}
