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

package edu.rice.cs.util.text;

import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.swing.Utilities;

/** Tests ConsoleDocument.
  * @version $Id: ConsoleDocumentTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ConsoleDocumentTest extends DrJavaTestCase {
  protected ConsoleDocument _doc;
  
  public void setUp() throws Exception {
    super.setUp();
    _doc = new ConsoleDocument(new InteractionsDJDocument());  // Why use InteractionsDJDocument?  Isn't it overkill?
  }
  
  public void tearDown() throws Exception {
    _doc = null;
    super.tearDown();
  }
  
  /** Tests basic interactions with a Swing Document. */
  public void testBasicDocOps() throws EditDocumentException {
    _doc.insertText(0, "one", null);
    assertEquals("first doc contents", "one", _doc.getText());
    
    _doc.insertText(_doc.getLength(), " three", null);
    assertEquals("second doc contents", "one three", _doc.getText());
    
    _doc.removeText(0, 3);
    _doc.insertText(0, "two", null);
    assertEquals("third doc contents", "two thr", _doc.getDocText(0, 7));
    
    _doc.append(" four", (String) null);
    assertEquals("fourth doc contents", "two three four", _doc.getText());
  }
  
  /** Tests that a EditDocumentException is thrown when it should be. */
  public void testException() {
    try {
      _doc.insertText(5, "test", null);
      fail("should have thrown an exception");
    }
    catch (EditDocumentException e) { /* Expected. Silently succeed. */ }
  }
  
  /** Tests that a SwingDocument can receive an object that determines whether certain edits are legal. */
  public void testEditCondition() throws EditDocumentException, BadLocationException {
    DocumentEditCondition c = new DocumentEditCondition() {
      public boolean canInsertText(int offs) { return (offs > 5); }
      public boolean canRemoveText(int offs) { return (offs == 1); }
    };
    _doc.insertText(0, "initial", null);
    assertEquals("first doc contents", "initial", _doc.getDocText(0, _doc.getLength()));
    
    _doc.setEditCondition(c);
    _doc.insertText(4, "1", null);
    assertEquals("insertText should be rejected", "initial", _doc.getText());
    _doc.insertText(2, "1", null);
    assertEquals("insertText should be rejected", "initial", _doc.getText());
    _doc.insertText(6, "2", null);
    assertEquals("insertText should be accepted", "initia2l", _doc.getText());
    _doc.forceInsertText(2, "3", null);
    assertEquals("forceInsertText should be accepted", "in3itia2l", _doc.getText());
//    System.err.println(_doc.getText());
    _doc.removeText(3, 1);
//    System.err.println(_doc.getText());
    assertEquals("removeText should be rejected", "in3itia2l", _doc.getText());
    _doc.removeText(6, 1);
    assertEquals("remove should be rejected", "in3itia2l", _doc.getText());
    _doc.removeText(1, 2);
    assertEquals("removeText should be accepted", "iitia2l", _doc.getText());
    _doc.forceRemoveText(6, 1);
    assertEquals("forceRemove should be accepted", "iitia2", _doc.getText());
    _doc.append("THE END", (String) null);
    assertEquals("forceRemove should be accepted", "iitia2THE END", _doc.getText());
    Utilities.invokeAndWait(new Runnable() { public void run() { _doc.reset(""); } });
    assertEquals("promptPos reset when doc is reset", 0, _doc.getPromptPos());
    _doc.setEditCondition(new DocumentEditCondition());
    _doc.append("THE END", null);
    assertEquals("append to reset document should be accepted", "THE END", _doc.getText());
    _doc.setPromptPos(_doc.getLength());
    assertEquals("promptPos is character position at end of document", _doc.getLength(), _doc.getPromptPos());
  }
}
