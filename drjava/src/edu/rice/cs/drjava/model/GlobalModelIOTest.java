package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.util.FileOps;
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
   * Creates a new document, modifies it, then allows a new document
   * to be created after ignoring the changes made.
   */
  public void testNewFileAllowAbandon() throws BadLocationException {
    setupDocument(FOO_TEXT);

    // Now try to "new" again and check for proper events
    TestListener listener = new TestListener() {
      public void newFileCreated() {
        assertAbandonCount(1);
        newCount++;
      }

      public boolean canAbandonFile(File file) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
    };

    _model.addListener(listener);
    _model.newFile();
    listener.assertNewCount(1);
    assertModified(false);
    assertLength(0);
  }

  /**
   * Creates a new document, modifies it, but disallows a call to create
   * a new document without saving changes.
   */
  public void testNewFileDisallowAbandon() throws BadLocationException {
    setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }
    };

    _model.addListener(listener);
    _model.newFile();
    listener.assertAbandonCount(1);
    assertModified(true);
    assertContents(FOO_TEXT);
  }

  /**
   * Opens a file, disregarding any changes made to the current document.
   */
  public void testOpenRealFileAllowAbandon()
    throws BadLocationException, IOException
  {
    final File tempFile = writeToNewTempFile(BAR_TEXT);

    TestListener listener = new TestListener() {
      public void fileOpened(File file) {
        assertEquals("file to open", tempFile, file);
        openCount++;
      }
    };

    _model.addListener(listener);
    _model.openFile(new FileSelector(tempFile));
    listener.assertOpenCount(1);
    assertModified(false);
    assertContents(BAR_TEXT);
  }

  /**
   * Initiates a file open, but cancels.
   */
  public void testCancelOpenFileAllowAbandon()
    throws BadLocationException, IOException
  {

    setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
    };

    _model.addListener(listener);
    _model.openFile(new CancelingSelector());
    assertModified(true);
    assertContents(FOO_TEXT);
  }

  /**
   * Attempts to open a non-existent file.
   */
  public void testOpenNonexistentFile()
    throws BadLocationException, IOException
  {
    _model.addListener(new TestListener());

    try {
      _model.openFile(new FileSelector(new File("fake-file")));
      fail("IO exception was not thrown!");
    }
    catch (FileNotFoundException fnf) {
      // As we hoped, the file was not found
    }

    assertLength(0);
    assertModified(false);
  }

  /**
   * Attempts to open a file, but decides to not throw away
   * changes, which causes the open to fail.
   */
  public void testOpenFileDisallowAbandon()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }
    };

    _model.addListener(listener);
    _model.openFile(new FileSelector(new File("junk-doesnt-exist")));
    listener.assertAbandonCount(1);
    assertModified(true);
    assertContents(FOO_TEXT);
  }

  /**
   * Attempts to make the first save of a document, but cancels instead.
   */
  public void testCancelFirstSave() throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);

    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());

    _model.saveFile(new CancelingSelector());
    assertModified(true);
    assertContents(FOO_TEXT);
  }

  /**
   * Makes a first save of the current document.
   */
  public void testRealSaveFirstSave() throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    _model.saveFile(new FileSelector(file));
    listener.assertSaveCount(1);
    assertModified(false);
    assertContents(FOO_TEXT);

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file));
  }

  /**
   * Saves a file already saved and overwrites its contents.
   */
  public void testSaveAlreadySaved() throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    assertModified(false);
    assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file));

    // Listener to use on future save
    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT);

    // Save over top of the previous file
    _model.saveFile(new FileSelector(file));
    listener.assertSaveCount(1);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 FileOps.readFile(file));
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
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    assertModified(false);
    assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file));

    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT);

    _model.saveFile(new CancelingSelector());

    // The file should have saved on top of the old text anyhow.
    // The canceling selector should never have been called.
    listener.assertSaveCount(1);
    assertModified(false);
    assertContents(BAR_TEXT);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 FileOps.readFile(file));
  }

  /**
   * Make sure that saveAs doesn't save if we cancel!
   */
  public void testCancelSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    assertModified(false);
    assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file));

    // No events better be fired!
    _model.addListener(new TestListener());

    // Muck up the document
    changeDocumentText(BAR_TEXT);

    _model.saveFileAs(new CancelingSelector());

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file));
  }

  /**
   * Make sure that saveAs saves to a different file.
   */
  public void testSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);
    final File file1 = tempFile();
    final File file2 = tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file1));
    assertModified(false);
    assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFile(file1));

    // Make sure we save now to the new file name
    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file2, f);
        saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT);

    _model.saveFileAs(new FileSelector(file2));

    assertEquals("contents of saved file1",
                 FOO_TEXT,
                 FileOps.readFile(file1));

    assertEquals("contents of saved file2",
                 BAR_TEXT,
                 FileOps.readFile(file2));
  }
}
