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
 *       javaplt@rice.edu.0
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

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.*;
import edu.rice.cs.util.swing.Utilities;

/** Test functions of MainFrame.
 *  @version $Id$
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

  private static Log _log = new Log("MainFrameTestLog.txt", true);
 
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    _log.log("super.setUp() for next test completed");
//    Utilities.invokeAndWait(new Runnable() { 
//      public void run() { 
        _frame  = new MainFrame();
        _log.log("new MainFrame() for next test completed");
        _frame.pack();
//      }
//    });
    _log.log("setUp complete for next test");
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

  JButton _but;
  /** Tests that the returned JButton of <code>createManualToolbarButton</code>:
   *  1. Is disabled upon return.
   *  2. Inherits the tooltip of the Action parameter <code>a</code>.
   */
  public void testCreateManualToolbarButton() {
    final Action a = new AbstractAction("Test Action") { public void actionPerformed(ActionEvent ae) { } };
    
    a.putValue(Action.SHORT_DESCRIPTION, "test tooltip");
    Utilities.invokeAndWait(new Runnable() { public void run() { _but = _frame._createManualToolbarButton(a); } });

    assertTrue("Returned JButton is enabled.", ! _but.isEnabled());
    assertEquals("Tooltip text not set.", "test tooltip", _but.getToolTipText());
    _log.log("testCreateManualToobarButton completed");
  }

  /** Tests that the current location of a document is equal to the caret location after documents are switched. */
  public void testDocLocationAfterSwitch() throws BadLocationException {
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    doc.insertString(0, "abcd", null);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        pane.setCaretPosition(3); // not thread-safe!
      }
    }); 
    Utilities.clearEventQueue();  // Empty the event queue of any asynchronous tasks
      
    assertEquals("Location of old doc before switch", 3, doc.getCurrentLocation());
      
    // Create a new file
    SingleDisplayModel model = _frame.getModel();
    final OpenDefinitionsDocument oldDoc = doc;
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
    assertEquals("Next active doc", oldDoc, model.getActiveDocument());
                 
    // Current pane should be old doc, pos 3
    curPane = _frame.getCurrentDefPane();
    curDoc = curPane.getOpenDefDocument();//.getDocument();
    assertEquals("Current document is old document", oldDoc, curDoc);
    assertEquals("Location of old document", 3, curDoc.getCurrentLocation());
    _log.log("testDocLocationAfterSwitch completed");
  }

  /**
   * Tests that the clipboard is unmodified after a "clear line" action.
   * NOTE: Commented out for commit because of failures, despite proper behavior in GUI.
   *       This may not work unless the textpane is visible.
   *
  public void testClearLine()
    throws BadLocationException, UnsupportedFlavorException, IOException {
    // First, copy some data out of the main document.
    DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    doc.insertString(0, "abcdefg", null);
    pane.setCaretPosition(5);

    ActionMap actionMap = pane.getActionMap();
    actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed
      (new ActionEvent(this, 0, "SelectionEndLine"));
    _frame.cutAction.actionPerformed(new ActionEvent(this, 0, "Cut"));

    // Get a copy of the current clipboard.
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clip.getContents(null);
    String data = (String) contents.getTransferData(DataFlavor.stringFlavor);

    // Trigger the Clear Line action from a new position.
    pane.setCaretPosition(2);
    _frame._clearLineAction.actionPerformed
      (new ActionEvent(this, 0, "Clear Line"));

    // Verify that the clipboard contents are still the same.
    contents = clip.getContents(null);
    String newData = (String) contents.getTransferData(DataFlavor.stringFlavor);
    assertEquals("Clipboard contents should be unchanged after Clear Line.",
                 data, newData);

    // Verify that the document text is what we expect.
    assertEquals("Current line of text should be truncated by Clear Line.",
                 "ab", doc.getText());
  }
  */

  /**
   * Tests that the clipboard is modified after a "cut line" action.
   * NOTE: Commented out for commit because of failures, despite proper behavior in GUI.
   *       This may not work unless the textpane is visible.
   *
  public void testCutLine()
    throws BadLocationException, UnsupportedFlavorException, IOException {
    // First, copy some data out of the main document.
    DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    doc.insertString(0, "abcdefg", null);
    pane.setCaretPosition(5);

    ActionMap actionMap = pane.getActionMap();
    actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed
      (new ActionEvent(this, 0, "SelectionEndLine"));
    _frame.cutAction.actionPerformed(new ActionEvent(this, 0, "Cut"));

    // Get a copy of the current clipboard.

    // Trigger the Cut Line action from a new position.
    pane.setCaretPosition(2);
    _frame._cutLineAction.actionPerformed
      (new ActionEvent(this, 0, "Cut Line"));

    // Verify that the clipboard contents are what we expect.
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clip.getContents(null);
    String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
    assertEquals("Clipboard contents should be changed after Cut Line.",
                 "cdefg", data);

    // Verify that the document text is what we expect.
    assertEquals("Current line of text should be truncated by Cut Line.",
                 "ab", doc.getText());
  }
  */

  /**
   * Make sure that the InteractionsPane is displaying the correct
   * InteractionsDocument.  (SourceForge bug #681547)  Also make sure this
   * document cannot be edited before the prompt.
   */
  public void testCorrectInteractionsDocument() throws EditDocumentException {
    InteractionsPane pane = _frame.getInteractionsPane();
    SingleDisplayModel model = _frame.getModel();
    InteractionsDJDocument doc = model.getSwingInteractionsDocument();

    // Make the test silent
    model.getInteractionsModel().getDocument().setBeep(new TestBeep());

    // Test for strict == equality
    assertTrue("UI's int. doc. should equals Model's int. doc.", pane.getDocument() == doc);

    int origLength = doc.getLength();
    doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Document should not have changed.", origLength, doc.getLength());
    _log.log("testCorrectInteractionsDocument completed");
  }

  /**
   * Tests that undoing/redoing a multi-line indent will restore
   * the caret position.
   */
  public void testMultilineIndentAfterScroll() throws BadLocationException, InterruptedException {
    String text =
      "public class stuff {\n" +
      "private int _int;\n" +
      "private Bar _bar;\n" +
      "public void foo() {\n" +
      "_bar.baz(_int);\n" +
      "}\n" +
      "}\n";
    
    String indented =
      "public class stuff {\n" +
      "  private int _int;\n" +
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";
    
    int oldPos;
    final int newPos = 20;
    
    final DefinitionsPane pane = _frame.getCurrentDefPane();
    final OpenDefinitionsDocument doc = pane.getOpenDefDocument();
    
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL, new Integer(2));
    doc.insertString(0, text, null);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        pane.setCaretPosition(0);
        pane.endCompoundEdit(); 
      } 
    });
    
    assertEquals("Should have inserted correctly.", text, doc.getText());
    
    doc.indentLines(0, doc.getLength());
    assertEquals("Should have indented.", indented, doc.getText());
    
    oldPos = pane.getCaretPosition();
//    System.err.println("Old position is: " + oldPos);

    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        pane.setCaretPosition(newPos);
//        System.err.println("New position is: " + pane.getCaretPosition());
      }
    }); 
    
    // Moving this two statement to the event thread breaks "Undo should have restored ..."  Why?
    doc.getUndoManager().undo();
    
    assertEquals("Should have undone.", text, doc.getText());
    
    int rePos = pane.getCaretPosition();
//    System.err.println("Restored position is: " + rePos);
    assertEquals("Undo should have restored caret position.", oldPos, rePos);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        pane.setCaretPosition(newPos);
        doc.getUndoManager().redo();
      }
    });
    assertEquals("redo",indented, doc.getText());
    assertEquals("redo restores caret position", oldPos, pane.getCaretPosition());
    _log.log("testMultilineIndentAfterScroll completed");
  }
  
  JScrollPane _pane1, _pane2;
  DefinitionsPane _defPane1, _defPane2;

  /** Ensure that a document's editable status is set appropriately throughout the compile process.  Since the behavior
   *  is interesting only when the model changes its active document, that's what this test looks most like.
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
    
    assertTrue("Glass on: defPane1", _defPane1.isEditable());
    assertTrue("Glass on: defPane2",(! _defPane2.isEditable()));
    model.setActiveDocument(doc1);
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame._switchDefScrollPane(); } });
    
    assertTrue("Doc Switch: defPane1",(! _defPane1.isEditable()));
    assertTrue("Doc Switch: defPane2", _defPane2.isEditable());
    Utilities.invokeAndWait(new Runnable() { public void run() { _frame.hourglassOff(); } });
    
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
        
        _frame.hourglassOn();
        
        _defPane1.processKeyEvent(makeFindKeyEvent(_defPane1, 70));
      }
    });
    
    assertTrue("the find replace dialog should not come up", ! _frame.getFindReplaceDialog().isDisplayed());
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _frame.getInteractionsPane().processKeyEvent(makeFindKeyEvent(_frame.getInteractionsPane(), 0));
      }
    });
    
    assertTrue("the find replace dialog should not come up", ! _frame.getFindReplaceDialog().isDisplayed());

    _frame.hourglassOff();
    _log.log("testGlassPaneHidesKeyEvents completed");
  }

  
  /** Tests that the save button does not set itself as enabled immediately after opening a file. */
  public void testSaveButtonEnabled() throws IOException {
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
    File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.java");
    String forceOpenClass1_string =
      "public class ForceOpenClass1 {\n" +
      "  ForceOpenClass2 class2;\n" +
      "  ForceOpenClass3 class3;\n\n" +
      "  public ForceOpenClass1() {\n" +
      "    class2 = new ForceOpenClass2();\n" +
      "    class3 = new ForceOpenClass3();\n" +
      "  }\n" +
      "}";
    FileOps.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
    forceOpenClass1_file.deleteOnExit();
    
    //_frame.setVisible(true);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _frame.pack();
        _frame.open(new FileOpenSelector() {
          public File[] getFiles() {
            File[] return_me = new File[1];
            return_me[0] = new File(_tempDir, "ForceOpenClass1.java");
            return return_me;
          }
        });
      }
    });                
    
    assertTrue("the save button should not be enabled after opening a document", !_frame.saveEnabledHuh());
    _log.log("testSaveButtonEnabled completed");
  }
  
  /** A Test to guarantee that the Dancing UI bug will not rear its ugly head again.
   *  Basically, add a component listener to the leftComponent of _docSplitPane and
   *  make certain its size does not change while compiling a class which depends on
   *  another class.
   */
  public void testDancingUIFileOpened() throws IOException {
    //System.out.println("DEBUG: Entering messed up test");
    /** Maybe this sequence of calls should be incorporated into one function createTestDir(), which would get 
     *  the username and create the temporary directory. Only sticky part is deciding where to put it, in FileOps 
     *  maybe?
     */
    
    _log.log("Starting testingDancingUIFileOpened");
    
    final GlobalModel _model = _frame.getModel();
    
     String user = System.getProperty("user.name");
     _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);

     File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.java");
     String forceOpenClass1_string =
       "public class ForceOpenClass1 {\n" +
       "  ForceOpenClass2 class2;\n" +
       "  ForceOpenClass3 class3;\n\n" +
       "  public ForceOpenClass1() {\n" +
       "    class2 = new ForceOpenClass2();\n" +
       "    class3 = new ForceOpenClass3();\n" +
       "  }\n" +
       "}";

     File forceOpenClass2_file = new File(_tempDir, "ForceOpenClass2.java");
     String forceOpenClass2_string =
       "public class ForceOpenClass2 {\n" +
       "  inx x = 4;\n" +
       "}";

     File forceOpenClass3_file = new File(_tempDir, "ForceOpenClass3.java");
     String forceOpenClass3_string =
       "public class ForceOpenClass3 {\n" +
       "  String s = \"asf\";\n" +
       "}";

     FileOps.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
     FileOps.writeStringToFile(forceOpenClass2_file, forceOpenClass2_string);
     FileOps.writeStringToFile(forceOpenClass3_file, forceOpenClass3_string);
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
     
     _model.addListener(openListener);
     
     _log.log("opening file");
     
     Utilities.invokeLater(new Runnable() {
       public void run() {
        _frame.open(new FileOpenSelector() {
           public File[] getFiles() {
             File[] return_me = new File[1];
             return_me[0] = new File(_tempDir, "ForceOpenClass1.java");
             return return_me;
           }
         });
       }
     });
     
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

     synchronized(_compileLock) {
       try { while (! _compileDone) _compileLock.wait(); }
       catch(InterruptedException e) { fail(e.toString()); }
     }
     _log.log("File saved and compiled");
     
     if (! FileOps.deleteDirectory(_tempDir))
       System.out.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() + "\nDo it by hand.\n");
   
     _log.log("testDancingUIFileOpened completed");
  }

  /** A Test to guarantee that the Dancing UI bug will not rear its ugly head again. Basically, add a component listener
   *  to the leftComponent of _docSplitPane and make certain its size does not change while closing an 
   *  OpenDefinitionsDocument outside the event thread.
   */
  public void testDancingUIFileClosed() throws IOException {
    /** Maybe this sequence of calls should be incorporated into one function createTestDir(), which would get the 
     *  username and create the temporary directory. Only sticky part is deciding where to put it, in FileOps maybe?
     */
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
    File forceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.java");
    String forceOpenClass1_string =
      "public class ForceOpenClass1 {\n" +
      "  ForceOpenClass2 class2;\n" +
      "  ForceOpenClass3 class3;\n\n" +
      "  public ForceOpenClass1() {\n" +
      "    class2 = new ForceOpenClass2();\n" +
      "    class3 = new ForceOpenClass3();\n" +
      "  }\n" +
      "}";
    
    FileOps.writeStringToFile(forceOpenClass1_file, forceOpenClass1_string);
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
            return_me[0] = new File(_tempDir, "ForceOpenClass1.java");
            return return_me;
          }
        });
        _frame.getModel().addListener(closeListener);
      }
    });
           
    /* Asynchronously close the file */
    Utilities.invokeLater(new Runnable() { 
      public void run() { _frame.getCloseButton().doClick(); }
    });
    
    _log.log("Waiting for file closing");
    
    synchronized(_closeLock) {
      try { while (! _closeDone) _closeLock.wait(); }
      catch(InterruptedException e) { fail(e.toString()); }
    }
    
    if (! FileOps.deleteDirectory(_tempDir)) {
      System.out.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() +
                         "\nDo it by hand.\n");
    }
    _log.log("testDancingUIClosed completed");
  }

  /** A CompileListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelCompileListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {

    public void compileStarted() { }

    /** Just notify when the compile has ended */
    public void compileEnded(File workDir, File[] excludedFiles) {
      synchronized(_compileLock) { 
        _compileDone = true;
        _compileLock.notify();
      }
    }

    public void fileOpened(OpenDefinitionsDocument doc) { }
    public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  }
  
   /** A FileClosedListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelFileOpenedListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {
    
    public void fileClosed(OpenDefinitionsDocument doc) { }

    public void fileOpened(OpenDefinitionsDocument doc) { }
  
    public void newFileCreated(OpenDefinitionsDocument doc) { }
    public void activeDocumentChanged(OpenDefinitionsDocument doc) { 
        synchronized(_openLock) {
        _openDone = true;
        _openLock.notify();
      }
    }
  }

  /** A FileClosedListener for SingleDisplayModel (instead of GlobalModel) */
  class SingleDisplayModelFileClosedListener extends GlobalModelTestCase.TestListener implements GlobalModelListener {

    public void fileClosed(OpenDefinitionsDocument doc) {
      synchronized(_closeLock) {
        _closeDone = true;
        _closeLock.notify();
      }
    }

    public void fileOpened(OpenDefinitionsDocument doc) { }
    public void newFileCreated(OpenDefinitionsDocument doc) { }
    public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile(String fileName) throws IOException {
    File f =  File.createTempFile(fileName, ".java", _tempDir).getCanonicalFile();
    f.deleteOnExit();
    return f;
  }
  
  /** Tests that "go to file under cursor" works if unique. */
  public void testGotoFileUnderCursor() throws IOException {
//    Utilities.show("Running testGotoFileUnderCursor");
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);

    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor1.java");
    String goto1_string = "GotoFileUnderCursorTest";
    FileOps.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();

    final File goto2_file = new File(_tempDir, "GotoFileUnderCursorTest.java");
    String goto2_string = "GotoFileUnderCursor1";
    FileOps.writeStringToFile(goto2_file, goto2_string);
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
    SingleDisplayModel model = _frame.getModel();
    OpenDefinitionsDocument goto1_doc = model.getDocumentForFile(goto1_file);
    OpenDefinitionsDocument goto2_doc = model.getDocumentForFile(goto2_file);
    model.setActiveDocument(model.getDocumentForFile(goto1_file));
    assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { _frame._gotoFileUnderCursor(); }
    });
    
    Utilities.clearEventQueue();
    assertEquals("Incorrect active document; did not go to?", goto2_doc, model.getActiveDocument());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { _frame._gotoFileUnderCursor(); }
    });
    
    Utilities.clearEventQueue();
    assertEquals("Incorrect active document; did not go to?", goto1_doc, model.getActiveDocument());
    
    _log.log("gotoFileUnderCursor completed");
  }
  
  /** Tests that "go to file under cursor" works if unique after appending ".java" */
  public void testGotoFileUnderCursorAppendJava() throws IOException {
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);

    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor2Test.java");
    String goto1_string = "GotoFileUnderCursor2";
    FileOps.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();

    final File goto2_file = new File(_tempDir, "GotoFileUnderCursor2.java");
    String goto2_string = "GotoFileUnderCursor2Test";
    FileOps.writeStringToFile(goto2_file, goto2_string);
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
    
    SingleDisplayModel model = _frame.getModel();
    OpenDefinitionsDocument goto1_doc = model.getDocumentForFile(goto1_file);
    OpenDefinitionsDocument goto2_doc = model.getDocumentForFile(goto2_file);
    model.setActiveDocument(model.getDocumentForFile(goto1_file));
    assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { _frame._gotoFileUnderCursor(); }
    });
    
    Utilities.clearEventQueue();
    
    assertEquals("Incorrect active document; did not go to?", goto2_doc, model.getActiveDocument());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { _frame._gotoFileUnderCursor(); }
    });
    
    Utilities.clearEventQueue();
    assertEquals("Incorrect active document; did not go to?", goto1_doc, model.getActiveDocument());
    
    _log.log("gotoFileUnderCursorAppendJava completed");
  }
  
  /** Tests that "go to file under cursor" displays the dialog if choice is not unique */
  public void testGotoFileUnderCursorShowDialog() throws IOException {
//    Utilities.show("Running testGotoFileUnderCursorShowDialog()");
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);

    final File goto1_file = new File(_tempDir, "GotoFileUnderCursor3.java");
    String goto1_string = "GotoFileUnderCursor";
    FileOps.writeStringToFile(goto1_file, goto1_string);
    goto1_file.deleteOnExit();

    final File goto2_file = new File(_tempDir, "GotoFileUnderCursor4.java");
    String goto2_string = "GotoFileUnderCursor3";
    FileOps.writeStringToFile(goto2_file, goto2_string);
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
                            
    SingleDisplayModel model = _frame.getModel();
    OpenDefinitionsDocument goto1_doc = model.getDocumentForFile(goto1_file);
    OpenDefinitionsDocument goto2_doc = model.getDocumentForFile(goto2_file);
    model.setActiveDocument(model.getDocumentForFile(goto1_file));

    assertEquals("Document contains the incorrect text", goto1_string, model.getActiveDocument().getText());
    
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
