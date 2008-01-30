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

import javax.swing.text.Position;
import java.io.File;

/**
 * Class for a simple document region. If a document is provided, then the region will move within the document.
 * @version $Id$
 */
public class SimpleDocumentRegion implements DocumentRegion {
  protected final OpenDefinitionsDocument _doc;
  protected final File _file;
  protected volatile int _startOffset;
  protected volatile int _endOffset;
  protected volatile Position _startPos = null;
  protected volatile Position _endPos = null;
  
  /** Create a new simple document region.
    * @param doc document that contains this region, or null if we don't have a document yet
    * @param file file that contains the region
    * @param so start offset of the region; if doc is non-null, then a Position will be created that moves within the document
    * @param eo end offset of the region; if doc is non-null, then a Position will be created that moves within the document
    */
  public SimpleDocumentRegion(OpenDefinitionsDocument doc, File file, int so, int eo) {
    _doc = doc;
    _file = file;
    _startOffset = so;
    _endOffset = eo;
    if (_doc != null) {
      try {
        _startPos = _doc.createPosition(so);
        _endPos = _doc.createPosition(eo);
      }
      catch(javax.swing.text.BadLocationException e) { /* ignore, offset will be static */ }
    }
  }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file */
  public File getFile() { return _file; }

  /** @return the start offset */
  public int getStartOffset() {
    if (_startPos != null) {
      // if we have a position that moves within the document, update the offset
      _startOffset = _startPos.getOffset();
    }
    return _startOffset;
  }

  /** @return the end offset */
  public int getEndOffset() {
    if (_endPos != null) {
      // if we have a position that moves within the document, update the offset
      _endOffset = _endPos.getOffset();
    }
    return _endOffset;
  }
  
  /** Structural equality method that copes with null!  This method should be a member of class Object. */
  public static boolean equals(Object o1, Object o2) { 
    if (o1 == null) return o2 == null;
    return o1.equals(o2);
  }
  
  /** @return true if the specified region is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != getClass()) return false;
    SimpleDocumentRegion o = (SimpleDocumentRegion) other;
    return equals(_doc, o._doc) && equals(_file, o._file) &&
            _startPos.getOffset() == o._startPos.getOffset() &&
            _endPos.getOffset() == o._endPos.getOffset();
  }
  
  /** @return the hash code. */
  public int hashCode() {
    int result;
    result = (_doc != null ? _doc.hashCode() : 0);
    result = 31 * result + (_file != null ? _file.hashCode() : 0);
    result = 31 * result + (_startPos != null ? _startPos.hashCode() : 0);
    result = 31 * result + (_endPos != null ? _endPos.hashCode() : 0);
    result = 31 * result + _startOffset;
    result = 31 * result + _endOffset;
    return result;
  }

  public String toString() {
    return (_doc != null ? _doc.toString() : "null") + " "+_startOffset+" .. "+_endOffset;
  }
}
