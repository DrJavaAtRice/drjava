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

package edu.rice.cs.util.swing;

import java.util.Stack;
import java.util.Vector;

import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.plt.object.ObjectUtil.hash;

/** This class has synchronized public methods because it is accessed outside of the event thread. */
public class HighlightManager {
  
  //private Hashtable<HighlightPosition, Stack<HighlightInfo>> _highlights;
  
  /** An unsorted Vector of Stack<HighlightInfo>, each of which corresponds to a unique
    * region in the document. All HighlightInfo objects within a given stack must correspond
    * to the same region but must have unique Highlighter.HighlightPainters.
    * Each stack is ordered so the most recent highlight is at the top.
    */
  private Vector<Stack<HighlightInfo>> _highlights;
  
  /** The component necessary for creating positions in in the document, which is also
    * contained within this component.
    */
  private JTextComponent _component;
  
  /** Constructor
    * @param jtc the component whose document will have positions created therein.
    */
  public HighlightManager(JTextComponent jtc) {
    _component = jtc;
    _highlights = new Vector<Stack<HighlightInfo>>();
  }
  
  /** Overrides to toString() to support unit testing */
  
  public String toString() { return "HighLightManager(" + _highlights + ")"; }
  
  /** Size of highlight stack; used only for unit testing */
  
  public int size() { return _highlights.size(); }
  
  /** Adds a highlight using the supplied painter to the vector element(Stack) that exactly corresponds to the 
    * specified bounds. The most recently added highlights over a given range appear on top of the older highlights. 
    * All highlights in a given range(Stack) must be unique, that is, each must use a different painter--redundant 
    * highlights are shifted to the top of the stack, but not added twice.
    * @param startOffset the offset at which the highlight is to begin.
    * @param endOffset the offset at which the highlight is to end.
    * @param p the Highlighter.HighlightPainter for painting
    * @return HighlightInfo the HighlightInfo object, for keeping a tag of a given highlight
    */
  public synchronized HighlightInfo addHighlight(int startOffset, int endOffset, Highlighter.HighlightPainter p) {
    
    HighlightInfo newLite = new HighlightInfo(startOffset,endOffset,p);
    
//      Utilities.showDebug("Adding highlight from " + startOffset + " to " + endOffset);
    Stack<HighlightInfo> lineStack = _getStackAt(newLite);
    
    if (lineStack != null) {
      int searchResult = lineStack.search(newLite);
      if (searchResult == 1) return lineStack.peek();
      if (searchResult > 1) {
        lineStack.remove(newLite);
      }
    }
    else {
      //add a new Stack to the empty place in the hashtable
      lineStack = new Stack<HighlightInfo>();
       _highlights.add(lineStack);
    }
    
    try {
      Object highlightTag = _component.getHighlighter().addHighlight(startOffset,endOffset,p);
      newLite.setHighlightTag(highlightTag);
      lineStack.push(newLite);
      return newLite;
    }
    catch (BadLocationException ble) {
      //if adding a highlight failed, remove any empty stack
      if (lineStack.isEmpty()) {
        _highlights.remove(lineStack);
      }
      throw new UnexpectedException(ble);
    }
  }
  
  /** Returns the Stack corresponding to the given region in the document, or null in none exists. ASSUMES that every 
    * Stack in the vector has a unique region.
    * @param h  the descriptor for the desired region.
    * @return  the corresponding Stack, or null
    */
  private Stack<HighlightInfo> _getStackAt(HighlightInfo h) {
    
    for (Stack<HighlightInfo> stack : _highlights) {
      if (stack.get(0).matchesRegion(h)) {
        return stack;
      }
    }
    //if here, no corresponding stack, so return null
    return null;
  }
  
  /** Removes a highlight with the specified start/end offsets and the given painter.
    * @param startOffset the offset at which the desired highlight should start.
    * @param endOffset the offset at which the desired highlight shoud end.
    * @param p the Highlighter.HighlightPainter for painting
    */
  public synchronized void removeHighlight(int startOffset, int endOffset, Highlighter.HighlightPainter p) {
    HighlightInfo newLite = new HighlightInfo(startOffset, endOffset, p);
    removeHighlight(newLite);
  }
  
  /** Removes a given highlight (HighlightInfo) from the highlighter
    * @param newLite the HighlightInfo object corresponding to the highlight needed to be removed
    */
  public void removeHighlight(HighlightInfo newLite) {
    
    
//      int startOffset = newLite.getStartOffset();
//      int endOffset = newLite.getEndOffset();
    
    Stack<HighlightInfo> lineStack = _getStackAt(newLite);
    
    if (lineStack== null) {
      //System.out.println("Error! No stack to access in region from " + startOffset+ " to " +  endOffset);
      return;
    }
    
    int searchResult = lineStack.search(newLite);
    //System.out.println("searchResult: " + searchResult);
    
    if (searchResult == 1) {
      HighlightInfo liteToRemove = lineStack.pop();
      _component.getHighlighter().removeHighlight(liteToRemove.getHighlightTag());
      //System.out.println("Removed highlight @ " + startOffset);
    }
    else if (searchResult > 1) {
      //System.out.println("Removing old instance...");
      lineStack.remove(newLite);
      _component.getHighlighter().removeHighlight(newLite.getHighlightTag());
    }
    
    if (lineStack.isEmpty()) {
      //System.out.println("Removing empty stack...");
      //remove the lineStack
      _highlights.remove(lineStack);
    }
    
  }
  
  /** The public inner class defining a "smart" highlight, which can return the value of its start and end
    * offsets for comparison with other highlights. Also keeps a tag to its actual highlight in the
    * component's highlighter for easy removal.
    */
  public class HighlightInfo {
    private Object _highlightTag;
    private Position _startPos;
    private Position _endPos;
    private Highlighter.HighlightPainter _painter;
    
    /** Constructor takes the bounds and the painter for a highlighter
      * @param from the offset at which the new highlight will start.
      * @param to the offset at which the new highlight will end.
      * @param p the Highlighter.HighlightPainter for painting
      */
    public HighlightInfo(int from, int to, Highlighter.HighlightPainter p) {
      
      _highlightTag = null;
      try {
        _startPos = _component.getDocument().createPosition(from);
        _endPos = _component.getDocument().createPosition(to);
      }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      
      _painter = p;
    }
    
    /** Set the highlight tag for later access to the highlight as it is stored in the components highlighter.
      * @param highlightTag the Object for keeping track of a stored highlight
      */
    public void setHighlightTag ( Object highlightTag) { _highlightTag = highlightTag; }
    
    /** Tests equivalency of one HighlightInfo object with this HighlightInfo object. Compares start
      * and end offsets, and the Highlighter.HighlightPainter -- returns true, if they are the same in both.
      * @param o the other HighlightInfo object to compare to this one.
      * @return boolean true, if equivalent; false otherwise.
      */
    public boolean equals(Object o) {
      
      if (o == null || ! (o instanceof HighlightInfo)) return false;  // subclasses are defined
      
        
      HighlightInfo hi = (HighlightInfo)o;
      /*
       //System.out.println("p0: " + p0 + "  obj.p0: " + obj.p0);
       //System.out.println("p1: " + p1 + "  obj.p1: " + obj.p1);
       //System.out.println("p: " + p + "  obj.p: " + obj.p);
       */
      boolean result = getStartOffset() == hi.getStartOffset() && 
        getEndOffset() == hi.getEndOffset() &&
        getPainter() == hi.getPainter();
      
      //System.out.println("HighlightInfo.equals() = " + result);
      return result;
    }
    
    /** Overrides hashCode() for consistency with override of equals(...)  */
    public int hashCode() { return hash(getPainter(), getStartOffset(), getEndOffset()); }
    
    public void remove() { removeHighlight(this); }
    
    /** Accessor for the highlight tag.
      * @return the highlight tag (which may be null)
      */
    public Object getHighlightTag() { return _highlightTag; }
    
    /** Accessor for the painter
      * @return the painter
      */
    public Highlighter.HighlightPainter getPainter() { return _painter; }
    
    /** Accessor for the starting offset of this highlight
      * @return the start offset
      */
    public int getStartOffset() { return _startPos.getOffset(); }
    
    /** Accessor for the ending offset of this highlight
      * @return the end offset
      */
    public int getEndOffset() { return _endPos.getOffset(); }
    
    /** Tests to see if the given offsets correspond to the offsets specified within this highlight.
      * @param h a HighlightInfo object given the start and end offsets
      * @return true, if the supplied offsets are the same as those of this highlight.
      */
    public boolean matchesRegion(HighlightInfo h) {
      return (getStartOffset() == h.getStartOffset() && getEndOffset() == h.getEndOffset());
    }
    
    /** Refreshes this HighlightInfo object, obtaining a new Highlighter. */
    public void refresh(Highlighter.HighlightPainter p ) {
      
      this.remove();
      HighlightInfo newHighlight = addHighlight(getStartOffset(), getEndOffset(), p);
      
      _painter = p;
      // turn this HighlightInfo object into the newHighlight
      _highlightTag = newHighlight.getHighlightTag();
    }
  }
}
