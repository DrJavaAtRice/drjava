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

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.drjava.ui.CommonCloseButton;

import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.MouseInputListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTableUI;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

/** Graphical form of a VectorOption for the Extra Classpath option. Uses a file chooser for each String element.
 *  @version $Id: VectorOptionComponent.java 5668 2012-08-15 04:58:30Z rcartwright $
 */
public abstract class VectorOptionComponent<T> extends OptionComponent<Vector<T>,JComponent> implements OptionConstants {
  protected JScrollPane _tableScrollPane;
  protected JPanel _panel;
  protected JTable _table;
  protected JPanel _buttonPanel;
  protected JButton _moveUpButton;
  protected JButton _moveDownButton;
  protected boolean _moveButtonEnabled = false;
  protected AbstractTableModel _tableModel;
  protected JButton _addButton;
  protected JButton _removeButton;
  protected JTable _buttonTable;
  protected AbstractTableModel _buttonTableModel;
  protected Vector<T> _data;
  protected String[] _columnNames;
  protected String _description;
  protected int _minRows = 0; // display arbitrarily many
  protected int _maxRows = 0; // display arbitrarily many

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
    this(opt, text, parent, colNames, null, false);
  }

  /** Constructor that allows for a tooltip description.
   */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent, String[] colNames,
                               String description) {
    this(opt, text, parent, colNames, description, false);
  }
  
  /** Builds a new VectorOptionComponent.
    * @param opt the option
    * @param text the label to display
    * @param parent the parent frame
    * @param colNames column names or empty array to hide
    * @param moveButtonEnabled true if the move buttons should be enabled
    */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent, String[] colNames,
                               String description, boolean moveButtonEnabled) {
    super(opt, text, parent);
    _columnNames = colNames;
    _moveButtonEnabled = moveButtonEnabled;

    //set up table
    _data = new Vector<T>();
    _tableModel = _makeDecoratedTableModel(_makeTableModel());
    _table = new JTable(_tableModel) {
      {
        // set the column with the "remove" buttons to width 18 (icon is width 16)
        getColumnModel().getColumn(getColumnCount()-1).setMinWidth(18);
        getColumnModel().getColumn(getColumnCount()-1).setMaxWidth(18);
      }
      
      // auto size the columns
      public Component prepareRenderer(final TableCellRenderer renderer,
                                       final int row, final int column) {
        final Component prepareRenderer = super.prepareRenderer(renderer, row, column);
        final TableColumn tableColumn = getColumnModel().getColumn(column);
        
        tableColumn.setPreferredWidth(Math.max(prepareRenderer.getPreferredSize().width,tableColumn.getPreferredWidth()));
        
        return prepareRenderer;
      }    
    };
    // if moving up and down is enabled, allow column drag and drop
    if (_moveButtonEnabled) {
      _table.setUI(new DragDropRowTableUI());
    }
    _table.setColumnSelectionAllowed(false);
    _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _table.getTableHeader().setReorderingAllowed(false);
    
    // create the remove button
    _removeButton = new CommonCloseButton();
    ButtonEditor buttonEditor = new ButtonEditor(_removeButton);
    _removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _removeAction();
      }
    });
    // add remove button as default renderer/editor for buttons
    _table.setDefaultRenderer(JButton.class, new ComponentCellRenderer());
    _table.setDefaultEditor(JButton.class, buttonEditor);

    // create other buttons
    _addButton = new JButton(_getAddAction());    
    _moveUpButton = new JButton(new AbstractAction("Move Up") {
      public void actionPerformed(ActionEvent ae) {
        _moveUpAction();
      }
    });
    _moveDownButton = new JButton(new AbstractAction("Move Down") {
      public void actionPerformed(ActionEvent ae) {
        _moveDownAction();
      }
    });
    
    // list of buttons
    final java.util.List<JButton> buttons = getButtons();
    // table model of buttons, for the table situated under the values table
    // one row, n buttons
    _buttonTableModel = new AbstractTableModel() {
      public String getColumnName(int col) { return ""; }
      public int getRowCount() { return 1; }
      public int getColumnCount() { return buttons.size(); }
      public Object getValueAt(int row, int column) { return buttons.get(column); }
      public Class<?> getColumnClass(int col) { return JButton.class; }
      public boolean isCellEditable(int row, int col) { return true; }
      public void setValueAt(Object value, int row, int col) { fireTableCellUpdated(row, col); }
    };
    
    // button table
    _buttonTable = new JTable(_buttonTableModel);
    _buttonTable.setColumnSelectionAllowed(false);
    _buttonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _buttonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    _buttonTable.getTableHeader().setReorderingAllowed(false);

    // add button editors/renderers to the button table
    for(int i = 0; i < _buttonTable.getColumnCount(); ++i) {      
      buttonEditor = new ButtonEditor(buttons.get(i));
      _buttonTable.getColumnModel().getColumn(i).setCellEditor(buttonEditor);
    }
    _buttonTable.setDefaultRenderer(JButton.class, new ComponentCellRenderer());
    _buttonTable.setTableHeader(null);
    
    // if moving up and down is enabled, add a selection listener that
    // enabled and disables the move up/down buttons if we have enough elements
    // in the table and a row is selected
    if (_moveButtonEnabled) {
      ListSelectionListener selListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          updateButtons();
        }
      };
      _table.getSelectionModel().addListSelectionListener(selListener);
      _table.getColumnModel().getSelectionModel().addListSelectionListener(selListener);    
    }
    
    // add components
    _tableScrollPane = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel tablesPanel = new JPanel(new BorderLayout());
    tablesPanel.add(_tableScrollPane, BorderLayout.CENTER);
    tablesPanel.add(_buttonTable, BorderLayout.SOUTH);

    _panel = new JPanel(new BorderLayout());
    _panel.add(tablesPanel, BorderLayout.CENTER);
    _panel.add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.SOUTH);

    resetToCurrent();

    if (_columnNames.length == 0) {
      _table.setTableHeader(null);
      _tableScrollPane.setColumnHeaderView(null);
    }

    setDescription(description);
    updateButtons();
    resizeTable();
    setComponent(_panel);
  }

  /** Returns the decorated table model. This adds another column to it with remove buttons. */
  protected AbstractTableModel _makeDecoratedTableModel(final AbstractTableModel other) {
    return new AbstractTableModel() {
      public String getColumnName(int col) {
        if (col==other.getColumnCount()) {
          return "";
        }
        else {
          return other.getColumnName(col);
        }
      }
      public int getRowCount() { return other.getRowCount(); }
      public int getColumnCount() { return other.getColumnCount()+1; }
      public Object getValueAt(int row, int column) {
        if (column==other.getColumnCount()) {
          return new CommonCloseButton();
        }
        else {
          return other.getValueAt(row, column);
        }
      }
      public Class<?> getColumnClass(int col) {
        if (col==other.getColumnCount()) {
          return JButton.class;
        }
        else {
          return other.getColumnClass(col);
        }
      }
      public boolean isCellEditable(int row, int col) {
        if (col==other.getColumnCount()) {
          return true;
        }
        else {
          return other.isCellEditable(row, col);
        }
      }
      public void setValueAt(Object value, int row, int col) {
        if (col==other.getColumnCount()) {
          fireTableCellUpdated(row, col);
        }
        else {
          other.setValueAt(value, row, col);
        }
      }
    };
  }

  /** Returns the table model. Can be overridden by subclasses. */
  protected AbstractTableModel _makeTableModel() {
    return new AbstractTableModel() {
      public String getColumnName(int col) { return (_columnNames.length == 0)?super.getColumnName(col):_columnNames[col]; }
      public int getRowCount() { return _data.size(); }
      public int getColumnCount() { return 1; }
      public Object getValueAt(int row, int column) { return _data.get(row); }      
    };
  }
  
  /** Renderer for GUI components. */
  protected static class ComponentCellRenderer implements TableCellRenderer {
    DefaultTableCellRenderer _default = new DefaultTableCellRenderer(); 
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
      if (value instanceof Component) return (Component) value;
      return _default.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
  
  /** Editor for buttons. Clicking causes the button action to be performed. */
  protected static class ButtonEditor extends DefaultCellEditor {
    public ButtonEditor(JButton b) {
      super(new JCheckBox());
      editorComponent = b;
      setClickCountToStart(1);
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
      });
    }
    
    protected void fireEditingStopped() {
      super.fireEditingStopped();
    }
    
    public Object getCellEditorValue() {
      return editorComponent;
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      return editorComponent;
    }
  }
  
  /** Drag and drop UI for tables. */
  public class DragDropRowTableUI extends BasicTableUI {
    private boolean draggingRow = false;
    private int startDragPoint;
    private int dyOffset;
    
    protected MouseInputListener createMouseInputListener() {
      return new DragDropRowMouseInputHandler();
    }
    
    public void paint(Graphics g, JComponent c) {
      super.paint(g, c);
      
      if (draggingRow) {
        g.setColor(_table.getParent().getBackground());
        Rectangle cellRect = _table.getCellRect(_table.getSelectedRow(), 0, false);
        g.copyArea(cellRect.x, cellRect.y, _table.getWidth(), _table.getRowHeight(), cellRect.x, dyOffset);
        
        if (dyOffset < 0) {
          g.fillRect(cellRect.x, cellRect.y + (_table.getRowHeight() + dyOffset), _table.getWidth(), (dyOffset * -1));
        } else {
          g.fillRect(cellRect.x, cellRect.y, _table.getWidth(), dyOffset);
        }
      }
    }
    
    class DragDropRowMouseInputHandler extends MouseInputHandler {
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        startDragPoint = (int)e.getPoint().getY();
      }
      
      public void mouseDragged(MouseEvent e) {
        if (_data.size()<2) return;
        int fromRow = _table.getSelectedRow();
        
        if (fromRow >= 0) {
          draggingRow = true;
          
          int rowHeight = _table.getRowHeight();
          int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
          
          int toRow = -1;
          int yMousePoint = (int)e.getPoint().getY();
          
          if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
            // Move row up
            toRow = fromRow - 1;
          } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
            // Move row down
            toRow = fromRow + 1;
          }
          
          if (toRow >= 0 && toRow < _table.getRowCount()) {
            T fromValue = _data.get(fromRow);
            T toValue = _data.get(toRow);
            _data.set(fromRow, toValue);
            _data.set(toRow, fromValue);
            _tableModel.fireTableRowsUpdated(Math.min(fromRow,toRow),Math.max(fromRow,toRow));
            _table.setRowSelectionInterval(toRow, toRow);
            startDragPoint = yMousePoint;
          }
          
          dyOffset = (startDragPoint - yMousePoint) * -1;
          _table.repaint();
        }
      }
      
      public void mouseReleased(MouseEvent e){
        super.mouseReleased(e);
        
        draggingRow = false;
        table.repaint();
      }
    }
  }
  
  /** Remove selected rows. */
  protected void _removeAction() {
    int[] rows = _table.getSelectedRows();
    if (rows.length > 0) {
      // remove starting from the back so the indices don't have to be adjusted
      for(int i=rows.length-1; i >= 0; --i) {
        if (rows[i] >= _data.size()) continue;
        _removeIndex(rows[i]);
      }
      int last = rows[rows.length-1];
      if (last == _data.size()) { // we removed the last element
        if (last > 0) { // and there's more than one element in the list
          _table.getSelectionModel().setSelectionInterval(last-1,last-1);
        }
      }
      else {
        _table.getSelectionModel().setSelectionInterval(last,last);
      }
      notifyChangeListeners();
    }
  }
  
  /** Move selected rows up by one row. */
  protected void _moveUpAction() {
    int[] rows = _table.getSelectedRows();
    if (rows.length > 0) {
      _table.getSelectionModel().clearSelection();
      for(int i = 0; i < rows.length; ++i) {
        if (rows[i] >= _data.size()) continue;
        if (rows[i] > 0) {
          T el = _data.remove(rows[i]);
          _data.insertElementAt(el, rows[i]-1);
          _table.getSelectionModel().addSelectionInterval(rows[i]-1,rows[i]-1);
          _tableModel.fireTableRowsUpdated(rows[i]-1,rows[i]);
        }
      }
      notifyChangeListeners();
    }
  }
  
  /** Move selected rows down by one row. */
  protected void _moveDownAction() {
    int[] rows = _table.getSelectedRows();
    if (rows.length > 0) {
      _table.getSelectionModel().clearSelection();
      for(int i = 0; i < rows.length; ++i) {
        if (rows[i] >= _data.size()) continue;
        if (rows[i] < _data.size()-1) {
          T el = _data.remove(rows[i]);
          _data.insertElementAt(el, rows[i]+1);
          _table.getSelectionModel().addSelectionInterval(rows[i]+1,rows[i]+1);
          _tableModel.fireTableRowsUpdated(rows[i],rows[i]+1);
        }
      }
      notifyChangeListeners();
    }
  }
  
  /** Add the value to the table, update and resize it. */
  protected void _addValue(T value) {
    _data.add(value);
    _tableModel.fireTableRowsInserted(_data.size()-1, _data.size()-1);
    _table.getSelectionModel().setSelectionInterval(_data.size()-1,_data.size()-1);
    notifyChangeListeners();
    resizeTable();
  }

  /** Remove the value at index i, update the table and resize it. */
  protected void _removeIndex(int i) {
    _data.remove(i);
    _tableModel.fireTableRowsDeleted(i,i);
    resizeTable();
  }

  /** Return the buttons that should be added to the table underneath. */
  protected java.util.List<JButton> getButtons() {
    List<JButton> buttons = new ArrayList<JButton>();
    buttons.add(_addButton);
    buttons.add(_removeButton);
    if (_moveButtonEnabled) {
      buttons.add(_moveUpButton);
      buttons.add(_moveDownButton);
    }
    return buttons;
  }
  
  /** Enable and disable buttons. */
  protected void updateButtons() {
    boolean editable = DrJava.getConfig().isEditable(_option);
    if (_moveButtonEnabled) {
      int[] rows = _table.getSelectedRows();
      boolean enable = (rows.length > 0) && (_data.size()>1) && editable;
      _moveUpButton.setEnabled(enable);
      _moveDownButton.setEnabled(enable);
      _buttonTableModel.setValueAt(null, 0, 1);
      _buttonTableModel.setValueAt(null, 0, 2);
    }
    _addButton.setEnabled(editable);
    _removeButton.setEnabled(editable);
    _table.setEnabled(editable);
  }

  /** Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _description = description;
    _tableScrollPane.setToolTipText(description);
    _table.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    Vector<T> oldValue = DrJava.getConfig().getSetting(_option);
    Vector<T> newValue = getValue();

    if ((oldValue.size() != newValue.size()) || // allow cheap short-circuiting
        (!oldValue.equals(newValue))) { 
      DrJava.getConfig().setSetting(_option, newValue);
      resetToCurrent();
    }
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
    resizeTable();
  }

  /** Displays the given value. */
  public void setValue(ArrayList<T> value) {
    _data = new Vector<T>(value);
    _tableModel.fireTableDataChanged();
    resizeTable();
  }
  
  /** Set the minimum and maximum number of rows to display before using a scrollbar, or 0 for arbitrarily many. */
  public void setRows(int minRows, int maxRows) { _minRows = minRows; _maxRows = maxRows; resizeTable(); }

  /** Return the required height of the table. */
  protected int getTableHeight() {
    int pixelsPerRow = 16;
    int rows = _tableModel.getRowCount();
    if (rows == 0) {
      rows = 1;
    }
    else {
      pixelsPerRow = _table.getPreferredSize().height/rows;
    }
    if (_maxRows > 0) {
        rows = Math.min(rows, _maxRows);
    }
    if (_minRows > 0) {
        rows = Math.max(rows, _minRows);
    }
//    FontMetrics fm = _table.getFontMetrics(_table.getFont());
//    int pixelsPerRow = fm.getHeight() + 1;
    int topBound = _tableScrollPane.getViewportBorderBounds().y;
    return rows * pixelsPerRow + topBound + 2;
  }
  
  /** Resizes the display table */
  public void resizeTable() {
    _tableScrollPane.setPreferredSize(new Dimension(0, getTableHeight()));
    _parent.validate();
  }

  /** Gets an action that adds a component to the set of options. */
  protected abstract Action _getAddAction();
}
