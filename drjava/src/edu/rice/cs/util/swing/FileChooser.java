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

import edu.rice.cs.plt.io.IOUtil;

import java.io.File;

import javax.swing.JFileChooser;

public class FileChooser extends JFileChooser {
  
  /** File system root for chooser */
  protected File _root;
  
  /** Creates a FileChooser with the given root, allowing only a single selection WITHIN the specified file system
    * @param root the root directory to display in the tree
    */
  public FileChooser(File root) { 
    super(root);
    _init(root);
  }
  
  /*---------- INITIALIZATION METHODS ----------*/
  
  /** Sets up the GUI components of the dialog */
  private void _init(final File root) {
    
    setRoot(root);
    
    setFileSelectionMode(FILES_ONLY);
    setDialogType(CUSTOM_DIALOG);
    setApproveButtonText("Select");
  }
  
  public void setRoot(File root) {
    _root = root; // may be null
    if (root != null) {
      if (! root.exists()) _root = null;
      else if (! root.isDirectory()) _root = root.getParentFile();
    }
  }
  
  public File getRoot() { return _root; }
  
  public boolean isTraversable(File f) {
    if (_root == null) return super.isTraversable(f);
//    Utilities.show("isTraversable(" + f + ") called; _root = " + _root);
    return f != null && f.isDirectory() && IOUtil.isMember(f, _root);
  }
}
