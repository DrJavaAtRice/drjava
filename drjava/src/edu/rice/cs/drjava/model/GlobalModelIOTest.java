package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * Test I/O functions of the global model.
 *
 * @version $Id$
 */
public class GlobalModelIOTest extends GlobalModelTestCase {
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelIOTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelIOTest.class);
  }

  /**
   * Creates a new document, modifies it, and then does the same
   * with a second document, ensuring that the changes are separate.
   */
  public void testMultipleFiles() throws BadLocationException {
    assertNumOpenDocs(0);

    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(1);

    // Create a second, empty document
    OpenDefinitionsDocument doc2 = _model.newFile();
    assertNumOpenDocs(2);
    assertModified(true, doc1);
    assertModified(false, doc2);
    assertContents(FOO_TEXT, doc1);
    assertLength(0, doc2);

    // Modify second document
    changeDocumentText(BAR_TEXT, doc2);
    assertModified(true, doc2);
    assertContents(FOO_TEXT, doc1);
    assertContents(BAR_TEXT, doc2);
  }

  /**
   * Opens several documents and ensures that the array
   * returned by the model is correct and in the right order.
   */
  public void testMultipleFilesArray() throws BadLocationException {
    OpenDefinitionsDocument doc1, doc2, doc3;
    doc1 = setupDocument(FOO_TEXT);
    doc2 = setupDocument(BAR_TEXT);
    doc3 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(3);

    OpenDefinitionsDocument[] docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 3, docs.length);

    assertEquals("document 1", doc1, docs[0]);
    assertEquals("document 2", doc2, docs[1]);
    assertEquals("document 3", doc3, docs[2]);
  }


  /**
   * Ensures closing documents works correctly.
   */
  public void testCloseMultipleFiles() throws BadLocationException {
    assertNumOpenDocs(0);
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);

    _model.closeFile(doc1);
    assertNumOpenDocs(1);

    OpenDefinitionsDocument[] docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 1, docs.length);
    assertContents(BAR_TEXT, docs[0]);

    _model.closeFile(doc2);
    assertNumOpenDocs(0);
    docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 0, docs.length);
  }


  /**
   * Creates a new document, modifies it, then allows it
   * to be closed, ignoring the changes made.
   */
  public void testCloseFileAllowAbandon() throws BadLocationException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    // Try to close and check for proper events
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
        assertAbandonCount(1);
        closeCount++;
      }
    };

    _model.addListener(listener);
    _model.closeFile(doc);
    listener.assertCloseCount(1);
  }

  /**
   * Creates a new document, modifies it, but disallows a call to
   * close it without saving changes.
   */
  public void testCloseFileDisallowAbandon() throws BadLocationException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
         closeCount++;
      }
    };

    _model.addListener(listener);
    _model.closeFile(doc);
    listener.assertAbandonCount(1);
    listener.assertCloseCount(0);
  }

  /**
   * Opens a file.
   */
  public void testOpenRealFile()
    throws BadLocationException, IOException
  {
    final File tempFile = writeToNewTempFile(BAR_TEXT);

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
    };

    _model.addListener(listener);
    try {
      OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile));
      listener.assertOpenCount(1);
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
  }

  /**
   * Initiates a file open, but cancels.
   */
  public void testCancelOpenFile()
    throws BadLocationException, IOException
  {

    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    assertNumOpenDocs(1);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }

      public void fileOpened(OpenDefinitionsDocument doc) {
        openCount++;
      }
    };

    _model.addListener(listener);
    try {
      OpenDefinitionsDocument newDoc =
        _model.openFile(new CancelingSelector());
    }
    catch (AlreadyOpenException aoe) {
      // Should not be open
      fail("File was already open!");
    }
    catch (OperationCanceledException oce) {
      // we expect this to be thrown
    }
    finally {
      assertNumOpenDocs(1);
      listener.assertOpenCount(0);

      OpenDefinitionsDocument[] docs = _model.getDefinitionsDocuments();
      doc = docs[0];
      assertModified(true, doc);
      assertContents(FOO_TEXT, doc);
    }
  }


  /**
   * Attempts to open a non-existent file.
   */
  public void testOpenNonexistentFile()
    throws BadLocationException, IOException
  {
    _model.addListener(new TestListener());

    OpenDefinitionsDocument doc = null;

    try {
      doc = _model.openFile(new FileSelector(new File("fake-file")));
      fail("IO exception was not thrown!");
    }
    catch (FileNotFoundException fnf) {
      // As we hoped, the file was not found
    }
    catch (AlreadyOpenException aoe) {
      // Should not be open
      fail("File was already open!");
    }
    catch (OperationCanceledException oce) {
      // Should not be canceled
      fail("Open was unexpectedly canceled!");
    }

    assertEquals("non-existant file", doc, null);
  }

  /**
   * Attempts to reopen an already open file.
   */
  public void testReopenFile()
    throws BadLocationException, IOException
  {
    final File tempFile = writeToNewTempFile(BAR_TEXT);

    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        File file = null;
        try {
          file = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not yet exist");
        }
        assertEquals("file to open", tempFile, file);
        openCount++;
      }
    };

    _model.addListener(listener);
    try {
      OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile));
      listener.assertOpenCount(1);
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

    // Now reopen
    try {
      OpenDefinitionsDocument doc2 = _model.openFile(new FileSelector(tempFile));
      fail("file should already be open");
    }
    catch (AlreadyOpenException aoe) {
      // Should not be open
      listener.assertOpenCount(1);
    }
    catch (OperationCanceledException oce) {
      // Should not be canceled
      fail("Open was unexpectedly canceled!");
    }

  }


  /**
   * Attempts to make the first save of a document, but cancels instead.
   */
  public void testCancelFirstSave() throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());

    doc.saveFile(new CancelingSelector());
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
  }

  /**
   * Makes a first save of the current document.
   */
  public void testRealSaveFirstSave() throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file exists
          fail("file does not exist");
        }
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    doc.saveFile(new FileSelector(file));
    listener.assertSaveCount(1);
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));
  }

  /**
   * Saves a file already saved and overwrites its contents.
   */
  public void testSaveAlreadySaved() throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));

    // Listener to use on future save
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file exists
          fail("file does not exist");
        }
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);

    // Save over top of the previous file
    doc.saveFile(new FileSelector(file));
    listener.assertSaveCount(1);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 FileOps.readFileAsString(file));
  }

  /**
   * First we save the document with FOO_TEXT.
   * Then we tell it to save over the old text, but pass in a CancelingSelector
   * to cancel if we are asked for a new file name. This should not happen
   * since the file is already saved.
   */
  public void testCancelSaveAlreadySaved()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));

    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file exists
          fail("file does not exist");
        }
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);

    doc.saveFile(new CancelingSelector());

    // The file should have saved on top of the old text anyhow.
    // The canceling selector should never have been called.
    listener.assertSaveCount(1);
    assertModified(false, doc);
    assertContents(BAR_TEXT, doc);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 FileOps.readFileAsString(file));
  }

  /**
   * Make sure that saveAs doesn't save if we cancel!
   */
  public void testCancelSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));

    // No events better be fired!
    _model.addListener(new TestListener());

    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);

    doc.saveFileAs(new CancelingSelector());

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));
  }

  /**
   * Make sure that saveAs saves to a different file.
   */
  public void testSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file1 = tempFile();
    final File file2 = tempFile();

    // No listeners here -- other tests ensure the first save works
    doc.saveFile(new FileSelector(file1));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file1));

    // Make sure we save now to the new file name
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file exists
          fail("file does not exist");
        }
        assertEquals("saved file name", file2, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);

    doc.saveFileAs(new FileSelector(file2));

    assertEquals("contents of saved file1",
                 FOO_TEXT,
                 FileOps.readFileAsString(file1));

    assertEquals("contents of saved file2",
                 BAR_TEXT,
                 FileOps.readFileAsString(file2));
  }
}
