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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.config.OptionConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Displayed when the user chooses to save the interactions history. It will show
 * the current history and allow the user to edit or save it to a file.
 * $Id$
 */
public class HistorySaveDialog extends ScrollableDialog {

  /**
   * Reference to the history text being edited.
   */
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
