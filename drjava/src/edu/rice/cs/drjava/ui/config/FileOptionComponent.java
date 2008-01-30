/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.event.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.FileSelectorComponent;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/** Graphical form of a FileOption.
 *
 *  TO DO: Replace the internal components here with an edu.rice.cs.util.swing.FileSelectorComponent.
 *
 *  @version $Id$
 */
public class FileOptionComponent extends OptionComponent<File> implements OptionConstants {

//  private JButton _button;
//  private JTextField _jtf;
//  private File _file;
//  private JFileChooser _jfc;
//  private FileFilter _fileFilter;  // null if not customized
  private FileSelectorComponent _component;

  public FileOptionComponent (FileOption opt, String text, Frame parent, JFileChooser jfc) {
    super(opt, text, parent);
//    _button = new JButton();
//    _button.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        chooseFile();
//      }
//    });
//    _button.setText("...");
//    _button.setMaximumSize(new Dimension(10,10));
//    _button.setMinimumSize(new Dimension(10,10));
//
//    _jtf = new JTextField();
//    _jtf.setColumns(30);
//
//    _jtf.setFont(_jtf.getFont().deriveFont(10f));
//    _jtf.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        chooseFileFromField();
//      }
//    });
//
//    _file = DrJava.getConfig().getSetting(_option);
//    _updateTextField(_file);
//
//    _jfc = jfc;
//    _fileFilter = null;
//
//    _panel = new JPanel();
//    _panel.setLayout(new BorderLayout());
//
//    _panel.add(_jtf, BorderLayout.CENTER);
//    _panel.add(_button, BorderLayout.EAST);
    _component = new FileSelectorComponent(parent, jfc, 30, 10f);
    File setting = DrJava.getConfig().getSetting(_option);
    if (setting != _option.getDefault()) {
      _component.setFileField(setting);
    }
    _component.getFileField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void removeUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void changedUpdate(DocumentEvent e) { notifyChangeListeners(); }
    });
  }

  /** Constructor that allows for a tooltip description. */
  public FileOptionComponent (FileOption opt, String text, Frame parent, String description, JFileChooser jfc) {
    this(opt, text, parent, jfc);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   *  @param description the tooltip text
   */
  public void setDescription(String description) {
    _component.setToolTipText(description);
//    _button.setToolTipText(description);
//    _jtf.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    File componentFile = _component.getFileFromField();
    File currentFile = DrJava.getConfig().getSetting(_option);
    
    if (componentFile != null && !componentFile.equals(currentFile)) {
      DrJava.getConfig().setSetting(_option, componentFile);
    }
    else if (componentFile == null) {
      DrJava.getConfig().setSetting(_option, _option.getDefault());
    }

    return true;
  }

  /** Displays the given value. */
  public void setValue(File value) {
//    _file = value;
//    _updateTextField(value);
    _component.setFileField(value);
  }

  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _component;
  }

//  /**
//   * Updates the text field to display the given file.
//   */
//  private void _updateTextField(File c) {
////    _jtf.setText(c.getAbsolutePath());
//    _component.setFileField(c);
//  }

  /**
   * Set the file filter for this file option component
   */
  public void setFileFilter(FileFilter fileFilter) {
//    _fileFilter = fileFilter;
    _component.setFileFilter(fileFilter);
  }

//  /**
//   * Shows a file chooser to pick a new file.  Allows picking directories.
//   */
//  public void chooseFile() {
//    if (_file != FileOption.NULL_FILE && _file.getParent() != null) {
//      _jfc.setCurrentDirectory(new File(_file.getParent()));
//    }
//
//    if (_fileFilter != null) {
//      _jfc.setFileFilter(_fileFilter);
//    }
//    File c = null;
//    int returnValue = _jfc.showDialog(_parent,
//                                     null);
//    if (returnValue == JFileChooser.APPROVE_OPTION)
//      c = _jfc.getSelectedFile();
//    if (c != null) {
//      _file = c;
//      _updateTextField(_file);
//    }
//  }

//  /**
//   *  The chooser method for the validation of filenames that are manually entered
//   *  into the text field.
//   *  @return False, if file does not exist. True, otherwise.
//   */
//  public boolean chooseFileFromField() {
//    String newValue = _jtf.getText().trim();
//    String currentValue = DrJava.getConfig().getSetting(_option).getAbsolutePath();
//
//    if (newValue.equals(currentValue)) {
//      return true;
//    }
//
//    File newFile = _option.parse(newValue);
//
//    if (newFile != null && !newFile.exists()) {
//      JOptionPane.showMessageDialog(_parent,
//                                    "The file '"+ newValue + "' is an invalid selection for\n" +
//                                    getLabelText() + " because it does not exist.",
//                                    "Invalid File Chosen for " + getLabelText() + "!",
//                                    JOptionPane.ERROR_MESSAGE);
//      return false;
//    }
//    else {
//      _file = newFile;
//      return true;
//    }
//  }
}
