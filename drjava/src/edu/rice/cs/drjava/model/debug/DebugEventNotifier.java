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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

import java.awt.EventQueue;

/** Keeps track of all listeners to a Debugger, and has the ability  to notify them of some event.
 *  <p>
 *  This class has a specific role of managing DebugListeners.  Other classes with similar names use similar code to 
 *  perform the same function for other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.  These 
 *  classes implement the appropriate interface definition so that they can be used transparently as composite packaging
 *  for a particular listener interface.
 *  <p>
 *  Components which might otherwise manage their own list of listeners use EventNotifiers instead to simplify their 
 *  internal implementation.  Notifiers should therefore be considered a private implementation detail of the 
 *  components, and should not be used directly outside of the "host" component.
 *  <p>
 *  All methods in this class must use the synchronization methods provided by ReaderWriterLock.  This ensures that
 *  multiple notifications (reads) can occur simultaneously, but only one thread can be adding or removing listeners 
 *  (writing) at a time, and no reads can occur during a write.
 *  <p>
 *  <i>No</i> methods on this class should be synchronized using traditional Java synchronization!
 *  <p>
 *
 * @version $Id: DebugEventNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DebugEventNotifier extends EventNotifier<DebugListener> implements DebugListener {
  
  /** Called when debugger mode has been enabled.  Must be executed in event thread. */
  public void debuggerStarted() {
    assert EventQueue.isDispatchThread();
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
    assert EventQueue.isDispatchThread();
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
    * displayed.  Must be executed only in the event thread.
    * @param doc Document to display
    * @param lineNumber Line to display or highlight
    * @param shouldHighlight true iff the line should be highlighted.
    */
  public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber,  boolean shouldHighlight) {
    assert EventQueue.isDispatchThread();
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
    * @param bp the breakpoint
    */
  public void regionAdded(Breakpoint bp) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) { _listeners.get(i).regionAdded(bp); }
    }
    finally { _lock.endRead(); }
  }

  /** Called when a breakpoint is reached during execution. Must be executed in event thread.
   * @param bp the breakpoint
   */
  public void breakpointReached(Breakpoint bp) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).breakpointReached(bp);
      }
    }
    finally { _lock.endRead(); }
  }

  /** Called when a breakpoint is changed during execution. Must be executed in event thread.
    * @param bp the breakpoint
    */
  public void regionChanged(Breakpoint bp) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).regionChanged(bp);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /** Called when a watch is set.  Must be executed in event thread.
    * @param w the watch
    */
  public void watchSet(DebugWatchData w) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) { _listeners.get(i).watchSet(w); }
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when a watch is removed.  Must be executed in event thread.
    * @param w the watch
    */
  public void watchRemoved(DebugWatchData w) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) { _listeners.get(i).watchRemoved(w); }
    }
    finally { _lock.endRead(); }
  }

  /** Called when a breakpoint is removed from a document.  Must be executed in event thread.
    * @param bp the breakpoint
    */
  public void regionRemoved(Breakpoint bp) {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).regionRemoved(bp);
    }
    finally { _lock.endRead(); }
  }

  /** Called when a step is requested on the current thread. Must be executed in event thread. */
  public void stepRequested() {
    assert EventQueue.isDispatchThread();
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
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).currThreadResumed();
    }
    finally { _lock.endRead(); }
  }

  /** Called when a thread starts. Must be executed in event thread. */
  public void threadStarted() {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).threadStarted();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current thread dies. Must be executed in event thread. */
  public void currThreadDied() {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).currThreadDied();
    }
    finally { _lock.endRead(); }
  }

  /** Called when any thread other than the current thread dies. Must be executed in event thread. */
  public void nonCurrThreadDied() {
    assert EventQueue.isDispatchThread();
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).nonCurrThreadDied();
    }
    finally { _lock.endRead(); }
  }

  /** Called when the current (selected) thread is set in the debugger.
    * @param thread the thread that was set as current
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
