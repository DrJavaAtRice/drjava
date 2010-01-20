/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse.util.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.EditDocumentException;

import junit.framework.TestCase;

/**
 * Tests the SWTDocumentAdapter.
 * 
 * NOTE: To run this test, you have to put the correct SWT directory on
 * your java.library.path...  (eg. org.eclipse.swt.motif_2.1.0/os/linux/x86)
 * On Linux, you can set your LD_LIBRARY_PATH to include this.  On other
 * platforms, you must start java with -Djava.library.path=...
 * 
 * @version $Id$
 */
public class SWTDocumentAdapterTest extends TestCase {
  
  
  // TO DO:
  //  - Figure out how to instantiate a StyledText for a unit test...
  
  protected Display _display;
  protected Shell _shell;
  protected StyledText _pane;
  protected SWTDocumentAdapter _doc;
  
  /**
   * Creates a new SWTDocumentAdapter for the tests.
   */
  public void setUp() {
    _display = new Display();
    _shell = new Shell(_display, SWT.TITLE | SWT.CLOSE);
    _pane = new StyledText(_shell, 0);
    _doc = new SWTDocumentAdapter(_pane);
  }
  
  /**
   * Disposes any Eclipse resources that were created.
   */
  public void tearDown() {
    _doc = null;
    _pane = null;
    _shell.dispose();
    _shell = null;
    _display.dispose();
    _display = null;
    System.gc();
  }
  
  /**
   * Tests basic interactions with a DocumentAdapter.
   */
  public void testBasicDocOps() throws EditDocumentException {
    _doc.insertText(0, "one", null);
    assertEquals("first doc contents", "one",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.insertText(_doc.getLength(), " three", null);
    assertEquals("second doc contents", "one three",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.removeText(0, 3);
    _doc.insertText(0, "two", null);
    assertEquals("third doc contents", "two thr", _doc.getDocText(0, 7));
  }
  
  /**
   * Tests that a EditDocumentException is thrown when it should be.
   */
  public void testException() {
    try {
      _doc.insertText(5, "test", null);
      fail("should have thrown an exception");
    }
    catch (EditDocumentException e) {
      // That's what we expect.
    }
  }
  
  /**
   * Tests that a SwingDocumentAdapter can receive an object that
   * determines whether certain edits are legal.
   */
  public void testEditCondition() 
    throws EditDocumentException
  {
    DocumentEditCondition c = new DocumentEditCondition() {
      public boolean canInsertText(int offs, String str, String style) {
        return (offs > 5);
      }
      public boolean canRemoveText(int offs, int len) {
        return (len == 2);
      }
    };
    _doc.insertText(0, "initial", null);
    assertEquals("first doc contents", "initial",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.setEditCondition(c);
    _doc.insertText(4, "1", null);
    assertEquals("insertText should be rejected", "initial",
                 _doc.getDocText(0, _doc.getLength()));
    _pane.replaceTextRange(2, 0, "1");
    assertEquals("replaceTextRange should be rejected", "initial",
                 _doc.getDocText(0, _doc.getLength()));
    _doc.insertText(6, "2", null);
    assertEquals("insertText should be accepted", "initia2l",
                 _doc.getDocText(0, _doc.getLength()));
    _doc.forceInsertText(2, "3", null);
    assertEquals("forceInsertText should be accepted", "in3itia2l",
                 _doc.getDocText(0, _doc.getLength()));
    
    _doc.removeText(1, 1);
    assertEquals("removeText should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getLength()));
    _pane.replaceTextRange(6, 1, "");
    assertEquals("replaceTextRange should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getLength()));
    _doc.removeText(1, 2);
    assertEquals("removeText should be accepted", "iitia2l",
                 _doc.getDocText(0, _doc.getLength()));
    _doc.forceRemoveText(6, 1);
    assertEquals("forceRemove should be accepted", "iitia2",
                 _doc.getDocText(0, _doc.getLength()));
    
  }
  
}
