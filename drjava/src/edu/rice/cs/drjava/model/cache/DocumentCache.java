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

/**
 * The document cache is a structure that organizes the instances
 * of the DefinitionsDocument.  It ensures that there no more than a 
 * set number of DefinitionsDocuments loaded in memory at one time 
 * since the information stored within them is large.  
 * <p>
 * It manages the documents with DocManagers.  There is exactly one 
 * manager for each OpenDefinitionsDocument that is registered with this 
 * cache. The managers are the only objects that keep a link to the 
 * document. Since the Managers themselves implement the DCacheAdapter 
 * interface, the rest of the model goes directly to the manager to get 
 * the instance of the DefinitionsDocument (which saves time).
 * <p>
 * When a manager is at the end of the most recently used list, the
 * cache notifies the manager and the manager discards the instance
 * of the DefinitionsDocument.  When the document is retrieved from
 * the manager by the rest of the model, the manager (if it isn't already
 * at the front of the LRU) notifies the cache of this event and the 
 * cache modifies the LRU accordingly dealing with any other document
 * managers that this event affects.
 * <p>
 * The LRU only applies to documents that have not been modified since
 * their last save.  If a document is modified, the cache does not
 * put it in the LRU nor does it tell the manager to ever discard the
 * instance of the DefinitionsDocument. Whenever that modified document 
 * is saved again, the manager is placed again in the LRU and is treated
 * as the others.
 */
public class DocumentCache{
  
  private int CACHE_SIZE;
  
  private LinkedList<DocManager> _lru;
    
  public DocumentCache(int size) {
    CACHE_SIZE = size;
    _lru = new LinkedList<DocManager>();
  }
  
  public DocumentCache() { this(24); }

  /**
   * Returns a cache adapter corresponding to the owner of the
   * given reconstructor.
   * @param odd The open definitions document that is registering.
   * (This is there for unit testing purposes).
   * @param rec A reconstructor from which to create the document
   * that is to be managed in this cache
   * @return an adapter that allows its owner to access its 
   * definitions document
   */
  public DCacheAdapter register(OpenDefinitionsDocument odd, DDReconstructor rec) {
    DocManager man = new DocManager(rec);
    notifyRegistrationListeners(odd,man);
    return man;
  }
  
  /**
   * Changes the number of allowed <b>unmodified</b> documents 
   * in the cache at one time.  <br>
   * Note: modified documents are not managed in the cache.
   */
  public synchronized void setCacheSize(int size) {
    if (size <= 0) throw new IllegalArgumentException("Cannot set the cache size to zero or less.");
    
    CACHE_SIZE = size;
    DocManager current;
    if (_lru.size() > CACHE_SIZE) {
      ListIterator<DocManager> it = _lru.listIterator();
      int i = 1;
      while (it.hasNext()) {
        current = it.next();
        if (i > CACHE_SIZE) {
          current.kickOut();
          it.remove();
        }
        i++;
      }
    }
  }
  
  public synchronized int getCacheSize() { return CACHE_SIZE; }
  
  public int getNumInCache() { return _lru.size(); }
    
  /**
   * Called by a manager when it is used by the model.
   * This causes the given document manager to be put at
   * the top of the LRU.
   * @param dm The document manager that was just used.
   */
  private synchronized void newFirst(DocManager dm) {
    if (_lru.size() > 0) _lru.getFirst().setNotFirst();
    _lru.remove(dm); // Make sure dm isn't in the LRU
    _lru.addFirst(dm);
    dm.setFirst();
    if (_lru.size() > CACHE_SIZE) {
      DocManager last = _lru.getLast();
      _lru.removeLast();
      last.kickOut();
    }
  }
  
  /**
   * Removes the given DocManager from the top LRU list
   * if in it at all.
   */
  private synchronized void remove(DocManager toRemove) {
    _lru.remove(toRemove);
    if (toRemove.isFirst()) toRemove.kickOut();
    if (_lru.size() > 0 )
      _lru.getFirst().setFirst(); // just in case the one removed was first
  }
  
  
  
  
  ///////////////////////////// DocManager //////////////////////////
  
  private static final int FIRST_IN_LRU = 0; // At the top of the LRU
  private static final int OTHER_IN_LRU = 1; // In the LRU but not first
  private static final int NOT_IN_LRU = 2;   // Inactive and not in the LRU
  private static final int UNMANAGED = 3;    // Possibly open but not managed by LRU
  
  /** Manages the retrieval of a document for a corresponding open definitions document.  This manager 
   *  only maintains its document data if it is among the top CACHE_SIZE most recently used managers in the set.
   */
  private class DocManager implements DCacheAdapter {
    
    private int _stat; // I know, this is not very OO
    private DDReconstructor _rec;
    private DefinitionsDocument _doc;
    
    /**
     * Instantiates a manager for the documents that are produced by
     * the given document reconstructor
     * @param rec The reconstructor used to create the document
     */
    public DocManager(DDReconstructor rec) {
      _stat = NOT_IN_LRU;
      _rec = rec;
      _doc = null;
    }
    
    public synchronized void setReconstructor(DDReconstructor rec) {
      _rec = rec;
      close();
    }
    
    public synchronized DDReconstructor getReconstructor() { return _rec; }
  
    /** Retrieves the document for the corresponding ODD.  If the document
     *  is not in memory, it loads it into memory and then returns it.
     * 
     *  If the file is modified in memory, it is unmanaged.  If it's not 
     *  t the front of the LRU and it is managed (unmodified in memory),
     *  then put it at the top of the LRU.  
     *  @return the document that is managed by this adapter
     */
    public synchronized DefinitionsDocument getDocument() 
      throws IOException, FileMovedException {
        
      if (_stat != FIRST_IN_LRU && _stat != UNMANAGED) makeMeFirst();
      else if (_stat == UNMANAGED && _doc !=null && !_doc.isModifiedSinceSave()) _stat = NOT_IN_LRU;

      if (_doc != null) return _doc;
  
      try {
        _doc = _rec.make();
        if (_doc == null) throw new IllegalStateException("the reconstructor made a null document");
      }
      catch(BadLocationException e) { throw new UnexpectedException(e); }
      return _doc;
    }
    
    /**
     * Checks whether the document is ready to be returned.  If false, then
     * the document would have to be loaded from disk.
     * @return if the document is already loaded
     */
    public synchronized boolean isReady() { return _doc != null; }
  
    /** Closes the corresponding document for this adapter. */
    public void close() {
      kickOut(false); // should not save the doc info
      DocumentCache.this.remove(this);
    }
        
    /** Should be called by the cache */
    void kickOut() { kickOut(true); }
    
    private synchronized void kickOut(boolean save) {
      if (_doc != null) {
        if (save) _rec.saveDocInfo(_doc);
        _doc.close();
        _doc = null;
      }
      _stat = NOT_IN_LRU;
    }
    
    /**
     * Tells the cache that this document should be first
     * document in the LRU.
     */
    private void makeMeFirst() { DocumentCache.this.newFirst(this); }
    
    // The following methods used by the cache to speed up algos
    // These enable the managers to decide when to bypass the 
    // LRU and just give the user the document.
    
    void setStatus(int stat) { _stat = stat; }
    int getStatus() { return _stat; }
    
    void setFirst() { _stat = FIRST_IN_LRU; }
    void setOut()   { _stat = NOT_IN_LRU; }
    void setNotFirst() {
      if (_doc != null && _doc.isModifiedSinceSave()) {
        _stat = UNMANAGED;
        DocumentCache.this.remove(this);
      }
      _stat = OTHER_IN_LRU;
    }
    
    boolean isFirst()    { return _stat == FIRST_IN_LRU; }
    boolean isNotFirst() { return _stat == OTHER_IN_LRU; }
    boolean isOut()      { return _stat == NOT_IN_LRU; }
    
    public String toString() { return "Manager for: " + _doc; } 
  }
  
  ////////////////////////////////////////
  
  /**
   * This interface allows the unit tests to get a handle
   * on what's going on since the work is spread between the
   * ODD, the cache, and the Adapters.
   */
  public interface RegistrationListener {
    public void registered(OpenDefinitionsDocument odd, DCacheAdapter man);
  }
  
  private LinkedList<RegistrationListener> _regListeners =   new LinkedList<RegistrationListener>();
  
  public void addRegistrationListener(RegistrationListener list) { _regListeners.add(list); }
  public void removeRegistrationListener(RegistrationListener list) {
    _regListeners.remove(list);
  }
  public void clearRegistrationListeners() {
    _regListeners.clear();
  }
  private void notifyRegistrationListeners(OpenDefinitionsDocument odd, DocManager man) {
    for (RegistrationListener list : _regListeners) {
      list.registered(odd,man);
    }
  }
  
}
