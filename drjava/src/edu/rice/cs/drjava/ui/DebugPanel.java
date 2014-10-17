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

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.ui.avail.*;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.RightClickMouseAdapter;

/**
 * Panel for displaying the debugger input and output in MainFrame.  This
 * class is a swing view class and hence should only be accessed from the 
 * event-handling thread.
 * @version $Id: DebugPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DebugPanel extends JPanel implements OptionConstants {

  private JSplitPane _tabsPane;
  private JTabbedPane _leftPane;
  private JTabbedPane _rightPane;
  private JPanel _tabsAndStatusPane;

  private JTable _watchTable;
  private JTable _stackTable;
  private JTable _threadTable;
  private long _currentThreadID;

  // private JPopupMenu _threadRunningPopupMenu;
  private JPopupMenu _threadSuspendedPopupMenu;
  private JPopupMenu _stackPopupMenu;
  private JPopupMenu _watchPopupMenu;
  private DebugThreadData _threadInPopup;

  private final SingleDisplayModel _model;
  private final MainFrame _frame;
  private final Debugger _debugger;

  private JPanel _buttonPanel;
  private JButton _closeButton;
  private JButton _resumeButton;
  private JButton _automaticTraceButton;
  private JButton _stepIntoButton;
  private JButton _stepOverButton;
  private JButton _stepOutButton;
  private JLabel _statusBar;

  private ArrayList<DebugWatchData> _watches;
  private ArrayList<DebugThreadData> _threads;
  private ArrayList<DebugStackData> _stackFrames;
  
  /* The following field is commented out because it was never written (and hence always null). */
//  private DefaultTreeCellRenderer dtcr;

  /** Constructs a new panel to display debugging information when the Debugger is active.  This is swing view class and hence should only
   *  be accessed from the event-handling thread.
   */
  public DebugPanel(MainFrame frame) {

    this.setLayout(new BorderLayout());

    _frame = frame;
    _model = frame.getModel();
    _debugger = _model.getDebugger();

    _watches = new ArrayList<DebugWatchData>();
    _threads = new ArrayList<DebugThreadData>();
    _stackFrames = new ArrayList<DebugStackData>();
    _leftPane = new JTabbedPane();
    _rightPane = new JTabbedPane();

    _setupTabPanes();

    _tabsPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, _leftPane, _rightPane);
    _tabsPane.setOneTouchExpandable(true);
    _tabsPane.setDividerLocation((int)(_frame.getWidth()/2.5));

    _tabsAndStatusPane = new JPanel(new BorderLayout());
    _tabsAndStatusPane.add(_tabsPane, BorderLayout.CENTER);

    _statusBar = new JLabel("");
    _statusBar.setForeground(Color.blue.darker());

    _tabsAndStatusPane.add(_statusBar, BorderLayout.SOUTH);

    this.add(_tabsAndStatusPane, BorderLayout.CENTER);

    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);

    _debugger.addListener(new DebugPanelListener());

    // Setup the color listeners.
    _setColors(_watchTable);
    _setColors(_stackTable);
    _setColors(_threadTable);
  }

  /** Quick helper for setting up color listeners. */
  private static void _setColors(Component c) {
    new ForegroundColorListener(c);
    new BackgroundColorListener(c);
  }

  /** Causes all display tables to update their information from the debug manager. */
  public void updateData() {
    assert EventQueue.isDispatchThread();
    if (_debugger.isReady()) {
      try {
        _watches = _debugger.getWatches();
        
        if (_debugger.isCurrentThreadSuspended())  _stackFrames = _debugger.getCurrentStackFrameData();
        else  _stackFrames = new ArrayList<DebugStackData>();
        
        _threads = _debugger.getCurrentThreadData();
      }
      catch (DebugException de) {
        // Thrown if
        MainFrameStatics.showDebugError(_frame, de);
      }
    }
    else {
      // Clean up if debugger dies
      _watches = new ArrayList<DebugWatchData>();
      _threads = new ArrayList<DebugThreadData>();
      _stackFrames = new ArrayList<DebugStackData>();
    }

    ((AbstractTableModel)_watchTable.getModel()).fireTableDataChanged();
    ((AbstractTableModel)_stackTable.getModel()).fireTableDataChanged();
    ((AbstractTableModel)_threadTable.getModel()).fireTableDataChanged();
  }


  /** Creates the tabbed panes in the debug panel. */
  private void _setupTabPanes() {

    // Watches table
    _initWatchTable();

    // Stack table
    _stackTable = new JTable( new StackTableModel());
    _stackTable.addMouseListener(new StackMouseAdapter());

    _rightPane.addTab("Stack", new JScrollPane(_stackTable));

    // Thread table
    _initThreadTable();

    // Sets the method column to always be 7 times as wide as the line column
    TableColumn methodColumn;
    TableColumn lineColumn;
    methodColumn = _stackTable.getColumnModel().getColumn(0);
    lineColumn = _stackTable.getColumnModel().getColumn(1);
    methodColumn.setPreferredWidth(7*lineColumn.getPreferredWidth());

    _initPopup();
  }

  private void _initWatchTable() {
    _watchTable = new JTable( new WatchTableModel());
    _watchTable.setDefaultEditor(_watchTable.getColumnClass(0), new WatchEditor());
    _watchTable.setDefaultRenderer(_watchTable.getColumnClass(0), new WatchRenderer());

    _leftPane.addTab("Watches", new JScrollPane(_watchTable));
  }

  private void _initThreadTable() {
    _threadTable = new JTable(new ThreadTableModel());
    _threadTable.addMouseListener(new ThreadMouseAdapter());
    _rightPane.addTab("Threads", new JScrollPane(_threadTable));

    // Sets the name column to always be 2 times as wide as the status column
    TableColumn nameColumn;
    TableColumn statusColumn;
    nameColumn = _threadTable.getColumnModel().getColumn(0);
    statusColumn = _threadTable.getColumnModel().getColumn(1);
    nameColumn.setPreferredWidth(2*statusColumn.getPreferredWidth());

    // Adds a cell renderer to the threads table
    _currentThreadID = 0;
    TableCellRenderer threadTableRenderer = new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        Component renderer =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        _setThreadCellFont(row);

        return renderer;
      }

      /**
       * Sets the font for a cell in the thread table.
       * @param row the current row
       */
      private void _setThreadCellFont(int row) {
        DebugThreadData currThread = _threads.get(row);
        if (currThread.getUniqueID() == _currentThreadID &&
            currThread.isSuspended()) {
          setFont(getFont().deriveFont(Font.BOLD));
        }
      }
    };
    _threadTable.getColumnModel().getColumn(0).setCellRenderer(threadTableRenderer);
    _threadTable.getColumnModel().getColumn(1).setCellRenderer(threadTableRenderer);
  }

  /** Adds config color support to DefaultCellEditor. */
  private static class WatchEditor extends DefaultCellEditor {

    WatchEditor() { super(new JTextField()); }

    /**
     * Overrides the default editor component to use proper coloring.
     */
    public Component getTableCellEditorComponent
        (JTable table, Object value, boolean isSelected, int row, int column) {
      Component editor = super.getTableCellEditorComponent
        (table, value, isSelected, row, column);
      _setColors(editor);
      return editor;
    }
  }

  /** Adds config color support to DefaultTableCellRenderer. */
  private class WatchRenderer extends DefaultTableCellRenderer {

    /**
     * Overrides the default rederer component to use proper coloring.
     */
    public Component getTableCellRendererComponent
        (JTable table, Object value, boolean isSelected, boolean hasFocus,
         int row, int column) {
      Component renderer = super.getTableCellRendererComponent
        (table, value, isSelected, hasFocus, row, column);
      _setColors(renderer);
      _setWatchCellFont(row);
      return renderer;
    }

    /**
     * Sets the font for a cell in the watch table.
     * @param row the current row
     */
    private void _setWatchCellFont(int row) {
      int numWatches = _watches.size();
      if (row < numWatches) {
        DebugWatchData currWatch = _watches.get(row);
        if (currWatch.isChanged()) {
          setFont(getFont().deriveFont(Font.BOLD));
        }
      }
    }
  }

  /** A table for displaying the watched variables and fields. Where is the synchronization for this class? */
  public class WatchTableModel extends AbstractTableModel {

    private String[] _columnNames = {"Name", "Value", "Type"};

    public String getColumnName(int col) { return _columnNames[col]; }
    public int getRowCount() { return _watches.size() + 1; }
    public int getColumnCount() { return _columnNames.length; }
    public Object getValueAt(int row, int col) {
      if (row < _watches.size()) {
        DebugWatchData watch = _watches.get(row);
        switch(col) {
          case 0: return watch.getName();
          case 1: return watch.getValue();
          case 2: return watch.getType();
        }
        fireTableRowsUpdated(row, _watches.size()-1);
        return null;
      }
      else {
        fireTableRowsUpdated(row, _watches.size()-1);
        // Last row blank
        return "";
      }
    }
    public boolean isCellEditable(int row, int col) {
      // First col for entering new values
      if (col == 0) return true;
      return false;
    }
    public void setValueAt(Object value, int row, int col) {
      try {
        if ((value == null) || (value.equals(""))) {
          // Remove value
          _debugger.removeWatch(row);
        }
        else {
          if (row < _watches.size())
            _debugger.removeWatch(row);
          // Add value
          _debugger.addWatch(String.valueOf(value));
        }
        //fireTableCellUpdated(row, col);
        fireTableRowsUpdated(row, _watches.size()-1);
      }
      catch (DebugException de) { MainFrameStatics.showDebugError(_frame, de); }
    }
  }

  /** A table for displaying the current stack trace. */
  public class StackTableModel extends AbstractTableModel {

    private String[] _columnNames = {"Method", "Line"};  // Do we need #?

    public String getColumnName(int col) { return _columnNames[col]; }
    
    public int getRowCount() {
      if (_stackFrames == null)  return 0;
      return _stackFrames.size();
    }
    public int getColumnCount() { return _columnNames.length; }

    public Object getValueAt(int row, int col) {
      DebugStackData frame = _stackFrames.get(row);
      switch(col) {
        case 0: return frame.getMethod();
        case 1: return Integer.valueOf(frame.getLine());
      }
      return null;
    }
    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }

  /** A table for displaying all current threads.  Where is the synchronization for this class? */
  public class ThreadTableModel extends AbstractTableModel {

    private String[] _columnNames = {"Name", "Status"};

    public String getColumnName(int col) { return _columnNames[col]; }

    public int getRowCount() {
      if (_threads == null) return 0;
      return _threads.size();
    }

    public int getColumnCount() {
      return _columnNames.length;
    }

    public Object getValueAt(int row, int col) {
      DebugThreadData threadData  = _threads.get(row);
      switch(col) {
        case 0: return threadData.getName();
        case 1: return threadData.getStatus();
        default: return null;
      }

    }

    public boolean isCellEditable(int row, int col) { return false; }
  }

  /** Creates the buttons for controlling the debugger. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    Action resumeAction = new AbstractAction("Resume") {
      public void actionPerformed(ActionEvent ae) {
        try { _frame.debuggerResume(); }
        catch (DebugException de) { MainFrameStatics.showDebugError(_frame, de); }
      }
    };
    _resumeButton = new JButton(resumeAction);
    _frame._addGUIAvailabilityListener(_resumeButton,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER_SUSPENDED);
    
    Action automaticTrace = new AbstractAction("Automatic Trace") {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerAutomaticTrace();
      }
    };
    _automaticTraceButton = new JButton(automaticTrace);
    _frame._addGUIAvailabilityListener(_automaticTraceButton,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER_SUSPENDED);
    
    Action stepIntoAction = new AbstractAction("Step Into") {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(Debugger.StepType.STEP_INTO);
      }
    };
    _stepIntoButton = new JButton(stepIntoAction);
    _frame._addGUIAvailabilityListener(_stepIntoButton,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER_SUSPENDED);

    Action stepOverAction = new AbstractAction("Step Over") {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(Debugger.StepType.STEP_OVER);
      }
    };
    _stepOverButton = new JButton(stepOverAction);
    _frame._addGUIAvailabilityListener(_stepOverButton,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER_SUSPENDED);

    Action stepOutAction = new AbstractAction( "Step Out" ) {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(Debugger.StepType.STEP_OUT);
      }
    };
    _stepOutButton = new JButton(stepOutAction);
    _frame._addGUIAvailabilityListener(_stepOutButton,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER,
                                      GUIAvailabilityListener.ComponentType.DEBUGGER_SUSPENDED);
    
    ActionListener closeListener =
      new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerToggle();
      }
    };

    _closeButton = new CommonCloseButton(closeListener);

    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);
    mainButtons.add(_resumeButton);
    mainButtons.add(_automaticTraceButton);
    mainButtons.add(_stepIntoButton);
    mainButtons.add(_stepOverButton);
    mainButtons.add(_stepOutButton);
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    
    gbLayout.setConstraints(_resumeButton, c);
    gbLayout.setConstraints(_automaticTraceButton, c);
    gbLayout.setConstraints(_stepIntoButton, c);
    gbLayout.setConstraints(_stepOverButton, c);
    gbLayout.setConstraints(_stepOutButton, c);
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    updateButtons();
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }

  /** Initializes the pop-up menu that is revealed when the user
   * right-clicks on a row in the thread table or stack table.
   */
  private void _initPopup() {
    // this is commented out because we do not currently support manual
    // suspension of a running thread.
//     _threadRunningPopupMenu = new JPopupMenu("Thread Selection");
//     JMenuItem threadRunningSuspend = new JMenuItem();
//     Action suspendAction = new AbstractAction("Suspend Thread") {
//       public void actionPerformed(ActionEvent e) {
//         try{
//           _debugger.suspend(getSelectedThread());
//         }
//         catch(DebugException exception) {
//           JOptionPane.showMessageDialog(_frame, "Cannot suspend the thread.", "Debugger Error", JOptionPane.ERROR_MESSAGE);
//         }
//       }
//     };
//     threadRunningSuspend.setAction(suspendAction);
//     _threadRunningPopupMenu.add(threadRunningSuspend);
//     threadRunningSuspend.setText("Suspend and Select Thread");

    Action selectAction = new AbstractAction("Select Thread") {
      public void actionPerformed(ActionEvent e) { _selectCurrentThread(); }
    };

    _threadSuspendedPopupMenu = new JPopupMenu("Thread Selection");
    _threadSuspendedPopupMenu.add(selectAction);
    _threadSuspendedPopupMenu.add(new AbstractAction("Resume Thread") {
      public void actionPerformed(ActionEvent e) {
        try {
          if (_threadInPopup.isSuspended()) _debugger.resume(_threadInPopup);
        }
        catch (DebugException dbe) { MainFrameStatics.showDebugError(_frame, dbe); }
      }
    });

    _stackPopupMenu = new JPopupMenu("Stack Selection");
    _stackPopupMenu.add(new AbstractAction("Scroll to Source") {
      public void actionPerformed(ActionEvent e) {
        try {
          _debugger.scrollToSource(getSelectedStackItem());
        }
        catch (DebugException de) { MainFrameStatics.showDebugError(_frame, de); }
      }
    });

    _watchPopupMenu = new JPopupMenu("Watches");
    _watchPopupMenu.add(new AbstractAction("Remove Watch") {
      public void actionPerformed(ActionEvent e) {
        try {
          _debugger.removeWatch(_watchTable.getSelectedRow());
          _watchTable.revalidate();
          _watchTable.repaint();
        }
        catch (DebugException de) { MainFrameStatics.showDebugError(_frame, de); }
      }
    });
    _watchTable.addMouseListener(new DebugTableMouseAdapter(_watchTable) {
      protected void _showPopup(MouseEvent e) {
        if (_watchTable.getSelectedRow() < _watchTable.getRowCount() - 1) {
          _watchPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      }
      protected void _action() {
      }
    });
  }

  /**
   */
  private void _selectCurrentThread() {
    if (_threadInPopup.isSuspended()) {
      try { _debugger.setCurrentThread(_threadInPopup); }
      catch(DebugException de) { MainFrameStatics.showDebugError(_frame, de); }
    }
  }

  /** gets the thread that is currently selected in the thread table
   * @return the highlighted thread
   */
  public DebugThreadData getSelectedThread() {
    int row = _threadTable.getSelectedRow();
    if (row == -1) {
      row = 0;  // if there is no selected index, just return the first element
    }
    return _threads.get(row);
  }

  /** Gets the DebugStackData that is currently selected in the stack table
   *  @return the highlighted stack element
   */
  public DebugStackData getSelectedStackItem() {
    return _stackFrames.get(_stackTable.getSelectedRow());
  }

  /** @return the selected watch */
  public DebugWatchData getSelectedWatch() {
    return _watches.get(_watchTable.getSelectedRow());
  }

  /** Listens to events from the debug manager to keep the panel updated. */
  class DebugPanelListener implements DebugListener {
    /** Called when the current thread is suspended. */
    public void currThreadSuspended() {
      // Only change GUI from event-dispatching thread
      Utilities.invokeLater(new Runnable() { public void run() { updateData(); } });
    }

    /** Called when the current thread is resumed */
    public void currThreadResumed() {
      // Only change GUI from event-dispatching thread
      Utilities.invokeLater(new Runnable() { public void run() { updateData(); } });
    }

    /** Called when a thread starts.  Only runs in event thread. */
    public void threadStarted() { updateData(); }

    /** Called when the current thread dies. Only runs in event thread. */
    public void currThreadDied() { updateData(); }

    /** Called when any thread other than the current thread dies. Only runs in event thread. */
    public void nonCurrThreadDied() { updateData(); }

    /** Called when the current (selected) thread is set in the debugger.
     *  @param thread the thread that was set as current
     */
    public void currThreadSet(DebugThreadData thread) {
      _currentThreadID = thread.getUniqueID();

      // Only change GUI from event-dispatching thread
      Utilities.invokeLater(new Runnable() { public void run() { updateData(); } });
    }
    
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) { }
    public void debuggerStarted() { }
    public void debuggerShutdown() { }
    public void breakpointReached(final Breakpoint bp) { }
    public void watchSet(final DebugWatchData w) { }
    public void watchRemoved(final DebugWatchData w) { }
    public void stepRequested() { }
    public void regionAdded(Breakpoint r) { }
    public void regionChanged(Breakpoint r) { }
    public void regionRemoved(Breakpoint r) { }
  }

  public void updateButtons() {
//    setAutomaticTraceButtonText();
  }

  /**Sets the AutomaticTraceButton text as well as the Automatic Trace check box menu item under Debugger based on whether automatic trace is enabled or not*/
  public void setAutomaticTraceButtonText() {
    if(_model.getDebugger().isAutomaticTraceEnabled()) {
      _automaticTraceButton.setText("Disable Trace"); 
    }
    else { 
      _automaticTraceButton.setText("Automatic Trace");
    }
    _frame.setAutomaticTraceMenuItemStatus();
  }
  
  public void setStatusText(String text) { _statusBar.setText(text); }

  public String getStatusText() { return _statusBar.getText(); }

  /** Updates the UI to a new look and feel.
   * Need to update the contained popup menus as well.
   *
   * Currently, we don't support changing the look and feel
   * on the fly, so this is disabled.
   *
  public void updateUI() {
    super.updateUI();
    if (_threadSuspendedPopupMenu != null) {
      SwingUtilities.updateComponentTreeUI(_threadSuspendedPopupMenu);
    }
    if (_stackPopupMenu != null) {
      SwingUtilities.updateComponentTreeUI(_stackPopupMenu);
    }
    if (_watchPopupMenu != null) {
      SwingUtilities.updateComponentTreeUI(_watchPopupMenu);
    }
  }*/

  /** Concrete DebugTableMouseAdapter for the thread table.
   */
  private class ThreadMouseAdapter extends DebugTableMouseAdapter {
    public ThreadMouseAdapter() {
      super(_threadTable);
    }

    protected void _showPopup(MouseEvent e) {
      _threadInPopup = _threads.get(_lastRow);
      if (_threadInPopup.isSuspended()) {
         _threadSuspendedPopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
//       else {
//         _threadRunningPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//       }
    }

    protected void _action() {
      _threadInPopup = _threads.get(_lastRow);
      _selectCurrentThread();
    }
  }

  /** Concrete DebugTableMouseAdapter for the stack table.
   */
  private class StackMouseAdapter extends DebugTableMouseAdapter {
    public StackMouseAdapter() {
      super(_stackTable);
    }

    protected void _showPopup(MouseEvent e) {
      _stackPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    protected void _action() {
      try {
        _debugger.scrollToSource(_stackFrames.get(_lastRow));
      }
      catch (DebugException de) {
        MainFrameStatics.showDebugError(_frame, de);
      }
    }
  }

  /** A mouse adapter that allows for double-clicking and
   * bringing up a right-click menu.
   */
  private abstract class DebugTableMouseAdapter extends RightClickMouseAdapter {
    protected JTable _table;
    protected int _lastRow;

    public DebugTableMouseAdapter(JTable table) {
      _table = table;
      _lastRow = -1;
    }

    protected abstract void _showPopup(MouseEvent e);
    protected abstract void _action();

    protected void _popupAction(MouseEvent e) {
      _lastRow = _table.rowAtPoint(e.getPoint());
      _table.setRowSelectionInterval(_lastRow, _lastRow);
      _showPopup(e);
    }

    public void mousePressed(MouseEvent e) {
      super.mousePressed(e);

      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        _lastRow = _table.rowAtPoint(e.getPoint());
        _action();
      }
    }
  }
  

}
