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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.config.*;

/**
 * Manages a list of the most recently used files to be displayed
 * in the File menu.
 * @version $Id$
 */
public class RecentFileManager implements OptionConstants {
  /**
   * position in the file menu for the next insert
   */
  protected int _pos;

  /**
   * All of the recently used files in the list, in order.
   */
  protected Vector<File> _recentFiles;

  /**
   * Menu items corresponding to each file in _recentFiles.
   */
  protected Vector<JMenuItem> _recentMenuItems;

  /**
   * The maximum number of files to display in the list.
   */
  protected int MAX = DrJava.getConfig().getSetting(RECENT_FILES_MAX_SIZE).intValue();

  /**
   * The File menu containing the entries.
   */
  protected JMenu _fileMenu;

  /**
   * true if the file manager manages recent project files
   */
  protected boolean _isProject;
  
  /**
   * The action to perform on any of the menu items.
   */
  Action _open;

  /**
   * The MainFrame containing the file menu.
   */
  MainFrame _frame;

  /**
   * Creates a new RecentFileManager.
   * @param pos Position in the file menu
   * @param fileMenu File menu to add the entry to
   * @param frame MainFrame containing the File menu
   */
  public RecentFileManager(int pos, JMenu fileMenu, MainFrame frame, boolean isProject) {
    _pos = pos;
    _fileMenu = fileMenu;
    _frame = frame;
    _recentFiles = new Vector<File>();
    _recentMenuItems = new Vector<JMenuItem>();
    _isProject = isProject;

    // Add each of the files stored in the config
    Vector<File> files;
    if(! _isProject)
      files = DrJava.getConfig().getSetting(RECENT_FILES);
    else
      files = DrJava.getConfig().getSetting(RECENT_PROJECTS);
    for (int i = files.size() - 1; i >= 0; i--) {
      updateOpenFiles(files.get(i));
    }
  }

  /**
   * Returns the list of recently used files, in order.
   */
  public Vector<File> getFileVector() {
    return _recentFiles;
  }

  /**
   * Changes the maximum number of files to display in the list.
   * @param newMax The new maximum number of files to display
   */
  public void updateMax(int newMax) {
    MAX = newMax;
  }

  /**
   * Saves the current list of files to the config object.
   */
  public void saveRecentFiles() {
    if(! _isProject)
      DrJava.getConfig().setSetting(RECENT_FILES,_recentFiles);
    else
      DrJava.getConfig().setSetting(RECENT_PROJECTS,_recentFiles);
  }

  /**
   * Updates the list after the given file has been opened.
   */
  public void updateOpenFiles(final File file) {

    if (_recentMenuItems.size() == 0) {
      _fileMenu.insertSeparator(_pos);  //one at top
      _pos++;
    }


    final FileOpenSelector _recentSelector = new FileOpenSelector() {
      public File[] getFiles() {
        return new File[] { file };
      }
    };

    JMenuItem newItem = new JMenuItem("");
    newItem.addActionListener(new AbstractAction("Open " + file.getName()) {
      public void actionPerformed(ActionEvent ae) {
        if(file.getName().endsWith(".pjt"))
          _frame.openProject(_recentSelector);
        else
          _frame.open(_recentSelector);
        // These two lines fix the problem in 1.3 where clicking on a file
        // in the recent file list will not show the caret, but they cause
        // 1.4.1 to show a phantom caret that won't do anything.
//        _frame.getCurrentDefPane().requestFocus();
//        _frame.getCurrentDefPane().getCaret().setVisible(true);
      }
    });
    removeIfInList(file);
    _recentMenuItems.add(0,newItem);
    _recentFiles.add(0,file);
    numberItems();
    _fileMenu.insert(newItem,_pos);

  }

  /**
   * Removes the given file from the list if it is already there.
   * Only removes the first occurrence of the file, since each
   * entry should be unique (based on canonical path).
   */
  public void removeIfInList(File file) {
    // Use canonical path if possible
    File canonical = null;
    try {
      canonical = file.getCanonicalFile();
    }
    catch (IOException ioe) {
      // Oh well, compare against the file as is
    }

    for (int i = 0; i < _recentFiles.size(); i++) {
      File currFile = _recentFiles.get(i);
      boolean match;
      if (canonical != null) {
        try {
          match = currFile.getCanonicalFile().equals(canonical);
        }
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

  /**
   * Trims the recent file list to the configured size and numbers the
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
    for (int i=0; i< _recentMenuItems.size(); i++ ) {
      currItem = _recentMenuItems.get(i);
      currItem.setText((i+1) + ". " + _recentFiles.get(i).getName());
    }
    // remove the separator
    if (MAX == 0 && !wasEmpty) {
      _fileMenu.remove(--_pos);
    }
  }
}
