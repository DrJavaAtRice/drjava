/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.project;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.util.Pair;

public class DocFile extends File {
  
  private Pair<Integer,Integer> _sel;
  private Pair<Integer,Integer> _scroll;
  private boolean _active;
  private String _package;
  private long _mod;
  
  /** Creates a docfile that has the same path as the given file, with default values for everything else. */
  public DocFile(File f) { this(f, null, null, false, null); }
  
  /** Creates a docfile from the given pathname with default values for everything else. */
  public DocFile(String pathname) { this(pathname, null, null, false, null); }
  
  /** Creates a docfile from the given parent and child with default values for everything else. */
  public DocFile(String parent, String child) { this(parent, child, null, null, false, null); }
  
  public DocFile(String pathname, Pair<Integer,Integer> selection, Pair<Integer,Integer> scroll, boolean active, String srcRoot) {
    super(pathname);
    init(selection, scroll, active, srcRoot);
  }
  public DocFile(File f, Pair<Integer,Integer> selection, Pair<Integer,Integer> scroll, boolean active, String srcRoot) {
    super(f, "");
    init(selection, scroll, active, srcRoot);
  }
  
  public DocFile(String parent, String child, Pair<Integer,Integer> selection, Pair<Integer,Integer> scroll, boolean active, String srcRoot) {
    super(parent, child);
    init(selection, scroll, active, srcRoot);
  }
  
  private void init(Pair<Integer,Integer> selection, Pair<Integer,Integer> scroll, boolean active, String pack) {
    _sel = selection;
    _scroll = scroll;
    _active = active;
    _package = pack;
  }
  ///////////////////// Overriden Methods //////////////////////
  
  
  public DocFile getAbsoluteFile() {
    if (isAbsolute()) return this;
    else
      return new DocFile(super.getAbsoluteFile(), _sel, _scroll, _active, _package);
  }
  
  public DocFile getCanonicalFile() throws IOException {
    return new DocFile(super.getCanonicalFile(), _sel, _scroll, _active, _package);
  }
  ///////////////////// Extra Data Methods /////////////////////
  
  /** @return the selection with the start and ending locations paired together.  The cursor location is at the second
   *  location in the pair while the selection is defined between the two.
   */
  public Pair<Integer,Integer> getSelection() { return _sel; }
  
  /** @param sel the pair to set the selection to */
  public void setSelection(Pair<Integer,Integer> sel) { _sel = sel; }
  
  /** @param start the start of the selection
   *  @param end the end of the selection (and the cursor position)
   */
  public void setSelection(int start, int end) {
    _sel = new Pair<Integer,Integer>(new Integer(start), new Integer(end));
  }  
  /** @return the selection with the first element being the vertical scroll and the second being the horizontal scroll. */
  public Pair<Integer,Integer> getScroll() { return _scroll; }
  
  /** @param scroll the pair to set the selection to */
  public void setScroll(Pair<Integer,Integer> scroll) { _scroll = scroll; }
  
  /** @param vert the vertical scroll of the scroll pane
   *  @param horiz the horizonal scroll of the scroll pane
   */
  public void setScroll(int vert, int horiz) {
    _scroll = new Pair<Integer,Integer>(new Integer(vert), new Integer(horiz));
  }
  
  /** @return true if this file is supposed to be the active document when opened. */
  public boolean isActive() { return _active; }
  
  /** @param active whether this file should be the active document when opened. */
  
  public void setActive(boolean active) { _active = active; }
  /** @return the package of the document stored in this file */
  
  public String getPackage() { return _package; }
  
  /** @param pack the name of the package defined in the document text. */
  public void setPackage(String pkg) { _package = pkg; }
  
  /** Sets lastModified for this file to the time the including project file was saved. The <code>lastModified</code>
   *  date any documents saved after the project file was written will not match the date recorded in the project file.
   *  @param mod the last known modification date when the project was saved. 
   */
  public void setSavedModDate(long mod) { _mod = mod; }
  
  /** @return the modification date of this file at the time the project file was generated. */
  public long getSavedModDate() { return _mod; }
}