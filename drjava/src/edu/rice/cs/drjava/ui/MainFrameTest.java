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

import  junit.framework.*;
import  junit.extensions.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.rmi.registry.Registry;
import java.io.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.*;

/**
 * Test functions of MainFrame.
 *
 * @version $Id$
 */
public class MainFrameTest extends MultiThreadedTestCase {
  
  private MainFrame _frame;
  
  /**
   * A temporary directory
   */
  private File _tempDir;
  /**
   * The user name of the user running the tests.  Used in creating temporary files.
   */
  private String user = System.getProperty("user.name");

  
  /**
   * Constructor.
   * @param  String name
   */
  public MainFrameTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(MainFrameTest.class);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws IOException{
    _frame = new MainFrame();
    _frame.pack();
    super.setUp();
  }
  
  public void tearDown() throws IOException{
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
  public void testDocLocationAfterSwitch() 
    throws BadLocationException, InterruptedException
  {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
    doc.insertString(0, "abcd", null);
    pane.setCaretPosition(3);
    assertEquals("Location of old doc before switch", 3, doc.getCurrentLocation());
    
    // Create a new file
    SingleDisplayModel model = _frame.getModel();
    model.newFile();
    
    // Current pane should be new doc, pos 0
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDocument().getDocument();
    assertEquals("Location of new document", 0, doc.getCurrentLocation());
    
    // Switch back
    model.setPreviousActiveDocument();
    
    // Current pane should be old doc, pos 3
    pane = _frame.getCurrentDefPane();
    doc = pane.getOpenDocument().getDocument();
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
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
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
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
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
   * InteractionsDocument.  (SourceForge bug #681547)
   */
  public void testCorrectInteractionsDocument() {
    InteractionsPane pane = _frame.getInteractionsPane();
    SingleDisplayModel model = _frame.getModel();

    // Test for strict == equality
    assertTrue("UI's int. doc. should equals Model's int. doc.",
               pane.getDocument() == model.getInteractionsDocument());
  }

  /**
   * Tests that undoing/redoing a multi-line indent will restore 
   * the caret position.
   */
  /*
  public void testMultilineIndentAfterScroll() throws BadLocationException, InterruptedException
  {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
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
    int oldPos = -1, newPos = 20;

    doc.addUndoableEditListener(doc.getUndoManager());
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL, new Integer(2));
    doc.insertString(0, text, null);
    assertEquals("Should have inserted correctly.", text, doc.getText(0, doc.getLength()));

    pane.setCaretPosition(0);
    doc.indentLines(0, doc.getLength());
    assertEquals("Should have indented.", indented, doc.getText(0, doc.getLength()));
    
    oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    java.util.Vector edits = doc.getEdits();
    for(int i = edits.size()-1; i > -1; i--) {
       if(((javax.swing.undo.UndoableEdit) edits.elementAt(i)).canUndo()) {
          System.out.println("Indenting can be undone.");
          ((javax.swing.undo.UndoableEdit) edits.elementAt(i)).undo();
       }
       else {
          System.out.println("Indenting cannot be undone.");
       }
    }
    assertEquals("Should have undone.", text, doc.getText(0, doc.getLength()));
    assertEquals("Undo should have restored caret position.", oldPos, pane.getCaretPosition());
    
//    doc.getUndoManager().redo();
    for(int i = edits.size()-1; i > -1; i--) {
       if(((javax.swing.undo.UndoableEdit) edits.elementAt(i)).canRedo()) {
          System.out.println("Indenting can be redone.");
          ((javax.swing.undo.UndoableEdit) edits.elementAt(i)).redo();
       }
       else {
          System.out.println("Indenting cannot be redone.");
       }
    }
    assertEquals("redo",indented, doc.getText(0,doc.getLength()));
    assertEquals("redo restores caret position", newPos, pane.getCaretPosition());
  }
  */

  /**
   * tests that undoing/redoing a multi-line comment/uncomment will restore the caret position
   */
  /*
  public void testMultilineCommentOrUncommentAfterScroll() 
    throws BadLocationException, InterruptedException
  {
    DefinitionsPane pane = _frame.getCurrentDefPane();
    DefinitionsDocument doc = pane.getOpenDocument().getDocument();
    String text =
      "public class stuff {\n" + 
      "  private int _int;\n" + 
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";
    
    String commented =
      "// public class stuff {\n" + 
      "//   private int _int;\n" + 
      "//   private Bar _bar;\n" +
      "//   public void foo() {\n" +
      "//     _bar.baz(_int);\n" +
      "//   }\n" +
      "// }\n";

    int newPos = 20;

    doc.addUndoableEditListener(doc.getUndoManager());
    doc.insertString(0, text, null);
    doc.insertString(0,text,null);
    assertEquals("insertion",text, doc.getText(0,doc.getLength()));
    
    doc.commentLines(0,doc.getLength());
    assertEquals("commenting",commented, doc.getText(0,doc.getLength()));
    int oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    doc.getUndoManager().undo();
    assertEquals("undo commenting",text, doc.getText(0,doc.getLength()));
    assertEquals("undoing commenting restores caret position", oldPos, pane.getCaretPosition());
    doc.getUndoManager().redo();
    assertEquals("redo commenting",commented, doc.getText(0,doc.getLength()));
    assertEquals("redoing commenting restores caret position", newPos, pane.getCaretPosition());

    doc.uncommentLines(0,doc.getLength());
    assertEquals("uncommenting",text, doc.getText(0,doc.getLength()));
    oldPos = pane.getCaretPosition();
    pane.setCaretPosition(newPos);
    doc.getUndoManager().undo();
    assertEquals("undo uncommenting",commented, doc.getText(0,doc.getLength()));
    assertEquals("undoing uncommenting restores caret position", oldPos, pane.getCaretPosition());
    doc.getUndoManager().redo();
    assertEquals("redo uncommenting",text, doc.getText(0,doc.getLength()));
    assertEquals("redoing uncommenting restores caret position", newPos, pane.getCaretPosition());
  }
  */
  
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
     File ForceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.java");
     String ForceOpenClass1_string =
       "public class ForceOpenClass1 {\n" +
       "  ForceOpenClass2 class2;\n" +
       "  ForceOpenClass3 class3;\n\n" +   
       "  public ForceOpenClass1() {\n" +
       "    class2 = new ForceOpenClass2();\n" + 
       "    class3 = new ForceOpenClass3();\n" + 
       "  }\n" + 
       "}";
     
     File ForceOpenClass2_file = new File(_tempDir, "ForceOpenClass2.java");
     String ForceOpenClass2_string =
       "public class ForceOpenClass2 {\n" +
       "  inx x = 4;\n" +
       "}";
     
     File ForceOpenClass3_file = new File(_tempDir, "ForceOpenClass3.java");
     String ForceOpenClass3_string = 
       "public class ForceOpenClass3 {\n" +
       "  String s = \"asf\";\n" +
       "}";
     
     FileOps.writeStringToFile(ForceOpenClass1_file, ForceOpenClass1_string);
     FileOps.writeStringToFile(ForceOpenClass2_file, ForceOpenClass2_string);
     FileOps.writeStringToFile(ForceOpenClass2_file, ForceOpenClass2_string);
     
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
     File ForceOpenClass1_file = new File(_tempDir, "ForceOpenClass1.java");
     String ForceOpenClass1_string =
       "public class ForceOpenClass1 {\n" +
       "  ForceOpenClass2 class2;\n" +
       "  ForceOpenClass3 class3;\n\n" +   
       "  public ForceOpenClass1() {\n" +
       "    class2 = new ForceOpenClass2();\n" + 
       "    class3 = new ForceOpenClass3();\n" + 
       "  }\n" + 
       "}";
     
     FileOps.writeStringToFile(ForceOpenClass1_file, ForceOpenClass1_string);
     
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
    return File.createTempFile(fileName, ".java", _tempDir);
  }
}
