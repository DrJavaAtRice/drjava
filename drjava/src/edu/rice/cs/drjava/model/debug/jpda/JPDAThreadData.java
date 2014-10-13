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

package edu.rice.cs.drjava.model.debug.jpda;

import com.sun.jdi.*;
import edu.rice.cs.drjava.model.debug.DebugThreadData;

/**
 * Class for keeping track of the currently running threads.
 * @version $Id: JPDAThreadData.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class JPDAThreadData extends DebugThreadData {
  private final ThreadReference _thread;
  
  /** Object for keeping track of a thread in the debuggee JVM.
   * @param thread JPDA's reference to the thread
   */
  public JPDAThreadData(ThreadReference thread) {
    super(threadName(thread), threadStatus(thread), thread.uniqueID());
    _thread = thread;
  }
  
  private static String threadName(ThreadReference thread) {
    try { return thread.name(); }
    catch (VMDisconnectedException e) { return ""; }
  }
  
  private static String threadStatus(ThreadReference thread) {
    String status = "(unknown)";
    try {
      switch (thread.status()) {
        case ThreadReference.THREAD_STATUS_MONITOR: 
          status = "MONITOR"; break;
        case ThreadReference.THREAD_STATUS_NOT_STARTED:
          status = "NOT STARTED"; break;
        case ThreadReference.THREAD_STATUS_RUNNING:
          status = "RUNNING"; break;
        case ThreadReference.THREAD_STATUS_SLEEPING:
          status = "SLEEPING"; break;
        case ThreadReference.THREAD_STATUS_UNKNOWN:
          status = "UNKNOWN"; break;
        case ThreadReference.THREAD_STATUS_WAIT:
          status = "WAIT"; break;
        case ThreadReference.THREAD_STATUS_ZOMBIE:
          status = "ZOMBIE"; break;
      }
    }
    catch (VMDisconnectedException e) {
      // status will be set to unknown
    }
    if ( safeIsSuspended(thread) && status.equals("RUNNING") ) {
      status = "SUSPENDED";
    }
    return status;
  }
  
  /** Tells whether or not the thread is suspended.
   * @return true iff the thread is suspended
   */
  public boolean isSuspended() { return safeIsSuspended(_thread); }
  
  /** Invoke {@code t.isSuspended()} under the protection of a try-catch block */
  private static boolean safeIsSuspended(ThreadReference t) {
    try { return t.isSuspended(); }
    catch (ObjectCollectedException e) { return false; }
    catch (VMDisconnectedException e) { return false; }
  }
  
}
