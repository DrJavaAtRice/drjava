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
 * Graphical form of a VectorOption for the Extra Classpath option.
 * Uses a file chooser for each String element.
 * @version $Id$
 */
public class VectorOptionComponent extends OptionComponent<Vector<File>>
  implements OptionConstants
{
  private JScrollPane _listScrollPane;
  private JPanel _panel;
  private JList _list;
  private JButton _addButton;
  private JButton _removeButton;
  private JButton _moveUpButton;
  private JButton _moveDownButton;
  private DefaultListModel _listModel;
  private FileFilter _fileFilter;
  private JFileChooser _jfc;

  public VectorOptionComponent (VectorOption<File> opt, String text, Frame parent) {
    super(opt, text, parent);

    //set up list
    _listModel = new DefaultListModel();
    _list = new JList(_listModel);
    _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    resetToCurrent();
    _fileFilter = ClasspathFilter.ONLY;

    /*
    Vector v = DrJava.getConfig().getSetting(_option);
    String[] array = new String[v.size()];
    v.copyInto(array);
    //_list.setListData(array);
    for (int i = 0; i < array.length; i++) {
      _listModel.add(array[i]);
    }
    */

    // set up JFileChooser
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    _jfc = new JFileChooser(workDir);
    _jfc.setDialogTitle("Select");
    _jfc.setApproveButtonText("Select");
    _jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _jfc.setMultiSelectionEnabled(true);

    _addButton = new JButton("Add");
    _addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        chooseFile();
        _list.setSelectedIndex(_listModel.getSize() - 1);
      }
    });

    _removeButton = new JButton("Remove");
    _removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          _listModel.remove(index);
          if (index == _listModel.getSize()) { // we removed the last element
            if (index > 0) // and there's more than one element in the list
            _list.setSelectedIndex(index - 1);
          }
          else
            _list.setSelectedIndex(index);
        }
      }
    });

    _moveUpButton = new JButton("Move Up");
    _moveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index > 0) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.add(index - 1, o);
            _list.setSelectedIndex(index - 1);
          }
        }
      }
    });

    _moveDownButton = new JButton("Move Down");
    _moveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index < _listModel.getSize() - 1) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.add(index + 1, o);
            _list.setSelectedIndex(index + 1);
          }
        }
      }
    });

    JPanel buttons = new JPanel();
    //buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    //buttons.add(Box.createGlue());
    buttons.add(_addButton);
    buttons.add(_removeButton);
    //buttons.add(Box.createGlue());
    buttons.add(_moveUpButton);
    buttons.add(_moveDownButton);
    //buttons.add(Box.createGlue());

    _listScrollPane = new JScrollPane(_list,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    _panel = new JPanel(new BorderLayout());
    _panel.add(_listScrollPane, BorderLayout.CENTER);
    _panel.add(buttons, BorderLayout.SOUTH);
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public VectorOptionComponent (VectorOption<File> opt, String text,
                                Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _listScrollPane.setToolTipText(description);
    _list.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    Vector<File> current = new Vector<File>();
    for (int i = 0; i < _listModel.getSize(); i++) {
      current.add((File)_listModel.getElementAt(i));
    }
    DrJava.getConfig().setSetting(_option, current);
    resetToCurrent();

    return true;
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
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _panel;
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
}
