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

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

import java.util.List;
import java.util.Iterator;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/**
 * The breakpoint object which has references to its OpenDefinitionsDocument and its 
 * BreakpointRequest
 */
public class Breakpoint extends DocumentDebugAction<BreakpointRequest> { 
  
  //private String _className;
  //private int _lineNumber;
  //private ReferenceType _ref;
  //private BreakpointRequest _breakpointReq;
   private Position _startPos;
   private Position _endPos;
   
  /**
   * @throws IllegalStateException if the document does not have a file
   */
  public Breakpoint( OpenDefinitionsDocument doc, int offset, int lineNumber, DebugManager manager) 
    throws DebugException, IllegalStateException {    
    
    super (manager, doc);
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _lineNumber = lineNumber;
    
    DefinitionsDocument defDoc = doc.getDocument();
    
    try {
      _startPos = defDoc.createPosition(defDoc.getLineStartPos(offset));
      _endPos = defDoc.createPosition( defDoc.getLineEndPos(offset));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    
    _initializeRequest(_manager.getReferenceType(_className, _lineNumber));
    //_doc = doc;
    //_createBreakpointRequest();
    
    DrJava.consoleOut().println("Breakpoint lineNumber is " + lineNumber);
  }
 
  /*private void _createBreakpointRequest() throws DebugException {

    
    //System.out.println("Setting breakpoint in class: " + className + 
    //                   ", line: " + lineNumber);
    
    _ref = manager.getReferenceType(_className, vm);
    
    
    // Get locations for the line number, use the first
    try {
      List lines = _ref.locationsOfLine(_lineNumber);
      if (lines.size() == 0) {
        // Can't find a location on this line
        //System.out.println("No locations found.");
        throw new DebugException("Could not find line number: " + _lineNumber);
      }
      Location loc = (Location) lines.get(0);
      _breakpointReq = vm.eventRequestManager().createBreakpointRequest(loc);
      _breakpointReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      _breakpointReq.enable();
      //System.out.println("Breakpoint: " + req);
    }
    catch (AbsentInformationException aie) {
      throw new DebugException("Could not find line number: " + aie);
    }
    
  }*/
  
  /**
   * Creates an appropriate EventRequest from the EventRequestManager, using
   * the provided ReferenceType, and stores it in the _request field.
   * @throws DebugException if the request could not be created.
   */
  protected void _createRequest(ReferenceType rt) throws DebugException {
    DrJava.consoleOut().println("Breakpoint._createRequest starting...");
    /*
    DrJava.consoleOut().println("rt.isVerified: " + rt.isVerified() +
                                " rt.isInitialized: " + rt.isInitialized() +
                                " rt.isPrepared: " + rt.isPrepared());
                                */
    // Get locations for the line number, use the first
    try {
      List lines = rt.locationsOfLine(_lineNumber);
      if (lines.size() == 0) {
        // Can't find a location on this line
        //System.out.println("No locations found.");
        throw new DebugException("Could not find line number: " + _lineNumber);
      }
      Location loc = (Location) lines.get(0);
      _request = _manager.getEventRequestManager().createBreakpointRequest(loc);
      DrJava.consoleOut().println("Created a breakpoint request: " + _request);
      //DrJava.consoleOut().println("new Breakpoint: " + toString());
      //_breakpointReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      //_breakpointReq.enable();
      //System.out.println("Breakpoint: " + req);
    }
    catch (AbsentInformationException aie) {
      throw new DebugException("Could not find line number: " + aie);
    }
  }
  
  /**
   * Accessor for the offset of this breakpoint's start position
   * @return the start offset
   */
  public int getStartOffset() {
    return _startPos.getOffset();
  }
  
  /**
   * Accessor for the offset of this breakpoint's end position
   * @return the end offset
   */
  public int getEndOffset(){
    return _endPos.getOffset();
  }
  
  public String toString() {
    if (_request != null) {
      return "Breakpoint[class: " + getClassName() + 
        ", lineNumber: " + getLineNumber() + 
        ", method: " + ((BreakpointRequest)_request).location().method() +
        ", codeIndex: " + ((BreakpointRequest)_request).location().codeIndex() + "]";
    }
    else
      return "Breakpoint[class: " + getClassName() + 
        ", lineNumber: " + getLineNumber() + "]";
  }
}