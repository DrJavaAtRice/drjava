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
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

import java.util.List;
import java.util.Iterator;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/**
 * The breakpoint object which has references to its OpenDefinitionsDocument and its 
 * StepRequest
 */
public class Step extends DebugAction<StepRequest> { 
  private ThreadReference _thread;
  private int _size;
  private int _depth;
  
  // Class patterns for which we don't want events
  private String[] excludes = {"java.*", "javax.*", "sun.*", 
    "com.sun.*", "koala.*"};
   
  /**
   * @throws IllegalStateException if the document does not have a file
   */
  public Step(DebugManager manager, int size, int depth) 
    throws DebugException, IllegalStateException {    
    // change super constructor to not take a doc?
    
    super (manager);
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _thread = _manager.getCurrentThread();
    _size = size;
    _depth = depth;
    //_lineNumber = lineNumber;
    _initializeRequest();
    //_doc = doc;
  }
  
  public boolean createRequest(ReferenceType rt) throws DebugException {
    return false;
  }
  
  /**
   * Creates an appropriate EventRequest from the EventRequestManager and 
   * stores it in the _request field.
   * @throws DebugException if the request could not be created.
   */
  protected void _createRequest() throws DebugException {
    DrJava.consoleOut().println("Step._createRequest starting...");
    
    _request = _manager.getEventRequestManager().
      createStepRequest(_manager.getCurrentThread(), _size, _depth);
    for (int i=0; i<excludes.length; ++i) {
      _request.addClassExclusionFilter(excludes[i]);
    }
    DrJava.consoleOut().println("Created a step request: " + _request + 
                                " size: " + _size + " depth: " + _depth);
      //DrJava.consoleOut().println("new Step: " + toString());
      //_breakpointReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      //_breakpointReq.enable();
      //System.out.println("Step: " + req);
      //_manager.addStepToMap(this);
  }
  
  public String toString() {
    return "Step[thread: " + _thread +  "]";
  }
}