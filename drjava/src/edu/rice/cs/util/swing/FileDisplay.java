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

package edu.rice.cs.util.swing;

import java.io.File;

/**
 * This class is a wrapper for a file whose <code>toString</code> method
 * outputs only the last element in the file path.  If it's a file, then
 * it outputs the file name without its parent directories.  If it's a 
 * directory, then it outputs the name of that directory only
 */
public class FileDisplay {
  
  private File _file;
  private String _rep;
  private boolean _repIsDifferent; // if the representation is different from the child string
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
  
  /**
   * If the representation of the file is different from the underlying
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