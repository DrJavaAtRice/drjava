/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;

/** Test functions of the single display model.
 *  @version $Id$
 */
public class SingleDisplayModelTest extends GlobalModelTestCase {

  // _log is inherited from GlobalModelTestCase
  
  private Object _readyLock = new Object(); // lock used for wait/notify on interpreterReady event
  
  /** Get the instance of the SingleDisplayModel.*/
  private DefaultGlobalModel getSDModel() { return  _model; }

  protected void assertNotEmpty() {
    assertTrue("number of documents", getSDModel().getOpenDefinitionsDocuments().size() > 0);
  }

  protected void assertActiveDocument(OpenDefinitionsDocument doc) {
    assertEquals("active document", doc, getSDModel().getActiveDocument());
  }

  /** Creates and returns a new document, makes sure newFile and activeDocumentChanged events are fired, and then
   *  adds some text.
   *  @return the new modified document
   */
  protected OpenDefinitionsDocument setupDocument(String text) throws BadLocationException {
    
    assertNotEmpty();
    SDTestListener listener = new SDTestListener() {
      public synchronized void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
      public synchronized void activeDocumentChanged(OpenDefinitionsDocument doc) { switchCount++; }
    };

    getSDModel().addListener(listener);

    listener.assertSwitchCount(0);

    // Open a new document
    int numOpen = getSDModel().getOpenDefinitionsDocuments().size();
    OpenDefinitionsDocument doc = getSDModel().newFile();
    assertNumOpenDocs(numOpen + 1);

    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    assertLength(0, doc);
    assertModified(false, doc);

    changeDocumentText(text, doc);  // not atomic but no other thread is trying to modify doc
    getSDModel().removeListener(listener);
    
    _log.log("New File " + doc + " created");

    return doc;
  }

  /** Tests the invariant that at least one document is open at time of creation. */
  public void testNotEmptyOnStartup() throws BadLocationException {
    // Should be one empty document after creation
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = getSDModel().getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);
    _log.log("testNotEmptyOnStartup completed");
  }

  /** Tests the setNext and setPrevious functions, making sure that the activeDocumentChanged event is called. */
  public void testDocumentSwitching() throws BadLocationException {
    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public synchronized void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
      public synchronized void activeDocumentChanged(OpenDefinitionsDocument doc) { switchCount++; }
    };
    getSDModel().addListener(listener);

    // Set up first document
    OpenDefinitionsDocument doc3 = getSDModel().getActiveDocument();
    changeDocumentText(FOO_TEXT, doc3);
    listener.assertSwitchCount(0);

    // Set up two more documents
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    assertActiveDocument(doc2);

    OpenDefinitionsDocument doc1 = setupDocument(BAZ_TEXT);
    assertNumOpenDocs(3);
    listener.assertNewCount(2);
    listener.assertSwitchCount(2);
    assertActiveDocument(doc1);

    // Make sure setPrevious doesn't move (at start of list)
    getSDModel().setActivePreviousDocument();
    listener.assertSwitchCount(3);
    assertActiveDocument(doc3);

    // Test setPrevious
    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(4);
    assertActiveDocument(doc1);
    
    // Test setPrevious
    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(5);
    assertActiveDocument(doc2);

    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(6);
    assertActiveDocument(doc3);

    // Make sure setNext doesn't move (at end of list)
    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(7);
    assertActiveDocument(doc1);

    // Test setPrevious
    getSDModel().setActivePreviousDocument();
    listener.assertSwitchCount(8);
    assertActiveDocument(doc3);

    // Test setActive
    getSDModel().setActiveDocument(doc1);
    listener.assertSwitchCount(9);
    assertActiveDocument(doc1);

    // Make sure number of docs hasn't changed
    assertNumOpenDocs(3);
    getSDModel().removeListener(listener);
    _log.log("testDocumentSwitching completed");
  }

  /** Ensures that an unmodified, empty document is closed after a file is opened, while a modified document
   *  is left open.
   */
  public void testCloseUnmodifiedAutomatically() throws BadLocationException, IOException,
    OperationCanceledException, AlreadyOpenException {
    
    assertNumOpenDocs(1); // This assertion depends on the active document being set before setUp() is finished
    OpenDefinitionsDocument doc = getSDModel().getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);

    final File tempFile = writeToNewTempFile(BAR_TEXT);

    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        File file = null;
        try { file = doc.getFile(); }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        try {
          assertEquals("file to open", tempFile.getCanonicalFile(), file.getCanonicalFile());
          synchronized(this) { openCount++; }
        }
        catch (IOException ioe) { fail("could not get canonical file"); }
      }
      public synchronized void fileClosed(OpenDefinitionsDocument doc) { closeCount++; }
      public synchronized void activeDocumentChanged(OpenDefinitionsDocument doc) { switchCount++; }
    };
    getSDModel().addListener(listener);

    // Open file, should replace the old
    doc = getSDModel().openFile(new FileSelector(tempFile));
    listener.assertOpenCount(1);
    listener.assertCloseCount(1);
    listener.assertSwitchCount(1);
    assertNumOpenDocs(1);
    assertModified(false, doc);
    assertContents(BAR_TEXT, doc);
    getSDModel().removeListener(listener);
    _log.log("testCloseUnmodifiedAutomatically completed");
  }

  /** Tests that active document is switched on close, and that a new file is created after the last one is closed. */
  public void testCloseFiles() throws BadLocationException {
    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public synchronized boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
      public synchronized void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
      public synchronized void fileClosed(OpenDefinitionsDocument doc) { closeCount++; }
      public synchronized void activeDocumentChanged(OpenDefinitionsDocument doc) { switchCount++; }
      public synchronized void interpreterReady(File wd) {  // closing all files calls resetInteractions
//        Utilities.show("interpreterReady(" + wd + ") called");
//        Utilities.show("Traceback is:\n" + StringOps.getStackTrace());
        interpreterReadyCount++;
        synchronized(_readyLock) { _readyLock.notify(); }
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
    
    _log.log("Starting second phase of testCloseFiles");

    // Set up two documents
    doc1 = _model.getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(3);
    
    _log.log("Just before calling _model.closeAllFiles()");

    // Close all files, ensure new one was created
    _model.closeAllFiles();
    Utilities.clearEventQueue();
    assertNumOpenDocs(1);
    assertLength(0, _model.getActiveDocument()); 
    
    // wait for interpreter to be ready
    try {
      synchronized(_readyLock) {
        if (listener.getInterpreterReadyCount() == 0) _readyLock.wait(10000);  // intentionally not a while 
      }
    }
    catch(InterruptedException e) { fail("Wait for interpreterReady event was interrupted by " + e); }
    listener.assertInterpreterReadyCount(1);  
    listener.assertNewCount(4);
    listener.assertCloseCount(4);
    listener.assertAbandonCount(4);

    _model.removeListener(listener);
//    _log.log("testCloseFiles completed");
  }

  /** Tests the getCompleteFileName method. */
  public void testCompleteFilename() throws BadLocationException, IOException, OperationCanceledException, 
    AlreadyOpenException {
    // Untitled
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    assertEquals("untitled display filename", "(Untitled)", doc.getCompletePath());

    // Ends in ".java"
    File file = File.createTempFile("DrJava-filename-test", ".java", _tempDir).getCanonicalFile();
    file.deleteOnExit();
    String name = file.getAbsolutePath();
    doc = _model.openFile(new FileSelector(file));
            
    assertEquals(".java display filename", name, doc.getCompletePath());

    // Doesn't contain ".java"
    file = File.createTempFile("DrJava-filename-test", ".txt", _tempDir).getCanonicalFile();
    file.deleteOnExit();
    name = file.getAbsolutePath();
    doc = _model.openFile(new FileSelector(file));
    assertEquals(".txt display filename", name, doc.getCompletePath());

    // Modified File
    file = File.createTempFile("DrJava-filename-test", ".java", _tempDir).getCanonicalFile();
    file.deleteOnExit();
    name = file.getAbsolutePath();
    doc = _model.openFile(new FileSelector(file));
    changeDocumentText("foo", doc);
    assertEquals(".java.txt display filename", name + " *", doc.getCompletePath());
    _log.log("testDisplayFilename completed");
  }
  
  public void testDeleteFileWhileOpen() 
    throws IOException, OperationCanceledException, AlreadyOpenException  {
    String txt = "This is some test text";
    File f = writeToNewTempFile(txt);
    OpenDefinitionsDocument doc1 = _model.openFile(new FileSelector(f));
    OpenDefinitionsDocument doc2 = _model.newFile();
    f.delete();
    _model.closeFile(doc1);
     _log.log("testDeleteFileWhileOpen completed");
    // TODO: possibly test with more files; test to make sure the 
    // active document get's switched correctly.
    
    // Closing one file works.  It doesn't work when you are closing 
    // multiple files including the one that doesn't exist on the file system.
    
    // Furthermore, if the user clicks "YES" to save to a different location, 
    // it doesn't prompt for the location to save, but immediately procedes with
    // the closing and runs into the DocumentNotFound exception which ultimately
    // leads to the View breaking.  Solution may lie in whatever is not letting
    // the file selector show up when clicking "YES" to resave.
  }
  public void testDeleteFileBeforeCloseAll() 
    throws IOException, OperationCanceledException, AlreadyOpenException {
    final File[] files = new File[10];
    for (int i = 0; i < 10; i++) {
      String txt = "Text for file " + i;
      files[i] = writeToNewTempFile(txt);
    }
    FileOpenSelector fos = new FileOpenSelector() {
      public File[] getFiles() throws OperationCanceledException { return files; }
    };
    _model.openFiles(fos);
    _log.log("Opened files " + Arrays.toString(files));
    OpenDefinitionsDocument doc = _model.getSortedOpenDefinitionsDocuments().get(5);
    _model.setActiveDocument(doc);
    _log.log("Active document is: " + doc);
    files[5].delete();
    _log.log("Delected document: " + doc);
    _model.closeAllFiles();
    _log.log("testDeleteFileBeforeCloseAll completed");
  }
  
  /** A GlobalModelListener for testing. By default it expects no events to be fired. To customize,
    * subclass and override one or more methods.
    */
  public static class SDTestListener extends TestListener implements GlobalModelListener {
    
    /** Extra counter for SDTestListener */
    protected volatile int switchCount;

    public void resetCounts() {
      super.resetCounts();
      switchCount = 0;
    }

    public void assertSwitchCount(int i) { assertEquals("number of active document switches", i, switchCount); }

    public void activeDocumentChanged(OpenDefinitionsDocument doc) {
      fail("activeDocumentChanged fired unexpectedly");
    }
    
    public int getInterpreterReadyCount() { return interpreterReadyCount; }
  }
}
