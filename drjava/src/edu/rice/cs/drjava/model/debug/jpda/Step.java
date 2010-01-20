/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.debug.jpda;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.debug.DebugException;

import java.util.StringTokenizer;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/** The breakpoint object which has references to its OpenDefinitionsDocument and its StepRequest */
public class Step extends DebugAction<StepRequest> implements OptionConstants {
  private final ThreadReference _thread;
  private final int _size;
  private final int _depth;

  // Java class patterns for which we may not want events
  private final String[] _javaExcludes = {"java.*", "javax.*", "sun.*", "com.sun.*", "com.apple.eawt.*", "com.apple.eio.*" };

  /** @throws IllegalStateException if the document does not have a file */
  public Step(JPDADebugger manager, int size, int depth)
    throws DebugException, IllegalStateException {
     super (manager);
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _thread = _manager.getCurrentThread();
    _size = size;
    _depth = depth;
    _countFilter = 1; //only step once.
    _initializeRequests();
  }

  //public boolean createRequest(ReferenceType rt) throws DebugException {
  //  return false;
  //}

  /** Creates an appropriate EventRequest from the EventRequestManager and
   * stores it in the _request field.
   * @throws DebugException if the request could not be created.
   */
  protected void _createRequests() throws DebugException {
    boolean stepJava = DrJava.getConfig().getSetting(DEBUG_STEP_JAVA).booleanValue();
    boolean stepInterpreter = DrJava.getConfig().getSetting(DEBUG_STEP_INTERPRETER).booleanValue();
    boolean stepDrJava = DrJava.getConfig().getSetting(DEBUG_STEP_DRJAVA).booleanValue();

    StepRequest request = _manager.getEventRequestManager().
      createStepRequest(_thread, _size, _depth);
    if (!stepJava) {
      for (int i = 0; i < _javaExcludes.length; i++) {
        request.addClassExclusionFilter(_javaExcludes[i]);
      }
    }
    if (!stepInterpreter) {
      request.addClassExclusionFilter("koala.*");
      request.addClassExclusionFilter("edu.rice.cs.dynamicjava.*");
    }
    if (!stepDrJava) {
      request.addClassExclusionFilter("edu.rice.cs.drjava.*");
      request.addClassExclusionFilter("edu.rice.cs.util.*");
      request.addClassExclusionFilter("edu.rice.cs.plt.*");
    }
    for(String s: DrJava.getConfig().getSetting(DEBUG_STEP_EXCLUDE)) {
      request.addClassExclusionFilter(s.trim());
    }

    // Add this request (the only one) to the list
    _requests.add(request);
  }

  public String toString() { return "Step[thread: " + _thread +  "]"; }
}
