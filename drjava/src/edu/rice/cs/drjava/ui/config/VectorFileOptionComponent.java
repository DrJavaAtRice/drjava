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
import javax.swing.filechooser.FileFilter;
import javax.swing.*;

import java.util.Vector;
import java.util.List;

import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.SwingFrame;

/** Graphical form of a VectorOption for the Extra Classpath/Sourcepath options.
  * Uses a file chooser for each File element.
 *  @version $Id: VectorFileOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class VectorFileOptionComponent extends VectorOptionComponent<File> implements OptionConstants {
  private JFileChooser _jfc;
  protected File _baseDir = null;
  
  public VectorFileOptionComponent(VectorOption<File> opt, String text, SwingFrame parent) {
    this(opt, text, parent, null);
  }
  
  /** Constructor that allows for a tooltip description. */
  public VectorFileOptionComponent(VectorOption<File> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /** Constructor with flag for move buttons. */
  public VectorFileOptionComponent(VectorOption<File> opt, String text, SwingFrame parent,
                                   String description, boolean moveButtonEnabled) {
    super(opt, text, parent, new String[] { }, description, moveButtonEnabled);  // creates all four buttons
    
    // set up JFileChooser
    File workDir = new File(System.getProperty("user.home"));

    _jfc = new JFileChooser(workDir);
    _jfc.setDialogTitle("Select");
    _jfc.setApproveButtonText("Select");
    _jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _jfc.setMultiSelectionEnabled(true);
    _jfc.setFileFilter(ClassPathFilter.ONLY);
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

    File[] c = null;
    int returnValue = _jfc.showDialog(_parent, null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      c = _jfc.getSelectedFiles();
    }
    if (c != null) {
      _table.getSelectionModel().clearSelection();
      for(int i = 0; i < c.length; i++) {
        _addValue(c[i]);
      }
    }
  }
  
  /** @return the file chooser */
  public JFileChooser getFileChooser() {
    return _jfc;
  }
  
  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent ae) {
        chooseFile();
      }
    };
  }
}
