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

import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.File;

/** Class for a simple document region that records positions rather than offsets.  See the WARNING below about hashing
  * on this type or its subtypes.
  * @version $Id$
  */
public class DocumentRegion implements OrderedDocumentRegion, Comparable<OrderedDocumentRegion> {
  protected final OpenDefinitionsDocument _doc;
  protected final File _file;
  protected volatile Position _startPosition; 
  protected volatile Position _endPosition;
  protected volatile DefaultMutableTreeNode _treeNode;
  
  /** Create a new simple document region using offsets.
    * @param doc document that contains this region, which cannot be null
    * @param file file that contains the region
    * @param so start offset of the region; if doc is non-null, then a Position will be created that moves within the document
    * @param eo end offset of the region; if doc is non-null, then a Position will be created that moves within the document
    */
  public DocumentRegion(OpenDefinitionsDocument doc, int so, int eo) {
    this(doc, createPosition(doc, so), createPosition(doc, eo));
  }
 
  /** Create a new simple document region with a bona fide document */
  public DocumentRegion(OpenDefinitionsDocument doc, Position sp, Position ep) {
    assert doc != null;
    _doc = doc;
    _file = doc.getRawFile();  // don't check the validity of _file here
    _startPosition = sp;
    _endPosition = ep;
  }
  
//  public DefaultMutableTreeNode getTreeNode() { return _treeNode; }
//  
//  public void setTreeNode(DefaultMutableTreeNode n) { _treeNode = n; }
  
  private static Position createPosition(OpenDefinitionsDocument doc, int i) {
    try { return doc.createPosition(i); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }

//  /** Structural equality method that copes with null!  This method should be a member of class Object. */
//  public static boolean equals(Object o1, Object o2) { 
//    if (o1 == null) return o2 == null;
//    return o1.equals(o2);
//  }
  
  /** Defines the equality relation on DocumentRegions.  This equivalence relation is consistent with the equivalence
    * relation induced by the compareTo method.
    */
  public final boolean equals(Object o) {
    if (o == null || ! (o instanceof IDocumentRegion)) return false;
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
  
  /** WARNING: The hashCode function is left unchanged making it inconsisent with equality.  Hence, only Identity based 
    * hash table should use this type as keys. */
      
//  /** This hash function is consistent with equality. */
//  public int hashCode() { return hash(_doc, getStartOffset(), getEndOffset()); }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file */
  public File getFile() { return _file; }

  /** @return the start offset */
  public int getStartOffset() { return _startPosition.getOffset(); }

  /** @return the end offset */
  public int getEndOffset() { return _endPosition.getOffset(); }
  
  /** @return the start position */
  public Position getStartPosition() { return _startPosition; }

  /** @return the end offset */
  public Position getEndPosition() { return _endPosition; }
  
  public String toString() {
    return (/* _doc != null ? */ _doc.toString() /* : "null" */) + "[" + getStartOffset() + " .. " + getEndOffset() + "]";
  }
}
