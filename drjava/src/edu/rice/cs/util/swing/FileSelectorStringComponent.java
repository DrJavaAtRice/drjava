/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.*;

/** Just like FileSelectorComponent, but it converts the file to a different string that gets displayed. */
public class FileSelectorStringComponent extends JPanel {
  
  /** The default number of columns for the text box. */
  public static final int DEFAULT_NUM_COLS = 30;

  /** The default font size for the text box. */
  public static final float DEFAULT_FONT_SIZE = 10f;

  /** The parent component of this component. */
  protected final Component _parent;

  /** Text field with the name of the selected file. */
  protected final JTextField _textField;

  /**  "..." button to open the file chooser. */
  protected final JButton _chooserButton;

  /** File chooser to open when clicking the "..." button. */
  protected FileChooser _chooser;

  /** The current file */
  protected File _file;
  
  /** Creates a new DirectorySelectorStringComponent with default dimensions.
   *  @param parent  Parent of this component.
   *  @param chooser File chooser to display from the "..." button.
   */
  public FileSelectorStringComponent(Component parent, FileChooser chooser) {
    this(parent, chooser, DEFAULT_NUM_COLS, DEFAULT_FONT_SIZE);
  }

  /** Creates a new DirectorySelectorStringComponent.
   *  @param parent   Parent of this component.
   *  @param chooser  File chooser to display from the "..." button.
   *  @param numCols  Number of columns to display in the text field
   *  @param fontSize Font size for the text field
   */
  public FileSelectorStringComponent(Component parent, FileChooser chooser, int numCols, float fontSize) {
    _parent = parent;
    _chooser = chooser;
    _file = null;
    
    _textField = new JTextField(numCols) {
      public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height); }
    };
    _textField.setFont(_textField.getFont().deriveFont(fontSize));
    _textField.setPreferredSize(new Dimension(22,22));
    
    _chooserButton = new JButton("...");
    _chooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _chooseFile(); }
    });
    _chooserButton.setMaximumSize(new Dimension(22, 22));
    _chooserButton.setMargin(new Insets(0,5,0,5));
    // Add components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(_textField);
    this.add(_chooserButton);
  }

  public void setEnabled(boolean isEnabled) {
    _textField.setEnabled(isEnabled);
    _chooserButton.setEnabled(isEnabled);
    super.setEnabled(isEnabled);
  }
  
  /** Returns the file text field. */
  public JTextField getTextField() { return _textField; }

  /** Returns the file chooser. */
  public FileChooser getFileChooser() { return _chooser; }

  /** Sets the file chooser. */
  public void setFileChooser(FileChooser c) { _chooser = c; }

  /** Converts a string representation from the text field into a File. */
  public File convertStringToFile(String s) {
    s = s.trim();
    if (s.equals("")) return null;
    return new File(s);
  }

  /** Converts a file to the string representation of the text field. */
  public String convertFileToString(File f) {    
    if (f == null)  return "";
    return f.toString();
  }
  
  /** Returns the last file that was selected. */
  public File getFileFromField() {
    // Get the file from the chooser
    String newValue = _textField.getText();
    File newFile = null;
    if (! newValue.equals("")) {
      newFile = convertStringToFile(newValue);
      if (! newFile.isDirectory() && ! _chooser.isFileSelectionEnabled()) {
        newFile = newFile.getParentFile();
      }
    }
    
    if (newFile != null && ! newFile.exists()) newFile = _file;

    return newFile;
  }

  /** Returns the string in the text field. */
  public String getText() { return _textField.getText(); }

  /** Sets the string in the text field. */
  public void setText(String s) { _textField.setText(s); }
  
  /** Sets the text of the file field to be the given file.
   *  @param file File to display in the file field.
   */
  public void setFileField(File file) {
    _file = file;
    if (file != null && ! file.getPath().equals("")) {
      try { _file = file.getCanonicalFile(); }
      catch(IOException e) { /* do nothing */ }
    }
    resetFileField();
  }

  public void resetFileField() {
    _textField.setText(convertFileToString(_file));
    _textField.setCaretPosition(_textField.getText().length());
  }

  public void setToolTipText(String text) {
    super.setToolTipText(text);
    _textField.setToolTipText(text);
    _chooserButton.setToolTipText(text);
  }

  /** Adds a filter to decide if a directory can be chosen. */
  public void addChoosableFileFilter(FileFilter filter) {
    _chooser.addChoosableFileFilter(filter);
  }
  
  /** Removes the given filefilter from the chooser */
  public void removeChoosableFileFilter(FileFilter filter) {
    _chooser.removeChoosableFileFilter(filter);
  }
  
  public void clearChoosableFileFilters() {
    _chooser.resetChoosableFileFilters();
  }

  /** Opens the file chooser to select a file, putting the result in the file field. */
  private void _chooseFile() { 
    File f = getFileFromField();
    if (f != null && f.exists()) {
      _chooser.setCurrentDirectory(f);
      _chooser.setSelectedFile(f);
    }
    
    int returnValue = _chooser.showDialog(_parent, null);
    if (returnValue == FileChooser.APPROVE_OPTION) {
      File chosen = _chooser.getSelectedFile();
      if (chosen != null) { setFileField(chosen); }
    }
  }

}