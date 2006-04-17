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
  public void startup() throws DebugException {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Disconnects the debugger from the Interactions JVM and cleans up any state. */
  public void shutdown() {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Returns the status of the debugger. */
  public boolean isReady() {
    return false;
    //throw new IllegalStateException("No debugger is available");
  }
  
  public boolean inDebugMode() { return false; }

  /** Suspends execution of the currently. */
  public void suspend(DebugThreadData d) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Suspends all the threads. */
  public void suspendAll() {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Sets the current thread which is being debugged to the thread referenced by d. */
  public void setCurrentThread(DebugThreadData d) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Resumes execution of the currently loaded document. */
  public void resume() {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Resumes execution of the given thread.
   *  @param data the DebugThreadData representing the thread to resume
   */
  public void resume(DebugThreadData data) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Steps into the execution of the currently loaded document.
   *  @param flag The flag denotes what kind of step to take. The following mark valid options:
   *  StepRequest.STEP_INTO, StepRequest.STEP_OVER, StepRequest.STEP_OUT
   */
  public void step(int flag) throws DebugException {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Called from interactionsEnded in MainFrame in order to clear any current StepRequests that remain. */
  public void clearCurrentStepRequest() {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Adds a watch on the given field or variable.
   *  @param field the name of the field we will watch
   */
  public void addWatch(String field) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Removes any watches on the given field or variable.
   *  @param field the name of the field we will watch
   */
  public void removeWatch(String field) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Removes the watch at the given index.
   *  @param index Index of the watch to remove
   */
  public void removeWatch(int index) {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Removes all watches on existing fields and variables. */
  public void removeAllWatches() {
    //throw new IllegalStateException("No debugger is available");
  }

  /** Toggles whether a breakpoint is set at the given line in the given document.
   *  @param doc Document in which to set or remove the breakpoint
   *  @param offset Start offset on the line to set the breakpoint
   *  @param lineNum Line on which to set or remove the breakpoint
   *  @param enabled true if this breakpoint should be enabled
   */
  public void toggleBreakpoint(OpenDefinitionsDocument doc, int offset, int lineNum, boolean isEnabled) throws DebugException {
    // throw new IllegalStateException("No debugger is available");
  }

  /**
   * Sets a breakpoint.
   *
   * @param breakpoint The new breakpoint to set
   */
  public void setBreakpoint(Breakpoint breakpoint) {
    //throw new IllegalStateException("No debugger is available");
  }

 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  * @param breakpoint The breakpoint to remove.
  */
  public void removeBreakpoint(Breakpoint breakpoint) {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public void removeAllBreakpoints() {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public Vector<Breakpoint> getBreakpoints() {
    return new Vector<Breakpoint>();
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Prints the list of breakpoints in the current session of DrJava, both pending
   * resolved Breakpoints are listed
   */
  public void printBreakpoints() {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns all currently watched fields and variables.
   */
  public Vector<DebugWatchData> getWatches() {
    return new Vector<DebugWatchData>();
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns a Vector of ThreadData or null if the vm is null
   */
  public Vector<DebugThreadData> getCurrentThreadData() {
    return new Vector<DebugThreadData>();
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns a Vector of StackData for the current thread or null if the
   * current thread is null.
   */
  public Vector<DebugStackData> getCurrentStackFrameData() {
    return new Vector<DebugStackData>();
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Adds a listener to this JPDADebugger.
   * @param listener a listener that reacts on events generated by the JPDADebugger
   */
  public void addListener(DebugListener listener) {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Removes a listener to this JPDADebugger.
   * @param listener listener to remove
   */
  public void removeListener(DebugListener listener) {
    //throw new IllegalStateException("No debugger is available");
  }

   /**
   * @return true if there are any threads in the program currently being
   * debugged which have been suspended (by the user or by hitting a breakpoint).
   */
  public boolean hasSuspendedThreads() {
    return false;
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns whether the thread the debugger is tracking is now running.
   */
  public boolean hasRunningThread() {
    return false;
    //throw new IllegalStateException("No Debugger is available");
  }

  /**
   * Returns whether the debugger's current thread is suspended.
   */
  public boolean isCurrentThreadSuspended() {
    return false;
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * scrolls to the source indicated by the given DebugStackData
   * @param data the DebugStackData representing the source location
   */
  public void scrollToSource(DebugStackData data) {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * scrolls to the source indicated by the given Breakpoint
   * @param bp the Breakpoint representing the source location
   */
  public void scrollToSource(Breakpoint bp) {
    //throw new IllegalStateException("No debugger is available");
  }

  /**
   * Gets the Breakpoint object at the specified line in the given class.
   * If the given data do not correspond to an actual breakpoint, null is returned.
   * @param line the line number of the breakpoint
   * @param className the name of the class the breakpoint's in
   * @return the Breakpoint corresponding to the line and className, or null if
   *         there is no such breakpoint.
   */
  public Breakpoint getBreakpoint(int line, String className) {
    return null;
    //throw new IllegalStateException("No debugger is available");
  }
}
