/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ListIterator;
import java.util.LinkedList;

public class DelegatingAction implements Action {
  private static final String[] KEYS = {
    DEFAULT,
    NAME,
    SHORT_DESCRIPTION,
    LONG_DESCRIPTION,
    SMALL_ICON,
    ACTION_COMMAND_KEY,
    ACCELERATOR_KEY,
    MNEMONIC_KEY,
  };
  
  /**
   * The action to delegate to. If it's null, this action is
   * disabled and all method calls will result in IllegalStateExceptions.
   */
  private Action _delegatee;
  private final LinkedList _listenerList = new LinkedList();

  public Object getValue(String key) {
    _checkState();
    return _delegatee.getValue(key);
  }

  public void putValue(String key, Object value) {
    _checkState();
    _delegatee.putValue(key, value);
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
    Boolean enabled = newDelegatee.isEnabled() ? Boolean.TRUE : Boolean.FALSE;

    PropertyChangeEvent enabledEvent
      =new PropertyChangeEvent(newDelegatee, "enabled", Boolean.FALSE, enabled);

    PropertyChangeEvent[] events = null;

    if (_delegatee != null) {
      events = new PropertyChangeEvent[KEYS.length];

      for (int i = 0; i < KEYS.length; i++) {
        Object oldValue = _delegatee.getValue(KEYS[i]);
        Object newValue = newDelegatee.getValue(KEYS[i]);

        events[i] = new PropertyChangeEvent(newDelegatee,
                                            KEYS[i],
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


