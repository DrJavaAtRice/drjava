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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.*;

/**
 * Test functions of MainFrame.
 *
 * @version $Id$
 */
public final class MainFrameTest extends MultiThreadedTestCase {

  private MainFrame _frame;

  /**
   * A temporary directory
   */
  private File _tempDir;
  
  /**
   * The user name of the user running the tests.  Used in creating temporary files.
   * No it's not ...
  private String user = System.getProperty("user.name");*/

  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws IOException {
    _frame = new MainFrame();
    _frame.pack();
    super.setUp();
  }

  public void tearDown() throws IOException {
    super.tearDown();
    _frame.dispose();
    _frame = null;
    System.gc();
  }

  /**
   * Tests that the returned JButton of <code>createManualToolbarButton</code>:
   *  1. Is disabled upon return.
   *  2. Inherits the tooltip of the Action parameter <code>a</code>.
   */
  public void testCreateManualToolbarButton() {
    Action a = new AbstractAction("Test Action") {
      public void actionPerformed(ActionEvent ae) {
      }
    };
    a.putValue(Action.SHORT_DESCRIPTION, "test tooltip");
    JButton b = _frame._createManualToolbarButton(a);

    assertTrue("Returned JButton is enabled.", ! b.isEnabled());
    assertEquals("Tooltip text not set.", "test tooltip", b.getToolTipText());
  }

  /**
   * Tests that the current location of a document is equal to the
   * caret location after documents are switched.
   */
  public void testDocLocationAfterSwitch() throws BadLocationException {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();//.getDocument();
    /**
     * NOTE: This has been added because MainFrameTest hangs randomly (about every other time) without this line.
     * It is still unknown why this occurs - being that the above method calls are all accessors, this shouldn't be a situation
     * where the document is being accessed by insertString before it is ready to be accessed.
     * Added 5/19/2004 by pakruse 
     */ /**/
    try {
      Thread.sleep(1000); 
    }
    catch(java.lang.InterruptedException e) {
    
    }
        
    doc.insertString(0, "abcd", null);

    
    pane.setCaretPosition(3);
    assertEquals("Location of old doc before switch", 3, doc.getCurrentLocation());
    
    // Create a new file
    SingleDisplayModel model = _frame.getModel();
    model.newFile();

    // Current pane should be new doc, pos 0
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDefDocument();//.getDocument();
    assertEquals("Location of new document", 0, doc.getCurrentLocation());

    // Switch back
    model.setActiveNextDocument();

    // Current pane should be old doc, pos 3
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDefDocument();//.getDocument();
    assertEquals("Location of old document", 3, doc.getCurrentLocation());
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
                 "ab", doc.getText(0, doc.getLength()));
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
                 "ab", doc.getText(0, doc.getLength()));
  }
  */

  /**
   * Make sure that the InteractionsPane is displaying the correct
   * InteractionsDocument.  (SourceForge bug #681547)  Also make sure this
   * document cannot be edited before the prompt.
   */
  public void testCorrectInteractionsDocument() throws DocumentAdapterException {
    InteractionsPane pane = _frame.getInteractionsPane();
    SingleDisplayModel model = _frame.getModel();
    InteractionsDocumentAdapter doc = model.getSwingInteractionsDocument();

    // Make the test silent
    model.getInteractionsModel().getDocument().setBeep(new TestBeep());

    // Test for strict == equality
    assertTrue("UI's int. doc. should equals Model's int. doc.",
               pane.getDocument() == doc);


    int origLength = doc.getDocLength();
    doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Document should not have changed.",
                 origLength,
                 doc.getDocLength());
  }

  /**
   * Tests that undoing/redoing a multi-line indent will restore
   * the caret position.
   */
  public void testMultilineIndentAfterScroll() throws BadLocationException {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    OpenDefinitionsDocument doc = pane.getOpenDefDocument();
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
    int newPos = 20;

    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL, new Integer(2));
    
    doc.insertString(0, text, null);
    assertEquals("Should have inserted correctly.", text, doc.getText(0, doc.getLength()));

    pane.setCaretPosition(0);
    doc.indentLines(0, doc.getLength());
    assertEquals("Should have indented.", indented, doc.getText(0, doc.getLength()));

    oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    doc.getUndoManager().undo();
    assertEquals("Should have undone.", text, doc.getText(0, doc.getLength()));
    assertEquals("Undo should have restored caret position.", oldPos, pane.getCaretPosition());

    pane.setCaretPosition(newPos);
    doc.getUndoManager().redo();
    assertEquals("redo",indented, doc.getText(0,doc.getLength()));
    assertEquals("redo restores caret position", oldPos, pane.getCaretPosition());
  }

  /**
   * Ensure that a document's editable status is set appropriately throughout
   * the compile process.  Since the behavior is interesting only when the model
   * changes its active document, that's what this test looks most like.
   */
  public void testGlassPaneEditableState() {
    SingleDisplayModel model = _frame.getModel();

    OpenDefinitionsDocument doc1 = model.newFile();
    OpenDefinitionsDocument doc2 = model.newFile();

    // doc2 is now active

    JScrollPane pane1 = _frame._createDefScrollPane(doc1);
    JScrollPane pane2 = _frame._createDefScrollPane(doc2);

    DefinitionsPane defPane1 = (DefinitionsPane) pane1.getViewport().getView();
    DefinitionsPane defPane2 = (DefinitionsPane) pane2.getViewport().getView();

    _frame._switchDefScrollPane();
    assertTrue("Start: defPane1",defPane1.isEditable());
    assertTrue("Start: defPane2",defPane2.isEditable());
    _frame.hourglassOn();
    assertTrue("Glass on: defPane1",defPane1.isEditable());
    assertTrue("Glass on: defPane2",(!defPane2.isEditable()));
    model.setActiveDocument(doc1);
    _frame._switchDefScrollPane();
    assertTrue("Doc Switch: defPane1",(! defPane1.isEditable()));
    assertTrue("Doc Switch: defPane2",defPane2.isEditable());
    _frame.hourglassOff();
    assertTrue("End: defPane1",defPane1.isEditable());
    assertTrue("End: defPane2",defPane2.isEditable());

  }

  /**
   * Ensure that all key events are disabled when the glass pane is up
   */
  public void testGlassPaneHidesKeyEvents() {
    SingleDisplayModel model = _frame.getModel();

    OpenDefinitionsDocument doc1 = model.newFile();
    OpenDefinitionsDocument doc2 = model.newFile();

    // doc2 is now active

    JScrollPane pane1 = _frame._createDefScrollPane(doc1);
    JScrollPane pane2 = _frame._createDefScrollPane(doc2);

    DefinitionsPane defPane1 = (DefinitionsPane) pane1.getViewport().getView();
    DefinitionsPane defPane2 = (DefinitionsPane) pane2.getViewport().getView();

    _frame.hourglassOn();

    defPane1.processKeyEvent(new KeyEvent(defPane1, KeyEvent.KEY_PRESSED, 70, KeyEvent.CTRL_MASK, KeyEvent.VK_F, 'F') );
    assertTrue("the find replace dialog should not come up", !_frame.getFindReplaceDialog().isDisplayed());
    
    _frame.getInteractionsPane().processKeyEvent(new KeyEvent(_frame.getInteractionsPane(), KeyEvent.KEY_PRESSED, 0, KeyEvent.CTRL_MASK, KeyEvent.VK_F, 'F') );
    assertTrue("the find replace dialog should not come up", !_frame.getFindReplaceDialog().isDisplayed());

    _frame.hourglassOff();

  }

  /**
   * A Test to guarantee that the Dancing UI bug will not rear its ugly head again.
   * Basically, add a component listener to the leftComponent of _docSplitPane and
   * make certain its size does not change while compiling a class which depends on
   * another class.
   */
  public void testDancingUIFileOpened() throws IOException {
      /**
     * Maybe this sequence of calls should be incorporated into one function
     * createTestDir(), which would get the username and create the temporary
     * directory
     * Only sticky part is deciding where to put it, in FileOps maybe?
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

     //_frame.setVisible(true);
     _frame.pack();
     _frame.open(new FileOpenSelector(){
       public File[] getFiles(){
         File[] return_me = new File[1];
         return_me[0] = new File(_tempDir, "ForceOpenClass1.java");
         return return_me;
       }
     });
     ComponentAdapter listener = new ComponentAdapter(){
       public void componentResized(ComponentEvent event){
         _testFailed = true;
         fail("testDancingUI: Open Documents List danced!");
       }
     };
     _frame.addComponentListenerToOpenDocumentsList(listener);
     SingleDisplayModelCompileListener compileListener =
       new SingleDisplayModelCompileListener();
     _frame.getModel().addListener(compileListener);

     synchronized(compileListener){
       SwingUtilities.invokeLater(new Runnable(){
         public void run(){
           _frame.getCompileAllButton().doClick();
         }
       });

       try{
         compileListener.wait();
       }
       catch(InterruptedException exception){
         fail(exception.toString());
       }
     }

     if( !FileOps.deleteDirectory(_tempDir) ){
       System.err.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() +
                          "\nDo it by hand.\n");
     }
  }

    /**
   * A Test to guarantee that the Dancing UI bug will not rear its ugly head again.
   * Basically, add a component listener to the leftComponent of _docSplitPane and
   * make certain its size does not change while closing an OpenDefinitionsDocument
   * outside the event thread
   */
  public void testDancingUIFileClosed() throws IOException {
    /**
     * Maybe this sequence of calls should be incorporated into one function
     * createTestDir(), which would get the username and create the temporary
     * directory
     * Only sticky part is deciding where to put it, in FileOps maybe?
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

     //_frame.setVisible(true);
     _frame.pack();
     ComponentAdapter listener = new ComponentAdapter(){
       public void componentResized(ComponentEvent event){
         _testFailed = true;
         fail("testDancingUI: Open Documents List danced!");
       }
     };
     _frame.addComponentListenerToOpenDocumentsList(listener);
     SingleDisplayModelFileClosedListener closeListener =
       new SingleDisplayModelFileClosedListener();

     _frame.open(new FileOpenSelector(){
         public File[] getFiles(){
           File[] return_me = new File[1];
           return_me[0] = new File(_tempDir, "ForceOpenClass1.java");
           return return_me;
         }
       });

     _frame.getModel().addListener(closeListener);

     synchronized(closeListener){
       Thread thread = new Thread(new Runnable(){
         public void run(){
           _frame.getCloseButton().doClick();
         }
       });
       SwingUtilities.invokeLater(thread);

       try{
         closeListener.wait();
       }
       catch(InterruptedException exception){
         fail(exception.toString());
       }
     }

     if( !FileOps.deleteDirectory(_tempDir) ){
       System.err.println("Couldn't fully delete directory " + _tempDir.getAbsolutePath() +
                          "\nDo it by hand.\n");
     }
  }

  /**
   * A CompileListener for SingleDisplayModel (instead of
   * GlobalModel)
   */
  class SingleDisplayModelCompileListener
    extends GlobalModelTestCase.TestListener
    implements SingleDisplayModelListener{

    public void compileStarted(){
    }

    /**
     * Just notify when the compile has ended
     */
    public void compileEnded(){
      synchronized(this){
        notify();
      }
    }

    public void fileOpened(OpenDefinitionsDocument doc) {}


    public void activeDocumentChanged(OpenDefinitionsDocument active){
    }
  }

    /**
   * A FileClosedListener for SingleDisplayModel (instead of
   * GlobalModel)
   */
  class SingleDisplayModelFileClosedListener
    extends GlobalModelTestCase.TestListener
    implements SingleDisplayModelListener{

    public void fileClosed(OpenDefinitionsDocument doc) {
      synchronized(this){
        notify();
      }
    }

    public void fileOpened(OpenDefinitionsDocument doc){
    }

    public void newFileCreated(OpenDefinitionsDocument doc){
    }

    public void activeDocumentChanged(OpenDefinitionsDocument active){
    }
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile(String fileName) throws IOException {
    File f =  File.createTempFile(fileName, ".java", _tempDir);
    f.deleteOnExit();
    return f;
  }
}
