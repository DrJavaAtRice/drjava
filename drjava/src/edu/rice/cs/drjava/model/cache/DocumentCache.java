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

import javax.swing.text.BadLocationException;
import java.util.*;
import java.io.IOException;

import edu.rice.cs.util.Pair;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
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
  
  private static final int INIT_CACHE_SIZE = 2; // normally 24
  
  /** @invariant _residentQueue.size() <= CACHE_SIZE */
  private int CACHE_SIZE;
  
  private OrderedHashSet<DocManager> _residentQueue;
  
  private Object _cacheLock = new Object();
    
  public DocumentCache(int size) {
//    Utilities.showDebug("DocumentCache created with size = " + size);
    CACHE_SIZE = size;
    _residentQueue = new OrderedHashSet<DocManager>();
  }
  
  public DocumentCache() { this(INIT_CACHE_SIZE); }

  /** Returns a cache adapter corresponding to the owner of the given reconstructor.
   *  @param odd The open definitions document that is registering.  (Useful for debugging purposes.)
   *  @param rec A reconstructor from which to create the document that is to be managed in this cache
   *  @return an adapter that allows its owner to access its definitions document
   */
  public DCacheAdapter register(OpenDefinitionsDocument odd, DDReconstructor rec) {
    DocManager mgr = new DocManager(rec, odd.toString(), odd.isUntitled());
    notifyRegistrationListeners(odd, mgr);
//    Utilities.showDebug("register(" + odd + ", " + rec + ") called");
    return mgr;
  }
  
  /** Changes the number of <b>unmodified</b> documents allowed in the cache at one time. <b> Note: modified documents 
   *  are not managed in the cache except in transitional situations when a queue document becomes modified.
   */
  public void setCacheSize(int size) {
    if (size <= 0) throw new IllegalArgumentException("Cannot set the cache size to zero or less.");
    int dist;
    DocManager[] removed = null;  // bogus initialization makes javac happy
    synchronized(_cacheLock) {   // lock the cache so entries can be reomoved if necessary
      CACHE_SIZE = size;
      dist = _residentQueue.size() - CACHE_SIZE;
      if (dist > 0) { 
        removed = new DocManager[dist];
        for (int i = 0; i < dist; i++) removed[i] = _residentQueue.remove(0);
      }
    }
    if (dist > 0) kickOut(removed);
  }
  
  private void kickOut(DocManager[] removed) {
    for (int i = 0; i < removed.length; i++) {
      DocManager dm = removed[i];
      dm.kickOut();
    }
  }
    
  public int getCacheSize() { return CACHE_SIZE; }
  public int getNumInCache() { return _residentQueue.size(); }
    
  /** Called by a manager dm when it is accessed by the model (using getDocument). This operation adds the document
   *  manager dm to the queue unless it is an UNMANAGED document or it is already in the queue.
   *  @param dm The document manager that was just accessed by the model.
   *  @pre the lock for thi is already held.
   */
  private void add(DocManager dm) {
//    Utilities.showDebug("add " + dm + " to the QUEUE\n" + "QUEUE = " + _residentQueue);
    if (dm == null) throw 
      new IllegalArgumentException("Cannot add a null document to the DocumentCache");
    if (dm.isUnmanagedOrUntitled() ) return;
    DocManager removed = null;
    synchronized(_cacheLock) { // lock the cache so that dm can be added if not already present
      if (_residentQueue.contains(dm)) return;
      _residentQueue.add(dm);
      if (_residentQueue.size() > CACHE_SIZE) removed = _residentQueue.remove(0);
    }
    if (removed != null) removed.kickOut();
  }
  
  private boolean remove(DocManager dm) { synchronized(_cacheLock) { return _residentQueue.remove(dm); } }
  
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
    
    private int _stat; // I know, this is not very OO
    private DDReconstructor _rec;
    private DefinitionsDocument _doc;
    private String _filename;
    
    private Object _dmLock = new Object();  // private synchronization lock
    
    /** Instantiates a manager for the documents that are produced by the given document reconstructor.
     *  @param rec The reconstructor used to create the document
     */
    public DocManager(DDReconstructor rec, String fn, boolean isUntitled) {
//      Utilities.showDebug("DocManager(" + rec + ", " + fn + ", " + isUntitled + ")");
      if (isUntitled) _stat = UNTITLED;
      else _stat = NOT_IN_QUEUE;
      _rec = rec;
      _doc = null;
     _filename = fn;
    }
    
    public DDReconstructor getReconstructor() { return _rec; }
    
    /** Gets the physical document (DD) for this manager.  If DD is not in memory, it loads it into memory and returns
     *  it.  If the document has been modified in memory since it was last fetched, make it "unmanaged", removing it from 
     *  the queue.  It will remain in memory until saved.  If a document is not in the queue, add it.
     *  @return the physical document that is managed by this adapter
     */
    public DefinitionsDocument getDocument() throws IOException, FileMovedException {
      boolean isResident = false;
      boolean makeUnmanaged = false;  // makeUnmanaged -> isResident
      synchronized(_dmLock) { // lock the document manageer so that its state can be updated
        isResident = _doc != null;
        if (isResident) {  // Document is in queue or "unmanaged" (a modified doc or a new doc with no file)
          if (isUnmanagedOrUntitled()) return _doc;
          makeUnmanaged = _doc.isModifiedSinceSave();
          if (makeUnmanaged)  { setUnmanaged(); }
        }
      }
      if (makeUnmanaged) remove(this); // remove this from queue
      if (isResident) return _doc;
        
      boolean isUntitled = false;
      synchronized(_dmLock) {      // Lock dm so that the _doc field can be updated.
        isUntitled = isUntitled();  // This locking may be overkill; once titled, always titled
        try { // _doc is not in memory
          _doc = _rec.make();
          if (_doc == null) throw new IllegalStateException("the reconstructor made a null document");
        }
        catch(BadLocationException e) { throw new UnexpectedException(e); }
      }
//      Utilities.showDebug("Document " + _doc + " reconstructed; _stat = " + _stat);
      if (! isUntitled) addToQueue();  // add this to queue if corresponds to a disk file
      return _doc;
    }
    
    /** Checks whether the document is ready to be returned.  If false, then the document would have to be 
     *  loaded from disk. 
     *  @return if the document is already loaded
     */
    public boolean isReady() { return _doc != null; }
  
    /** Closes the corresponding document for this adapter.  Done when a document is closed by the navigator. */
    public void close() {
//      Utilities.showDebug("close() called on " + this);
      remove(this);
      closingKickOut();
    }
    
    public void documentSaved(String fileName) {
      boolean addThis = false;
      synchronized(_dmLock) {  // lock the document manager so that document manager fields can be updated
        addThis = isUnmanagedOrUntitled();
        if (addThis) {
          setNotInQueue();
          _filename = fileName;
        }
      }
      if (addThis) { addToQueue(); }  // synchronization is done in add method for cache
    }
    
    private boolean notInQueue() { return ! _residentQueue.contains(this); }
    private boolean isUntitled() { return _stat == UNTITLED; }
    private boolean isUnmanagedOrUntitled() { return (_stat & 0x1) != 0; }  // tests if _stat is odd
    private void setUnmanaged() { _stat = UNMANAGED; }
    private void setNotInQueue() { _stat = NOT_IN_QUEUE; }
    private void addToQueue() {
      add(this);
      _stat = IN_QUEUE;
    }
        
    /** Called by the cache when the document is removed from the active queue and subject to virtualization. 
     *  @pre lock for this already held. */
    void kickOut() { kickOut(false); }
    /** Called by the cache when the document is being closed.   Note that _doc can be null in this case!
     *  @pre lock for this already held. */
    void closingKickOut() { kickOut(true); }
   
    private void kickOut(boolean isClosing) {
//      Utilities.showDebug("kickOut(" + isClosing + ") called on " + this);
      synchronized(_dmLock) {
        if (! isClosing) {
          /* virtualize this document */
//        Utilities.showDebug("Virtualizing " + _doc);
          _rec.saveDocInfo(_doc);
        }
        if (_doc != null) {
          _doc.close();  // done elsewhere when isClosing is true?
          _doc = null;
        }
        _stat = NOT_IN_QUEUE;
      }
    }
    
    public String toString() { return _filename; } 
  }
  
  ////////////////////////////////////////
  
  /** This interface allows the unit tests to get a handle on what's going on since the work is spread
   *  between the ODD, the cache, and the Adapters.
   */
  public interface RegistrationListener {
    public void registered(OpenDefinitionsDocument odd, DCacheAdapter man);
  }
  
  private LinkedList<RegistrationListener> _regListeners =   new LinkedList<RegistrationListener>();
  
  public void addRegistrationListener(RegistrationListener list) { _regListeners.add(list); }
  public void removeRegistrationListener(RegistrationListener list) { _regListeners.remove(list); }
  public void clearRegistrationListeners() { _regListeners.clear(); }
  private void notifyRegistrationListeners(OpenDefinitionsDocument odd, DocManager man) {
    for (RegistrationListener list : _regListeners) { list.registered(odd,man); }
  }
}
