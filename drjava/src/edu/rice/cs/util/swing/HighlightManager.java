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

package edu.rice.cs.util.swing;
  
import java.util.Stack;
import java.util.Vector;

import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.util.UnexpectedException;

public class HighlightManager {
    
    //private Hashtable<HighlightPosition, Stack<HighlightInfo>> _highlights;
 
    /**
     * An unsorted Vector of Stack<HighlightInfo>, each of which corresponds to a unique
     *  region in the document. All HighlightInfo objects within a given stack must correspond
     *  to the same region but must have unique Highlighter.HighlightPainters.
     * Each stack is ordered so the most recent highlight is at the top.
     */
    private Vector<Stack<HighlightInfo>> _highlights;
    
    /**
     * The component necessary for creating positions in in the document, which is also
     *  contained within this component.
     */
    private JTextComponent _component;
    
    /**
     * Constructor
     * @param jtc the component whose document will have positions created therein.
     */
    public HighlightManager( JTextComponent jtc) {
      _component = jtc;
      _highlights = new Vector<Stack<HighlightInfo>>();
    }
  
    /**
     * Adds a highlight using the supplied painter to the vector element(Stack) that exactly corresponds 
     *  to the specified bounds. The most recently added highlights over a given range appear
     *  on top of the older highlights. All highlights in a given range(Stack) must be unique, that is,
     *  each must use a different painter -- redundant highlights are shifted to the top of the 
     *  stack, but not added twice.
     * @param startOffset the offset at which the highlight is to begin.
     * @param endOffset the offset at which the highlight is to end.
     * @param p the Highlighter.HighlightPainter for painting
     * @return HighlightInfo the HighlightInfo object, for keeping a tag of a given highlight
     */
    public HighlightInfo addHighlight(int startOffset, int endOffset, Highlighter.HighlightPainter p) {
      
      HighlightInfo newLite = new HighlightInfo(startOffset,endOffset,p);
      
      //System.out.println("Adding highlight from "+startOffset+" to "+endOffset);      
      Stack<HighlightInfo> lineStack = _getStackAt(startOffset, endOffset);
      
      if (lineStack != null) {
        int searchResult = lineStack.search(newLite);
        if (searchResult == 1) return lineStack.peek();
        if (searchResult > 1) {
          lineStack.removeElement(newLite); 
        }
        
        HighlightInfo liteOnTop = lineStack.peek();
        _component.getHighlighter().removeHighlight( liteOnTop.getHighlightTag() );
       
      }
      else {
        //add a new Stack to the empty place in the hashtable
        lineStack = new Stack<HighlightInfo>();
        _highlights.addElement(lineStack);
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
          _highlights.removeElement(lineStack);
        }
        throw new UnexpectedException(ble);
      }
    }
    
    /**
     * Returning the Stack corresponding to the given region in the document, or null
     *  if there is none. Requires every Stack in the vector to have a unique region.
     * @param from the starting offset
     * @param to the ending offset
     * @return the corresponding Stack, or null
     */
    private Stack<HighlightInfo> _getStackAt ( int from, int to) {
      
      for (int i=0; i<_highlights.size(); i++) {

        Stack<HighlightInfo> stack = _highlights.elementAt(i);
        
        if (stack.isEmpty()) continue;
        
        if (stack.elementAt(0).matchesRegion(from, to)) {
          return _highlights.elementAt(i);
        }
      }
      //if here, no corresponding stack, so return null
      return null;
    }
    
    /**
     * Convenience method for removing a highlight with the specified start/end offsets and the given 
     *  painter. 
     *  @param startOffset the offset at which the desired highlight should start.
     *  @param endOffset the offset at which the desired highlight shoud end.
     *  @param p the Highlighter.HighlightPainter for painting
     */
    public void removeHighlight(int startOffset, int endOffset, Highlighter.HighlightPainter p) {
      HighlightInfo newLite = new HighlightInfo(startOffset,endOffset,p);
      removeHighlight(newLite);
    }
      
    /**
     * Removes a given highlight (HighlightInfo) from the highlighter
     *  @param newLite the HighlightInfo object corresponding to the highlight needed to be removed
     */
    public void removeHighlight (HighlightInfo newLite) {

      
      int startOffset = newLite.getStartOffset();
      int endOffset = newLite.getEndOffset();
      
      Stack<HighlightInfo> lineStack = _getStackAt(startOffset, endOffset);
      
      if (lineStack== null) {
        //System.out.println("Error! No stack to access in region from " + startOffset+ " to "+ endOffset);
        return;
      }
      
      int searchResult = lineStack.search(newLite);
      //System.out.println("searchResult: "+searchResult);
      
      if (searchResult == 1) {
        HighlightInfo liteToRemove = lineStack.pop();
        _component.getHighlighter().removeHighlight( liteToRemove.getHighlightTag());
        //System.out.println("Removed highlight @ "+startOffset);
        
        if (!lineStack.isEmpty()) {
          HighlightInfo liteOnTop = lineStack.peek();
          try {
            Object highlightTag = _component.getHighlighter().addHighlight(liteOnTop.getStartOffset(), 
                                                                liteOnTop.getEndOffset(), 
                                                                liteOnTop.getPainter());
            liteOnTop.setHighlightTag(highlightTag);
          }
          catch (BadLocationException ble) {
            throw new UnexpectedException(ble);
          }
        }

      }
      else if (searchResult > 1) {
        //System.out.println("Removing old instance...");
        lineStack.removeElement(newLite); 
      }
      
      if (lineStack.isEmpty()) {
        //System.out.println("Removing empty stack...");
        //remove the lineStack
        _highlights.removeElement(lineStack);
      }
      
    }
    
    /**
     * The public inner class defining a "smart" highlight, which can return the value of its start and end
     *  offsets for comparison with other highlights. Also keeps a tag to its actual highlight in the 
     *  component's highlighter for easy removal.
     */
    public class HighlightInfo {
      private Object _highlightTag;
      private Position _startPos;
      private Position _endPos;
      private Highlighter.HighlightPainter _p;
      
      /**
       * Constructor takes the bounds and the painter for a highlighter
       * @param from the offset at which the new highlight will start.
       * @param to the offset at which the new highlight will end.
       * @param p the Highlighter.HighlightPainter for painting
       */
      public HighlightInfo( int from, int to, Highlighter.HighlightPainter p) {
        
        _highlightTag = null;
        try {
          _startPos = _component.getDocument().createPosition(from);
          _endPos = _component.getDocument().createPosition(to);
        }
        catch (BadLocationException ble) {
          throw new UnexpectedException(ble);
        }
        
        _p = p;
      }
      
      /**
       * Set the highlight tag for later access to the highlight as it is stored in the components
       *  highlighter.
       * @param highlightTag the Object for keeping track of a stored highlight
       */
      public void setHighlightTag ( Object highlightTag) {
        _highlightTag = highlightTag;
      }
      
      /**
       * Tests equivalency of one HighlightInfo object with this HighlightInfo object. Compares start
       *  and end offsets, and the Highlighter.HighlightPainter -- returns true, if they are the same in both.
       *  @param o the other HighlightInfo object to compare to this one.
       *  @return boolean true, if equivalent; false otherwise.
       */
      public boolean equals( Object o) {
        
        if (o instanceof HighlightInfo) {
          
          HighlightInfo obj = (HighlightInfo)o;
          /*
           //System.out.println("p0: "+p0+"  obj.p0: "+obj.p0);
           //System.out.println("p1: "+p1+"  obj.p1: "+obj.p1);
           //System.out.println("p: "+p+"  obj.p: "+obj.p);
           */
          boolean result = ( matchesRegion(obj.getStartOffset(), obj.getEndOffset())
                              && _p == obj.getPainter());
          
          //System.out.println("HighlightInfo.equals() = "+result);
          return result;
        }
        else return false;
      }
      
      public void remove() {
        removeHighlight(this);
      }
          
      /**
       * Accessor for the highlight tag
       * @return the highlight tag
       */
      public Object getHighlightTag() {
        //might be null
        return _highlightTag;
      }
      
      /**
       * Accessor for the painter
       * @return the painter
       */
      public Highlighter.HighlightPainter getPainter() {
        return _p;
      }
      
      /**
       * Accessor for the starting offset of this highlight
       * @return the start offset
       */
      public int getStartOffset() {
        return _startPos.getOffset();
      }
      
      /**
       * Accessor for the ending offset of this highlight
       * @return the end offset
       */
      public int getEndOffset() {
        return _endPos.getOffset();
      }
      
      /**
       * Tests to see if the given offsets correspond to the offsets specified within this
       * highlight.
       * @param from the start offset
       * @param to the end offset
       * @return true, if the supplied offsets are the same as those of this highlight.
       */
      public boolean matchesRegion( int from, int to) {
        return (getStartOffset() == from && getEndOffset() == to);
      }
      
      /**
       * Refreshes this HighlightInfo object, obtaining a new Highlighter 
       */
      public void refresh ( Highlighter.HighlightPainter p ) {
                
        this.remove();
        HighlightInfo newHighlight = addHighlight(getStartOffset(), 
                                                  getEndOffset(),
                                                  p);
        _p = p;
        // turn this HighlightInfo object into the newHighlight
        _highlightTag = newHighlight.getHighlightTag();
      }
    }
}
