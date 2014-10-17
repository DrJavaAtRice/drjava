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

/** A dummy InteractionsListener that does nothing.
 *  @version $Id: DummyInteractionsListener.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DummyInteractionsListener implements InteractionsListener {
  
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
  public void interpreterReady(File wd) { }

  /** Called when the interactions JVM was closed by System.exit
   * or by being aborted. Immediately after this the interactions
   * will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) { }
  
  /** Called if the interpreter reset failed. (Subclasses must maintain listeners.) */
  public void interpreterResetFailed(Throwable t) { }
  
  /** Called when the active interpreter is changed.
   * @param inProgress Whether the new interpreter is currently processing an interaction (i.e. whether an 
   * interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) { }

  /** Called when enter was typed in the interactions pane but the interaction was incomplete. */
  public void interactionIncomplete() { }
  
}

