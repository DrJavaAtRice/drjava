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

import java.util.*;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.util.swing.Utilities;

/** Browser history manager for the entire model.  Follows readers/writers locking protocol of EventNotifier.
  */
public class BrowserHistoryManager extends EventNotifier<RegionManagerListener<BrowserDocumentRegion>> {
  /** Two regions are similar if they are in the same document and not more than DIFF_THRESHOLD lines apart. */
  public static final int DIFF_THRESHOLD = 5;

  /** List of regions. */
  private volatile Stack<BrowserDocumentRegion> _pastRegions = new Stack<BrowserDocumentRegion>();
  private volatile Stack<BrowserDocumentRegion> _futureRegions = new Stack<BrowserDocumentRegion>();
  
  private volatile int _maxSize;
  
  /** Create a new ConcreteRegionManager with the specified maximum size.
    * @param size maximum number of regions that can be stored in this manager.
    */
  public BrowserHistoryManager(int size) { _maxSize = size; }
  
  /** Create a new ConcreteRegionManager without maximum size. */
  public BrowserHistoryManager() { this(0); }
  
  /** Add the supplied DocumentRegion r to the manager as current region.
    * @param r the DocumentRegion to be inserted into the manager */
  public void addBrowserRegion(final BrowserDocumentRegion r, final GlobalEventNotifier notifier) { 
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();

    final BrowserDocumentRegion current = getCurrentRegion();
    if ((current != null) && (similarRegions(current, r))) {
      // the region to be added is similar to the current region
      // just update the current region
//      edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("Updating instead of adding: " + current + " --> " + r);
      current.update(r);
    }
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
  
  /** Add the supplied DocumentRegion r to the manager before the current region.
    * @param r the DocumentRegion to be inserted into the manager */
  public void addBrowserRegionBefore(final BrowserDocumentRegion r, final GlobalEventNotifier notifier) { 
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();

    final BrowserDocumentRegion current = getCurrentRegion();
    if ((current != null) && (similarRegions(current, r))) {
      // the region to be added is similar to the current region
      // just update the current region
//      edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("Updating instead of adding: " + current + " --> " + r);
      current.update(r);
    }
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
    if (_maxSize > 0) {
      int size = _pastRegions.size() + _futureRegions.size();
      int diff = size - _maxSize;
      for (int i = 0; i < diff; ++i) {
        // always remove the element farthest away from the larger stack
        remove(((_pastRegions.size()>_futureRegions.size())?_pastRegions:_futureRegions).get(0));
      }
    }
  }
  
  /** Remove the given DocumentRegion from the manager.
    * @param r  the DocumentRegion to be removed.
    */
  public /* synchronized */ void remove(final BrowserDocumentRegion r) {
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
  
  /** @return a SortedSet<BrowserDocumentRegion> containing the DocumentRegion objects in this mangager. */
  public SortedSet<BrowserDocumentRegion> getRegions() {
    TreeSet<BrowserDocumentRegion> ts = new TreeSet<BrowserDocumentRegion>(_pastRegions);
    ts.addAll(_futureRegions);
    return ts;
  }
  
  /** Tells the manager to remove all regions. */
  public /* synchronized */ void clearBrowserRegions() {
    while(_pastRegions.size()+_futureRegions.size() > 0) {
      remove(((_pastRegions.size()>_futureRegions.size())?_pastRegions:_futureRegions).get(0));
    }
  }
  
  /** @return the current region or null if none selected */
  public BrowserDocumentRegion getCurrentRegion() {
    if (_pastRegions.isEmpty()) return null;
    return _pastRegions.peek();
  }
  
  /** @return true if the current region is the first in the list, i.e. prevCurrentRegion is without effect */
  public /* synchronized */ boolean isCurrentRegionFirst() { return (_pastRegions.size()<2); }
  
  /** @return true if the current region is the last in the list, i.e. nextCurrentRegion is without effect */
  public /* synchronized */ boolean isCurrentRegionLast() { return (_futureRegions.size()<1); }
  
  /** Make the region that is more recent the current region.
    * @return new current region */
  public /* synchronized */ BrowserDocumentRegion nextCurrentRegion(final GlobalEventNotifier notifier) {
    if (isCurrentRegionLast()) return null;
    _pastRegions.push(_futureRegions.pop());
    notifier.browserChanged();
    return _pastRegions.peek();
  }
  
  /** Make the region that is less recent the current region.
    * @return new current region */
  public /* synchronized */ BrowserDocumentRegion prevCurrentRegion(final GlobalEventNotifier notifier) {
    if (isCurrentRegionFirst()) return null;
    _futureRegions.push(_pastRegions.pop());
    notifier.browserChanged();
    return _pastRegions.peek();
  }  
  
  /** Set the maximum number of regions that can be stored in this manager.
    * @param size maximum number of regions, or 0 if no maximum
    */
  public /* synchronized */ void setMaximumSize(int size) {
    _maxSize = size;
    
    // remove regions if necessary
    shrinkManager();
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
  
  /** Return true if the two regions are similar. */
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