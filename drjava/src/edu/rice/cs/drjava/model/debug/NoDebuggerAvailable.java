/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import gj.util.Vector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Placeholder class indicating that no debugger is available to DrJava.
 * This class follows the Singleton pattern.
 * @version $Id$
 */
public class NoDebuggerAvailable implements Debugger {
  /**
   * Singleton instance of this class.
   */
  public static final NoDebuggerAvailable ONLY = new NoDebuggerAvailable();
  
  /**
   * Private constructor: use the ONLY field.
   */
  private NoDebuggerAvailable() {
  }
  
  /**
   * Returns whether the debugger is currently available in this JVM.
   * This does not indicate whether it is ready to be used.
   */
  public boolean isAvailable() {
    return false;
  }
  
  /**
   * Attaches the debugger to the Interactions JVM to prepare for debugging.
   */
  public void startup() throws DebugException {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Disconnects the debugger from the Interactions JVM and cleans up
   * any state.
   */
  public void shutdown() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Returns the status of the debugger
   */
  public boolean isReady() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Suspends execution of the currently 
   */
  public void suspend(DebugThreadData d){
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Suspends all the threads
   */
  public void suspendAll(){
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Sets the current thread which is being debugged to the thread referenced by d
   */
  public void setCurrentThread(DebugThreadData d){
    throw new IllegalStateException("No debugger is available");    
  }
  
  /**
   * Resumes execution of the currently loaded document.
   */
  public void resume() {
    throw new IllegalStateException("No debugger is available");
  }
    
  /** 
   * Steps into the execution of the currently loaded document.
   * @flag The flag denotes what kind of step to take. The following mark valid options:
   * StepRequest.STEP_INTO
   * StepRequest.STEP_OVER
   * StepRequest.STEP_OUT
   */
  public void step(int flag) throws DebugException {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Called from interactionsEnded in MainFrame in order to clear any current 
   * StepRequests that remain.
   */
  public void clearCurrentStepRequest() {
    throw new IllegalStateException("No debugger is available");
  }

  /**
   * Adds a watch on the given field or variable.
   * @param field the name of the field we will watch
   */
  public void addWatch(String field) {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Removes any watches on the given field or variable.
   * @param field the name of the field we will watch
   */
  public void removeWatch(String field) {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public void removeWatch(int index) {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Removes all watches on existing fields and variables.
   */
  public void removeAllWatches() {
    throw new IllegalStateException("No debugger is available");
  }
  

  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param offset Start offset on the line to set the breakpoint
   * @param lineNumber Line on which to set or remove the breakpoint
   */
  public void toggleBreakpoint(OpenDefinitionsDocument doc, 
                               int offset, int lineNum) 
    throws DebugException
  {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Sets a breakpoint.
   *
   * @param breakpoint The new breakpoint to set
   */
  public void setBreakpoint(Breakpoint breakpoint) {
    throw new IllegalStateException("No debugger is available");
  }
  
 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  *
  * @param breakpoint The breakpoint to remove.
  * @param className the name of the class the BP is being removed from.
  */
  public void removeBreakpoint(Breakpoint breakpoint) {
    throw new IllegalStateException("No debugger is available");
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public void removeAllBreakpoints() {
    throw new IllegalStateException("No debugger is available");
  }

  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public Vector<Breakpoint> getBreakpoints() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Prints the list of breakpoints in the current session of DrJava, both pending
   * resolved Breakpoints are listed
   */
  public void printBreakpoints() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Returns all currently watched fields and variables.
   */
  public Vector<DebugWatchData> getWatches() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Returns a Vector of ThreadData or null if the vm is null
   */
  public Vector<DebugThreadData> getCurrentThreadData() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Returns a Vector of StackData for the current thread or null if the 
   * current thread is null.
   */
  public Vector<DebugStackData> getCurrentStackFrameData() {
    throw new IllegalStateException("No debugger is available");
  }
  
  /**
   * Adds a listener to this JPDADebugger.
   * @param listener a listener that reacts on events generated by the JPDADebugger
   */
  public void addListener(DebugListener listener) {
    throw new IllegalStateException("No debugger is available");
  }

  /**
   * Removes a listener to this JPDADebugger.
   * @param listener listener to remove
   */
  public void removeListener(DebugListener listener) {
    throw new IllegalStateException("No debugger is available");
  }
  
   /**
   * @return true if there are any threads in the program currently being 
   * debugged which have been suspended (by the user or by hitting a breakpoint).
   */
  public boolean hasSuspendedThreads(){
    throw new IllegalStateException("No debugger is available");
  }
}
