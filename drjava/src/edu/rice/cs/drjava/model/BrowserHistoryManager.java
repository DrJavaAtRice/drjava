/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.awt.EventQueue;

import java.util.*;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.util.swing.Utilities;

/** Browser history manager for the entire model.  Follows readers/writers locking protocol of EventNotifier. */
public class BrowserHistoryManager extends EventNotifier<RegionManagerListener<BrowserDocumentRegion>> {
  
  /** Two regions are similar if they are in the same document and not more than DIFF_THRESHOLD lines apart. */
  public static final int DIFF_THRESHOLD = 0;

  /** List of regions.  _pastRegions is a stack of the past regions (browse backward) with current region on top.
    *                   _futureRegions is a stack of the future (browse forward) regions to be explored when the
    *                      current region "pointer" is advanced.  The top element of this stack is the first
    *                      future region. 
    * Both lists can be empty before any document is viewed.  ?? */
  
  private volatile ArrayDeque<BrowserDocumentRegion> _pastRegions = new ArrayDeque<BrowserDocumentRegion>();
  private volatile ArrayDeque<BrowserDocumentRegion> _futureRegions = new ArrayDeque<BrowserDocumentRegion>();
  
  private volatile int _maxSize;
  
  /** Create a new ConcreteRegionManager with the specified maximum size.
    * @param size maximum number of regions that can be stored in this manager.
    */
  public BrowserHistoryManager(int size) { _maxSize = size; }
  
  /** Create a new ConcreteRegionManager without maximum size. */
  public BrowserHistoryManager() { this(0); }
  
  /** Add the supplied BrowserDocumentRegion r to the manager as current region, i.e. the top element of the
    * _pastRegions stack
    * @param r the BrowserDocumentRegion to be inserted into the manager 
    * @param notifier a GlobalEventNotifier
    */
  public void addBrowserRegion(final BrowserDocumentRegion r, final GlobalEventNotifier notifier) { 
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();

    final BrowserDocumentRegion current = getCurrentRegion();
    if (current != null && r.equiv(current)) return;  // return if region to be added is duplicate
    else {
      _pastRegions.push(r);
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
    }
    notifier.browserChanged();
  }
  
  /** Add the supplied StaticDocumentRegion r to the manager before the current region.
    * @param r the StaticDocumentRegion to be inserted into the manager
    * @param notifier a GlobalEventNotifier
    */
  public void addBrowserRegionBefore(final BrowserDocumentRegion r, final GlobalEventNotifier notifier) { 
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();

    final BrowserDocumentRegion current = getCurrentRegion();
    if ((current != null) && (r.equiv(current))) return;  
    
    else {
      if (_pastRegions.size() == 0) {
        _pastRegions.push(r);
      }
      else {
        _futureRegions.push(_pastRegions.pop());
        _pastRegions.push(r);
      }
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
    }
    notifier.browserChanged();
  }
  
  /** Remove regions if there are more than the maximum number allowed. Typically used to remove one region. */
  private void shrinkManager() {
    // Do nothing if maxsize is non-positive (typically zero)
    if (_maxSize <= 0) return;
    int size = _pastRegions.size() + _futureRegions.size();
    int diff = size - _maxSize;
    for (int i = 0; i < diff; ++i) {
      // always remove the element farthest away in the larger stack
      ArrayDeque<BrowserDocumentRegion> targetStack = 
        (_pastRegions.size() > _futureRegions.size()) ? _pastRegions : _futureRegions;
      if (! targetStack.isEmpty()) removeLast(targetStack);
    }
  }
  
  /** Remove the last region from the given stack and clean up.
    * @param stack the non-empty ArrayDeque<BrowserDocumentRegion>() to be shrunk.
    */
  public void removeLast(final ArrayDeque<BrowserDocumentRegion> stack) {
    assert ! stack.isEmpty();
    final BrowserDocumentRegion r = stack.getLast();  // r is region to be removed
    stack.removeLast();  // removes r from stack
    
    OpenDefinitionsDocument doc = r.getDocument();
    doc.removeBrowserRegion(r);  // removes r from its corresponding document
    
    // Notify listeners of this event
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _lock.startRead();
        try { for (RegionManagerListener<BrowserDocumentRegion> l: _listeners) { l.regionRemoved(r); } } 
        finally { _lock.endRead(); }
      } 
    });
  }
  
  /** Remove the given BrowserDocumentRegion from the manager and clean up.
    * @param r  the BrowserDocumentRegion to be removed.
    */
  public void remove(final BrowserDocumentRegion r) {
    OpenDefinitionsDocument doc = r.getDocument();
    if (!_pastRegions.remove(r)) _futureRegions.remove(r);
    
    doc.removeBrowserRegion(r);
    
    // Notify listeners of this event
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _lock.startRead();
        try { for (RegionManagerListener<BrowserDocumentRegion> l: _listeners) { l.regionRemoved(r); } } 
        finally { _lock.endRead(); }
      } 
    });
  }
  
  /** @return a {@code RegionSet>} containing the StaticDocumentRegion objects in this mangager. */
  public RegionSet<BrowserDocumentRegion> getRegions() {
    RegionSet<BrowserDocumentRegion> rs = new RegionSet<BrowserDocumentRegion>();
    rs.addAll(_pastRegions);
    rs.addAll(_futureRegions);
    return rs;
  }
  
  /** Tells the manager to remove all regions from past and future stacks. */
  public void clearBrowserRegions() {
    if (_futureRegions != null) while (! _futureRegions.isEmpty()) removeLast(_futureRegions);
    if (_pastRegions != null) while (! _pastRegions.isEmpty()) removeLast(_pastRegions);
  }
  
  /** @return the current region or null if none selected */
  public BrowserDocumentRegion getCurrentRegion() { return _pastRegions.peek(); } // peek() returns null on empty
  
  /** @return true if the current region is null or the first in _pastRegions, i.e., prevCurrentRegion is without effect */
  public boolean isCurrentRegionFirst() { return (_pastRegions.size() <= 1); }
  
  /** @return true if the current region is the last in the list, i.e., nextCurrentRegion is without effect */
  public boolean isCurrentRegionLast() { return (_futureRegions.isEmpty()); }
  
  /** Move the region cursor forward.
    * @param notifier a GlobalEventNotifier
    * @return new current region 
    */
  public BrowserDocumentRegion nextCurrentRegion(final GlobalEventNotifier notifier) {
    if (isCurrentRegionLast()) return null; // currentRegion may be null
    BrowserDocumentRegion r = _futureRegions.pop();  // pre: _futureRegions has at least 1 element
    _pastRegions.push(r);                            // post: _pastRegions has at least two elements
    notifier.browserChanged();
    return r;
  }
  
  /** Move the region cursor backward if possible
    * @return new current region 
    * @param notifier a GlobalEventNotifier
    */
  public BrowserDocumentRegion prevCurrentRegion(final GlobalEventNotifier notifier) {
    if (isCurrentRegionFirst()) return null;  // currentRegion may be null
    BrowserDocumentRegion r = _pastRegions.pop();  // post: _pastRegions has at least 1 element
    _futureRegions.push(r);                        // post: _futureRegions has at least 1 element
    notifier.browserChanged();
    return _pastRegions.getFirst();                // ! _pastRegions.isEmpty()
  }  
  
  /** Set the maximum number of regions that can be stored in this manager.
    * @param size maximum number of regions, or 0 if no maximum
    */
  public void setMaximumSize(int size) {
    _maxSize = size;
    shrinkManager(); // remove regions if necessary
  }
  
  /** @return the maximum number of regions that can be stored in this manager. */
  public int getMaximumSize() { return _maxSize; }
  
  /** Apply the given command to the specified region to change it.
    * @param region the region to find and change
    * @param cmd command that mutates the region. */
  public void changeRegion(final BrowserDocumentRegion region, Lambda<BrowserDocumentRegion,Object> cmd) {
    cmd.value(region);
    Utilities.invokeLater(new Runnable() { public void run() {
      // notify
      _lock.startRead();
      try {
        for (RegionManagerListener<BrowserDocumentRegion> l: _listeners) { l.regionChanged(region); }
      } finally { _lock.endRead(); }            
    } });
  }
  
  /** @param r1 the first region to compare
   * @param r2 the second region to compare
   * @return true if the two regions are similar. 
   */
  public static boolean similarRegions(BrowserDocumentRegion r1, BrowserDocumentRegion r2) {
    OpenDefinitionsDocument d = r1.getDocument();
    if (d!=r2.getDocument()) return false;
    int l1 = d.getLineOfOffset(r1.getStartOffset());
    int l2 = d.getLineOfOffset(r2.getStartOffset());
    return (Math.abs(l1-l2) <= DIFF_THRESHOLD);
  }
  
  public String toString() {
    ArrayList<BrowserDocumentRegion> future = new ArrayList<BrowserDocumentRegion>(_futureRegions);
    Collections.reverse(future);
    return "Past: " + _pastRegions.toString() + ", Future: " + future.toString();
  }
}
