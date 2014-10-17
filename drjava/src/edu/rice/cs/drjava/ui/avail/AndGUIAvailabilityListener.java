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
import java.util.HashSet;

/**
 * Listener responding to the availability of several GUI components.
 *
 * @version $Id: AndGUIAvailabilityListener.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public abstract class AndGUIAvailabilityListener implements GUIAvailabilityListener {
  protected final HashSet<ComponentType> _components = new HashSet<ComponentType>();
  protected final GUIAvailabilityNotifier _notifier;
  protected volatile boolean _lastValue = true;
  
  /** Create a listener that responds to changes in availability of several GUI components.
    * @param components components that must be available */
  public AndGUIAvailabilityListener(GUIAvailabilityNotifier notifier, ComponentType... components) {
    _notifier = notifier;
    for(ComponentType c: components) {
      _components.add(c);
      _lastValue &= _notifier.isAvailable(c);
    }
  }
  
  /** @return true if all required components are available */
  public boolean isAvailable() {
    for(ComponentType c: _components) {
      if (!_notifier.isAvailable(c)) return false;
    }
    return true;
  }
  
  /** Called when a component's availability changes.
    * @param component the component whose availability changed
    * @param available true if component is available */  
  public void availabilityChanged(ComponentType component, boolean available) {
    if (_components.contains(component)) {
      boolean newValue = isAvailable();
      if (_lastValue != newValue) {
        _lastValue = newValue;
        availabilityChanged(newValue);
      }
    }
  }
  
  /** Called when the combined availability of all components changes.
    * @param available true if all components are available */  
  public abstract void availabilityChanged(boolean available);
}
