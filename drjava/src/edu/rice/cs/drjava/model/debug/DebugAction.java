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

import com.sun.jdi.*;
import com.sun.jdi.request.*;

import java.io.File;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

/**
 * Keeps track of information about any request to the debugger, such
 * as Breakpoints.
 * @version $Id$
 */
public abstract class DebugAction<T extends EventRequest> {
  public static final int ANY_LINE = -1;
  
  protected DebugManager _manager;
  protected String _className;
  protected File _file;
  protected OpenDefinitionsDocument _doc;
  
  // Request fields
  protected T _request = null;
  protected int _suspendPolicy = EventRequest.SUSPEND_NONE;
  protected boolean _enabled = true;
  protected int _countFilter = -1;
  protected int _lineNumber = ANY_LINE;
  
  /**
   * Creates a new DebugAction.  Automatically tries to create the EventRequest
   * if a ReferenceType can be found, or else adds this object to the
   * PendingRequestManager. Any subclass should automatically call 
   * _initializeRequest in its constructor.
   * @param manager DebugManager in charge
   * @param doc Document this action corresponds to
   */
  public DebugAction (DebugManager manager, OpenDefinitionsDocument doc) 
    throws DebugException, IllegalStateException {
    _manager = manager;
    _className = _getQualifiedClassName(doc);
    _file = doc.getFile();
    _doc = doc;
  }
  
  /**
   * Returns the class name this DebugAction occurs in.
   */
  public String getClassName() {
    return _className;
  }
  
  /**
   * Returns the file this DebugAction occurs in.
   */
  public File getFile() {
    return _file;
  }
  
  /**
   * Returns the document this DebugAction occurs in.
   */
  public OpenDefinitionsDocument getDocument() {
    return _doc;
  }
  
  /**
   * Returns the EventRequest corresponding to this DebugAction, if it has
   * been created, null otherwise.
   */
  public T getRequest() {
    return _request;
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
   * ReferenceType becomes available.
   * @return true if the EventRequest is successfully created
   */
  public boolean createRequest(ReferenceType rt) throws DebugException {
    if (!rt.isPrepared()) {
      // Can't create a request if class not prepared
      DrJava.consoleOut().println("createRequest: not prepared!");
      return false;
    }
    
    _createRequest(rt);
    if (_request != null) {
      _prepareRequest(_request);
      DrJava.consoleOut().println("Request successfully created: " + _request);
      return true;
    }
    else {
      DrJava.consoleOut().println("createRequest didn't assign to _request...");
      return false;
    }
  }
 
  /**
   * This should always be called from the constructor of the subclass. Tries
   * to create the request, but if unable, will add the request to the
   * pendingRequestManager
   */
  protected void _initializeRequest() throws DebugException {
    ReferenceType ref = _manager.getReferenceType(_className, _lineNumber);
    if (ref != null) {
      DrJava.consoleOut().println("Found a ref, creating request right away.");
      createRequest(ref);
    }
    if (_request == null) {
      // couldn't create the request yet, add to the pending request manager
      DrJava.consoleOut().println("No ref, add to pending request manager");
      _manager.getPendingRequestManager().addPendingRequest(this);
    }
  }
  
  /**
   * Overridden in Breakpoint to pass a line number so that getReferenceType
   * in DebugManager knows to check its inner classes for the line number
   */
  /*protected ReferenceType _getReferenceType() {
    return _manager.getReferenceType(_className);
  }*/
  
  /**
   * Creates an appropriate EventRequest from the EventRequestManager, using
   * the provided ReferenceType, and stores it in the _request field.
   * @param rt ReferenceType used to try to create the request
   * @throws DebugException if the request could not be created.
   */
  protected abstract void _createRequest(ReferenceType rt) throws DebugException;
  
  /**
   * Prepares an EventRequest with the current stored values.
   * @param request the EventRequest to prepare
   */
  protected void _prepareRequest(EventRequest request) {
    request.setEnabled(false);
    
    if (_countFilter != -1) {
      request.addCountFilter(_countFilter);
    }
    request.setSuspendPolicy(_suspendPolicy);
    request.setEnabled(_enabled);
    //System.out.println("request.isEnabled(): " + request.isEnabled() +
    //                   "suspendPolicy: " + request.suspendPolicy());
  }
  
  /**
   * Gets the package and class name of the given OpenDefinitionsDocument
   * @param doc OpenDefinitionsDocument whose qualified class name is desired
   * @return the qualified class name
   */
  protected String _getQualifiedClassName(OpenDefinitionsDocument doc) {
    String packageName = "";
    String className = "";
    try {
      packageName = doc.getDocument().getPackageName();
    }
    catch (InvalidPackageException e) {
      // Couldn't find package, pretend there's none
    }
    if ((packageName != null) && (!packageName.equals(""))) {
      className = packageName + ".";
    }
    className += doc.getClassName();
    return className;
  }
}