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

import gj.util.Vector;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.*;

/** 
 * Panel for displaying the debugger input and output in MainFrame.
 * @version $Id$
 */
public class DebugPanel extends JPanel implements OptionConstants {
  
  private JSplitPane _tabsPane;
  private JTabbedPane _leftPane;
  private JTabbedPane _rightPane;
  
  private JTable _watchTable;
  private DefaultMutableTreeNode _breakpointRootNode;
  private DefaultTreeModel _bpTreeModel;
  private JTree _bpTree;
  private JTable _stackTable;
  private JTable _threadTable;
  
  private final SingleDisplayModel _model;
  private final MainFrame _frame;
  private final DebugManager _debugger;
  
  private JPanel _buttonPanel;
  private JButton _closeButton; 
  private JButton _resumeButton;
  private JButton _stepIntoButton;
  private JButton _stepOverButton;
  private JButton _stepOutButton;
  private JLabel _statusBar;
  
  private Vector<DebugManager.WatchData> _watches;
  private Vector<DebugManager.ThreadData> _threads;
  private Vector<DebugManager.StackData> _stackFrames;
  
  /**
   * Constructs a new panel to display debugging information when the
   * DebugManager is active.
   */
  public DebugPanel( MainFrame frame ) {
    
    this.setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    _debugger = _model.getDebugManager();
    
    _watches = _debugger.getWatches();
    _threads = _debugger.getCurrentThreadData();
    _stackFrames = _debugger.getCurrentStackFrameData();
    _leftPane = new JTabbedPane();
    _rightPane = new JTabbedPane();
    
    _setupTabPanes();
    
    _tabsPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               true,
                               _leftPane,
                               _rightPane);
    _tabsPane.setOneTouchExpandable(true);
    _tabsPane.setDividerLocation((int)(_frame.getWidth()/2.5));
    
    this.add(_tabsPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    
    _statusBar = new JLabel("");
    /*_statusBar.setBorder(
      new CompoundBorder(new EmptyBorder(2,2,2,2),
                         new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                                            new EmptyBorder(2,2,2,2))));
                                            */
    _statusBar.setForeground(Color.blue.darker());
    this.add(_statusBar, BorderLayout.SOUTH);
    
    _debugger.addListener(new DebugPanelListener());
  }
  
  /**
   * Causes all display tables to update their information from the debug manager.
   * @param updateThreads Whether to check for current stack trace and threads
   */
  public void updateData(boolean updateStack) {
     if (_debugger.isReady()) {
      _watches = _debugger.getWatches();
      if (updateStack) {
        // Get threads and stack (thread suspended)
        _threads = _debugger.getCurrentThreadData();
        _stackFrames = _debugger.getCurrentStackFrameData();
      }
      else {
        // Empty tables (thread resumed)
        _threads = new Vector<DebugManager.ThreadData>();
        _stackFrames = new Vector<DebugManager.StackData>();
      }
    }
    else {
      // Clean up if debugger dies
      _watches = new Vector<DebugManager.WatchData>();
      _threads = new Vector<DebugManager.ThreadData>();
      _stackFrames = new Vector<DebugManager.StackData>();
      // also clear breakpoint tree?
    }

    ((AbstractTableModel)_watchTable.getModel()).fireTableDataChanged();
    ((AbstractTableModel)_stackTable.getModel()).fireTableDataChanged();
    if (_threadTable != null) {
      ((AbstractTableModel)_threadTable.getModel()).fireTableDataChanged();
    }

  }
  
  
  /**
   * Creates the tabbed panes in the debug panel.
   */
  public void _setupTabPanes() {
    
    // Watches table
    _watchTable = new JTable( new WatchTableModel());
    _leftPane.addTab("Watches", new JScrollPane(_watchTable));
    
    // Breakpoint tree
    _breakpointRootNode = new DefaultMutableTreeNode("Breakpoints");
    _bpTreeModel = new DefaultTreeModel(_breakpointRootNode);
    _bpTree = new JTree(_bpTreeModel);
    _bpTree.setEditable(false);
    _bpTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _bpTree.setShowsRootHandles(true);
    _bpTree.setRootVisible(false);
    _bpTree.putClientProperty("JTree.lineStyle", "Angled");
    _bpTree.setScrollsOnExpand(true);
    // Breakpoint tree cell renderer
    DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
    dtcr.setLeafIcon(null);
    dtcr.setOpenIcon(null);
    dtcr.setClosedIcon(null);
    _bpTree.setCellRenderer(dtcr);
    
    _leftPane.addTab("Breakpoints", new JScrollPane(_bpTree));
    
    // Stack table
    _stackTable = new JTable( new StackTableModel());

    _rightPane.addTab("Stack", new JScrollPane(_stackTable));
    
    // Thread table
    if (DrJava.CONFIG.getSetting(DEBUG_SHOW_THREADS).booleanValue()) {
      _threadTable = new JTable( new ThreadTableModel());
      _rightPane.addTab("Threads", new JScrollPane(_threadTable)); 
      // Sets the name column to always be 2 times as wide as the status column
      TableColumn nameColumn = null;
      TableColumn statusColumn = null;
      nameColumn = _threadTable.getColumnModel().getColumn(0);
      statusColumn = _threadTable.getColumnModel().getColumn(1);
      nameColumn.setPreferredWidth(2*statusColumn.getPreferredWidth()); 
    }
    DrJava.CONFIG.addOptionListener( OptionConstants.DEBUG_SHOW_THREADS, 
                                    new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        if (oce.value.booleanValue()) {
          if (_threadTable == null) {
            _threadTable = new JTable( new ThreadTableModel());
            _rightPane.addTab("Threads", new JScrollPane(_threadTable));
            // Sets the name column to always be 2 times as wide as the status column
            TableColumn nameColumn = null;
            TableColumn statusColumn = null;
            nameColumn = _threadTable.getColumnModel().getColumn(0);
            statusColumn = _threadTable.getColumnModel().getColumn(1);
            nameColumn.setPreferredWidth(2*statusColumn.getPreferredWidth()); 
          }          
        }
        else {
          if (_threadTable != null) {
            _threadTable = null;
            _rightPane.remove(1);
          }
        }
      }
    });
    // Sets the method column to always be 7 times as wide as the line column
    TableColumn methodColumn = null;
    TableColumn lineColumn = null;
    methodColumn = _stackTable.getColumnModel().getColumn(0);
    lineColumn = _stackTable.getColumnModel().getColumn(1);
    methodColumn.setPreferredWidth(7*lineColumn.getPreferredWidth());  
  }
  
  /**
   * A table for displaying the watched variables and fields.
   */
  public class WatchTableModel extends AbstractTableModel {
    
    private String[] _columnNames = {"Name", "Value", "Type"};
    
    public String getColumnName(int col) { 
      return _columnNames[col]; 
    }
    public int getRowCount() { return _watches.size() + 1; }
    public int getColumnCount() { return _columnNames.length; }
    public Object getValueAt(int row, int col) {
      if (row < _watches.size()) {
        DebugManager.WatchData watch = _watches.elementAt(row);
        switch(col) {
          case 0: return watch.getName();
          case 1: Object value = watch.getValue();
          if (value == null)
            return "null";
          else if (value == DebugManager.WatchUndefinedValue.Singleton)
            return "<not in scope>";
          else
            return value.toString();        
          case 2: return watch.getType();
        }
        return null;
      }
      else {
        // Last row blank
        return "";
      }
    }
    public boolean isCellEditable(int row, int col) { 
      // First col for entering new values
      if (col == 0) {
        return true;
      }
      else {
        return false;
      }
    }
    public void setValueAt(Object value, int row, int col) {
      if ((value == null) || (value.equals(""))) {
        // Remove value
        _debugger.removeWatch(row);
      }
      else {
        // Add value
        _debugger.addWatch(String.valueOf(value));
      }
      fireTableCellUpdated(row, col);
    }
  }
  
  /**
   * A table for displaying the current stack trace.
   */
  public class StackTableModel extends AbstractTableModel {
    
    private String[] _columnNames = {"Method", "Line"};  // Do we need #?
    
    public String getColumnName(int col) { 
      return _columnNames[col]; 
    }
    public int getRowCount() { 
      if (_stackFrames == null) {
        return 0;
      }
      return _stackFrames.size();
    }
    public int getColumnCount() { return _columnNames.length; }
    
    public Object getValueAt(int row, int col) { 
      DebugManager.StackData frame = _stackFrames.elementAt(row);
      switch(col) {
        case 0: return frame.getMethod();
        case 1: return new Integer(frame.getLine());
      }
      return null;
    }
    public boolean isCellEditable(int row, int col) { 
      return false; 
    }
  }
  
  /**
   * A table for displaying all current threads.
   */
  public class ThreadTableModel extends AbstractTableModel {
    
    private String[] _columnNames = {"Name", "Status"};
    
    public String getColumnName(int col) { 
      return _columnNames[col]; 
    }
    public int getRowCount() { 
      if (_threads == null) {
        return 0;
      }
      return _threads.size();
    }
    public int getColumnCount() { return _columnNames.length; }
    
    public Object getValueAt(int row, int col) { 
      DebugManager.ThreadData threadData  = _threads.elementAt(row);
      switch(col) {
        case 0: return threadData.getName();
        case 1: return threadData.getStatus();
      }
      
      return null;
    }
    public boolean isCellEditable(int row, int col) { 
      return false; 
    }
  }
  
  /**
   * Creates the buttons for controlling the debugger.
   */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    mainButtons.setLayout( new GridLayout(0,1));
    
    Action resumeAction = new AbstractAction( "Resume" ) {
      public void actionPerformed(ActionEvent ae) {
        try {
          _frame.debuggerResume();
        }
        catch (DebugException de) {
          _frame._showDebugError(de);
        }
        _closeButton.requestFocus();
      }
    };
    _resumeButton = new JButton(resumeAction);
    
    Action stepIntoAction = new AbstractAction( "Step Into" ) {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(DebugManager.STEP_INTO);
        _stepIntoButton.requestFocus();
      }
    };
    _stepIntoButton = new JButton(stepIntoAction);
    
    Action stepOverAction = new AbstractAction( "Step Over" ) {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(DebugManager.STEP_OVER);
        _stepOverButton.requestFocus();
      }
    };
    _stepOverButton = new JButton(stepOverAction);
    
    Action stepOutAction = new AbstractAction( "Step Out" ) {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerStep(DebugManager.STEP_OUT);
        _stepOutButton.requestFocus();
      }
    };
    _stepOutButton = new JButton(stepOutAction);
    
    Action closeAction = new AbstractAction( "X" ) {
      public void actionPerformed(ActionEvent ae) {
        _frame.debuggerToggle();
      }
    };
    _closeButton = new JButton(closeAction);
    
    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);
    mainButtons.add(_resumeButton);
    mainButtons.add(_stepIntoButton);
    mainButtons.add(_stepOverButton);
    mainButtons.add(_stepOutButton);     
    disableButtons();
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }
  
  /**
   * Listens to events from the debug manager to keep the panel updated.
   */
  class DebugPanelListener implements DebugListener {
  
    /**
     * Called when debugger mode has been enabled.
     */
    public void debuggerStarted() {}
    
    /**
     * Called when debugger mode has been disabled.
     */
    public void debuggerShutdown() {}
    
    /**
     * Called when the given line is reached by the current thread in the 
     * debugger, to request that the line be displayed.
     * @param doc Document to display
     * @param lineNumber Line to display or highlight
     */
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber) {}
    
    /**
     * Called when a breakpoint is set in a document.
     * Adds the breakpoint to the tree of breakpoints.
     * @param bp the breakpoint
     */
    public void breakpointSet(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          DefaultMutableTreeNode bpDocNode = new DefaultMutableTreeNode(bp.getClassName());
          
          // Look for matching document node
          Enumeration documents = _breakpointRootNode.children();
          while (documents.hasMoreElements()) {
            DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
            if (doc.getUserObject().equals(bpDocNode.getUserObject())) {
              
              // Create a new breakpoint in this node
              //Sort breakpoints by line number.
              Enumeration lineNumbers = doc.children();
              while (lineNumbers.hasMoreElements()) {
                DefaultMutableTreeNode lineNumber = (DefaultMutableTreeNode)lineNumbers.nextElement();
                
                //if line number of indexed breakpoint is less than new breakpoint, continue
                if (((Integer)lineNumber.getUserObject()).intValue() > bp.getLineNumber()) {
                  
                  //else, add to the list
                  DefaultMutableTreeNode newBreakpoint = 
                    new DefaultMutableTreeNode(new Integer(bp.getLineNumber()));
                  _bpTreeModel.insertNodeInto(newBreakpoint,
                                              doc,
                                              doc.getIndex(lineNumber));
                  
                  // Make sure this node is visible
                  _bpTree.scrollPathToVisible(new TreePath(newBreakpoint.getPath()));
                  return;
                }
              }
              //if none are greater, add at the end 
              DefaultMutableTreeNode newBreakpoint = 
                new DefaultMutableTreeNode(new Integer(bp.getLineNumber()));
              _bpTreeModel.insertNodeInto(newBreakpoint,
                                          doc,
                                          doc.getChildCount());
              
              // Make sure this node is visible
              _bpTree.scrollPathToVisible(new TreePath(newBreakpoint.getPath()));
              return;
            }
          }
          // No matching document node was found, so create one
          _bpTreeModel.insertNodeInto(bpDocNode,
                                      _breakpointRootNode,
                                      _breakpointRootNode.getChildCount());
          DefaultMutableTreeNode newBreakpoint = 
            new DefaultMutableTreeNode(new Integer(bp.getLineNumber()));
          _bpTreeModel.insertNodeInto(newBreakpoint,
                                      bpDocNode,
                                      bpDocNode.getChildCount());
          
          // Make visible
          TreePath pathToNewBreakpoint = new TreePath(newBreakpoint.getPath());
          _bpTree.scrollPathToVisible(pathToNewBreakpoint);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    /**
     * Called when a breakpoint is reached during execution.
     * @param bp the breakpoint
     */
    public void breakpointReached(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          DefaultMutableTreeNode bpDoc = new DefaultMutableTreeNode(bp.getClassName());
          
          // Find the document node for this breakpoint
          Enumeration documents = _breakpointRootNode.children();
          while (documents.hasMoreElements()) {
            DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
            if (doc.getUserObject().equals(bpDoc.getUserObject())) {              
              // Find the correct line number node for this breakpoint
              Enumeration lineNumbers = doc.children();
              while (lineNumbers.hasMoreElements()) {
                DefaultMutableTreeNode lineNumber = 
                  (DefaultMutableTreeNode)lineNumbers.nextElement();
                if (lineNumber.getUserObject().equals(new Integer(bp.getLineNumber()))) {
                  
                  // Select the node which has been hit
                  TreePath pathToNewBreakpoint = new TreePath(lineNumber.getPath());
                  _bpTree.scrollPathToVisible(pathToNewBreakpoint);
                  _bpTree.setSelectionPath(pathToNewBreakpoint);
                }
              }
            }
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    /**
     * Called when a breakpoint is removed from a document.
     * Removes the breakpoint from the tree of breakpoints.
     * @param bp the breakpoint
     */
    public void breakpointRemoved(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          DefaultMutableTreeNode bpDocNode = new DefaultMutableTreeNode(bp.getClassName());
          
          // Find the document node for this breakpoint
          Enumeration documents = _breakpointRootNode.children();
          while (documents.hasMoreElements()) {
            DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
            if (doc.getUserObject().equals(bpDocNode.getUserObject())) {
              // Find the correct line number node for this breakpoint
              Enumeration lineNumbers = doc.children();
              while (lineNumbers.hasMoreElements()) {
                DefaultMutableTreeNode lineNumber = 
                  (DefaultMutableTreeNode)lineNumbers.nextElement();
                if (lineNumber.getUserObject().equals(new Integer(bp.getLineNumber()))) {
                  _bpTreeModel.removeNodeFromParent(lineNumber);
                  if (doc.getChildCount() == 0) {
                    // this document has no more breakpoints, remove it
                    _bpTreeModel.removeNodeFromParent(doc);
                  }        
                  return;
                }
              }
            }
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    /**
     * Called when a step is requested on the current thread.
     */
    public void stepRequested() {}
    
    /**
     * Called when the current thread is suspended
     */
    public void currThreadSuspended() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          updateData(true);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    /**
     * Called when the current thread is resumed
     */
    public void currThreadResumed() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          updateData(false);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    /**
     * Called when the current thread dies
     */
    public void currThreadDied() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          updateData(false);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
  }
  

  
  /**
   * Enables and disables the appropriate buttons depending on if the current
   * thread has been suspended or resumed
   * @param isSuspended indicates if the current thread has been suspended
   */
  public void setThreadDependentButtons(boolean isSuspended) {
    _resumeButton.setEnabled(isSuspended);
    _stepIntoButton.setEnabled(isSuspended);
    _stepOverButton.setEnabled(isSuspended);
    _stepOutButton.setEnabled(isSuspended);
  }
  public void disableButtons() {
    setThreadDependentButtons(false);
  }
  
  public void setStatusText(String text) {
    _statusBar.setText(text);
  }
  
  public String getStatusText() {
    return _statusBar.getText();
  }
}

