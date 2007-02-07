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

package edu.rice.cs.drjava.model.debug.jpda;

import com.sun.jdi.*;
import edu.rice.cs.drjava.model.debug.DebugThreadData;

/**
 * Class for keeping track of the currently running threads.
 * @version $Id: DebugThreadData.java 3901 2006-06-30 05:28:11Z rcartwright $
 */
public class JPDAThreadData extends DebugThreadData {
  private final ThreadReference _thread;
  
  /**
   * Object for keeping track of a thread in the debuggee JVM.
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
  
  /**
   * Tells whether or not the thread is suspended.
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
