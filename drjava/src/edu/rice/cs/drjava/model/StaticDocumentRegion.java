/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.util.UnexpectedException;

import java.util.List;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/** Class for a simple document region that only records region offsets, not positions.  Instances of this class
  * represent dummy regions used in searching SortSets of DocumentRegions.  Hence, getLineStart() and 
  * getLineEnd() should never be called.  
  * 
  * WARNING: this class overrides the equals method but does not override the 
  * hashCode method to maintain consistency.  Hence, instances can only be 
  * used as keys in identity based hash tables.  NOTE: since class instances
  * are mutable, a hashCode method consistent with equals WOULD NOT WORK anyway.
  * @version $Id$
  */
public class StaticDocumentRegion implements OrderedDocumentRegion {
  protected final OpenDefinitionsDocument _doc;

  // The following two fields are ignored in subclasses of StaticDocumentRegion
  protected volatile Position _start; 
  protected volatile Position _end;  // _end >= _start
 
  /** Create a new simple document region with a bona fide document and offsets
    * @param doc    the document within which to create the region; it cannot be null
    * @param start  the start offset
    * @param end    the end offset
    */
  public StaticDocumentRegion(OpenDefinitionsDocument doc, int start, int end) {
    assert doc != null;
    assert end >= start; // split in two to help diagnose bug 2906538: AssertionError After Go To Find Result and Edit
    _doc = doc;

    try {
      _start = _doc.createPosition(start);
      _end = _doc.createPosition(end);
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }

  }
  
  /** Defines the equality relation on DocumentRegions.  This equivalence relation is consistent with the equivalence
    * relation induced by the compareTo method.  NOTE: using DocumentRegions as hash keys does not work because this
    * class is mutable and this class does not override hashCode (which would not work anyway!).
    */
  @Override
  public final boolean equals(Object o) {
    if (o == null || ! (o instanceof IDocumentRegion)) return false;
    IDocumentRegion r = (IDocumentRegion) o;
    return getDocument() == r.getDocument() && getStartOffset() == r.getStartOffset() && getEndOffset() == r.getEndOffset();
  }
  
  /** A trivial override of hashCode to satisfy javac, which complains if hashCode is not overridden.
    * WARNING: This overriding leaves the hashCode function unchanged making it inconsisent with equality.  Hence, only
    * Identity based hash tables should use this type as keys. Since this class is mutable, there is NO overriding of 
    * hashCode that will work for conventional (equals-based) hash tables.*/
  @Override
  public final int hashCode() { return super.hashCode(); }
  
  /** Totally orders regions lexicographically based on (_doc, endOffset, startOffset). This method is typically applied
    * to regions within the same document. 
    */
  @Override
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
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the start offset */
  public int getStartOffset() { return _start.getOffset(); }

  /** @return the end offset */
  public int getEndOffset() { return _end.getOffset(); }
  
  /** Throws exception indicating that getLineStart() is not supported. */
  public int getLineStartOffset() { 
    throw new UnsupportedOperationException("StaticDocumentRegion does not suppport getLineStart()"); 
  }
  
  /** Throws exception indicating that getLineEnd() is not supported. */
  public int getLineEndOffset() { 
    throw new UnsupportedOperationException("StaticDocumentRegion does not suppport getLineEnd()"); 
  }
  
  /** Throws exception indicating that getString() is not supported. */
  public String getString() { 
    throw new UnsupportedOperationException("StaticDocumentRegion does not suppport getString()"); 
  }
  
  public boolean isEmpty() { return getStartOffset() == getEndOffset(); }
  
  public String toString() {
    return (/* _doc != null ? */ _doc.toString() /* : "null" */) + "[" + getStartOffset() + " .. " + getEndOffset() + "]";
  }
}
