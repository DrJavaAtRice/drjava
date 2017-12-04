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

import java.util.List;
import java.util.ArrayList;

import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/** Class for document regions that are totally ordered by allocation chronology.  They do not conform to the invariants
  * required for OrderedDocumentRegions.
  * Warning: this class defines compareTo which implicitly defines a coarser equality relation than equals
  * @version $Id$
  */
public class BrowserDocumentRegion implements IDocumentRegion, Comparable<BrowserDocumentRegion> {

  private static volatile int _indexCounter = 0;   // sequence number counter for browser regions
  private final int _index;                        // unique sequence number for this region
  protected final OpenDefinitionsDocument _doc;    // document for this region
  protected final File _file;                      // file for this region
  protected final Position _startPosition;         // start position for this region
  protected final Position _endPosition;           // final position for this region
//  protected volatile DefaultMutableTreeNode _treeNode;
  
  /** Create a new simple document region with a bona fide document.
    * @param doc document that contains this region
    * @param sp start position of the region 
    * @param ep end position of the region
    */
  public BrowserDocumentRegion(OpenDefinitionsDocument doc, Position sp, Position ep) {
    assert doc != null;
    _index = _indexCounter++;    // sequence number records allocation order
    _doc = doc;
    _file = doc.getRawFile();    // don't check the validity of _file here
    _startPosition = sp;
    _endPosition = ep;
//    _treeNode = null;
  }
  
  /* Relying on default equals operation. */
  
  /** This hash function is consistent with equals. */
  public int hashCode() { return _index; }
  
  /** This relation is consistent with equals. */
  public int compareTo(BrowserDocumentRegion r) { return _index - r._index; }
  
  /** Override of equals that is consistent with hashCode. */
  public boolean equals(Object o) { 
    if (o.getClass() != BrowserDocumentRegion.class) return false;
    BrowserDocumentRegion obdr = (BrowserDocumentRegion) o;  // cannot fail
    return obdr.hashCode() == hashCode();
  }
  
  public boolean equiv(BrowserDocumentRegion bdr) { 
    return (_doc == bdr._doc) && (getStartOffset() == bdr.getStartOffset());
  }
  
  public int getIndex() { return _index; }
  
  /** @return the document. */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file. */
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
    return _doc.toString() + "[" + getStartOffset() + "]";
  }
}
