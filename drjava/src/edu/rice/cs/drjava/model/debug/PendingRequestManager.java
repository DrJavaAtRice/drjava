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

import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Keeps track of DocumentDebugActions that are waiting to be resolved when the
 * classes they corresponed to are prepared.  (Only DocumentDebugActions have
 * reference types which can be prepared.)
 * @version $Id$
 */

public class PendingRequestManager {
  private JPDADebugger _manager;
  private Hashtable<String, Vector<DocumentDebugAction>> _pendingActions;

  public PendingRequestManager(JPDADebugger manager) {
    _manager = manager;
    _pendingActions = new Hashtable<String, Vector<DocumentDebugAction>>();
  }

  /**
   * Called if a breakpoint is set before its class is prepared
   * @param action The DebugAction that is pending
   */
  public void addPendingRequest (DocumentDebugAction action) {
    Vector<DocumentDebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      actions = new Vector<DocumentDebugAction>();

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

  /**
   * Called if a breakpoint is set and removed before its class is prepared
   * @param action The DebugAction that was set and removed
   */
  public void removePendingRequest (DocumentDebugAction action) {
    Vector<DocumentDebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      return;
    }
    actions.remove(action);
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

    // Get the pending actions for this class (and inner classes)
    Vector<DocumentDebugAction> actions = _pendingActions.get(className);
    Vector<DocumentDebugAction> failedActions =
      new Vector<DocumentDebugAction>();
    //DrJava.consoleOut().println("pending actions: " + actions);
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
      int lineNumber = actions.get(i).getLineNumber();
      if (lineNumber != DebugAction.ANY_LINE) {
        try {
          List lines = rt.locationsOfLine(lineNumber);
          if (lines.size() == 0) {
            // Requested line number not in reference type, skip this action
            //i++;
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
        // next line was in condition for if
        actions.get(i).createRequests(refTypes);
//        if (!) {
          // if no request created, skip this action
          //i++;
//        }
//        else {
          // Experiment: try never removing the action or event request.
          //  This way, multiple classloads of this class will always have
          //  the DebugActions set properly
          /*

          // if request created, remove the current action and keep i here
          actions.remove(i);
          // check if the vector is empty
          if (actions.size() == 0) {
            _pendingActions.remove(className);
            _manager.getEventRequestManager().deleteEventRequest(event.request());
          }
        */
//        }
      }
      catch (DebugException e) {
        failedActions.add(actions.get(i));
        //i++;
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
