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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import java.util.StringTokenizer;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/**
 * The breakpoint object which has references to its OpenDefinitionsDocument and its
 * StepRequest
 */
public class Step extends DebugAction<StepRequest> implements OptionConstants {
  private ThreadReference _thread;
  private int _size;
  private int _depth;

  // Java class patterns for which we may not want events
  private String[] javaExcludes = {"java.*", "javax.*", "sun.*", "com.sun.*", "com.apple.eawt.*", "com.apple.eio.*" };

  /**
   * @throws IllegalStateException if the document does not have a file
   */
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

  /**
   * Creates an appropriate EventRequest from the EventRequestManager and
   * stores it in the _request field.
   * @throws DebugException if the request could not be created.
   */
  protected void _createRequests() throws DebugException {
    boolean stepJava = DrJava.getConfig().getSetting(DEBUG_STEP_JAVA).booleanValue();
    boolean stepInterpreter = DrJava.getConfig().getSetting(DEBUG_STEP_INTERPRETER).booleanValue();
    boolean stepDrJava = DrJava.getConfig().getSetting(DEBUG_STEP_DRJAVA).booleanValue();
    String exclude = DrJava.getConfig().getSetting(DEBUG_STEP_EXCLUDE);

    StepRequest request = _manager.getEventRequestManager().
      createStepRequest(_thread, _size, _depth);
    if (!stepJava) {
      for (int i=0; i < javaExcludes.length; i++) {
        request.addClassExclusionFilter(javaExcludes[i]);
      }
    }
    if (!stepInterpreter) {
      request.addClassExclusionFilter("koala.*");
    }
    if (!stepDrJava) {
      request.addClassExclusionFilter("edu.rice.cs.drjava.*");
      request.addClassExclusionFilter("edu.rice.cs.util.*");
    }
    StringTokenizer st = new StringTokenizer(exclude, ",");
    while (st.hasMoreTokens()) {
      request.addClassExclusionFilter(st.nextToken().trim());
    }


    // Add this request (the only one) to the list
    _requests.add(request);
  }

  public String toString() {
    return "Step[thread: " + _thread +  "]";
  }
}
