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

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.beans.*;

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Date;
import java.net.URL;

import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.platform.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.DebugListener;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.repl.InteractionsEditorKit;
import edu.rice.cs.drjava.ui.config.*;
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;
import edu.rice.cs.drjava.ui.JUnitPanel.JUnitErrorListPane;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeOptionListener;
import edu.rice.cs.drjava.ui.config.ConfigFrame;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.util.swing.DelegatingAction;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.SwingWorker;

/**
 * Tests the Definitions Pane
 * @version $Id$
 */
public class DefinitionsPaneTest extends TestCase {

  private MainFrame _frame;

  public DefinitionsPaneTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(DefinitionsPaneTest.class);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    _frame = new MainFrame();
  }
  

  /**
   * Tests that a simulated key press with the meta modifier is correct
   * Reveals bug 676586
   */
   public void testMetaKeyPress() {
     DefinitionsPane definitions = _frame.getCurrentDefPane();
     DefinitionsDocument doc = definitions.getOpenDocument().getDocument();
     _assertDocumentEmpty(doc, "point 0");
     /* The following is the sequence of key events that happen when the user presses Meta-a */
    definitions.processKeyEvent(new KeyEvent(definitions, KeyEvent.KEY_PRESSED, (new Date()).getTime(),
					     InputEvent.META_MASK, KeyEvent.VK_META));
     _assertDocumentEmpty(doc, "point 1");
    definitions.processKeyEvent(new KeyEvent(definitions, KeyEvent.KEY_PRESSED, (new Date()).getTime(),
					     InputEvent.META_MASK, KeyEvent.VK_W));
     _assertDocumentEmpty(doc, "point 2");
    definitions.processKeyEvent(new KeyEvent(definitions, KeyEvent.KEY_TYPED, (new Date()).getTime(),
					     InputEvent.META_MASK, KeyEvent.VK_UNDEFINED, 'w'));
     _assertDocumentEmpty(doc, "point 3");
    definitions.processKeyEvent(new KeyEvent(definitions, KeyEvent.KEY_RELEASED, (new Date()).getTime(),
					     InputEvent.META_MASK, KeyEvent.VK_W));
     _assertDocumentEmpty(doc, "point 4");
    definitions.processKeyEvent(new KeyEvent(definitions, KeyEvent.KEY_RELEASED, (new Date()).getTime(),
					     0, KeyEvent.VK_META));
     _assertDocumentEmpty(doc, "point 5");
   }

  private void _assertDocumentEmpty(DefinitionsDocument doc, String message){
    try {
      assertEquals(message, "", doc.getText(0, doc.getLength()));
    } catch(BadLocationException ble){
      ble.printStackTrace();
      fail("BadLocationException");
    }
  }

}



  class KeyTestListener implements KeyListener {

    public void keyPressed(KeyEvent e){
      DefinitionsPaneTest.fail("Unexpected keypress " + e);
    }

    public void keyReleased(KeyEvent e){
      DefinitionsPaneTest.fail("Unexpected keyrelease " + e);
    }

    public void keyTyped(KeyEvent e){
      DefinitionsPaneTest.fail("Unexpected keytyped " + e);
    }

    public boolean done(){
      return true;
    }
  }

