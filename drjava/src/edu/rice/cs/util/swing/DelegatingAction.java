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

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
import java.util.HashMap;
import java.util.LinkedList;

public class DelegatingAction implements Action {
  /** These keys will be copied from the delegatee. All other keys are held in this action itself.
   */
  private static final String[] KEYS_TO_DELEGATE = {
    //DEFAULT,
    NAME,
    //SHORT_DESCRIPTION,
    //LONG_DESCRIPTION,
    //SMALL_ICON,
    //ACTION_COMMAND_KEY,
    //ACCELERATOR_KEY,
    //MNEMONIC_KEY,
  };

  /** Can't use a more specific type parameter because of Action interface. */
  private HashMap<String, Object> _localProperties = new HashMap<String, Object>();

  /** The action to delegate to. If it's null, this action is disabled and all method calls will result in 
    * IllegalStateExceptions.
    */
  private volatile Action _delegatee;
  private final LinkedList<PropertyChangeListener> _listenerList =
    new LinkedList<PropertyChangeListener>();

  /** Returns value of the key, from delegatee is it's in {@link #KEYS_TO_DELEGATE} or from this if not. */
  public Object getValue(String key) {
    _checkState();
    
    if (_isDelegatedKey(key)) return _delegatee.getValue(key);
    else return _localProperties.get(key);
  }

  private boolean _isDelegatedKey(String key) {
    for (int i = 0; i < KEYS_TO_DELEGATE.length; i++) {
      if (KEYS_TO_DELEGATE[i].equals(key)) return true;
    }
    return false;
  }

  public void putValue(String key, Object value) {
    _checkState();

    if (_isDelegatedKey(key)) {
      _delegatee.putValue(key, value);
    }
    else {
      Object old = _localProperties.get(key);
      _localProperties.put(key, value);

      PropertyChangeEvent event = new PropertyChangeEvent(this, key, old, value);
      for (PropertyChangeListener listener : _listenerList) {
        listener.propertyChange(event);
      }
    }
  }

  public void setEnabled(boolean b) {
    _checkState();
    _delegatee.setEnabled(b);
  }

  public boolean isEnabled() {
    _checkState();
    return _delegatee.isEnabled();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    _checkState();
    _delegatee.addPropertyChangeListener(listener);
    _listenerList.add(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    _checkState();
    _delegatee.removePropertyChangeListener(listener);
    _listenerList.remove(listener);
  }

  public void actionPerformed(ActionEvent ae) {
    _checkState();
    _delegatee.actionPerformed(ae);
  }

  public Action getDelegatee() { return _delegatee; }
    
  public void setDelegatee(final Action newDelegatee) {
    if (newDelegatee == null) {
      throw new IllegalArgumentException("setDelegatee(null) is not allowed!");
    }
    
    // create property change notifications
    boolean isEnabled = newDelegatee.isEnabled();

    PropertyChangeEvent enabledEvent
       = new PropertyChangeEvent(newDelegatee, "enabled", Boolean.FALSE, isEnabled);

    PropertyChangeEvent[] events = null;

    if (_delegatee != null) {
      events = new PropertyChangeEvent[KEYS_TO_DELEGATE.length];

      for (int i = 0; i < KEYS_TO_DELEGATE.length; i++) {
        Object oldValue = _delegatee.getValue(KEYS_TO_DELEGATE[i]);
        Object newValue = newDelegatee.getValue(KEYS_TO_DELEGATE[i]);

        events[i] = new PropertyChangeEvent(newDelegatee,
                                            KEYS_TO_DELEGATE[i],
                                            oldValue,
                                            newValue);
      }
    }

    // remove listeners from old and add to new
    for (PropertyChangeListener listener : _listenerList) {
      if (_delegatee != null) {
        _delegatee.removePropertyChangeListener(listener);
      }

      newDelegatee.addPropertyChangeListener(listener);

      // fire property change events for all properties

      if (events != null) {
        for (int i = 0; i < events.length; i++) {
          listener.propertyChange(events[i]);
        }
      }

      listener.propertyChange(enabledEvent);
    }

    _delegatee = newDelegatee;
  }

  private void _checkState() {
    if (_delegatee == null) {
      throw new IllegalStateException("delegatee is null!");
    }
  }
}


