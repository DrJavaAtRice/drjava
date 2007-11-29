/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import java.util.LinkedList;
import edu.rice.cs.util.ReaderWriterLock;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/** Base class for all component-specific EventNotifiers.  This class provides common methods to 
 *  manage listeners of a specific type.  T the type of the listener class to be managed.
 *  @version $Id$
 */
public abstract class EventNotifier<T> {
  /** All T Listeners that are listening to the model.  Accesses to this collection are protected by the 
   *  ReaderWriterLock. The collection must be synchronized, since multiple readers could access it at once.
   */
  protected final LinkedList<T> _listeners = new LinkedList<T>();

  /** Provides synchronization primitives for solving the readers/writers problem.  In EventNotifier, adding and 
   *  removing listeners are considered write operations, and all notifications are considered read operations. Multiple 
   *  reads can occur simultaneously, but only one write can occur at a time, and no reads can occur during a write.
   */
  protected final ReaderWriterLock _lock = new ReaderWriterLock();
  
  /** Adds a listener to the notifier.
   *  @param listener a listener that reacts on events
   */
  public void addListener(T listener) {
//    Utilities.showDebug("Adding listener " + listener + " to event notifier " + this);
    _lock.startWrite();
    try { _listeners.add(listener); }
    finally {
      _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
    }
  }

  /** Removes a listener from the notifier. If the thread already holds the lock,
    * then the listener is removed later, but as soon as possible.
    * Note: It is NOT guaranteed that the listener will not be executed again.
    * @param listener a listener that reacts on events
    */
  public void removeListener(final T listener) {
//    Utilities.show("writeLock on _listeners grabbed by " + this);
    try {
      _lock.startWrite();
      try { _listeners.remove(listener); }
      finally {
        _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
      }
    }
    catch(ReaderWriterLock.DeadlockException e) {
      // couldn't remove right now because this thread already owns a lock
      // remember to remove it later
      new Thread(new Runnable() {
        public void run() {
          _lock.startWrite();
          try { _listeners.remove(listener); }
          finally { _lock.endWrite(); }
        }
      }, "Pending Listener Removal").start();
//      synchronized(_listenersToRemove) {
//        _listenersToRemove.add(listener);
//      }
    }
  }

  /** Removes all listeners from this notifier.  If the thread already holds the lock,
    * then the listener is removed later, but as soon as possible.
    * Note: It is NOT guaranteed that the listener will not be executed again. */
  public void removeAllListeners() {
//    new ScrollableDialog(null, "Grabbing writeLock on event queue", "", "").show();
    try { 
      _lock.startWrite();
      try { _listeners.clear(); }
      finally {
        _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
      }
    }
    catch(ReaderWriterLock.DeadlockException e) {
      // couldn't remove right now because this thread already owns a lock
      // remember to remove it later
      new Thread(new Runnable() {
        public void run() {
          _lock.startWrite();
          try { _listeners.clear(); }
          finally { _lock.endWrite(); }
        }
      }, "Pending Listener Removal").start();
    }
  }
}
