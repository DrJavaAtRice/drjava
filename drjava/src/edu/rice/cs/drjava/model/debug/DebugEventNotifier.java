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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Keeps track of all listeners to a Debugger, and has the ability
 * to notify them of some event.
 *
 * This class has a specific role of managing DebugListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 *
 * @version $Id$
 */
public class DebugEventNotifier extends EventNotifier<DebugListener>
    implements DebugListener {

  // -------------------- READER METHODS --------------------

  /**
   * Called when debugger mode has been enabled.
   */
  public void debuggerStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).debuggerStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when debugger mode has been disabled.
   */
  public void debuggerShutdown() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).debuggerShutdown();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the given line is reached by the current thread in the
   * debugger, to request that the line be displayed.
   * @param doc Document to display
   * @param lineNumber Line to display or highlight
   * @param shouldHighlight whether to highlight the given line or not
   */
  public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber,
                                    boolean shouldHighlight) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).threadLocationUpdated(doc, lineNumber,
                                                shouldHighlight);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a breakpoint is set in a document.
   * @param bp the breakpoint
   */
  public void breakpointSet(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).breakpointSet(bp);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a breakpoint is reached during execution.
   * @param bp the breakpoint
   */
  public void breakpointReached(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).breakpointReached(bp);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a breakpoint is removed from a document.
   * @param bp the breakpoint
   */
  public void breakpointRemoved(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).breakpointRemoved(bp);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a step is requested on the current thread.
   */
  public void stepRequested() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).stepRequested();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the current thread is suspended
   */
  public void currThreadSuspended() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).currThreadSuspended();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the current thread is resumed
   */
  public void currThreadResumed() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).currThreadResumed();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a thread starts
   */
  public void threadStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).threadStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the current thread dies
   */
  public void currThreadDied() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).currThreadDied();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when any thread other than the current thread dies
   */
  public void nonCurrThreadDied() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).nonCurrThreadDied();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the current (selected) thread is set in the debugger.
   * @param thread the thread that was set as current
   */
  public void currThreadSet(DebugThreadData thread) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).currThreadSet(thread);
      }
    }
    finally {
      _lock.endRead();
    }
  }
}
