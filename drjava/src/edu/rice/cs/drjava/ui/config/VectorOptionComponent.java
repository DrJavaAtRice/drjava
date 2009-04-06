/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.ui.config;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;
import java.util.ArrayList;

/** Graphical form of a VectorOption for the Extra Classpath option. Uses a file chooser for each String element.
 *  TODO: define a static make method that adds buttons so that moveUp and moveDown button definitions can be moved
 *  to subclass
 *  @version $Id$
 */
public abstract class VectorOptionComponent<T> extends OptionComponent<Vector<T>> implements OptionConstants {
  protected JScrollPane _tableScrollPane;
  protected JPanel _panel;
  protected JTable _table;
  protected JPanel _buttonPanel;
  protected JButton _addButton;
  protected JButton _removeButton;
  protected JButton _moveUpButton;   /* Only used in VectorFileOptionComponent subclass. */
  protected JButton _moveDownButton; /* Only used in VectorFileOptionComponent subclass. */
  protected AbstractTableModel _tableModel;
  protected static final int NUM_ROWS = 5;
  protected static final int PIXELS_PER_ROW = 18;
  protected Vector<T> _data;
  protected String[] _columnNames;

  /** Builds a new VectorOptionComponent with hidden column name.
    * @param opt the option
    * @param text the label to display
    * @param parent the parent frame
    */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent) {
    this(opt, text, parent, new String[0]);
  }
  
  /** Builds a new VectorOptionComponent.
    * @param opt the option
    * @param text the label to display
    * @param parent the parent frame
    * @param colNames column names or empty array to hide
    */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent, String[] colNames) {
    super(opt, text, parent);
    _columnNames = colNames;

    //set up table
    _data = new Vector<T>();
    _tableModel = _makeTableModel();
    _table = new JTable(_tableModel) {
      {
        final TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        
        for (int i=0;i<getColumnCount(); ++i) {
          int w = renderer.getTableCellRendererComponent(this,getModel().getColumnName(i), false, false, 0, i).getPreferredSize().width;
          getColumnModel().getColumn(i).setPreferredWidth(w);
        }
      }
      
      public Component prepareRenderer(final TableCellRenderer renderer,
                                       final int row, final int column) {
        final Component prepareRenderer = super.prepareRenderer(renderer, row, column);
        final TableColumn tableColumn = getColumnModel().getColumn(column);
        
        tableColumn.setPreferredWidth(Math.max(prepareRenderer.getPreferredSize().width,tableColumn.getPreferredWidth()));
        
        return prepareRenderer;
      }    
    };
    _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    resetToCurrent();

    _addButton = new JButton(_getAddAction());
    _removeButton = new JButton(new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) {
        int[] rows = _table.getSelectedRows();
        if (rows.length>0) {
          // remove starting from the back so the indices don't have to be adjusted
          for(int i=rows.length-1; i>=0; --i) {
            _data.remove(rows[i]);
            _tableModel.fireTableRowsDeleted(rows[i],rows[i]); System.out.flush();
          }
          int last = rows[rows.length-1];
          if (last==_data.size()) { // we removed the last element
            if (last>0) { // and there's more than one element in the list
              _table.getSelectionModel().setSelectionInterval(last-1,last-1);
            }
          }
          else {
            _table.getSelectionModel().setSelectionInterval(last,last);
          }
        }
      }
    });
    
    /* Only used in VectorFileOptionComponent subclass */
    _moveUpButton = new JButton(new AbstractAction("Move Up") {
      public void actionPerformed(ActionEvent ae) {
        int[] rows = _table.getSelectedRows();
        if (rows.length>0) {
          _table.getSelectionModel().clearSelection();
          for(int i=0; i<rows.length; ++i) {
            if (rows[i]>0) {
              T el = _data.remove(rows[i]);
              _data.insertElementAt(el, rows[i]-1);
              _table.getSelectionModel().addSelectionInterval(rows[i]-1,rows[i]-1);
              _tableModel.fireTableRowsUpdated(rows[i]-1,rows[i]);
            }
          }
        }
      }
    });

    /* Only used in VectorFileOptionComponent subclass */
    _moveDownButton = new JButton(new AbstractAction("Move Down") {
      public void actionPerformed(ActionEvent ae) {
        int[] rows = _table.getSelectedRows();
        if (rows.length>0) {
          _table.getSelectionModel().clearSelection();
          for(int i=0; i<rows.length; ++i) {
            if (rows[i]<_data.size()-1) {
              T el = _data.remove(rows[i]);
              _data.insertElementAt(el, rows[i]+1);
              _table.getSelectionModel().addSelectionInterval(rows[i]+1,rows[i]+1);
              _tableModel.fireTableRowsUpdated(rows[i],rows[i]+1);
            }
          }
        }
      }
    });
    
    _buttonPanel = new JPanel();
    _buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
    _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.X_AXIS));
    
    _buttonPanel.add(Box.createHorizontalGlue());
    _addButtons(); // all buttons needs to be added consecutively as a group for glue to work properly               
    _buttonPanel.add(Box.createHorizontalGlue());

    _tableScrollPane = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _panel = new JPanel(new BorderLayout());
    _panel.add(_tableScrollPane, BorderLayout.CENTER);
    _panel.add(_buttonPanel, BorderLayout.SOUTH);

    _tableScrollPane.setPreferredSize(new Dimension(0, NUM_ROWS * PIXELS_PER_ROW));
    if (_columnNames.length==0) {
      _table.setTableHeader(null);
      _tableScrollPane.setColumnHeaderView(null);
    }
  }

  /** Returns the table model. Can be overridden by subclasses. */
  protected AbstractTableModel _makeTableModel() {
    return new AbstractTableModel() {
      public String getColumnName(int col) { return (_columnNames.length==0)?super.getColumnName(col):_columnNames[col]; }
      public int getRowCount() { return _data.size(); }
      public int getColumnCount() { return 1; }
      public Object getValueAt(int row, int column) { return _data.get(row); }
    };
  }
  
  protected void _addValue(T value) {
    _data.add(value);
    _tableModel.fireTableRowsInserted(_data.size()-1, _data.size()-1);
    _table.getSelectionModel().setSelectionInterval(_data.size()-1,_data.size()-1);    
  }

  /** Adds buttons to _buttonPanel */
  protected void _addButtons() {
    _buttonPanel.add(_addButton);
    _buttonPanel.add(_removeButton);
  }
  
  /** Constructor that allows for a tooltip description.
   */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _tableScrollPane.setToolTipText(description);
    _table.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    Vector<T> current = getValue();
    DrJava.getConfig().setSetting(_option, current);
    resetToCurrent();
    return true;
  }
  
  /** Accessor to the current contents of the table.
    * @return The contents of the list in this component in the form of a Vector.
    */
  public Vector<T> getValue() {
    return new Vector<T>(_data);
  }

  /** Displays the given value. */
  public void setValue(Vector<T> value) {
    _data = new Vector<T>(value);
    _tableModel.fireTableDataChanged();
  }

  /** Displays the given value. */
  public void setValue(ArrayList<T> value) {
    _data = new Vector<T>(value);
    _tableModel.fireTableDataChanged();
  }

  /** Return's this OptionComponent's configurable component. */
  public JComponent getComponent() { return _panel; }

  /** Gets an action that adds a component to the set of options. */
  protected abstract Action _getAddAction();
}
