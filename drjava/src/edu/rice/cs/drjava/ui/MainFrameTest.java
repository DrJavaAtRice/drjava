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

package edu.rice.cs.drjava.ui;

import  junit.framework.*;
import  junit.extensions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.rmi.registry.Registry;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;

/**
 * Test functions of MainFrame.
 *
 * @version $Id$
 */
public class MainFrameTest extends TestCase {
  
  private MainFrame _frame;
  
  /**
   * Constructor.
   * @param  String name
   */
  public MainFrameTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(MainFrameTest.class);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    _frame = new MainFrame(Registry.REGISTRY_PORT);
  }
  
  /**
   * Tests that the returned JButton of <code>createManualToolbarButton</code>:
   *  1. Is disabled upon return.
   *  2. Inherits the tooltip of the Action parameter <code>a</code>.
   */
  public void testCreateManualToolbarButton() {
    Action a = new AbstractAction("Test Action") {
      public void actionPerformed(ActionEvent ae) {
      }
    };
    a.putValue(Action.SHORT_DESCRIPTION, "test tooltip");
    JButton b = _frame._createManualToolbarButton(a);
    
    assertTrue("Returned JButton is enabled.", ! b.isEnabled());
    assertEquals("Tooltip text not set.", "test tooltip", b.getToolTipText());
  }
  
  /**
   * Tests that the current location of a document is equal to the
   * caret location after documents are switched.
   */
  public void testDocLocationAfterSwitch() 
    throws BadLocationException, InterruptedException
  {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
    doc.insertString(0, "abcd", null);
    pane.setCaretPosition(3);
    assertEquals("Location of old doc before switch", 3, doc.getCurrentLocation());
    
    // Create a new file
    SingleDisplayModel model = _frame.getModel();
    model.newFile();
    
    // Current pane should be new doc, pos 0
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDocument().getDocument();
    assertEquals("Location of new document", 0, doc.getCurrentLocation());
    
    // Switch back
    model.setPreviousActiveDocument();
    
    // Current pane should be old doc, pos 3
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDocument().getDocument();
    assertEquals("Location of old document", 3, doc.getCurrentLocation());
  }
}
