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

import edu.rice.cs.drjava.model.repl.*;

import junit.framework.*;
import junit.extensions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.rmi.registry.Registry;

/**
 * Test functions of InteractionsPane.
 *
 * @version $Id$
 */
public class InteractionsPaneTest extends TestCase {
  
  private InteractionsDocument _doc;
  private InteractionsPane _pane;
  private boolean _ready = false;
  
  /**
   * Constructor.
   * @param  String name
   */
  public InteractionsPaneTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(InteractionsPaneTest.class);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    _doc = new TestInteractionsDocument();
    _pane = new InteractionsPane(_doc);
    _pane.setCaretPosition(_doc.getPromptPos());
    _ready = true;
  }
  
  public void tearDown() {
    _doc = null;
    _pane = null;
    System.gc();
  }
  
  /**
   * Tests that this.setUp() puts the caret in the correct position.
   */
  public void testInitialPosition() {
    assertEquals("Initial caret not in the correct position.",
                 1,
                 _pane.getCaretPosition(),
                 _doc.getPromptPos());
  }
  
  /**
   * Tests that moving the caret left when it's already at the prompt will
   * cycle it to the end of the line.
   */
  public void testCaretMovementCyclesWhenAtPrompt() {
    while (! _ready);
    
    _doc.insertBeforeLastPrompt("test", new SimpleAttributeSet());
    _pane.setCaretPosition(_doc.getPromptPos());
    
    _pane._moveLeft.actionPerformed(null);
    assertEquals("Caret was not cycled when moved left at the prompt.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's already at the end will
   * cycle it to the prompt.
   */
  public void testCaretMovementCyclesWhenAtEnd() {
    while (! _ready);
    
    _doc.insertBeforeLastPrompt("test", new SimpleAttributeSet());
    _pane.setCaretPosition(_doc.getLength());
    
    _pane._moveLeft.actionPerformed(null);
    assertEquals("Caret was not cycled when moved right at the end.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret left when it's before the prompt will
   * cycle it to the prompt.
   */
  public void testLeftBeforePromptMovesToPrompt() {
    while (! _ready);
    
    _pane.setCaretPosition(10);
    _pane._moveLeft.actionPerformed(null);
    assertEquals("Left arrow doesn't move to prompt when caret is before prompt.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's before the prompt will
   * cycle it to the end of the document.
   */
  public void testRightBeforePromptMovesToEnd() {
    while (! _ready);
    
    _pane.setCaretPosition(10);
    _pane._moveRight.actionPerformed(null);
    assertEquals("Right arrow doesn't move to end when caret is before prompt.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret up (recalling the previous command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallMovesToEnd() {
    while (! _ready);
    
    _pane.setCaretPosition(10);
    _pane._historyPrevAction.actionPerformed(null);
    assertEquals("Caret not moved to end on up arrow.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }
  
  public class TestInteractionsDocument extends AbstractInteractionsDocument {
    public void interpretCurrentInteraction() {
      fail("interpretCurrentInteraction called unexpectedly");
    }
  }
}
