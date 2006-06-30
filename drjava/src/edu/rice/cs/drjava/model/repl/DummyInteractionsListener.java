/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.io.File;

/** A dummy InteractionsListener that does nothing.
 *  @version $Id: InteractionsListener.java 3808 2006-04-19 19:28:49Z jon-lugo $
 */
public class DummyInteractionsListener implements InteractionsListener {
  
  /** Called after an interaction is started by the GlobalModel.  */
  public void interactionStarted() { }

  /** Called when an interaction has finished running. */
  public void interactionEnded() { }
  
  /** Called when the interactions window generates a syntax error.
   *  @param offset the error's offset into the InteractionsDocument
   *  @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) { }

  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() { }
  
  /** Called when the interactions window is reset. */
  public void interpreterReady(File wd) { }

  /** Called when the interactions JVM was closed by System.exit
   *  or by being aborted. Immediately after this the interactions
   *  will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) { }
  
  /** Called if the interpreter reset failed. (Subclasses must maintain listeners.) */
  public void interpreterResetFailed(Throwable t) { }
  
  /** Called when the active interpreter is changed.
   *  @param inProgress Whether the new interpreter is currently processing an interaction (i.e. whether an 
   *  interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) { }

  /** Called when enter was typed in the interactions pane but the interaction was incomplete. */
  public void interactionIncomplete() { }
  
  /** Called when the slave JVM is used for interpretation or unit testing. */
  public void slaveJVMUsed() { }
}

