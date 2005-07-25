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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Keeps track of all listeners to a Debugger, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing DebugListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 * <p>
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
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
 * @version $Id$
 */
public class DebugEventNotifier extends EventNotifier<DebugListener> implements DebugListener {
  
  /** Called when debugger mode has been enabled.  Must be executed in event thread. */
  public void debuggerStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).debuggerStarted();
      }
    }
    finally { _lock.endRead(); }
  }

  /** Called when debugger mode has been disabled.  Must be executed in event thread. */
  public void debuggerShutdown() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).debuggerShutdown();
      }
    }
    finally { _lock.endRead(); }
  }

  /** Called when the given line is reached by the current thread in the debugger, to request that the line be 
    *  displayed.  Must be executed only in the event thread.
    *  @param doc Document to display
    *  @param lineNumber Line to display or highlight
    *  @param shouldHighlight true iff the line should be highlighted.
    */
  public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber,  boolean shouldHighlight) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).threadLocationUpdated(doc, lineNumber, shouldHighlight);
      }
    }
    finally { _lock.endRead(); }
  }

  /** Called when a breakpoint is set in a document.  Must be executed in event thread.
   *  @param bp the breakpoint
   */
  public void breakpointSet(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) { _listeners.get(i).breakpointSet(bp); }
    }
    finally { _lock.endRead(); }
  }

  /** Called when a breakpoint is reached during execution. Must be executed in event thread.
   * @param bp the breakpoint
   */
  public void breakpointReached(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).breakpointReached(bp);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /** Called when a breakpoint is removed from a document.  Must be executed in event thread.
   *  @param bp the breakpoint
   */
  public void breakpointRemoved(Breakpoint bp) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).breakpointRemoved(bp);
    }
    finally { _lock.endRead(); }
  }

  /** Called when a step is requested on the current thread. Must be executed in event thread. */
  public void stepRequested() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).stepRequested();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current thread is suspended.  */
  public void currThreadSuspended() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).currThreadSuspended();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current thread is resumed.  Must be executed in event thread. */
  public void currThreadResumed() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).currThreadResumed();
    }
    finally { _lock.endRead(); }
  }

  /** Called when a thread starts. Must be executed in event thread. */
  public void threadStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).threadStarted();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current thread dies. Must be executed in event thread. */
  public void currThreadDied() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).currThreadDied();
    }
    finally { _lock.endRead(); }
  }

  /** Called when any thread other than the current thread dies. Must be executed in event thread. */
  public void nonCurrThreadDied() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).nonCurrThreadDied();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current (selected) thread is set in the debugger.
   *  @param thread the thread that was set as current
   */
  public void currThreadSet(DebugThreadData thread) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).currThreadSet(thread);
      }
    }
    finally { _lock.endRead(); }
  }
}
