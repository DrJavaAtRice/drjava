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

package edu.rice.cs.drjava.model;

import java.util.LinkedList;
import edu.rice.cs.util.ReaderWriterLock;
import edu.rice.cs.util.swing.ScrollableDialog;

/** Base class for all component-specific EventNotifiers.  This class provides common methods to 
 *  manage listeners of a specific type.  T the type of the listener class to be managed.
 *  @version $Id$
 */
public abstract class EventNotifier<T> {

  /** All T Listeners that are listening to the model.
   *  Accesses to this collection are protected by the ReaderWriterLock.
   *  The collection must be synchronized, since multiple readers could access it at once.
   */
  protected final LinkedList<T> _listeners = new LinkedList<T>();

  /** Provides synchronization primitives for solving the readers/writers
   *  problem.  In EventNotifier, adding and removing listeners are considered
   *  write operations, and all notifications are considered read operations.
   *
   *  Multiple reads are allowed simultaneously, but only one write can occur
   *  at a time, and no reads can occur during a write.
   */
  protected final ReaderWriterLock _lock = new ReaderWriterLock();

  /**
   * Adds a listener to the notifier.
   * @param listener a listener that reacts on events
   */
  public void addListener(T listener) {
//    new ScrollableDialog(null, "Grabbing writeLock on event queue", "", "").show();
    _lock.startWrite();
    try { _listeners.add(listener); }
    finally {
      _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
    }
  }

  /** Removes a listener from the notifier.
   *  @param listener a listener that reacts on events
   */
  public void removeListener(T listener) {
//    new ScrollableDialog(null, "Grabbing writeLock on event queue", "", "").show();
    _lock.startWrite();
    try { _listeners.remove(listener); }
    finally {
      _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
    }
  }

  /** Removes all listeners from this notifier.  */
  public void removeAllListeners() {
//    new ScrollableDialog(null, "Grabbing writeLock on event queue", "", "").show();
    _lock.startWrite();
    try { _listeners.clear(); }
    finally {
      _lock.endWrite();
//      new ScrollableDialog(null, "Released writeLock on event queue", "", "").show();
    }
  }
}
