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

import java.util.Vector;
import java.util.List;
import java.io.File;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.LineNotExecutableException;
import javax.swing.text.BadLocationException;

/** Superclasses all DebugActions that are associated with specific OpenDefinitionsDocuments.
  * @version $Id: DocumentDebugAction.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class DocumentDebugAction<T extends EventRequest> extends DebugAction<T> {
  
  protected volatile String _className;
  protected volatile String _exactClassName;
  protected volatile File _file;
  protected volatile OpenDefinitionsDocument _doc;
  protected int _offset;
  
  public final int SHORT_DOC_MAX_LENGTH = 20000;
  
  
  /** Creates a new DocumentDebugAction.  Automatically tries to create the EventRequest if a ReferenceType can be 
    * found, or else adds this object to the PendingRequestManager. Any subclass should automatically call
    * _initializeRequest in its constructor.
    * @param manager JPDADebugger in charge
    * @param doc Document this action corresponds to
    * @param offset Offset into the document that the action affects
    */
  public DocumentDebugAction (JPDADebugger manager, OpenDefinitionsDocument doc, int offset) throws DebugException {
    super(manager);
    _exactClassName = null;
    try {
      if (offset >= 0) {
        if (doc.getLength() < SHORT_DOC_MAX_LENGTH) {
          // only do this on short files
          // in long files, getEnclosingClassName might take too long
          _exactClassName = doc.getEnclosingClassName(offset, true);
        }
      }
    }
    catch(ClassNameNotFoundException cnnfe) { /* ignore, we don't need the exact class name */ }
    catch(BadLocationException ble) { /* ignore, we don't need the exact class name */ }
    try {
      if (offset >= 0) {
        _className = doc.getQualifiedClassName(offset);
      }
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Still couldn't find a class name, use ""
      _className = "";
    }
    // System.out.println("Breakpoint added: " + _className + ", exact=" + _exactClassName);
    
    try {
      _file = doc.getFile();
      if (_file == null) throw new DebugException("This document has no source file.");
    }
    catch (FileMovedException fme) {
      throw new DebugException("This document's file no longer exists: " + fme.getMessage());
    }
    _doc = doc;
    _offset = offset;
  }
  
  /** Returns the class name this DebugAction occurs in. */
  public String getClassName() { return _className; }
  
  /** Returns the file this DebugAction occurs in. */
  public File getFile() { return _file; }
  
  /** Returns the document this DebugAction occurs in. */
  public OpenDefinitionsDocument getDocument() { return _doc; }
  
  /** @return offset of this debug action. */
  public int getOffset() { return _offset; }
  
  /** @return exact class name, or null if not available. */
  public String getExactClassName() { return _exactClassName; }
  
  /** Creates EventRequests corresponding to this DebugAction, using the given ReferenceTypes.  This is called either 
    * from the DebugAction constructor or the PendingRequestManager, depending on when the ReferenceTypes become 
    * available.  (There may be multiple reference types for the same class if a custom class loader is used.)
    * @return true if the EventRequest is successfully created
    */
  public boolean createRequests(Vector<ReferenceType> refTypes) throws DebugException {
    _createRequests(refTypes);
    if (_requests.size() > 0) {
      _prepareRequests(_requests);
      return true;
    }
    else return false;
  }
  
  /** This should always be called from the constructor of the subclass.  Attempts to create EventRequests on the 
    * given ReferenceTypes, and also adds this action to the pending request manager (so identical classes loaded
    * in the future will also have this action).
    */
  protected void _initializeRequests(Vector<ReferenceType> refTypes) throws DebugException {
    if (refTypes.size() > 0) createRequests(refTypes);
    else {
      if (_exactClassName != null) {
        List<ReferenceType> referenceTypes = _manager.getVM().classesByName(_exactClassName);
        if (referenceTypes.size() > 0) {
          // class has been loaded, but couldn't find this line number
          throw new LineNotExecutableException("Cannot set breakpoint, line " + getLineNumber() + 
                                               " is not an executable line.\nYou may have to recompile.");
        }
      }
    }
    //if (_request == null) {
    // couldn't create the request yet, add to the pending request manager
    
    // Experiment: always add to pending request, to deal with multpile class loads
    _manager.getPendingRequestManager().addPendingRequest(this);
    //}
  }
  
  /** Creates appropriate EventRequests from the EventRequestManager and stores them in the _requests field.
    * @param refTypes All (identical) ReferenceTypes to which this action applies.  (There may be multiple if a custom
    *        class loader is in use.)
    * @throws DebugException if the requests could not be created.
    */
  protected abstract void _createRequests(Vector<ReferenceType> refTypes) throws DebugException;
  
  /** Prepares this EventRequest with the current stored values.
    * @param request the EventRequest to prepare
    */
  protected void _prepareRequest(T request) {
    super._prepareRequest(request);
    request.putProperty("document", _doc);
  }
}
