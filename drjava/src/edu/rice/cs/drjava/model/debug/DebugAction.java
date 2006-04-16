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

import com.sun.jdi.request.*;
import java.util.Vector;

/**
 * Keeps track of information about any request to the debugger, such
 * as Breakpoints.
 * @version $Id$
 */
public abstract class DebugAction<T extends EventRequest> {
  public static final int ANY_LINE = -1;

  protected JPDADebugger _manager;

  // Request fields

  /**
   * Vector of EventRequests.  There might be more than one, since
   * there can be multiple reference types for one class.  They all
   * share the same attributes, though, so the other fields don't
   * need to be vectors.
   */
  protected Vector<T> _requests;
  protected int _suspendPolicy = EventRequest.SUSPEND_NONE;
  protected boolean _isEnabled = true;
  protected int _countFilter = -1;
  protected int _lineNumber = ANY_LINE;

  /**
   * Creates a new DebugAction.  Automatically tries to create the EventRequest
   * if a ReferenceType can be found, or else adds this object to the
   * PendingRequestManager. Any subclass should automatically call
   * _initializeRequest in its constructor.
   * @param manager JPDADebugger in charge
   */
  public DebugAction(JPDADebugger manager) {
    _manager = manager;
    _requests = new Vector<T>();
  }

  /**
   * Returns the EventRequest corresponding to this DebugAction, if it has
   * been created, null otherwise.
   */
  public Vector<T> getRequests() {
    return _requests;
  }

  /**
   * Returns the line number this DebugAction occurs on
   */
  public int getLineNumber() {
    return _lineNumber;
  }

  /**
   * Creates an EventRequest corresponding to this DebugAction, using the
   * given ReferenceType.  This is called either from the DebugAction
   * constructor or the PendingRequestManager, depending on when the
   * ReferenceType becomes available. This DebugAction must be an
   * instance of DocumentDebugAction since a ReferenceType is being
   * used.
   * @return true if the EventRequest is successfully created
   */
  //public abstract boolean createRequests(ReferenceType rt) throws DebugException;

  public boolean createRequests() throws DebugException {
    _createRequests();
    if (_requests.size() > 0) {
      _prepareRequests(_requests);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * This should always be called from the constructor of the subclass. Tries
   * to create all applicable EventRequests for this DebugAction.
   */
  protected void _initializeRequests() throws DebugException {
    createRequests();
    if (_requests.size() == 0) {
      throw new DebugException("Could not create EventRequests for this action!");
    }
  }

  /**
   * Creates an appropriate EventRequest from the EventRequestManager and
   * stores it in the _request field.
   * @throws DebugException if the request could not be created.
   */
  protected void _createRequests() throws DebugException { }

  /**
   * Prepares all relevant EventRequests with the current stored values.
   * @param requests the EventRequests to prepare
   */
  protected void _prepareRequests(Vector<T> requests) {
    for (int i=0; i < requests.size(); i++) {
      _prepareRequest(requests.get(i));
    }
  }

  /**
   * Prepares this EventRequest with the current stored values.
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
  public void setEnabled(boolean isEnabled) {
    _isEnabled = isEnabled;
  }
}
