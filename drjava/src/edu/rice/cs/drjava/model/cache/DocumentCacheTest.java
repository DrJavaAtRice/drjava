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

package edu.rice.cs.drjava.model.cache;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.util.*;

/**
 * This used to extend GlobalModelTestCase, but now it 
 * extends just TestCase because the GlobalModelTestCase
 * was changed to use a DefaultSingleDisplayModel rather
 * than a DefaultGlobalModel.  The extra code in Single
 * Display makes it very difficult to predict which documents
 * will be in the cache and in which order.  This is because 
 * The single display model has extra algorithms that 
 * maintain the single-active-document invariant.  Given that
 * the SingleDisplayModel is not keeping any references to
 * DefinitionsDocuments, that should not be a source of a
 * memory leak for the DocumentCache
 */
public class DocumentCacheTest extends TestCase {
  
  private DefaultGlobalModel _model;
  private DocumentCache _cache;
  private Hashtable<OpenDefinitionsDocument, DCacheAdapter> _adapterTable;
  
  private int _doc_made;
  private int _doc_saved;
  
  protected File _tempDir;

  public void setUp() throws IOException {
//    super.setUp();
    createModel();
    
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
    
    _cache = _model.getDocumentCache();
    _cache.setCacheSize(4);
    _adapterTable = new Hashtable<OpenDefinitionsDocument, DCacheAdapter> ();
    _cache.addRegistrationListener(new DocumentCache.RegistrationListener() {
      public void registered(OpenDefinitionsDocument odd, DCacheAdapter a) {
        _adapterTable.put(odd, a);
      }
    });
    _doc_made = 0;
    _doc_saved = 0;
  }
  
  public void tearDown() throws IOException {
    boolean ret = FileOps.deleteDirectory(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);
    _model.dispose();

    _tempDir = null;
    _model = null;
    System.gc();
  }
  
  /**
   * Instantiates the GlobalModel to be used in the test cases.
   */
  protected void createModel() {
    //_model = new DefaultSingleDisplayModel(_originalModel);
    _model = new TestGlobalModel();

    // Wait until it has connected
    _model.waitForInterpreter();
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java", _tempDir);
  }
  
  /**
   * Create a new temporary file in _tempDir.  Calls with the same
   * int will return the same filename, while calls with different
   * ints will return different filenames.
   */
  protected File tempFile(int i) throws IOException {
    return File.createTempFile("DrJava-test" + i, ".java", _tempDir);
  }
  
  protected OpenDefinitionsDocument openFile(final File f) throws IOException{
    try{
      OpenDefinitionsDocument doc = _model.openFile(new FileOpenSelector(){        
        public File getFile() {
          return f;
        }
        
        public File[] getFiles() {
          return new File[] {f};
        }
      });
      return doc;
    }catch(AlreadyOpenException e){
      throw new IOException(e.getMessage());
    }catch(OperationCanceledException e){
      throw new IOException(e.getMessage());
    }
  }
  /**
   * a good warmup test case
   */
  public void testCacheSize() {
    _cache.setCacheSize(6);
    assertEquals("Wrong cache size", 6, _cache.getCacheSize());
  }
  
  public void testDocumentsInAndOutOfTheCache() throws BadLocationException, IOException {
    assertEquals("Wrong Cache Size", 4, _cache.getCacheSize());
    
    // The documents should not be activated upon creation 
    
    OpenDefinitionsDocument doc1 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    OpenDefinitionsDocument doc2 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    OpenDefinitionsDocument doc3 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    OpenDefinitionsDocument doc4 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    OpenDefinitionsDocument doc5 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    OpenDefinitionsDocument doc6 =  _model.newFile();
    assertEquals("There should be 0 documents in the cache", 0, _cache.getNumInCache()); // was 0
    
    // This tests that isModifiedSinceSave does not cause the document to load into the cache,
    // so the two that should have been kicked out, 1 & 2 should not be loaded uppon calling isModified.

    assertFalse("Document 1 shouldn't be modified", doc1.isModifiedSinceSave());
    assertFalse("Document 2 shouldn't be modified", doc2.isModifiedSinceSave());
    assertFalse("Document 3 shouldn't be modified", doc3.isModifiedSinceSave());
    assertFalse("Document 4 shouldn't be modified", doc4.isModifiedSinceSave());
    assertFalse("Document 5 shouldn't be modified", doc5.isModifiedSinceSave());
    assertFalse("Document 6 shouldn't be modified", doc6.isModifiedSinceSave());
    assertEquals("There should be 4 documents in the cache", 0, _cache.getNumInCache());
    
    assertFalse("Document 1 shouldn't be ready", _adapterTable.get(doc1).isReady());
    assertFalse("Document 2 shouldn't be ready", _adapterTable.get(doc2).isReady());
    assertFalse("Document 3 shouldn't be ready", _adapterTable.get(doc3).isReady());
    assertFalse("Document 4 shouldn't be ready", _adapterTable.get(doc4).isReady());
    assertFalse("Document 5 shouldn't be ready", _adapterTable.get(doc5).isReady());
    assertFalse("Document 6 shouldn't be ready", _adapterTable.get(doc6).isReady());
    
    
    // Activate all documents and make sure that the right ones get kicked out
    // Front of LRU -> a b c d | e f <- out of LRU
    doc1.getLength();
    doc2.getLength();
    doc3.getLength();
    doc4.getLength();
    doc5.getLength();
    doc6.getLength();
    
    // 6 5 4 3 | 2 1
    assertFalse("Document 1 shouldn't be ready", _adapterTable.get(doc1).isReady());
    assertFalse("Document 2 shouldn't be ready", _adapterTable.get(doc2).isReady());
    assertTrue("Document 3 shouldn't be ready", _adapterTable.get(doc3).isReady());
    assertTrue("Document 4 shouldn't be ready", _adapterTable.get(doc4).isReady());
    assertTrue("Document 5 shouldn't be ready", _adapterTable.get(doc5).isReady());
    assertTrue("Document 6 shouldn't be ready", _adapterTable.get(doc6).isReady());
        
    doc1.getLength(); // 1 6 5 4 | 3 2
    assertTrue("The document 1 should should now be in the cache", _adapterTable.get(doc1).isReady());    
    assertEquals("There should still be 1 documents in the cache", 4, _cache.getNumInCache()); 
    assertFalse("The document 3 should have been kicked out of the cache", _adapterTable.get(doc3).isReady());
    
    doc2.getLength(); // 2 1 6 5 | 4 3
    assertTrue("The document 2 should should now be in the cache", _adapterTable.get(doc2).isReady());
    assertEquals("There should still be 2 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 4 should have been kicked out of the cache", _adapterTable.get(doc4).isReady());
    
    doc3.getLength(); // 3 2 1 6 | 5 4
    assertTrue("The document 3 should should now be in the cache", _adapterTable.get(doc3).isReady());
    assertEquals("There should still be 3 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 5 should have been kicked out of the cache", _adapterTable.get(doc5).isReady());
    
    doc4.getLength(); // 4 3 2 1 | 6 5
    assertTrue("The document 4 should should now be in the cache", _adapterTable.get(doc4).isReady());
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 6 should have been kicked out of the cache", _adapterTable.get(doc6).isReady());
    
    doc5.getLength(); // 5 4 3 2 | 1 6
    assertTrue("The document 5 should should now be in the cache", _adapterTable.get(doc5).isReady());
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 1 should have been kicked out of the cache", _adapterTable.get(doc1).isReady());
    
    doc6.getLength(); // 6 5 4 3 | 2 1
    assertTrue("The document 6 should should now be in the cache", _adapterTable.get(doc6).isReady());
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 2 should have been kicked out of the cache", _adapterTable.get(doc2).isReady());
    
    // Load documents out of order
    doc4.getLength(); // 4 6 5 3 | 2 1
    assertTrue("The document 3 should should still be in the cache", _adapterTable.get(doc3).isReady());    
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    doc5.getLength(); // 5 4 6 3 | 2 1
    assertTrue("The document 3 should should still be in the cache", _adapterTable.get(doc3).isReady());    
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    doc3.getLength(); // 3 5 4 6 | 2 1
    assertTrue("The document 6 should should still be in the cache", _adapterTable.get(doc6).isReady());    
    assertEquals("There should still be 4 documents in the cache", 4, _cache.getNumInCache());
    doc4.getLength(); // 4 3 5 6 | 2 1
    assertTrue("The document 6 should should still be in the cache", _adapterTable.get(doc6).isReady());    
    
    assertEquals("There should be 4 documents in the cache", 4, _cache.getNumInCache());
    assertFalse("The document 1 should still be out of the cache", _adapterTable.get(doc1).isReady());
    assertFalse("The document 2 should still be out of the cache", _adapterTable.get(doc2).isReady());
    
    // Test the resize cache method by increasing the size of the cache to 5, which is still less than the number of open documents: 6
    _cache.setCacheSize(5); // 4 3 5 6 | 2 1 
    assertEquals("The cache size should now be 5", 5, _cache.getCacheSize());
    assertEquals("There should still only be 4 files in the cache", 4, _cache.getNumInCache());
    
    doc2.getLength(); // 2 4 3 5 6 | 1
    assertTrue("The document 2 should now be in the cache", _adapterTable.get(doc2).isReady());
    assertFalse("The document 1 should still be out of the cache", _adapterTable.get(doc1).isReady());
    assertEquals("There should be 5 documents in the cache", 5, _cache.getNumInCache());
    
    _cache.setCacheSize(3); // 2 4 3 | 5 6 1
    
    assertEquals("The cache size should now be 3", 3, _cache.getCacheSize());
    assertEquals("There should be 3 documents in the cache", 3, _cache.getNumInCache());
    assertTrue("The document 2 should be in the cache", _adapterTable.get(doc2).isReady());
    assertTrue("The document 4 should be in the cache", _adapterTable.get(doc4).isReady());
    assertTrue("The document 3 should be in the cache", _adapterTable.get(doc3).isReady());
    assertFalse("The document 5 should now be out of the cache", _adapterTable.get(doc5).isReady());
    assertFalse("The document 6 should now be out of the cache", _adapterTable.get(doc6).isReady());
    assertFalse("The document 1 should still be out of the cache", _adapterTable.get(doc1).isReady());
  }
  
  public void testGetDDocFromCache() throws BadLocationException, IOException, OperationCanceledException {
    File file1 = tempFile(1);
    File file2 = tempFile(2);
    File file3 = tempFile(3);
    File file4 = tempFile(4);
    File file5 = tempFile(5);
    File file6 = tempFile(6);
    
    // opening a document should set it as active
    OpenDefinitionsDocument doc1 = openFile(file1);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc1).isReady());
    assertEquals("There should be 1 documents in the cache", 1, _cache.getNumInCache());
    OpenDefinitionsDocument doc2 = openFile(file2);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc2).isReady());
    assertEquals("There should be 2 documents in the cache", 2, _cache.getNumInCache());
    OpenDefinitionsDocument doc3 = openFile(file3);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc3).isReady());
    assertEquals("There should be 3 documents in the cache", 3, _cache.getNumInCache());
    OpenDefinitionsDocument doc4 = openFile(file4);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc4).isReady());
    assertEquals("There should be 4 documents in the cache", 4, _cache.getNumInCache());
    OpenDefinitionsDocument doc5 = openFile(file5);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc5).isReady());
    assertFalse("The document should not start out in the cache", _adapterTable.get(doc1).isReady());
    assertEquals("There should be 4 documents in the cache", 4, _cache.getNumInCache());
    OpenDefinitionsDocument doc6 = openFile(file6);
    assertTrue("The document should not start out in the cache", _adapterTable.get(doc6).isReady());
    assertFalse("The document should not start out in the cache", _adapterTable.get(doc2).isReady());
    assertEquals("There should be 4 documents in the cache", 4, _cache.getNumInCache());
  }
  
  
  private DefinitionsDocument _saved; // used for testReconstructor()
  
  public void testReconstructor() throws IOException{
    final int i = 0;
    DDReconstructor d = new DDReconstructor(){
      public DefinitionsDocument make(){
        _doc_made++;
        return _saved;
      }
      public void saveDocInfo(DefinitionsDocument doc){
        _doc_saved++;
      }
      public void addDocumentListener(javax.swing.event.DocumentListener dl) {
        // don't do anything
      }
    };
    
    OpenDefinitionsDocument doc1 =  _model.newFile();
    assertFalse("The document should not be in the cache", _adapterTable.get(doc1).isReady());
    _saved = _adapterTable.get(doc1).getDocument();
    assertTrue("The document should be in the cache", _adapterTable.get(doc1).isReady());
    
    _adapterTable.get(doc1).setReconstructor(d);
    assertFalse("The document should not be in the cache after an update", _adapterTable.get(doc1).isReady());
    
    _adapterTable.get(doc1).getDocument(); // force the cache to reconstruct the document.

    assertEquals("The make in the reconstructor was called 1nce", 1, _doc_made);
    assertEquals("The save in the reconstructor was not called", 0, _doc_saved);
  }
  
  // not being used.  The new definition of the cache allows for a closed document, if it is used again, to bring its document back.
  // This should be dealt with.
  public void testNoDDocInCache(){
   OpenDefinitionsDocument doc1 = _model.newFile();
   _model.closeFile(doc1);
   assertFalse("The document should now be closed", _adapterTable.get(doc1).isReady());
  }


  public void testNumListeners(){
   OpenDefinitionsDocument doc1 = _model.newFile();
   OpenDefinitionsDocument doc2 = _model.newFile();
   OpenDefinitionsDocument doc3 = _model.newFile();
   OpenDefinitionsDocument doc4 = _model.newFile();
   OpenDefinitionsDocument doc5 = _model.newFile();

   int numDocListeners = doc1.getDocumentListeners().length;
   int numUndoListeners = doc1.getUndoableEditListeners().length;
   
   doc1.getLength();
   doc2.getLength();
   doc3.getLength();
   doc4.getLength();

   // this will kick document one out of the cache
   doc5.getLength();
 
   // this will reconstruct document 1
   doc1.getLength();
   
   assertEquals("the number of document listeners is the same after reconstruction", numDocListeners, doc1.getDocumentListeners().length);
   assertEquals("the number of undoableEditListeners is the same after reconstruction", numUndoListeners, doc1.getUndoableEditListeners().length);

  }
  
  /**
   * There used to be a memory leak where various listeners, 
   * LeafElements, and other extraneous references from the model, 
   * definitions pane, and main frame would be preventing the
   * definitions panes/documents from being GC'd at the correct
   * times causing the entire program to run out of heap space
   * when working with large numbers of files.  This problem was
   * agrivated when we added project facility and implemented
   * the document cache (which was supposed to solve our memory
   * problem but actually worsened it).  
   * <p>Adam and Jonathan went through great pains to remove 
   * these references, so <b>don't break our work!!!</b></p>
   */
  public void testMemoryLeak() throws InterruptedException{
    _memLeakCounter=0;
    FinalizationListener<DefinitionsDocument> fl = new FinalizationListener<DefinitionsDocument>() {
      public void finalized(FinalizationEvent<DefinitionsDocument> fe) {
        _memLeakCounter++;
      }
    };
    
    // Adding the listeners will load the document into the cache
    OpenDefinitionsDocument doc1 = _model.newFile();
    OpenDefinitionsDocument doc2 = _model.newFile();
    OpenDefinitionsDocument doc3 = _model.newFile();
    OpenDefinitionsDocument doc4 = _model.newFile();
    OpenDefinitionsDocument doc5 = _model.newFile();
        
    doc1.addFinalizationListener(fl);
    doc2.addFinalizationListener(fl);
    doc3.addFinalizationListener(fl);
    doc4.addFinalizationListener(fl);
    doc5.addFinalizationListener(fl); // kick 1 out
    
    assertEquals("There should be 4 in the LRU", 4, _cache.getNumInCache());
    System.gc();
    Thread.sleep(100);
    
    
    assertFalse("doc1 should be the one that's not ready", _adapterTable.get(doc1).isReady());
    assertEquals("One doc should have been collected", 1, _memLeakCounter);
    
    doc1.getLength(); // kick 2
    
    // make sure doc1 has it's finalization listener still
    List<FinalizationListener<DefinitionsDocument>> list = doc1.getFinalizationListeners();
    assertEquals("There should only be one finalization listener", 1, list.size());
    assertEquals("The finalization listener should be fl", fl, list.get(0));
    
    doc2.getLength(); // kick 3
    doc3.getLength(); // kick 4
    doc4.getLength(); // kick 5
    doc5.getLength(); // kick 1
    
    System.gc();
    Thread.sleep(100);
    assertEquals("several docs should have been collected", 6, _memLeakCounter);
    
  }
  private int _memLeakCounter;
  
  /**
   * This is just so that we can have an instance of a DefaultGlobalModel
   * rather than a single display model.
   */
  private class TestGlobalModel extends DefaultGlobalModel {
    public void addListener(GlobalModelListener listener) { addListenerHelper(listener); }
    public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) { /* dummy method */ }
    public void saveAllFiles(FileSaveSelector fs) throws IOException { saveAllFilesHelper(fs); }
    
    public OpenDefinitionsDocument newFile() { return newFile(null); }
    public OpenDefinitionsDocument openFile(FileOpenSelector fs) 
      throws IOException, OperationCanceledException, AlreadyOpenException { 
      return openFileHelper(fs); 
    }
    public boolean closeFile(OpenDefinitionsDocument doc) { return closeFileHelper(doc); }
    public OpenDefinitionsDocument openFiles(FileOpenSelector com)
      throws IOException, OperationCanceledException, AlreadyOpenException {
      return openFilesHelper(com); 
    }
    
  }
}
