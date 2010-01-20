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
package edu.rice.cs.drjava.model;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList; 
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.swing.Utilities;

/** Simple region manager for the entire model.  Follows readers/writers locking protocol of EventNotifier. 
  * TODO: fix the typing of regions.  In many (all?) places, R should be OrderedDocumentRegion. 
  */
public class ConcreteRegionManager<R extends OrderedDocumentRegion> extends EventNotifier<RegionManagerListener<R>> implements 
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
  
  /** Convinces the type checker to accept a DocumentRegion as an R.  This works when you need an R object only for use 
    * with compareTo because all implementations of OrderedDocumentRegion inherit from DocumentRegion and compareTo is 
    * defined in DocumentRegion. 
    */
  @SuppressWarnings("unchecked")
  private <T> T newDocumentRegion(OpenDefinitionsDocument odd, int start, int end) { 
    return (T) new DocumentRegion(odd, start, end);
  }
  
  /** Gets the sorted set of regions less than r. */
  public SortedSet<R> getHeadSet(R r) {
    SortedSet<R> oddRegions = _regions.get(r.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return emptySet();
    return oddRegions.headSet(r);
  }
  
  /** Gets the sorted set of regions greater than or equal to r. */
  public SortedSet<R> getTailSet(R r) {
    SortedSet<R> oddRegions = _regions.get(r.getDocument());
    if (oddRegions == null || oddRegions.isEmpty()) return emptySet();
    return oddRegions.tailSet(r);
  }
  
//  private static <R extends OrderedDocumentRegion> SortedSet<R> reverse(SortedSet<R> inputSet) {
//    if (inputSet.isEmpty()) return inputSet;
//    /* Create outputSet with reverse ordering. */
//    SortedSet<R> outputSet = new TreeSet<R>(new Comparator<OrderedDocumentRegion>() { 
//      public int compare(OrderedDocumentRegion o1, OrderedDocumentRegion o2) { return - o1.compareTo(o2); } 
//    });
//    for (R r: inputSet) outputSet.add(r);
//    return outputSet;
//  }
  
  /** Returns the region [start, end) containing offset.  Since regions can never overlap, there is at most one such 
    * region in the given document.  (Degenerate regions can coalesce but they are empty implying that
    * they are never returned by this method.)  Only runs in the event thread.
    * @param odd the document
    * @param offset the offset in the document
    * @return the DocumentRegion at the given offset, or null if it does not exist.
    */
  public R getRegionAt(OpenDefinitionsDocument odd, int offset) { 
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    /* Get the tailSet consisting of the ordered set of regions [start, end) such that end > offset. */
    @SuppressWarnings("unchecked")
    SortedSet<R> tail = getTailSet((R) newDocumentRegion(odd, 0, offset + 1));
    
    /* If tail contains a match, it must be the first region, since all regions in a document are disjoint and no
     * degenerate region in tail can contain offset. (Every degenerate region is disjoint from every other region 
     * because it is empty.) tail is sorted by [endOffset, startOffset]; tail may be empty. */

    if (tail.size() == 0) return null;
    R r = tail.first();
    
    if (r.getStartOffset() <= offset) return r;
    else return null;
  }
  
  /** Finds the interval of regions in odd such that the line label (excerpt) for the region contains offset. */
  public Pair<R, R> getRegionInterval(OpenDefinitionsDocument odd, int offset) {
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
//    System.err.println("getRegionsNear(" + odd + ", " + offset + ") called");
    
    /* Get the interval of regions whose line label (excerpts) contain offset. The maximium size of the excerpt 
     * enclosing a region is 120 characters; it begins at the start of the line containing the start of the region. 
     * Since empty regions are ignore (and deleted as soon as they are found), the end of a containing region must be 
     * less than 120 characters from offset.  Find the tail set of all regions [start, end) where offset - 120 < end.
     */
    @SuppressWarnings("unchecked")
    SortedSet<R> tail = getTailSet((R) new DocumentRegion(odd, 0, offset - 119));
    
    /* Search tail, selecting first and last regions r such that r.getLineEnd() >= offset and r.getLineStart <= offset.
     * The tail is totally order on BOTH getLineStart() and getLineEnd() because the functions mapping start to lineStart
     * and end to lineEnd are monotonic and the tail is totally ordered on BOTH start and end (since regions do not 
     * overlap).  Hence, we can abandon the search as soon as we reach a region r such that r.getLineStart() > offset.  
     * tail may be empty. */
    
    if (tail.size() == 0) return null;
    
    // Find the first and last regions whose bounds (using line boundaries) contain offset
    Iterator<R> it = tail.iterator();
    R first = null;
    R last = null;
    
    // Find first
    while (it.hasNext()) {
      R r = it.next();
//      System.err.println("Testing region '" + r.getString() + "'");
      /* Note: r may span more than one line. */
      int lineStart = r.getLineStartOffset();
//      System.err.println("lineStart = " + lineStart + " offset = " + offset);
      if (lineStart > offset) break;  // first == null implying test following loop will return
      int lineEnd = r.getLineEndOffset();
//      System.err.println("lineEnd = " + lineEnd);
      if (lineStart - 1 <= offset && lineEnd >= offset) {  // - 1 required to handle inserting wing comment chars
        first = r;
//        System.err.println("Found first region in getRegionInterval = '" + r.getString() +  "'");
        break;
      }
    }
    if (first == null) return null;
    
    // Find last
    last = first;
    while (it.hasNext()) {
      R r = it.next();
      int lineStart = r.getLineStartOffset();
      if (lineStart > offset) break;
      int lineEnd = r.getLineEndOffset();
      if (lineStart <= offset && lineEnd >= offset) {
        last = r;
      }
    }
//    System.err.println("Found last region in getRegionInterval = '" + last +  "'");
    return new Pair<R, R>(first, last);
  }
    
//  /** Returns the rightmost region in the given document that contains [startOffset, endOffset], or null if one does
//    * not exist.  Only executes in the event thread.  Otherwise offset args could be invalid.
//    * Note: this method could be implemented  more cleanly using a revserseIterator on the headSet containing all
//    * regions preceding or equal to the selection. but this functionality was not added to TreeSet until Java 6.0.
//    * @param odd the document
//    * @param offset the offset in the document
//    * @return the DocumentRegion at the given offset, or null if it does not exist.
//    */
//  public Collection<R> getOverlappingRegions(OpenDefinitionsDocument odd, int startOffset, int endOffset) {
//    
//    assert EventQueue.isDispatchThread();
//    
//    /* First try finding the rightmost region on the same line containing the selection. Unnecessary in Java 6.0. */
//    int lineStart = odd._getLineStartPos(startOffset);
//    
//    @SuppressWarnings("unchecked")
//    SortedSet<R> tail = getTailSet((R) newDocumentRegion(odd, lineStart, endOffset));
//    // tail is sorted by <startOffset, endOffset>; tail may be empty
//    R match = null;
//    for (R r: tail) {
//      if (r.getStartOffset() <= startOffset) {
//        if (r.getEndOffset() >= endOffset) match = r;
//      }
//      else break;  // for all remaining r : R (r.getStartOffset() > offset)
//    }
//    if (match != null) return match;
//    
//    /* No match found starting on same line; look for best match starting on preceding lines. */
//    @SuppressWarnings("unchecked")
//    SortedSet<R> revHead = reverse(getHeadSet((R) newDocumentRegion(odd, lineStart, lineStart))); // linear cost! Ugh!
//    
//    /* Find first match in revHead */
//    Iterator<R> it = revHead.iterator();  // In Java 6.0, it is computable in constant time from headSet using reverseIterator
//    
//    R next;
//    while (it.hasNext()) {
//      next = it.next();
//      if (next.getEndOffset() >= endOffset) { match = next; break; }
//    }
//    
//    if (match == null) return null; // no match found
//   
//    /* Try to improve the match by narrowing endOffset. */
//    while (it.hasNext()) { 
//      next = it.next();
//      if (next.getStartOffset() < match.getStartOffset()) return match;  // no more improvement possible
//      assert next.getStartOffset() == match.getStartOffset();
//      if (next.getEndOffset() >= endOffset) match = next;  // improvement because next precedes match in getRegions(odd)
//    }
//
//    return match;  // last region in revHead was the best match
//  }

  /** Returns the set of regions in the given document that overlap the specified interval [startOffset, endOffset),
    * including degenerate regions [offset, offset) where [offset, offset] is a subset of (startOffset, endOffset).
    * Assumes that all regions in the document are disjoint.  Note: degenerate empty regions with form [offset, offset) 
    * vacuously satisfy this property.  Only executes in the event thread.
    * Note: this method could be implemented more cleanly using a revserseIterator on the headSet containing all
    * regions preceding or equal to the selection. but this functionality was not added to TreeSet until Java 6.0.
    * @param odd the document
    * @param startOffset  the left end of the specified interval
    * @param endOffset  the right end of the specified interval
    * @return the Collection<DocumentRegion> of regions overlapping the interval.
    */
  public Collection<R> getRegionsOverlapping(OpenDefinitionsDocument odd, int startOffset, int endOffset) {
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    LinkedList<R> result = new LinkedList<R>();
    if (startOffset == endOffset) return result;
    
    /* Find all regions with an endPoint greater than startOffset. */
    
    @SuppressWarnings("unchecked")
    SortedSet<R> tail = getTailSet((R) newDocumentRegion(odd, 0, startOffset + 1));
    
    // tail is sorted by <startOffset, endOffset>; tail may be empty

    for (R r: tail) {
      if (r.getStartOffset() >= endOffset) break;
      else result.add(r);
    }
    return result;
  }
  
  /** Add the supplied DocumentRegion to the manager.  Only runs in event thread after initialization?
    * @param region the DocumentRegion to be inserted into the manager
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
//    System.err.println("ConcreteRegionManager.removeRegion(" + region + ") called");
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
  
  /** Invoke {@link #removeRegion} on all of the given regions. */
  public void removeRegions(Iterable<? extends R> regions) {
    for (R r: regions) removeRegion(r);
  }
  
  private void _notifyRegionRemoved(final R region) {
    _lock.startRead();
    try { for (RegionManagerListener<R> l: _listeners) { l.regionRemoved(region); } } 
    finally { _lock.endRead(); }
  }
    
  /** Remove the specified document from _documents and _regions (removing all of its contained regions). */
  public void removeRegions(final OpenDefinitionsDocument doc) {
    assert doc != null;
//    System.err.println("Removing regions from ODD " + doc + " in " + this);
//    System.err.println("_documents = " + _documents);
    boolean found = _documents.remove(doc);
//    System.err.println("ODD " + doc + " exists in " + this);
    if (found) {
      final SortedSet<R> regions = _regions.get(doc);
//      System.err.println("Before removal, regions = " + regions);
      // The following ugly loop is dictated by the "fail fast" semantics of Java iterators
      while (! regions.isEmpty()) {
        R r = regions.first();
        regions.remove(r);  
        _notifyRegionRemoved(r);
      }
//      System.err.println("After removal, regions = " + regions);
    }
  }
  
  /** @return a Vector<R> containing the DocumentRegion objects for document odd in this mangager. */
  public SortedSet<R> getRegions(OpenDefinitionsDocument odd) { return _regions.get(odd); }
  
  public int getRegionCount() {
    int regions = 0;
    for (OpenDefinitionsDocument odd: _documents) regions += _regions.get(odd).size();
    return regions;
  }
  
  public ArrayList<R> getRegions() {
    ArrayList<R> regions = new ArrayList<R>();
    for (OpenDefinitionsDocument odd: _documents) regions.addAll(_regions.get(odd));
    return regions;
  }
  
  public ArrayList<FileRegion> getFileRegions() {
    ArrayList<FileRegion> regions = new ArrayList<FileRegion>();
    for (OpenDefinitionsDocument odd: _documents) {
      File f = odd.getRawFile();
      for (R r: _regions.get(odd)) regions.add(new DummyDocumentRegion(f, r.getStartOffset(), r.getEndOffset()));
    }
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
    for (R r: getRegions()) removeRegion(r);
//    final ArrayList<R> regions = getRegions();
////    System.err.println("ConcreteRegionManager.clearRegions() called with regions = " + regions);
//    // Notify all listeners for this manager that all regions are being removed; listener access _regions and _documents
//    _notifyRegionsRemoved(regions);  // fails to close the associated panel because _documents not yet cleared.
//    // Remove all regions in this manager
//    _regions.clear();
//    _documents.clear();
  }
  
//  /** Set the current region. 
//    * @param region new current region */
//  public void setCurrentRegion(final R region) { throw new UnsupportedOperationException(); }
  
  /** Apply the given command to the specified region to change it.
    * @param region the region to find and change
    * @param cmd command that mutates the region. */
  public void changeRegion(final R region, Lambda<R,Object> cmd) {
    cmd.value(region);
    // notify
    _lock.startRead();
    try { for (RegionManagerListener<R> l: _listeners) { l.regionChanged(region); } } 
    finally { _lock.endRead(); }            
  }
  
  /** Updates _lineStartPos, _lineEndPos of regions in the interval [firstRegion, lastRegion] using total ordering on
    * regions.  Removes empty regions.  firstRegion and lastRegion are not necessarily regions in this manager.  
    */
  public void updateLines(R firstRegion, R lastRegion) { 
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    /* Get the tailSet consisting of the ordered set of regions >= firstRegion. */
    SortedSet<R> tail = getTailSet(firstRegion);
    if (tail.size() == 0) return; // tail can be empty if firstRegion is a constructed DocumentRegion

    List<R> toBeRemoved = new ArrayList<R>();  // nonsense to avoid concurrent modification exception
    for (R region: tail) {
      if (region.compareTo(lastRegion) > 0) break;
      region.update();  // The bounds of this region must be recomputed.
      if (region.getStartOffset() == region.getEndOffset()) toBeRemoved.add(region); 
    }
    removeRegions(toBeRemoved);
  }
} 
