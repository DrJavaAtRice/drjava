/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.io.File;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.concurrent.CompletionMonitor;

/** An extension of InteractionsListener that adds support for a completion monitor accessed by the method 
  * waitResetDone().  The waitResetDone method blocks if interpreterResetting() has been called and interpreterReady() 
  * has not yet been  called since interpreterResetting() was called.  This class is extended by many other listener 
  * classes (both anonymous and named).  The extra method waitResetDone() must NOT run in the event thread in contrast 
  * to all methods in the InteractionsListener interface.
  * @version $Id: DefaultInteractionsListener.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DefaultInteractionsListener implements InteractionsListener {
  
  public static final Log _log = new Log("GlobalModel.txt", false);
  
  private static final int WAIT_TIMEOUT = 20000; // time to wait for _interactionDone or _restartDone 
  private CompletionMonitor _resetDone = new CompletionMonitor(true);
  
  /** Called after an interaction is started by the GlobalModel.  */
  public void interactionStarted() { }

  /** Called when an interaction has finished running. */
  public void interactionEnded() { }
  
  /** Called when the interactions window generates a syntax error.
    * @param offset the error's offset into the InteractionsDocument
    * @param length the length of the error
    */
  public void interactionErrorOccurred(int offset, int length) { }

  /** Activate the completion monitor for a reset. NOTE: this only needs to be done when slave jvm is restarted.*/
  public void interpreterResetting() { 
    _resetDone.raise();
    _log.log("_resetDone.raise() called; after call, _resetDone.isTrue() = " + _resetDone.isTrue() + 
             "; _resetDone = " + _resetDone);
  }
  
  /** Called when the interactions window has been reset. */
  public void interpreterReady() { 
    _resetDone.signal();
    _log.log("In DefaultInteractionsListener.interpreterReady(), _resetDone.signal() called; after call, " +
             "_resetDone.isTrue() = " + _resetDone.isTrue() + "; _resetDone = " + _resetDone);
  } 
  
  /** Called when the interactions JVM was closed by System.exit or by being aborted. Immediately after this the 
    * interactions pane typically is reset.
    * @param status the exit code
    */
  public void interpreterExited(int status) { }
  
  /** Called if the interpreter reset failed. (Subclasses must maintain listeners.) */
  public void interpreterResetFailed(Throwable t) { }
  
  /** Called when enter was typed in the interactions pane but the interaction was incomplete. */
  public void interactionIncomplete() { }
  
  /** Extra method not in InteractionsListener interface */
  
  /** Waits until flag in _resetDone is true.  If flag has been raised (set to false), this means waiting until 
   * interpreterReady has been called.  Resets the CompletionMonitor flag to true. */
  public void waitResetDone() {
    _log.log("In DefaultInteractionsListener.waitRestDone, waiting for _resetDone flag to be true or signalled; " +
             "_resetDone = " + _resetDone);
    boolean wasDone = _resetDone.attemptEnsureSignaled(WAIT_TIMEOUT);  // resets the flag to true
    _log.log("Did signal occur in DefaultInteractionsListener? " + wasDone);
    if (! wasDone) {
      throw new UnexpectedException("Interactions pane failed to reset within " + WAIT_TIMEOUT + " milliseconds");
    }
    _log.log("waiting in DefaultInteractionsListener is complete");
  }
}

