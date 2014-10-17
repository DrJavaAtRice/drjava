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

package edu.rice.cs.drjava.model.repl;

import java.io.File;

import edu.rice.cs.drjava.model.EventNotifier;

/** Keeps track of all listeners to an InteractionsModel, and has the ability to notify them of some event. <p>
  * This class has a specific role of managing InteractionsListeners.  Other classes with similar names use similar 
  * code to perform the same function for other interfaces, e.g. JavadocEventNotifier and GlobalEventNotifier.
  * These classes implement the appropriate interface definition so that they can be used transparently as composite 
  * packaging for a particular listener interface. <p>
  * Components which might otherwise manage their own list of listeners use EventNotifiers instead to simplify their 
  * internal implementation.  Notifiers should therefore be considered a private implementation detail of the
  * components, and should not be used directly outside of the "host" component. <p>
  * All methods in this class must use the synchronization methods provided by ReaderWriterLock.  This ensures 
  * that multiple notifications (reads) can occur simultaneously, but only one thread can be adding or removing 
  * listeners (writing) at a time, and no reads can occur during a write. <p>
  * <i>No</i> methods on this class should be synchronized using traditional Java synchronization! <p>
  * @version $Id: InteractionsEventNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
  */

public class InteractionsEventNotifier extends EventNotifier<InteractionsListener> implements InteractionsListener {
  
  /** Called after an interaction is started by the GlobalModel. */
  public void interactionStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++)  _listeners.get(i).interactionStarted();
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when an interaction has finished running. */
  public void interactionEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).interactionEnded();
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions window generates a syntax error.
    * @param offset the error's offset into the InteractionsDocument
    * @param length the length of the error
    */
  public void interactionErrorOccurred(int offset, int length) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).interactionErrorOccurred(offset, length);
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() {
    _lock.startRead();
    try {
      int size = _listeners.size();
//      Utilities.showDebug("InteractionsEventNotifier: interpreterResetting called on " + size + " listeners");
      for (int i = 0; i < size; i++) _listeners.get(i).interpreterResetting();
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions window is reset. */
  public void interpreterReady(File wd) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) _listeners.get(i).interpreterReady(wd);
    }
    finally { _lock.endRead(); }
  }
  
  /** Called if the interpreter reset failed.
    * @param t Throwable explaining why the reset failed. (Subclasses must maintain listeners.)
    */
  public void interpreterResetFailed(final Throwable t) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++)  _listeners.get(i).interpreterResetFailed(t);
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions JVM was closed by System.exit or by being aborted. Immediately after this the 
    * interactions will be reset.
    * @param status the exit code
    */
  public void interpreterExited(int status) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) { _listeners.get(i).interpreterExited(status); }
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the active interpreter is changed.
    * @param inProgress Whether the new interpreter is currently in progress with an interaction (ie. whether an 
    * interactionEnded event will be fired)
    */
  public void interpreterChanged(boolean inProgress) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++)  _listeners.get(i).interpreterChanged(inProgress);
    }
    finally { _lock.endRead(); }
  }
  
  /** Notifies the view that the current interaction is incomplete. */
  public void interactionIncomplete() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++)  _listeners.get(i).interactionIncomplete();
    }
    finally { _lock.endRead(); }
  }
  
}
