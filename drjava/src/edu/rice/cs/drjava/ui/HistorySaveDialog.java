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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.*;

/**
 * Displayed when the user chooses to save the interactions history. It will show
 * the current history and allow the user to edit or save it to a file.
 * $Id$
 */
public class HistorySaveDialog extends DrJavaScrollableDialog {

  /** Reference to the history text being edited. */
  private String _history;

  /**
   * Lock to ensure this history is only edited by one user at a time.
   * TODO: Is this necessary?
   */
  private Object _historyLock = new Object();

  /**
   * Creates a new HistorySaveDialog.
   * @param parent Parent frame for this dialog
   */
  public HistorySaveDialog (JFrame parent) {
    super(parent, "Save Interactions History",
          "Make any changes to the history, and then click \"Save\".", "");
  }

  /**
   * Creates a custom set of buttons for this panel, including
   * Save and Cancel.
   */
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
      public void actionPerformed (ActionEvent ae) {
        _dialog.dispose();
      }
    };

    JButton saveButton = new JButton(saveAction);
    JButton cancelButton = new JButton(cancelAction);
    _buttonPanel.add(saveButton);
    _buttonPanel.add(cancelButton);
    _dialog.getRootPane().setDefaultButton(saveButton);
  }

  /**
   * Shows the dialog for editing the given history.
   * @param history History to edit
   * @return Edited history, if it is saved.  Null, if not.
   */
  public String editHistory(String history) {
    synchronized(_historyLock) {
      _history = null; // make it null by default
      _textArea.setText(history);
      _textArea.setEditable(true);

      // Block until the dialog is closed
      show();

      // The save action will set the history field
      return _history;
    }
  }
}
