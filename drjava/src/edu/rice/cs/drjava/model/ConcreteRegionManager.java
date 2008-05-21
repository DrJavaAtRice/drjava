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
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingUtilities;

import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;

/** Simple region manager for the entire model.  Follows readers/writers locking protocol of EventNotifier. */
class ConcreteRegionManager<R extends OrderedDocumentRegion> extends EventNotifier<RegionManagerListener<R>> implements 
  RegionManager<R> {
  
  /** Hashtable mapping documents to collections of regions.  Primitive operations are thread safe. */
  private volatile Hashtable<OpenDefinitionsDocument, SortedSet<R>> _regions = 
    new Hashtable<OpenDefinitionsDocument, SortedSet<R>>();
  
  /** The domain of the _regions.  This field can be extracted from _regions so it is provided to improve performance
    * Primitive operations are thread-safe. 
    */
  private volatile Set<OpenDefinitionsDocument> _documents = 
    Collections.synchronizedSet(new HashSet<OpenDefinitionsDocument>());
  
  private volatile R _current = null;
  
  /* Depending on default constructor */
  
  /** @return the current region or null if none selected */
  public R getCurrentRegion() { return _current; }
  
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
  
  /** Returns the sorted set of regions that are >= startRegion and < endRegion.
    * @param startRegion first region to compare to
    * @param endRegion second region to compare to
    * @return sorted set of regions between the two specified regions */
  private SortedSet<R> getRangeSet(R startRegion, R endRegion) {
    SortedSet<R> oddRegions = _regions.get(startRegion.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return emptySet();
    SortedSet<R> tailset = oddRegions.tailSet(startRegion);
    if (tailset == null || tailset.isEmpty()) return emptySet();
    return tailset.headSet(endRegion);
  }
  
  /** Returns true if the region manager contains the specified region.
    * @param r region to check
    * @return true if region is found */
  public boolean contains(R r) {
    SortedSet<R> oddRegions = _regions.get(r.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return false;
    for (OpenDefinitionsDocument doc: _documents) {
      if (oddRegions.contains(r)) { return true; }
    }
    return false;
  }
  
  /** Returns the region in the given document overlapping the region [start,end],
    * or null if one does not exist.
    * @param odd the document
    * @param start the start offset in the document
    * @param end the end offset in the document
    * @return the DocumentRegion overlapping [start,end], or null if it does not exist.
    */
  public R getRegionAt(OpenDefinitionsDocument odd, int start, int end) {
    final int lineStartOffset = odd.getLineStartPos(start);
    final int lineEndOffset = odd.getLineEndPos(end);
    // get all the regions after the beginning of the start line and before the end of the end line
    @SuppressWarnings("unchecked")
    SortedSet<R> range = getRangeSet((R) newDocumentRegion(odd, lineStartOffset, lineStartOffset),
                                     (R) newDocumentRegion(odd, lineEndOffset+1, lineEndOffset+1));
    if (range.isEmpty()) return null;

    // now see if there is a region that overlaps with the region [start,end]
    for(R r: range) {
      final int rStart = r.getStartOffset();
      final int rEnd = r.getEndOffset();
      // is rStart not past the [start,end] region?
      if  (rStart<=end) {
        // is rEnd inside [start,end]?
        if ((rEnd>=start) && (rEnd<=end)) {
//          // r contained? [start...<rStart...rEnd>...end]
//          if (rStart>=start) { return r; }
          // r overlaps left? <rStart...[start...rEnd>...end]
          if (rStart<=start) { return r; }
        }
        // is rEnd on the right side of [start,end]?
        else if ((rEnd>=start) && (rEnd>=end)) {
          // r encloses? <rStart...[start...end]...rEnd>
          if (rStart<=start) { return r; }
          // r overlaps right? [start...<rStart...end]...rEnd>
          if (rStart>=start) { return r; }
        }
      }
    }
    return null;
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
      docRegions = Collections.synchronizedSortedSet(new TreeSet<R>()); 
      _regions.put(odd, docRegions);
    }
    
    // Check for duplicate region
    if (! docRegions.contains(region)) { // region does not already exist in manager
      docRegions.add(region);  // modifies docRegions, which is part of _regions
//        Utilities.show("Region manager " + this + " added region " + region);
//        Utilities.show("docRegions for document " + odd + " = " + _regions.get(odd));
    }
    
    _current = region;
//      final int regionIndex = getIndexOf(region);
//      final String stackTrace = StringOps.getStackTrace();
    
    assert _documents.contains(odd);
    
    // notify.  invokeLater unnecessary if it only runs in the event thread
    Utilities.invokeLater(new Runnable() { public void run() {
      _lock.startRead();
      try {
        for (RegionManagerListener<R> l: _listeners) { l.regionAdded(region); }
      } finally { _lock.endRead(); }
    } });
  }
  
  /** Remove the given IDocumentRegion from the manager.  If any document's regions are emptied, remove the document
    * from the keys in _regions.
    * @param region the IDocumentRegion to be removed.
    */
  public void removeRegion(final R region) {      
    // if we're removing the current region, select a more recent region, if available
    // if a more recent region is not available, select a less recent region, if available
    // if a less recent region is not available either, set to null
    final R current = _current; // so we can verify if _current got changed
    
    OpenDefinitionsDocument doc = region.getDocument();
    SortedSet<R> docRegions = _regions.get(doc);
    if (docRegions == null) return;  // since region is not stored in this region manager, exit!
    docRegions.remove(region);  // remove the region from the manager
    if (docRegions.isEmpty()) {
      _documents.remove(doc);
      _regions.remove(doc);
    }
    
    // notify
    Utilities.invokeLater(new Runnable() { public void run() {
      _lock.startRead();
      try {
        for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(region); }
      } finally { _lock.endRead(); }
    } });
  }
  
  /** Remove the specified document from _documents and _regions (removing all of its contained regions). */
  public void removeRegions(OpenDefinitionsDocument doc) {
    assert doc != null;
    boolean found = _documents.remove(doc);
    if (found) {
      for (final R region: _regions.get(doc)) {
        // notify
        Utilities.invokeLater(new Runnable() { public void run() {
          _lock.startRead();
          try {
            for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(region); }
          } finally { _lock.endRead(); }
        } });
      }
      _regions.remove(doc);
    }
  }
  
  /** @return a Vector<R> containing the DocumentRegion objects for document odd in this mangager. */
  public SortedSet<R> getRegions(OpenDefinitionsDocument odd) { return _regions.get(odd); }
  
  public Vector<R> getRegions() {
    Vector<R> regions = new Vector<R>();
    for (OpenDefinitionsDocument odd: _documents) regions.addAll(_regions.get(odd));
//      Utilities.show("in ConcreteRegionManager " + this + ",_documents = " + _documents);
//      Utilities.show("getRegions returning: " + regions);
    return regions;
  }
  
  /** Tells the manager to remove all regions. */
  public void clearRegions() {
    for(OpenDefinitionsDocument doc: _documents) {
      for (final R region: _regions.get(doc)) {
        // notify
        Utilities.invokeLater(new Runnable() { public void run() {
          _lock.startRead();
          try {
            for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(region); }
          } finally { _lock.endRead(); }
        } });
      }
    }
    _regions.clear(); 
    _documents.clear();
  }
  
  /** Set the current region. 
    * @param region new current region */
  public void setCurrentRegion(final R region) { _current = region; }
  
  /** Apply the given command to the specified region to change it.
    * @param region the region to find and change
    * @param cmd command that mutates the region. */
  public void changeRegion(final R region, Lambda<Object, R> cmd) {
//      final OpenDefinitionsDocument doc = region.getDocument();
    cmd.apply(region);
    Utilities.invokeLater(new Runnable() { public void run() {
      // notify
      _lock.startRead();
      try {
        for (RegionManagerListener<R> l: _listeners) { l.regionChanged(region); }
      } finally { _lock.endRead(); }            
    } });
  }
} 