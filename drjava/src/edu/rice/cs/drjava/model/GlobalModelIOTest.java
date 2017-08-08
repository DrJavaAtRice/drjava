/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Test I/O functions of the global model.  TODO: move document observations to event thread.
  * @version $Id: GlobalModelIOTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class GlobalModelIOTest extends GlobalModelTestCase implements OptionConstants {
  
  // _log normally is inherited from GlobalModelTestCase
  public static final Log _log  = new Log("GlobalModel.txt", false);
 
  /** Creates a new document, modifies it, and then does the same with a second document, checking for inteference. */
  public void MultipleFiles() throws BadLocationException {
    
    _log.log("+++Starting testMultipleFiles()");
        
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
    
    _log.log("+++Completing testMultipleFiles()");
  }
  
  /** Opens several documents and ensures that the array returned by the model is correct and in the right order. */
  public void xtestMultipleFilesArray() throws BadLocationException {
    
    _log.log("+++Starting testMultipleFilesArray");

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
    
    _log.log("+++Completing testMultipleFilesArray");
    Utilities.clearEventQueue();
  }
  
  /** Ensures closing documents works correctly. */
  public void xtestCloseMultipleFiles() throws BadLocationException {
    
    _log.log("+++Starting testCloseMultipleFiles()");
        
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
    
    _log.log("+++Completing testCloseMultipleFiles()");
  }
  
  
  /** Creates a new document, modifies it, then allows it to be closed, ignoring the changes made. */
  public void xtestCloseFileAllowAbandon() throws BadLocationException {
    
    _log.log("+++Starting testCloseFileDisallowAbandon");
    
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
    
    _log.log("+++Completing testCloseFileAllowAbandon");
  }
  
  /** Creates a new document, modifies it, but disallows a call to close it without saving changes. */
  public void xtestCloseFileDisallowAbandon() throws BadLocationException {
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
    
    _log.log("+++Completing testCloseFileDisallowAbandon");
  }
  
  /** Opens a file. */
  public void xtestOpenRealFile() throws BadLocationException, IOException {
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final TestListener listener = new TestFileIOListener(tempFile);
    
    _log.log("+++Starting testOpenRealFile");
    
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
    
    _log.log("+++Completing testOpenRealFile");
  }
  
  /** Initiates a file open, but cancels. */
  public void xtestCancelOpenFile() throws BadLocationException, IOException {
    
    _log.log("+++Starting testCancelOpenFile");
    
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
    
    _log.log("+++Completing testCancelOpenFile");
  }
  
  /** Attempts to open a non-existent file. */
  public void xtestOpenNonexistentFile() throws IOException {
    
    _log.log("+++Starting testOpenNonexistentFile");
    
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
    
    _log.log("+++Completing testOpenNonexistentFile");
  }
  
  /** Attempts to reopen an already open file. */
  public void xtestReopenFile() throws BadLocationException, IOException {
  
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final TestListener listener = new TestFileIOListener(tempFile);
    
    _log.log("+++Starting testReopenFile");
    
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
    _log.log("+++Completing testReopenFile");
  }
  
  /** Opens multiple files. */
  public void xtestOpenMultipleFiles() throws BadLocationException, IOException {
    
    _log.log("+++Starting testOpenMultipleFiles");
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
    
    _log.log("+++Completing testOpenMultipleFiles");
  }
  
  /** Initiates a file open, but cancels. */
  public void xtestCancelOpenMultipleFiles() throws BadLocationException, IOException {
    
    _log.log("+++Starting CancelMultipleOpenFiles");
    
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
    
    _log.log("+++Completing testCancelOpenMultipleFiles");
  }
  
  /** Attempts to open a non-existent file. */
  public void xtestOpenMultipleNonexistentFiles() throws IOException {
    
    _log.log("+++Starting testOpenMultipleNonexistentFiles");
    
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
    
    _log.log("+++Completing testOpenMultipleNonexistentFiles");
  }
  
  /** Error checking for openening multiple files checks for null and an array w/null. */
  public void xtestOpenMultipleFilesError() {
    
    _log.log("+++Starting testOpenMultipleFilesError");
    
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
    
    _log.log("+++Completed OpenMultipleFilesError");
  }
  
  /** Force a file to be opened with getDocumentforFile. */
  public void xtestForceFileOpen() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException {
    
       _log.log("+++Starting testForceFileOpen");
       
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
    _log.log("+++Completing testForceFileOpen");
  }
  
  /** Attempts to make the first save of a document, but cancels instead. */
  public void xtestCancelFirstSave() throws BadLocationException, IOException {
    
    _log.log("+++Starting testCancelFirstSave");
 
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    
    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());
    
//    boolean saved = 
    saveFile(doc, new CancelingSelector());
//    assertTrue("doc should not have been saved", ! saved);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
    
    _log.log("+++Completing testCancelFirstSave");
  }
  
  /** Makes a first save of the current document. */
  public void xtestRealSaveFirstSave() throws BadLocationException, IOException {
    
    _log.log("+++Starting testRealSaveFirstSave");
    
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
    
    _log.log("+++Completing testRealSaveFirstSave");
  }
  
  /** Makes a first save-copy of the current document, ensures that it's still modified. */
  public void xtestRealSaveFirstSaveCopy() throws BadLocationException, IOException {
    _log.log("+++Starting testRealSaveFirstSaveCopy");
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
    
    _log.log("+++Completing testRealSaveFirstSaveCopy");
  }
  
  /** Saves a file already saved and overwrites its contents. */
  public void xtestSaveAlreadySavedAndOverwrite() throws Exception {
    _log.log("+++Starting testSaveAlreadySavedAndOverwrite");
    
    //disable file backups, remember original setting
    Boolean backupStatus = DrScala.getConfig().getSetting(BACKUP_FILES);
    DrScala.getConfig().setSetting(BACKUP_FILES, Boolean.FALSE);
    Utilities.clearEventQueue();  // config changes rely on the event thread
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    // No listeners here -- other tests ensure the first save works
    assertFalse("Confirm that backup status is initially false", DrScala.getConfig().getSetting(BACKUP_FILES));
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
    _log.log("fileName = " + file + " backupName = " + backup);
    backup.delete();
    
    assertFalse("Confirm that backup has been deleted if it already existed", backup.exists());
    
    _model.addListener(listener);
    
    // Muck up the document
    changeDocumentText(BAR_TEXT, doc);
    Utilities.clearEventQueue();
    
    _log.log("Document text = '" + doc.getText() + "'");
    
    // Save over top of the previous file
    saveFile(doc, new FileSelector(file)); 
    
//    Utilities.clearEventQueue();
    listener.assertSaveCount(1);
    assertEquals("Contents of saved file 2nd write", BAR_TEXT, IOUtil.toString(file));
    assertFalse("No backup was made", backup.exists());
    _log.log("Confirm that " + backup + " does not exist: " + backup.exists());
    //enable file backups
    
    DrScala.getConfig().setSetting(BACKUP_FILES, Boolean.TRUE);
    Utilities.clearEventQueue();
    
    // Muck up the document
    changeDocumentText(FOO_TEXT, doc);

    _log.log("Backup status = " + DrScala.getConfig().getSetting(BACKUP_FILES) + " for backup file " + backup + 
             " backup.exists() = " +  backup.exists());
    
    assertTrue("Confirm that BACKUP_FILES is true", DrScala.getConfig().getSetting(BACKUP_FILES));
    assertFalse("Confirm that backup file " + backup + " does not yet exist", backup.exists());
    assertEquals("Confirm that file " + file + " was modified properly", BAR_TEXT, IOUtil.toString(file));
    _log.log("Old contents of file " + file + " = '" + IOUtil.toString(file) + "'");
    
     // Save over top of the previous file
    saveFile(doc, new FileSelector(file));
    Utilities.clearEventQueue();
    
    assertTrue("Confirm that backup file " + backup + " was created", backup.exists());
    
    _log.log("After saving, confirm " + backup + " exists: " + backup.exists());
    _log.log("Backup has contents '" + IOUtil.toString(backup) + "'");
    _log.log("New file has contents '" + IOUtil.toString(file) + "'");

    listener.assertSaveCount(2);
    _log.log("After checking save count, confirm " + backup + " exists: " + backup.exists());
    assertEquals("contents of saved file 3rd write", FOO_TEXT, IOUtil.toString(file));
    assertEquals("contents of backup file 3rd write", BAR_TEXT, IOUtil.toString(backup));
    
    /* Set the config back to the original option */
    DrScala.getConfig().setSetting(BACKUP_FILES, backupStatus);
    
    _log.log("+++Completing testSaveAlreadySavedAndOverwrite");
  }
  
  /** Saves the document with FOO_TEXT and then saves over the old text, passing in a CancelingSelector
    * to cancel if we are asked for a new file name.  Confirms that no cancellation happens (since the
    * file is already saved.
    */
  public void xtestSaveAlreadySaved() throws BadLocationException, IOException {
    
    _log.log("+++Starting testCancelSaveAlreadySaved");
    
    {/* Bracket former testSaveAlreadySaved */
      
      OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
      final File file = tempFile();
      
      // No listeners here -- other tests ensure the first save works
      saveFile(doc, new FileSelector(file));
      assertModified(false, doc);
      _log.log("original file saved");
      assertContents(FOO_TEXT, doc);
      assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(file));
      
      _log.log("Creating listener");
      
      TestListener listener = new TestListener() {
        public void fileSaved(OpenDefinitionsDocument doc) {
          File f = null;
          try { f = doc.getFile(); }
          catch (FileMovedException fme) { fail("file does not exist");  /* We know file should exist */ }
          try {
            assertEquals("saved file", file.getCanonicalFile(), f.getCanonicalFile());
//            System.err.println("Saved file is same as original");
            synchronized(this) { saveCount++; }
          }
          catch (IOException ioe) { fail("could not get canonical file"); }
        }
      };
      
      _model.addListener(listener);
      
//      System.err.println("Listener created");
      
      // Muck up the document
      changeDocumentText(BAR_TEXT, doc);
      
//      System.err.println("Document changed");
      
      saveFile(doc, new CancelingSelector());
      
//      System.err.println("saveFile(...) is executed");
      
      // The file should have saved on top of the old text anyhow.
      // The canceling selector should never have been called.
      listener.assertSaveCount(1);
      assertModified(false, doc);
      assertContents(BAR_TEXT, doc);
      
      assertEquals("contents of saved file", BAR_TEXT, IOUtil.toString(file));
      
      _log.log("+++Completing testCancelSaveAlreadySaved");
    }
  }
      
//    /* Consolidation of testCancelSaveAsAlreadySaved */
  
  /** Make sure that saveAs doesn't save if we cancel! */
  public void xtestCancelSaveAsAlreadySaved() throws BadLocationException, IOException {
    
    _log.log("+++Starting testCancelSaveAsAlreadySaved");
     
    { /* Bracket former testCancelSaveAsAlreadySaved */
   
      final OpenDefinitionsDocument fooDoc = setupDocument(FOO_TEXT);
      final File fooFile = tempFile();
      
      // No listeners here -- other tests ensure the first save works
      saveFile(fooDoc, new FileSelector(fooFile));
      _log.log("saveFile(...) executed");
      assertModified(false, fooDoc);
      assertContents(FOO_TEXT, fooDoc);
      assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(fooFile));
      
      // No events better be fired!
      _model.addListener(new TestListener());
      _log.log("TestListener added");
      
      // Muck up the document
      Utilities.invokeAndWait(new Runnable() {
        public void run() { changeDocumentText(BAR_TEXT, fooDoc); }
      });
      
      _log.log("Document text changed");
      
      saveFileAs(fooDoc, new CancelingSelector());
      
//      System.err.println("saveFileAs(...) executed");
      
      assertEquals("contents of saved file", FOO_TEXT, IOUtil.toString(fooFile));
      
      _log.log("+++Completing testCancelSaveAsAlreadySaved");
    }
  }

  /** Comprehensive test of the "Save As" command */
  public void xtestSaveAs() throws BadLocationException, IOException {
    
    _log.log("+++Starting testSaveAs");
    
    { /* Bracket former testSaveAsAlreadySaved */
      /** Ensures that saveAs saves to a different file. */
      
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
      
      _log.log("+++Completing testSaveAs");
    }
  }
    
    /* Consolidatingvoid testSaveAsExistsAndOverwrite */
  
  public void xtestSaveAsExists() throws BadLocationException, IOException {
    
    { /* Bracket former testSaveAsExists() */
      
      _log.log("+++Starting testSaveAsExistsAndOverwrite");
      
      Utilities.invokeAndWait(new Runnable() {
        public void run() { 
          try {
            OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
            File file = tempFile();
            doc.saveFileAs(new WarningFileSelector(file));
            fail("Did not warn of open file as expected");
          }
          catch (Exception e) { /* Good behavior for file saving ... */ }
        }
      });
      
      _log.log("testSaveAsExistsAndOverwrite completed");  
    }
  
    /* Consolidating former testSaveAsExistsAndOpen */
    
    { /* Bracket testSaveAsExistsAndOpen */
      
      _log.log("+++Starting testSaveAsExistsAndOpen");
      
      Utilities.invokeAndWait(new Runnable() {
        public void run() { 
          try {
            File file1 = tempFile(1);
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
//            finally {
//              _model.closeFile(doc1);
//              _model.closeFile(doc2);
//            }
          }
          catch (Exception e) {
            // should never happen
            fail("Exception thrown in testSaveAsExistsAndOpen.  Traceback: \n" + e);
          }
        }
      });
      
      _log.log("+++Completing testSaveAsExistsAndOpen");
    }
  
    /* Consolidation of former testSaveAllSaveCorrectFiles() */

    /* Ensures that all open files are saved in appropriate order, i.e., even with BAR file as active document, save all
     * should first prompt to save FOO, then BAR. */
    
    { /* Bracket former testSaveAllSaveCorrectFiles */
      
      _log.log("+++Starting testSaveAllSaveCorrectFiles");
      OpenDefinitionsDocument fooDoc = setupDocument(FOO_TEXT);
      OpenDefinitionsDocument barDoc = setupDocument(BAR_TEXT);
      OpenDefinitionsDocument trdDoc = setupDocument("third document contents");
      final File fileFoo = tempFile();
      final File fileBar = tempFile();
      final File fileTrd = tempFile();
      fooDoc.setFile(fileFoo);
      barDoc.setFile(fileBar);
      trdDoc.setFile(fileTrd);
      
      // None of these documents has been entered in the _documentsRepos
      
      // check.
      final FileSelector fs = new FileSelector(fileFoo);
      
      saveAllFiles(_model, fs);
      
      assertEquals("contents of saved fileFoo", FOO_TEXT, IOUtil.toString(fileFoo));
      assertEquals("contents of saved fileBar", BAR_TEXT, IOUtil.toString(fileBar));
      assertEquals("contents of saved fileTrd", "third document contents", IOUtil.toString(fileTrd));
      
      _model.closeFile(fooDoc);
      _model.closeFile(barDoc);
      _model.closeFile(trdDoc);
      
      _log.log("+++Completing testSaveAllSaveCorrectFiles");
    }
  }

  
  /** Forces a file to be opened with getDocumentforFile. */
  public void xtestRevertFile() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException {
    
    _log.log("+++Starting testRevertFile");
    
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
    
    _log.log("+++Completing testRevertFile");
  }
  
  
  public void xtestModifiedByOther() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException {
    
    _log.log("+++Starting testModifiedByOther");
    
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
    
    _log.log("+++Completing testModifiedByOther");
  }
  
  public void xtestModifiedByOtherFalse() throws BadLocationException, IOException, OperationCanceledException,
    AlreadyOpenException, InterruptedException {
    
    _log.log("+++Starting testModifiedByOtherFalse");
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
    
    _log.log("+++Completing testModifiedByOtherFalse");
  }
  
  /** Interprets some statements, saves the history, clears the history, then loads the history. */
  public void testSaveClearAndLoadHistory() throws EditDocumentException, IOException, InterruptedException {
    
    _log.log("+++Starting testSaveClearAndLoadHistory+++");
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
    _log.log("Waiting for first interaction to complete");
    listener.waitInteractionDone();
    
    listener.assertInteractionEndCount(1);
    listener.assertInteractionStartCount(1);
    
    listener.logInteractionStart();
    interpretIgnoreResult(s2);
    _log.log("Waiting for second interaction to complete");
    listener.waitInteractionDone();
    
    listener.logInteractionStart();
    interpretIgnoreResult(s3);
    _log.log("Waiting for third interaction to complete");
    listener.waitInteractionDone();
    
    _log.log("All interactions complete");
    
    // check that the history contains the correct value
    _log.log("Confirming that history has correct value");
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
    _log.log("contents of saved file = '" + IOUtil.toString(f) + "'");
    assertEquals("contents of saved file", History.HISTORY_FORMAT_VERSION_2 + s1 + delim + s2 + delim + s3 + delim,
                 IOUtil.toString(f));
    
    _log.log("Clearing history");
    _model.clearHistory();
    // confirm that the history is clear
    assertEquals("History is not clear", "", _model.getHistoryAsString());
    
    _log.log("Resetting interactions in testSaveClearAndLoadHistory");
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _model.resetInteractions(_model.getWorkingDirectory());
        _log.log("Interactions reset is complete.");
        _model.resetConsole();
      }
    });
    _log.log("Waiting for ResetDone");
    listener.waitResetDone();
    Utilities.clearEventQueue();
    
    listener.logInteractionStart();
    _log.log("Loading history");
    safeLoadHistory(fs);
    _log.log("Waiting for InteractionDone");
    listener.waitInteractionDone();
        
    // check that output of loaded history is correct
    _log.log("Checking output of loaded history");
    ConsoleDocument con = _model.getConsoleDocument();
    debug.log(con.getDocText(0, con.getLength()).trim());
    _log.log("Output text is '" + con.getDocText(0, con.getLength()).trim() + "'");
    assertEquals("Output of loaded history is not correct", "x = 5", con.getDocText(0, con.getLength()).trim());
    listener.assertInteractionStartCount(4);
    listener.assertInteractionEndCount(4);
    _model.removeListener(listener);

    _log.log("+++Completing testSaveClearAndLoadHistory+++");
  }
  
  /** Loads two history files, one whose statements end in semicolons, and one whose statements do not.
    * Makes sure that it doesn't matter.
    */
  public void testLoadHistoryWithAndWithoutSemicolons() throws IOException, EditDocumentException, 
    InterruptedException {
    
    _log.log("+++Starting testLoadHistoryWithAndWithoutSemicolons");
    
    final InteractionListener listener = new InteractionListener();
    _model.addListener(listener);
    final File f1 = tempFile(1);
    final File f2 = tempFile(2);
    final FileSelector fs1 = new FileSelector(f1);
    final FileSelector fs2 = new FileSelector(f2);
    final String s1 = "val x = 5";
    final String s2 = "println(\"x = \" + x);";
    final String s3 = "val x = 5;";
    final String s4 = "println(\"x = \" + x)";
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        IOUtil.attemptWriteStringToFile(f1, s1 + '\n' + s2 + '\n');
        IOUtil.attemptWriteStringToFile(f2, s3 + '\n' + s4 + '\n');
      }
    });

    listener.assertInteractionStartCount(0);
    listener.logInteractionStart();
    safeLoadHistory(fs1);
//    System.err.println("fs1[" + fs1 + "]");
    listener.waitInteractionDone();
    
    listener.logInteractionStart();
    safeLoadHistory(fs2);
//    System.err.println("fs2[" + fs2 + "]");
    listener.waitInteractionDone();
    
    // check that output of loaded history is correct
    ConsoleDocument con = _model.getConsoleDocument();
//    System.err.println("con = \n" + con.getDocText(0, con.getLength()) + "\n***End of Console***");
    
    assertEquals("Output of loaded history is not correct: " + con.getDocText(0, con.getLength()).trim(),
                 "x = 5" + StringOps.EOL + "x = 5",
                 con.getDocText(0, con.getLength()).trim());

    _log.log("+++Completing testLoadHistoryWithAndWithoutSemicolons");
  }
  
  /** Test for the possibility that the file has been moved or deleted
    * since it was last referenced
    */
  public void xtestFileMovedWhenTriedToSave() throws BadLocationException, IOException {
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    _log.log("+++Starting testFileMovedWhenTriedToSave+++");
    
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
   
    _log.log("+++Completing testFileMovedWhenTriedToSave+++");
  }
  
  /** Tests that input can be written to and read from the console correctly. */
  public void xtestConsoleInput() throws EditDocumentException {
    _log.log("+++Starting testConsoleInput+++");
    _model.getInteractionsModel().setInputListener(new InputListener() {
      int n = 0;
      public String getConsoleInput() {
        n++;
        if (n > 1) throw new IllegalStateException("Input should only be requested once!");
        return "input\n";  // '\n' is used because this input is generated by Swing processing of keystrokes
      }
    });
    _log.log("ClassPath = '" + IterUtil.multilineToString(_model.getClassPath()) + "'");
    String result = interpret("val z = System.in.read()");
    _log.log("interpretation result = " + result);
    String expected = "z: Int = " + String.valueOf((int)'i');
    assertEquals("read() should prompt for input and return the first byte of \"input\"", expected, result);
    
    interpret("import java.io._");
    interpret("val br = new BufferedReader(new InputStreamReader(System.in))");
    result = interpret("val text = br.readLine()");
    assertEquals("readLine() should return the rest of \"input\" without prompting for input",
                 "text: String = nput", result);
    _log.log("+++Completing testConsoleInput+++");
  }
  
  class TestIOListener extends TestListener {
    public synchronized void fileOpened(OpenDefinitionsDocument doc) {  openCount++; } 
    public synchronized void fileClosed(OpenDefinitionsDocument doc) { closeCount++; } 
  }
  
  class TestFileIOListener extends TestIOListener {
    File _expected;
    TestFileIOListener(File f) { _expected = f; }
    public void fileOpened(OpenDefinitionsDocument doc) {
      _log.log("TestIOListener.fileOpened called.  openCount = " + openCount);
      super.fileOpened(doc);
      _log.log("After super.fileOpened called, openCount = " + openCount);
      File file = FileOps.NULL_FILE;
      try { file = doc.getFile(); }
      catch (FileMovedException fme) { fail("file does not exist"); }     // We know file should exist
      assertEquals("file to open", IOUtil.attemptCanonicalFile(_expected), IOUtil.attemptCanonicalFile(file));
    }
  }
}
