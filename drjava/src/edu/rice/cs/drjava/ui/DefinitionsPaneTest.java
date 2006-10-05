/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.MultiThreadedTestCase;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/** Tests the Definitions Pane
  * @version $Id$
  */
public final class DefinitionsPaneTest extends MultiThreadedTestCase {

  private volatile MainFrame _frame;
  
  private static Log _log = new Log("DefinitionsPaneTest.txt", false);
  
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        DrJava.getConfig().resetToDefaults();
        _frame = new MainFrame(); 
      } 
    });
  }
  
  public void tearDown() throws Exception {
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _frame.dispose();
        _frame = null;
      }
    });
    super.tearDown();
  }
  
  /** Tests that shift backspace works the same as backspace. (Ease of use issue 693253).  Ideally, this test should
    * be lighter weight, and not require the creation of an entire MainFrame+GlobalModel.  Refactor?
    * NOTE: This test doesn't work yet, since we can't currently bind two keys to the same action.  This should be 
    * implemented as part of feature request 683300.
    */
  public void testShiftBackspace() throws BadLocationException {
//    _log.log("Starting testShiftBackSpace");
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    final char undefined = KeyEvent.CHAR_UNDEFINED;
    final int pressed = KeyEvent.KEY_PRESSED;
    final int released = KeyEvent.KEY_RELEASED;
    final int shift = InputEvent.SHIFT_MASK;
    
    _assertDocumentEmpty(doc, "before testing");
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append("test", null);
        defPane.setCaretPosition(4);
    
        int shiftBksp = OptionConstants.KEY_DELETE_PREVIOUS.getDefault().getKeyCode();
        
        // The following is the sequence of key events for shift+backspace
        defPane.processKeyEvent(new KeyEvent(defPane, pressed, (new Date()).getTime(), shift, shiftBksp, undefined));
        _log.log("first key event processed");
        defPane.processKeyEvent(new KeyEvent(defPane, released, (new Date()).getTime(), shift, shiftBksp, undefined));
        _log.log("second key event processed");
        _assertDocumentContents(doc, "tes", "Did not delete on shift+backspace");
        _log.log("Halfway through testShiftBackspace");
 
        int shiftDel = OptionConstants.KEY_DELETE_NEXT.getDefault().getKeyCode();
        defPane.setCaretPosition(1);
        // The following is the sequence of key events for shift+delete
        defPane.processKeyEvent(new KeyEvent(defPane, pressed, (new Date()).getTime(), shift, shiftDel, undefined));
        defPane.processKeyEvent(new KeyEvent(defPane, released, (new Date()).getTime(), shift, shiftDel, undefined));
        _assertDocumentContents(doc, "ts", "Did not delete on shift+delete");
        _log.log("testShiftBackSpace completed");
      }
    });
    

  }

  
  /** Tests that typing a brace in a string/comment does not cause an indent. */
  public void testTypeBraceNotInCode() throws BadLocationException {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");
    _log.log("calling invokeAndWait in testTypeBraceNotInCode");
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append("  \"", null);
        defPane.setCaretPosition(3);
        // The following is the sequence of key events for a left brace
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_TYPED, (new Date()).getTime(), 0, 
                                                 KeyEvent.VK_UNDEFINED, '{'));
      }
    });
        
    _assertDocumentContents(doc, "  \"{", "Brace should not indent in a string");
    _log.log("testTypeBraceNotInCode completed");
  }
  
  /** Tests that typing Enter in a string/comment does cause an indent.  This behavior works in practice, but I can't 
    * get the test to work.  If we use definitions.processKeyEvent, the caret position is not updated, so the " * " 
    * is not inserted.  If we try to dispatchEvent from the EventDispatchingThread, it hangs...?
    */
  public void testTypeEnterNotInCode() throws BadLocationException, InterruptedException, InvocationTargetException {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    _frame.setVisible(true);
    OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");
    doc.insertString(0, "/**", null);
    
    defPane.setCaretPosition(3);
    // The following is the sequence of key events for Enter
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        int enter = KeyEvent.VK_ENTER;
        defPane.dispatchEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(), 0, enter, KeyEvent.CHAR_UNDEFINED));
        defPane.dispatchEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(), 0, enter, KeyEvent.CHAR_UNDEFINED));
      }
    });
    _assertDocumentContents(doc, "/**\n * ", "Enter should indent in a comment");
    _log.log("testTypeEnterNotInCode completed");
  }
  
  /** Tests that a simulated key press with the meta modifier is correct.  Reveals bug 676586. */
  public void testMetaKeyPress() throws BadLocationException {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    _assertDocumentEmpty(doc, "point 0");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        // The following is the sequence of key events that happen when the user presses Meta-a
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(),
                                                 InputEvent.META_MASK, KeyEvent.VK_META, KeyEvent.CHAR_UNDEFINED));
      }
    });
        
    _assertDocumentEmpty(doc, "point 1");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(),
                                                 InputEvent.META_MASK, KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED));
      }
    });
    
    _assertDocumentEmpty(doc, "point 2");
        
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_TYPED, (new Date()).getTime(),
                                                 InputEvent.META_MASK, KeyEvent.VK_UNDEFINED, 'w'));
        
      }
    });
    
    _assertDocumentEmpty(doc, "point 3");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(),
                                                 InputEvent.META_MASK, KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED));
        
      }
    });
    
    _assertDocumentEmpty(doc, "point 4");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(),
                                                 0, KeyEvent.VK_META, KeyEvent.CHAR_UNDEFINED));
      }
    });
    
    _assertDocumentEmpty(doc, "point 5");
    
    _log.log("testMetaKeyPress completed");
  }
  
  /** Tests that undoing/redoing a multi-line comment/uncomment will restore the caret position */
  public void testMultilineCommentOrUncommentAfterScroll() throws BadLocationException {
    
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    final String text =
      "public class stuff {\n" +
      "  private int _int;\n" +
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";
    
    String commented =
      "//public class stuff {\n" +
      "//  private int _int;\n" +
      "//  private Bar _bar;\n" +
      "//  public void foo() {\n" +
      "//    _bar.baz(_int);\n" +
      "//  }\n" +
      "//}\n";
    
    final int newPos = 20;
    
    // The following statement hung when run in the main test thread.  There must be a pending access to doc in a
    // task on the event queue that sometimes has not yet executed.
    
    Utilities.invokeAndWait(new Runnable() { public void run() { doc.append(text, null); } });
    
    assertEquals("insertion", text, doc.getText());
    
    // Need to do this here since the commentLines action in MainFrame usually takes care of this.  
    // I can't run the test here because I'm not sure how to select the text so that we can comment it.

    Utilities.invokeAndWait(new Runnable() { public void run() { pane.endCompoundEdit(); } });
     
    doc.acquireWriteLock();
    try { doc.commentLines(0, doc.getLength()); }
    finally { doc.releaseWriteLock(); }
    
    //    pane.endCompoundEdit();
    assertEquals("commenting", commented, doc.getText());
    
    int oldPos = pane.getCaretPosition();
    
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.setCaretPosition(newPos); } });
    
    doc.getUndoManager().undo();  
    assertEquals("undo commenting", text, doc.getText());
    assertEquals("undoing commenting restores caret position", oldPos, pane.getCaretPosition());
    
    // Perturb the caret position and redo
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.setCaretPosition(newPos); } });
    doc.getUndoManager().redo();
    assertEquals("redo commenting", commented, doc.getText());
    assertEquals("redoing commenting restores caret position", oldPos, pane.getCaretPosition());
    
    // Need to do this here since the commentLines action in MainFrame usually takes care of this.  
    // I can't simulate a keystroke here because I'm not sure how to select the text so that we can comment it.
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.endCompoundEdit(); } });
    
    doc.acquireWriteLock();
    try { doc.uncommentLines(0, doc.getLength()); }
    finally { doc.releaseWriteLock(); }
    
    //    pane.endCompoundEdit();
    assertEquals("uncommenting", text, doc.getText());
    
    oldPos = pane.getCaretPosition();  // executing this method call outside of the event thread is borderline
    
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.setCaretPosition(newPos);  } });
    doc.getUndoManager().undo();
    
    assertEquals("undo uncommenting", commented, doc.getText());
    assertEquals("undoing uncommenting restores caret position", oldPos, pane.getCaretPosition());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.setCaretPosition(newPos); } });
    doc.getUndoManager().redo();
    assertEquals("redo uncommenting",text, doc.getText());
    assertEquals("redoing uncommenting restores caret position", oldPos, pane.getCaretPosition());
    
    _log.log("testMultiLineCommentOrUncommentAfterScroll completed");
  }
  
  protected void _assertDocumentEmpty(DJDocument doc, String message) {
    _assertDocumentContents(doc, "", message);
  }
  
  protected void _assertDocumentContents(DJDocument doc, String contents, String message) {
    assertEquals(message, contents, doc.getText());
  }
  
  public void testGranularUndo() throws BadLocationException {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    //    doc.addUndoableEditListener(doc.getUndoManager());
    
    // 1
    assertEquals("Should start out empty.", "",  doc.getText());
    
    // Type in consecutive characters and see if they are all undone at once.
    // Type 'a'
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_TYPED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_UNDEFINED, 'a'));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED));
        defPane.setCaretPosition(doc.getLength());
        
        // Type '!'
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_PRESSED,
                                                 (new Date()).getTime(),
                                                 0,
                                                 KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_TYPED,
                                                 (new Date()).getTime(),
                                                 0,
                                                 KeyEvent.VK_UNDEFINED, '!'));
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_RELEASED,
                                                 (new Date()).getTime(),
                                                 0,
                                                 KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.CHAR_UNDEFINED));
        defPane.setCaretPosition(doc.getLength());
        
        // Type 'B'
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_PRESSED,
                                                 (new Date()).getTime(),
                                                 InputEvent.SHIFT_MASK,
                                                 KeyEvent.VK_B, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_TYPED,
                                                 (new Date()).getTime(),
                                                 0,
                                                 KeyEvent.VK_UNDEFINED, 'B'));
        defPane.processKeyEvent(new KeyEvent(defPane,
                                                 KeyEvent.KEY_RELEASED,
                                                 (new Date()).getTime(),
                                                 InputEvent.SHIFT_MASK,
                                                 KeyEvent.VK_B, KeyEvent.CHAR_UNDEFINED));
        defPane.setCaretPosition(doc.getLength());
        
        // Type '9'
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_9, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_TYPED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_UNDEFINED, '9'));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_9, KeyEvent.CHAR_UNDEFINED));
        defPane.setCaretPosition(doc.getLength());
      } 
    });
    
    assertEquals("The text should have been inserted", "a!B9",  doc.getText());
    
    // Call the undoAction in MainFrame through the KeyBindingManager.
    final KeyStroke ks = DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO);
    final Action a = KeyBindingManager.Singleton.get(ks);
    
    final KeyEvent e = new KeyEvent(defPane, KeyEvent.KEY_PRESSED, 0, ks.getModifiers(), ks.getKeyCode(),
                                    KeyEvent.CHAR_UNDEFINED);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.processKeyEvent(e); } });
  
    assertEquals("Should have undone correctly.", "", doc.getText());
    
    // 2
    /* Test bug #905405 Undo Alt+Anything Causes Exception */
    
    // What does the following code test?  There are no assertions!  -- Corky 5/9/06
    
    // Type 'Alt-B'
     Utilities.invokeAndWait(new Runnable() {
       public void run() {
         defPane.processKeyEvent(new KeyEvent(defPane,
                                                  KeyEvent.KEY_PRESSED,
                                                  (new Date()).getTime(),
                                                  InputEvent.ALT_MASK,
                                                  KeyEvent.VK_Q, KeyEvent.CHAR_UNDEFINED));
         defPane.processKeyEvent(new KeyEvent(defPane,
                                                  KeyEvent.KEY_TYPED,
                                                  (new Date()).getTime(),
                                                  InputEvent.ALT_MASK,
                                                  KeyEvent.VK_UNDEFINED, 'Q'));
         defPane.processKeyEvent(new KeyEvent(defPane,
                                                  KeyEvent.KEY_RELEASED,
                                                  (new Date()).getTime(),
                                                  InputEvent.ALT_MASK,
                                                  KeyEvent.VK_Q, KeyEvent.CHAR_UNDEFINED));
         
         /*
          * If the bug is not fixed in DefinitionsPane.processKeyEvent, this test
          * will not fail because the exception is thrown in another thread.
          * However, the stack trace will get printed onto the console.  I don't
          * know how to fix this problem in case someone unfixes the bug.
          */
         SwingUtilities.notifyAction(a, ks, e, e.getSource(), e.getModifiers());
    //    definitions.setCaretPosition(doc.getLength());
       }
     });
    
    
    // 2
    /* This part doesn't work right now because by just calling processKeyEvent we
     * have to manually move the caret, and the UndoWithPosition is off by one.  This
     * bites us since when the backspace is done, the backspace undo position is
     * still at position 1 which doesn't exist in the document anymore.
     *
     * 
     // Test undoing backspace.
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_PRESSED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED));
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_TYPED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_UNDEFINED, 'a'));
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_RELEASED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED));
     definitions.setCaretPosition(doc.getLength());
     
     assertEquals("The text should have been inserted", "a",
     doc.getText());
     
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_PRESSED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED));
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_TYPED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_UNDEFINED, '\010'));
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_RELEASED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED));
     System.out.println(definitions.getCaretPosition());
     definitions.setCaretPosition(doc.getLength());
     
     assertEquals("The text should have been deleted", "",
     doc.getText());
     
     // Call the undoAction in MainFrame through the KeyBindingManager.
     //    KeyStroke ks = DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO);
     //    Action a = KeyBindingManager.Singleton.get(ks);
     //    KeyEvent e = new KeyEvent(definitions,
     //                              KeyEvent.KEY_PRESSED,
     //                              0,
     //                              ks.getModifiers(),
     //                              ks.getKeyCode(),
     //                              ks.getKeyChar());
     // Performs the action a
     definitions.processKeyEvent(new KeyEvent(definitions,
     KeyEvent.KEY_PRESSED,
     (new Date()).getTime(),
     ks.getModifiers(),
     ks.getKeyCode(), KeyEvent.CHAR_UNDEFINED));
     //    doc.getUndoManager().undo();
     assertEquals("Should have undone correctly.", "a",
     doc.getText());*/
     
     _log.log("testGranularUndo completed");
  }
  
  
  public void testActiveAndInactive() {
    SingleDisplayModel _model = _frame.getModel();  // creates a frame with a new untitled document and makes it active
    
    DefinitionsPane pane1, pane2;
    DJDocument doc1, doc2;
    
    pane1 = _frame.getCurrentDefPane(); 
    doc1 = pane1.getDJDocument();
    assertTrue("the active pane should have an open definitions document", doc1 instanceof OpenDefinitionsDocument);
    
    _model.newFile();  // creates a new untitled document and makes it active
    pane2 = _frame.getCurrentDefPane();  
    doc2 = pane2.getDJDocument();
    
    assertTrue("the active pane should have an open definitions document", doc2 instanceof OpenDefinitionsDocument);
    
    _model.setActiveNextDocument();    // makes doc1 active
    DefinitionsPane pane = _frame.getCurrentDefPane();
    assertEquals("Confirm that next pane is the other pane", pane1, pane);
    
    assertTrue("pane2 should have an open definitions document", doc2 instanceof OpenDefinitionsDocument);
    assertTrue("pane1 should have an open definitions document", doc1 instanceof OpenDefinitionsDocument);
    
    _log.log("testActiveAndInactive completed");
  }
  
  
  private int _finalCount;
  private int _finalDocCount;
  
  public void testDocumentPaneMemoryLeak()  throws InterruptedException, java.io.IOException{
    _finalCount = 0;
    _finalDocCount = 0;
    
    FinalizationListener<DefinitionsPane> fl = new FinalizationListener<DefinitionsPane>() {
      public void finalized(FinalizationEvent<DefinitionsPane> e) {
        _finalCount++;
//        System.out.println("Finalizing: " + e.getObject().hashCode());
      }
    };
    
    FinalizationListener<DefinitionsDocument> fldoc = new FinalizationListener<DefinitionsDocument>() {
      public void finalized(FinalizationEvent<DefinitionsDocument> e) {
        _finalDocCount++;
      }
    };
    
    SingleDisplayModel _model = _frame.getModel();
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
//    System.out.println("Created File: " + _frame.getCurrentDefPane().hashCode());
    
    // all the panes have a listener, so lets close all files
    
    _model.closeAllFiles();
    
    // make sure that the event queue is empty (can we explicity test this condition?)
    Utilities.clearEventQueue();
    Utilities.clearEventQueue();
    
    System.gc();
    System.runFinalization();
    
    Utilities.clearEventQueue();   
    
    System.gc();
    System.runFinalization();
//    System.out.println("Current: " + _frame.getCurrentDefPane().hashCode());
    
//    System.out.println("Foo");
//    System.in.read();
    assertEquals("all the defdocs should have been garbage collected", 6, _finalDocCount);
    assertEquals("all the panes should have been garbage collected", 6, _finalCount);
//    System.err.println("_finalCount = " + _finalCount);
    
    _log.log("testDocumentPaneMemoryLeak completed");
  }
  
  // This testcase checks that we do no longer discard Alt keys that would be used to make the {,},[,] chars that the french keyboards has.
  // Using the Locale did not work, and checking if the key was consumed by the document would only pass on the specific keyboards.
  // It was therefore unavoidable to add a few lines of code in the original code that is only used for this test case.
  // These lines were added to the DefinitionsPane.java file.
  public void testFrenchKeyStrokes() throws IOException, InterruptedException {
    
    final DefinitionsPane pane = _frame.getCurrentDefPane(); // pane is NOT null.
    //KeyEvent ke = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, InputEvent.ALT_MASK, KeyEvent.VK_UNDEFINED, '{'); 
    final KeyEvent ke1 = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'T'); 
    
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.processKeyEvent(ke1); } });
    assertFalse("The KeyEvent for pressing \"T\" should not involve an Alt Key if this fails we are in trouble!", pane.checkAltKey());
    
    final KeyEvent ke2 = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, InputEvent.ALT_MASK, KeyEvent.VK_UNDEFINED, '{'); 
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.processKeyEvent(ke2); } });
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    final KeyEvent ke3 = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, InputEvent.ALT_MASK, KeyEvent.VK_UNDEFINED, '}'); 
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.processKeyEvent(ke3); } });
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    
    final KeyEvent ke4 = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, InputEvent.ALT_MASK, KeyEvent.VK_UNDEFINED, '['); 
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.processKeyEvent(ke4); } });
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    final KeyEvent ke5 = new KeyEvent(pane, KeyEvent.KEY_TYPED, 0, InputEvent.ALT_MASK, KeyEvent.VK_UNDEFINED, ']'); 
    Utilities.invokeAndWait(new Runnable() { public void run() { pane.processKeyEvent(ke5);  } });
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    _log.log("testFrenchKeyStrokes completed");
  } 

/* We had several problems with the backspace deleting 2 chars instead of one.
 * Recently the problem reoccured in Java version 1.4, but not in 1.5
 * This shows that we clearly needs a test for this.
 */
  public void testBackspace() {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");

    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append("test", null);
        defPane.setCaretPosition(4);
        int backspaceCode = KeyEvent.VK_BACK_SPACE;
        // The following is the sequence of key events for backspace
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_PRESSED, (new Date()).getTime(), 0, 
                                                 backspaceCode, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_RELEASED, (new Date()).getTime(), 0,
                                                 backspaceCode, KeyEvent.CHAR_UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, KeyEvent.KEY_TYPED, (new Date()).getTime(), 0,
                                                 KeyEvent.VK_UNDEFINED, '\b'));
      }
    });
    
    _assertDocumentContents(doc, "tes", "Deleting with Backspace went wrong");
    
    _log.log("testBackSpace completed");
  }
  
  private volatile String _result;
  
  /** Tests the functionality that allows brace matching that displays the line matched in the status bar */
  public void testMatchBraceText() {

    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    Utilities.clearEventQueue();
    
    _assertDocumentEmpty(doc, "before testing");
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append( 
                   "{\n" +
                   "public class Foo {\n" + //21
                   "  private int whatev\n" + //42
                   "  private void _method()\n" + //67
                   "  {\n" + //71
                   "     do stuff\n" + //85
                   "     new Object() {\n" + //105
                   "         }\n" + //116
                   "  }\n" +
                   "}" +
                   "}"
                     , null);
        
        defPane.setCaretPosition(4); 
      } 
    });
    
    /* Ensure that DocumentListeners complete. */
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    
    final String fileName = doc.getCompletePath();
    assertEquals("Should display the document path", fileName, _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() {  defPane.setCaretPosition(115); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the line matched", "Matches:      new Object() {", _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(102);  } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the document matched", fileName, _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(119); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the line matched", "Matches:   private void _method()...{", _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(121); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the line matched", "Matches: public class Foo {", _frame.getFileNameField());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(122); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display only one brace when matching an open brace that is the first character in a line",
                 "Matches: {", _result);
    
    _log.log("testMatchBraceTest completed");
  }

  class KeyTestListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keypress " + e); }
    public void keyReleased(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keyrelease " + e); }
    public void keyTyped(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keytyped " + e);  }
    public boolean done() { return true; }
  }
}

