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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.*;

/**
 * A JPanel with a text box and a "..." button used to select
 * a file or directory.  The file name is editable in the text
 * box, and a JFileChooser is displayed if the user clicks the
 * "..." button.
 *
 * @version $Id$
 */
public class FileSelectorComponent extends JPanel {
  /**
   * The default number of columns for the text box.
   */
  public static final int DEFAULT_NUM_COLS = 30;

  /**
   * The default font size for the text box.
   */
  public static final float DEFAULT_FONT_SIZE = 10f;


  /**
   * The parent frame of this component.
   */
  protected final Frame _parent;

  /**
   * Text field with the name of the selected file.
   */
  protected final JTextField _fileField;

  /**
   * "..." button to open the file chooser.
   */
  protected final JButton _chooserButton;

  /**
   * File chooser to open when clicking the "..." button.
   */
  protected final JFileChooser _chooser;

  /**
   * File filter to use in the chooser.
   */
  protected FileFilter _fileFilter;
  
  /**
   * The current file
   */
  protected File _file;

  /**
   * Creates a new FileSelectorComponent with default dimensions.
   *
   * @param parent  Parent of this component.
   * @param chooser File chooser to display from the "..." button.
   */
  public FileSelectorComponent(Frame parent, JFileChooser chooser) {
    this(parent, chooser, DEFAULT_NUM_COLS, DEFAULT_FONT_SIZE);
  }

  /**
   * Creates a new FileSelectorComponent.
   *
   * @param parent   Parent of this component.
   * @param chooser  File chooser to display from the "..." button.
   * @param numCols  Number of columns to display in the text field
   * @param fontSize Font size for the text field
   */
  public FileSelectorComponent(Frame parent, JFileChooser chooser,
                               int numCols, float fontSize) {
    _parent = parent;
    _chooser = chooser;
    _fileFilter = null;
    _file = null;
    
    _fileField = new JTextField(numCols) {
      public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
      }
    };
    _fileField.setFont(_fileField.getFont().deriveFont(fontSize));
    _fileField.setPreferredSize(new Dimension(22,22));
    _fileField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        validateTextField();
      }
    });
    _fileField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        validateTextField();
      }
      public void focusLost(FocusEvent e) {
        validateTextField();
      }
    });
    
    _chooserButton = new JButton("...");
    _chooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _chooseFile();
      }
    });
    _chooserButton.setMaximumSize(new Dimension(22, 22));
    _chooserButton.setMargin(new Insets(0,5,0,5));
    // Add components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(_fileField);
    this.add(_chooserButton);
  }

  public void setEnabled(boolean enabled) {
    _fileField.setEnabled(enabled);
    _chooserButton.setEnabled(enabled);
    super.setEnabled(enabled);
  }
  

  /**
   * Returns the file text field.
   */
  public JTextField getFileField() {
    return _fileField;
  }

  /**
   * Returns the file chooser.
   */
  public JFileChooser getFileChooser() {
    return _chooser;
  }

  /**
   * Returns the file currently typed into the file field.
   */
  public File getFileFromField() {
    String txt = _fileField.getText().trim();
    if (txt.equals(""))
      _file = null;
    else 
      _file = new File(txt);
    
    return _file;
  }

  /**
   * Sets the text of the file field to be the given file.
   *
   * @param file File to display in the file field.
   */
  public void setFileField(File file) {
    if (file != null && file.exists()) {
      try {
        _file = file.getCanonicalFile();
      }
      catch(IOException e) {
        //handle it gracefully
        _file = file.getAbsoluteFile();
      }
      resetFileField();
    }
  }

  public void resetFileField() {
    if (_file == null) {
      _fileField.setText("");
    }
    else {
      _fileField.setText(_file.toString());
      _fileField.setCaretPosition(_fileField.getText().length());
    }
  }
  
  /**
   * Sets the file filter to use.
   */
  public void setFileFilter(FileFilter filter) {
    _fileFilter = filter;
  }

  public void setToolTipText(String text) {
    super.setToolTipText(text);
    _fileField.setToolTipText(text);
    _chooser.setToolTipText(text);
  }
  
  /**
   * Opens the file chooser to select a file, putting the result
   * in the file field.
   */
  private void _chooseFile() {
    // Set the chooser to be in the right directory
    if (_file != null && _file.exists()) {
      _chooser.setCurrentDirectory(_file);
      _chooser.setSelectedFile(_file);
    }

    // Apply the filter
    if (_fileFilter != null) {
      _chooser.setFileFilter(_fileFilter);
    }

    // Get the file from the chooser
    int returnValue = _chooser.showDialog(_parent, null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File chosen = _chooser.getSelectedFile();
      if (chosen != null) {
        setFileField(chosen);
      }
    }
  }
   
  // used so that the focus listener and the action listener do not
  // both validate the incorrect text.  This ensures that only the first
  // one gets it.
  private boolean _validationInProgress = false;
  
  /**
   *  The chooser method for the validation of filenames that are manually entered
   *  into the text field.
   *  @return False, if file does not exist. True, otherwise.
   */
  public boolean validateTextField() {
    if (_validationInProgress) return true;
    _validationInProgress = true;
    
    String newValue = _fileField.getText().trim();
    
    File newFile = null;
    if (!newValue.equals(""))
      newFile = new File(newValue);
    
    if (newFile != null && !newFile.exists()) {
      JOptionPane.showMessageDialog(_parent,
                                    "The file '"+ newValue + "'\n" +
                                    "is invalid because it does not exist.",
                                    "Invalid File Name",
                                    JOptionPane.ERROR_MESSAGE);
      resetFileField(); // revert if not valid
      _validationInProgress = false;
      return false;
    }
    else {
      setFileField(newFile);
      _validationInProgress = false;
      return true;
    }
  }
}