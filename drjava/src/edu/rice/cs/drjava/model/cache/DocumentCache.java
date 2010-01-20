/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

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
import edu.rice.cs.plt.iter.IterUtil;

/** The document cache is a structure that maps OpenDefinitionsDocuments to DefinitionsDocuments (which contain
  * the actual document text).  Since the latter can consume a lot of memory, the cache virtualizes some of them
  * using DefinitionsDocument reconstructors (DDReconstructor).  It tries to limit the number of 
  * DefinitionsDocuments loaded in memory at one time, but it must of course retain all modified 
  * DefinitionsDocuments.
  * <p>
  * The cache creates a DocManager for each OpenDefinitionsDocument entered (registered) in the cache. The managers
  * maintain the actual links to DefinitionsDocuments. Since the Managers themselves implement the DCacheAdapter 
  * interface, the model goes directly to the manager to get the instance of the DefinitionsDocument.
  * <p>
  * When a document is accessed through the document manager by the model, the cache informs the manager, which 
  * tells the active queue to add the manager to the end of the queue--if it isn't already in the queue.  If the
  * active queue had already reached maximum size, it deletes the last document in the queue to keep the queue from
  * growing larger than its maximum size.
  * <p>
  * The resident queue only contains documents that have not been modified since their last save (except in the process
  * of responding to notification that a document has been modified).  When a document is modified for the first time, 
  * it is immediately removed from the resident queue and marked as UNMANAGED by its document manager.  An
  * UNMANAGED document remains in memory until it is saved or closed without being saved.  If such a document is
  * saved, it is inserted again in the resident queue.
  * <p>
  * Since the cache and document managers can both be concurrently accessed from multiple threads, the methods in the
  * DocumentCache and DocManager classes are synchronized.  Some operations require locks on both the cache and a
  * document manager, but the code is written so that none of require these locks to be held simultaneously.
  */

public class DocumentCache {
  
  /** Log file. */
  private static final Log _log = new Log("DocumentCache.txt", false);
  
  private static final int INIT_CACHE_SIZE = 32;
  
  /** invariant _residentQueue.size() <= CACHE_SIZE */
  private int CACHE_SIZE;
  
  private LinkedHashSet<DocManager> _residentQueue;
  
  private Object _cacheLock = new Object();
  
  /* General constructor.  Not currently used except when called by default constructor. */
  public DocumentCache(int size) {
//    Utilities.showDebug("DocumentCache created with size = " + size);
    CACHE_SIZE = size;
    _residentQueue = new LinkedHashSet<DocManager>();
  }
  
  /* Default constructor; uses default cache size. */
  public DocumentCache() { this(INIT_CACHE_SIZE); }
  
  /** Returns a cache adapter corresponding to the owner of the given reconstructor.
    * @param odd The open definitions document that is registering.  (Useful for debugging purposes.)
    * @param rec A reconstructor from which to create the document that is to be managed in this cache
    * @return an adapter that allows its owner to access its definitions document
    */
  public DCacheAdapter register(OpenDefinitionsDocument odd, DDReconstructor rec) {
    DocManager mgr = new DocManager(rec, odd.isUntitled());
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
    synchronized(_cacheLock) {    // lock the cache so entries can be removed if necessary
      CACHE_SIZE = size;
      int diff = _residentQueue.size() - CACHE_SIZE;
      if (diff > 0) {
        Iterable<DocManager> toRemove = IterUtil.snapshot(IterUtil.truncate(_residentQueue, diff));
        for (DocManager dm : toRemove) { _residentQueue.remove(dm); dm.kickOut(); }
      }
    }
  }
  
  public int getCacheSize() { return CACHE_SIZE; }
  public int getNumInCache() { return _residentQueue.size(); }
  
  public String toString() { return _residentQueue.toString(); }
  
  
  ///////////////////////////// DocManager //////////////////////////
  
  private static final int IN_QUEUE = 0;     // In the resident queue and hence subject to virtualization
  private static final int UNTITLED = 1;     // An untitled document not in queue (may or may not be modified)
  private static final int NOT_IN_QUEUE = 2; // Virtualized and not in the QUEUE
  private static final int UNMANAGED = 3;    // A modified, titled document not in the queue
  /** Note: before extending this table, check that the extension does not conflict with isUnmangedOrUntitled() */
  
  /** Manages the retrieval of a document for a corresponding open definitions document.  This manager only 
    * maintains its document data if it contained in _residentQueue, which is maintained using a round-robin
    * replacement scheme.
    */
  private class DocManager implements DCacheAdapter {
    
    private final DDReconstructor _rec;
    private volatile int _stat; // I know, this is not very OO
    private volatile DefinitionsDocument _doc;
    
    /** Instantiates a manager for the documents that are produced by the given document reconstructor.
      * @param rec The reconstructor used to create the document
      */
    public DocManager(DDReconstructor rec, boolean isUntitled) {
//      Utilities.showDebug("DocManager(" + rec + ", " + fn + ", " + isUntitled + ")");
      _rec = rec;
      if (isUntitled) _stat = UNTITLED; 
      else _stat = NOT_IN_QUEUE;
      _doc = null;
//      System.err.println(this + " constructed");
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
//      System.err.println("Making document for " + this);
      if (_stat == NOT_IN_QUEUE) add();       // add this to queue 
      return _doc;
    }
    
    /** Gets the physical document (DD) for this manager.  If DD is not in memory, it loads it from its image in its
      * DDReconstructor and returns it.  If the document has been modified in memory since it was last fetched, make 
      * it "unmanaged", removing it from the queue.  It will remain in memory until saved.  If a document is not in 
      * the queue, add it.
      * @return the physical document that is managed by this adapter
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
    
    /** Gets the length of this document using (i) cached _doc or (ii) reconstructor (which may force the document
      * to be loaded. */
    public int getLength() {
      final DefinitionsDocument doc = _doc;  // create a snapshot of _doc
      if (doc == null /* || ! doc.isModifiedSinceSave()*/) return _rec.getText().length();
      return doc.getLength();
    }
    
    
    /** Gets the text of this document using the cached reconstructor if document is not resident or it is unchanged.
      * If document is not locked, may return stale data. */
    public String getText() {
      final DefinitionsDocument doc = _doc;  // create a snapshot of _doc
      if (doc == null /* || ! doc.isModifiedSinceSave() */) return _rec.getText();  
//      if (doc == null) return _rec.getText();
      return doc.getText();
    }
    
    /* Gets the specified substring of this document; throws BadLocationException if the specification is ill-formed. */
    public String getText(int offset, int len) throws BadLocationException { 
      final DefinitionsDocument doc = _doc; // create a snapshot of _doc
      if (doc == null) {
        try { return _rec.getText().substring(offset, offset + len); }
        catch(IndexOutOfBoundsException e) { throw new BadLocationException(e.getMessage(), offset); }  
      }
//      _log.log("getText(" + offset + ", " + len + ") called on '" + text + "' which has " + text.length() + " chars");
      return doc.getText(offset, len); 
    }
    
    /** Checks whether the document is resident (in the cache or modified). 
      * @return if the document is resident.
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
    public void documentSaved() {
//      Utilities.showDebug("Document " + _doc + " has been saved");
//      System.err.println("Document " + _doc + " has been saved");
      synchronized(_cacheLock) {  // lock the document manager so that document manager fields can be updated
        if (isUnmanagedOrUntitled()) {
          add();  // add formerly unmanaged/untitled document to queue
        }
      }
    }
    
    /** Adds this DocManager to the queue and sets status to IN_QUEUE.  Assumes _cacheLock is already held. */
    private void add() {
//      Utilities.showDebug("add " + this + " to the QUEUE\n" + "QUEUE = " + _residentQueue);
//      System.err.println("adding " + this + " to the QUEUE\n" + "QUEUE = " + _residentQueue);
      if (! _residentQueue.contains(this)) {
        _residentQueue.add(this);
        _stat = IN_QUEUE;
      }
      if (_residentQueue.size() > CACHE_SIZE) IterUtil.first(_residentQueue).remove();
    }
    
    /** Removes this DocManager from the queue and sets status to NOT_IN_QUEUE.  Assumes _cacheLock is already held. */
    private void remove() { 
      _residentQueue.remove(this);
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
    
    public String toString() { return "DocManager for " + _rec.toString() + "[stat = " + _stat + "]"; } 
  }
  
  ////////////////////////////////////////
  
  /** This interface allows the unit tests to get a handle on what's going on since the work is spread
    * between the ODD, the cache, and the Adapters.
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
