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

package edu.rice.cs.util.swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.*;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;

/** A JPanel with a text box and a "..." button used to select a file or directory.  The file name is editable in the
  * text box, and a JFileChooser is displayed if the user clicks the "..." button.
  * TODO: make this inherit from FileSelectorStringComponent or factor the common code into an abstract class!  Duplicated code!
  * @version $Id: FileSelectorComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class FileSelectorComponent extends JPanel {
  
  /** The default number of columns for the text box. */
  public static final int DEFAULT_NUM_COLS = 30;
  
  /** The default font size for the text box. */
  public static final float DEFAULT_FONT_SIZE = 10f;
  
  /** The parent frame of this component. */
  protected final SwingFrame _parent;
  
  /** Text field with the name of the selected file. */
  protected final JTextField _fileField;
  
  /** "..." button to open the file chooser. */
  protected final JButton _chooserButton;
  
  /** File chooser to open when clicking the "..." button. */
  protected final JFileChooser _chooser;
  
  /** File filter to use in the chooser. */
  protected volatile FileFilter _fileFilter;
  
  /** The current file */
  protected volatile File _file;
  
  /** True if file must exist. */
  protected volatile boolean _mustExist;
  
  /** Creates a new FileSelectorComponent with default dimensions.
    * @param parent  Parent of this component.
    * @param chooser File chooser to display from the "..." button.
    */
  public FileSelectorComponent(SwingFrame parent, JFileChooser chooser) {
    this(parent, chooser, DEFAULT_NUM_COLS, DEFAULT_FONT_SIZE, true);
  }
  
  /** Creates a new FileSelectorComponent.
    * @param parent   Parent of this component.
    * @param chooser  File chooser to display from the "..." button.
    * @param numCols  Number of columns to display in the text field
    * @param fontSize Font size for the text field
    */
  public FileSelectorComponent(SwingFrame parent, JFileChooser chooser, int numCols, float fontSize) {
    this(parent, chooser, numCols, fontSize, true);
  }
  
  /** Creates a new FileSelectorComponent.
    * @param parent    Parent of this component; may be null if the FileSelector is supposed to stand-alone.
    * @param chooser   File chooser to display from the "..." button.
    * @param numCols   Number of columns to display in the text field
    * @param fontSize  Font size for the text field
    * @param mustExist force selection of existing file
    */
  public FileSelectorComponent(SwingFrame parent, JFileChooser chooser, int numCols, float fontSize, boolean mustExist) {
    
    if (chooser == null) throw new UnexpectedException("Error: called new FileSelectorComponent(...) with a null chooser!");
    
    _parent = parent;
    _chooser = chooser;
    _fileFilter = null;
    _mustExist = mustExist;
    
    _fileField = new JTextField(numCols) {
      public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
      }
    };
    
    _fileField.setFont(_fileField.getFont().deriveFont(fontSize));
    _fileField.setPreferredSize(new Dimension(22, 22));
    _fileField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { validateTextField(); }
    });
    
    _fileField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) { /* validateTextField(); */ }
      public void focusLost(FocusEvent e) { validateTextField(); }
    });
    
    _chooserButton = new JButton("...");
    _chooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _chooseFile(); }
    });
    
    _chooserButton.setMaximumSize(new Dimension(22, 22));
    _chooserButton.setMargin(new Insets(0, 5 ,0, 5));
    
    // Add components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(_fileField);
    this.add(_chooserButton);
  }
  
  public void setEnabled(boolean isEnabled) {
    _fileField.setEnabled(isEnabled);
    _chooserButton.setEnabled(isEnabled);
    super.setEnabled(isEnabled);
  }
  
  /** Returns the file text field. */
  public JTextField getFileField() { return _fileField; }
  
  /** Returns the file chooser. */
  public JFileChooser getFileChooser() { return _chooser; }
  
  /** Returns the file currently typed into the file field. */
  public File getFileFromField() {
    String txt = _fileField.getText().trim();
    if (txt.equals("")) _file = FileOps.NULL_FILE;
    else _file = new File(txt);
    
    return _file;
  }
  
  /** Sets the text of the file field to be the given file.
    * @param file File to display in the file field.
    */
  public void setFileField(File file) {
    _file = file;
    if (file != null && !file.getPath().equals("")) {
      try {_file = file.getCanonicalFile(); }
      catch(IOException e) { /* do nothing */ }
    }
    resetFileField();
  }
  
  public void resetFileField() {
    if (_file == null) _fileField.setText("");
    else {
      _fileField.setText(_file.getPath());
      _fileField.setCaretPosition(_fileField.getText().length());
    }
  }
  
  /** Sets the file filter to use. */
  public void setFileFilter(FileFilter filter) { _fileFilter = filter; }
  
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    _fileField.setToolTipText(text);
    _chooser.setToolTipText(text);
  }
  
  /** Opens the file chooser to select a file, putting the result in the file field. */
  protected void _chooseFile() {
    // Set the chooser to be in the right directory
    if (_file != null && _file.exists()) {
      _chooser.setCurrentDirectory(_file);
      _chooser.setSelectedFile(_file);
    }
    
    // Apply the filter
    if (_fileFilter != null) _chooser.setFileFilter(_fileFilter);
    
    // Get the file from the chooser
    int returnValue = _chooser.showDialog(_parent, null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File chosen = _chooser.getSelectedFile();
      if (chosen != null) setFileField(chosen);
    }
  }
  
  // used so that the focus listener and the action listener do not
  // both validate the incorrect text.  This ensures that only the first
  // one gets it.
//  private boolean _validationInProgress = false;
  
  /** The chooser method for the validation of filenames that are manually entered into the text field.
    * @return False, if file does not exist. True, otherwise.
    */
  public boolean validateTextField() {
//    if (_validationInProgress) return true;
//    _validationInProgress = true;
    
    String newValue = _fileField.getText().trim();
    
    File newFile = FileOps.NULL_FILE;
    if (! newValue.equals(""))
      newFile = new File(newValue);
    
    if (newFile != FileOps.NULL_FILE && _mustExist && !newFile.exists()) {
      JOptionPane.showMessageDialog(_parent, "The file '" +  newValue + "'\nis invalid because it does not exist.",
                                    "Invalid File Name", JOptionPane.ERROR_MESSAGE);
      if (_file != null && ! _file.exists()) _file = FileOps.NULL_FILE;
      resetFileField(); // revert if not valid
      
//      _validationInProgress = false;
      return false;
    }
    else {
      setFileField(newFile);
//      _validationInProgress = false;
      return true;
    }
  }
}
