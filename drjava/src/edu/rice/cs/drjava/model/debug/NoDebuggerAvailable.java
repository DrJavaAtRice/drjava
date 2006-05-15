/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.drjava.model.debug;

import java.util.Vector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Placeholder class indicating that no debugger is available to DrJava.
 * This class follows the Singleton pattern.
 * @version $Id$
 */
public class NoDebuggerAvailable implements Debugger {
  
  /** Singleton instance of this class. */
  public static final NoDebuggerAvailable ONLY = new NoDebuggerAvailable();

  /** Private constructor: use the ONLY field. */
  private NoDebuggerAvailable() { }

  /** Returns whether the debugger is currently available in this JVM. This does not indicate whether it is ready to
   *  be used.
   */
  public boolean isAvailable() { return false; }

  /** Attaches the debugger to the Interactions JVM to prepare for debugging. */
  public void startup() throws DebugException { }

  /** Disconnects the debugger from the Interactions JVM and cleans up any state. */
  public void shutdown() { }

  /** Returns the status of the debugger. */
  public boolean isReady() { return false; }

  /** Suspends execution of the currently. */
  public void suspend(DebugThreadData d) { }

  /** Suspends all the threads. */
  public void suspendAll() { }

  /** Sets the current thread which is being debugged to the thread referenced by d. */
  public void setCurrentThread(DebugThreadData d) { }

  /** Resumes execution of the currently loaded document. */
  public void resume() { }

  /** Resumes execution of the given thread.
   *  @param data the DebugThreadData representing the thread to resume
   */
  public void resume(DebugThreadData data) { }

  /** Steps into the execution of the currently loaded document.
   *  @param flag The flag denotes what kind of step to take. The following mark valid options:
   *  StepRequest.STEP_INTO, StepRequest.STEP_OVER, StepRequest.STEP_OUT
   */
  public void step(int flag) throws DebugException { }

  /** Called from interactionsEnded in MainFrame in order to clear any current StepRequests that remain. */
  public void clearCurrentStepRequest() { }

  /** Adds a watch on the given field or variable.
   *  @param field the name of the field we will watch
   */
  public void addWatch(String field) { }

  /** Removes any watches on the given field or variable.
   *  @param field the name of the field we will watch
   */
  public void removeWatch(String field) { }

  /** Removes the watch at the given index.
   *  @param index Index of the watch to remove
   */
  public void removeWatch(int index) { }

  /** Removes all watches on existing fields and variables. */
  public void removeAllWatches() { }

  /** Toggles whether a breakpoint is set at the given line in the given document. */
  public void toggleBreakpoint(OpenDefinitionsDocument doc, int offset, int lineNum, boolean isEnabled) throws DebugException { }

  /** Sets a breakpoint. */
  public void setBreakpoint(Breakpoint breakpoint) { }

 /** Removes a breakpoint. */
  public void removeBreakpoint(Breakpoint breakpoint) { }

  /** Returns all currently watched fields and variables. */
  public Vector<DebugWatchData> getWatches() { return new Vector<DebugWatchData>(); }

  /** Returns a Vector of ThreadData or null if the vm is null. */
  public Vector<DebugThreadData> getCurrentThreadData() { return new Vector<DebugThreadData>(); }

  /** Returns a Vector of StackData for the current thread or null if the current thread is null. */
  public Vector<DebugStackData> getCurrentStackFrameData() { return new Vector<DebugStackData>(); }

  /** Adds a listener to this Debugger. */
  public void addListener(DebugListener listener) { }

  /** Removes a listener to this JPDADebugger. */
  public void removeListener(DebugListener listener) { }

  /** @return true if there are any threads in the program currently being
   *  debugged which have been suspended (by the user or by hitting a breakpoint).
   */
  public boolean hasSuspendedThreads() { return false; }

  /** Returns whether the thread the debugger is tracking is now running. */
  public boolean hasRunningThread() { return false; }

  /** Returns whether the debugger's current thread is suspended. */
  public boolean isCurrentThreadSuspended() { return false; }

  /** scrolls to the source indicated by the given DebugStackData */
  public void scrollToSource(DebugStackData data) { }

  /** scrolls to the source indicated by the given Breakpoint */
  public void scrollToSource(Breakpoint bp) { }

  /** Gets the Breakpoint object at the specified line in the given class. */
  public Breakpoint getBreakpoint(int line, String className) { return null; }
}
