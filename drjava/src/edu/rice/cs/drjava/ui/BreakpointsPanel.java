/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import java.util.Vector;

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/**
 * Panel for displaying the breakpoints and watches.  This
 * class is a swing view class and hence should only be accessed from the 
 * event-handling thread.
 * @version $Id: DebugPanel.java,v 1.69 2006/02/20 21:22:03 rcartwright Exp $
 */
public class BreakpointsPanel extends TabbedPanel {
  private JPanel _leftPane;
  
  private DefaultMutableTreeNode _breakpointRootNode;
  private DefaultTreeModel _bpTreeModel;
  private JTree _bpTree;
  
  private JPopupMenu _breakpointPopupMenu;
  
  private final SingleDisplayModel _model;
  private final MainFrame _frame;
  private final Debugger _debugger;
  private DefinitionsPane _defPane = null;
  
  private JPanel _buttonPanel;
  private JButton _goToButton;
  private JButton _enableDisableButton;
  private JButton _removeButton;
  private JButton _removeAllButton;
  
  private DefaultTreeCellRenderer dtcr;
  
  /** Constructs a new panel to display breakpoints.  This is swing view class and hence should only
   *  be accessed from the event-handling thread.
   */
  public BreakpointsPanel(MainFrame frame) {
    super(frame, "Breakpoints");
    
    this.setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    _debugger = _model.getDebugger();
    
    /******** Initialize the panels containing the checkboxes ********/
    this.removeAll(); // actually, override the behavior of TabbedPanel

    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    _leftPane = new JPanel(new BorderLayout());
    _setupBreakpointTree();
    
    this.add(_leftPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    
    _debugger.addListener(new BreakpointsPanelListener());
    
    // Setup the color listeners.
    _setColors(_bpTree);
  }
  
  /** Quick helper for setting up color listeners. */
  private static void _setColors(Component c) {
    new ForegroundColorListener(c);
    new BackgroundColorListener(c);
  }
  
  protected void _close() {
    _defPane.requestFocusInWindow();
    if (_displayed) stopListening();
    super._close();
  }
  
  /** Called from MainFrame in response to opening this or changes in the active document. */
  void beginListeningTo(DefinitionsPane defPane) {
    if (_defPane==null) {
      _displayed = true;
      _defPane = defPane;      
      _updateButtons();
    }
    else
      throw new UnexpectedException(new RuntimeException("BreakpointsPanel should not be listening to anything"));
  }

  /** Called from MainFrame upon closing this Dialog or changes in the active document */
  public void stopListening() {
    if (_defPane != null) {
      _defPane = null;
      _displayed = false;
    } 
  }
  
  /** Creates the breakpoint tree. */
  private void _setupBreakpointTree() {
    _breakpointRootNode = new DefaultMutableTreeNode("Breakpoints");
    _bpTreeModel = new DefaultTreeModel(_breakpointRootNode);
    _bpTree = new BPTree(_bpTreeModel);
    _bpTree.setEditable(false);
    _bpTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _bpTree.setShowsRootHandles(true);
    _bpTree.setRootVisible(false);
    _bpTree.putClientProperty("JTree.lineStyle", "Angled");
    _bpTree.setScrollsOnExpand(true);
    _bpTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        _updateButtons();
      }
    });
    // Breakpoint tree cell renderer
    dtcr = new BreakPointRenderer();
    dtcr.setOpaque(false);
    _setColors(dtcr);
    _bpTree.setCellRenderer(dtcr);
    
    _leftPane.add(new JScrollPane(_bpTree));
    
    _initPopup();
  }
  
  /** Update button state and text. */
  private void _updateButtons() {
    try {
      Breakpoint bp = _getSelectedBreakpoint();
      boolean enable = (bp != null);
      _goToButton.setEnabled(enable);
      _enableDisableButton.setEnabled(enable);
      _removeButton.setEnabled(enable);
      if (enable) {
        if (bp.isEnabled()) {
          _enableDisableButton.setText("Disable");
        }
        else {
          _enableDisableButton.setText("Enable");
        }
      }
    }
    catch (DebugException de) {
      _goToButton.setEnabled(false);
      _enableDisableButton.setEnabled(false);
      _removeButton.setEnabled(false);
    }
    _removeAllButton.setEnabled((_breakpointRootNode!=null) && (_breakpointRootNode.getDepth()>0));
  }
  
  /** Adds config color support to DefaultTreeCellEditor. */
  static class BreakPointRenderer extends DefaultTreeCellRenderer {
    
    public void setBackground(Color c) {
      this.setBackgroundNonSelectionColor(c);
    }
    
    public void setForeground(Color c) {
      this.setTextNonSelectionColor(c);
    }
    
    private BreakPointRenderer() {
      this.setTextSelectionColor(Color.black);
      setLeafIcon(null);
      setOpenIcon(null);
      setClosedIcon(null);
    }
    
    /**
     * Overrides the default renderer component to use proper coloring.
     */
    public Component getTreeCellRendererComponent
      (JTree tree, Object value, boolean selected, boolean expanded,
       boolean leaf, int row, boolean hasFocus) {
      Component renderer = super.getTreeCellRendererComponent
        (tree, value, selected, expanded, leaf, row, hasFocus);
      
      if (renderer instanceof JComponent) {
        ((JComponent) renderer).setOpaque(false);
      }
      
      _setColors(renderer);
      return renderer;
    }
  }
  
  /** Creates the buttons for controlling the breakpoints. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    mainButtons.setLayout( new GridLayout(0,1));
    
    Action enableDisableAction = new AbstractAction("Disable") {
      public void actionPerformed(ActionEvent ae) {
        _enableDisableBreakpoint();
      }
    };
    _enableDisableButton = new JButton(enableDisableAction);
    
    Action goToAction = new AbstractAction("Go to") {
      public void actionPerformed(ActionEvent ae) {
        _goToBreakpoint();
      }
    };
    _goToButton = new JButton(goToAction);
    
    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) {
        _removeBreakpoint();
      }
    };
    _removeButton = new JButton(removeAction);
    
    Action removeAllAction = new AbstractAction("Remove All") {
      public void actionPerformed(ActionEvent ae) {
        try {
          _debugger.removeAllBreakpoints();
        }
        catch (DebugException de) {
          _frame._showDebugError(de);
        }
      }
    };
    _removeAllButton = new JButton(removeAllAction);
    
    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);
    mainButtons.add(_goToButton);
    mainButtons.add(_enableDisableButton);
    mainButtons.add(_removeButton);
    mainButtons.add(_removeAllButton);
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }
  
  /**
   * Initializes the pop-up menu.
   */
  private void _initPopup() {
    _breakpointPopupMenu = new JPopupMenu("Breakpoint");
    _breakpointPopupMenu.add(new AbstractAction("Go to Breakpoint") {
      public void actionPerformed(ActionEvent e) {
        _goToBreakpoint();
      }
    });
    _breakpointPopupMenu.add(new AbstractAction("Remove Breakpoint") {
      public void actionPerformed(ActionEvent e) {
        _removeBreakpoint();
      }
    });
    _bpTree.addMouseListener(new BreakpointMouseAdapter());
  }
  
  /** Gets the currently selected breakpoint in the breakpoint tree, or null if the selected node is a classname and not a breakpoint.
   *  @return the current breakpoint in the tree
   *  @throws DebugException if the node is not a valid breakpoint
   */
  private Breakpoint _getSelectedBreakpoint() throws DebugException {
    TreePath path = _bpTree.getSelectionPath();
    if (path == null || path.getPathCount() != 3) {
      return null;
    }
    else {
      DefaultMutableTreeNode lineNode =
        (DefaultMutableTreeNode)path.getLastPathComponent();
      int line = ((BPTreeUserObj) lineNode.getUserObject()).lineNumber();
      DefaultMutableTreeNode classNameNode =
        (DefaultMutableTreeNode) path.getPathComponent(1);
      String className = (String) classNameNode.getUserObject();
      return _debugger.getBreakpoint(line, className);
    }
  }
  
  /** Go to breakpoint. */
  private void _goToBreakpoint() {
    try {
      Breakpoint bp = _getSelectedBreakpoint();
      if (bp != null) {
        _debugger.scrollToSource(bp);
      }
    }
    catch (DebugException de) {
      _frame._showDebugError(de);
    }
  }
  
  /** Toggle breakpoint's enable/disable flag. */
  private void _enableDisableBreakpoint() {
    try {
      Breakpoint bp = _getSelectedBreakpoint();
      if (bp != null) {
        bp.setEnabled(!bp.isEnabled());
        _updateButtons();
      }
    }
    catch (DebugException de) {
      _frame._showDebugError(de);
    }
  }
  
  /** Remove the breakpoint. */
  private void _removeBreakpoint() {
    try {
      Breakpoint bp = _getSelectedBreakpoint();
      if (bp != null) _debugger.removeBreakpoint(bp);
    }
    catch (DebugException de) { _frame._showDebugError(de); }
  }
  
  /** Listens to events from the debug manager to keep the panel updated. */
  class BreakpointsPanelListener implements DebugListener {
    
    /** Called when debugger mode has been enabled. Must be executed in event thread. */
    public void debuggerStarted() { }
    
    /** Called when debugger mode has been disabled. Must be executed in event thread. */
    public void debuggerShutdown() { }
    
    /** Called when the given line is reached by the current thread in the debugger, to request that the line be 
     *  displayed.  Must be executed only in the event thread.
     *  @param doc Document to display
     *  @param lineNumber Line to display or highlight
     *  @param shouldHighlight true iff the line should be highlighted.
     */
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) { }
    
    /** Called when a breakpoint is set in a document. Adds the breakpoint to the tree of breakpoints.
     *  Must be executed in event thread.
     *  @param bp the breakpoint
     */
    public void breakpointSet(final Breakpoint bp) {
//      // Only change GUI from event-dispatching thread
//      Runnable doCommand = new Runnable() {
//        public void run() {
      DefaultMutableTreeNode bpDocNode = new DefaultMutableTreeNode(bp.getClassName());
      
      // Look for matching document node
      // Raw type here due to Swing's use of raw types.
      Enumeration documents = _breakpointRootNode.children();
      while (documents.hasMoreElements()) {
        DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
        if (doc.getUserObject().equals(bpDocNode.getUserObject())) {
          
          // Create a new breakpoint in this node
          //Sort breakpoints by line number.
          // Raw type here due to Swing's use of raw types.
          Enumeration lineNumbers = doc.children();
          while (lineNumbers.hasMoreElements()) {
            DefaultMutableTreeNode lineNumber = (DefaultMutableTreeNode)lineNumbers.nextElement();
            
            //if line number of indexed breakpoint is less than new breakpoint, continue
            if (((BPTreeUserObj)lineNumber.getUserObject()).lineNumber() > bp.getLineNumber()) {
              
              //else, add to the list
              DefaultMutableTreeNode newBreakpoint =
                new DefaultMutableTreeNode(new BPTreeUserObj(bp.getLineNumber(), bp.isEnabled()));
              _bpTreeModel.insertNodeInto(newBreakpoint, doc, doc.getIndex(lineNumber));
              
              // Make sure this node is visible
              _bpTree.scrollPathToVisible(new TreePath(newBreakpoint.getPath()));
              return;
            }
          }
          //if none are greater, add at the end
          DefaultMutableTreeNode newBreakpoint =
            new DefaultMutableTreeNode(new BPTreeUserObj(bp.getLineNumber(), bp.isEnabled()));
          _bpTreeModel.insertNodeInto(newBreakpoint, doc, doc.getChildCount());
          
          // Make sure this node is visible
          _bpTree.scrollPathToVisible(new TreePath(newBreakpoint.getPath()));
          return;
        }
      }
      // No matching document node was found, so create one
      _bpTreeModel.insertNodeInto(bpDocNode, _breakpointRootNode, _breakpointRootNode.getChildCount());
      DefaultMutableTreeNode newBreakpoint =
        new DefaultMutableTreeNode(new BPTreeUserObj(bp.getLineNumber(), bp.isEnabled()));
      _bpTreeModel.insertNodeInto(newBreakpoint, bpDocNode, bpDocNode.getChildCount());
      
      // Make visible
      TreePath pathToNewBreakpoint = new TreePath(newBreakpoint.getPath());
      _bpTree.scrollPathToVisible(pathToNewBreakpoint);
//        }
//      };
//      Utilities.invokeLater(doCommand);
      
      _updateButtons();
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
      Utilities.invokeLater(doCommand);
    }
    
    /**
     * Called when a breakpoint is changed.
     * Removes the breakpoint from the tree of breakpoints.
     * @param bp the breakpoint
     */
    public void breakpointChanged(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          DefaultMutableTreeNode bpDocNode = new DefaultMutableTreeNode(bp.getClassName());
          
          // Find the document node for this breakpoint
          Enumeration documents = _breakpointRootNode.children();
          boolean found = false;
          while ((!found) && (documents.hasMoreElements())) {
            DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
            if (doc.getUserObject().equals(bpDocNode.getUserObject())) {
              // Find the correct line number node for this breakpoint
              Enumeration lineNumbers = doc.children();
              while (lineNumbers.hasMoreElements()) {
                DefaultMutableTreeNode lineNumber =
                  (DefaultMutableTreeNode)lineNumbers.nextElement();
                BPTreeUserObj uo = (BPTreeUserObj)lineNumber.getUserObject();
                if (uo.lineNumber()==bp.getLineNumber()) {
                  uo.setEnabled(bp.isEnabled());
                  ((DefaultTreeModel)_bpTree.getModel()).nodeChanged(lineNumber);
                  found = true;
                  break;
                }
              }
            }
          }
          _updateButtons();
        }
      };
      Utilities.invokeLater(doCommand);
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
          boolean found = false;
          while ((!found) && (documents.hasMoreElements())) {
            DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
            if (doc.getUserObject().equals(bpDocNode.getUserObject())) {
              // Find the correct line number node for this breakpoint
              Enumeration lineNumbers = doc.children();
              while (lineNumbers.hasMoreElements()) {
                DefaultMutableTreeNode lineNumber =
                  (DefaultMutableTreeNode)lineNumbers.nextElement();
                if (((BPTreeUserObj)lineNumber.getUserObject()).lineNumber()==bp.getLineNumber()) {
                  _bpTreeModel.removeNodeFromParent(lineNumber);
                  if (doc.getChildCount() == 0) {
                    // this document has no more breakpoints, remove it
                    _bpTreeModel.removeNodeFromParent(doc);
                  }
                  found = true;
                  break;
                }
              }
            }
          }
          _updateButtons();
        }
      };
      Utilities.invokeLater(doCommand);
    }
    
    public void watchSet(final DebugWatchData w) { }
    public void watchRemoved(final DebugWatchData w) { }
    
    /** Called when a step is requested on the current thread. */
    public void stepRequested() { }
    
    /** Called when the current thread is suspended. */
    public void currThreadSuspended() { }
    
    /** Called when the current thread is resumed */
    public void currThreadResumed() { }
    
    /** Called when a thread starts.  Must be executed in event thread. */
    public void threadStarted() { }
    
    /** Called when the current thread dies. Must be executed in event thread. */
    public void currThreadDied() { }
    
    /** Called when any thread other than the current thread dies. Must be executed in event thread. */
    public void nonCurrThreadDied() {  }
    
    /** Called when the current (selected) thread is set in the debugger.
     *  @param thread the thread that was set as current
     */
    public void currThreadSet(DebugThreadData thread) { }
  }
  
  /**
   * Mouse adapter for the breakpoint tree.
   */
  private class BreakpointMouseAdapter extends RightClickMouseAdapter {
    protected void _popupAction(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      TreePath path = _bpTree.getPathForLocation(x, y);
      if (path != null && path.getPathCount() == 3) {
        _bpTree.setSelectionRow(_bpTree.getRowForLocation(x, y));
        _breakpointPopupMenu.show(e.getComponent(), x, y);
      }
    }
    
    public void mousePressed(MouseEvent e) {
      super.mousePressed(e);
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        _goToBreakpoint();
      }
    }
  }
  
  private class BPTree extends JTree {
    public BPTree(DefaultTreeModel s) {
      super(s);
    }
    
    public void setForeground(Color c) {
      super.setForeground(c);
      if (dtcr != null) dtcr.setTextNonSelectionColor(c);
    }
    
    public void setBackground(Color c) {
      super.setBackground(c);
      if (BreakpointsPanel.this != null && dtcr != null) dtcr.setBackgroundNonSelectionColor(c);
    }
  }
  
  private class BPTreeUserObj {
    private int _lineNumber;
    private boolean _enabled;
    public int lineNumber() { return _lineNumber; }
    public boolean isEnabled() { return _enabled; }
    public void setEnabled(boolean e) { _enabled = e; }
    public BPTreeUserObj(int l, boolean e) { _lineNumber = l; _enabled = e; }
    public String toString() { return String.valueOf(_lineNumber) + ((_enabled)?"":" (disabled)"); }
  }
}
