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

import gj.util.Vector;
import java.io.File;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;

/**
 * Superclasses all DebugActions that are associated with specific 
 * OpenDefinitionsDocuments. 
 * @version $Id$
 */
public abstract class DocumentDebugAction<T extends EventRequest> 
  extends DebugAction<T> {  
  
  protected String _className;
  protected File _file;
  protected OpenDefinitionsDocument _doc;  
  
  
  /**
   * Creates a new DocumentDebugAction.  Automatically tries to create the 
   * EventRequest if a ReferenceType can be found, or else adds this object to the
   * PendingRequestManager. Any subclass should automatically call 
   * _initializeRequest in its constructor.
   * @param manager DebugManager in charge
   * @param doc Document this action corresponds to
   * @param offset Offset into the document that the action affects
   */
  public DocumentDebugAction (DebugManager manager, 
                              OpenDefinitionsDocument doc,
                              int offset)
    throws DebugException 
  {
    super(manager);
    try {
      _className = doc.getDocument().getQualifiedClassName(offset);
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Couldn't find class name, leave as empty string
      _className = "";
    }
    try {
      _file = doc.getFile();
    }
    catch (FileMovedException fme) {
      throw new DebugException("This document's file no longer exists: " +
                               fme.getMessage());
    }
    catch (IllegalStateException ise) {
      throw new DebugException("This document has no file: " +
                               ise.getMessage());
    }
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
   * Creates EventRequests corresponding to this DebugAction, using the
   * given ReferenceTypes.  This is called either from the DebugAction
   * constructor or the PendingRequestManager, depending on when the
   * ReferenceTypes become available.  (There may be multiple reference
   * types for the same class if a custom class loader is used.)
   * @return true if the EventRequest is successfully created
   */
  public boolean createRequests(Vector<ReferenceType> refTypes) 
    throws DebugException
  {
    _createRequests(refTypes);
    if (_requests.size() > 0) {
      _prepareRequests(_requests);
      return true;
    }
    else {
      return false;
    }
  }
 
  /**
   * This should always be called from the constructor of the subclass.
   * Attempts to create EventRequests on the given ReferenceTypes, and
   * also adds this action to the pending request manager (so identical 
   * classes loaded in the future will also have this action).
   */
  protected void _initializeRequests(Vector<ReferenceType> refTypes)
    throws DebugException
  {
    if (refTypes.size() > 0) {
      createRequests(refTypes);
    }
    //if (_request == null) {
      // couldn't create the request yet, add to the pending request manager
    
    // Experiment: always add to pending request, to deal with multpile class loads
    _manager.getPendingRequestManager().addPendingRequest(this);
    //}
  }
  
  /**
   * Creates appropriate EventRequests from the EventRequestManager and 
   * stores them in the _requests field.
   * @param refTypes All (identical) ReferenceTypes to which this action
   * applies.  (There may be multiple if a custom class loader is in use.)
   * @throws DebugException if the requests could not be created.
   */
  protected abstract void _createRequests(Vector<ReferenceType> refTypes)
    throws DebugException;
  
  /**
   * Prepares this EventRequest with the current stored values.
   * @param request the EventRequest to prepare
   */
  protected void _prepareRequest(T request) {
    super._prepareRequest(request);
    request.putProperty("document", _doc);
  }
}