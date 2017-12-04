/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import java.util.ArrayList;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/** * Placeholder class indicating that no debugger is available to DrJava.
 * This class follows the Singleton pattern.
 * @version $Id$
 */
public class NoDebuggerAvailable implements Debugger {
  
  /** Singleton instance of this class. */
  public static final NoDebuggerAvailable ONLY = new NoDebuggerAvailable();
  
  /** Private constructor: use the ONLY field. */
  private NoDebuggerAvailable() { }
  
  /** @return whether the debugger is currently available in this JVM. This 
   *         does not indicate whether it is ready to be used.
   */
  public boolean isAvailable() { return false; }
  
  public DebugModelCallback callback() { return new DebugModelCallback() { }; }
  
  /** Attaches the debugger to the Interactions JVM to prepare for debugging. */
  public void startUp() throws DebugException { }
  
  /** Disconnects the debugger from the Interactions JVM and cleans up any state. */
  public void shutdown() { }
  
  /** @return the status of the debugger. */
  public boolean isReady() { return false; }
  
  /** Suspends execution of the currently. 
   * @param d info about the thread to be suspended
   */
  public void suspend(DebugThreadData d) { }
  
  /** Suspends all the threads. */
  public void suspendAll() { }
  
  /** Sets the current thread which is being debugged to the thread referenced by d. */
  public void setCurrentThread(DebugThreadData d) { }

  /** Enables automatic trace
   * @param e true if automatic trace is to be enabled; false for disabled
   */
  public void setAutomaticTraceEnabled(boolean e) { }
  
  /** @return whether automatic trace has been enabled within the debugger*/
  public boolean isAutomaticTraceEnabled() { return false; } 

  /** Resumes execution of the currently loaded document. */
  public void resume() { }
  
  /** Resumes execution of the given thread.
   * @param data the DebugThreadData representing the thread to resume
   */
  public void resume(DebugThreadData data) { }
  
  /** Steps the execution of the currently loaded document. 
   * @param type the type of step to take
   */
  public void step(StepType type) throws DebugException { }
  
  /** Called from interactionsEnded in MainFrame in order to clear any current 
   * StepRequests that remain. 
   */
  public void clearCurrentStepRequest() { }
  
  /** Adds a watch on the given field or variable.
   * @param field the name of the field we will watch
   */
  public void addWatch(String field) { }
  
  /** Removes any watches on the given field or variable.
   * @param field the name of the field we will watch
   */
  public void removeWatch(String field) { }
  
  /** Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public void removeWatch(int index) { }
  
  /** Removes all watches on existing fields and variables. */
  public void removeAllWatches() { }
  
  /** Toggles whether a breakpoint is set at the given line in the given document. 
   * @param doc the document within which to toggle the breakpoint 
   * @param offset the line offset within the document at which to toggle the breakpoint
   * @param isEnabled true if the breakpoint is to be enabled; false for disabled
   * @return the current status of the breakpoint (true for enabled, false for disabled)
   * @throws DebugException if something goes wrong
   */
  public boolean toggleBreakpoint(OpenDefinitionsDocument doc, int offset, boolean isEnabled) 
    throws DebugException { return false; }
  
  /** Sets a breakpoint. 
   * @param breakpoint the breakpoint to set
   */
  public void setBreakpoint(Breakpoint breakpoint) { }
  
  /** Removes a breakpoint. 
   * @param breakpoint the breakpoint to set 
   */
  public void removeBreakpoint(Breakpoint breakpoint) { }
  
  /** @return all currently watched fields and variables. */
  public ArrayList<DebugWatchData> getWatches() { return new ArrayList<DebugWatchData>(); }
  
  /** @return a Vector of ThreadData or null if the vm is null. */
  public ArrayList<DebugThreadData> getCurrentThreadData() { return new ArrayList<DebugThreadData>(); }
  
  /** @return a Vector of StackData for the current thread or null if the current thread is null. */
  public ArrayList<DebugStackData> getCurrentStackFrameData() { return new ArrayList<DebugStackData>(); }
  
  /** Adds a listener to this Debugger. 
   * @param listener the listener to add
   */
  public void addListener(DebugListener listener) { }
  
  /** Removes a listener to this JPDADebugger. 
   * @param listener the listener to add
   */
  public void removeListener(DebugListener listener) { }
  
  /** @return true if there are any threads in the program currently being
    * debugged which have been suspended (by the user or by hitting a breakpoint).
    */
  public boolean hasSuspendedThreads() { return false; }
  
  /** @return whether the thread the debugger is tracking is now running. */
  public boolean hasRunningThread() { return false; }
  
  /** @return whether the debugger's current thread is suspended. */
  public boolean isCurrentThreadSuspended() { return false; }
  
  /** Scrolls to the source indicated by the given DebugStackData 
   * @param data the data to scroll to
   */
  public void scrollToSource(DebugStackData data) { }
  
  /** Scrolls to the source indicated by the given Breakpoint 
   * @param bp the breakpoint to scroll to
   */
  public void scrollToSource(Breakpoint bp) { }
  
  /** @param line the line within which to get the breakpoint
   * @param className the class within which to get the breakpoint
   * @return the Breakpoint object at the specified line in the given class. 
   */
  public Breakpoint getBreakpoint(int line, String className) { return null; }
}
