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


package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.*;
//import java.io.Serializable;

/** Displayed when the user chooses to save the interactions history. It will show the current history and allow the
  * user to edit or save it to a file.
  * $Id: HistorySaveDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class HistorySaveDialog extends DrJavaScrollableDialog /* implements Serializable */ {
  
  /** Reference to the history text being edited. */
  private String _history;
  
  /** Creates a new HistorySaveDialog.
    * @param parent Parent frame for this dialog
    */
  public HistorySaveDialog (JFrame parent) {
    super(parent, "Save Interactions History",
          "Make any changes to the history, and then click \"Save\".", "");
  }
  
  /** Creates a custom set of buttons for this panel, including Save and Cancel. */
  protected void _addButtons() {
    // Updates the _history field with the new contents and closes the dialog
    Action saveAction = new AbstractAction("Save") {
      public void actionPerformed (ActionEvent ae) {
        _history = _textArea.getText();
        _dialog.dispose();
      }
    };
    
    // Closes the dialog
    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed (ActionEvent ae) { _dialog.dispose(); }
    };
    
    JButton saveButton = new JButton(saveAction);
    JButton cancelButton = new JButton(cancelAction);
    _buttonPanel.add(saveButton);
    _buttonPanel.add(cancelButton);
    _dialog.getRootPane().setDefaultButton(saveButton);
  }
  
  /** Shows the dialog for editing the given history.
    * @param history History to edit
    * @return Edited history, if it is saved.  Null, if not.
    */
  public String editHistory(String history) {
//    synchronized(_historyLock) {
    _history = null; // make it null by default
    _textArea.setText(history);
    _textArea.setEditable(true);
    
    // Block until the dialog is closed
    show();
    
    // The save action will set the history field
    return _history;
//    }
  }
}
