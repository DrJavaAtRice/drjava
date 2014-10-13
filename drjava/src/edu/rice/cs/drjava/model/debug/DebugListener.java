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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/** Any class which wants to listen to events fired by the Debugger should implement this interface and use Debugger's
  * addDebugListener() method.
  * @version $Id: DebugListener.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface DebugListener extends RegionManagerListener<Breakpoint> {
  
  /** Called when debugger mode has been enabled. Must be executed in event thread. */
  public void debuggerStarted();
  
  /** Called when debugger mode has been disabled.  Must be executed in event thread. */
  public void debuggerShutdown();
  
  /** Called when the given line is reached by the current thread in the debugger, to request that the line be 
    * displayed.  Must be executed only in the event thread.
    * @param doc Document to display
    * @param lineNumber Line to display or highlight
    * @param shouldHighlight true iff the line should be highlighted.
    */
  public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight);  
  
  /** Called when a breakpoint is reached during execution.
    * @param bp the breakpoint
    */
  public void breakpointReached(Breakpoint bp);
  
  /** Called when a watch is set.  Must be executed in event thread.
    * @param w the watch
    */
  public void watchSet(DebugWatchData w);
  
  /** Called when a watch is removed.  Must be executed in event thread.
    * @param w the watch
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
    * @param thread the thread that was set as current
    */
  public void currThreadSet(DebugThreadData thread);
  
  // from RegionManagerListener<Breakpoint>:
  // public void regionAdded(Breakpoint r, int index);
  // public void regionChanged(Breakpoint r, int index);
  // public void regionRemoved(Breakpoint r);
}