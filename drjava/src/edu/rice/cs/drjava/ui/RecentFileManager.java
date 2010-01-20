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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;

import edu.rice.cs.util.FileOpenSelector;

/**
 * Manages a list of the most recently used files to be displayed
 * in the File menu.
 * @version $Id$
 */
public class RecentFileManager implements OptionConstants {
  /** Position in the file menu for the next insert. */
  protected int _pos;
  
  /** All of the recently used files in the list, in order. */
  protected Vector<File> _recentFiles;
  
  /** Menu items corresponding to each file in _recentFiles. */
  protected Vector<JMenuItem> _recentMenuItems;
  
  /** The maximum number of files to display in the list. */
  protected int MAX = DrJava.getConfig().getSetting(RECENT_FILES_MAX_SIZE).intValue();
  
  /** The File menu containing the entries. */
  protected JMenu _fileMenu;
  
  /** The OptionConstant that should be used to retrieve the list of recent files. */
  protected VectorOption<File> _settingConfigConstant;
  
  /** An action that will be invoked when the file is clicked. */
  protected RecentFileAction _recentFileAction;
  
  /** Creates a new RecentFileManager.
    * @param pos  Position in the file menu
    * @param fileMenu  File menu to add the entry to
    */
  public RecentFileManager(int pos, JMenu fileMenu, RecentFileAction action, VectorOption<File> settingConfigConstant) {
    _pos = pos;
    _fileMenu = fileMenu;
    _recentFileAction = action;
    _recentFiles = new Vector<File>();
    _recentMenuItems = new Vector<JMenuItem>();
    _settingConfigConstant = settingConfigConstant;
    
    // Add each of the files stored in the config
    Vector<File> files = DrJava.getConfig().getSetting(_settingConfigConstant);
    
    for (int i = files.size() - 1; i >= 0; i--) {
      File f = files.get(i);
      if (f.exists()) updateOpenFiles(f); 
    }
  }
  
  /** Returns the list of recently used files, in order. */
  public Vector<File> getFileVector() { return _recentFiles; }
  
  /** Changes the maximum number of files to display in the list.
    * @param newMax The new maximum number of files to display
    */
  public void updateMax(int newMax) { MAX = newMax; }
  
  /** Saves the current list of files to the config object. */
  public void saveRecentFiles() {
    DrJava.getConfig().setSetting(_settingConfigConstant, _recentFiles);
  }
  
  /** Updates the list after the given file has been opened. */
  public void updateOpenFiles(final File file) {
    
    if (_recentMenuItems.size() == 0) {
      _fileMenu.insertSeparator(_pos);  //one at top
      _pos++;
    }
    
    final FileOpenSelector recentSelector = new FileOpenSelector() {
      public File[] getFiles() { return new File[] { file }; }
    };
    
    JMenuItem newItem = new JMenuItem("");
    newItem.addActionListener(new AbstractAction("Open " + file.getName()) {
      public void actionPerformed(ActionEvent ae) {
        if (_recentFileAction != null) {
          _recentFileAction.actionPerformed(recentSelector);
        }
      }
    });
    try { newItem.setToolTipText(file.getCanonicalPath()); }
    catch(IOException e) {
      // don't worry about it at this point
    }
    removeIfInList(file);
    _recentMenuItems.add(0,newItem);
    _recentFiles.add(0,file);
    numberItems();
    _fileMenu.insert(newItem,_pos);
  }
  
  /** Removes the given file from the list if it is already there.
    * Only removes the first occurrence of the file, since each
    * entry should be unique (based on canonical path).
    */
  public void removeIfInList(File file) {
    // Use canonical path if possible
    File canonical = null;
    try { canonical = file.getCanonicalFile(); }
    catch (IOException ioe) {
      // Oh well, compare against the file as is
    }
    
    for (int i = 0; i < _recentFiles.size(); i++) {
      File currFile = _recentFiles.get(i);
      boolean match;
      if (canonical != null) {
        try { match = currFile.getCanonicalFile().equals(canonical); }
        catch (IOException ioe) {
          // Oh well, compare the files themselves
          match = currFile.equals(file);
        }
      }
      else {
        // (couldn't find canonical for file; compare as is)
        match = currFile.equals(file);
      }
      
      if (match) {
        _recentFiles.remove(i);
        JMenuItem menuItem = _recentMenuItems.get(i);
        _fileMenu.remove(menuItem);
        _recentMenuItems.remove(i);
        break;
      }
    }
  }
  
  /** Trims the recent file list to the configured size and numbers the
   * remaining files according to their position in the list
   */
  public void numberItems() {
    int delPos = _recentMenuItems.size();
    boolean wasEmpty = (delPos == 0);
    while (delPos > MAX) {
      JMenuItem delItem = _recentMenuItems.get(delPos - 1);
      _recentMenuItems.remove(delPos - 1);
      _recentFiles.remove(delPos - 1);
      _fileMenu.remove(delItem);
      
      delPos = _recentMenuItems.size();
    }
    JMenuItem currItem;
    for (int i = 0; i < _recentMenuItems.size(); i++ ) {
      currItem = _recentMenuItems.get(i);
      currItem.setText((i+1) + ". " + _recentFiles.get(i).getName());
    }
    // remove the separator
    if (MAX == 0 && !wasEmpty) { _fileMenu.remove(--_pos); }
  }
  
  /** This interface is to be implemented and passed to the manager
   * upon creation. This action specifies what is performed when the
   * user selects a file from the list
   */
  public interface RecentFileAction {    
    public void actionPerformed(FileOpenSelector selector);
  }
}
