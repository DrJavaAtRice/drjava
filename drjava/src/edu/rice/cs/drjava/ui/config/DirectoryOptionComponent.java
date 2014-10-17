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

import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.SwingFrame;

/** Graphical form of a FileOption.
 *
 *  @version $Id: DirectoryOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DirectoryOptionComponent extends OptionComponent<File,DirectorySelectorComponent> implements OptionConstants {

  private DirectorySelectorComponent _component;

  public DirectoryOptionComponent (FileOption opt, String text, SwingFrame parent, DirectoryChooser dc) {
    super(opt, text, parent);
    
    _component = new DirectorySelectorComponent(parent, dc, 30, 10f);
    _component.setFileField(DrJava.getConfig().getSetting(_option));
    _component.getFileField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void removeUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void changedUpdate(DocumentEvent e) { notifyChangeListeners(); }
    });
    
    setComponent(_component);
  }

  /** Constructor that allows for a tooltip description. */
  public DirectoryOptionComponent (FileOption opt, String text, SwingFrame parent, String desc, DirectoryChooser dc) {
    this(opt, text, parent, dc);
    setDescription(desc);
  }

  /** Sets the tooltip description text for this option.
   *  @param description the tooltip text
   */
  public void setDescription(String description) { _label.setToolTipText(description); }

  /** Updates the config object with the new setting.
   *  @return true if the new value is set successfully
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
  public void setValue(File value) { _component.setFileField(value); }

//  /** Return's this OptionComponent's configurable component. */
//  public DirectorySelectorComponent getComponent() { return _component; }

  /** Adds a filter to decide if a directory can be chosen. */
  public void addChoosableFileFilter(FileFilter filter) { _component.addChoosableFileFilter(filter); }
}
