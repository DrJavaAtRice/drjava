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
import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.config.*;

public class RecentFileManager implements OptionConstants {
    int _pos; // position in the file menu for the next insert
    Vector<File> _recentFiles;
    Vector<JMenuItem> _recentMenuItems;
    int MAX = DrJava.CONFIG.getSetting(RECENT_FILES_MAX_SIZE).intValue();
    JMenu _fileMenu;
    Action _open;
    MainFrame _frame;
    
    public RecentFileManager(int pos, JMenu fileMenu, MainFrame frame) {
      _pos = pos;
      _fileMenu = fileMenu;
      _frame = frame;
      _recentFiles = new Vector<File>();
      _recentMenuItems = new Vector<JMenuItem>();
      Vector<File> files = DrJava.CONFIG.getSetting(RECENT_FILES);
      for (int i = files.size() - 1; i >= 0; i--) {
        updateOpenFiles(files.elementAt(i));
      }
    }
    
    public Vector<File> getFileVector() {
      return _recentFiles;
    }
    
    public void updateMax(int newMax) {
      MAX = newMax;
    }
    
    public void saveRecentFiles() { 
      DrJava.CONFIG.setSetting(RECENT_FILES,_recentFiles);
    }
    
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
      _recentMenuItems.insertElementAt(newItem,0);
      _recentFiles.insertElementAt(file,0);
      numberItems();
      _fileMenu.insert(newItem,_pos);
     
    }
    
    public void removeIfInList(File file) {
      int index = _recentFiles.indexOf(file);
      
      if (index >= 0) {
        _recentFiles.removeElementAt(index);
        JMenuItem delItem = _recentMenuItems.elementAt(index);
        _fileMenu.remove(delItem);
        _recentMenuItems.removeElementAt(index);
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
        JMenuItem delItem = _recentMenuItems.elementAt(delPos - 1);
        _recentMenuItems.removeElementAt(delPos - 1);
        _recentFiles.removeElementAt(delPos - 1);
        _fileMenu.remove(delItem);

        delPos = _recentMenuItems.size();
      }
      JMenuItem currItem;
      String itemText;
      for (int i=0; i< _recentMenuItems.size(); i++ ) {
        currItem = _recentMenuItems.elementAt(i);
        currItem.setText((i+1) + ". " + _recentFiles.elementAt(i).getName());        
      }
      // remove the separator
      if (MAX == 0 && !wasEmpty) {
        _fileMenu.remove(--_pos);
      }     
    }
  }