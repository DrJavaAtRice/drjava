/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.text;

import org.eclipse.swt.custom.StyledText;

import junit.framework.TestCase;

/**
 * Tests the SWTDocumentAdapter.
 * 
 * NOTE: To run this test, you have to put the correct SWT directory on
 * your java.library.path...  (eg. org.eclipse.swt.motif_2.1.0/os/linux/x86)
 * 
 * @version $Id$
 */
public class SWTDocumentAdapterTest extends TestCase {
  
  
  // TO DO:
  //  - Figure out how to instantiate a StyledText for a unit test...
  
  protected StyledText _pane;
  protected SWTDocumentAdapter _doc;
  
  public void setUp() {
    _pane = new StyledText(null, 0);  // this doesn't work...
    _doc = new SWTDocumentAdapter(_pane);
  }
  
  public void tearDown() {
    _doc = null;
    _pane = null;
    System.gc();
  }
  
  /**
   * Tests basic interactions with a DocumentAdapter.
   */
  public void testBasicDocOps() throws DocumentAdapterException {
    _doc.insertText(0, "one", null);
    assertEquals("first doc contents", "one",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.insertText(_doc.getDocLength(), " three", null);
    assertEquals("second doc contents", "one three",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.removeText(0, 3);
    _doc.insertText(0, "two", null);
    assertEquals("third doc contents", "two thr",
                 _doc.getDocText(0, 7));
  }
  
  /**
   * Tests that a DocumentAdapterException is thrown when it should be.
   */
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
   * Tests that a SwingDocumentAdapter can receive an object that
   * determines whether certain edits are legal.
   */
  public void testEditCondition() 
    throws DocumentAdapterException
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
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.setEditCondition(c);
    _doc.insertText(4, "1", null);
    assertEquals("insertText should be rejected", "initial",
                 _doc.getDocText(0, _doc.getDocLength()));
    _pane.replaceTextRange(2, 0, "1");
    assertEquals("replaceTextRange should be rejected", "initial",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.insertText(6, "2", null);
    assertEquals("insertText should be accepted", "initia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.forceInsertText(2, "3", null);
    assertEquals("forceInsertText should be accepted", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.removeText(1, 1);
    assertEquals("removeText should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _pane.replaceTextRange(6, 1, "");
    assertEquals("replaceTextRange should be rejected", "in3itia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.removeText(1, 2);
    assertEquals("removeText should be accepted", "iitia2l",
                 _doc.getDocText(0, _doc.getDocLength()));
    _doc.forceRemoveText(6, 1);
    assertEquals("forceRemove should be accepted", "iitia2",
                 _doc.getDocText(0, _doc.getDocLength()));
    
  }
  
}