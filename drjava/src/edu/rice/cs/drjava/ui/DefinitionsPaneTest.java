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

import  junit.framework.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.Date;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Tests the Definitions Pane
 * @version $Id$
 */
public final class DefinitionsPaneTest extends TestCase {

  private MainFrame _frame;

  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    DrJava.getConfig().resetToDefaults();
    _frame = new MainFrame();
  }

  public void tearDown() {
    _frame.dispose();
    _frame = null;
    System.gc();
  }

  /**
   * Tests that shift backspace works the same as backspace.
   * (Ease of use issue 693253)
   *
   * Ideally, this test would be a bit lighter weight, and not require
   * the creation of an entire MainFrame+GlobalModel.  Some refactoring
   * is in order...
   *
   * NOTE: This test doesn't work yet, since we can't currently bind
   * two keys to the same action.  This should be implemented as part of
   * feature request 683300.
   */
  public void testShiftBackspace() throws BadLocationException {
    DefinitionsPane definitions = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = definitions.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");
    doc.insertString(0, "test", null);

    definitions.setCaretPosition(4);
    int shiftBackspaceCode =
      OptionConstants.KEY_DELETE_PREVIOUS.getDefault().getKeyCode();
    // The following is the sequence of key events for shift+backspace
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             shiftBackspaceCode));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             shiftBackspaceCode));
    _assertDocumentContents(doc, "tes", "Did not delete on shift+backspace");


    int shiftDeleteCode =
      OptionConstants.KEY_DELETE_NEXT.getDefault().getKeyCode();
    definitions.setCaretPosition(1);
    // The following is the sequence of key events for shift+delete
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             shiftDeleteCode));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             shiftDeleteCode));
    _assertDocumentContents(doc, "ts", "Did not delete on shift+delete");
  }

  /**
   * Tests that typing a brace in a string/comment does not cause an indent.
   */
  public void testTypeBraceNotInCode() throws BadLocationException {
    DefinitionsPane definitions = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = definitions.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");
    doc.insertString(0, "  \"", null);

    definitions.setCaretPosition(3);
    // The following is the sequence of key events for a left brace
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_UNDEFINED, '{'));
    _assertDocumentContents(doc, "  \"{", "Brace should not indent in a string");
  }

  /**
   * Tests that typing Enter in a string/comment does cause an indent.
   * This behavior works in practice, but I can't get the test to work.
   *
   * If we use definitions.processKeyEvent, the caret position is not
   * updated, so the " * " is not inserted.  If we try to dispatchEvent
   * from the EventDispatchingThread, it hangs...?
   *
  public void testTypeEnterNotInCode() throws BadLocationException,
    InterruptedException, java.lang.reflect.InvocationTargetException {
    final DefinitionsPane definitions = _frame.getCurrentDefPane();
    _frame.show();
    OpenDefinitionsDocument doc = definitions.getOpenDefDocument();
    _assertDocumentEmpty(doc, "before testing");
    doc.insertString(0, "/**", null);

    definitions.setCaretPosition(3);
    // The following is the sequence of key events for Enter
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        definitions.dispatchEvent(new KeyEvent(definitions,
                                               KeyEvent.KEY_PRESSED,
                                               (new Date()).getTime(),
                                               0,
                                               KeyEvent.VK_ENTER));
        definitions.dispatchEvent(new KeyEvent(definitions,
                                               KeyEvent.KEY_RELEASED,
                                               (new Date()).getTime(),
                                               0,
                                               KeyEvent.VK_ENTER));
      }
    });
    _assertDocumentContents(doc, "/**\n * ", "Enter should indent in a comment");
  }*/

  /**
   * Tests that a simulated key press with the meta modifier is correct
   * Reveals bug 676586
   */
  public void testMetaKeyPress() throws BadLocationException {
    DefinitionsPane definitions = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = definitions.getOpenDefDocument();
    _assertDocumentEmpty(doc, "point 0");
    // The following is the sequence of key events that happen when the user presses Meta-a
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

  /**
   * tests that undoing/redoing a multi-line comment/uncomment will restore
   * the caret position
   */
  public void testMultilineCommentOrUncommentAfterScroll() throws BadLocationException {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    String text =
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

    int newPos = 20;

    doc.insertString(0, text, null);
    assertEquals("insertion",text, doc.getText(0,doc.getLength()));

    doc.commentLines(0,doc.getLength());
    assertEquals("commenting",commented, doc.getText(0,doc.getLength()));
    int oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    doc.getUndoManager().undo();
    assertEquals("undo commenting",text, doc.getText(0,doc.getLength()));
    assertEquals("undoing commenting restores caret position", oldPos, pane.getCaretPosition());
    pane.setCaretPosition(newPos);
    doc.getUndoManager().redo();
    assertEquals("redo commenting",commented, doc.getText(0,doc.getLength()));
    assertEquals("redoing commenting restores caret position", oldPos, pane.getCaretPosition());

    doc.uncommentLines(0,doc.getLength());
    assertEquals("uncommenting",text, doc.getText(0,doc.getLength()));
    oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    doc.getUndoManager().undo();
    assertEquals("undo uncommenting",commented, doc.getText(0,doc.getLength()));
    assertEquals("undoing uncommenting restores caret position", oldPos, pane.getCaretPosition());
    pane.setCaretPosition(newPos);
    doc.getUndoManager().redo();
    assertEquals("redo uncommenting",text, doc.getText(0,doc.getLength()));
    assertEquals("redoing uncommenting restores caret position", oldPos, pane.getCaretPosition());
  }

  protected void _assertDocumentEmpty(Document doc, String message)
    throws BadLocationException
  {
    _assertDocumentContents(doc, "", message);
  }

  protected void _assertDocumentContents(Document doc,
                                       String contents,
                                       String message)
    throws BadLocationException
  {
    assertEquals(message, contents, doc.getText(0, doc.getLength()));
  }

  public void testGranularUndo() throws BadLocationException {
    DefinitionsPane definitions = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = definitions.getOpenDefDocument();
//    doc.addUndoableEditListener(doc.getUndoManager());

    // 1
    assertEquals("Should start out empty.", "",
                 doc.getText(0, doc.getLength()));

    // Type in consecutive characters and see if they are all undone at once.
    // Type 'a'
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_UNDEFINED, 'a'));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED));
    definitions.setCaretPosition(doc.getLength());

    // Type '!'
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.CHAR_UNDEFINED));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_UNDEFINED, '!'));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.CHAR_UNDEFINED));
    definitions.setCaretPosition(doc.getLength());

    // Type 'B'
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             KeyEvent.VK_B, KeyEvent.CHAR_UNDEFINED));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_UNDEFINED, 'B'));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             InputEvent.SHIFT_MASK,
                                             KeyEvent.VK_B, KeyEvent.CHAR_UNDEFINED));
    definitions.setCaretPosition(doc.getLength());

    // Type '9'
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_9, KeyEvent.CHAR_UNDEFINED));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_UNDEFINED, '9'));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_RELEASED,
                                             (new Date()).getTime(),
                                             0,
                                             KeyEvent.VK_9, KeyEvent.CHAR_UNDEFINED));
    definitions.setCaretPosition(doc.getLength());
    assertEquals("The text should have been inserted", "a!B9",
                 doc.getText(0, doc.getLength()));

    // Call the undoAction in MainFrame through the KeyBindingManager.
    final KeyStroke ks = DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO);
    final Action a = KeyBindingManager.Singleton.get(ks);

    final KeyEvent e = new KeyEvent(definitions,
                                    KeyEvent.KEY_PRESSED,
                                    0,
                                    ks.getModifiers(),
                                    ks.getKeyCode(),
                                    KeyEvent.CHAR_UNDEFINED);
    definitions.processKeyEvent(e);
//                              ks.getKeyChar());
    // Performs the action a
//    SwingUtilities.notifyAction(a, ks, e, e.getSource(), e.getModifiers());
//    doc.getUndoManager().undo();
    assertEquals("Should have undone correctly.", "",
                 doc.getText(0, doc.getLength()));

    // 2
    /* Test bug #905405 Undo Alt+Anything Causes Exception */

    // Type 'Alt-B'
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_PRESSED,
                                             (new Date()).getTime(),
                                             InputEvent.ALT_MASK,
                                             KeyEvent.VK_Q, KeyEvent.CHAR_UNDEFINED));
    definitions.processKeyEvent(new KeyEvent(definitions,
                                             KeyEvent.KEY_TYPED,
                                             (new Date()).getTime(),
                                             InputEvent.ALT_MASK,
                                             KeyEvent.VK_UNDEFINED, 'Q'));
    definitions.processKeyEvent(new KeyEvent(definitions,
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


    // 2
    /* This part doesn't work right now because by just calling processKeyEvent we
     * have to manually move the caret, and the UndoWithPosition is off by one.  This
     * bites us since when the backspace is done, the backspace undo position is
     * still at position 1 which doesn't exist in the document anymore.
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
                 doc.getText(0, doc.getLength()));

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
                 doc.getText(0, doc.getLength()));

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
                 doc.getText(0, doc.getLength()));*/
  }
  
  
  public void testActiveAndInactive(){
    SingleDisplayModel _model = _frame.getModel();
    
    _model.newFile();

    DefinitionsPane currpane, oldpane;
    Document ddoc, olddoc;
    
    currpane = _frame.getCurrentDefPane();
    
    ddoc = currpane.getDocument();
    assertTrue("the active pane should have an open defintions document", ddoc instanceof OpenDefinitionsDocument);
    
    _model.setActiveNextDocument();
    oldpane = currpane;
    currpane = _frame.getCurrentDefPane();
    olddoc = oldpane.getDocument();
    ddoc = currpane.getDocument();
    assertFalse("the old pane should not have an open defintions document", olddoc instanceof OpenDefinitionsDocument);
    assertTrue("the active pane should have an open defintions document", ddoc instanceof OpenDefinitionsDocument);
  }
  
  
  private int _finalCount;
  private int _finalDocCount;
  public void testDocumentPaneMemoryLeak()  throws InterruptedException, java.io.IOException{
    _finalCount = 0;
    _finalDocCount = 0;
    
    
    FinalizationListener<DefinitionsPane> fl = new FinalizationListener<DefinitionsPane>(){
      public void finalized(FinalizationEvent<DefinitionsPane> e){
        _finalCount++;
      }
    };
    
    FinalizationListener<DefinitionsDocument> fldoc = new FinalizationListener<DefinitionsDocument>(){
      public void finalized(FinalizationEvent<DefinitionsDocument> e){
        _finalDocCount++;
      }
    };

    SingleDisplayModel _model = _frame.getModel();
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
    _model.newFile().addFinalizationListener(fldoc);
    _frame.getCurrentDefPane().addFinalizationListener(fl);
    
    
    
    // all the panes have a listener, so lets close all files
    
    _frame.closeAll();
    
    System.gc();
    Thread.sleep(100);
    assertEquals("all the panes have been garbage collected", 5, _finalCount);
    assertEquals("all the defdocs have been garbage collected", 5, _finalDocCount);
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

