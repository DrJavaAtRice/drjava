/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug.jpda;

import edu.rice.cs.drjava.model.OrderedDocumentRegion;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.IDocumentRegion;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.DebugException;

import java.awt.EventQueue;
import java.util.List;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/** The breakpoint object which has references to its OpenDefinitionsDocument and its BreakpointRequest.  See the
  * WARNING below about hashing on this type or its subtypes.
  */
public class JPDABreakpoint extends DocumentDebugAction<BreakpointRequest> implements Breakpoint {
  
  private volatile Position _position;
  private volatile Position _startPos;
  private volatile Position _endPos;
  
  /** Note that _position, which records the breakpoint position, is inherited from DocumentDebugAction. */
  private volatile OpenDefinitionsDocument _doc;
  
  /** @throws DebugException if the document does not have a file */
  public JPDABreakpoint(OpenDefinitionsDocument doc, int offset, boolean isEnabled, JPDADebugger manager)
    throws DebugException {
    
    super(manager, doc, offset);
    
    assert EventQueue.isDispatchThread();
    _doc = doc;
    try { _position = doc.createPosition(offset); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
    
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _isEnabled = isEnabled;
    update();
    
    if (_manager != null && _manager.isReady()) {
      // the debugger is on, so initialize now
      // otherwise breakpoint gets re-set when debugger is enabled
      Vector<ReferenceType> refTypes = _manager.getReferenceTypes(_className, _manager.LLBreakpointLineNum(this));
      _initializeRequests(refTypes);
      setEnabled(isEnabled);
    }
  }
  
  /** Returns the String used on the JTree label for this breakpoint. */
  public String getString() {
    try {
      int start = _startPos.getOffset();
      int end = _endPos.getOffset();
      int length = end - start;
      if (length <= 120) return _doc.getText(start, length);
      StringBuilder sb = new StringBuilder(124);
      sb.append(_doc.getText(start, 120)).append(" ...");
      return sb.toString();
    }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
//  public void setTreeNode(DefaultMutableTreeNode node) { 
//    throw new UnsupportedOperationException("JPDA Breakpoint does not support setTreeNode");
//  }
//  
//  public DefaultMutableTreeNode getTreeNode() {
//    throw new UnsupportedOperationException("JPDA Breakpoint does not support getTreeNode");
//  }
  
  /** Creates appropriate EventRequests from the EventRequestManager and stores them in the _requests field.
    * @param refTypes  All (identical) ReferenceTypes to which this action applies.  (There may be multiples if a custom
    *                  class loader is in use.)
    * @throws DebugException if the requests could not be created.
    */
  protected void _createRequests(Vector<ReferenceType> refTypes) throws DebugException {
    try {
      for (int i = 0; i < refTypes.size(); i++) {
        ReferenceType rt = refTypes.get(i);
        
        if (!rt.isPrepared()) {
          // Not prepared, so skip this one
          continue;
        }
        
        // Get locations for the line number, use the first
        List<Location> lines = rt.locationsOfLine(_manager.LLBreakpointLineNum(this));
        if (lines.size() == 0) {
          // Can't find a location on this line
          setEnabled(false);          
          throw new DebugException("Could not find line number: " + _lineNumber);
        }
        Location loc = lines.get(0);
        
        BreakpointRequest request = _manager.getEventRequestManager().createBreakpointRequest(loc);
        request.setEnabled(_isEnabled);
        _requests.add(request);
      }
    }
    catch (AbsentInformationException aie) { throw new DebugException("Could not find line number: " + aie); }
  }
  
  /** Accessor for the offset of this breakpoint's start position
    * @return the start offset
    */
  public int getStartOffset() { return _startPos.getOffset(); }
  
  /** Accessor for the offset of this breakpoint's end position
    * @return the end offset
    */
  public int getEndOffset() { return _endPos.getOffset(); }
  
  /** Accessor for the offset of this breakpoint's start position
    * @return the start offset
    */
  public int getLineStartOffset() { return _startPos.getOffset(); }
  
  /** Accessor for the offset of this breakpoint's end position
    * @return the end offset
    */
  public int getLineEndOffset() { return _endPos.getOffset(); }
  
  /** Update _startPos, _endPos and _lineNumber from the position that moves with the document. */
  public void update() {
    try {  // _doc is inherited from DocumentRegion
      int offset = _position.getOffset();
      _startPos = _doc.createPosition(_doc._getLineStartPos(offset));
      _endPos = _doc.createPosition(_doc._getLineEndPos(offset));
      _lineNumber = _doc.getLineOfOffset(offset)+1; // our line numbers are 1-based
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }  // should never happen
  }
  
  public boolean isEmpty() { update(); return getStartOffset() == getEndOffset(); }
    
//  /** Accessor for this breakpoint's start position
//    * @return the start position
//    */
//  public Position getStartPosition() { return _startPos; }
  
//  /** Accessor for this breakpoint's end position
//    * @return the end position
//    */
//  public Position getEndPosition() { return _endPos; }
  
  /** Defines the equality relation on DocumentRegions.  This equivalence relation on allocated objects is finer
    * grained than the equivalence relation induced by compareTo because it requires equality on Position objects, 
    * not just equality of the current offsets of Positions. 
    */
  public final boolean equals(Object o) {
    if (o == null || ! (o instanceof IDocumentRegion)) return false;
    update(); 
    IDocumentRegion r = (IDocumentRegion) o;
    return getDocument() == r.getDocument() && getStartOffset() == r.getStartOffset() && getEndOffset() == r.getEndOffset();
  }
  
  /** Totally orders regions lexicographically based on (_doc, endOffset, startOffset). This method is typically applied
    * to regions within the same document. 
    */
  public int compareTo(OrderedDocumentRegion r) {
    int docRel = getDocument().compareTo(r.getDocument());
    if (docRel != 0) return docRel;
    // At this point, we know that this and r have identical file paths, but they do not have to be the same allocation
    
    assert getDocument() == r.getDocument();  // DrJava never creates two ODD objects with the same path
    int end1 = getEndOffset();
    int end2 = r.getEndOffset();
    int endDiff = end1 - end2;
    if (endDiff != 0) return endDiff;
    
    int start1 = getStartOffset();
    int start2 = r.getStartOffset();
    return start1 - start2;
  }
  
  /** Returns the line number this DebugAction occurs on */
  public int getLineNumber() {
    update();
    return _lineNumber;
  }
  
  /* WARNING: overriding hashCode to "agree" with equals is disastrous because Breakpoint offsets change!  Hashcode must
   * be inconsisent with equals to produce an invariant value.  Hence, you must use IdentityHashMap instead of HashMap
   * or Hashtable. 
   */
//  public int hashCode() { return ObjectUtil.hash(_doc, getStartOffset(), getEndOffset()); }
  
  /** Enable/disable the breakpoint. */
  public void setEnabled(boolean isEnabled) {
    assert EventQueue.isDispatchThread();
    boolean old = _isEnabled;
    super.setEnabled(isEnabled);
    try {
      for(BreakpointRequest bpr: _requests) {
        bpr.setEnabled(isEnabled);
      }
    }
    catch(VMDisconnectedException vmde) { /* just ignore */ }
    if (_isEnabled!=old) _manager.notifyBreakpointChange(this);
  }
  
  public String toString() {
    String cn = getClassName();
    if (_exactClassName != null) { cn = _exactClassName.replace('$', '.'); }
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
