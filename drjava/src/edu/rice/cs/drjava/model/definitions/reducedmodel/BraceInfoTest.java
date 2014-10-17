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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.indent.IndentRulesTestCase;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

/** Test class for the IndentInfo class.
  * @version $Id: BraceInfoTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class BraceInfoTest extends DrJavaTestCase {
  private String _text;
  private AbstractDJDocument _document;
  
  public void setUp() throws Exception {
    super.setUp();
    _document = new AbstractDJDocument(IndentRulesTestCase.TEST_INDENT_LEVEL) {
      protected int startCompoundEdit() {
        //Do nothing
        return 0;
      }
      protected void endCompoundEdit(int key) { /* Do nothing. */ }
      protected void endLastCompoundEdit() { /* Do nothing. */ }
      protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
        /* Do nothing */
      }
      protected void _styleChanged() { /* Do nothing. */ }
    };
  }
  
  private void _lineEnclosingBraceTestHelper(int location, String msg, int expDist, String expBraceType) {
    _document.setCurrentLocation(location);
    BraceInfo info = _document._getLineEnclosingBrace();
    assertEquals(msg + ": distance", expDist, info.distance());
    assertEquals(msg + ": braceType", expBraceType, info.braceType());
  }
  
  private void _enclosingBraceTestHelper(int location, String msg, int expDist, String expBraceType) {
    _document.setCurrentLocation(location);
    BraceInfo info = _document._getEnclosingBrace();
    assertEquals(msg + ": distance", expDist, info.distance());
    assertEquals(msg + ": braceType", expBraceType, info.braceType());
  }
  
  public void testFieldsForCurrentLocation() throws BadLocationException {
    
    _text = "foo {\nvoid m1(int a,\nint b) {\n}\n}";
    //       .   . ..   .  ..     . .    . . ... .
    //       |          |         |           |
    //       0          10        20          30
    
    _document.clear();
    _document.insertString(0, _text, null);
    
    _lineEnclosingBraceTestHelper(0, "0 -- no brace or newline", -1 , "");
    _enclosingBraceTestHelper(0, "0 -- no brace or newline", -1 , "");
    _lineEnclosingBraceTestHelper(4, "No brace or newline", -1, "");
    _enclosingBraceTestHelper(4, "No brace or newline", -1, "");
    _lineEnclosingBraceTestHelper(5, "Curly brace preceding but no newline", -1, "");
    _enclosingBraceTestHelper(5, "Curly brace preceding but no newline", 1, "{");
    _lineEnclosingBraceTestHelper(6, "Curly brace preceding a newline", 2, "{");
    _enclosingBraceTestHelper(6, "Curly brace preceding a newline", 2, "{");
    _lineEnclosingBraceTestHelper(10, "Curly brace preceding a newline", 2, "{");
    _enclosingBraceTestHelper(10, "Curly brace preceding a newline", 6, "{");
    _lineEnclosingBraceTestHelper(13, "Curly brace preceding a newline", 2, "{");  // pos 13 is just after '1'
    _enclosingBraceTestHelper(13, "Curly brace preceding a newline; '(' follows", 9, "{");
    _lineEnclosingBraceTestHelper(14, "Curly brace preceding a newline", 2, "{");
    _enclosingBraceTestHelper(14, "Paren preceding on current line", 1, "(");
    _lineEnclosingBraceTestHelper(20, "Curly brace preceding a newline", 2, "{");  // pos 20 is just before newline
    _enclosingBraceTestHelper(20, "Paren preceding on current line", 7, "(");
    _lineEnclosingBraceTestHelper(21, "Newline after paren", 8, "(");
    _enclosingBraceTestHelper(21, "Newline after paren", 8, "(");
    
    _lineEnclosingBraceTestHelper(26, "Just before close paren", 8, "(");
    _enclosingBraceTestHelper(26, "Just before close paren", 13, "(");
    _lineEnclosingBraceTestHelper(28, "After close paren, just before second open brace", 8, "(");
    _enclosingBraceTestHelper(28, "After close paren, just before second open brace", 24, "{");
    _lineEnclosingBraceTestHelper(29, "Just before newline in second set of braces", 8, "(");
    _enclosingBraceTestHelper(29, "Just before newline in second set of braces", 1, "{");
    _lineEnclosingBraceTestHelper(30, "Just after newline in second set of braces", 2, "{");
    _enclosingBraceTestHelper(30, "Just after newline in second set of braces", 2, "{");
    _lineEnclosingBraceTestHelper(31, "Just after a close curly brace on a new line", 2, "{");
    _enclosingBraceTestHelper(31, "Just after a close curly brace on a new line", 27, "{");
    _lineEnclosingBraceTestHelper(32, "Just after newline, just before final close brace", 28, "{");
    _enclosingBraceTestHelper(32, "Just after newline, just before final close brace", 28, "{");
    _lineEnclosingBraceTestHelper(33, "End of text", 28, "{");
    _enclosingBraceTestHelper(33, "End of text, just after final closng brace", -1, "");
  }
}
