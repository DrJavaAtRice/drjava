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

import java.io.File;
import java.lang.ref.WeakReference;
import javax.swing.text.Position;

/**
 * Class for a document region that remains static all the time and does not respond to changes in the document.
 * @version $Id$Region
 */
public class StaticDocumentRegion implements DocumentRegion {
  protected final OpenDefinitionsDocument _doc;
  protected final File _file;
  protected final int _startOffset;
  protected final int _endOffset;
  protected final String _string;
  
  /** Create a new static document region. Precondition: s != null. */
  public StaticDocumentRegion(OpenDefinitionsDocument doc, File file, int so, int eo, String s) {
    assert s != null;
    _doc = doc;
    _file = file;
    _startOffset = so;
    _endOffset = eo;
    _string = s;
  }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file */
  public File getFile() { return _file; }

  /** @return the start offset */
  public int getStartOffset() {
    return (_doc == null || _doc.getLength() >= _startOffset) ? _startOffset : _doc.getLength();
  }

  /** @return the end offset */
  public int getEndOffset() {
    return (_doc == null || _doc.getLength()>=_endOffset) ? _endOffset : _doc.getLength();
  }
  
  /** @return the string it was assigned */
  public String getString() {
    return _string;
  }
  
  private static boolean equals(OpenDefinitionsDocument doc1, OpenDefinitionsDocument doc2) {
    if (doc1 == null) return (doc2 == null);
    if (doc2 == null) return false;
    return doc1.equals(doc2);
  }
  
  private static boolean equals(File f1, File f2) {
    if (f1 == null) return (f2 == null);
    if (f2 == null) return false;
    return f1.equals(f2);
  }
  
  /** @return true if the specified region is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || !(other instanceof StaticDocumentRegion)) return false;
    StaticDocumentRegion o = (StaticDocumentRegion) other;
    return (equals(_doc, o._doc) && equals(_file, o._file) && _startOffset == o._startOffset &&
            _endOffset == o._endOffset && _string.equals(o._string));
  }
  
  public String toString() {
    return (_doc != null ? _doc.toString() : "null") + " " + _startOffset + " .. " + _endOffset;
  }
}
