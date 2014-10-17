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
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.TreeMap;

import java.io.File;

import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.drjava.model.debug.DebugException;
//import edu.rice.cs.drjava.model.compiler.LanguageLevelStackTraceMapper;

/** Keeps track of DocumentDebugActions that are waiting to be resolved when the classes they corresponed to are 
  * prepared.  (Only DocumentDebugActions have reference types which can be prepared.)
  * @version $Id: PendingRequestManager.java 5594 2012-06-21 11:23:40Z rcartwright $
  */

public class PendingRequestManager {
  private JPDADebugger _manager;
  private HashMap<String, Vector<DocumentDebugAction<?>>> _pendingActions;
  
  public PendingRequestManager(JPDADebugger manager) {
    _manager = manager;
    _pendingActions = new HashMap<String, Vector<DocumentDebugAction<?>>>();
  }
  
  /** Called if a breakpoint is set before its class is prepared
    * @param action The DebugAction that is pending
    */
  public void addPendingRequest (DocumentDebugAction<?> action) {
    String className = action.getClassName();
    Vector<DocumentDebugAction<?>> actions = _pendingActions.get(className);
    if (actions == null) {
      actions = new Vector<DocumentDebugAction<?>>();
      
      // only create a ClassPrepareRequest once per class
      ClassPrepareRequest request =
        _manager.getEventRequestManager().createClassPrepareRequest();
      // Listen for events from the class, and also its inner classes
      request.addClassFilter(className + "*");
      request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      request.enable();
      //System.out.println("Creating prepareRequest in class " + className);
    }
    actions.add(action);
    _pendingActions.put(className, actions);
  }
  
  /** Called if a breakpoint is set and removed before its class is prepared
    * @param action The DebugAction that was set and removed
    */
  public void removePendingRequest (DocumentDebugAction<?> action) {
    String className = action.getClassName();
    Vector<DocumentDebugAction<?>> actions = _pendingActions.get(className);
    if (actions == null) {
      return;
    }
    actions.remove(action);
    // check if the vector is empty
    if (actions.size() == 0) {
      _pendingActions.remove(className);
    }
  }
  
  /** Recursively look through all nested types to see if the line number exists.
    * @param lineNumber line number to look for
    * @param rt reference type to start at
    * @return true if line number is found
    */
  private boolean recursiveFindLineNumber(int lineNumber, ReferenceType rt) {
    try {
      for(Location l: rt.allLineLocations()) {
        if (l.lineNumber() == lineNumber) { return true; }
      }
      for(ReferenceType nested: rt.nestedTypes()) {
        if (recursiveFindLineNumber(lineNumber, nested) == true) { return true; }
      }
    }
    catch (AbsentInformationException aie) { /* fall through and return false */ }
    return false;
  }
  
  
  /**
   * Method to change Language Level line numbers into their java file counterparts
   * @param dda the DocumentDebugAction whose line needs to be adjusted
   * @return the correct line number for the .java file
   */
  public int LLDDALineNum(DocumentDebugAction<?> dda){
    int line = dda.getLineNumber();
    File f = dda.getFile();
    
//    if (DrJavaFileUtils.isLLFile(f)) {
//      f = DrJavaFileUtils.getJavaForLLFile(f);
//      TreeMap<Integer, Integer> tM = _manager.getLLSTM().readLLBlock(f);
//      line = tM.get(dda.getLineNumber());
//    }
    return line;
  }
  
  /** Called by the EventHandler whenever a ClassPrepareEvent occurs.  This will take the event, get the class that was
    * prepared, lookup the Vector of DebugAction that was waiting for this class's preparation, iterate through this 
    * Vector, and attempt to create the Breakpoints that
    * were pending. Since the keys to the Hashtable are the names of the
    * outer class, the $ and everything after it must be cropped off from the
    * class name in order to do the lookup. During the lookup, however, the line
    * number of each action is checked to see if the line number is contained
    * in the given event's ReferenceType. If not, we ignore that pending action
    * since it is not in the class that was just prepared, but may be in one of its
    * inner classes.
    * @param event The ClassPrepareEvent that just occured
    */
  public void classPrepared (ClassPrepareEvent event) throws DebugException {
    ReferenceType rt = event.referenceType();
    //System.out.println("In classPrepared. rt: " + rt);
    //System.out.println("equals getReferenceType: " +
    //                   rt.equals(_manager.getReferenceType(rt.name())));
    String className = rt.name();
    
    // crop off the $ if there is one and anything after it
    int indexOfDollar = className.indexOf('$');
    if (indexOfDollar > 1) {
      className = className.substring(0, indexOfDollar);
    }
    
    // Get the pending actions for this class (and inner classes)
    Vector<DocumentDebugAction<?>> actions = _pendingActions.get(className);
    Vector<DocumentDebugAction<?>> failedActions =
      new Vector<DocumentDebugAction<?>>();
    //System.out.println("pending actions: " + actions);
    if (actions == null) {
      // Must have been a different class with a matching prefix, ignore it
      // since we're not interested in this class.
      return;
    }
    else if (actions.isEmpty()) {
      // any actions that were waiting for this class to be prepared have been
      // removed
      _manager.getEventRequestManager().deleteEventRequest(event.request());
      return;
    }
    for (int i = 0; i < actions.size(); i++) {
      DocumentDebugAction<?> a = actions.get(i);
      int lineNumber = LLDDALineNum(a);//a.getLineNumber();
      if (lineNumber != DebugAction.ANY_LINE) {
        try {
          List<Location> lines = rt.locationsOfLine(lineNumber);
          if (lines.size() == 0) {
            // Do not disable action; the line number might just be in another class in the same file
            String exactClassName = a.getExactClassName();
            if (exactClassName != null && exactClassName.equals(rt.name())) {
              _manager.printMessage(actions.get(i).toString() + " not on an executable line; disabled.");
              actions.get(i).setEnabled(false);
            }
            
            // Requested line number not in reference type, skip this action
            continue;
          }
        }
        catch (AbsentInformationException aie) {
          // outer class has no line number info, skip this action
          continue;
        }
      }
      // check if the action was successfully created
      try {
        Vector<ReferenceType> refTypes = new Vector<ReferenceType>();
        refTypes.add(rt);
        a.createRequests(refTypes);  // This type warning will go away in JDK 1.5
      }
      catch (DebugException e) {
        failedActions.add(a);
        // System.out.println("Exception preparing request!! " + e);
      }
    }
    
    // For debugging purposes
    /*
     List l = _manager.getEventRequestManager().breakpointRequests();
     System.out.println("list of eventrequestmanager's breakpointRequests: " +
     l);
     for (int i = 0; i < l.size(); i++) {
     BreakpointRequest br = (BreakpointRequest)l.get(i);
     System.out.println("isEnabled(): " + br.isEnabled() +
     " suspendPolicy(): " + br.suspendPolicy() +
     " location(): " + br.location());
     }
     */
    if (failedActions.size() > 0) {
      // need to create an exception framework
      throw new DebugException("Failed actions: " + failedActions);
    }
  }
}
