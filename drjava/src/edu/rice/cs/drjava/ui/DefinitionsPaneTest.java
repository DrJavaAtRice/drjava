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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.MultiThreadedTestCase;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Date;

/** Tests the Definitions Pane
  * @version $Id: DefinitionsPaneTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class DefinitionsPaneTest extends MultiThreadedTestCase {

  private volatile MainFrame _frame;
  
  public static final Log _log = new Log("DefinitionsPaneTest.txt", false);  // used in other tests
  
  private static final char UNDEFINED = KeyEvent.CHAR_UNDEFINED;
  private static final int PRESSED = KeyEvent.KEY_PRESSED;
  private static final int RELEASED = KeyEvent.KEY_RELEASED;
  private static final int SHIFT = InputEvent.SHIFT_MASK;
  private static final int TYPED = KeyEvent.KEY_TYPED;
  private static final int VK_UNDEF = KeyEvent.VK_UNDEFINED;
  private static final int META = KeyEvent.VK_META;
  private static final int W = KeyEvent.VK_W;
  private static final int M_MASK = InputEvent.META_MASK;
  private static final int BANG = KeyEvent.VK_EXCLAMATION_MARK;
  private static final int ALT = InputEvent.ALT_MASK;
  
  private static final int DEL_NEXT = OptionConstants.KEY_DELETE_NEXT.getDefault().get(0).getKeyCode();
  private static final int DEL_PREV = OptionConstants.KEY_DELETE_PREVIOUS.getDefault().get(0).getKeyCode();
    
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    
    /* The following use of invokeAndWait has been motivated by occasional test failures in set up (particularly in
     * MainFrame creation and packing) among different test methods in this test class. */
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        DrJava.getConfig().resetToDefaults();
        _frame = new MainFrame();
        _frame.pack(); 
      }
    });
  }
  
  public void tearDown() throws Exception {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _frame.dispose();
        _log.log("Main Frame disposed");
        _frame = null;
      }
    });
    Utilities.clearEventQueue();
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
  
    _assertDocumentEmpty(doc, "before testing");
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append("test", null);
        defPane.setCaretPosition(4);
        
        // The following is the sequence of key events for shift+backspace
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), SHIFT, DEL_PREV, UNDEFINED));
        _log.log("first key event processed");
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), SHIFT, DEL_PREV, UNDEFINED));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    
    _log.log("second key event processed");
    _assertDocumentContents(doc, "tes", "Did not delete on shift+backspace");
    _log.log("Halfway through testShiftBackspace");
    
     
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        
        defPane.setCaretPosition(1);
        // The following is the sequence of key events for shift+delete
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), SHIFT, DEL_NEXT, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), SHIFT, DEL_NEXT, UNDEFINED));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    _assertDocumentContents(doc, "ts", "Did not delete on shift+delete");
    _log.log("testShiftBackSpace completed");
   
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
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, '{'));
      }
    });
    Utilities.clearEventQueue();
        
    _assertDocumentContents(doc, "  \"{", "Brace should not indent in a string");
    _log.log("testTypeBraceNotInCode completed");
  }
  
  /** Tests that typing Enter in a string/comment does cause an indent.  This behavior works in practice, but I can't 
    * get the test to work.  If we use definitions.processKeyEvent, the caret position is not updated, so the " * " 
    * is not inserted.  If we try to dispatchEvent from the EventDispatchingThread, it hangs...?
    */
  public void testTypeEnterNotInCode() throws BadLocationException, InterruptedException, InvocationTargetException {
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
        final DefinitionsPane defPane = _frame.getCurrentDefPane();
//        _frame.setVisible(true);
        final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
        try { 
          _assertDocumentEmpty(doc, "before testing");
          doc.insertString(0, "/**", null);
          defPane.setCaretPosition(3);
          // The following is the sequence of key events for Enter
          int enter = KeyEvent.VK_ENTER;
          defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), 0, enter, UNDEFINED));
          defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, enter, UNDEFINED));
          _frame.validate();
        }
        catch(Throwable t) { listenerFail(t); }
        
        _log.log("Completed processing of keyEvents");
        
        _assertDocumentContents(doc, "/**\n * ", "Enter should indent in a comment");
        _log.log("testTypeEnterNotInCode completed");
      }
    });
  }
  
  /** Tests that a simulated key press with the meta modifier is correct.  Reveals bug 676586. */
  public void testMetaKeyPress() throws BadLocationException {
    final DefinitionsPane defPane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
    _assertDocumentEmpty(doc, "point 0");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        // The following is the sequence of key events that happen when the user presses Meta-a
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), M_MASK, META, UNDEFINED));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
        
    _assertDocumentEmpty(doc, "point 1");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), M_MASK, W, UNDEFINED));
        _frame.validate();
      }
    }); 
    Utilities.clearEventQueue();
    
    _assertDocumentEmpty(doc, "point 2");
        
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), M_MASK, VK_UNDEF, 'w'));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    
    _assertDocumentEmpty(doc, "point 3");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), M_MASK, W, UNDEFINED));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
     
    _assertDocumentEmpty(doc, "point 4");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, META, UNDEFINED));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    
    _assertDocumentEmpty(doc, "point 5");
    
    _log.log("testMetaKeyPress completed");
  }
  
  // Used to hold a document offset between successive Runnables moved to the event thread;
  private int _redoPos;
  
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
    
    final String commented =
      "//public class stuff {\n" +
      "//  private int _int;\n" +
      "//  private Bar _bar;\n" +
      "//  public void foo() {\n" +
      "//    _bar.baz(_int);\n" +
      "//  }\n" +
      "//}\n";
    
    
    // Need to do this here since the commentLines action in MainFrame usually takes care of this.  
    // I can't run the test here because I'm not sure how to select the text so that we can comment it
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append(text, null);
        assertEquals("insertion", text, doc.getText());
//        System.err.println("Compound? " + pane._inCompoundEdit);
//        System.err.println("After append, loc = " + doc.getCurrentLocation());
        pane.endCompoundEdit();
        doc.commentLines(0, doc.getLength()); 

        assertEquals("commenting", commented, doc.getText());
        int newPos = doc.getCurrentLocation();
//        System.err.println("newPos = " + newPos);

        doc.getUndoManager().undo(); 
//        System.err.println("cursor pos = " + doc.getCurrentLocation());
//        int pos = doc.getCurrentLocation();
        assertEquals("undo commenting", text, doc.getText());
//        System.err.println("cursor pos = " + pos + "\n");
//        // doc.commentLines moves the cursor to 0 before inserting wing comment chars
//
//        assertTrue("dummy test", true);
//        System.err.println("undone text = '" + doc.getText() + "'");
//        assertTrue("dummy test", true);

        assertEquals("undoing commenting restores cursor position", 0, doc.getCurrentLocation());
        
        doc.getUndoManager().redo();
        assertEquals("redo commenting", commented, doc.getText());
        assertEquals("redoing commenting restores cursor position", newPos, doc.getCurrentLocation());

        pane.endCompoundEdit(); 
        doc.uncommentLines(0, doc.getLength()); 
        assertEquals("uncommenting", text, doc.getText());

        _redoPos = doc.getCurrentLocation();  
    
        doc.getUndoManager().undo();
        
      } });
    
    // undo may spawn new events that fix up the value of _currentLocation; must break our of invokeAndWait to let
    // them execute

    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
//        System.err.println("cursor pos = " + doc.getCurrentLocation());
    
        assertEquals("undo uncommenting", commented, doc.getText());
//        System.err.println("cursor pos = " + doc.getCurrentLocation());

        // doc.uncommentLines moves the cursor to 0 before removing the wing comment chars
        assertEquals("undoing uncommenting restores cursor position", 0, doc.getCurrentLocation());
    
        doc.getUndoManager().redo();
        assertEquals("redo uncommenting",text, doc.getText());
        assertEquals("redoing uncommenting restores cursor position", _redoPos, doc.getCurrentLocation());
                                                        
//        fail("print System.err");
      }
    });
    
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
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), 0, KeyEvent.VK_A, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'a'));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, KeyEvent.VK_A, UNDEFINED));
//        defPane.setCaretPosition(doc.getLength());
        assertEquals("caret at line end", doc.getLength(), defPane.getCaretPosition());
        
        // Type '!'
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), 0, BANG, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, '!'));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, BANG, UNDEFINED));
//        defPane.setCaretPosition(doc.getLength());
        assertEquals("caret at line end", doc.getLength(), defPane.getCaretPosition());
        
        // Type 'B'
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_B, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'B'));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_B, UNDEFINED));
//        defPane.setCaretPosition(doc.getLength());
        assertEquals("caret at line end", doc.getLength(), defPane.getCaretPosition());
        
        // Type '9'
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), 0, KeyEvent.VK_9, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, '9'));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, KeyEvent.VK_9, UNDEFINED));
//        defPane.setCaretPosition(doc.getLength());
        assertEquals("caret at line end", doc.getLength(), defPane.getCaretPosition());
        _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
    
    assertEquals("The text should have been inserted", "a!B9",  doc.getText());
    
    // Call the undoAction in MainFrame through the KeyBindingManager.
    final Vector<KeyStroke> ks = DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO);
    final Action a = KeyBindingManager.ONLY.get(ks.get(0));
    
    final KeyEvent e = new KeyEvent(defPane, PRESSED, 0, ks.get(0).getModifiers(), ks.get(0).getKeyCode(), UNDEFINED);
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
      defPane.processKeyEvent(e); 
      _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
  
    assertEquals("Should have undone correctly.", "", doc.getText());
    
    // 2
    /* Test bug #905405 Undo Alt+Anything Causes Exception */
    
    // What does the following code test?  There are no assertions!  -- Corky 5/9/06
    
    // Type 'Alt-B'
     Utilities.invokeAndWait(new Runnable() {
       public void run() {
         defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), ALT, KeyEvent.VK_Q, UNDEFINED));
         defPane.processKeyEvent(new KeyEvent(defPane,
                                                  TYPED,
                                                  (new Date()).getTime(),
                                                  ALT,
                                                  VK_UNDEF, 'Q'));
         defPane.processKeyEvent(new KeyEvent(defPane,
                                                  RELEASED,
                                                  (new Date()).getTime(),
                                                  ALT,
                                                  KeyEvent.VK_Q, UNDEFINED));
         
         /*
          * If the bug is not fixed in DefinitionsPane.processKeyEvent, this test
          * will not fail because the exception is thrown in another thread.
          * However, the stack trace will get printed onto the console.  I don't
          * know how to fix this problem in case someone unfixes the bug.
          */
         SwingUtilities.notifyAction(a, ks.get(0), e, e.getSource(), e.getModifiers());
         _frame.validate();
    //    definitions.setCaretPosition(doc.getLength());
       }
     });
     Utilities.clearEventQueue();
    
    // 2
    /* This part doesn't work right now because by just calling processKeyEvent we
     * have to manually move the caret, and the UndoWithPosition is off by one.  This
     * bites us since when the backspace is done, the backspace undo position is
     * still at position 1 which doesn't exist in the document anymore.
     *
     * 
     // Test undoing backspace.
     definitions.processKeyEvent(new KeyEvent(definitions,
     PRESSED,
     (new Date()).getTime(),
     0,
     VK_UNDEF, UNDEFINED));
     definitions.processKeyEvent(new KeyEvent(definitions,
     TYPED,
     (new Date()).getTime(),
     0,
     VK_UNDEF, 'a'));
     definitions.processKeyEvent(new KeyEvent(definitions,
     RELEASED,
     (new Date()).getTime(),
     0,
     VK_UNDEF, UNDEFINED));
     definitions.setCaretPosition(doc.getLength());
     
     assertEquals("The text should have been inserted", "a",
     doc.getText());
     
     definitions.processKeyEvent(new KeyEvent(definitions,
     PRESSED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_BACK_SPACE, UNDEFINED));
     definitions.processKeyEvent(new KeyEvent(definitions,
     TYPED,
     (new Date()).getTime(),
     0,
     VK_UNDEF, '\010'));
     definitions.processKeyEvent(new KeyEvent(definitions,
     RELEASED,
     (new Date()).getTime(),
     0,
     KeyEvent.VK_BACK_SPACE, UNDEFINED));
     System.out.println(definitions.getCaretPosition());
     definitions.setCaretPosition(doc.getLength());
     
     assertEquals("The text should have been deleted", "",
     doc.getText());
     
     // Call the undoAction in MainFrame through the KeyBindingManager.
     //    KeyStroke ks = DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO);
     //    Action a = KeyBindingManager.Singleton.get(ks);
     //    KeyEvent e = new KeyEvent(definitions,
     //                              PRESSED,
     //                              0,
     //                              ks.getModifiers(),
     //                              ks.getKeyCode(),
     //                              ks.getKeyChar());
     // Performs the action a
     definitions.processKeyEvent(new KeyEvent(definitions,
     PRESSED,
     (new Date()).getTime(),
     ks.getModifiers(),
     ks.getKeyCode(), UNDEFINED));
     //    doc.getUndoManager().undo();
     assertEquals("Should have undone correctly.", "a",
     doc.getText());*/
     
     _log.log("testGranularUndo completed");
  }
  
  
  public void testActiveAndInactive() {
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
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
    });
  }
      
  /** This testcase checks that we do no longer discard Alt keys that would be used to make the {,},[,] chars that the 
    * French keyboards has.  Using the Locale did not work, and checking if the key was consumed by the document would
    * only pass on the specific keyboards.  It was therefore unavoidable to add a few lines of code in the original code
    * that is only used for this test case. These lines were added to the DefinitionsPane.java file. */
  public void testFrenchKeyStrokes() throws IOException, InterruptedException {
    
    final DefinitionsPane pane = _frame.getCurrentDefPane(); // pane is NOT null.
    //KeyEvent ke = new KeyEvent(pane, TYPED, 0, ALT, VK_UNDEF, '{'); 
    final KeyEvent ke1 = new KeyEvent(pane, TYPED, 0, 0, VK_UNDEF, 'T'); 
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.processKeyEvent(ke1); 
        _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
    
    assertFalse("The KeyEvent for pressing \"T\" should not involve an Alt Key if this fails we are in trouble!", 
                pane.checkAltKey());
    
    final KeyEvent ke2 = new KeyEvent(pane, TYPED, 0, ALT, VK_UNDEF, '{'); 
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.processKeyEvent(ke2); 
        _frame.validate();   
      } 
    });
    Utilities.clearEventQueue();
        
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    final KeyEvent ke3 = new KeyEvent(pane, TYPED, 0, ALT, VK_UNDEF, '}'); 
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.processKeyEvent(ke3); 
        _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
    
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    
    final KeyEvent ke4 = new KeyEvent(pane, TYPED, 0, ALT, VK_UNDEF, '['); 
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.processKeyEvent(ke4);
        _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
    
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    final KeyEvent ke5 = new KeyEvent(pane, TYPED, 0, ALT, VK_UNDEF, ']'); 
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.processKeyEvent(ke5); 
        _frame.validate();
      } 
    });
    Utilities.clearEventQueue();
    
    assertTrue("Alt should have been registered and allowed to pass!", pane.checkAltKey());
    
    _log.log("testFrenchKeyStrokes completed");
  } 

/* We had several problems with the backspace deleting 2 chars instead of one.
 * Recently the problem reoccured in Java version 1.4, but not in 1.5
 * This shows that we clearly needs a test for this.
 */
  public void testBackspace() {
   
    Utilities.invokeAndWait(new Runnable() { 
      
      public void run() { 
        final DefinitionsPane defPane = _frame.getCurrentDefPane();
        final OpenDefinitionsDocument doc = defPane.getOpenDefDocument();
        _assertDocumentEmpty(doc, "before testing");
        doc.append("test", null);
        defPane.setCaretPosition(4);
        final int VK_BKSP = KeyEvent.VK_BACK_SPACE;
        // The following is the sequence of key events for backspace
        defPane.processKeyEvent(new KeyEvent(defPane, PRESSED, (new Date()).getTime(), 0, VK_BKSP, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, RELEASED, (new Date()).getTime(), 0, VK_BKSP, UNDEFINED));
        defPane.processKeyEvent(new KeyEvent(defPane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, '\b'));
        _frame.validate();
        _assertDocumentContents(doc, "tes", "Deleting with Backspace went wrong");
        _log.log("testBackSpace completed");
      }
    });
    
  
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
    
    final String taggedFileName = "Editing " + doc.getCompletePath();
    assertEquals("Should display the document path", taggedFileName, _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() {  defPane.setCaretPosition(115); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the line matched", "Bracket matches:      new Object() {", _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(102);  } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the document matched", "Bracket matches:      new Object(", _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(119); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });

    assertEquals("Should display the line matched", "Bracket matches:   private void _method()...{", _result);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(121); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display the line matched", "Bracket matches: public class Foo {", _frame.getFileNameField());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { defPane.setCaretPosition(122); } });
    // Complete the actions spawned by the preceding command before executing the following command
    Utilities.invokeAndWait(new Runnable() { public void run() {  _result = _frame.getFileNameField(); } });
    assertEquals("Should display only one brace when matching an open brace that is the first character in a line",
                 "Bracket matches: {", _result);
    
    _log.log("testMatchBraceTest completed");
  }

  static class KeyTestListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keypress " + e); }
    public void keyReleased(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keyrelease " + e); }
    public void keyTyped(KeyEvent e) { DefinitionsPaneTest.fail("Unexpected keytyped " + e);  }
    public boolean done() { return true; }
  }
  
}

