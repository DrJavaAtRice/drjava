/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.avail;

import edu.rice.cs.drjava.model.EventNotifier;
import java.util.HashMap;

/**
 * Keeps track of all listeners to GUI availability.
 * <p>
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 * <p>
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * <p>
 *
 * @version $Id: GUIAvailabilityNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class GUIAvailabilityNotifier extends EventNotifier<GUIAvailabilityListener> implements GUIAvailabilityListener {
  /** The current availabilities of the individual components. */
  protected final HashMap<ComponentType,Integer> _values = new HashMap<ComponentType,Integer>();
 
  // public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("avail.txt",true);
  
  /** Create a new notifier with all components available. */
  public GUIAvailabilityNotifier() {
    for(ComponentType component: ComponentType.values()) {
      _values.put(component, 0);
    }
  }
  
  /** Return true if the component is available.
    * @param component the component to query
    * @return true if available */
  public boolean isAvailable(ComponentType component) {
    return (_values.get(component)==0);
  }

  /** Returns the count for the specified component, where 0 means available, and 1 or greater means
    * unavailable (perhaps nested).
    * @param component the component to query
    * @return count */
  public int getCount(ComponentType component) {
    return _values.get(component);
  }
  
  /** Make sure a component is unavailable, i.e. the count is at least 1. If the count is 0,
    * increase it to 1. If the count is already 1 or greater, than don't do anything.
    * @param component the component that needs to be unavailable */
  public void ensureUnavailable(ComponentType component) {
    if (isAvailable(component)) { availabilityChanged(component, false); }
  }
  
  /** Make a component (more) unavailable. This may be nested.
    * @param component the component that is unavailable */
  public void unavailable(ComponentType component) {
    availabilityChanged(component, false);
  }
  
  /** Make sure a component is available, i.e. the count is 0. If the count is greater than 0,
    * change it to 0. If the count is already 0, than don't do anything.
    * @param component the component that needs to be available */
  public void ensureAvailable(ComponentType component) {
    // LOG.log("ensureAvailable "+component, new RuntimeException());
    if (!isAvailable(component)) {
      _values.put(component, 0);
      notifyListeners(component);
    }
  }
  
  /** Make a component (more) available. This may be nested. If the count of the component
    * is still larger than 0 after this call, the component will remain unavailable.
    * @param component the component that is available */
  public void available(ComponentType component) {
    availabilityChanged(component, true);
  }

  /** Make sure the availability of the component is as specified. If available is true,
    * this calls ensureAvailable; if available is false, it calls ensureUnavailable.
    * @param component the component that needs to be available
    * @param available true to make the component available, false otherwise */
  public void ensureAvailabilityIs(ComponentType component, boolean available) {
    if (available) { ensureAvailable(component); } else { ensureUnavailable(component); }
  }
  
  /** Called to change a components availability. Nested (non-)availability is supported:
    * calling this method with available=false twice will require two calls with available=true
    * to make the component available again.
    * @param component the component whose availability changed
    * @param available true if component is available */  
  public void availabilityChanged(ComponentType component, boolean available) {
    // LOG.log("availabilityChanged "+component+" "+available, new RuntimeException());
    int value = _values.get(component);
    boolean changed = false;
    if (available) {
      // made available, decrement count if >0
      if (value==1) {
        _values.put(component, 0);
        changed = true;
      }
      else if (value>1) {
        _values.put(component, value-1);
      }
    }
    else {
      // made unavailable, increment count
      _values.put(component, value+1);
      changed = true;
    }
    if (changed) { notifyListeners(component); }
  }
  
  /** Notify the listeners for the specified component.
    * @param component the component whose listeners should be notified */
  protected void notifyListeners(ComponentType component) {
    _lock.startRead();
    try { for (GUIAvailabilityListener cl : _listeners) {
      cl.availabilityChanged(component, isAvailable(component));
    } }
    finally { _lock.endRead(); }
  }
}
