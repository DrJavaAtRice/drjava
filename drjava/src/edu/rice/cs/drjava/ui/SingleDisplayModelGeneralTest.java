package edu.rice.cs.drjava.ui;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.ListModel;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * Test general functions of the single display model.
 *
 * @version $Id$
 */
public class SingleDisplayModelGeneralTest extends SingleDisplayModelTestCase {
  /**
   * Constructor.
   * @param  String name
   */
  public SingleDisplayModelGeneralTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(SingleDisplayModelGeneralTest.class);
  }

  /**
   * Tests the invariant that at least one document is open
   * at time of creation.
   */
  public void testNotEmptyOnStartup() throws BadLocationException {
    // Should be one empty document after creation
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);
  }
  
  /**
   * Tests the setNext and setPrevious functions, making
   * sure that the activeDocumentChanged event is called.
   */
  public void testDocumentSwitching() throws BadLocationException {
    // Check for proper events
    TestListener listener = new TestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    _model.addListener(listener);
    
    // Set up first document
    OpenDefinitionsDocument doc1 = _model.getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    
    // Set up two more documents
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    assertActiveDocument(doc2);

    OpenDefinitionsDocument doc3 = setupDocument("");
    assertNumOpenDocs(3);
    listener.assertNewCount(2);
    listener.assertSwitchCount(2);
    assertActiveDocument(doc3);
    
    // Make sure setNext doesn't move (at end of list)
    _model.setNextActiveDocument();
    listener.assertSwitchCount(2);
    assertActiveDocument(doc3);
    
    // Test setPrevious
    _model.setPreviousActiveDocument();
    listener.assertSwitchCount(3);
    assertActiveDocument(doc2);
    
    _model.setPreviousActiveDocument();
    listener.assertSwitchCount(4);
    assertActiveDocument(doc1);
    
    // Make sure setPrevious doesn't move (at start of list)
    _model.setPreviousActiveDocument();
    listener.assertSwitchCount(4);
    assertActiveDocument(doc1);
    
    // Test setNext
    _model.setNextActiveDocument();
    listener.assertSwitchCount(5);
    assertActiveDocument(doc2);
    
    // Test setActive
    _model.setActiveDocument(0);
    listener.assertSwitchCount(6);
    assertActiveDocument(doc1);
    
    // Make sure number of docs hasn't changed
    assertNumOpenDocs(3);
    _model.removeListener(listener);
  }

  /**
   * Ensures that an unmodified, empty document is closed
   * after a file is opened, while a modified document
   * is left open.
   */
  public void testCloseUnmodifiedAutomatically() 
    throws BadLocationException, IOException
  {
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);

    // Check for proper events
    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        File file = null;
        try {
          file = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("file to open", tempFile, file);
        openCount++;
      }
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    _model.addListener(listener);
    
    // Open file, should replace the old
    try {
      doc = _model.openFile(new FileSelector(tempFile));
      listener.assertOpenCount(1);
      listener.assertCloseCount(1);
      listener.assertSwitchCount(1);
      assertNumOpenDocs(1);
      assertModified(false, doc);
      assertContents(BAR_TEXT, doc);
    }
    catch (AlreadyOpenException aoe) {
      // Should not be open
      fail("File was already open!");
    }
    catch (OperationCanceledException oce) {
      // Should not be canceled
      fail("Open was unexpectedly canceled!");
    }
    
    _model.removeListener(listener);
  }
  
  /**
   * Tests that active document is switched on close, and that
   * a new file is created after the last one is closed.
   */
  public void testCloseFiles() throws BadLocationException {
    // Check for proper events
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
      
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    _model.addListener(listener);
    
    // Set up two documents
    OpenDefinitionsDocument doc1 = _model.getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertActiveDocument(doc2);
    assertNumOpenDocs(2);
    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    
    // Close one
    _model.closeFile(_model.getActiveDocument());
    assertNumOpenDocs(1);
    listener.assertCloseCount(1);
    listener.assertAbandonCount(1);
    listener.assertSwitchCount(2);
    assertActiveDocument(doc1);
    assertContents(FOO_TEXT, _model.getActiveDocument());
    
    // Close the other
    _model.closeFile(_model.getActiveDocument());
    listener.assertCloseCount(2);
    listener.assertAbandonCount(2);
    
    // Ensure a new document was created
    assertNumOpenDocs(1);
    listener.assertNewCount(2);
    listener.assertSwitchCount(3);
    assertLength(0, _model.getActiveDocument());
    
    // Set up two documents
    doc1 = _model.getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(3);
    
    // Close all files, ensure new one was created
    _model.closeAllFiles();
    assertNumOpenDocs(1);
    assertLength(0, _model.getActiveDocument());
    listener.assertNewCount(4);
    listener.assertCloseCount(4);
    listener.assertAbandonCount(4);
    
    _model.removeListener(listener);
  }
  
}
