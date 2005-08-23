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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.definitions.ColoringView;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.swing.Utilities;

import java.util.List;

import javax.swing.text.*;

import junit.framework.*;


/**
 * Test functions of InteractionsPane.
 *
 * @version $Id$
 */
public final class InteractionsPaneTest extends TestCase {

  protected InteractionsDocumentAdapter _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected InteractionsPane _pane;
  protected InteractionsController _controller;

  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws Exception {
    super.setUp();
    _adapter = new InteractionsDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
    _doc = _model.getDocument();
    _pane = new InteractionsPane(_adapter) {
      public int getPromptPos() {
       return _model.getDocument().getPromptPos();
      }
    };
    // Make tests silent
    _pane.setBeep(new TestBeep());

    _controller = new InteractionsController(_model, _adapter, _pane);
    
  }

  public void tearDown() throws Exception {
    _controller = null;
    _doc = null;
    _model = null;
    _pane = null;
    _adapter = null;
    super.tearDown();
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
    _doc.insertText(_doc.getLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _controller.moveToPrompt();
        _controller.moveLeftAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved left at the prompt.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret right when it's already at the end will
   * cycle it to the prompt.
   */
  public void testCaretMovementCyclesWhenAtEnd() throws DocumentAdapterException {
    _doc.insertText(_doc.getLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _controller.moveToEnd();
        _controller.moveRightAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved right at the end.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret left when it's before the prompt will
   * cycle it to the prompt.
   */
  public void testLeftBeforePromptMovesToPrompt() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.moveLeftAction.actionPerformed(null);
      }
    });
    assertEquals("Left arrow doesn't move to prompt when caret is before prompt.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret right when it's before the prompt will
   * cycle it to the end of the document.
   */
  public void testRightBeforePromptMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.moveRightAction.actionPerformed(null);
      }
    });
    assertEquals("Right arrow doesn't move to end when caret is before prompt.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret up (recalling the previous command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallPrevMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() {  
      public void run() {
      _pane.setCaretPosition(1);
      _controller.historyPrevAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on up arrow.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret down (recalling the next command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallNextMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.historyNextAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on down arrow.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  public void testCaretStaysAtEndDuringInteraction() throws DocumentAdapterException {
    
    _doc.setInProgress(true);
    _doc.insertText(_doc.getLength(), "simulated output", InteractionsDocument.DEFAULT_STYLE);
    _doc.setInProgress(false);
    Utilities.clearEventQueue();
    assertEquals("Caret is at the end after output while in progress.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that the caret catches up to the prompt if it is before it and
   * output is displayed.
   */
  public void testCaretMovesUpToPromptAfterInsert() throws DocumentAdapterException {
    _doc.append("typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(1); } });
//    System.err.println("caretPostion = " + _pane.getCaretPosition());
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
//     System.err.println("caretPostion = " + _pane.getCaretPosition());
    Utilities.clearEventQueue();
//     System.err.println("caretPostion = " + _pane.getCaretPosition());
    assertEquals("Caret is at the prompt after output inserted.", _doc.getPromptPos(), _pane.getCaretPosition());

    _doc.insertPrompt();
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(1); } });
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("Caret is at the end after output inserted.", _doc.getPromptPos(), _pane.getCaretPosition());
  }

  /**
   * Tests that the caret is moved properly when the current interaction
   * is cleared.
   */
  public void testClearCurrentInteraction() throws DocumentAdapterException {
    _doc.insertText(_doc.getLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { public void run() { _controller.moveToEnd(); } });

    _doc.clearCurrentInteraction();
    Utilities.clearEventQueue();
    assertEquals("Caret is at the prompt after output cleared.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
    assertEquals("Prompt is at the end after output cleared.",
                 _doc.getLength(),
                 _doc.getPromptPos());
  }

  /**
   * Tests that the InteractionsPane cannot be edited before the prompt.
   */
  public void testCannotEditBeforePrompt() throws DocumentAdapterException {
    int origLength = _doc.getLength();
    _doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("Document should not have changed.",
                 origLength,
                 _doc.getLength());
  }

  /** Tests that the caret is put in the correct position after an insert. */
  public void testCaretUpdatedOnInsert() throws DocumentAdapterException {
    _doc.insertText(_doc.getLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    final int pos = _doc.getLength() - 5;
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(pos); } });

    // Insert text before the prompt
    _doc.insertBeforeLastPrompt("aa", InteractionsDocument.DEFAULT_STYLE);
     Utilities.clearEventQueue();
    assertEquals("caret should be in correct position", pos + 2, _pane.getCaretPosition());

    // Move caret to prompt and insert more text
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(_doc.getPromptPos()); } });
    _doc.insertBeforeLastPrompt("b", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());

    // Move caret before prompt and insert more text
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(0); } });
    _doc.insertBeforeLastPrompt("ccc", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());

    // Move caret after prompt and insert more text
    final int newPos = _doc.getPromptPos();
    // simulate a keystroke by putting caret just *after* pos of insert
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(newPos+1); } });
    _doc.insertText(newPos, "d", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("caret should be immediately after the d", newPos + 1, _pane.getCaretPosition());
  }

//   public void testSystemIn() {
//     final Object lock = new Object();
//     final StringBuffer buf = new StringBuffer();
//     synchronized(_controller._inputEnteredAction) {
//       new Thread("Testing System.in") {
//         public void run() {
//           synchronized(_controller._inputEnteredAction) {
//             _controller._inputEnteredAction.notify();
//             synchronized(lock) {
//               buf.append(_controller._inputListener.getConsoleInput());
//             }
//           }
//         }
//       }.start();
//       try {
//         _controller._inputEnteredAction.wait();
//       }
//       catch (InterruptedException ie) {
//       }
//     }
//     try {
//       Thread.sleep(2000);
//     }
//     catch (InterruptedException ie) {
//     }
//     _controller._insertNewlineAction.actionPerformed(null);
//     _controller._inputEnteredAction.actionPerformed(null);
//     synchronized(lock) {
//       assertEquals("Should have returned the correct text.", "\n\n", buf.toString());
//     }
//   }
  
  /* This test does not appear to be properly synchronized. */
  public void testSystemIn() {
    final Object lock = new Object();
    final StringBuffer buf = new StringBuffer();
    
    new Thread("Testing System.in") {
      public void run() {
        synchronized(lock) { buf.append(_controller.getInputListener().getConsoleInput()); }
      }
    }.start();
    
    try { _controller._popupConsole.waitForConsoleReady(); }
    catch (InterruptedException ie) { }
    
    _controller._popupConsole.insertConsoleText("test-text");
    _controller._popupConsole.interruptConsole();
    synchronized(lock) {
      assertEquals("Should have returned the correct text.", "test-text\n", buf.toString());
    }
  }
  
  public void testPromptListClearedOnReset() throws Exception {
    //Can't use the fields declared in setUp - it doesn't use a real InteractionsModel
    MainFrame mf = new MainFrame();
    
    Utilities.clearEventQueue();
    GlobalModel gm = mf.getModel();
    _controller = mf.getInteractionsController();
    _model = gm.getInteractionsModel();
    _adapter = gm.getSwingInteractionsDocument();
    _doc = gm.getInteractionsDocument();
    _pane = mf.getInteractionsPane();
    _pane.resetPrompts();

    Utilities.clearEventQueue();
//    System.err.println(_pane.getPromptList());
    assertEquals("PromptList before insert should contain 0 elements", 0, _pane.getPromptList().size());
        
    // Insert some text                   
    _doc.insertText(_doc.getLength(), "5", InteractionsDocument.NUMBER_RETURN_STYLE);
    _pane.setCaretPosition(_doc.getLength());
    
    Utilities.clearEventQueue();
    assertEquals("PromptList after insert should contain 1 element", 1, _pane.getPromptList().size());    
    assertEquals("First prompt should be saved as being at position",
                 InteractionsDocument.DEFAULT_BANNER.length() + InteractionsDocument.DEFAULT_PROMPT.length(),
                 (int)_pane.getPromptList().get(0)); //needs cast to prevent ambiguity
    
    _doc.insertPrompt();
    _pane.setCaretPosition(_doc.getLength());
    
    Utilities.clearEventQueue();
    assertEquals("PromptList after insertion of new prompt should contain 2 elements", 2, _pane.getPromptList().size());
    assertEquals("First prompt should be saved as being at position",
                 InteractionsDocument.DEFAULT_BANNER.length() + InteractionsDocument.DEFAULT_PROMPT.length(),
   (int) _pane.getPromptList().get(0)); //needs cast to prevent ambiguity
    assertEquals("Second prompt should be saved as being at position",
                 InteractionsDocument.DEFAULT_BANNER.length() + InteractionsDocument.DEFAULT_PROMPT.length() * 2 + 1,
                 (int)_pane.getPromptList().get(1)); //needs cast to prevent ambiguity
    
    
    synchronized(_model) {
      // Reset should clear
      _model.setWaitingForFirstInterpreter(false);
      //this adds the "Resetting Interactions" 
      _model.resetInterpreter();
      _model.interpreterResetting();
    }
    Utilities.clearEventQueue();
    synchronized(_model) {
      assertEquals("PromptList after reset should contain no elements", 0, _pane.getPromptList().size());
    }  
  }
    
}
