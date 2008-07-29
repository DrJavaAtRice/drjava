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

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.swing.Utilities;

/** Browser history manager for the entire model.  Follows readers/writers locking protocol of EventNotifier.  This 
  * history is simply a list together with a current pointer. 
  */
public class BrowserHistoryManager extends EventNotifier<RegionManagerListener<BrowserDocumentRegion>> {
  
  /** List of regions.  Public operations are thread safe. */
  private volatile TreeSet<BrowserDocumentRegion> _regions = new TreeSet<BrowserDocumentRegion>();
  
  private volatile BrowserDocumentRegion _current = null;
  private volatile int _maxSize;
  
  /** Create a new ConcreteRegionManager with the specified maximum size.
    * @param size maximum number of regions that can be stored in this manager.
    */
  public BrowserHistoryManager(int size) { _maxSize = size; }
  
  /** Create a new ConcreteRegionManager without maximum size. */
  public BrowserHistoryManager() { this(0); }
  
  /** Tests if specified region r is contained in this manager.
    * @param r  The region
    * @return  whether the manager contains region r
    */
//    public boolean contains(DocumentRegion r) { return _regionSet.contains(r); }
  
  // Not used
//  private boolean equals(BrowserDocumentRegion r1, BrowserDocumentRegion r2) {
//    return r1.getDocument() == r2.getDocument() && r1.getStartOffset() == r2.getStartOffset() &&
//      r1.getEndOffset() == r2.getEndOffset();
//  }

  /** Add the supplied DocumentRegion r to the manager just above _current and set _current to refer to r.  Only runs
    * in event thread after initialization.  Notifies regionAdded listeners if _currentIndex is changed.  Assumes 
    * _regions.isEmpty() || _regions.contains(_current) and _regions.isEmpty() == (_current == null)
    * @param region the DocumentRegion to be inserted into the manager
    * @param index the index at which the DocumentRegion was inserted
    */
  
  public void addBrowserRegion(final BrowserDocumentRegion r, final GlobalEventNotifier notifier) {
    
    assert EventQueue.isDispatchThread();
    
//    Utilities.show("addBrowserRegion(" + r + ") called with regions = " + _regions + " and current = " + _current);
    if (_current != null) {
      // flush the stack of regions above _current
//      Utilities.show("Flushing elements above current = " + _current);
      SortedSet<BrowserDocumentRegion> tail = _regions.tailSet(_current); 
//      Utilities.show("Tail set above current is " + tail);
      Iterator<BrowserDocumentRegion> it = tail.iterator();
      if (it.hasNext()) {
        BrowserDocumentRegion nr = it.next();  // skip over current
        assert nr == _current;
//        Utilities.show("Skipped " + nr);
      }
      while (it.hasNext()) { 
        BrowserDocumentRegion nr = it.next(); 
        it.remove(); 
//        Utilities.show("Removed " + nr + ", leaving " + _regions);
      }
    }
//    Utilities.show("Before adding, regions = " + _regions);
    _current = r;
    
    _regions.add(r);
    r.getDocument().addBrowserRegion(r);
    
    // Notify listeners of this event
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _lock.startRead();
        try { for (RegionManagerListener<BrowserDocumentRegion> l: _listeners) { l.regionAdded(r); } } 
        finally { _lock.endRead(); }
      } 
    });
    
    // remove a region if necessary
    shrinkManager();
    notifier.browserChanged();
  }
  
  /** Remove regions if there are more than the maximum number allowed. Typically used to remove one region. */
  private void shrinkManager() {
    if (_maxSize > 0) {
      int size = _regions.size();
      int diff = size - _maxSize;
      for (int i = 0; i < diff; ++i) remove(_regions.first());
    }
  }
  
  /** Remove the given DocumentRegion from the manager.
    * @param region  the DocumentRegion to be removed.
    */
  public synchronized void remove(final BrowserDocumentRegion r) {
    OpenDefinitionsDocument doc = r.getDocument();
    _regions.remove(r);
    doc.removeBrowserRegion(r);
    _current = next(r);
  }
  
  /** @return the region immediately higher than r and last (highest) if no higher region exists). */
  private BrowserDocumentRegion next(BrowserDocumentRegion r) {
    BrowserDocumentRegion next = _higher(_regions, r);
    if (next == null && ! _regions.isEmpty()) next = _regions.last();
    return next;
  }
  
  /** @return an ArrayList<DocumentRegion> containing the DocumentRegion objects in this mangager. */
  public SortedSet<BrowserDocumentRegion> getRegions() { return _regions; }
  
  /** Tells the manager to remove all regions. */
  public synchronized void clearBrowserRegions() {
    _current = null;
    _regions.clear();
  }
  
  /** @return the current region or null if none selected */
  public BrowserDocumentRegion getCurrentRegion() { return _current; }
  
  /** @return true if the current region is the first in the list, i.e. prevCurrentRegion is without effect */
  public synchronized boolean isCurrentRegionFirst() { return (! _regions.isEmpty()) && _current == _regions.first(); }
  
  /** @return true if the current region is the last in the list, i.e. nextCurrentRegion is without effect */
  public synchronized boolean isCurrentRegionLast() { return (! _regions.isEmpty()) && _current == _regions.last();  }
  
  /** Set the current region. 
    * @param index  the index of the new current region, may be -1. */
  public void setCurrentRegion(BrowserDocumentRegion r) { _current = r; }
  
  /** Make the region that is more recent the current region.  If _current is null, set it to refer to first.
    * @return new current region */
  public synchronized BrowserDocumentRegion nextCurrentRegion(final GlobalEventNotifier notifier) {
//    Utilities.show("nextCurrentRegion called with regions = " + _regions + " and _current = " + _current);
    if (_regions.isEmpty()) return null;
    if (_current == null) _current = _regions.first();
    else _current = _higher(_regions, _current);
    notifier.browserChanged();
    return _current;
  }
  
  /** Make the region that is less recent the current region.  If _current is null, set it to refer to last.
    * @return new current region */
  public synchronized BrowserDocumentRegion prevCurrentRegion(final GlobalEventNotifier notifier) {
//    Utilities.show("prevCurrentRegion called with regions = " + _regions + " and _current = " + _current);
    if (_regions.isEmpty()) return null;
    if (_current == null) _current = _regions.last();
    else _current = _lower(_regions, _current);
//    Utilities.show("returning " + _current);
    notifier.browserChanged();
    return _current;
  }
  
  /** @return the highest region (using compareTo) <= r; if no such region exists, return the lowest region,
    * which is null if _regions is empty. */
  private BrowserDocumentRegion _lower(TreeSet<BrowserDocumentRegion> regions, BrowserDocumentRegion r) {
    if (_regions.isEmpty()) return null;
    BrowserDocumentRegion least = _regions.first();
    SortedSet<BrowserDocumentRegion> lowerSet = regions.subSet(least, r);
//    Utilities.show("Given regions = " + _regions + ", lowerSet = " + lowerSet);
    if (lowerSet.isEmpty()) return _regions.first();
    return lowerSet.last();
  }
  
  /** @return the lowest region (using compareTo) greater than r; if no such region exists, return the highest region,
    * which is null if _regions is empty. */
  private BrowserDocumentRegion _higher(TreeSet<BrowserDocumentRegion> regions, BrowserDocumentRegion r) {
    if (_regions.isEmpty()) return null;
    SortedSet<BrowserDocumentRegion> higherSet = regions.tailSet(r);
    
    Iterator<BrowserDocumentRegion> it = higherSet.iterator();
    while (it.hasNext()) {
      BrowserDocumentRegion result = it.next();
      if (! result.equals(r)) return result;
    }
    return _regions.last();
  }
  
  
  /** Set the maximum number of regions that can be stored in this manager.  If the maximum capacity has been reached
    * and another region is added, the region at the end farther away from the insertion location will be discarded.
    * @param size maximum number of regions, or 0 if no maximum
    */
  public synchronized void setMaximumSize(int size) {
    _maxSize = size;
    
    // remove regions if necessary
    shrinkManager();
  }
  
  /** @return the maximum number of regions that can be stored in this manager. */
  public int getMaximumSize() { return _maxSize; }
  
  /** Apply the given command to the specified region to change it.
    * @param region the region to find and change
    * @param cmd command that mutates the region. */
  public void changeRegion(final BrowserDocumentRegion region, Lambda<Object,BrowserDocumentRegion> cmd) {
    cmd.apply(region);
    Utilities.invokeLater(new Runnable() { public void run() {
      // notify
      _lock.startRead();
      try {
        for (RegionManagerListener<BrowserDocumentRegion> l: _listeners) { l.regionChanged(region); }
      } finally { _lock.endRead(); }            
    } });
  }
}