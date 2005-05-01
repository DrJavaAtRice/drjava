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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.EventNotifier;

/**
 * Keeps track of all listeners to an InteractionsModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing InteractionsListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. JavadocEventNotifier and GlobalEventNotifier.
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
public class InteractionsEventNotifier extends
    EventNotifier<InteractionsListener> implements InteractionsListener {

  /**
   * Called after an interaction is started by the GlobalModel.
   */
  public void interactionStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interactionStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when an interaction has finished running.
   */
  public void interactionEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interactionEnded();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the interactions window generates a syntax error.
   *
   * @param offset the error's offset into the InteractionsDocument
   * @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interactionErrorOccurred(offset, length);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the interactionsJVM has begun resetting.
   */
  public void interpreterResetting() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interpreterResetting();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the interactions window is reset.
   */
  public void interpreterReady() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interpreterReady();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called if the interpreter reset failed.
   * @param t Throwable explaining why the reset failed.
   * (Subclasses must maintain listeners.)
   */
  public void interpreterResetFailed(final Throwable t) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interpreterResetFailed(t);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the interactions JVM was closed by System.exit
   * or by being aborted. Immediately after this the interactions
   * will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interpreterExited(status);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when the active interpreter is changed.
   * @param inProgress Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interpreterChanged(inProgress);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Notifies the view that the current interaction is incomplete.
   */
  public void interactionIncomplete() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for (int i = 0; i < size; i++) {
        _listeners.get(i).interactionIncomplete();
      }
    }
    finally {
      _lock.endRead();
    }
  }
}
