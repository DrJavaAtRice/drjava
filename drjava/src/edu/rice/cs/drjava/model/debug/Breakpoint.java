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

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

import java.util.List;
import java.util.Iterator;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/**
 * The breakpoint object which has references to its OpenDefinitionsDocument and its 
 * BreakpointRequest
 */
public class Breakpoint { 
  
  private OpenDefinitionsDocument _doc;
  private String _className;
  private int _lineNumber;
  private ReferenceType _ref;
  private BreakpointRequest _breakpointReq;
  
  public Breakpoint( OpenDefinitionsDocument doc, int lineNumber, VirtualMachine vm) 
    throws DebugException {
    
    _doc = doc;
    _lineNumber = lineNumber;
    
    _createBreakpointRequest(vm);
  }
 
  private void _createBreakpointRequest(VirtualMachine vm) throws DebugException {

    String packageName = "";
    try {
      _doc.getDocument().getPackageName();
    }
    catch (InvalidPackageException e) {
      // Couldn't find package, pretend there's none
    }
    _className = packageName + _doc.getClassName();
    //System.out.println("Setting breakpoint in class: " + className + 
    //                   ", line: " + lineNumber);
    
    _ref = _getReferenceType(_className, vm);
    
    
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
    
  }
  
  private ReferenceType _getReferenceType(String className, VirtualMachine vm) throws DebugException {
    // Get all classes that match this name
    List classes = vm.classesByName(className);
    //System.out.println("Num of classes found: " + classes.size());
    ReferenceType ref = null; //Reference type is null
    // Assume first one is correct, for now
    
    if (classes.size() == 0) {
      //_tryLoadingClass(className, vm);
      classes = vm.classesByName(className);
    }
    if (classes.size() >0) {
      ref = (ReferenceType) classes.get(0);
      return ref;
    }
    else throw new DebugException ("Couldn't find the class corresponding to '" + className +"'.");
  }
   
  public OpenDefinitionsDocument getDocument() {
    return _doc;
  }
  
  public BreakpointRequest getRequest() {
    return _breakpointReq;
  }
  
  public ReferenceType getReference() {
    return _ref;
  }
  
  public String getClassName() {
    return _className;
  }
 
  public int getLineNumber() {
    return _breakpointReq.location().lineNumber();
  }
  
  public String toString() {
    return "Breakpoint @ "+getLineNumber()+" in "+getClassName();
  }
}