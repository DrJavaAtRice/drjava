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
import edu.rice.cs.drjava.config.PropertyMaps;
import edu.rice.cs.drjava.config.DrJavaProperty;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.CompletionMonitor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Dialog allowing the user to select a variable.
 */
public class InsertVariableDialog extends JFrame implements OptionConstants {
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
  
  /** Selected entry, or null of cancelled. */
  private edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty> _selected = null;
  
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
        if (_tabbedPane.getSelectedIndex()<0) { return; }
        String category = _tabbedPane.getTitleAt(_tabbedPane.getSelectedIndex());
        Map<String, DrJavaProperty> properties = PropertyMaps.ONLY.getProperties(category);
        String key = _varTableModel.get(category).getValueAt(_varTable.get(category).getSelectedRow(),0).toString();
        DrJavaProperty value = properties.get(key);
        _varValueField.setText(value.toString());
        _helpPane.setText(value.getHelp());
        _selected = new edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty>(key, value);
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
    gridbag.setConstraints(_helpPane, c);
    top.add(_helpPane);
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
    pack();        

    MainFrame.setPopupLoc(InsertVariableDialog.this, _mainFrame);
  }
  
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
        String key = _varTableModel.get(category).getValueAt(_varTable.get(category).getSelectedRow(),0).toString();
        DrJavaProperty value = PropertyMaps.ONLY.getProperty(category,key);
        _selected = new edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty>(key, value);
        _varValueField.setText(value.toString());
        _helpPane.setText(value.getHelp());
      }
    });
    _varTable.get(category).setSelectionModel(lsm);
    
    TreeSet<String> sorted = new TreeSet<String>();
    for(DrJavaProperty p: PropertyMaps.ONLY.getProperties(category).values()) {
      sorted.add(p.getName());
    }
    
    for(String key: sorted) {
      Vector<String> row = new Vector<String>();
      row.add(key);
      _varTableModel.get(category).addRow(row);
    }
    
    _varTable.get(category).setRowSelectionInterval(0,0);
    
    return varTableSP;
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
  
  protected void updatePanes() {
    _tabbedPane.removeAll();
    for (String category: PropertyMaps.ONLY.getCategories()) {
      _tabbedPane.addTab(category, createPane(category, PropertyMaps.ONLY.getProperties(category)));
    }
    _tabbedPane.setSelectedIndex(0);
    String category = _tabbedPane.getTitleAt(_tabbedPane.getSelectedIndex());
    Map<String, DrJavaProperty> properties = PropertyMaps.ONLY.getProperties(category);
    _varTable.get(category).getSelectionModel().setSelectionInterval(0,0);
    String key = _varTableModel.get(category).getValueAt(_varTable.get(category).getSelectedRow(),0).toString();
    DrJavaProperty value = properties.get(key);
    _varValueField.setText(value.toString());
    _helpPane.setText(value.getHelp());
    _selected = new edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty>(key, value);
  }
  
  public edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty> getSelected() { return _selected; }
  
  protected WindowAdapter _windowListener = new WindowAdapter() {
    public void windowDeactivated(WindowEvent we) {
      InsertVariableDialog.this.toFront();
    }
    public void windowClosing(WindowEvent we) {
      _cancelCommand();
    }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      updatePanes();
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
