/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.CompletionMonitor;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Properties;

/**
 * Dialog allowing the user to select a variable.
 */
public class InsertVariableDialog extends JFrame implements OptionConstants {
  /**
   * Table with variables.
   */
  private JTable _varTable;
  
  /**
   * Model for the table.
   */
  private DefaultTableModel _varTableModel;
  
  /**
   * Field to preview the value of the variable.
   */
  private JTextField _varValueField;
  
  /**
   * Button to accept the selection.
   */
  private JButton _okBtn;
  
  /**
   * Button to cancel.
   */
  private JButton _cancelBtn;

  /** Main frame. */
  private MainFrame _mainFrame;
  
  /** Properties to use. */
  private Properties _props;
  
  /** Selected entry, or null of cancelled. */
  private edu.rice.cs.plt.tuple.Pair<String,String> _selected = null;
  
  /** Completion monitor to tell the calling dialog that we're done. */
  private CompletionMonitor _cm;
  
  /** Create a dialog.
   *  @param mf the instance of mainframe to query into the project
   */
  public InsertVariableDialog(MainFrame mf, Properties props, CompletionMonitor cm) {
    super("Insert Variable");
    _mainFrame = mf;
    _props = props;
    _cm = cm;
    initComponents();
  }

  /** Build the dialog. */
  private void initComponents() {
    super.getContentPane().setLayout(new GridLayout(1,1));

    Action okAction = new AbstractAction("Select") {
      public void actionPerformed(ActionEvent e) {
        _okCommand();
      }
    };
    _okBtn = new JButton(okAction);
    
    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        _cancelCommand();
      }
    };
    _cancelBtn = new JButton(cancelAction);
    
    JPanel buttons = new JPanel();
    buttons.add(_okBtn);
    buttons.add(_cancelBtn);

    _varValueField = new JTextField();
    _varValueField.setEditable(false);
    
    JPanel bottom = new JPanel(new BorderLayout());    
    bottom.add(_varValueField, BorderLayout.CENTER);    
    bottom.add(buttons, BorderLayout.SOUTH);
    
    JPanel main = new JPanel(new BorderLayout());
    main.add(bottom, BorderLayout.SOUTH);
        
    _varTableModel = new DefaultTableModel(0,1) {
      public String getColumnName(int column) {
        switch(column) {
          case 0: return "Variable";
          default: return super.getColumnName(column);
        }
      }
      
      public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
          case 0: return String.class;
          default: return super.getColumnClass(columnIndex);
        }
      }
      public boolean isCellEditable(int row, int column) { return false; }
    };
    
    _varTable = new JTable(_varTableModel);
    JScrollPane varTableSP = new JScrollPane(_varTable);
    varTableSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    varTableSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//    _varTable.setMinimumSize(new Dimension(300, 200));
//    _varTable.setPreferredSize(new Dimension(300, 200));
    _varTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _varTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    ListSelectionModel lsm = _varTable.getSelectionModel();
    lsm.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        String key = _varTableModel.getValueAt(_varTable.getSelectedRow(),0).toString();
        _selected = new edu.rice.cs.plt.tuple.Pair<String,String>(key, _props.getProperty(key));
        _varValueField.setText(_props.getProperty(key));
      }
    });
    _varTable.setSelectionModel(lsm);
    // do not allow preview to have focus
    _varTable.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        if (e.getOppositeComponent() == _varValueField) {
          _varTable.requestFocus();
        }
      }
    });


    for(Object o: _props.keySet()) {
      String key = o.toString();
      Vector<String> row = new Vector<String>();
      row.add(key);
      _varTableModel.addRow(row);
    }

    _varTable.setRowSelectionInterval(0,0);
    String key = _varTableModel.getValueAt(_varTable.getSelectedRow(),0).toString();
    _varValueField.setText(_props.getProperty(key));
    _selected = new edu.rice.cs.plt.tuple.Pair<String,String>(key, _props.getProperty(key));
    
    main.add(varTableSP, BorderLayout.CENTER);
    
    super.getContentPane().add(main);
    super.setResizable(false);
    pack();

    MainFrame.setPopupLoc(this, _mainFrame);    
  }
  
  protected void _okCommand() {
    setVisible(false);
    _cm.set();
  }
  
  protected void _cancelCommand() {
    _selected = null;
    setVisible(false);
    _cm.set();
  }
  
  public edu.rice.cs.plt.tuple.Pair<String,String> getSelected() { return _selected; }
  
  protected WindowAdapter _windowListener = new WindowAdapter() {
    public void windowDeactivated(WindowEvent we) {
      InsertVariableDialog.this.toFront();
    }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      addWindowListener(_windowListener);
    }
    else {
      removeWindowFocusListener(_windowListener);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
}
