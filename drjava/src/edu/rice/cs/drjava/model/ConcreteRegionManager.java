/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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
package edu.rice.cs.drjava.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.MutableTreeNode;

import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;

/** Simple region manager for the entire model.  Follows readers/writers locking protocol of EventNotifier. */
class ConcreteRegionManager<R extends OrderedDocumentRegion> extends EventNotifier<RegionManagerListener<R>> implements 
  RegionManager<R> {
  
  /** Hashtable mapping documents to collections of regions.  Primitive operations are thread safe. */
  private volatile HashMap<OpenDefinitionsDocument, SortedSet<R>> _regions = 
    new HashMap<OpenDefinitionsDocument, SortedSet<R>>();
  
  /** The domain of the _regions.  This field can be extracted from _regions so it is provided to improve performance
    * Primitive operations are thread-safe. 
    */
  private volatile Set<OpenDefinitionsDocument> _documents = new HashSet<OpenDefinitionsDocument>();
  
  /* Depending on default constructor */
  
  /** @return the set of documents containing regions. */
  public Set<OpenDefinitionsDocument> getDocuments() { return _documents; }
  
  private static final SortedSet<Object> EMPTY_SET = new TreeSet<Object>();
  
  /*  Convinces the type checker to accept EMPTY_SET as a set of R. */
  @SuppressWarnings("unchecked")
  private <T> T emptySet() { return (T) EMPTY_SET; }
  
  /** Convinces the type checker to accept a DocumentRegion as an R.  This works when you need an R object only for use with compareTo
    * becasue all implementations of IDocumentRegion inherit from DocumentRegion and compareTo is defined in DocumentRegion. */
  @SuppressWarnings("unchecked")
  private <T> T newDocumentRegion(OpenDefinitionsDocument odd, int start, int end) { 
    return (T) new DocumentRegion(odd, start, end);
  }
  
  private SortedSet<R> getHeadSet(R r) {
    SortedSet<R> oddRegions = _regions.get(r.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return emptySet();
    return oddRegions.headSet(r);
  }
  
  private SortedSet<R> getTailSet(R r) {
    SortedSet<R> oddRegions = _regions.get(r.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return emptySet();
    return oddRegions.tailSet(r);
  }
  
  private static <R extends OrderedDocumentRegion> SortedSet<R> reverse(SortedSet<R> inputSet) {
    if (inputSet.isEmpty()) return inputSet;
    /* Create outputSet with reverse ordering. */
    SortedSet<R> outputSet = new TreeSet<R>(new Comparator<OrderedDocumentRegion>() { 
      public int compare(OrderedDocumentRegion o1, OrderedDocumentRegion o2) { return - o1.compareTo(o2); } 
    });
    for (R r: inputSet) outputSet.add(r);
    return outputSet;
  }
  
  /** Returns the rightmost region in the given document that contains offset, or null if one does not exist.  Assumes
    * that read lock on odd is held (or equivalent).  Otherwise offset could be invalid.
    * @param odd the document
    * @param offset the offset in the document
    * @return the DocumentRegion at the given offset, or null if it does not exist.
    */
  public R getRegionAt(OpenDefinitionsDocument odd, int offset) { return getRegionContaining(odd, offset, offset); }
  
  /** Returns the rightmost region in the given document that contains [startOffset, endOffset], or null if one does
    * not exist.  Assumes that read lock on odd is held (or equivalent).  Otherwise offset args could be invalid.
    * Note; this method could be implemented  more cleanly using a revserseIterator on the headSet containing all
    * regions preceding or equal to the selection. but this functionality was not added to TreeSet until Java 6.0.
    * @param odd the document
    * @param offset the offset in the document
    * @return the DocumentRegion at the given offset, or null if it does not exist.
    */
  public R getRegionContaining(OpenDefinitionsDocument odd, int startOffset, int endOffset) {
    
    /* First try finding the rightmost region on the same line containing the selection. Unnecessary in Java 6.0. */
    int lineStart = odd._getLineStartPos(startOffset);
    
    @SuppressWarnings("unchecked")
    SortedSet<R> tail = getTailSet((R) newDocumentRegion(odd, lineStart, endOffset));
    // tail is sorted by <startOffset, endOffset>; tail may be empty
    R match = null;
    for (R r: tail) {
      if (r.getStartOffset() <= startOffset) {
        if (r.getEndOffset() >= endOffset) match = r;
      }
      else break;  // for all remaining r : R (r.getStartOffset() > offset)
    }
    if (match != null) return match;
    
    /* No match found starting on same line; look for best match starting on preceding lines. */
    @SuppressWarnings("unchecked")
    SortedSet<R> revHead = reverse(getHeadSet((R) newDocumentRegion(odd, lineStart, lineStart))); // linear cost! Ugh!
    
    /* Find first match in revHead */
    Iterator<R> it = revHead.iterator();  // In Java 6.0, it is computable in constant time from headSet using reverseIterator
    
    R next;
    while (it.hasNext()) {
      next = it.next();
      if (next.getEndOffset() >= endOffset) { match = next; break; }
    }
    
    if (match == null) return null; // no match found
   
    /* Try to improve the match by narrowing endOffset. */
    while (it.hasNext()) { 
      next = it.next();
      if (next.getStartOffset() < match.getStartOffset()) return match;  // no more improvement possible
      assert next.getStartOffset() == match.getStartOffset();
      if (next.getEndOffset() >= endOffset) match = next;  // improvement because next precedes match in getRegions(odd)
    }

    return match;  // last region in revHead was the best match
  }
  
  /** Add the supplied DocumentRegion to the manager.  Only runs in event thread after initialization?
    * @param region the DocumentRegion to be inserted into the manager
    * @param index the index at which the DocumentRegion was inserted
    */
  public void addRegion(final R region) {
    final OpenDefinitionsDocument odd = region.getDocument();
    SortedSet<R> docRegions = _regions.get(odd);
    if (docRegions == null) { // if necessary create a Hashtable entry for odd and insert it in the _documents set
      _documents.add(odd);
      docRegions = new TreeSet<R>(); 
      _regions.put(odd, docRegions);
    }
    
    // Check for duplicate region
    final boolean alreadyPresent = docRegions.contains(region);
    if (! alreadyPresent) {    // region does not already exist in manager
      docRegions.add(region);  // modifies docRegions, which is part of _regions
    }
    
    assert _documents.contains(odd);
    
    // only notify if the region was actually added
    if (! alreadyPresent) {
      // notify.  invokeLater unnecessary if it only runs in the event thread
      _lock.startRead();
      try { for (RegionManagerListener<R> l: _listeners) { l.regionAdded(region); } } 
      finally { _lock.endRead(); }
    }
  }
  
  /** Remove the given IDocumentRegion from the manager.  If any document's regions are emptied, remove the document
    * from the keys in _regions.  Notification removes the panel node for the region.
    * @param region the IDocumentRegion to be removed.
    */
  public void removeRegion(final R region) {      
    
    OpenDefinitionsDocument doc = region.getDocument();
    SortedSet<R> docRegions = _regions.get(doc);
//    System.err.println("doc regions for " + doc + " = " + docRegions);
    if (docRegions == null) return;  // since region is not stored in this region manager, exit!
    final boolean wasRemoved = docRegions.remove(region);  // remove the region from the manager
    if (docRegions.isEmpty()) {
      _documents.remove(doc);
      _regions.remove(doc);
    }

    // only notify if the region was actually removed
    if (wasRemoved) _notifyRegionRemoved(region);
  }
  
  private void _notifyRegionRemoved(final R region) {
    _lock.startRead();
    try { for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(region); } } 
    finally { _lock.endRead(); }
  }
  
  private void _notifyRegionsRemoved(Collection<R> regions) {
    _lock.startRead();
    try { 
      for (R r: regions) {
        for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(r); } } 
    }
    finally { _lock.endRead(); }
  }

  
  /** Remove the specified document from _documents and _regions (removing all of its contained regions). */
  @SuppressWarnings("unchecked")
  public void removeRegions(final OpenDefinitionsDocument doc) {
    assert doc != null;
//    System.err.println("Removing ODD " + doc + " in " + this);
    boolean found = _documents.remove(doc);
    if (found) {
//      System.err.println("Removing document regions for " + doc + " in " + this);
      final SortedSet<R> regions = _regions.get(doc);
      // The following ugly line of code is dictated by the "fail fast" semantics of Java iterators
      while (! regions.isEmpty()) regions.remove(regions.first());
    }
  }
  
  /** @return a Vector<R> containing the DocumentRegion objects for document odd in this mangager. */
  public SortedSet<R> getRegions(OpenDefinitionsDocument odd) { return _regions.get(odd); }
  
  public ArrayList<R> getRegions() {
    ArrayList<R> regions = new ArrayList<R>();
    for (OpenDefinitionsDocument odd: _documents) regions.addAll(_regions.get(odd));
    return regions;
  }
  
  public boolean contains(R region) {
    for (OpenDefinitionsDocument doc: _documents) {
      if (_regions.get(doc).contains(region)) return true;
    }
    return false;
  }
  
  /** Tells the manager to remove all regions. */
  public void clearRegions() {
    final ArrayList<R> regions = getRegions();
    // Remove all regions in this manager
    _regions.clear();
    _documents.clear();
    // Notify all listeners for this manager that all regions have been removed
    _notifyRegionsRemoved(regions);
  }
  
//  /** Set the current region. 
//    * @param region new current region */
//  public void setCurrentRegion(final R region) { throw new UnsupportedOperation(); }
  
  /** Apply the given command to the specified region to change it.
    * @param region the region to find and change
    * @param cmd command that mutates the region. */
  public void changeRegion(final R region, Lambda<Object, R> cmd) {
    cmd.apply(region);
    // notify
    _lock.startRead();
    try { for (RegionManagerListener<R> l: _listeners) { l.regionChanged(region); } } 
    finally { _lock.endRead(); }            
  }
} 
