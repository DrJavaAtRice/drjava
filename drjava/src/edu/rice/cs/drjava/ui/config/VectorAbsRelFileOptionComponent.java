/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;


import java.awt.event.*;
import java.io.File;
import edu.rice.cs.util.AbsRelFile;
import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;
import java.util.List;

import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.CheckBoxJList;

/** Graphical form of a VectorOption for the Extra Classpath/Sourcepath options.
  * Uses a file chooser for each AbsRelFile element.
 *  @version $Id: VectorAbsRelFileOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class VectorAbsRelFileOptionComponent extends VectorOptionComponent<AbsRelFile> implements OptionConstants {
  private FileFilter _fileFilter;
  private JFileChooser _jfc;
  protected File _baseDir = null;
  
  public VectorAbsRelFileOptionComponent (VectorOption<AbsRelFile> opt, String text, SwingFrame parent) {
    this(opt, text, parent, null);
  }
  
  /** Constructor that allows for a tooltip description. */
  public VectorAbsRelFileOptionComponent (VectorOption<AbsRelFile> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /** Constructor with flag for move buttons. */
  public VectorAbsRelFileOptionComponent (VectorOption<AbsRelFile> opt, String text, SwingFrame parent,
                                          String description, boolean moveButtonEnabled) {
    super(opt, text, parent, new String[] { "File", "Absolute" }, description, moveButtonEnabled);  // creates all four buttons

    // Absolute column
    _table.getColumnModel().getColumn(1).setMinWidth(80);
    _table.getColumnModel().getColumn(1).setMaxWidth(80);
    
    // set up JFileChooser
    File workDir = new File(System.getProperty("user.home"));

    _jfc = new JFileChooser(workDir);
    _jfc.setDialogTitle("Select");
    _jfc.setApproveButtonText("Select");
    _jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _jfc.setMultiSelectionEnabled(true);
    _fileFilter = ClassPathFilter.ONLY;
    
    final TableCellRenderer renderer = _table.getTableHeader().getDefaultRenderer();
    int w = renderer.getTableCellRendererComponent(_table,_table.getModel().getColumnName(1), false, false, 0, 1).getPreferredSize().width;
    _table.getColumnModel().getColumn(1).setPreferredWidth(w);
  }
  
  /** Returns the table model. Can be overridden by subclasses. */
  protected AbstractTableModel _makeTableModel() {
    return new AbstractTableModel() {
      public String getColumnName(int col) { return (_columnNames.length == 0)?super.getColumnName(col):_columnNames[col]; }
      public int getRowCount() { return _data.size(); }
      public int getColumnCount() { return 2; }
      public Object getValueAt(int row, int col) {
        switch(col) {
          case 0: return _data.get(row);
          case 1: return _data.get(row).keepAbsolute();
        }
        throw new IllegalArgumentException("Illegal column");
      }
      public Class<?> getColumnClass(int col) {
        switch(col) {
          case 0: return String.class;
          case 1: return Boolean.class;
        }
        throw new IllegalArgumentException("Illegal column");
      }
      public boolean isCellEditable(int row, int col) {
        if (col<1) {
          return false;
        } else {
          return true;
        }
      }
      public void setValueAt(Object value, int row, int col) {
        AbsRelFile f = _data.get(row);
        switch(col) {
          case 1:
            f.keepAbsolute((Boolean)value);
            break;
          default:
            throw new IllegalArgumentException("Illegal column");
        }
        fireTableCellUpdated(row, col);
      }
    };
  }

  /** Set the file filter for this vector option component. */
  public void setFileFilter(FileFilter fileFilter) {
    _fileFilter = fileFilter;
  }
  
  /** Sets the directory where the chooser will start if no file is selected. */
  public void setBaseDir(File f) {
    if (f.isDirectory()) { _baseDir = f; }
  }
  
  /** Shows a file chooser for adding a file to the element. */
  public void chooseFile() {
    int[] rows = _table.getSelectedRows();
    File selection = (rows.length==1)?_data.get(rows[0]):null;
    if (selection != null) {
      File parent = selection.getParentFile();
      if (parent != null) {
        _jfc.setCurrentDirectory(parent);
      }
    }
    else {
      if (_baseDir != null) { _jfc.setCurrentDirectory(_baseDir); }
    }

    _jfc.setFileFilter(_fileFilter);

    File[] c = null;
    int returnValue = _jfc.showDialog(_parent, null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      c = _jfc.getSelectedFiles();
    }
    if (c != null) {
      _table.getSelectionModel().clearSelection();
      for(int i = 0; i < c.length; i++) {
        _addValue(new AbsRelFile(c[i]));
      }
    }
  }
  
  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent ae) {
        chooseFile();
      }
    };
  }
}
