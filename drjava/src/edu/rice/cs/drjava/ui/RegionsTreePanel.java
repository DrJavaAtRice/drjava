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

import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.*;

import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/**
 * Panel for displaying regions in a tree sorted by class name and line number.
 * This class is a swing view class and hence should only be accessed from the event-handling thread.
 * @version $Id$
 */
public abstract class RegionsTreePanel<R extends DocumentRegion> extends TabbedPanel {
  protected JPanel _leftPane;
  
  protected DefaultMutableTreeNode _regionRootNode;
  protected DefaultTreeModel _regTreeModel;
  protected JTree _regTree;
  protected String _title;
  
  protected JPopupMenu _regionPopupMenu;
  
  protected final SingleDisplayModel _model;
  protected final MainFrame _frame;
  
  protected JPanel _buttonPanel;
  
  protected DefaultTreeCellRenderer dtcr;
  
  /** Constructs a new panel to display regions in a tree.
   *  This is swing view class and hence should only be accessed from the event-handling thread.
   *  @param frame the MainFrame
   *  @param title title of the pane
   */
  public RegionsTreePanel(MainFrame frame, String title) {
    super(frame, title);
    _title = title;
    this.setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    
    this.removeAll(); // override the behavior of TabbedPanel

    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    _leftPane = new JPanel(new BorderLayout());
    _setupRegionTree();
    
    this.add(_leftPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    
    // Setup the color listeners.
    _setColors(_regTree);
  }
  
  /** Quick helper for setting up color listeners. */
  private static void _setColors(Component c) {
    new ForegroundColorListener(c);
    new BackgroundColorListener(c);
  }
  
  /** Close the pane. */
  protected void _close() {
    super._close();
    updateButtons();
  }
  
  /** Creates the region tree. */
  private void _setupRegionTree() {
    _regionRootNode = new DefaultMutableTreeNode(_title);
    _regTreeModel = new DefaultTreeModel(_regionRootNode);
    _regTree = new RegionTree(_regTreeModel);
    _regTree.setEditable(false);
    _regTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _regTree.setShowsRootHandles(true);
    _regTree.setRootVisible(false);
    _regTree.putClientProperty("JTree.lineStyle", "Angled");
    _regTree.setScrollsOnExpand(true);
    _regTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        updateButtons();
      }
    });
    
    // Region tree cell renderer
    dtcr = new RegionRenderer();
    dtcr.setOpaque(false);
    _setColors(dtcr);
    _regTree.setCellRenderer(dtcr);

    _leftPane.add(new JScrollPane(_regTree));

    _initPopup();
    
    ToolTipManager.sharedInstance().registerComponent(_regTree);
  }
  
  /** Update button state and text. Should be overridden if additional buttons are added besides "Go To", "Remove" and "Remove All". */
  protected void updateButtons() {
  }
  
  /** Adds config color support to DefaultTreeCellEditor. */
  class RegionRenderer extends DefaultTreeCellRenderer {
    
    public void setBackground(Color c) {
      this.setBackgroundNonSelectionColor(c);
    }
    
    public void setForeground(Color c) {
      this.setTextNonSelectionColor(c);
    }
    
    private RegionRenderer() {
      this.setTextSelectionColor(Color.black);
      setLeafIcon(null);
      setOpenIcon(null);
      setClosedIcon(null);
    }

    /**
     * Overrides the default renderer component to use proper coloring.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, 
                                                   boolean leaf, int row, boolean hasFocus) {
      Component renderer = super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, leaf, row, hasFocus);
      
      if (renderer instanceof JComponent) { ((JComponent) renderer).setOpaque(false); }
      
      _setColors(renderer);
      
      // set tooltip
      String tooltip = null;
      if (leaf) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if (node.getUserObject() instanceof RegionTreeUserObj) {
          @SuppressWarnings("unchecked") R r = ((RegionTreeUserObj<R>)(node.getUserObject())).region();
          
          OpenDefinitionsDocument doc = r.getDocument();
          doc.readLock();
          try {
            int lnr = doc.getLineOfOffset(r.getStartOffset())+1;
            int startOffset = r.getStartOffset();
            int endOffset = doc.getOffset(lnr+6);
            if (endOffset<0) { endOffset = doc.getLength()-1; }
            
            // convert to HTML (i.e. < to &lt; and > to &gt; and newlines to <br>)
            String s = doc.getText(startOffset, endOffset-startOffset);
            s = s.replace("<", "&lt;");
            s = s.replace(">", "&gt;");
            s = s.replace(System.getProperty("line.separator"),"<br>");
            s = s.replace("\n","<br>");
            tooltip = "<html><pre>"+s+"</pre></html>";
          }
          catch(javax.swing.text.BadLocationException ble) { tooltip = null; /* just don't give a tool tip */ }
          finally { doc.readUnlock(); }
        }
      }
      setToolTipText(tooltip);
      return renderer;
    }
  }

  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JButton[] makeButtons() {        
    return new JButton[0];    
  }
  
  /** Creates the buttons for controlling the regions. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    JButton[] buts = makeButtons();

    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);    
    for (JButton b: buts) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;

    for (JButton b: buts) { gbLayout.setConstraints(b, c); }
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }
  
  /** Makes the popup menu actions. Should be overridden. */
  protected AbstractAction[] makePopupMenuActions() {
    return null;
  }
  
  /** Initializes the pop-up menu. */
  private void _initPopup() {
    _regionPopupMenu = new JPopupMenu(_title);
    AbstractAction[] acts = makePopupMenuActions();
    if (acts!=null) {
      for (AbstractAction a: acts) {
        _regionPopupMenu.add(a);
      }
      _regTree.addMouseListener(new RegionMouseAdapter());
    }
  }
  
  /** Gets the currently selected region in the region tree, or null if the selected node is a classname and not a region.
   *  @return the current region in the tree
   */
  protected R getSelectedRegion() {
    TreePath path = _regTree.getSelectionPath();
    if (path == null || path.getPathCount() != 3) {
      return null;
    }
    else {
      DefaultMutableTreeNode lineNode = (DefaultMutableTreeNode)path.getLastPathComponent();
      @SuppressWarnings("unchecked") R r = ((RegionTreeUserObj<R>) lineNode.getUserObject()).region();
      return r;
    }
  }
  
  /** Go to region. */
  protected void goToRegion() {
    R r = getSelectedRegion();
    if (r != null) {
      _frame.scrollToDocumentAndLine(r.getDocument(), r.getDocument().getLineOfOffset(r.getStartOffset())+1, false);
    }
  }
    
  /** Add a region to the tree. Must be executed in event thread.
   *  @param r the region
   */
  public void addRegion(final R r) {
    String name = "";
    try {
      name = r.getDocument().getQualifiedClassName();
    }
    catch (ClassNameNotFoundException cnnfe) {
      name = r.getDocument().toString();
    }

    DefaultMutableTreeNode regDocNode = new DefaultMutableTreeNode(name);
    
    // Look for matching document node
    // Raw type here due to Swing's use of raw types.
    Enumeration documents = _regionRootNode.children();
    boolean done = false;
    while (!done && (documents.hasMoreElements())) {
      DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
      if (doc.getUserObject().equals(regDocNode.getUserObject())) {
        
        // Create a new region in this node
        // Sort regions by line number.
        // Raw type here due to Swing's use of raw types.
        Enumeration lineNumbers = doc.children();
        while (lineNumbers.hasMoreElements()) {
          DefaultMutableTreeNode lineNumber = (DefaultMutableTreeNode)lineNumbers.nextElement();
          
          // if line number of indexed regions is less than new region, continue
          int lnr = r.getDocument().getLineOfOffset(r.getStartOffset())+1;
          if (((RegionTreeUserObj)lineNumber.getUserObject()).lineNumber() == lnr) {
            // don't add, already there
            // just make sure this node is visible
            _regTree.scrollPathToVisible(new TreePath(lineNumber));
            done = true;
            break;
          }
          else if (((RegionTreeUserObj)lineNumber.getUserObject()).lineNumber() > lnr) {
            
            // else, add to the list
            DefaultMutableTreeNode newRegion = new DefaultMutableTreeNode(new RegionTreeUserObj<R>(r));
            _regTreeModel.insertNodeInto(newRegion, doc, doc.getIndex(lineNumber));
            
            // Make sure this node is visible
            _regTree.scrollPathToVisible(new TreePath(newRegion.getPath()));
            done = true;
            break;
          }
        }
        if (done) { break; }
        
        // if none are greater, add at the end
        DefaultMutableTreeNode newRegion = new DefaultMutableTreeNode(new RegionTreeUserObj<R>(r));
        _regTreeModel.insertNodeInto(newRegion, doc, doc.getChildCount());
        
        // Make sure this node is visible
        _regTree.scrollPathToVisible(new TreePath(newRegion.getPath()));
        done = true;
        break;
      }
    }
    
    if (!done) {
      // No matching document node was found, so create one
      _regTreeModel.insertNodeInto(regDocNode, _regionRootNode, _regionRootNode.getChildCount());
      DefaultMutableTreeNode newRegion = new DefaultMutableTreeNode(new RegionTreeUserObj<R>(r));
      _regTreeModel.insertNodeInto(newRegion, regDocNode, regDocNode.getChildCount());
      
      // Make visible
      TreePath pathToNewRegion = new TreePath(newRegion.getPath());
      _regTree.scrollPathToVisible(pathToNewRegion);
    }
    
    updateButtons();
  }
  
  /** Remove a region from the tree. Must be executed in event thread.
   *  @param r the region
   */
  public void removeRegion(final R r) {
    // Only change GUI from event-dispatching thread
    Runnable doCommand = new Runnable() {
      public void run() {
        String name = "";
        try {
          name = r.getDocument().getQualifiedClassName();
        }
        catch (ClassNameNotFoundException cnnfe) {
          name = r.getDocument().toString();
        }
        
        DefaultMutableTreeNode regDocNode = new DefaultMutableTreeNode(name);
        
        // Find the document node for this region
        Enumeration documents = _regionRootNode.children();
        boolean found = false;
        while ((!found) && (documents.hasMoreElements())) {
          DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
          if (doc.getUserObject().equals(regDocNode.getUserObject())) {
            // Find the correct line number node for this breakpoint
            Enumeration lineNumbers = doc.children();
            while (lineNumbers.hasMoreElements()) {
              DefaultMutableTreeNode lineNumber = (DefaultMutableTreeNode)lineNumbers.nextElement();
              if (((RegionTreeUserObj)lineNumber.getUserObject()).lineNumber()==(r.getDocument().getLineOfOffset(r.getStartOffset())+1)) {
                _regTreeModel.removeNodeFromParent(lineNumber);
                // notify
                if (doc.getChildCount() == 0) {
                  // this document has no more breakpoints, remove it
                  _regTreeModel.removeNodeFromParent(doc);
                }
                found = true;
                break;
              }
            }
          }
        }
        updateButtons();
      }
    };
    Utilities.invokeLater(doCommand);
  }
  
  /** Remove all regions for this document from the tree. Must be executed in event thread.
   */
  public void removeRegions(final OpenDefinitionsDocument odd) {
    // Only change GUI from event-dispatching thread
    Runnable doCommand = new Runnable() {
      public void run() {
        String name = "";
        try {
          name = odd.getQualifiedClassName();
        }
        catch (ClassNameNotFoundException cnnfe) {
          name = odd.toString();
        }
        
        DefaultMutableTreeNode regDocNode = new DefaultMutableTreeNode(name);
        
        // Find the document node for this region
        Enumeration documents = _regionRootNode.children();
        while (documents.hasMoreElements()) {
          DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
          if (doc.getUserObject().equals(regDocNode.getUserObject())) {
            while(doc.getChildCount()>0) {
              DefaultMutableTreeNode lineNumber = (DefaultMutableTreeNode)doc.getFirstChild();
              @SuppressWarnings("unchecked") R r = (R) ((RegionTreeUserObj<R>)lineNumber.getUserObject()).region();
              _regTreeModel.removeNodeFromParent(lineNumber);
            }
            _regTreeModel.removeNodeFromParent(doc);
          }
        }
        updateButtons();
      }
    };
    Utilities.invokeLater(doCommand);
  }
  
  /**
   * Mouse adapter for the region tree.
   */
  protected class RegionMouseAdapter extends RightClickMouseAdapter {
    protected void _popupAction(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      TreePath path = _regTree.getPathForLocation(x, y);
      if (path != null && path.getPathCount() == 3) {
        _regTree.setSelectionRow(_regTree.getRowForLocation(x, y));
        _regionPopupMenu.show(e.getComponent(), x, y);
      }
    }
    
    public void mousePressed(MouseEvent e) {
      super.mousePressed(e);
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        goToRegion();
      }
    }
  }
  
  protected class RegionTree extends JTree {
    public RegionTree(DefaultTreeModel s) {
      super(s);
    }
    
    public void setForeground(Color c) {
      super.setForeground(c);
      if (dtcr != null) dtcr.setTextNonSelectionColor(c);
    }
    
    public void setBackground(Color c) {
      super.setBackground(c);
      if (RegionsTreePanel.this != null && dtcr != null) dtcr.setBackgroundNonSelectionColor(c);
    }
  }
  
  protected static class RegionTreeUserObj<R extends DocumentRegion> {
    private R _region;
    public int lineNumber() { return _region.getDocument().getLineOfOffset(_region.getStartOffset())+1; }
    public R region() { return _region; }
    public RegionTreeUserObj(R r) { _region = r; }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      _region.getDocument().readLock();
      try {
        sb.append(lineNumber());
        try {
          int length = Math.min(120, _region.getEndOffset()-_region.getStartOffset());
          String text = ": " + _region.getDocument().getText(_region.getStartOffset(), length).trim();
          sb.append(text);
        } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      } finally { _region.getDocument().readUnlock(); }
      return sb.toString();
    }
    public boolean equals(Object other) {
      @SuppressWarnings("unchecked") RegionTreeUserObj<R> o = (RegionTreeUserObj<R>)other;
      return (o.region().getDocument().equals(region().getDocument())) &&
        (o.region().getStartOffset()==region().getStartOffset()) &&
        (o.region().getEndOffset()==region().getEndOffset());
    }
  }
}
