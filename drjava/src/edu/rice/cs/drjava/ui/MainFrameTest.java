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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.util.List;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.compiler.CompilerListener;

/** Test functions of MainFrame.
  * @version $Id: MainFrameTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class MainFrameTest extends MultiThreadedTestCase {
  
  private volatile MainFrame _frame;
  
  /** A temporary directory */
  private volatile File _tempDir;
  
  /* Flag and lock for signalling when file has been opened and active document changed. */
  protected volatile boolean _openDone;
  protected final Object _openLock = new Object();
  
  /* Flag and lock for signalling when file has been closed. */
  protected volatile boolean _closeDone;
  protected final Object _closeLock = new Object();
  
  /* Flag and lock for signalling when compilation is done. */
  protected volatile boolean _compileDone;
  protected final Object _compileLock = new Object();
  
  private final static Log _log = new Log("MainFrameTest.txt", false);
  
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    // Perform ainFrame initialization in the event thread because the event thread is ALREADY running
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
//        _log.log("super.setUp() for next test completed");
        
        _frame  = new MainFrame();
//        _log.log("new MainFrame() for next test completed");
        EventQueue.invokeLater(new Runnable() { public void run() { _frame.pack(); } });
//        _log.log("setUp complete for next test");
      }
    });
  }
  
  public void tearDown() throws Exception {
//    Utilities.invokeLater(new Runnable() {
//      public void run() {
        _frame.dispose();
        _frame = null;
        /* try { */ MainFrameTest.super.tearDown(); /* } */
//        catch(Exception e) { throw new UnexpectedException(e); }
//      }
//    });
    super.tearDown();
  }
  
  JButton _but;
  /** Tests that the returned JButton of <code>createManualToolbarButton</code>:
    * 1. Is disabled upon return.
    * 2. Inherits the tooltip of the Action parameter <code>a</code>.
    */
  public void testCreateManualToolbarButton() {
    final Action a = new AbstractAction("Test Action") { public void actionPerformed(ActionEvent ae) { } };
    
    a.putValue(Action.LONG_DESCRIPTION, "test tooltip");
    Utilities.invokeAndWait(new Runnable() { public void run() { _but = _frame._createManualToolbarButton(a); } });
    
    assertTrue("Returned JButton is enabled.", ! _but.isEnabled());
    assertEquals("Tooltip text not set.", "test tooltip", _but.getToolTipText());
    _log.log("testCreateManualToobarButton completed");
  }
  
  /** Tests that the current location of a document is equal to the caret Position after switching to another document and back. */
  public void testDocLocationAfterSwitch() throws BadLocationException {
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    setDocText(doc.getDocument(), "abcd");
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.setCurrentLocation(3); 
        pane.setCaretPosition(3);  // The caret is not affected by setCurrentLocation
      } 
    }); 
    
    assertEquals("Location of old doc before switch", 3, doc.getCurrentLocation());
    assertEquals("Location of cursor in old document", 3, pane.getCaretPosition());
    
    // Create a new file
    SingleDisplayModel model = _frame.getModel();
    final OpenDefinitionsDocument oldDoc = doc;
    final DefinitionsPane oldPane = pane;
    final OpenDefinitionsDocument newDoc = model.newFile();
    
    // Current pane should be new doc, pos 0
    DefinitionsPane curPane;
    OpenDefinitionsDocument curDoc;
    curPane = _frame.getCurrentDefPane();
    curDoc = curPane.getOpenDefDocument();//.getDocument();
    assertEquals("New curr DefPane's document", newDoc, curDoc);
    assertEquals("Location in new document", 0, newDoc.getCurrentLocation());
    
    // Switch back to old document
    model.setActiveNextDocument(); 
    Utilities.clearEventQueue();
    assertEquals("Next active doc", oldDoc, model.getActiveDocument());

   
    // Current pane should be old doc, pos 3
    curPane = _frame.getCurrentDefPane();
    curDoc = curPane.getOpenDefDocument();//.getDocument();
    assertEquals("Next active pane", oldPane, curPane);
    assertEquals("Current document is old document", oldDoc, curDoc);
    assertEquals("Location of caret in old document", 3, curPane.getCaretPosition());
    _log.log("testDocLocationAfterSwitch completed");
  }
  
  
  private String _data;
  
  /** Tests that the clipboard is unmodified after a "clear line" action. */
  public void testClearLine() throws BadLocationException, UnsupportedFlavorException, IOException {
    // First, copy some data out of the main document.
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    final String clipString = "***Clipboard***";
//    _frame.setVisible(true);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
        try { 
          doc.insertString(0, "abcdefg", null);
          pane.setCaretPosition(5);
          
//        ActionMap actionMap = _pane.getActionMap();
//        String selString = DefaultEditorKit.selectionEndLineAction;
//        actionMap.get(selString).actionPerformed(new ActionEvent(this, 0, "SelectionEndLine"));
//        _frame.cutAction.actionPerformed(new ActionEvent(this, 0, "Cut"));
          
          _frame.validate();
//          _frame.paint(_frame.getGraphics());
//          ActionMap actionMap = _pane.getActionMap();
//          String selString = DefaultEditorKit.selectionEndLineAction;
//          actionMap.get(selString).actionPerformed(new ActionEvent(this, 0, "SelectionEndLine"));
//          pane.setSelectionStart(2);
//          pane.setSelectionEnd(7);
          
          // Get a copy of the current clipboard.
          Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
          // Insert valid string in clipboars.
          
          clip.setContents(new StringSelection(clipString), _frame);
          Transferable contents = clip.getContents(_frame);
          
          // Trigger the Clear Line action from a new position.
          pane.setCaretPosition(2);
          _frame.validate();
//          _frame.paint(_frame.getGraphics());
          
          _frame._clearLineAction.actionPerformed(new ActionEvent(pane, 0, "Clear Line"));
//          _frame.paint(_frame.getGraphics());
          _frame.validate();
          
          // Verify that the clipboard contents are still the same.
          contents = clip.getContents(null);
          _data = (String) contents.getTransferData(DataFlavor.stringFlavor);
        }
        catch(Throwable t) { listenerFail(t.getMessage()); }
      }
    });
    Utilities.clearEventQueue();
    
    assertEquals("Clipboard contents should be unchanged after Clear Line.", clipString, _data);
    
    // Verify that the document text is what we expect.
    assertEquals("Current line of text should be truncated by Clear Line.", "ab", doc.getText());
    _log.log("testClearLine completed");
  }
  
  /** Tests that the clipboard is modified after a "cut line" action.
    * NOTE: Commented out for commit because of failures, despite proper behavior in GUI.
    *       This may not work unless ActionEvents are dropped in the event queue
    */
  public void testCutLine() throws BadLocationException {
    // First, copy some data out of the main document.
    
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
//    _frame.setVisible(true);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
        try { 
          doc.insertString(0, "abcdefg", null);
          pane.setCaretPosition(5);
          _frame.validate();
//          _frame.paint(_frame.getGraphics());
//          ActionMap actionMap = _pane.getActionMap();
//          String selString = DefaultEditorKit.selectionEndLineAction;
//          actionMap.get(selString).actionPerformed(new ActionEvent(pane, 0, "SelectionEndLine"));
          pane.setSelectionStart(2);
          pane.setSelectionEnd(7);
          _frame.validate();
          pane.cut();
//          _frame.cutAction.actionPerformed(new ActionEvent(pane, 0, "Cut"));
          
          // Get a copy of the current clipboard.
          
          // Trigger the Cut Line action from a new position.
//          _pane.setCaretPosition(2);
          _frame.validate();
//          _frame.paint(_frame.getGraphics());
          
//          _frame._cutLineAction.actionPerformed(new ActionEvent(this, 0, "Cut Line"));
//          _frame.dispatchEvent(new ActionEvent(this, 0, "Cut Line"));
          
          // Verify that the clipboard contents are what we expect.
          Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable contents = clip.getContents(null);
          _data = (String) contents.getTransferData(DataFlavor.stringFlavor);
        }
        catch(Throwable t) { listenerFail(t.getMessage()); }
      }
    });
    Utilities.clearEventQueue();
    assertEquals("Clipboard contents should be changed after Cut Line.", "cdefg", _data);
    
    // Verify that the document text is what we expect.
    assertEquals("Current line of text should be truncated by Cut Line.", "ab", doc.getText());
    _log.log("testCutLine completed");
  }
  
  
  /** Make sure that the InteractionsPane is displaying the correct InteractionsDocument.  (SourceForge bug #681547)
    * Also make sure this document cannot be edited before the prompt.
    */
  public void testCorrectInteractionsDocument() throws EditDocumentException {
    InteractionsPane pane = _frame.getInteractionsPane();
    final SingleDisplayModel model = _frame.getModel();
    InteractionsDJDocument doc = model.getSwingInteractionsDocument();
    
    // Make the test silent
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        model.getInteractionsModel().getDocument().setBeep(new TestBeep()); 
      }
    });
    Utilities.clearEventQueue();
    
    // Test for strict == equality
    assertTrue("UI's int. doc. should equals Model's int. doc.", pane.getDocument() == doc);
    
    int origLength = doc.getLength();
    doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("Document should not have changed.", origLength, doc.getLength());
    _log.log("testCorrectInteractionsDocument completed");
  }
  
  /** Tests that undoing/redoing a multi-line indent will restore the caret position. */
  public void testMultilineIndentAfterScroll() throws BadLocationException, InterruptedException {
    final String text =
      "public class stuff {\n" +
      "private int _int;\n" +
      "private Bar _bar;\n" +
      "public void foo() {\n" +
      "_bar.baz(_int);\n" +
      "}\n" +
      "}\n";
    
    final String indented =
      "public class stuff {\n" +
      "  private int _int;\n" +
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";
    
    final int newPos = 20;
    
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    
    setConfigSetting(OptionConstants.INDENT_LEVEL, Integer.valueOf(2));

    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append(text, null);
        pane.setCaretPosition(0);
        pane.endCompoundEdit(); 
      } 
    });
    
    Utilities.clearEventQueue();
    assertEquals("Should have inserted correctly.", text, doc.getText());
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { doc.indentLines(0, doc.getLength()); }
    });
    
    assertEquals("Should have indented.", indented, doc.getText());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        doc.getUndoManager().undo();
//        System.err.println("New position is: " + pane.getCaretPosition());
      }
    }); 

    assertEquals("Should have undone.", text, doc.getText());
    
//    int rePos = doc.getCurrentLocation();
//    System.err.println("Restored position is: " + rePos);
    // cursor will be located at beginning of first line that is changed
//    assertEquals("Undo should have restored cursor position.", oldPos, rePos);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        pane.setCaretPosition(newPos);
        doc.getUndoManager().redo();
      }
    });
    Utilities.clearEventQueue();
    
    assertEquals("redo",indented, doc.getText());
//    assertEquals("redo restores caret position", oldPos, pane.getCaretPosition());
    _log.log("testMultilineIndentAfterScroll completed");
  }
  
  JScrollPane _pane1, _pane2;
  DefinitionsPane _defPane1, _defPane2;
  
  /** Ensure that a document's editable status is set appropriately throughout the compile process.  Since the behavior
    * is interesting only when the model changes its active document, that's what this test looks most like.
    */
  public void testGlassPaneEditableState() {
    SingleDisplayModel model = _frame.getModel();
    
    final OpenDefinitionsDocument doc1 = model.newFile();
    final OpenDefinitionsDocument doc2 = model.newFile();
    
    // doc2 is now active
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        
        _pane1 = _frame._createDefScrollPane(doc1);
        _pane2 = _frame._createDefScrollPane(doc2);
        
        _defPane1 = (DefinitionsPane) _pane1.getViewport().getView();
        _defPane2 = (DefinitionsPane) _pane2.getViewport().getView();
        
        _frame._switchDefScrollPane();
      }
    });
    
    Utilities.clearEventQueue(); // Execute all pending asynchronous tasks;
    
    assertTrue("Start: defPane1", _defPane1.isEditable());
    assertTrue("Start: defPane2", _defPane2.isEditable());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame.hourglassOn(); } });
    Utilities.clearEventQueue();
    
    assertTrue("Glass on: defPane1", _defPane1.isEditable());
    assertTrue("Glass on: defPane2",(! _defPane2.isEditable()));
    model.setActiveDocument(doc1);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame._switchDefScrollPane(); } });
    Utilities.clearEventQueue();
    
    assertTrue("Doc Switch: defPane1",(! _defPane1.isEditable()));
    assertTrue("Doc Switch: defPane2", _defPane2.isEditable());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame.hourglassOff(); } });
    Utilities.clearEventQueue();
    
    assertTrue("End: defPane1", _defPane1.isEditable());
    assertTrue("End: defPane2", _defPane2.isEditable());
    _log.log("testGlassPaneEditableState completed");
  }
  
  private KeyEvent makeFindKeyEvent(Component c, long when) {
    return new KeyEvent(c, KeyEvent.KEY_PRESSED, when, KeyEvent.CTRL_MASK, KeyEvent.VK_F, 'F');
  }
  
  /** Ensure that all key events are disabled when the glass pane is up. */
  public void testGlassPaneHidesKeyEvents() {
    SingleDisplayModel model = _frame.getModel();
    
    final OpenDefinitionsDocument doc1 = model.newFile();
    final OpenDefinitionsDocument doc2 = model.newFile();
    
    // doc2 is now active
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane1 = _frame._createDefScrollPane(doc1);
        _pane2 = _frame._createDefScrollPane(doc2);
        _defPane1 = (DefinitionsPane) _pane1.getViewport().getView();
        _defPane2 = (DefinitionsPane) _pane2.getViewport().getView();
        _frame.validate();
        _frame.hourglassOn();
        _defPane1.processKeyEvent(makeFindKeyEvent(_defPane1, 70));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    
    assertTrue("the find replace dialog should not come up", ! _frame.getFindReplaceDialog().isDisplayed());
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _frame.getInteractionsPane().processKeyEvent(makeFindKeyEvent(_frame.getInteractionsPane(), 0));
        _frame.validate();
      }
    });
    Utilities.clearEventQueue();
    
    assertTrue("the find replace dialog should not come up", ! _frame.getFindReplaceDialog().isDisplayed());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame.hourglassOff(); } });
    _log.log("testGlassPaneHidesKeyEvents completed");
  }
  
  
  /** Tests that the save button does not set itself as enabled immediately after opening a file. */
  public void testSaveButtonEnabled() throws IOException {
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.scala");
    String forceOpenClass1_string =
      "class ForceOpenClass1 {\n" +
      "  var class2: ForceOpenClass2 = null \n" +
      "  var class3: ForceOpenClass3 = null \n" +
      "  def ForceOpenClass1() {\n" +
      "    class2 = new ForceOpenClass2()\n" +
      "    class3 = new ForceOpenClass3()\n" +
      "  }\n" +
      "}";
    
    IOUtil.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
    forceOpenClass1_file.deleteOnExit();
    
    //_frame.setVisible(true);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _frame.pack();
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() {
            File[] return_me = new File[1];
            return_me[0] = new File(_tempDir, "ForceOpenClass1.scala");
            return return_me;
          }
        });
      }
    }); 
    Utilities.clearEventQueue();
    
    assertTrue("the save button should not be enabled after opening a document", !_frame.isSaveEnabled());
    _log.log("testSaveButtonEnabled completed");
  }
  
  /** A Test to guarantee that the Dancing UI bug will not rear its ugly head again.
    * Basically, add a component listener to the leftComponent of _docSplitPane and
    * make certain its size does not change while compiling a class which depends on
    * another class.
    */
  public void testDancingUIFileOpened() throws IOException {
    //System.out.println("DEBUG: Entering messed up test");
    /** Maybe this sequence of calls should be incorporated into one function createTestDir(), which would get 
      * the username and create the temporary directory. Only sticky part is deciding where to put it, in FileOps 
      * maybe?
      */
    
    _log.log("Starting testingDancingUIFileOpened");
    
    final GlobalModel _model = _frame.getModel();
    
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    
    File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.scala");
    String forceOpenClass1_string =
      "class ForceOpenClass1 {\n" +
      "  var class2: ForceOpenClass2 = null\n" +
      "  var class3: ForceOpenClass3 = null\n" +
      "  def ForceOpenClass1() {\n" +
      "    class2 = new ForceOpenClass2()\n" +
      "    class3 = new ForceOpenClass3()\n" +
      "  }\n" +
      "}";
    
    File forceOpenClass2_file = new File(_tempDir, "ForceOpenClass2.scala");
    String forceOpenClass2_string =
      "class ForceOpenClass2 {\n" +
      "  var x:inx = 4\n" +
      "}";
    
    File forceOpenClass3_file = new File(_tempDir, "ForceOpenClass3.java");
    String forceOpenClass3_string =
      "class ForceOpenClass3 {\n" +
      "   var s:String = \"asf\"\n" +
      "}";
    
    IOUtil.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
    IOUtil.writeStringToFile(forceOpenClass2_file, forceOpenClass2_string);
    IOUtil.writeStringToFile(forceOpenClass3_file, forceOpenClass3_string);
    forceOpenClass1_file.deleteOnExit();
    forceOpenClass2_file.deleteOnExit();
    forceOpenClass3_file.deleteOnExit();
    
    _log.log("DancingUIFileOpened Set Up");
    
    //_frame.setVisible(true);
    
    // set up listeners and signal flags
    
    final ComponentAdapter listener = new ComponentAdapter() {
      public void componentResized(ComponentEvent event) {
        _testFailed = true;
        fail("testDancingUI: Open Documents List danced!");
      }
    };
    final SingleDisplayModelFileOpenedListener openListener = new SingleDisplayModelFileOpenedListener();
    final SingleDisplayModelCompileListener compileListener = new SingleDisplayModelCompileListener();
    
    _openDone = false;
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
//       _frame.setVisible(true);
        _frame.pack();
        _frame.addComponentListenerToOpenDocumentsList(listener);
      }
    });
    Utilities.clearEventQueue();
    
    _model.addListener(openListener);
    
    _log.log("opening file");
    
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() {
            File[] return_me = new File[1];
            return_me[0] = new File(_tempDir, "ForceOpenClass1.scala");
            return return_me;
          }
        });
      }
    });
    Utilities.clearEventQueue();
    
    /* wait until file has been open and active document changed. */
    synchronized(_openLock) {
      try { while (! _openDone) _openLock.wait(); }
      catch(InterruptedException e) { fail(e.toString()); }
    }
    
    _model.removeListener(openListener);
    
    _log.log("File opened");
    
    _compileDone = false;
    _model.addListener(compileListener);
    
    // save and compile the new file asynchronously
    
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
        _log.log("saving all files");
        _frame._saveAll();
        _log.log("invoking compileAll action");
        _frame.getCompileAllButton().doClick();
      }
    });
    Utilities.clearEventQueue();
    
    synchronized(_compileLock) {
      try { while (! _compileDone) _compileLock.wait(); }
      catch(InterruptedException e) { fail(e.toString()); }
    }
    _log.log("File saved and compiled");
    
    if (! IOUtil.deleteRecursively(_tempDir))
      System.out.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() + "\nDo it by hand.\n");
    
    _log.log("testDancingUIFileOpened completed");
  }
  
  /** A Test to guarantee that the Dancing UI bug will not rear its ugly head again. Basically, add a component listener
    * to the leftComponent of _docSplitPane and make certain its size does not change while closing an 
    * OpenDefinitionsDocument outside the event thread.
    */
  public void testDancingUIFileClosed() throws IOException {
    /** Maybe this sequence of calls should be incorporated into one function createTestDir(), which would get the 
      * username and create the temporary directory. Only sticky part is deciding where to put it, in FileOps maybe?
      */
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.scala");
    String forceOpenClass1_string =
      "class ForceOpenClass1 {\n" +
      "  var class2: ForceOpenClass2 = null\n" +
      "  var class3: ForceOpenClass3 = null\n" +
      "  def ForceOpenClass1() {\n" +
      "    var class2 = new ForceOpenClass2()\n" +
      "    var class3 = new ForceOpenClass3()\n" +
      "  }\n" +
      "}";
    
    IOUtil.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
    forceOpenClass1_file.deleteOnExit();
    
    final ComponentAdapter listener = new ComponentAdapter() {
      public void componentResized(ComponentEvent event) {
        _testFailed = true;
        fail("testDancingUI: Open Documents List danced!");
      }
    };
    final SingleDisplayModelFileClosedListener closeListener = new SingleDisplayModelFileClosedListener();
    
    _closeDone = false;
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
//       _frame.setVisible(true);
        _frame.pack();
        _frame.addComponentListenerToOpenDocumentsList(listener);
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() {
            File[] return_me = new File[1];
            return_me[0] = new File(_tempDir, "ForceOpenClass1.scala");
            return return_me;
          }
        });
        _frame.getModel().addListener(closeListener);
      }
    });
    Utilities.clearEventQueue();
    
    /* Asynchronously close the file */
    Utilities.invokeLater(new Runnable() { 
      public void run() { _frame.getCloseButton().doClick(); }
    });
    
    _log.log("Waiting for file closing");
    
    synchronized(_closeLock) {
      try { while (! _closeDone) _closeLock.wait(); }
      catch(InterruptedException e) { fail(e.toString()); }
    }
    
    if (! IOUtil.deleteRecursively(_tempDir)) {
      System.out.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() + "\nDo it by hand.\n");
    }
    _log.log("testDancingUIFileClosed completed");
  }
  
  /** A CompileListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelCompileListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {
    
    @Override public void compileStarted() { }
    
    /** Just notify when the compile has ended */
    @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
      synchronized(_compileLock) { 
        _compileDone = true;
        _compileLock.notify();
      }
    }
    
    @Override public void fileOpened(OpenDefinitionsDocument doc) { }
    @Override public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  }
  
  /** A FileClosedListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelFileOpenedListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {
    
    @Override public void fileClosed(OpenDefinitionsDocument doc) { }
    
    @Override public void fileOpened(OpenDefinitionsDocument doc) { }
    
    @Override public void newFileCreated(OpenDefinitionsDocument doc) { }
    @Override public void activeDocumentChanged(OpenDefinitionsDocument doc) { 
      synchronized(_openLock) {
        _openDone = true;
        _openLock.notify();
      }
    }
  }
  
  /** A FileClosedListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelFileClosedListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {
    
    @Override public void fileClosed(OpenDefinitionsDocument doc) {
      synchronized(_closeLock) {
        _closeDone = true;
        _closeLock.notify();
      }
    }
    
    @Override public void fileOpened(OpenDefinitionsDocument doc) { }
    @Override public void newFileCreated(OpenDefinitionsDocument doc) { }
    @Override public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  }
  
  /** Create a new temporary file in _tempDir. */
  protected File tempFile(String fileName) throws IOException {
    File f =  File.createTempFile(fileName, ".scala", _tempDir).getCanonicalFile();
    f.deleteOnExit();
    return f;
  }
  
  /** Tests that "go to file under cursor" works if unique. */
  public void testGotoFileUnderCursor() throws IOException {
//    Utilities.show("Running testGotoFileUnderCursor");
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    
    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor1.scala");
    final String goto1_string = "GotoFileUnderCursorTest";
    IOUtil.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();
    
    final File goto2_file = new File(_tempDir, "GotoFileUnderCursorTest.scala");
    String goto2_string = "GotoFileUnderCursor1";
    IOUtil.writeStringToFile(goto2_file, goto2_string);
    goto2_file.deleteOnExit();
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _frame.pack();
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() { return new File[] { goto1_file, goto2_file }; }
        });
      }
    });
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _frame.initGotoFileDialog();
        _frame._gotoFileDialog.addWindowListener(new WindowListener() {
          public void windowActivated(WindowEvent e) { throw new RuntimeException("Should not activate _gotoFileDialog"); }
          public void windowClosed(WindowEvent e) { throw new RuntimeException("Should not close _gotoFileDialog"); }
          public void windowClosing(WindowEvent e) { throw new RuntimeException("Should not be closing _gotoFileDialog"); }
          public void windowDeactivated(WindowEvent e) { throw new RuntimeException("Should not deactivate _gotoFileDialog"); }
          public void windowDeiconified(WindowEvent e) { throw new RuntimeException("Should not deiconify _gotoFileDialog"); }
          public void windowIconified(WindowEvent e) { throw new RuntimeException("Should not iconify _gotoFileDialog"); }
          public void windowOpened(WindowEvent e) { throw new RuntimeException("Should not open _gotoFileDialog"); }
        });
      }});
    
    Utilities.clearEventQueue();
    Utilities.invokeAndWait(new Runnable() { public void run() {
      SingleDisplayModel model = _frame.getModel();
      try {
        OpenDefinitionsDocument goto1_doc = model.getDocumentForFile(goto1_file);
        OpenDefinitionsDocument goto2_doc = model.getDocumentForFile(goto2_file);
        model.setActiveDocument(model.getDocumentForFile(goto1_file));
        assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
        
        _frame._gotoFileUnderCursor();
        
        assertEquals("Incorrect active document; did not go to?", goto2_doc, model.getActiveDocument());
        
        _frame._gotoFileUnderCursor();
        
        assertEquals("Incorrect active document; did not go to?", goto1_doc, model.getActiveDocument());
      }
      catch(IOException ioe) { throw new UnexpectedException(ioe); }
    } });

    _log.log("gotoFileUnderCursor completed");
  }
  
  /** Tests that "go to file under cursor" works if unique after appending ".scala" */
  public void testGotoFileUnderCursorAppendJava() throws IOException {
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrScala-test-" + user, "");
    
    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor2Test.scala");
    final String goto1_string = "GotoFileUnderCursor2";
    IOUtil.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();
    
    final File goto2_file = new File(_tempDir, "GotoFileUnderCursor2.scala");
    String goto2_string = "GotoFileUnderCursor2Test";
    IOUtil.writeStringToFile(goto2_file, goto2_string);
    goto2_file.deleteOnExit();
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _frame.pack();
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() {
            return new File[] { goto1_file, goto2_file };
          }
        });
      }
    });
    
    Utilities.clearEventQueue();
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _frame.initGotoFileDialog();
        _frame._gotoFileDialog.addWindowListener(new WindowListener() {
          public void windowActivated(WindowEvent e) { throw new RuntimeException("Should not activate _gotoFileDialog"); }
          public void windowClosed(WindowEvent e) { throw new RuntimeException("Should not close _gotoFileDialog"); }
          public void windowClosing(WindowEvent e) { throw new RuntimeException("Should not be closing _gotoFileDialog"); }
          public void windowDeactivated(WindowEvent e) { throw new RuntimeException("Should not deactivate _gotoFileDialog"); }
          public void windowDeiconified(WindowEvent e) { throw new RuntimeException("Should not deiconify _gotoFileDialog"); }
          public void windowIconified(WindowEvent e) { throw new RuntimeException("Should not iconify _gotoFileDialog"); }
          public void windowOpened(WindowEvent e) { throw new RuntimeException("Should not open _gotoFileDialog"); }
        });
      }});
    
    Utilities.clearEventQueue();
    
    Utilities.invokeAndWait(new Runnable() { public void run() {
      SingleDisplayModel model = _frame.getModel();
      try {
        OpenDefinitionsDocument goto1_doc = model.getDocumentForFile(goto1_file);
        OpenDefinitionsDocument goto2_doc = model.getDocumentForFile(goto2_file);
        model.setActiveDocument(model.getDocumentForFile(goto1_file));
        assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
        
        _frame._gotoFileUnderCursor();
        
        assertEquals("Incorrect active document; did not go to?", goto2_doc, model.getActiveDocument());
        
        _frame._gotoFileUnderCursor();
        
        assertEquals("Incorrect active document; did not go to?", goto1_doc, model.getActiveDocument());
      }
      catch(IOException ioe) { throw new UnexpectedException(ioe); }
    } });
    
    _log.log("gotoFileUnderCursorAppendJava completed");
  }
  
  /** Tests that "go to file under cursor" displays the dialog if choice is not unique */
  public void testGotoFileUnderCursorShowDialog() throws IOException {
//    Utilities.show("Running testGotoFileUnderCursorShowDialog()");
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    
    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor3.scala");
    final String goto1_string = "GotoFileUnderCursor";
    IOUtil.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();
    
    final File goto2_file = new File(_tempDir, "GotoFileUnderCursor4.scala");
    String goto2_string = "GotoFileUnderCursor3";
    IOUtil.writeStringToFile(goto2_file, goto2_string);
    goto2_file.deleteOnExit();
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _frame.pack();
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() { return new File[] { goto1_file, goto2_file }; }
        });
      }
    });
    
    final int[] count = new int[2];
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        _frame.initGotoFileDialog();
        _frame._gotoFileDialog.addWindowListener(new WindowListener() {
          public void windowActivated(WindowEvent e) { ++count[0]; }
          public void windowClosed(WindowEvent e) { throw new RuntimeException("Should not close _gotoFileDialog"); }
          public void windowClosing(WindowEvent e) { throw new RuntimeException("Should not be closing _gotoFileDialog"); }
          public void windowDeactivated(WindowEvent e) { /* throw new RuntimeException("Should not deactivate _gotoFileDialog"); */ }
          public void windowDeiconified(WindowEvent e) { throw new RuntimeException("Should not deiconify _gotoFileDialog"); }
          public void windowIconified(WindowEvent e) { throw new RuntimeException("Should not iconify _gotoFileDialog"); }
          public void windowOpened(WindowEvent e) { ++count[1]; }
        });
      }
    });
    
    Utilities.clearEventQueue();
    
    Utilities.invokeAndWait(new Runnable() { public void run() {
      SingleDisplayModel model = _frame.getModel();
      try {
        model.setActiveDocument(model.getDocumentForFile(goto1_file));
        
        assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
      }
      catch(IOException ioe) { throw new UnexpectedException(ioe); }
    } });

    Utilities.invokeAndWait(new Runnable() { public void run() { _frame._gotoFileUnderCursor(); } });                    
    Utilities.clearEventQueue();  // wait for any asynchronous actions to complete
    
    
    /* The following test was commented out before test following it was.  It presumably fails even if
     * the "MainFrame.this.isVisible()" test mentioned below is removed from _gotoFileUnderCursor */
//    assertEquals("Did not activate _gotoFileDialog", 1, count[0]);
    /* The following test was commented out after suppressing this display when _frame is not visible.  If it is 
     * uncommented, then the "MainFrame.this.isVisible()" test in _gotoFileDialog must be removed. */
//    assertEquals("Did not open _gotoFileDialog", 1, count[1]);
    
    _log.log("gotoFileUnderCursorShowDialog completed");
  }
}
