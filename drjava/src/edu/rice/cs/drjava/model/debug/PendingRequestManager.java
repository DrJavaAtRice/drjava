/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.DrJava;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import gj.util.Hashtable;
import gj.util.Vector;

import java.util.List;
import java.util.LinkedList;

/**
 * Keeps track of DebugActions that are waiting to be resolved when the
 * classes they corresponed to are prepared.
 * @version $Id$
 */

public class PendingRequestManager {
  private DebugManager _manager;
  private Hashtable<String, Vector<DebugAction>> _pendingActions;
  
  public PendingRequestManager(DebugManager manager) {
    _manager = manager;
    _pendingActions = new Hashtable<String, Vector<DebugAction>>();
  }
  
  /**
   * Called if a breakpoint is set before its class is prepared
   * @param action The DebugAction that is pending
   */
  public void addPendingRequest (DocumentDebugAction action) {
    Vector<DebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      actions = new Vector<DebugAction>();
      
      // only create a ClassPrepareRequest once per class
      ClassPrepareRequest request = 
        _manager.getEventRequestManager().createClassPrepareRequest();
      request.addClassFilter(className + "*");
      request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      request.enable();
      //System.out.println("Creating prepareRequest in class " + className);
    }
    actions.addElement(action);
    _pendingActions.put(className, actions);
  }  
  
  /**
   * Called if a breakpoint is set and removed before its class is prepared
   * @param action The DebugAction that was set and removed
   */
  public void removePendingRequest (DocumentDebugAction action) {
    Vector<DebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      return;
    }
    actions.removeElement(action);
    // check if the vector is empty
    if (actions.size() == 0) {
      _pendingActions.remove(className);
    }
  }
  
  /**
   * Called by the EventHandler whenever a ClassPrepareEvent occurs.
   * This will take the event, get the class that was prepared, lookup
   * the Vector of DebugAction that was waiting for this class's preparation,
   * iterate through this Vector, and attempt to create the Breakpoints that
   * were pending. Since the keys to the HashTable are the names of the
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
    //DrJava.consoleOut().println("In classPrepared. rt: " + rt);
    //DrJava.consoleOut().println("equals getReferenceType: " + 
    //                   rt.equals(_manager.getReferenceType(rt.name())));
    String className = rt.name();
    
    // crop off the $ if there is one and anything after it
    int indexOfDollar = className.indexOf('$');    
    if (indexOfDollar > 1) {
      className = className.substring(0, indexOfDollar);
    }
    Vector<DebugAction> actions = _pendingActions.get(className);
    Vector<DebugAction> failedActions = new Vector<DebugAction>();
    //DrJava.consoleOut().println("pending actions: " + actions);
    if (actions == null) {
      // any actions that were waiting for this class to be prepared have been
      // removed
      _manager.getEventRequestManager().deleteEventRequest(event.request());
      return;
    }
    for (int i = 0; i < actions.size();) {
      int lineNumber = actions.elementAt(i).getLineNumber();
      /*
      System.out.println("i: "+i+" actions.size(): " + actions.size() + 
                         " actions.elementAt(i): " + actions.elementAt(i) +
                         " actions.elementAt(i).getLineNumber(): " + 
                         actions.elementAt(i).getLineNumber());
                         */
      if (lineNumber != DebugAction.ANY_LINE) {
        List lines = new LinkedList();
        try {
          lines = rt.locationsOfLine(lineNumber);
        }
        catch (AbsentInformationException aie) {
          // outer class has no line number info, skip this action
        }
        if (lines.size() == 0) {
          i++;
          continue;
        }
      }
      // check if the action was successfully created
      try {
        if (!actions.elementAt(i).createRequest(rt)) {
          // if no request created, skip this action
          i++;
        }
        else {
          // if request created, remove the current action and keep i here
          actions.removeElementAt(i);
          // check if the vector is empty
          if (actions.size() == 0) {
            _pendingActions.remove(className);
            _manager.getEventRequestManager().deleteEventRequest(event.request());
          }
        }
      }
      catch (DebugException e) {
        failedActions.addElement(actions.elementAt(i));
        i++;
       // DrJava.consoleOut().println("Exception preparing request!! " + e);
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