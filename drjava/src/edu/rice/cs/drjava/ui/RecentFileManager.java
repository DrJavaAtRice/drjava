/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
  public RecentFileManager(int pos, JMenu fileMenu, MainFrame frame) {
    _pos = pos;
    _fileMenu = fileMenu;
    _frame = frame;
    _recentFiles = new Vector<File>();
    _recentMenuItems = new Vector<JMenuItem>();

    // Add each of the files stored in the config
    Vector<File> files = DrJava.getConfig().getSetting(RECENT_FILES);
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
    DrJava.getConfig().setSetting(RECENT_FILES,_recentFiles);
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
      public File[] getFiles() throws OperationCanceledException {
        return new File[] { file };
      }
    };

    JMenuItem newItem = new JMenuItem("");
    newItem.addActionListener(new AbstractAction("Open " + file.getName()) {
      public void actionPerformed(ActionEvent ae) {
        _frame.open(_recentSelector);
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
      boolean match = false;
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
