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

package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import java.util.List;
import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import junit.extensions.*;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.ListModel;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

/**
 * Test I/O functions of the global model.
 *
 * @version $Id$
 */
public final class GlobalModelIOTest extends GlobalModelTestCase
  implements OptionConstants
{
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelIOTest(String name) {
    super(name);
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

    List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 3, docs.size());

    assertEquals("document 1", doc1, docs.get(0));
    assertEquals("document 2", doc2, docs.get(1));
    assertEquals("document 3", doc3, docs.get(2));
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

    List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 1, docs.size());
    assertContents(BAR_TEXT, docs.get(0));

    _model.closeFile(doc2);
    assertNumOpenDocs(0);
    docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 0, docs.size());
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
        catch (FileMovedException fme) {
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
      //OpenDefinitionsDocument newDoc =
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

      List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
      doc = docs.get(0);
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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        try {
          assertEquals("file to open", tempFile.getCanonicalPath(),
                       file.getCanonicalPath());
        }
        catch (IOException ioe) {
          throw new UnexpectedException(ioe);
        }
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
      //OpenDefinitionsDocument doc2 =
        _model.openFile(new FileSelector(tempFile));
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

    // Now reopen same file with a different path
    //  eg. /tmp/MyFile -> /tmp/./MyFile
    try {
      File parent = tempFile.getParentFile();
      String dotSlash = "." + System.getProperty("file.separator");
      parent = new File(parent, dotSlash);
      File sameFile = new File(parent, tempFile.getName());
      //OpenDefinitionsDocument doc2 =
        _model.openFile(new FileSelector(sameFile));
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
   * Opens multiple files.
   */
  public void testOpenMultipleFiles()
    throws BadLocationException, IOException
  {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    final File tempFile2 = writeToNewTempFile(BAR_TEXT);

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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }

        if (tempFile1.equals(file)) {
          assertEquals("file to open", tempFile1, file);
        } else {
          assertEquals("file to open", tempFile2, file);
        }

        openCount++;
      }
    };

    _model.addListener(listener);
    try {
      OpenDefinitionsDocument doc = _model.openFiles(new FileSelector(tempFile1,tempFile2));
      listener.assertOpenCount(2);
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
    listener.assertOpenCount(2);
    List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
    assertEquals("size of document array", 2, docs.size());
    assertContents(FOO_TEXT, docs.get(0));
    assertContents(BAR_TEXT, docs.get(1));

  }

  /**
   * Initiates a file open, but cancels.
   */
  public void testCancelOpenMultipleFiles()
    throws BadLocationException, IOException
  {

    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);

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
      //OpenDefinitionsDocument newDoc =
        _model.openFiles(new CancelingSelector());
    }
    catch (AlreadyOpenException aoe) {
      // Should not be open
      fail("File was already open!");
    }
    catch (OperationCanceledException oce) {
      // we expect this to be thrown
    }
    finally {
      assertNumOpenDocs(2);
      listener.assertOpenCount(0);

      List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
      doc1 = docs.get(0);
      assertModified(true, doc1);
      assertContents(FOO_TEXT, doc1);

      doc2 = docs.get(1);
      assertModified(true, doc2);
      assertContents(BAR_TEXT, doc2);
    }
  }


  /**
   * Attempts to open a non-existent file.
   */
  public void testOpenMultipleNonexistentFiles()
    throws BadLocationException, IOException
  {

    OpenDefinitionsDocument doc = null;
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);

    //TestListener listener = new TestListener();
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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("file to open", tempFile1, file);

        openCount++;
      }
    };
    _model.addListener(listener);


    try {
      doc = _model.openFiles(new FileSelector(tempFile1,
                                              new File("fake-file")));
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
    listener.assertOpenCount(1);
  }

  /**
   * Error checking for openening multiple files
   * checks for null and an array w/null
   */
  public void testOpenMultipleFilesError()
    throws BadLocationException, IOException
  {

    OpenDefinitionsDocument doc = null;
    //final File tempFile1 = writeToNewTempFile(FOO_TEXT);

    try {
      doc = _model.openFiles(new FileOpenSelector() {
        public File getFile() throws OperationCanceledException {
          return null;
        }
        public File[] getFiles() throws OperationCanceledException {
          return new File[] {null};
        }
      });
      fail("IO exception was not thrown!");
    }
    catch (IOException e) {
      // As we hoped, the file was not found
    }
    catch (Exception e) {
      fail("Unexpectedly exception caught!");
    }

    try {
      doc = _model.openFiles(new FileOpenSelector() {
        public File getFile() throws OperationCanceledException {
          return null;
        }
        public File[] getFiles() throws OperationCanceledException {
          return null;
        }
      });

      fail("IO exception was not thrown!");
    }
    catch (IOException e) {
      // As we hoped, the file was not found
    }
    catch (Exception e) {
      fail("Unexpectedly exception caught!");
    }

    assertEquals("non-existant file", doc, null);
  }

  /**
   * Force a file to be opened with getDocumentforFile
   */
  public void testForceFileOpen()
    throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException
  {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    final File tempFile2 = writeToNewTempFile(BAR_TEXT);
    // don't catch and fail!

    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        //File file = null;
        try {
          //file =
          doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not exist");
        }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        openCount++;
      }
    };

    _model.addListener(listener);
    // Open file 1
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
    listener.assertOpenCount(1);
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);

    // Get file 1
    OpenDefinitionsDocument doc1 = _model.getDocumentForFile(tempFile1);
    listener.assertOpenCount(1);
    assertEquals("opened document", doc, doc1);
    assertContents(FOO_TEXT, doc1);

    // Get file 2, forcing it to be opened
    OpenDefinitionsDocument doc2 = _model.getDocumentForFile(tempFile2);
    listener.assertOpenCount(2);
    assertContents(BAR_TEXT, doc2);
  }

  /**
   * Attempts to make the first save of a document, but cancels instead.
   */
  public void testCancelFirstSave() throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());

    boolean saved = doc.saveFile(new CancelingSelector());
    assertTrue("doc should not have been saved", !saved);
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
        catch (FileMovedException fme) {
          // We know file should exist
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
    //disable file backups, remember original setting
    Boolean backupStatus = DrJava.getConfig().getSetting(BACKUP_FILES);
    DrJava.getConfig().setSetting(BACKUP_FILES, Boolean.FALSE);

    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listeners here -- other tests ensure the first save works
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));

    // Listener to use on future saves
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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("saved file name", file, f);
        saveCount++;
      }
    };

    File backup = new File(file.getPath() + "~");
    backup.delete();

    _model.addListener(listener);

    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);

    // Save over top of the previous file
    doc.saveFile(new FileSelector(file));
    listener.assertSaveCount(1);

    assertEquals("contents of saved file 2nd write",
                 BAR_TEXT,
                 FileOps.readFileAsString(file));

    assertEquals("no backup was made", false, backup.exists());


    //enable file backups
    DrJava.getConfig().setSetting(BACKUP_FILES, Boolean.TRUE);

    // Muck up the document
    changeDocumentText(FOO_TEXT, doc);

    // Save over top of the previous file
    doc.saveFile(new FileSelector(file));
    listener.assertSaveCount(2);

    assertEquals("contents of saved file 3rd write",
                 FOO_TEXT,
                 FileOps.readFileAsString(file));
    assertEquals("contents of backup file 3rd write",
                 BAR_TEXT,
                 FileOps.readFileAsString(backup));

    /* Set the config back to the original option */
    DrJava.getConfig().setSetting(BACKUP_FILES, backupStatus);
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
        catch (FileMovedException fme) {
          // We know file should exist
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
        catch (FileMovedException fme) {
          // We know file should exist
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

  public void testSaveAsExistsForOverwrite()
    throws BadLocationException, IOException {

    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file1 = tempFile();
    try {
      doc.saveFileAs(new WarningFileSelector(file1));
      fail("Did not ask to verify overwrite as expected");
    }
    catch (OverwriteException e1) {
      // good behavior for file saving...
    }
  }

  public void testSaveAsExistsAndOpen()
    throws BadLocationException, IOException,
    OperationCanceledException, AlreadyOpenException{
    OpenDefinitionsDocument doc1,doc2;
    final File file1,file2;

    file1 = tempFile(1);
    doc1 = _model.getDocumentForFile(file1);
    changeDocumentText(FOO_TEXT,doc1);
    doc1.saveFileAs(new FileSelector(file1));

    file2 = tempFile(2);
    doc2 = _model.getDocumentForFile(file2);
    changeDocumentText(BAR_TEXT, doc2);

    try {
      doc2.saveFileAs(new WarningFileSelector(file1));
      fail("Did not warn of open file as expected");
    }
    catch (OpenWarningException e) {
      // good behavior for file saving...
    }
  }


  /**
   * Make sure that all open files are saved in appropriate order,
   * ie, even with BAR file as active document, save all should
   * first prompt to save FOO, then BAR.
   */
  public void testSaveAllSaveCorrectFiles()
    throws BadLocationException, IOException {
    OpenDefinitionsDocument fooDoc = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument barDoc = setupDocument(BAR_TEXT);
    OpenDefinitionsDocument trdDoc = setupDocument("third document contents");
    final File file1 = tempFile();
    final File file2 = tempFile();
    final File file3 = tempFile();
    fooDoc.getDocument().setFile(file1);
    barDoc.getDocument().setFile(file2);
    trdDoc.getDocument().setFile(file3);

    // check.
    FileSelector fs = new FileSelector(file1);

    _model.saveAllFiles(fs); // this should save the files as file1,file2,file3 respectively

    assertEquals("contents of saved file1",
                 FOO_TEXT,
                 FileOps.readFileAsString(file1));
    assertEquals("contents of saved file1",
                 BAR_TEXT,
                 FileOps.readFileAsString(file2));
    assertEquals("contents of saved file1",
                 "third document contents",
                 FileOps.readFileAsString(file3));
  }
  /**
   * Force a file to be opened with getDocumentforFile
   */
  public void testRevertFile()
    throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException
  {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!


    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        //File file = null;
        try {
          //file =
          doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not exist");
        }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        openCount++;
      }
      public void fileReverted(OpenDefinitionsDocument doc) {
        fileRevertedCount++;
      }
    };

    _model.addListener(listener);
    // Open file 1
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
    listener.assertOpenCount(1);
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);

    assertEquals("original doc unmodified",doc.isModifiedSinceSave(), false);
    changeDocumentText(BAR_TEXT, doc);
    assertEquals("doc now modified",doc.isModifiedSinceSave(), true);
    tempFile1.delete();
    try {
      doc.revertFile();
      fail("File should not be on disk.");
    }
    catch (FileMovedException fme) {
      // Revert should not take place because file is not there.
    }
    assertEquals("doc NOT reverted",doc.isModifiedSinceSave(), true);
    assertContents(BAR_TEXT, doc);
  }


  public void testModifiedByOther()
    throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException
  {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!

    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) { }
      public void fileReverted(OpenDefinitionsDocument doc) {
        fileRevertedCount++;
      }
      public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
        shouldRevertFileCount++;
        return true;
      }
    };

    _model.addListener(listener);
    // Open file 1
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
    listener.assertShouldRevertFileCount(0);
    listener.assertFileRevertedCount(0);
    assertModified(false, doc);

    doc.revertIfModifiedOnDisk();


    listener.assertShouldRevertFileCount(0);
    listener.assertFileRevertedCount(0);
    synchronized(tempFile1) {
      tempFile1.wait(2000);
    }

    String s = "THIS IS ONLY A TEST";
    FileOps.writeStringToFile(tempFile1, s);
    assertEquals("contents of saved file",
                 s,
                 FileOps.readFileAsString(tempFile1));

    tempFile1.setLastModified((new java.util.Date()).getTime());

    assertTrue("modified on disk1", doc.isModifiedOnDisk());
    boolean res = doc.revertIfModifiedOnDisk();
    assertTrue("file reverted", res);


    listener.assertShouldRevertFileCount(1);
    listener.assertFileRevertedCount(1);
    assertContents(s,doc);
  }

  public void testModifiedByOtherFalse()
    throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException
  {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!

    TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) { }

      public void fileReverted(OpenDefinitionsDocument doc) {
        fileRevertedCount++;
      }
      public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
        shouldRevertFileCount++;
        return false;
      }
    };

    _model.addListener(listener);
    // Open file 1
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
    listener.assertShouldRevertFileCount(0);
    listener.assertFileRevertedCount(0);
    assertModified(false, doc);

    doc.revertIfModifiedOnDisk();
    listener.assertShouldRevertFileCount(0);
    listener.assertFileRevertedCount(0);

    synchronized(tempFile1) {
      tempFile1.wait(2000);
    }

    String s = "THIS IS ONLY A TEST";
    FileOps.writeStringToFile(tempFile1, s);
    assertEquals("contents of saved file",
                 s,
                 FileOps.readFileAsString(tempFile1));

    assertTrue("modified on disk1", doc.isModifiedOnDisk());
    boolean reverted = doc.revertIfModifiedOnDisk();
    assertTrue("modified on disk", reverted == false);
    listener.assertShouldRevertFileCount(1);
    listener.assertFileRevertedCount(0);
    assertContents(FOO_TEXT, doc);
  }

  /**
   * Interprets some statements, saves the history, clears the history, then loads
   * the history.
   */
  public void testSaveClearAndLoadHistory() throws DocumentAdapterException,
    BadLocationException, InterruptedException, IOException
  {
    String newLine = System.getProperty("line.separator");
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        synchronized(this) {
          interactionStartCount++;
        }
      }
      public void interactionEnded() {
        synchronized(this) {
          interactionEndCount++;
          // stops threads from waiting
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    File f = tempFile();
    FileSelector fs = new FileSelector(f);
    String s1 = "int x = 5;";
    String s2 = "System.out.println(\"x = \" + x)";
    String s3 = "int y;" + newLine + "int z;";
    listener.assertInteractionStartCount(0);
    listener.assertInteractionEndCount(0);
    interpretIgnoreResult(s1);
    // wait for interpret to finish
    while (listener.interactionEndCount == 0) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    listener.assertInteractionEndCount(1);
    listener.assertInteractionStartCount(1);
    interpretIgnoreResult(s2);
    while (listener.interactionEndCount == 1) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    interpretIgnoreResult(s3);
    while (listener.interactionEndCount == 2) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    // check that the history contains the correct value
    assertEquals("History and getHistoryAsString should be the same.",
                 s1 + newLine + s2 + newLine + s3 + newLine,
                 _model.getHistoryAsString());
    String delim = History.INTERACTION_SEPARATOR + newLine;
    assertEquals("History and getHistoryAsStringWithSemicolons don't match up correctly.",
                 s1 + delim + s2 + delim + s3 + delim,
                 _model.getHistoryAsStringWithSemicolons());
    listener.assertInteractionEndCount(3);
    listener.assertInteractionStartCount(3);
    _model.saveHistory(fs);

    // check that the file contains the correct value
    assertEquals("contents of saved file",
                 History.HISTORY_FORMAT_VERSION_2 +
                 s1 + delim + s2 + delim + s3 + delim,
                 FileOps.readFileAsString(f));

    _model.clearHistory();
    // check that the history is clear
    assertEquals("History is not clear",
                 "",
                 _model.getHistoryAsString());
    _model.loadHistory(fs);
    while (listener.interactionEndCount == 3) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    // check that output of loaded history is correct
    ConsoleDocument con = _model.getConsoleDocument();
    assertEquals("Output of loaded history is not correct",
                 "x = 5",
                 con.getDocText(0, con.getDocLength()).trim());
    listener.assertInteractionStartCount(4);
    listener.assertInteractionEndCount(4);
    _model.removeListener(listener);
  }

  /**
   * Loads two history files, one whose statements end in semicolons, and one whose statements do not.
   * Makes sure that it doesn't matter.
   */
  public void testLoadHistoryWithAndWithoutSemicolons() throws BadLocationException,
    InterruptedException, IOException, DocumentAdapterException
  {
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        synchronized(this) {
          interactionStartCount++;
        }
      }
      public void interactionEnded() {
        synchronized(this) {
          interactionEndCount++;
          // stops threads from waiting
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    File f1 = tempFile(1);
    File f2 = tempFile(2);
    FileSelector fs1 = new FileSelector(f1);
    FileSelector fs2 = new FileSelector(f2);
    String s1 = "int x = 5;";
    String s2 = "System.out.println(\"x = \" + x)";
    String s3 = "x = 5;";
    String s4 = "System.out.println(\"x = \" + x)";
    FileOps.writeStringToFile(f1,s1+'\n'+s2+'\n');
    FileOps.writeStringToFile(f2,s3+'\n'+s4+'\n');

    listener.assertInteractionStartCount(0);
    _model.loadHistory(fs1);
    while (listener.interactionEndCount == 0) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    _model.loadHistory(fs2);
    while (listener.interactionEndCount < 2) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }
    // check that output of loaded history is correct
    ConsoleDocument con = _model.getConsoleDocument();
    assertEquals("Output of loaded history is not correct: " +
                 con.getDocText(0, con.getDocLength()).trim(),
                 "x = 5"+System.getProperty("line.separator")+"x = 5",
                 con.getDocText(0, con.getDocLength()).trim());
  }

  /**
   * Test for the possibility that the file has been moved or deleted
   *  since it was last referenced
   */
  public void testFileMovedWhenTriedToSave()
    throws BadLocationException, IOException {

    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    doc.saveFile(new FileSelector(file));

    TestListener listener = new TestListener();

    _model.addListener(listener);

    file.delete();
    changeDocumentText(BAR_TEXT, doc);
    try {
      doc.saveFile(new WarningFileSelector(file));
      fail("Save file should have thrown an exception");
    }
    catch (GlobalModelTestCase.FileMovedWarningException fme) {
      // this is expected to occur:
      //  WarningFileSelector throws it in shouldSaveAfterFileMoved()
    }

    assertModified(true, doc);
    assertContents(BAR_TEXT, doc);
  }

  /**
   * Tests that input can be written to and read from the console correctly.
   */
  public void testConsoleInput() throws DocumentAdapterException {
    _model.setInputListener(new InputListener() {
      int n = 0;
      public String getConsoleInput() {
        n++;
        if (n > 1) {
          throw new IllegalStateException("Input should only be requested once!");
        }
        return "input\n";
      }
    });

    String result = interpret("System.in.read()");
    String expected = /* DefaultInteractionsModel.INPUT_REQUIRED_MESSAGE + */
      String.valueOf((int)'i');
    assertEquals("read() should prompt for input and return the first byte of \"input\"",
                 expected, result);

    interpret("import java.io.*;");
    interpret("br = new BufferedReader(new InputStreamReader(System.in))");
    result = interpret("br.readLine()");
    assertEquals("readLine() should return the rest of \"input\" without prompting for input",
                 "\"nput\"", result);
  }
}
