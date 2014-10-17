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

package edu.rice.cs.drjava.model;

import java.io.*;

import java.util.List;
import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Test I/O functions of the global model.  TODO: move document observations to event thread.
  * @version $Id: GlobalModelIOTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class GlobalModelIOTest extends GlobalModelTestCase implements OptionConstants {
  
  // _log is inherited from GlobalModelTestCase
  
  /** Creates a new document, modifies it, and then does the same with a second document, checking for inteference. */
  public void testMultipleFiles() throws BadLocationException {
    assertNumOpenDocs(1);
    
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(2);
    
    // Create a second, empty document
    OpenDefinitionsDocument doc2 = _model.newFile();
    assertNumOpenDocs(3);
    assertModified(true, doc1);
    assertModified(false, doc2);
    assertContents(FOO_TEXT, doc1);
    assertLength(0, doc2);
    
    // Modify second document
    changeDocumentText(BAR_TEXT, doc2);
    assertModified(true, doc2);
    assertContents(FOO_TEXT, doc1);
    assertContents(BAR_TEXT, doc2);
    
    _log.log(this + ".testMultipleFiles() completed");
  }
  
  /** Opens several documents and ensures that the array returned by the model is correct and in the right order. */
  public void testMultipleFilesArray() throws BadLocationException {
    OpenDefinitionsDocument doc1, doc2, doc3;
    doc1 = setupDocument(FOO_TEXT);
    doc2 = setupDocument(BAR_TEXT);
    doc3 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(4);
    
    List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
    assertEquals("size of document array", 4, docs.size());
    
    assertEquals("document 1", doc1, docs.get(1));
    assertEquals("document 2", doc2, docs.get(2));
    assertEquals("document 3", doc3, docs.get(3));
    
    _log.log(this + ".testMultipleFilesArray() completed");
  }
  
  /** Ensures closing documents works correctly. */
  public void testCloseMultipleFiles() throws BadLocationException {
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    assertNumOpenDocs(2);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(3);
    
    _model.closeFile(doc1);
    assertNumOpenDocs(2);
    
    List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
    assertEquals("size of document array", 2, docs.size());
    assertContents(BAR_TEXT, docs.get(1));
    
    _model.closeFile(doc2);
    assertNumOpenDocs(1);
    docs = _model.getOpenDefinitionsDocuments();
    assertEquals("size of document array", 1, docs.size());
    
    _log.log(this + ".testCloseMultipleFiles() completed");
  }
  
  
  /** Creates a new document, modifies it, then allows it to be closed, ignoring the changes made. */
  public void testCloseFileAllowAbandon() throws BadLocationException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    
    // Try to close and check for proper events
    TestListener listener = new TestIOListener() {
      public synchronized boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
    };
    
    _model.addListener(listener);
    _model.closeFile(doc);
    listener.assertAbandonCount(1);
    listener.assertCloseCount(1);  // closed one document
    listener.assertOpenCount(0);
    
    _log.log("testCloseFileAllowAbandon completed");
  }
  
  /** Creates a new document, modifies it, but disallows a call to close it without saving changes. */
  public void testCloseFileDisallowAbandon() throws BadLocationException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    
    TestListener listener = new TestIOListener() {
      public synchronized boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }
    };
    
    _model.addListener(listener);
    _model.closeFile(doc);
    listener.assertAbandonCount(1);
    listener.assertCloseCount(0);
    listener.assertOpenCount(0);
    
    _log.log("testCloseFileDisallowAbandon completed");
  }
  
  /** Opens a file. */
  public void testOpenRealFile() throws BadLocationException, IOException {
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    
    final TestListener listener = new TestFileIOListener(tempFile); 
    
    _model.addListener(listener);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile));
          listener.assertOpenCount(1);
          listener.assertCloseCount(1);  // Untitled document is closed when doc is opened
          assertModified(false, doc);
          assertContents(BAR_TEXT, doc);
        }
        catch(AlreadyOpenException aoe) {
          // Should not be open
          fail("File was already open!");
        }
        catch(OperationCanceledException oce) {
          // Should not be canceled
          fail("Open was unexpectedly canceled!");
        }
        catch(Exception e) { 
          // Should never happen
          fail("Exception thrown in testOpenRealFile.  Traceback: " + e);
        }
      }
    });
    
    _log.log("testOpenRealFile completed");
  }
  
  /** Initiates a file open, but cancels. */
  public void testCancelOpenFile() throws BadLocationException, IOException {
    
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    assertNumOpenDocs(2);
    
    final TestListener listener = new TestIOListener() {
      public synchronized boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
    };
    
    _model.addListener(listener);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          //OpenDefinitionsDocument newDoc =
          _model.openFile(new CancelingSelector());
        }
        catch(AlreadyOpenException aoe) {
          // Should not be open
          fail("File was already open!");
        }
        catch(OperationCanceledException oce) {
          // we expect this to be thrown
        }
        catch(Exception e) {  // should never happen
          throw new UnexpectedException(e);  
        }
        finally {
          assertNumOpenDocs(2);
          listener.assertOpenCount(0);
          listener.assertCloseCount(0);
          
          List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
          OpenDefinitionsDocument doc = docs.get(1);
          assertModified(true, doc);
          try { assertContents(FOO_TEXT, doc); }
          catch(BadLocationException e) { 
            fail("BadLocation in assertContents test. Traceback: " + e);
          }
        }
      }
    });
    
    _log.log("testCancelOpenFile completed");
  }
  
  /** Attempts to open a non-existent file. */
  public void testOpenNonexistentFile() throws IOException {
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
    
    assertEquals("doc file should be non-existent", doc, null);
    
    _log.log("testOpenNonexistentFile completed");
  }
  
  /** Attempts to reopen an already open file. */
  public void testReopenFile() throws BadLocationException, IOException {
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    
    final TestListener listener = new TestFileIOListener(tempFile);
    
    _model.addListener(listener);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile));
          listener.assertOpenCount(1);
          listener.assertCloseCount(1);  //  Untitled document closed when doc is opened
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
        catch (Exception e) {
          // Should not happen
          fail("Exception thrown in testReopenFile().  Traceback: " + e);
        }
      }
    });
    
    // Now reopen
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          //OpenDefinitionsDocument doc2 =
          _model.openFile(new FileSelector(tempFile));
          fail("file should already be open");
        }
        catch (AlreadyOpenException aoe) {
          // Should not be opened
          listener.assertOpenCount(1);
          listener.assertCloseCount(1);  
        }
        catch (OperationCanceledException oce) {
          // Should not be canceled
          fail("Open was unexpectedly canceled!");
        }
        catch (Exception e) {
          // Should not happen
          fail("Exception thrown in testReopenFile().  Traceback: " + e);
        }
      }
    });
    
    // Now reopen same file with a different path
    //  eg. /tmp/MyFile -> /tmp/./MyFile
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
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
        catch (Exception e) {
          // Should not happen
          fail("Exception thrown in testReopenFile().  Traceback: " + e);
        }
      }
    });
    _log.log("testReopenFile completed");
  }
  
  /** Opens multiple files. */
  public void testOpenMultipleFiles() throws BadLocationException, IOException {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    final File tempFile2 = writeToNewTempFile(BAR_TEXT);
    
    TestListener listener = new TestIOListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        super.fileOpened(doc);
        File file = FileOps.NULL_FILE;
        try { file = doc.getFile(); }
        catch (FileMovedException fme) { fail("file does not exist"); } // We know file should exist
        if (tempFile1.equals(file))
          assertEquals("file to open", IOUtil.attemptCanonicalFile(tempFile1), IOUtil.attemptCanonicalFile(file));
        else assertEquals("file to open", IOUtil.attemptCanonicalFile(tempFile2), IOUtil.attemptCanonicalFile(file));
      }
    };
    
    _model.addListener(listener);
    try {
      OpenDefinitionsDocument[] docs = _model.openFiles(new FileSelector(tempFile1, tempFile2));
      listener.assertOpenCount(2);
      listener.assertCloseCount(1);  // closed Untitled document
      assertEquals("Number of docs returned", docs.length, 2);
      assertModified(false, docs[0]);
      assertContents(FOO_TEXT, docs[0]);
      assertModified(false, docs[1]);
      assertContents(BAR_TEXT, docs[1]);
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
    List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
    assertEquals("size of document array", 2, docs.size());
    assertContents(FOO_TEXT, docs.get(0));
    assertContents(BAR_TEXT, docs.get(1));
    
    _log.log("testOpenMultipleFiles completed");
  }
  
  /** Initiates a file open, but cancels. */
  public void testCancelOpenMultipleFiles() throws BadLocationException, IOException {
    
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(3);
    
    final TestListener listener = new TestIOListener() {
      public synchronized boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
    };
    
    _model.addListener(listener);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
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
            assertNumOpenDocs(3);
            listener.assertOpenCount(0);
            listener.assertCloseCount(0);
            
            List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
            OpenDefinitionsDocument newDoc1 = docs.get(1);
            assertModified(true, newDoc1);
            assertContents(FOO_TEXT, newDoc1);
            
            OpenDefinitionsDocument newDoc2 = docs.get(2);
            assertModified(true, newDoc2);
            assertContents(BAR_TEXT, newDoc2);
          }
        }
        catch (Exception e) {
          // should never happen
          fail("Exception thrown in testCancelOpenMultipleFiles.  Traceback: \n" + e);
        }
      }
    });
    
    _log.log("testCancelOpenMultipleFiles completed");
  }
  
  /** Attempts to open a non-existent file. */
  public void testOpenMultipleNonexistentFiles() throws IOException {
    
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    
    //TestListener listener = new TestListener();
    TestListener listener = new TestFileIOListener(tempFile1) {
      public synchronized void filesNotFound(File... f) { fileNotFoundCount++; }
    };
    
    _model.addListener(listener);
    
    OpenDefinitionsDocument[] docs = null;
    try { docs = _model.openFiles(new FileSelector(tempFile1, new File("fake-file"))); }
    catch (FileNotFoundException fnf) { fail("FileNotFound exception was not thrown!"); }  // Should not have moved
    catch (AlreadyOpenException aoe) { fail("File was already open!"); }                   // Should not be open
    catch (OperationCanceledException oce) { fail("Open was unexpectedly canceled!"); }    // Should not be canceled
    assertTrue("one file was opened", docs != null && docs.length == 1);
    listener.assertOpenCount(1);
    listener.assertCloseCount(1);  // closed Untitled document
    listener.assertFileNotFoundCount(1);
    
    _log.log("testOpenMultipleNonexistentFiles completed");
  }
  
  /** Error checking for openening multiple files checks for null and an array w/null. */
  public void testOpenMultipleFilesError() {
    
    OpenDefinitionsDocument[] docs = null;
    //final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    
    try {
      docs = _model.openFiles(new FileOpenSelector() {
        public File[] getFiles() { return new File[] {null}; }
      });
      fail("IO exception was not thrown!");
    }
    catch (IOException e) {  /* As we expected, the file was not found */ }
    catch (Exception e) { fail("Unexpectedly exception caught!"); }
    
    try {
      docs = _model.openFiles(new FileOpenSelector() {
        public File[] getFiles() { return null; }
      });
      
      fail("IO exception was not thrown!");
    }
    catch (IOException e) { /* As we expected, the file was not found. */ }
    catch (Exception e) { fail("Unexpectedly exception caught!"); }
    
    assertEquals("no doc files should be open", null, docs);
    
    _log.log("testOpenMultipleFilesError completed");
  }
  
  /** Force a file to be opened with getDocumentforFile. */
  public void testForceFileOpen() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException {
    
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    final File tempFile2 = writeToNewTempFile(BAR_TEXT);
    // don't catch and fail!
    
    final TestListener listener = new TestIOListener();
    
    _model.addListener(listener);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {    
          // Open file 1
          OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
          listener.assertOpenCount(1);
          listener.assertCloseCount(1);  // closed Untitled document
          assertModified(false, doc);
          assertContents(FOO_TEXT, doc);
          
          // Get file 1
          OpenDefinitionsDocument doc1 = _model.getDocumentForFile(tempFile1);
          listener.assertOpenCount(1);
          listener.assertCloseCount(1);   // closed Untitled document
          assertEquals("opened document", doc, doc1);
          assertContents(FOO_TEXT, doc1);
          
          // Get file 2, forcing it to be opened
          OpenDefinitionsDocument doc2 = _model.getDocumentForFile(tempFile2);
          listener.assertOpenCount(2);
          listener.assertCloseCount(1);  // closed Untitled document
          assertContents(BAR_TEXT, doc2);
        }
        catch (Exception e) {
          // should never happen
          fail("Exception thrown in testForceFileOpen. Traceback: \n" + e);
        }
      }
    });
  }
  
  /** Attempts to make the first save of a document, but cancels instead. */
  public void testCancelFirstSave() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    
    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());
    
//    boolean saved = 
      saveFile(doc, new CancelingSelector());
//    assertTrue("doc should not have been saved", ! saved);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
    
    _log.log("testForceFileOpen completed");
  }
  
  /** Makes a first save of the current document. */
  public void testRealSaveFirstSave() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) { fail("file does not exist"); }   // We know file should exist
        try {
          assertEquals("saved file name", file.getCanonicalFile(), f.getCanonicalFile());
          synchronized(this) { saveCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
    };
    
    _model.addListener(listener);
    saveFile(doc, new FileSelector(file));
    listener.assertSaveCount(1);
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    _log.log("testRealSaveFirstSave completed");
  }
  
  /** Makes a first save-copy of the current document, ensures that it's still modified. */
  public void testRealSaveFirstSaveCopy() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) { fail("file does not exist"); }   // We know file should exist
        try {
          assertEquals("saved file name", file.getCanonicalFile(), f.getCanonicalFile());
          synchronized(this) { saveCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
    };
    
    _model.addListener(listener);
    saveFileCopy(doc, new SaveCopyFileSelector(file));
    listener.assertSaveCount(0); // not "saved" because it doesn't change the state
    assertModified(true, doc); // still modified
    assertContents(FOO_TEXT, doc);
    
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    _log.log("testRealSaveFirstSaveCopy completed");
  }
  
  /** Saves a file already saved and overwrites its contents. */
  public void testSaveAlreadySaved() throws Exception {
    //disable file backups, remember original setting
    Boolean backupStatus = DrJava.getConfig().getSetting(BACKUP_FILES);
    DrJava.getConfig().setSetting(BACKUP_FILES, Boolean.FALSE);
    Utilities.clearEventQueue();  // config changes rely on the event thread
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    // No listeners here -- other tests ensure the first save works
    assertFalse("Confirm that backup status is initially false", DrJava.getConfig().getSetting(BACKUP_FILES));
    saveFile(doc, new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    // Listener to use on future saves
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) { 
//          System.err.println("File " + f + " to be saved DOES NOT EXIST");
          fail("file does not exist"); // We know file should exist
        }   
        try {
          assertEquals("saved file", file.getCanonicalFile(), f.getCanonicalFile());
          synchronized(this) { saveCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
    };
    
    final File backup = new File(file.getPath() + "~");
//    System.err.println("fileName = " + file);
//    System.err.println("backupName = " + backup);
    backup.delete();
    
    assertFalse("Confirm that backup has been deleted if it already existed", backup.exists());
    
    _model.addListener(listener);
    
    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);
    Utilities.clearEventQueue();
    
//    System.err.println("Document text = '" + doc.getText() + "'");
    
    // Save over top of the previous file
    saveFile(doc, new FileSelector(file)); 
    
//    Utilities.clearEventQueue();
    listener.assertSaveCount(1);
    assertEquals("Contents of saved file 2nd write", BAR_TEXT, IOUtil.toString(file));
    assertFalse("No backup was made", backup.exists());
//    System.err.println("Confirm that " + backup + " does not exist: " + backup.exists());
    //enable file backups
    
    DrJava.getConfig().setSetting(BACKUP_FILES, Boolean.TRUE);
    Utilities.clearEventQueue();
    
    // Muck up the document
    changeDocumentText(FOO_TEXT, doc);

//    System.err.println("Backup status = " + DrJava.getConfig().getSetting(BACKUP_FILES) + " for backup file " + backup);
//    System.err.println("Before saving to '" + file + "', confirm " + backup + " does not exist: " + backup.exists());
    
    assertTrue("Confirm that BACKUP_FILES is true", DrJava.getConfig().getSetting(BACKUP_FILES));
    assertFalse("Confirm that backup file " + backup + " does not yet exist", backup.exists());
    assertEquals("Confirm that file " + file + " was modified properly", BAR_TEXT, IOUtil.toString(file));
//    System.err.println("Old contents of file " + file + " = '" + IOUtil.toString(file) + "'");
     // Save over top of the previous file
    saveFile(doc, new FileSelector(file));
    Utilities.clearEventQueue();
    
    assertTrue("Confirm that backup file " + backup + " was created", backup.exists());
    
//    System.err.println("After saving, confirm " + backup + " exists: " + backup.exists());
//    System.err.println("Backup has contents '" + IOUtil.toString(backup) + "'");
//    System.err.println("New file has contents '" + IOUtil.toString(file) + "'");

    listener.assertSaveCount(2);
//    System.err.println("After checking save count, confirm " + backup + " exists: " + backup.exists());
    assertEquals("contents of saved file 3rd write", FOO_TEXT, IOUtil.toString(file));
    assertEquals("contents of backup file 3rd write", BAR_TEXT, IOUtil.toString(backup));
    
    /* Set the config back to the original option */
    DrJava.getConfig().setSetting(BACKUP_FILES, backupStatus);
    
    _log.log("testSaveAlreadySaved completed");
  }
  
  /** Saves the document with FOO_TEXT and then saves over the old text, passing in a CancelingSelector
    * to cancel if we are asked for a new file name.  Confirms that no cancellation happens (since the
    * file is already saved.
    */
  public void testCancelSaveAlreadySaved() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    // No listeners here -- other tests ensure the first save works
    saveFile(doc, new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) { fail("file does not exist");  /* We know file should exist */ }
        try {
          assertEquals("saved file", file.getCanonicalFile(), f.getCanonicalFile());
          synchronized(this) { saveCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
    };
    
    _model.addListener(listener);
    
    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);
    
    saveFile(doc, new CancelingSelector());
    
    // The file should have saved on top of the old text anyhow.
    // The canceling selector should never have been called.
    listener.assertSaveCount(1);
    assertModified(false, doc);
    assertContents(BAR_TEXT, doc);
    
    assertEquals("contents of saved file", BAR_TEXT, IOUtil.toString(file));
    
    _log.log("testCancelSaveAlreadySaved completed");
  }
  
  /** Make sure that saveAs doesn't save if we cancel! */
  public void testCancelSaveAsAlreadySaved() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    // No listeners here -- other tests ensure the first save works
    saveFile(doc, new FileSelector(file));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    // No events better be fired!
    _model.addListener(new TestListener());
    
    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);
    
    saveFileAs(doc, new CancelingSelector());
    
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
    
    _log.log("testCancelSaveAsAlreadySaved completed");
  }
  
  /** Ensures that saveAs saves to a different file. */
  public void testSaveAsAlreadySaved() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file1 = tempFile();
    final File file2 = tempFile();
    
    // No listeners here -- other tests ensure the first save works
    saveFile(doc, new FileSelector(file1));
    assertModified(false, doc);
    assertContents(FOO_TEXT, doc);
    assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file1));
    
    // Make sure we save now to the new file name
    TestListener listener = new TestListener() {
      public void fileSaved(OpenDefinitionsDocument doc) {
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) { fail("file does not exist");   /* We know file should exist */ }
        try {
          assertEquals("saved file", file2.getCanonicalFile(), f.getCanonicalFile());
          synchronized(this) { saveCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
    };
    
    _model.addListener(listener);
    
    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);
    
    saveFileAs(doc, new FileSelector(file2));
    
    assertEquals("contents of saved file1", FOO_TEXT, IOUtil.toString(file1));
    
    assertEquals("contents of saved file2", BAR_TEXT, IOUtil.toString(file2));
    
    _log.log("testSaveAsAlreadySaved completed");
  }
  
  public void testSaveAsExistsForOverwrite() throws BadLocationException, IOException {
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file1 = tempFile();
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try { 
          doc.saveFileAs(new WarningFileSelector(file1));
          fail("Did not warn of open file as expected");
        }
        catch (Exception e) { /* Good behavior for file saving ... */ }
      }
    });
    
    _log.log("testSaveAsExistsForOverwrite completed");
  }
  
  public void testSaveAsExistsAndOpen() throws BadLocationException, IOException {
    
    final File file1 = tempFile(1);
        
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try {
          OpenDefinitionsDocument doc1 = _model.getDocumentForFile(file1);
          changeDocumentText(FOO_TEXT,doc1);
          saveFileAs(doc1, new FileSelector(file1));
          
          File file2 = tempFile(2);
          OpenDefinitionsDocument doc2 = _model.getDocumentForFile(file2);
          changeDocumentText(BAR_TEXT, doc2);
          
          try { 
            doc2.saveFileAs(new WarningFileSelector(file1));
            fail("Did not warn of open file as expected");
          }
          catch (OpenWarningException e) { /* Good behavior for file saving ... */ }
        }
        catch (Exception e) {
          // should never happen
          fail("Exception thrown in testSaveAsExistsAndOpen.  Traceback: \n" + e);
        }
      }
    });
    
    _log.log("testSaveAsExistsAndOpen completed");
  }
  
  
  /** Ensures that all open files are saved in appropriate order, i.e., even with BAR file as active document, save all
    * should first prompt to save FOO, then BAR.
    */
  public void testSaveAllSaveCorrectFiles()
    throws BadLocationException, IOException {
    OpenDefinitionsDocument fooDoc = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument barDoc = setupDocument(BAR_TEXT);
    OpenDefinitionsDocument trdDoc = setupDocument("third document contents");
    final File file1 = tempFile();
    final File file2 = tempFile();
    final File file3 = tempFile();
    fooDoc.setFile(file1);
    barDoc.setFile(file2);
    trdDoc.setFile(file3);
    
    // None of these documents has been entered in the _documentsRepos
    
    // check.
    final FileSelector fs = new FileSelector(file1);
    
    saveAllFiles(_model, fs);
    
    assertEquals("contents of saved file1", FOO_TEXT, IOUtil.toString(file1));
    assertEquals("contents of saved file2", BAR_TEXT, IOUtil.toString(file2));
    assertEquals("contents of saved file3", "third document contents", IOUtil.toString(file3));
    
    _log.log("testSaveAllSaveCorrectFiles completed");
  }
  
  /** Forces a file to be opened with getDocumentforFile. */
  public void testRevertFile() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException {
    
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!
    
    TestListener listener = new TestIOListener();
    
    _model.addListener(listener);
    // Open file 1
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile1));
    listener.assertOpenCount(1);
    listener.assertCloseCount(1);  // closed Untitled document
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
    
    _log.log("testRevertFile completed");
  }
  
  
  public void testModifiedByOther() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException {
    
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!
    
    TestListener listener = new TestIOListener() {
      public synchronized void fileReverted(OpenDefinitionsDocument doc) { fileRevertedCount++; }
      public synchronized boolean shouldRevertFile(OpenDefinitionsDocument doc) {
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
    synchronized(tempFile1) { tempFile1.wait(2000); }
    
    String s = "THIS IS ONLY A TEST";
    IOUtil.writeStringToFile(tempFile1, s);
    assertEquals("contents of saved file", s, IOUtil.toString(tempFile1));
    
    tempFile1.setLastModified((new java.util.Date()).getTime());
    
    assertTrue("modified on disk1", doc.modifiedOnDisk());
    boolean res = doc.revertIfModifiedOnDisk();
    assertTrue("file reverted", res);
    
    
    listener.assertShouldRevertFileCount(1);
    listener.assertFileRevertedCount(1);
    assertContents(s,doc);
    
    _log.log("testModifiedByOther completed");
  }
  
  public void testModifiedByOtherFalse() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException {
    final File tempFile1 = writeToNewTempFile(FOO_TEXT);
    // don't catch and fail!
    
    final TestListener listener = new TestIOListener() {
      public synchronized void fileReverted(OpenDefinitionsDocument doc) { fileRevertedCount++; }
      public synchronized boolean shouldRevertFile(OpenDefinitionsDocument doc) {
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
    
    synchronized(tempFile1) { tempFile1.wait(2000); }
    
    String s = "THIS IS ONLY A TEST";
    IOUtil.writeStringToFile(tempFile1, s);
    assertEquals("contents of saved file", s, IOUtil.toString(tempFile1));
    
    assertTrue("modified on disk1", doc.modifiedOnDisk());
    boolean reverted = doc.revertIfModifiedOnDisk();
    assertTrue("modified on disk", reverted == false);
    listener.assertShouldRevertFileCount(1);
    listener.assertFileRevertedCount(0);
    assertContents(FOO_TEXT, doc);
    
    
    _log.log("testModifiedByOtherFalse completed");
  }
  
  /** Interprets some statements, saves the history, clears the history, then loads the history. */
  public void testSaveClearAndLoadHistory() throws EditDocumentException, IOException, InterruptedException {
    String newLine = StringOps.EOL;
    final InteractionListener listener = new InteractionListener();
    
    _model.addListener(listener);
    File f = tempFile();
    FileSelector fs = new FileSelector(f);
    String s1 = "val x = 5";
    String s2 = "println(\"x = \" + x);";
    String s3 = "val y = 12" + newLine + "val z = 7";
    listener.assertInteractionStartCount(0);
    listener.assertInteractionEndCount(0);
    
    interpretIgnoreResult(s1);
    listener.waitInteractionDone();
    
    listener.assertInteractionEndCount(1);
    listener.assertInteractionStartCount(1);
    
    listener.logInteractionStart();
    interpretIgnoreResult(s2);
    listener.waitInteractionDone();
    
    listener.logInteractionStart();
    interpretIgnoreResult(s3);
    listener.waitInteractionDone();
    
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
    safeSaveHistory(fs);
    
    // check that the file contains the correct value
    assertEquals("contents of saved file", History.HISTORY_FORMAT_VERSION_2 + s1 + delim + s2 + delim + s3 + delim,
                 IOUtil.toString(f));
    
    _model.clearHistory();
    // confirm that the history is clear
    assertEquals("History is not clear", "", _model.getHistoryAsString());
    
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
        _model.resetInteractions(_model.getWorkingDirectory());
        _model.resetConsole();
      }
    });
    listener.waitResetDone();
    
    listener.logInteractionStart();
    safeLoadHistory(fs);
    listener.waitInteractionDone();
        
    // check that output of loaded history is correct
    ConsoleDocument con = _model.getConsoleDocument();
    debug.log(con.getDocText(0, con.getLength()).trim());
//    System.out.println("Output text is '" + con.getDocText(0, con.getLength()).trim() + "'");
    assertEquals("Output of loaded history is not correct", "x = 5", con.getDocText(0, con.getLength()).trim());
    listener.assertInteractionStartCount(4);
    listener.assertInteractionEndCount(4);
    _model.removeListener(listener);

    _log.log("testSaveClearAndLoadHistory completed");
  }
  
  /** Loads two history files, one whose statements end in semicolons, and one whose statements do not.
    * Makes sure that it doesn't matter.
    */
  public void testLoadHistoryWithAndWithoutSemicolons() throws IOException, EditDocumentException, 
    InterruptedException {
    
    final InteractionListener listener = new InteractionListener();
    _model.addListener(listener);
    File f1 = tempFile(1);
    File f2 = tempFile(2);
    FileSelector fs1 = new FileSelector(f1);
    FileSelector fs2 = new FileSelector(f2);
    String s1 = "val x = 5";
    String s2 = "println(\"x = \" + x);";
    String s3 = "val x = 5;";
    String s4 = "println(\"x = \" + x)";
    IOUtil.writeStringToFile(f1, s1 + '\n' + s2 + '\n');
    IOUtil.writeStringToFile(f2, s3 + '\n' + s4 + '\n');
    
    listener.assertInteractionStartCount(0);
    safeLoadHistory(fs1);
//    System.err.println("fs1[" + fs1 + "]");
    listener.waitInteractionDone();
    
    listener.logInteractionStart();
    safeLoadHistory(fs2);
//    System.err.println("fs2[" + fs2 + "]");
    listener.waitInteractionDone();
    
    // check that output of loaded history is correct
    ConsoleDocument con = _model.getConsoleDocument();
    System.err.println("con = \n" + con + "\n***End of Console***");
                       
    assertEquals("Output of loaded history is not correct: " + con.getDocText(0, con.getLength()).trim(),
                 "x = 5" + StringOps.EOL + "x = 5",
                 con.getDocText(0, con.getLength()).trim());

    _log.log("testLoadHistoryWithAndWithoutSemicolons completed");
  }
  
  /** Test for the possibility that the file has been moved or deleted
    * since it was last referenced
    */
  public void testFileMovedWhenTriedToSave() throws BadLocationException, IOException {
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    saveFile(doc, new FileSelector(file));
    
    TestListener listener = new TestListener();
    
    _model.addListener(listener);
    
    file.delete();
    changeDocumentText(BAR_TEXT, doc);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try { 
          doc.saveFile(new WarningFileSelector(file));
          fail("Did not warn of open file as expected");
        }
        catch (Exception e) { /* Good behavior for file saving ... */ }
      }
    });

    assertModified(true, doc);
    assertContents(BAR_TEXT, doc);
   
    _log.log("testFileMovedWhenTriedToSave completed");
  }
  
  /** Tests that input can be written to and read from the console correctly. */
  public void testConsoleInput() throws EditDocumentException {
    _model.getInteractionsModel().setInputListener(new InputListener() {
      int n = 0;
      public String getConsoleInput() {
        n++;
        if (n > 1) throw new IllegalStateException("Input should only be requested once!");
        return "input\n";  // '\n' is used becuae this input is generated by Swing processing of keystrokes
      }
    });
    
    String result = interpret("val z = System.in.read()");
    String expected = "z: Int = " + String.valueOf((int)'i');
    assertEquals("read() should prompt for input and return the first byte of \"input\"", expected, result);
    
    interpret("import java.io._");
    interpret("val br = new BufferedReader(new InputStreamReader(System.in))");
    result = interpret("val text = br.readLine()");
    assertEquals("readLine() should return the rest of \"input\" without prompting for input",
                 "text: java.lang.String = nput", result);
    
    
    _log.log("testConsoleInput completed");
  }
  
  class TestIOListener extends TestListener {
    public synchronized void fileOpened(OpenDefinitionsDocument doc) {  openCount++; } 
    public synchronized void fileClosed(OpenDefinitionsDocument doc) { closeCount++; } 
  }
  
  class TestFileIOListener extends TestIOListener {
    File _expected;
    TestFileIOListener(File f) { _expected = f; }
    public void fileOpened(OpenDefinitionsDocument doc) {
//      System.err.println("TestIOListener.fileOpened called.  openCount = " + openCount);
      super.fileOpened(doc);
//      System.err.println("After super.fileOpened called, openCount = " + openCount);
      File file = FileOps.NULL_FILE;
      try { file = doc.getFile(); }
      catch (FileMovedException fme) { fail("file does not exist"); }     // We know file should exist
      assertEquals("file to open", IOUtil.attemptCanonicalFile(_expected), IOUtil.attemptCanonicalFile(file));
    }
  }
}
