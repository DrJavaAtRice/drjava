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
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Graphical form of a FileOption.
 * @version $Id$
 */
public class FileOptionComponent extends OptionComponent<File> 
  implements OptionConstants {
  
  private JButton _button;
  private JTextField _jtf;
  private File _currentFile;
  private File _newFile;
  private JFileChooser _jfc;
  private JPanel _panel;
  
  public FileOptionComponent (FileOption opt, String text, Frame parent) {
    super(opt, text, parent);
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseFile();
      }
    });
    _button.setText("...");
    _button.setMaximumSize(new Dimension(10,10));
    _button.setMinimumSize(new Dimension(10, 10));
    
    _jtf = new JTextField();
    _jtf.setColumns(30);
    
    _jtf.setFont(_jtf.getFont().deriveFont(10f));
    _jtf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean tf = chooseFileFromField();
      }
    });
    
    _currentFile = DrJava.CONFIG.getSetting(_option);
    _newFile = _currentFile;
    _updateTextField(_currentFile);
    
    File workDir = DrJava.CONFIG.getSetting(WORKING_DIRECTORY);
       
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    _jfc = new JFileChooser(workDir);
    
    _panel = new JPanel();
    _panel.setLayout(new BorderLayout());
     
    _panel.add(_jtf, BorderLayout.CENTER);
    _panel.add(_button, BorderLayout.EAST);
  }
  
  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
        
    boolean validChoice = chooseFileFromField();
    if (!validChoice) return false;
    
    if (!_newFile.equals(_currentFile)) {
      DrJava.CONFIG.setSetting(_option, _newFile);
      _currentFile = _newFile;
    }
    
    return true;
  } 
  
  /**
   * Resets this component to the current config value.
   */
  public void resetToCurrent() {
    _newFile = _currentFile;
    _updateTextField(_newFile);
  }
  
  /**
   * Resets this component to the option's default value.
   */
  public void resetToDefault() {
    _newFile = _option.getDefault();
    _updateTextField(_newFile);
  }
  
  /**
   * Displays the given value.
   */
  public void setDisplay(File value) {
    _updateTextField(value);
  }
  
  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() { 
    return _panel;
  }
  
  /**
   * Updates the text field to display the given file.
   */
  private void _updateTextField(File c) {    
    _jtf.setText(c.getAbsolutePath());
  }

  /**
   * Shows a file chooser to pick a new file.  Allows picking directories.
   */
  public void chooseFile() {

    if (_newFile != FileOption.NULL_FILE && _newFile.getParent() != null) {
      _jfc.setCurrentDirectory( new File(_newFile.getParent()));
    }
    
    _jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    File c = null;
    int returnValue = _jfc.showDialog(_parent,
                                     null);
    if (returnValue == JFileChooser.APPROVE_OPTION) 
      c = _jfc.getSelectedFile();
    if (c != null) {
      _newFile = c;
      _updateTextField(_newFile);
    }
    
  }
    
  /**
   *  The chooser method for the validation of filenames that are manually entered
   *  into the text field.
   *  @return False, if file does not exist. True, otherwise.
   */
  public boolean chooseFileFromField() {
   String newValue = _jtf.getText().trim();
   String currentValue = _currentFile.getAbsolutePath();
     
   if (newValue.equals(currentValue)) return true;

   File newFile = _option.parse(newValue);
   
   if (newFile != null && !newFile.exists()) {
     JOptionPane.showMessageDialog(_parent, 
                                   "The file '"+ newValue+"' is an invalid selection for\n" +
                                   getLabelText() + " because it does not exist.", 
                                   "Invalid File Chosen for "+ getLabelText() +"!", 
                                   JOptionPane.ERROR_MESSAGE);
     return false;
   }
  
   _newFile = newFile;
     
   return true;
  }
  
}