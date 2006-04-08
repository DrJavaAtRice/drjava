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

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

import java.util.Vector;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

import java.io.*;

/** The breakpoint object which has references to its OpenDefinitionsDocument and its BreakpointRequest
 */
public class Breakpoint extends DocumentDebugAction<BreakpointRequest> implements DebugBreakpointData {

   private Position _startPos;
   private Position _endPos;

  /** @throws DebugException if the document does not have a file */
  public Breakpoint(OpenDefinitionsDocument doc, int offset, int lineNumber, boolean enabled, JPDADebugger manager)
    throws DebugException {

    super(manager, doc, offset);
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _lineNumber = lineNumber;
    _enabled = enabled;

    try {
      _startPos = doc.createPosition(doc.getLineStartPos(offset));
      _endPos = doc.createPosition( doc.getLineEndPos(offset));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }

    if ((_manager!=null) && (_manager.isReady())) {
      // the debugger is on, so initialize now
      // otherwise breakpoint gets re-set when debugger is enabled
      Vector<ReferenceType> refTypes = _manager.getReferenceTypes(_className, _lineNumber);
      _initializeRequests(refTypes);
      setEnabled(enabled);
    }
  }
  
  /** Creates appropriate EventRequests from the EventRequestManager and
   *  stores them in the _requests field.
   *  @param refTypes All (identical) ReferenceTypes to which this action
   *  applies.  (There may be multiple if a custom class loader is in use.)
   *  @throws DebugException if the requests could not be created.
   */
  protected void _createRequests(Vector<ReferenceType> refTypes) throws DebugException {
    try {
      for (int i=0; i < refTypes.size(); i++) {
        ReferenceType rt = refTypes.get(i);

        if (!rt.isPrepared()) {
          // Not prepared, so skip this one
          continue;
        }

        // Get locations for the line number, use the first
        List lines = rt.locationsOfLine(_lineNumber);
        if (lines.size() == 0) {
          // Can't find a location on this line
          setEnabled(false);          
          throw new DebugException("Could not find line number: " + _lineNumber);
        }
        Location loc = (Location) lines.get(0);
        
        BreakpointRequest request = _manager.getEventRequestManager().createBreakpointRequest(loc);
        request.setEnabled(_enabled);
        _requests.add(request);
      }
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

  /** Accessor for the offset of this breakpoint's end position
   *  @return the end offset
   */
  public int getEndOffset() {
    return _endPos.getOffset();
  }
  
  /** Enable/disable the breakpoint. */
  public void setEnabled(boolean enabled) {
    boolean old = _enabled;
    super.setEnabled(enabled);
    try {
      for(BreakpointRequest bpr: _requests) {
        bpr.setEnabled(enabled);
      }
    }
    catch(VMDisconnectedException vmde) { /* just ignore */ }
    if (_enabled!=old) _manager._notifier.breakpointChanged(this);
  }

  public String toString() {
    String cn = getClassName();
    if (_exactClassName!=null) { cn = _exactClassName.replace('$', '.'); }
    if (_requests.size() > 0) {
      // All BreakpointRequests are identical-- one copy for each loaded
      //  class.  So just print info from the first one, and how many there are.
      return "Breakpoint[class: " + cn +
        ", lineNumber: " + getLineNumber() +
        ", method: " + _requests.get(0).location().method() +
        ", codeIndex: " + _requests.get(0).location().codeIndex() +
        ", numRefTypes: " + _requests.size() + "]";
    }
    else {
      return "Breakpoint[class: " + cn +
        ", lineNumber: " + getLineNumber() + "]";
    }
  }
}
