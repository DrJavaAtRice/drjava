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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;

import java.util.Vector;


/**
 * Graphical form of a VectorOption for the Extra Classpath/Sourcepath options.
 * Uses a file chooser for each File element.
 * @version $Id$
 */
public class VectorFileOptionComponent extends VectorOptionComponent<File>
  implements OptionConstants
{
  private FileFilter _fileFilter;
  private JFileChooser _jfc;
  protected JButton _moveUpButton;
  protected JButton _moveDownButton;
  
  public VectorFileOptionComponent (VectorOption<File> opt, String text, Frame parent) {
    super(opt, text, parent);

    _moveUpButton = new JButton(new AbstractAction("Move Up") {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index > 0) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.insertElementAt(o, index - 1);
            _list.setSelectedIndex(index - 1);
          }
        }
      }
    });

    _moveDownButton = new JButton(new AbstractAction("Move Down") {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index < _listModel.getSize() - 1) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.insertElementAt(o, index + 1);
            _list.setSelectedIndex(index + 1);
          }
        }
      }
    });
    _buttonPanel.add(_moveUpButton);
    _buttonPanel.add(_moveDownButton);

    // set up JFileChooser
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File(System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    _jfc = new JFileChooser(workDir);
    _jfc.setDialogTitle("Select");
    _jfc.setApproveButtonText("Select");
    _jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _jfc.setMultiSelectionEnabled(true);
    _fileFilter = ClasspathFilter.ONLY;
  }
  
  /**
   * Constructor that allows for a tooltip description.
   */
  public VectorFileOptionComponent (VectorOption<File> opt, String text,
                                Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /**
   * Displays the given value.
   */
  public void setValue(Vector<File> value) {
    File[] array = new File[value.size()];
    value.copyInto(array);
    _listModel.clear();
    for (int i = 0; i < array.length; i++) {
      _listModel.addElement(array[i]);
    }
  }

  /**
   * Set the file filter for this vector option component
   */
  public void setFileFilter(FileFilter fileFilter) {
    _fileFilter = fileFilter;
  }
  
  /**
   * Shows a file chooser for adding a file to the element.
   */
  public void chooseFile() {
    File selection = (File) _list.getSelectedValue();
    if (selection != null) {
      File parent = selection.getParentFile();
      if (parent != null) {
        _jfc.setCurrentDirectory(parent);
      }
    }

    _jfc.setFileFilter(_fileFilter);

    File[] c = null;
    int returnValue = _jfc.showDialog(_parent, null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      c = _jfc.getSelectedFiles();
    }
    if (c != null) {
      for(int i = 0; i < c.length; i++) {
        _listModel.addElement(c[i]);
      }
    }
  }
  
  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent ae) {
        chooseFile();
        _list.setSelectedIndex(_listModel.getSize() - 1);
      }
    };
  }
}
