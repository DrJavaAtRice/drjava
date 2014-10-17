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

package edu.rice.cs.drjava.ui;


import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import edu.rice.cs.drjava.ui.InteractionsController;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import java.util.Date;

/** Test functions of InteractionsPane.
  * @version $Id: InteractionsPaneTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class InteractionsPaneTest extends DrJavaTestCase {
  
  private static final char UNDEFINED = KeyEvent.CHAR_UNDEFINED;
  private static final int PRESSED = KeyEvent.KEY_PRESSED;
  private static final int RELEASED = KeyEvent.KEY_RELEASED;
  private static final int SHIFT = InputEvent.SHIFT_MASK;
  private static final int TYPED = KeyEvent.KEY_TYPED;
  private static final int VK_UNDEF = KeyEvent.VK_UNDEFINED;
  
  protected volatile InteractionsDJDocument _adapter;
  protected volatile InteractionsModel _model;
  protected volatile InteractionsDocument _doc;
  protected volatile InteractionsPane _pane;
  protected volatile InteractionsController _controller;
  
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _adapter = new InteractionsDJDocument();
        _model = new TestInteractionsModel(_adapter);
        _doc = _model.getDocument();
        _pane = new InteractionsPane(_adapter) {
          public int getPromptPos() { return _model.getDocument().getPromptPos(); }
        };
        // Make tests silent
        _pane.setBeep(new TestBeep());
        _controller = new InteractionsController(_model, _adapter, _pane, new Runnable() { public void run() { } });
//        _controller.setCachedCaretPos(_pane.getCaretPosition());
//        _controller.setCachedPromptPos(_doc.getPromptPos());
//        System.err.println("_controller = " + _controller);
      }
    });
  }
  
  public void tearDown() throws Exception {
//    _controller = null;
//    _doc = null;
//    _model = null;
//    _pane = null;
//    _adapter = null;
    super.tearDown();
  }
  
  /** Tests that this.setUp() puts the caret in the correct position. */
  public void testInitialPosition() {
    assertEquals("Initial caret not in the correct position.", _pane.getCaretPosition(), _doc.getPromptPos());
  }
  
  /** Tests that moving the caret left when it's already at the prompt will cycle it to the end of the line. */
  public void testCaretMovementCyclesWhenAtPrompt() throws EditDocumentException {
    _doc.append("test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _controller.moveToPrompt();
        _controller.moveLeftAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved left at the prompt.", _doc.getLength(), _pane.getCaretPosition());
  }
  
  /** Tests that moving the caret right when it's already at the end will cycle it to the prompt. */
  public void testCaretMovementCyclesWhenAtEnd() throws EditDocumentException {
    _doc.append("test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _controller.moveToEnd();
        _controller.moveRightAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved right at the end.", _doc.getPromptPos(), _pane.getCaretPosition());
  }
  
  /** Tests that moving the caret left when it's before the prompt will cycle it to the prompt. */
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
  
  /** Tests that moving the caret right when it's before the prompt will cycle it to the end of the document. */
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
  
  /** Tests that moving the caret up (recalling the previous command from history) will move the caret to the end
    * of the document.
    */
  public void testHistoryRecallPrevMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() {  
      public void run() {
        _pane.setCaretPosition(1);
        _controller.historyPrevAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on up arrow.", _doc.getLength(), _pane.getCaretPosition());
  }
  
  /** Tests that moving the caret down (recalling the next command from history) will move the caret to the end of
    * the document.
    */
  public void testHistoryRecallNextMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.historyNextAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on down arrow.", _doc.getLength(), _pane.getCaretPosition());
  }
  
  public void testCaretStaysAtEndDuringInteraction() throws EditDocumentException {
//    System.err.println("start caret pos = " + _pane.getCaretPosition());
//    System.err.println("start prompt pos = " + _doc.getPromptPos());
    _doc.setInProgress(true);
//    System.err.println(_pane.getCaretPosition());
    _model.replSystemOutPrint("simulated output");
    Utilities.clearEventQueue();
    _doc.setInProgress(false);
//    System.err.println("caret pos = " + _pane.getCaretPosition());
//    System.err.println("prompt pos = " + _doc.getPromptPos());
//    System.err.println("Document = |" + _doc.getDocText(0, _doc.getLength()) + "|");
    assertEquals("Caret is at the end after output while in progress.", _doc.getLength(), _pane.getCaretPosition());
  }
  
  /** Tests that the caret catches up to the prompt if it is before it and output is displayed. */
  public void testCaretMovesUpToPromptAfterInsert() throws EditDocumentException {
    _model.replSystemOutPrint("typed text");
//    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(1); } });
//    _controller.setCachedCaretPos(1);
    _model.replSystemOutPrint("simulated output");
    Utilities.clearEventQueue();
    assertEquals("Caret is at the prompt after output inserted.", _doc.getPromptPos(), _pane.getCaretPosition());
  }
  
  /** Tests that the caret is moved properly when the current interaction is cleared. */
  public void testClearCurrentInteraction() throws EditDocumentException {
    _doc.append("typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { public void run() { _controller.moveToEnd(); } });
    
    _doc.clearCurrentInteraction();
    Utilities.clearEventQueue();
    assertEquals("Caret is at the prompt after output cleared.", _doc.getPromptPos(), _pane.getCaretPosition());
    assertEquals("Prompt is at the end after output cleared.", _doc.getLength(), _doc.getPromptPos());
  }
  
  /** Tests that the InteractionsPane cannot be edited before the prompt. */
  public void testCannotEditBeforePrompt() throws EditDocumentException {
    Utilities.clearEventQueue(); // wait until pending event queue tranactions have completed.
    int origLength = _doc.getLength();
    Utilities.invokeAndWait(new Runnable() {
      public void run() { _doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE); }
    });
    assertEquals("Document should not have changed.", origLength, _doc.getLength());
  }
  
  /** Tests that the caret is put in the correct position after an insert. */
  public void testCaretUpdatedOnInsert() throws EditDocumentException {
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
        // Type 'T'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_T, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'T'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_T, UNDEFINED));
        
        // Type 'Y'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_Y, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'Y'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_Y, UNDEFINED));
        
        // Type 'P'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_P, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'P'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_P, UNDEFINED));
        
        // Type 'E'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_E, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'E'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_E, UNDEFINED));
        
        // Type 'D'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'D'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
      }
    });

    Utilities.clearEventQueue();
    Utilities.clearEventQueue();
//    System.err.println("Document = '" + _doc.getText() + "'");
    assertEquals("caret should be at end of document", _doc.getLength(), _pane.getCaretPosition());
    
    final int pos = _doc.getLength() - 5;
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(pos); } });

//    _controller.setCachedCaretPos(pos);
//    System.err.println("docLength = " +  _doc.getLength() + " caretPos = " + _pane.getCaretPosition());
    
    // Insert text before the prompt
    _model.replSystemErrPrint("aa");
    Utilities.clearEventQueue();

//    System.err.println("Document = '" + _doc.getText() + "'");
//    System.err.println("docLength = " +  _doc.getLength() + " caretPos = " + _pane.getCaretPosition());
    assertEquals("caret should be in correct position", pos + 2, _pane.getCaretPosition());
    
    // Move caret to prompt and insert more text
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(_doc.getPromptPos()); } });
    _model.replSystemOutPrint("b");
    Utilities.clearEventQueue();
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());
    
    _model.replSystemErrPrint("ccc");
    Utilities.clearEventQueue();
//    System.err.println("promptPos = " + _doc.getPromptPos() + " caretPos = " + _pane.getCaretPosition());
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());
    
    // Move caret after prompt and insert more text
    final int newPos = _doc.getPromptPos();

    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        // simulate a keystroke by putting caret just *after* pos of insert
        _pane.setCaretPosition(newPos + 1);
//        _controller.setCachedCaretPos(newPos + 1);
        // Type 'D'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'D'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
      } 
    });
    Utilities.clearEventQueue();
    assertEquals("caret should be one char after the inserted D", newPos + 2, _pane.getCaretPosition());
  }
  
  public void testSystemIn_NOJOIN() {
    final Object bufLock = new Object();
    final StringBuilder buf = new StringBuilder();
    
    final CompletionMonitor completionMonitor = new CompletionMonitor();
    
    _controller.addConsoleStateListener(new InteractionsController.ConsoleStateListener() {
      public void consoleInputStarted(InteractionsController c) {
        completionMonitor.signal();
      }     
      public void consoleInputCompleted(String text, InteractionsController c) {
        // do not assert the text here since it won't be called from the testing thread.
        // It is called on the following thread that calls getConsoleInput()
      }
    });
    
    // TODO: can we somehow interrupt this thread to allow us to join with it?
    new Thread("Testing System.in") {
      public void run() {
        synchronized(bufLock) {
          String s = _controller.getInputListener().getConsoleInput();
          buf.append(s);
        }
      }
    }.start();
    
    // Wait for console input to begin
    completionMonitor.attemptEnsureSignaled();
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        _controller.insertConsoleText("test-text"); 
        _controller.interruptConsoleInput();
      }
    });
    
    // Make sure the buffer 'buf' is updated
    synchronized(bufLock) {
      // we don't expect the newline anymore, this is now added by the input box
      assertEquals("Should have returned the correct text.", "test-text", buf.toString());
    }
  }
  
  /**
   * Tests basic functionality of undo/redo
   */
  public void testUndoRedoWorks() {
    final String oldText = _doc.getText();
    final InteractionsDJDocument doc = (InteractionsDJDocument)_pane.getDJDocument();
    Utilities.invokeAndWait(new Runnable(){
      public void run() {
        _doc.append("Undo test text 1",InteractionsDocument.DEFAULT_STYLE);
        String newText = _doc.getText();
        doc.getUndoManager().undo();
        assertEquals("Undo did not remove added text",_doc.getText(),oldText);
        doc.getUndoManager().redo();
        assertEquals("Redo did not add back the added text",newText,_doc.getText());
        doc.getUndoManager().undo();
        assertEquals("Undo did not remove added text after redo",_doc.getText(),oldText);
      }
    });
  }
 
  /** Tests to see if SHIFT_ENTER starts a new undo action  */
  public void testUndoRedoNewLine() {
    final String oldText = _doc.getText();
    final InteractionsDJDocument doc = (InteractionsDJDocument)_pane.getDJDocument();
    
    Utilities.invokeAndWait(new Runnable(){
      public void run() {
        _doc.append("Undo test text",InteractionsDocument.DEFAULT_STYLE);
        
        
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_ENTER, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_ENTER, UNDEFINED));
        
        String newOldText = _doc.getText();
        
        _doc.append("More text",InteractionsDocument.DEFAULT_STYLE);
        
        doc.getUndoManager().undo();
        
        assertEquals("Undo did not remove added text after line change", newOldText, _doc.getText());
        
        doc.getUndoManager().undo();
        doc.getUndoManager().undo();
        
        assertEquals("Didn't Undo original typing and SHIFT_ENTER call", oldText, _doc.getText());
      }
    });
  }                
  // NOT USED
//  /** Fields used in a closure in testPromptList */
//  private volatile int _firstPrompt, _secondPrompt, _size;
//  private volatile boolean _resetDone;
//  
//  public void testPromptListClearedOnReset() throws Exception {
//    // Can't use the fields declared in setUp; we need a real InteractionsModel
//    final MainFrame _mf = new MainFrame();
//    final Object _resetLock = new Object();
//    
//    Utilities.clearEventQueue();
//    GlobalModel gm = _mf.getModel();
//    _controller = _mf.getInteractionsController();
//    _model = gm.getInteractionsModel();
//    _adapter = gm.getSwingInteractionsDocument();
//    _doc = gm.getInteractionsDocument();
//    _pane = _mf.getInteractionsPane();
//    
//    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.resetPrompts(); } });
//    
//    Utilities.clearEventQueue();
//
////    System.err.println(_pane.getPromptList());
//    assertEquals("PromptList before insert should contain 0 elements", 0, _pane.getPromptList().size());
//        
//    // Insert some text 
//    _doc.append("5", InteractionsDocument.NUMBER_RETURN_STYLE);
//
//    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(_doc.getLength()); } });
////    System.err.println(_pane.getPromptList());
//    
//    Utilities.clearEventQueue();
//    
//    assertEquals("PromptList after insert should contain 1 element", 1, _pane.getPromptList().size());    
//    assertEquals("First prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length(),
//                 (int)_pane.getPromptList().get(0)); //needs cast to prevent ambiguity
//    
//    _doc.insertPrompt();
//    Utilities.clearEventQueue();
//    
//    assertEquals("PromptList has length 2", 2, _pane.getPromptList().size());
//    
//    Utilities.invokeAndWait(new Runnable() {
//      public void run() { 
//        _pane.setCaretPosition(_doc.getLength());
//        _firstPrompt = (int) _pane.getPromptList().get(0); // cast prevents ambiguity
//        _secondPrompt = (int) _pane.getPromptList().get(1); // cast prevents ambiguity
//      }
//    });
//    
//    assertEquals("PromptList after insertion of new prompt should contain 2 elements", 2, _pane.getPromptList().size());
//    assertEquals("First prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length(),
//                 _firstPrompt); 
//    assertEquals("Second prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length() * 2 + 1,
//                 _secondPrompt); 
//    
//    synchronized(_resetLock) { _resetDone = false; }
//    _model.addListener(new DummyInteractionsListener() {
//      public void interpreterReady(File wd) {
//        synchronized(_resetLock) {
//          _resetDone = true;
//          _resetLock.notifyAll();
//        }
//      }});
//      
////    System.err.println("Executing reset interpreter");  
//    _model.resetInterpreter(FileOption.NULL_FILE);
//    Utilities.clearEventQueue();
// 
//    /* Wait until reset has finished. Reset is started just before interpreterReady notification. */
//    synchronized(_resetLock) { while (! _resetDone) _resetLock.wait(); }
//    Utilities.clearEventQueue();
// 
//    // wait until the reset operation (which is queued ahead of us) has grabbed the WriteLock
//    Utilities.invokeAndWait(new Runnable() { public void run() {  _size = _pane.getPromptList().size(); } });
//      
//    Utilities.clearEventQueue();
////    System.err.println("PromptList for pane " + _pane.hashCode() + " is " + _pane.getPromptList());
//    
//    assertEquals("PromptList after reset should contain one element", 1, _size);
//  }
  
}
