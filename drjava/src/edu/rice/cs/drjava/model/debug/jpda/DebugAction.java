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

import com.sun.jdi.request.*;
import java.util.Vector;
import edu.rice.cs.drjava.model.debug.DebugException;

/** Keeps track of information about any request to the debugger, such as Breakpoints.
  * @version $Id: DebugAction.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class DebugAction<T extends EventRequest> {
  public static final int ANY_LINE = -1;

  protected final JPDADebugger _manager;

  // Request fields

  /** Vector of EventRequests.  There might be more than one, since there can be multiple reference types for one class.
    * They all share the same attributes, though, so the other fields don't need to be vectors.
    */
  protected final Vector<T> _requests;
  protected volatile int _suspendPolicy = EventRequest.SUSPEND_NONE;
  protected volatile boolean _isEnabled = true;
  protected volatile int _countFilter = -1;
  protected volatile int _lineNumber = ANY_LINE;

  /** Creates a new DebugAction.  Automatically tries to create the EventRequest if a ReferenceType can be found, or 
    * else adds this object to the PendingRequestManager. Any subclass should automatically call _initializeRequest 
    * in its constructor.
    * @param manager JPDADebugger in charge
    */
  public DebugAction(JPDADebugger manager) {
    _manager = manager;
    _requests = new Vector<T>();
  }

  /** Returns the EventRequest corresponding to this DebugAction, if it has been created, null otherwise. */
  public Vector<T> getRequests() { return _requests; }

  /** Returns the line number this DebugAction occurs on */
  public int getLineNumber() { return _lineNumber; }

  /** Creates an EventRequest corresponding to this DebugAction, using the given ReferenceType.  This is called either
    * from the DebugAction constructor or the PendingRequestManager, depending on when the ReferenceType becomes 
    * available. This DebugAction must be an instance of DocumentDebugAction since a ReferenceType is being used.
    * @return true if the EventRequest is successfully created
    */
  //public abstract boolean createRequests(ReferenceType rt) throws DebugException;

  public boolean createRequests() throws DebugException {
    _createRequests();
    if (_requests.size() > 0) {
      _prepareRequests(_requests);
      return true;
    }
    else return false;
  }

  /** This should always be called from the constructor of the subclass. Tries to create all applicable EventRequests
    * for this DebugAction.
    */
  protected void _initializeRequests() throws DebugException {
    createRequests();
    if (_requests.size() == 0) {
      throw new DebugException("Could not create EventRequests for this action!");
    }
  }

  /** Creates an appropriate EventRequest from the EventRequestManager and stores it in the _request field.
    * @throws DebugException if the request could not be created.
    */
  protected void _createRequests() throws DebugException { }

  /** Prepares all relevant EventRequests with the current stored values.
    * @param requests the EventRequests to prepare
    */
  protected void _prepareRequests(Vector<T> requests) {
    for (int i = 0; i < requests.size(); i++) {
      _prepareRequest(requests.get(i));
    }
  }

  /** Prepares this EventRequest with the current stored values.
    * @param request the EventRequest to prepare
    */
  protected void _prepareRequest(T request) {
    // the request must be disabled to be edited
    request.setEnabled(false);

    if (_countFilter != -1) {
      request.addCountFilter(_countFilter);
    }
    request.setSuspendPolicy(_suspendPolicy);
    request.setEnabled(_isEnabled);

    // Add properties
    request.putProperty("debugAction", this);
  }
  
  /** @return true if breakpoint is enabled. */
  public boolean isEnabled() { return _isEnabled; }
  
  
  /** Enable/disable the breakpoint. */
  public void setEnabled(boolean isEnabled) { _isEnabled = isEnabled; }
}
