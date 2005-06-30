/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.text;

import javax.swing.text.BadLocationException;

import junit.framework.TestCase;

/**
 * Tests the SwingDocument.
 * @version $Id$
 */
public class SwingDocumentTest extends TestCase {
  protected SwingDocument _doc;
  
  public void setUp() throws Exception {
    super.setUp();
    _doc = new SwingDocument();
  }
  
  public void tearDown() throws Exception {
    _doc = null;
    super.tearDown();
  }
  
  /**
   * Tests basic interactions with a DocumentAdapter.
   */
  public void testBasicDocOps() throws DocumentAdapterException {
    _doc.insertText(0, "one", null);
    assertEquals("first doc contents", "one",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.insertText(_doc.getDocLength(), " three", null);
    assertEquals("second doc contents", "one three",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.removeText(0, 3);
    _doc.insertText(0, "two", null);
    assertEquals("third doc contents", "two thr", _doc.getDocText(0, 7));
    
    _doc.append(" four", null);
    assertEquals("fourth doc contents", "two three four", _doc.getText());
  }
  
  /** Tests that a DocumentAdapterException is thrown when it should be. */
  public void testException() {
    try {
      _doc.insertText(5, "test", null);
      fail("should have thrown an exception");
    }
    catch (DocumentAdapterException e) {
      // That's what we expect.
    }
  }
  
  /**
   * Tests that a SwingDocument can receive an object that
   * determines whether certain edits are legal.
   */
  public void testEditCondition() throws DocumentAdapterException, BadLocationException {
    DocumentEditCondition c = new DocumentEditCondition() {
      public boolean canInsertText(int offs) {
        return (offs > 5);
      }
      public boolean canRemoveText(int offs) {
        return (offs == 1);
      }
    };
    _doc.insertText(0, "initial", null);
    assertEquals("first doc contents", "initial",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.setEditCondition(c);
    _doc.insertText(4, "1", null);
    assertEquals("insertText should be rejected", "initial",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.insertString(2, "1", null);
    assertEquals("insertString should be rejected", "initial",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.insertText(6, "2", null);
    assertEquals("insertText should be accepted", "initia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.forceInsertText(2, "3", null);
    assertEquals("forceInsertText should be accepted", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.removeText(3, 1);
    assertEquals("removeText should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.remove(6, 1);
    assertEquals("remove should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.removeText(1, 2);
    assertEquals("removeText should be accepted", "iitia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.forceRemoveText(6, 1);
    assertEquals("forceRemove should be accepted", "iitia2",
                 _doc.getDocText(0, _doc.getDocLength()));
    
  }
}
