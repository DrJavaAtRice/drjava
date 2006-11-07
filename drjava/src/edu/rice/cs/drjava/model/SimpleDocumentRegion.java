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
        _startPos = _doc.createWrappedPosition(so);
        _endPos = _doc.createWrappedPosition(eo);
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
  
  /** @return true if the specified region is equal to this one. */
  public boolean equals(Object other) {
    if (!(other instanceof SimpleDocumentRegion) || (other==null)) return false;
    SimpleDocumentRegion o = (SimpleDocumentRegion)other;
    return (((_doc == null && o._doc == null) || _doc.equals(o._doc)) &&
            ((_file == null && o._file == null) || _file.equals(o._file)) &&
            _startPos.getOffset() == o._startPos.getOffset() &&
            _endPos.getOffset() == o._endPos.getOffset());
  }
  
  public String toString() {
    return (_doc != null ? _doc.toString() : "null") + " "+_startOffset+" .. "+_endOffset;
  }
}
