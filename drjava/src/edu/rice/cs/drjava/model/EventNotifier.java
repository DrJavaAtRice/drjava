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

package edu.rice.cs.drjava.model;

import gj.util.Vector;
import gj.util.Enumeration;

/**
 * Keeps track of all listeners to the model, and has the ability
 * to notify them of some event.
 * @version $Id$
 */
public class EventNotifier {
  /**
   * All GlobalModelListeners that are listening to the model.
   * All accesses should be synchronized on this field.
   */
  private final Vector<GlobalModelListener> _listeners;
  
  /**
   * Creates a new EventNotifier with an empty list of listeners.
   */
  public EventNotifier() {
    _listeners = new Vector<GlobalModelListener>();
  }
  
  /**
   * Add a listener to the model.
   * @param listener a listener that reacts on events
   */
  public void addListener(GlobalModelListener listener) {
    synchronized(_listeners) {
      _listeners.addElement(listener);
    }
  }

  /**
   * Remove a listener from the model.
   * @param listener a listener that reacts on events
   */
  public void removeListener(GlobalModelListener listener) {
    synchronized(_listeners) {
      _listeners.removeElement(listener);
    }
  }

  /**
   * Removes all listeners from this notifier.
   */
  public void removeAllListeners() {
    synchronized(_listeners) {
      _listeners.removeAllElements();
    }
  }
  
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  public void notifyListeners(Notifier n) {
    synchronized(_listeners) {
      Enumeration<GlobalModelListener> i = _listeners.elements();

      while(i.hasMoreElements()) {
        GlobalModelListener cur = i.nextElement();
        n.notifyListener(cur);
      }
    }
  }
  
  /**
   * Allows the GlobalModel to ask its listeners a yes/no question and
   * receive a response.
   * @param EventPoller p the question being asked of the listeners
   * @return the listeners' responses ANDed together, true if they all
   * agree, false if some disagree
   */
  public boolean pollListeners(Poller p) {
    synchronized(_listeners) {
      Enumeration<GlobalModelListener> i = _listeners.elements();
      boolean poll = true;
      
      while(i.hasMoreElements()) {
        GlobalModelListener cur = i.nextElement();
        poll = poll && p.poll(cur);
      }
      return poll;
    }
  }

  /**
   * Class model for notifying listeners of an event.
   */
  public abstract static class Notifier {
    public abstract void notifyListener(GlobalModelListener l);
  }

  /**
   * Class model for asking listeners a yes/no question.
   */
  public abstract static class Poller {
    public abstract boolean poll(GlobalModelListener l);
  }
}