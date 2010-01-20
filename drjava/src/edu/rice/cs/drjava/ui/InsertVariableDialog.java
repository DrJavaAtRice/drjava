/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (javaplt@rice.edu)
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
import edu.rice.cs.drjava.config.PropertyMaps;
import edu.rice.cs.drjava.config.DrJavaProperty;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Dialog allowing the user to select a variable.
 */
public class InsertVariableDialog extends SwingFrame implements OptionConstants {
  /** Tab pane. */
  JTabbedPane _tabbedPane = new JTabbedPane();
  
  /** Table with variables.
   */
  private Map<String, JTable> _varTable = new HashMap<String, JTable>();
  
  /** Model for the table.
   */
  private Map<String, DefaultTableModel> _varTableModel = new HashMap<String, DefaultTableModel>();
  
  /** Field to preview the value of the variable.
   */
  private JTextField _varValueField;
  
  /** Help/Description for the variable. */
  private JTextPane _helpPane;
  
  /** Button to accept the selection.
   */
  private JButton _okBtn;
  
  /** Button to cancel.
   */
  private JButton _cancelBtn;
  
  /** Main frame. */
  private MainFrame _mainFrame;
  
  /** Selected entry, or null if canceled. */
  private Pair<String,DrJavaProperty> _selected = null;
  
  /** Completion monitor to tell the calling dialog that we're done. */
  private CompletionMonitor _cm;
  
  /** Create a dialog.
    * @param mf the instance of mainframe to query into the project
    */
  public InsertVariableDialog(MainFrame mf, CompletionMonitor cm) {
    super("Insert Variable");
    _mainFrame = mf;
    _cm = cm;
    initComponents();
    initDone();  // call mandated by SwingFrame contract
    pack();        
    Utilities.setPopupLoc(InsertVariableDialog.this, _mainFrame);
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
    
    _helpPane = new JTextPane();
    _helpPane.setToolTipText("Description of the variable.");
    _helpPane.setEditable(false);
    _helpPane.setPreferredSize(new Dimension(500,150));
    _helpPane.setBorder(new javax.swing.border.EmptyBorder(0,10,0,10));
    JScrollPane helpPaneSP = new JScrollPane(_helpPane);
    helpPaneSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      
    _varValueField = new JTextField();
    updatePanes();
    _tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (_tabbedPane.getSelectedIndex() < 0) { return; }
        String category = _tabbedPane.getTitleAt(_tabbedPane.getSelectedIndex());
        Map<String, DrJavaProperty> properties = PropertyMaps.TEMPLATE.getProperties(category);
        int row = _varTable.get(category).getSelectedRow();
        if (row < 0) { return; }
        String key = _varTableModel.get(category).getValueAt(row,0).toString();
        DrJavaProperty value = properties.get(key);
        _varValueField.setText(value.toString());
        _helpPane.setText(value.getHelp());
        _helpPane.setCaretPosition(0);
        _selected = Pair.make(key, value);
      }
    });
    
    JPanel main = new JPanel(new BorderLayout());
    
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(_varValueField, BorderLayout.CENTER);    
    bottom.add(buttons, BorderLayout.SOUTH);
    main.add(bottom, BorderLayout.SOUTH);

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel top = new JPanel(gridbag);
    Insets insets = new Insets(0, 10, 5, 10);
//    JPanel top = new JPanel(new GridLayout(2,1));
    
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 3.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(_tabbedPane, c);
    top.add(_tabbedPane);

    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1.0;
    c.insets = insets;
    gridbag.setConstraints(helpPaneSP, c);
    top.add(helpPaneSP);
    main.add(top, BorderLayout.CENTER);
    
    //The following line enables to use scrolling tabs.
    _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    // do not allow preview to have focus
    _tabbedPane.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        if (e.getOppositeComponent() == _varValueField) {
          _tabbedPane.getSelectedComponent().requestFocus();
        }
      }
    });
    
    super.getContentPane().add(main);
    super.setResizable(false);
  }
  
  /** Create a scroll pane for the specified category with the properties provided in the map.
    * @param category category name
    * @param props map from property names to actual properties in this category */
  protected JScrollPane createPane(final String category, final Map<String, DrJavaProperty> props) {
    _varTableModel.put(category,new DefaultTableModel(0,1) {
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
    });
    
    _varTable.put(category, new JTable(_varTableModel.get(category)));
    JScrollPane varTableSP = new JScrollPane(_varTable.get(category));
    varTableSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    varTableSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    _varTable.get(category).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _varTable.get(category).setDragEnabled(false);
    _varTable.get(category).setPreferredScrollableViewportSize(new Dimension(500,250));
    _varTable.get(category).putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    ListSelectionModel lsm = _varTable.get(category).getSelectionModel();
    lsm.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int row = _varTable.get(category).getSelectedRow();
        if (row < 0) { return; }
        String key = _varTableModel.get(category).getValueAt(row,0).toString();
        DrJavaProperty value = PropertyMaps.TEMPLATE.getProperty(category,key);
        _selected = Pair.make(key, value);
        _varValueField.setText(value.getLazy(PropertyMaps.TEMPLATE));
        _helpPane.setText(value.getHelp());
        _helpPane.setCaretPosition(0);
      }
    });
    _varTable.get(category).setSelectionModel(lsm);
    
    TreeSet<String> sorted = new TreeSet<String>();
    for(DrJavaProperty p: PropertyMaps.TEMPLATE.getProperties(category).values()) {
      sorted.add(p.getName());
    }
    
    for(String key: sorted) {
      Vector<String> row = new Vector<String>();  // Vector is mandated by interface to DefaultTableModel
      row.add(key);
      _varTableModel.get(category).addRow(row);
    }
    
    _varTable.get(category).setRowSelectionInterval(0,0);
    
    return varTableSP;
  }
  
  /** Close the dialog, keeping the last selection in _selected. */
  protected void _okCommand() {
    setVisible(false);
    _cm.signal();
  }
  
  /** Cancel and close the dialog. */
  protected void _cancelCommand() {
    _selected = null;
    setVisible(false);
    _cm.signal();
  }
  
  /** Update the properties in all the panes. */
  protected void updatePanes() {
    Pair<String,DrJavaProperty> sel = getSelected();
    String selCategory = null;
    if (sel != null) {
      selCategory = _tabbedPane.getTitleAt(_tabbedPane.getSelectedIndex());
    }
    _tabbedPane.removeAll();
    for (String category: PropertyMaps.TEMPLATE.getCategories()) {
      _tabbedPane.addTab(category, createPane(category, PropertyMaps.TEMPLATE.getProperties(category)));
    }
    if (sel != null) {
      if (selCategory == null) { sel = null; } else {
        int i;
        for (i = 0; i < _tabbedPane.getTabCount(); ++i) {
          if (_tabbedPane.getTitleAt(i).equals(selCategory)) { _tabbedPane.setSelectedIndex(i); break; }
        }
        if (i == _tabbedPane.getTabCount()) { sel = null; } else {
          DefaultTableModel tm = _varTableModel.get(selCategory);
          for (i = 0; i < tm.getRowCount(); ++i) {
            String key = tm.getValueAt(i,0).toString();
            if (key.equals(sel.second().getName())) {
              _varTable.get(selCategory).getSelectionModel().setSelectionInterval(i,i);
              break;
            }
          }
          if (i==tm.getRowCount()) {
            // not found, select first
            _varTable.get(selCategory).getSelectionModel().setSelectionInterval(0,0);
          }
          _varValueField.setText(sel.second().toString());
          _helpPane.setText(sel.second().getHelp());
          _helpPane.setCaretPosition(0);
          _selected = sel;
        }
      }
    }
    if (sel == null) {
      _tabbedPane.setSelectedIndex(0);
      String category = _tabbedPane.getTitleAt(_tabbedPane.getSelectedIndex());
      Map<String, DrJavaProperty> properties = PropertyMaps.TEMPLATE.getProperties(category);
      _varTable.get(category).getSelectionModel().setSelectionInterval(0,0);
      int row = _varTable.get(category).getSelectedRow();
      if (row >= 0) {
        String key = _varTableModel.get(category).getValueAt(row,0).toString();
        DrJavaProperty value = properties.get(key);
        _varValueField.setText(value.toString());
        _helpPane.setText(value.getHelp());
        _helpPane.setCaretPosition(0);
        _selected = Pair.make(key, value);
      }
    }
  }

  /** Return a pair consisting of the name of the property and the property itself. */
  public Pair<String,DrJavaProperty> getSelected() { return _selected; }
  
  /** Runnable1 that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { _cancelCommand(); }
  };

  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      updatePanes();
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
}
