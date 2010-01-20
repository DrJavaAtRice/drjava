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
package edu.rice.cs.util.swing;

import java.io.File;

/** This class is a wrapper for a file whose <code>toString</code> method outputs only the last element in the file
  * path.  If it's a file, then it outputs the file name without its parent directories.  If it's a  directory, then
  * it outputs the name of that directory only
  */
public class FileDisplay {
  
  private File _file;
  private String _rep;
  private boolean _isNew;
  
  protected FileDisplayManager _fdm;
  
  FileDisplay(File f, FileDisplayManager fdm) { 
    this(fdm);
    _file = f;
    _rep = formatRep(f);
  }
  
  FileDisplay(File parent, String child, FileDisplayManager fdm) {
    this(fdm);
    if (child == null || child.equals("")) {
      _file = new File(parent, ".");
    }
    else {
      _file = new File(parent, child);
    }
    _rep = formatRep(_file);
  }
  
  private FileDisplay(FileDisplayManager fdm) {
    _fdm = fdm;
  }
  
  public static FileDisplay newFile(File parent, FileDisplayManager fdm) {
    FileDisplay fd = new FileDisplay(parent, "", fdm);
    fd._isNew = true;
    fd._rep = getDefaultNewFileRep();
    return fd;
  }
  
  public File getParentFile() { return _file.getParentFile(); }
  
  public File getFile() { return _file; }
  
  /** If the representation of the file is different from the underlying
   * child string of the path, then the node represented by this file display
   * cannot be edited. If the user edited the text by giving a new representation,
   * there is no way to determine what the new child string of the path should be.
   * However, if the user is creating a new node in the tree, they will be able 
   * to edit it.
   */
  public boolean isEditable() { return (_isNew || (_file.canWrite() && _rep.equals(_file.getName()))); }
  
  public boolean isNew() { return _isNew; }
  
  public String getRepresentation() { return _rep; }
  
  public final String toString() { return _rep; }
  
  protected String formatRep(File file) {
    return _fdm.getName(file);
  }
    
  protected static String getDefaultNewFileRep() {
    return "New Folder";
  }
  
  
}