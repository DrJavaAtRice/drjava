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

import java.io.File;
import java.lang.ref.WeakReference;
import javax.swing.text.Position;

import edu.rice.cs.util.FileOps;

/**
 * Class for a document region that can move with changes in the document; its text, however, remains constant.
 * @version $Id$Region
 */
public class MovingDocumentRegion extends SimpleDocumentRegion {
  protected final String _string;
  
  /** Create a new moving document region. */
  public MovingDocumentRegion(OpenDefinitionsDocument doc, File file, Position sp, Position ep, String s) {
    super(doc, sp, ep);
    _string = s;
  }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file */
  public File getFile() { return _file; }

  /** @return the start offset */
  public int getStartOffset() {
    return 
      (_doc == null || _doc.getLength() >= _startPosition.getOffset()) ? _startPosition.getOffset() : _doc.getLength();
  }

  /** @return the end offset */
  public int getEndOffset() {
    return (_doc == null || _doc.getLength() >= _endPosition.getOffset()) ? _endPosition.getOffset() : _doc.getLength();
  }
  
  /** @return the string it was assigned */
  public String getString() {
    return _string;
  }
  
  /** @return true if objects a and b are equal; null values are handled correctly. */
  public static boolean equals(Object a, Object b) {
    if (a == null) return (b == null);
    return a.equals(b);
  }
  
//  /** @return true if the specified region is equal to this one. */
//  public boolean equals(Object other) {
//    if (other == null || other.getClass() != this.getClass()) return false;
//    MovingDocumentRegion o = (MovingDocumentRegion) other;
//    return equals(_doc, o._doc) && equals(_file, o._file) &&
//            _startPosition.getOffset() == o._startPosition.getOffset() &&
//            _endPosition.getOffset() == o._endPosition.getOffset() &&
//            _string.equals(o._string);
//  }
//  
//  /** @return the hash code. */
//  public int hashCode() {
//    int result;
//    result = (_doc != null ? _doc.hashCode() : 0);
//    result = 31 * result + (_file != null ? _file.hashCode() : 0);
//    result = 31 * result + (_startPosition != null ? _startPosition.hashCode() : 0);
//    result = 31 * result + (_endPosition != null ? _endPosition.hashCode() : 0);
//    result = 31 * result + (_string != null ? _string.hashCode() : 0);
//    return result;
//  }
//  
  public String toString() {
    return 
      (_doc != null ? _doc.toString() : "null") + " " + _startPosition.getOffset() + " .. " + _endPosition.getOffset();
  }
}
