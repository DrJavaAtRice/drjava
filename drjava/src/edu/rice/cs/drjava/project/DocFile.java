/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.project;

import java.io.File;
import java.io.IOException;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.plt.tuple.Pair;

public class DocFile extends AbsRelFile {
  
  private Pair<Integer, Integer> _sel;
  private Pair<Integer, Integer> _scroll;
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
  
  /* Relying on equals and hashCode methods inherited from File. */
  
  
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
    _sel = new Pair<Integer,Integer>(Integer.valueOf(start), Integer.valueOf(end));
  }  
  /** @return the selection with the first element being the vertical scroll and the second being the horizontal scroll. */
  public Pair<Integer,Integer> getScroll() { return _scroll; }
  
  /** @param scroll the pair to set the selection to */
  public void setScroll(Pair<Integer,Integer> scroll) { _scroll = scroll; }
  
  /** @param vert the vertical scroll of the scroll pane
   *  @param horiz the horizonal scroll of the scroll pane
   */
  public void setScroll(int vert, int horiz) {
    _scroll = new Pair<Integer,Integer>(Integer.valueOf(vert), Integer.valueOf(horiz));
  }
  
  /** @return  {@code true} if this file is supposed to be the active document when opened. */
  public boolean isActive() { return _active; }
  
  /** @param active  Whether this file should be the active document when opened. */
  
  public void setActive(boolean active) { _active = active; }
  /** @return the package of the document stored in this file */
  
  public String getPackage() { return _package; }
  
  /** @param pkg  The name of the package defined in the document text. */
  public void setPackage(String pkg) { _package = pkg; }
  
  /** Sets lastModified for this file to the time the including project file was saved. The <code>lastModified</code>
   *  date any documents saved after the project file was written will not match the date recorded in the project file.
   *  @param mod the last known modification date when the project was saved. 
   */
  public void setSavedModDate(long mod) { _mod = mod; }
  
  /** @return  The modification date of this file at the time the project file was generated. */
  public long getSavedModDate() { return _mod; }
}