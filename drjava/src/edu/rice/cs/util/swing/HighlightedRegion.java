/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.util.Stack;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;

import edu.rice.cs.plt.tuple.Pair;

/** Class for a document region that is highlighted and that can move with
  * changes in the document.
  * @version $Id$ */
public class HighlightedRegion extends DocumentRegion implements IHighlighted {
  /** Stack of painters used to highlight this region, with the most recent highlight on top. */
  protected final Stack<PainterTag> _painters = new Stack<PainterTag>();
  /** The test component in which the highlighting will occur */
  protected final JTextComponent _component;
  
  /** Create a new highlighted region.
    * @param c the text component in which the highlighting will occur
    * @param sp the start position of the region
    * @param ep the end position of the region */
  public HighlightedRegion(JTextComponent c, Position sp, Position ep) {
    super(null, sp, ep);
    _component = c;
  }
  
  /** @return the text component in which the highlighting will occur */
  public JTextComponent getComponent() { return _component; }

  /** @return true if objects a and b are equal; null values are handled correctly. */
  public static boolean equals(Object a, Object b) {
    if (a == null) return (b == null);
    return a.equals(b);
  }  

  /** Add a painter.
    * @param p the painter to add
    * @return the tag associated with the newly added painter, or null if there was an error */
  public synchronized PainterTag addPainter(Highlighter.HighlightPainter p) {
    int i = 0;
    PainterTag found = null;
    for(PainterTag pt: _painters) {
      if (pt.painter==p) {
        // painter found, remove it, then push it back on top
        _removePainter(i, pt.tag);
        found = pt;
        break;
      }
      ++i;
    }
    if (found==null) {
      // painter not yet found, add it on top
      try {
        found = new PainterTag(p, _component.getHighlighter().addHighlight(_startPosition.getOffset(),
                                                                           _endPosition.getOffset(),
                                                                           p));
      }
      catch(BadLocationException e) { return null; }
    }
    _painters.push(found);
    return found;
  }
  
  /** Remove the painter with the specified tag.
    * @param t tag of the painter to remove */
  public synchronized void removePainter(PainterTag t) {
    int i = 0;
    for(PainterTag pt: _painters) {
      if (pt.tag==t) { _removePainter(i, pt.tag); return; }
      ++i;
    }
  }
  
  /** Remove the painter at the specified index.
    * @param index index of the painter to remove
    * @param tag tag of the painter to remove */
  protected void _removePainter(int index, Object tag) {
    assert (index >= 0);
    assert (index < _painters.size());
    _painters.removeElementAt(index);
    _component.getHighlighter().removeHighlight(tag);
  }
  
  /** Move the painter with the specified tag to the front. */
  public synchronized void movePainterToFront(PainterTag t) {
    int i = 0;
    for(PainterTag pt: _painters) {
      if (pt.tag==t.tag) {
        // painter found, remove it, then push it back on top
        _removePainter(i, pt.tag);
        _painters.push(pt);
        return;
      }
      ++i;
    }
  }
  
  /** Clear all painters. */
  public synchronized void clearPainters() {
    while(_painters.size()>0) {
      PainterTag pt = _painters.peek();
      _removePainter(0, pt.tag);
    }
  }
}
