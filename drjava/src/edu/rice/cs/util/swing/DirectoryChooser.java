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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.awt.Component;

public class DirectoryChooser extends JFileChooser {
  
  /** GUI component that owns the dialog (if any) for this directory chooser. */
  protected Component _owner;
  
  /** File system root for chooser */
  protected File _root;
  
  /** Creates a DirectoryChooser rooted at file system root, allowing only a single selection. */
  public DirectoryChooser() { this(null, null, false, false); }
  
  /** Creates a DirectoryChooser rooted at the file system root, allowing only single selection. */
  public DirectoryChooser(Component owner) { this(owner, null, false, false); }
  
  /** Creates a DirectoryChooser rooted at the file system root, allowing multiple selection as specified.
    * @param allowMultiple whether to allow multiple selection
    */
  public DirectoryChooser(Component owner, boolean allowMultiple) { this(owner, null, allowMultiple, false); }
  
  /** Creates a DirectoryChooser with the given root, allowing only a single selection.
    * @param root the root directory to display in the tree
    */
  public DirectoryChooser(Component owner, File root) { this(owner, root, false, false); }
  
  /** Creates a DirectoryChooser with the given root, allowing multiple selections as specified.
    * @param root the root directory to display in the tree. If null, then show entire file system
    * @param allowMultiple whether to allow multiple selection
    */
  public DirectoryChooser(Component owner, File root, boolean allowMultiple, boolean showHidden) {
    /* This super call sets current directory to root if it is valid directory, root.parentFile() if it is a valid 
     * non-directory file, and the system default otherwise. */
    super(root);
    _init(owner, root, allowMultiple, showHidden);
  }
  
  /*---------- INITIALIZATION METHODS ----------*/
  
  /** Sets up the GUI components of the dialog */
  private void _init(Component owner, final File root, boolean allowMultiple, boolean showHidden) {
    
    
//    if (root != null && root.exists()) {
//      setFileView(new FileView() { 
//                    public Boolean isTraversable(File f) { 
//                      return Boolean.valueOf(f.isDirectory() && FileOps.inFileTree(f, root)); 
//                    }});
//    }
    
    _owner = owner;
    _root = root; // may be null
    if (root != null) {
      if (! root.exists()) _root = null;
      else if (! root.isDirectory()) _root = root.getParentFile();
    }
    
    setMultiSelectionEnabled(allowMultiple);
    setFileHidingEnabled(! showHidden);
    setFileSelectionMode(DIRECTORIES_ONLY);
    setDialogType(CUSTOM_DIALOG);
    setApproveButtonText("Select");
    setFileFilter(new FileFilter() {
      public boolean accept(File f) { return true; }
      public String getDescription() { return "All Folders"; }
    });
  }
  
  public int showDialog(File initialSelection) {
    setCurrentDirectory(initialSelection);
    return showDialog(_owner, null);  // null means leave the approve button text unchanged
  }
  
  /** Set the owner of this DirectoryChooser. */
  public void setOwner(Component owner) { _owner = owner; }
  
  /** Shows the dialog with the same selection as the last time the dialog was shown. If this is the first time it is
    * shown, then the root is selected.
    */
  public int showDialog() { return showDialog(_owner, null); }
  
  /** returns which directories were selected in the tree
    * @return an array of files for the selected directories
    */
  public File[] getSelectedDirectories() { return getSelectedFiles(); }
  
  /** returns which directory was selected in the tree
    * @return the file for the selected directory, null if none selected
    */
  public File getSelectedDirectory() { return getSelectedFile(); }
  
//  public boolean isTraversable(File f) {
//    if (_root == null) return super.isTraversable(f);
//    Utilities.show("isTraversable(" + f + ") called; _root = " + _root);
//    return f.isDirectory() && FileOps.inFileTree(f, _root);
//  }
}
