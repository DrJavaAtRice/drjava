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

import com.sun.jdi.*;

/**
 * Class for keeping track of the currently running threads.
 * @version $Id$
 */
public class DebugThreadData {
  private final ThreadReference _thread;
  private final String _name;
  private final String _status;
  private final long _uniqueID;
  
  /**
   * Object for keeping track of a thread in the debuggee JVM.
   * @param thread JPDA's reference to the thread
   */
  public DebugThreadData(ThreadReference thread) {
    _thread = thread;
    String name;
    try {
      name = _thread.name();
    }
    catch (VMDisconnectedException vmde) {
      name = "";
    }
    _name = name;
    String status = "(unknown)";
    switch (_thread.status()) {
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
    if( isSuspended() && status.equals("RUNNING") ){
      _status = "SUSPENDED";
    }
    else{
      _status = status;
    }
    
    _uniqueID = _thread.uniqueID();
  }
  
  /**
   * Returns the name of this thread.
   */
  public String getName() {
    return _name;
  }
  
  /**
   * Returns the status of this thread (at the time of this object's construction)
   */
  public String getStatus() {
    return _status;
  }
  
  public long getUniqueID() {
    return _uniqueID;
  }
  
  /**
   * Tells whether or not the thread is suspended.
   * @return true iff the thread is suspended
   */
  public boolean isSuspended() {
    try {
      return _thread.isSuspended();
    }
    catch (ObjectCollectedException oce) {
      return false;
    }
    catch (VMDisconnectedException vmde) {
      return false;
    }
  }
}
