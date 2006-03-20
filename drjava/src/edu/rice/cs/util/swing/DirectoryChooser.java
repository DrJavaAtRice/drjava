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

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

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
   *  @param allowMultiple whether to allow multiple selection
   */
  public DirectoryChooser(Component owner, boolean allowMultiple) { this(owner, null, allowMultiple, false); }
  
  /** Creates a DirectoryChooser with the given root, allowing only a single selection.
   *  @param root the root directory to display in the tree
   */
  public DirectoryChooser(Component owner, File root) { this(owner, root, false, false); }
  
  /** Creates a DirectoryChooser with the given root, allowing multiple selections as specified.
   *  @param root the root directory to display in the tree. If null, then show entire file system
   *  @param allowMultiple whether to allow multiple selection
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
//                      return Boolean.valueOf(f.isDirectory() && FileOps.isInFileTree(f, root)); 
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
  
  /** Shows the dialog with the same selection as the last time the dialog was shown. If this is the first time it is
   *  shown, then the root is selected.
   */
  public int showDialog() { return showDialog(_owner, null); }
  
  /** returns which directories were selected in the tree
   *  @return an array of files for the selected directories
   */
  public File[] getSelectedDirectories() { return getSelectedFiles(); }
  
  /** returns which directory was selected in the tree
   *  @return the file for the selected directory, null if none selected
   */
  public File getSelectedDirectory() { return getSelectedFile(); }
  
//  public boolean isTraversable(File f) {
//    if (_root == null) return super.isTraversable(f);
//    Utilities.show("isTraversable(" + f + ") called; _root = " + _root);
//    return f.isDirectory() && FileOps.isInFileTree(f, _root);
//  }
}
