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

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Displayed when the user chooses to save the interactions history. It will show
 * the current history and allow the user to edit or save it to a file.
 * $Id$
 */
public class HistorySaveDialog extends JDialog {
  private MainFrame _frame;
  private FileSaveSelector _selector;
  private String _history;
  
  private JTextArea _textArea;
  private JButton _saveButton;
  private JButton _cancelButton;
  private JScrollPane _textScroll;
  private JPanel _buttonPanel;
  
  public HistorySaveDialog (MainFrame frame, FileSaveSelector selector, String history) {
    _frame = frame;
    _selector = selector;
    _history = history;
    
    Container content = this.getContentPane();
    
    content.setLayout(new BorderLayout());
    
    
    _textArea = new JTextArea();
    _textArea.append(_history);
    _saveButton = new JButton("Save");
    _saveButton.addActionListener(_saveAction);
    _cancelButton = new JButton("Cancel");
    _cancelButton.addActionListener(_cancelAction);
    
    this.setTitle("Save Interactions History");
    this.setSize(400,300);
    this.setLocation(300,300);
    
    _textScroll = new JScrollPane(_textArea,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    _buttonPanel = new JPanel();
    _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.X_AXIS));
    _buttonPanel.add(Box.createGlue());
    _buttonPanel.add(_saveButton);
    _buttonPanel.add(_cancelButton);
    
    content.add(_textScroll, BorderLayout.CENTER);
    content.add(_buttonPanel, BorderLayout.SOUTH);
    
    this.show();    
  }
  
  private Action _saveAction = new AbstractAction() {
    public void actionPerformed (ActionEvent ae) {      
      File c = null;
      try {
        c = _selector.getFile();
      }
      catch (OperationCanceledException oce) {
        return;
        // don't need to do anything
      }
      if (c != null) {
        if (c.getName().indexOf('.') == -1)
          c = new File(c.getAbsolutePath() + "." + InteractionsHistoryFilter.HIST_EXTENSION);
        try {
          FileOutputStream fos = new FileOutputStream(c);
          OutputStreamWriter osw = new OutputStreamWriter(fos);
          BufferedWriter bw = new BufferedWriter(osw);
          String newHistory = _textArea.getText();
          bw.write(newHistory, 0, newHistory.length());
          bw.close();
        }
        catch (IOException ioe) {
          _frame._showIOError(new IOException("An error occured writing the history to a file"));
        }
      }
      HistorySaveDialog.this.dispose();
    }
  };
  
  private Action _cancelAction = new AbstractAction() {
    public void actionPerformed (ActionEvent ae) {
      HistorySaveDialog.this.dispose();
    }
  };
}