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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/** Any class which wants to listen to events fired by the Debugger should implement this interface and use Debugger's
 *  addDebugListener() method.
 *  @version $Id$
 */
public interface DebugListener {
  
  /** Called when debugger mode has been enabled. Must be executed in event thread. */
  public void debuggerStarted();
  
  /** Called when debugger mode has been disabled.  Must be executed in event thread. */
  public void debuggerShutdown();

  /** Called when the given line is reached by the current thread in the debugger, to request that the line be 
   *  displayed.  Must be executed only in the event thread.
   *  @param doc Document to display
   *  @param lineNumber Line to display or highlight
   *  @param shouldHighlight true iff the line should be highlighted.
   */
  public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight);  
  
  /** Called when a breakpoint is set in a document.  Must be executed in event thread.
   *  @param bp the breakpoint
   */
  public void breakpointSet(Breakpoint bp);
  
  /** Called when a breakpoint is reached during execution.
   *  @param bp the breakpoint
   */
  public void breakpointReached(Breakpoint bp);
  
  /** Called when a breakpoint is removed from a document.  Must be executed in event thread.
   *  @param bp the breakpoint
   */
  public void breakpointRemoved(Breakpoint bp);

  /** Called when a watch is set.  Must be executed in event thread.
   *  @param w the watch
   */
  public void watchSet(DebugWatchData w);
  
  /** Called when a watch is removed.  Must be executed in event thread.
   *  @param w the watch
   */
  public void watchRemoved(DebugWatchData w);
  
  /** Called when a step is requested on the current thread.  Must be executed in event thread. */
  public void stepRequested();
  
  /** Called when the current thread is suspended. */
  public void currThreadSuspended();
  
  /** Called when the current thread is resumed. Must be executed in event thread. */
  public void currThreadResumed();
  
  /** Called when a thread starts. Must be executed in event thread. */
  public void threadStarted();
  
  /** Called when the current thread dies. Must be executed in event thread. */
  public void currThreadDied();
  
  /** Called when any thread other than the current thread dies. Must be executed in event thread. */
  public void nonCurrThreadDied();

  /** Called when the current (selected) thread is set in the debugger.
   *  @param thread the thread that was set as current
   */
  public void currThreadSet(DebugThreadData thread);
}