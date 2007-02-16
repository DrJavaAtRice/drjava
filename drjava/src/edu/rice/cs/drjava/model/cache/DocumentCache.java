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

package edu.rice.cs.drjava.model.cache;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.util.*;
import java.io.IOException;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.OrderedHashSet;

/** The document cache is a structure that maps OpenDefinitionsDocuments to DefinitionsDocuments (which contain
 *  the actual document text).  Since the latter can consume a lot of memory, the cache virtualizes some of them
 *  using DefinitionsDocument reconstructors (DDReconstructor).  It tries to limit the number of 
 *  DefinitionsDocuments loaded in memory at one time, but it must of course retain all modified 
 *  DefinitionsDocuments.
 *  <p>
 *  The cache creates a DocManager for each OpenDefinitionsDocument entered (registered) in the cache. The managers
 *  maintain the actual links to DefinitionsDocuments. Since the Managers themselves implement the DCacheAdapter 
 *  interface, the model goes directly to the manager to get the instance of the DefinitionsDocument.
 *  <p>
 *  When a document is accessed through the document manager by the model, the cache informs the manager, which 
 *  tells the active queue to add the manager to the end of the queue--if it isn't already in the queue.  If the
 *  active queue had already reached maximum size, it deletes the last document in the queue to keep the queue from
 *  growing larger than its maximum size.
 *  <p>
 *  The resident queue only contains documents that have not been modified since their last save (except in the process
 *  of responding to notification that a document has been modified).  When a document is modified for the first time, 
 *  it is immediately removed from the resident queue and marked as UNMANAGED by its document manager.  An
 *  UNMANAGED document remains in memory until it is saved or closed without being saved.  If such a document is
 *  saved, it is inserted again in the resident queue.
 *  <p>
 *  Since the cache and document managers can both be concurrently accessed from multiple threads, the methods in the
 *  DocumentCache and DocManager classes are synchronized.  Some operations require locks on both the cache and a
 *  document manager, but the code is written so that none of require these locks to be held simultaneously.
 */

public class DocumentCache {
  
  /** Log file. */
  private static final Log _log = new Log("DocumentCache.txt", false);
 
  private static final int INIT_CACHE_SIZE = 32;
  
  /** @invariant _residentQueue.size() <= CACHE_SIZE */
  private int CACHE_SIZE;
  
  private OrderedHashSet<DocManager> _residentQueue;
  
  private Object _cacheLock = new Object();
    
  /* General constructor.  Not currently used except when called by default constructor. */
  public DocumentCache(int size) {
//    Utilities.showDebug("DocumentCache created with size = " + size);
    CACHE_SIZE = size;
    _residentQueue = new OrderedHashSet<DocManager>();
  }
  
  /* Default constructor; uses default cache size. */
  public DocumentCache() { this(INIT_CACHE_SIZE); }

  /** Returns a cache adapter corresponding to the owner of the given reconstructor.
   *  @param odd The open definitions document that is registering.  (Useful for debugging purposes.)
   *  @param rec A reconstructor from which to create the document that is to be managed in this cache
   *  @return an adapter that allows its owner to access its definitions document
   */
  public DCacheAdapter register(OpenDefinitionsDocument odd, DDReconstructor rec) {
    DocManager mgr = new DocManager(rec, odd.toString(), odd.isUntitled());
    notifyRegistrationListeners(odd, mgr);  // runs synchronously; only used in tests
//    System.err.println("register(" + odd + ", " + rec + ") called");
    return mgr;
  }
  
  /** Changes the number of <b>unmodified</b> documents allowed in the cache at one time. <b> Note: modified documents 
    * are not managed in the cache except in transitional situations when a queue document becomes modified.  Only
    * used in tests.
    */
  public void setCacheSize(int size) {
    if (size <= 0) throw new IllegalArgumentException("Cannot set the cache size to zero or less.");
    int diff;
    DocManager[] removed = null;  // bogus initialization makes javac happy
    synchronized(_cacheLock) {    // lock the cache so entries can be removed if necessary
      CACHE_SIZE = size;
      diff = _residentQueue.size() - CACHE_SIZE;
      if (diff > 0) { 
        removed = new DocManager[diff];
        for (int i = 0; i < diff; i++) removed[i] = _residentQueue.remove(0);
      }
      if (diff > 0) kickOut(removed);
    }
  }
  
  /** Kicks out all documents in removed.  Assumes that _cacheLock is already held. */
  private void kickOut(DocManager[] removed) {
    for (DocManager dm: removed) dm.kickOut();
  }
    
  public int getCacheSize() { return CACHE_SIZE; }
  public int getNumInCache() { return _residentQueue.size(); }
    
  
  
  ///////////////////////////// DocManager //////////////////////////
  
  private static final int IN_QUEUE = 0;     // In the resident queue and hence subject to virtualization
  private static final int UNTITLED = 1;     // An untitled document not in queue (may or may not be modified)
  private static final int NOT_IN_QUEUE = 2; // Virtualized and not in the QUEUE
  private static final int UNMANAGED = 3;    // A modified, titled document not in the queue
  /** Note: before extending this table, check that the extension does not conflict with isUnmangedOrUntitled() */
  
  /** Manages the retrieval of a document for a corresponding open definitions document.  This manager only 
   *  maintains its document data if it contained in _residentQueue, which is maintained using a round-robin
   *  replacement scheme.
   */
  private class DocManager implements DCacheAdapter {
    
    private final DDReconstructor _rec;
    private volatile int _stat; // I know, this is not very OO
    private volatile DefinitionsDocument _doc;
    private volatile String _fileName;
    
    /** Instantiates a manager for the documents that are produced by the given document reconstructor.
     *  @param rec The reconstructor used to create the document
     */
    public DocManager(DDReconstructor rec, String fn, boolean isUntitled) {
//      Utilities.showDebug("DocManager(" + rec + ", " + fn + ", " + isUntitled + ")");
      _rec = rec;
      if (isUntitled) _stat = UNTITLED; 
      else _stat = NOT_IN_QUEUE;
      _doc = null;
     _fileName = fn;
    }
    
    /** Adds DocumentListener to the reconstructor. */
    public void addDocumentListener(DocumentListener l) { _rec.addDocumentListener(l); }
    
    /** Makes this document; assumes that cacheLock is already held. */
    private DefinitionsDocument makeDocument() {
      try { // _doc is not in memory
        _doc = _rec.make();
        assert _doc != null;
      }
      catch(Exception e) { throw new UnexpectedException(e); }
//        Utilities.showDebug("Document " + _doc + " reconstructed; _stat = " + _stat);
      if (_stat == NOT_IN_QUEUE) add();       // add this to queue 
      return _doc;
    }
    
    /** Gets the physical document (DD) for this manager.  If DD is not in memory, it loads it into memory and returns
     *  it.  If the document has been modified in memory since it was last fetched, make it "unmanaged", removing it from 
     *  the queue.  It will remain in memory until saved.  If a document is not in the queue, add it.
     *  @return the physical document that is managed by this adapter
     */
    public DefinitionsDocument getDocument() throws IOException, FileMovedException {
//      Utilities.showDebug("getDocument called on " + this + " with _stat = " + _stat);
      
//      The following double-check idiom is safe in Java 1.4 and later JVMs provided that _doc is volatile.
      final DefinitionsDocument doc = _doc;  // create a snapshot of _doc
      if (doc != null) return doc;  
      synchronized(_cacheLock) { // lock the cache so that this DocManager's state can be updated
        if (_doc != null) return _doc;  // _doc may have changed since test outside of _cacheLock
        return makeDocument();
      }
    }
    
    /** Gets the text of this document using in order of preference (i) cached _doc; (ii) cached reconstructor _image; 
      * and (iii) the document after forcing it to be loaded. */
    public String getText() {
      final DefinitionsDocument doc = _doc;  // create a snapshot of _doc
      if (doc != null) return doc.getText();
      String image = _rec.getText();  // There is a technical race here; _doc could be set and modified before here
      if (image != null) return image;
      synchronized(_cacheLock) { // lock the state of this DocManager
        if (_doc != null) return _doc.getText(); // _doc may have changed since test outside of _cacheLock
        return makeDocument().getText();
      }
    }
    
    /* Gets the specified substring of this document; throws an exception if the specification is ill-formed. */
    public String getText(int offset, int len) { 
      String text = getText();
//      _log.log("getText(" + offset + ", " + len + ") called on '" + text + "' which has " + text.length() + " chars");
      
      return text.substring(offset, offset + len);
    }
    
    /** Checks whether the document is resident (in the cache or modified). 
     *  @return if the document is resident.
     */
    public boolean isReady() {  return _doc != null; }  // _doc is volatile so synchronization is unnecessary
  
    /** Closes the corresponding document for this adapter.  Done when a document is closed by the navigator. */
    public void close() {
//      Utilities.showDebug("close() called on " + this);
      synchronized(_cacheLock) {
        _residentQueue.remove(this);
        closingKickOut();
      }
    }
    
    public void documentModified() {
      synchronized(_cacheLock) { 
        _residentQueue.remove(this); // remove modified document from queue if present
        _stat = UNMANAGED;
      }
    }
    
     public void documentReset() {
      synchronized(_cacheLock) { 
        if (_stat == UNMANAGED) add(); // add document to queue if it was formerly unmanaged
      }
    }
     
    /** Updates status of this document in the cache. */
    public void documentSaved(String fileName) {
//      Utilities.showDebug("Document " + _doc + " has been saved as " + fileName);
      synchronized(_cacheLock) {  // lock the document manager so that document manager fields can be updated
        if (isUnmanagedOrUntitled()) {
          _fileName = fileName;
          add();  // add formerly unmanaged/untitled document to queue
        }
      }
    }
    
    /** Adds this DocManager to the queue and sets status to IN_QUEUE.  Assumes _cacheLock is already held. */
    private void add() {
//      Utilities.showDebug("add " + this + " to the QUEUE\n" + "QUEUE = " + _residentQueue);
      if (! _residentQueue.contains(this)) {
        _residentQueue.add(this);
        _stat = IN_QUEUE;
      }
      if (_residentQueue.size() > CACHE_SIZE) _residentQueue.get(0).remove();
    }
    
    /** Removes this DocManager from the queue and sets status to NOT_IN_QUEUE.  Assumes _cacheLock is already held. */
    private void remove() { 
      boolean removed = _residentQueue.remove(this);
      kickOut();
    }
    
    /** All of the following private methods presume that _cacheLock is held */
    private boolean isUnmanagedOrUntitled() { return (_stat & 0x1) != 0; }  // tests if _stat is odd
     
    /** Called by the cache when the document is removed from the active queue and subject to virtualization. 
      * Assumes cacheLock is already held. 
      */
    void kickOut() { kickOut(false); }
    
    /** Called by the cache when the document is being closed.   Note that _doc can be null in this case!
      * Assumes cacheLock is already held. 
      */
    void closingKickOut() { kickOut(true); }
   
    /** Performs the actual kickOut operation.  Assumes cacheLock is already held. */
    private void kickOut(boolean isClosing) {
//      Utilities.showDebug("kickOut(" + isClosing + ") called on " + this);
      if (! isClosing) {
        /* virtualize this document */
//        Utilities.showDebug("Virtualizing " + _doc);
        _rec.saveDocInfo(_doc);
      }
      if (_doc != null) {
        _doc.close(); 
        _doc = null;
      }
      _stat = NOT_IN_QUEUE;
    }
    
    public String toString() { return _fileName; } 
  }
  
  ////////////////////////////////////////
  
  /** This interface allows the unit tests to get a handle on what's going on since the work is spread
   *  between the ODD, the cache, and the Adapters.
   */
  public interface RegistrationListener {
    public void registered(OpenDefinitionsDocument odd, DCacheAdapter man);
  }
  
  private LinkedList<RegistrationListener> _regListeners =   new LinkedList<RegistrationListener>();
  
  public void addRegistrationListener(RegistrationListener list) { synchronized(_regListeners) { _regListeners.add(list); } }
    public void removeRegistrationListener(RegistrationListener list) { synchronized(_regListeners) { _regListeners.remove(list); } }
  public void clearRegistrationListeners() { _regListeners.clear(); }
  // Only used in DocumentCacheTest; must be synchronous for test to succeed.
  private void notifyRegistrationListeners(final OpenDefinitionsDocument odd, final DocManager man) {
    synchronized(_regListeners) {
      if (_regListeners.isEmpty()) return; 
      Utilities.invokeAndWait(new Runnable() {
        public void run() { for (RegistrationListener list : _regListeners) { list.registered(odd, man); } }
      });
    }
  }
}
