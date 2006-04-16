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

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.LinkedList;

public class DelegatingAction implements Action {
  /**
   * These keys will be copied from the delegatee. All other keys
   * are held in this action itself.
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

  /**
   * Can't use a more specific type parameter because of Action interface.
   */
  private HashMap<String, Object> _localProperties = new HashMap<String, Object>();

  /**
   * The action to delegate to. If it's null, this action is
   * disabled and all method calls will result in IllegalStateExceptions.
   */
  private Action _delegatee;
  private final LinkedList<PropertyChangeListener> _listenerList =
    new LinkedList<PropertyChangeListener>();

  /**
   * Returns value of the key, from delegatee is it's in {@link #KEYS_TO_DELEGATE}
   * or from this if not.
   */
  public Object getValue(String key) {
    _checkState();

    if (_isDelegatedKey(key)) {
      return _delegatee.getValue(key);
    }
    else {
      return _localProperties.get(key);
    }
  }

  private boolean _isDelegatedKey(String key) {
    for (int i = 0; i < KEYS_TO_DELEGATE.length; i++) {
      if (KEYS_TO_DELEGATE[i].equals(key)) {
        return true;
      }
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
      ListIterator itor = _listenerList.listIterator();

      PropertyChangeEvent event = new PropertyChangeEvent(this, key, old, value);

      while (itor.hasNext()) {
        PropertyChangeListener listener = (PropertyChangeListener) itor.next();
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

  public void setDelegatee(final Action newDelegatee) {
    if (newDelegatee == null) {
      throw new IllegalArgumentException("setDelegatee(null) is not allowed!");
    }

    // create property change notifications
    Boolean isEnabled = newDelegatee.isEnabled() ? Boolean.TRUE : Boolean.FALSE;

    PropertyChangeEvent enabledEvent
      =new PropertyChangeEvent(newDelegatee, "enabled", Boolean.FALSE, isEnabled);

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
    ListIterator itor = _listenerList.listIterator();
    while (itor.hasNext()) {
      PropertyChangeListener listener = (PropertyChangeListener) itor.next();

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


