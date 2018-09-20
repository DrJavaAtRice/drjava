/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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
import java.util.concurrent.TimeoutException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.concurrent.CompletionMonitor;


//TODO: this class should be renamed NullInteractionsListener as should all of the other "dummy" classes.  
/** An InteractionsListener that does nothing other than support a waitUntilDone method that blocks until
  * interpreterReady is called.
  *  @version $Id: DummyInteractionsListener.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DummyInteractionsListener implements InteractionsListener {
  
  private static final int WAIT_TIMEOUT = 10000; // time to wait for _interactionDone or _resetDone 
  public CompletionMonitor _resetDone = new CompletionMonitor();
  
  /** Called after an interaction is started by the GlobalModel.  */
  public void interactionStarted() { }

  /** Called when an interaction has finished running. */
  public void interactionEnded() { }
  
  /** Called when the interactions window generates a syntax error.
   * @param offset the error's offset into the InteractionsDocument
   * @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) { }

  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() { }
  
  /** Called when the interactions window is reset. */
//  public void interpreterReady(File wd) { interpreterReady(); }
  public void interpreterReady() { _resetDone.signal(); } 
  
  /** Called when the interactions JVM was closed by System.exit or by being aborted. Immediately after this the 
    * interactions pane typically is reset.
    * @param status the exit code
    */
  public void interpreterExited(int status) { }
  
  /** Called if the interpreter reset failed. (Subclasses must maintain listeners.) */
  public void interpreterResetFailed(Throwable t) { }
  
//  /** Called when the active interpreter is changed.
//   * @param inProgress Whether the new interpreter is currently processing an interaction (i.e. whether an 
//   * interactionEnded event will be fired)
//   */
//  public void interpreterReplaced() { }

  /** Called when enter was typed in the interactions pane but the interaction was incomplete. */
  public void interactionIncomplete() { }
  
  /* Waits until interpreterReady has been called and resets the completion monitor when that event happens. */
  public void waitResetDone() throws TimeoutException {
    boolean wasDone = _resetDone.attemptEnsureSignaled(WAIT_TIMEOUT);
    if (! wasDone) {
      _resetDone.reset();  // 
      throw new TimeoutException("Interactions pane failed to reset within " + WAIT_TIMEOUT + " milliseconds");
    }
  }
}

