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
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

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
  
  protected SwingDocumentAdapter _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected InteractionsPane _pane;
  protected InteractionsController _controller;
  
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public InteractionsPaneTest(String name) {
    super(name);
  }
  
  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return new TestSuite(InteractionsPaneTest.class);
  }
  
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    _adapter = new SwingDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
    _doc = _model.getDocument();
    _pane = new InteractionsPane(_adapter);
    // Make tests silent
    _pane.setBeep(new TestBeep());

    _controller = new InteractionsController(_model, _adapter, _pane);
  }
  
  public void tearDown() {
    _controller = null;
    _doc = null;
    _model = null;
    _pane = null;
    _adapter = null;
    System.gc();
  }
  
  /**
   * Tests that this.setUp() puts the caret in the correct position.
   */
  public void testInitialPosition() {
    assertEquals("Initial caret not in the correct position.",
                 _pane.getCaretPosition(),
                 _doc.getPromptPos());
  }
  
  /**
   * Tests that moving the caret left when it's already at the prompt will
   * cycle it to the end of the line.
   */
  public void testCaretMovementCyclesWhenAtPrompt() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToPrompt();
    
    _controller.moveLeftAction.actionPerformed(null);
    assertEquals("Caret was not cycled when moved left at the prompt.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's already at the end will
   * cycle it to the prompt.
   */
  public void testCaretMovementCyclesWhenAtEnd() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToEnd();
    
    _controller.moveRightAction.actionPerformed(null);
    assertEquals("Caret was not cycled when moved right at the end.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret left when it's before the prompt will
   * cycle it to the prompt.
   */
  public void testLeftBeforePromptMovesToPrompt() {
    _pane.setCaretPosition(1);
    _controller.moveLeftAction.actionPerformed(null);
    assertEquals("Left arrow doesn't move to prompt when caret is before prompt.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's before the prompt will
   * cycle it to the end of the document.
   */
  public void testRightBeforePromptMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.moveRightAction.actionPerformed(null);
    assertEquals("Right arrow doesn't move to end when caret is before prompt.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret up (recalling the previous command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallPrevMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.historyPrevAction.actionPerformed(null);
    assertEquals("Caret not moved to end on up arrow.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret down (recalling the next command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallNextMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.historyNextAction.actionPerformed(null);
    assertEquals("Caret not moved to end on down arrow.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  public void testCaretStaysAtEndDuringInteraction() throws DocumentAdapterException {
    _doc.setInProgress(true);
    _doc.insertText(_doc.getDocLength(), "simulated output", InteractionsDocument.DEFAULT_STYLE);
    _doc.setInProgress(false);
    assertEquals("Caret is at the end after output while in progress.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that the caret catches up to the prompt if it is before it and
   * output is displayed.
   */
  public void testCaretMovesUpToPromptAfterInsert() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    _pane.setCaretPosition(1);
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Caret is at the prompt after output inserted.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
    
    _doc.insertPrompt();
    _pane.setCaretPosition(1);
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Caret is at the end after output inserted.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that the caret is moved properly when the current interaction
   * is cleared.
   */
  public void testClearCurrentInteraction() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToEnd();
    
    _doc.clearCurrentInteraction();
    assertEquals("Caret is at the prompt after output cleared.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
    assertEquals("Prompt is at the end after output cleared.",
                 _doc.getDocLength(),
                 _doc.getPromptPos());
  }
  
  /**
   * Tests that the InteractionsPane cannot be edited before the prompt.
   */
  public void testCannotEditBeforePrompt() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Document should not have changed.",
                 origLength,
                 _doc.getDocLength());
  }
  
  /**
   * Tests that the caret is put in the correct position after an insert.
   */
  public void testCaretUpdatedOnInsert() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text",
                    InteractionsDocument.DEFAULT_STYLE);
    int pos = _doc.getDocLength() - 5;
    _pane.setCaretPosition(pos);
    
    // Insert text before the prompt
    _doc.insertBeforeLastPrompt("aa", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be in correct position",
                 pos + 2, _pane.getCaretPosition());
    
    // Move caret to prompt and insert more text
    _pane.setCaretPosition(_doc.getPromptPos());
    _doc.insertBeforeLastPrompt("b", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be at prompt",
                 _doc.getPromptPos(), _pane.getCaretPosition());
    
    // Move caret before prompt and insert more text
    _pane.setCaretPosition(0);
    _doc.insertBeforeLastPrompt("ccc", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be at prompt",
                 _doc.getPromptPos(), _pane.getCaretPosition());
  }
}
