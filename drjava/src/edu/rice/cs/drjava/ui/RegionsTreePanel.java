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

package edu.rice.cs.drjava.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicToolTipUI;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.IDocumentRegion;
import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.RightClickMouseAdapter;
import edu.rice.cs.util.swing.Utilities;

/** Panel for displaying regions in a tree sorted by class name and line number.  Only accessed from event thread.
  * @version $Id$
  */
public abstract class RegionsTreePanel<R extends IDocumentRegion> extends TabbedPanel {
  protected JPanel _leftPane;
  
  protected DefaultMutableTreeNode _rootNode;
  protected DefaultTreeModel _regTreeModel;
  public JTree _regTree;
  protected String _title;
  protected RegionManager<R> _regionManager;
  
  protected JPopupMenu _regionPopupMenu;
  
  protected final SingleDisplayModel _model;
  protected final MainFrame _frame;
  
  protected JPanel _buttonPanel;
  
  protected DefaultTreeCellRenderer dtcr;
  
  /* _ */
  
  /** Cached values from last region insertion. _cachedDoc is non-null iff the last added region occurred at the end of
    * the list of regions for its document. If _cachedDoc is null, the other cached values are invalid. */
  protected OpenDefinitionsDocument _cachedDoc = null;
  protected DefaultMutableTreeNode _cachedDocNode = null;
  protected int _cachedRegionIndex = 0;
  protected int _cachedStartOffset = 0;
  
  /** State pattern to improve performance when rapid changes are made. */
  protected final IChangeState DEFAULT_STATE = new DefaultState();
//  protected final IChangeState CHANGING_STATE = new ChangingState();
  protected IChangeState _changeState = DEFAULT_STATE;
  
  /** A table mapping each document entered in this panel to its corresponding MutableTreeNode in _regTreeModel. */
  protected volatile HashMap<OpenDefinitionsDocument, DefaultMutableTreeNode> _docToTreeNode = 
    new HashMap<OpenDefinitionsDocument, DefaultMutableTreeNode>();
  
  /** A table mapping each region entered in this panel to its corresponding MutableTreeNode in _regTreeModel. */
  protected volatile IdentityHashMap<R, DefaultMutableTreeNode> _regionToTreeNode = 
    new IdentityHashMap<R, DefaultMutableTreeNode>();
  
  /** State variable used to control the granular updating of the tabbed panel. */
  private volatile long _lastChangeTime;
  private volatile Object _updateLock = new Object();
  private volatile boolean _updatePending = false;
  public static final long UPDATE_DELAY = 2000L;  // update delay threshold in milliseconds
  
  /** Constructs a new panel to display regions in a tree. This is swing view class and hence should only be accessed 
    * from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    */
  public RegionsTreePanel(MainFrame frame, String title, RegionManager<R> regionManager) {
    super(frame, title);
    _title = title;
    _regionManager = regionManager;
    setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    
    removeAll(); // override the behavior of TabbedPanel
    
    _changeState = DEFAULT_STATE;
    
    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    _leftPane = new JPanel(new BorderLayout());
    _setupRegionTree();
    
    this.add(_leftPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());

    _lastChangeTime = _frame.getLastChangeTime();
    
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    updateButtons();
    
    // Setup the color listeners.  Not clear what effect these listeners have given recent changes to the renderer.
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
  
//  /** Set the state to handle rapid changes. When a lot of changes are about to be made,
//    * this state should be set to postpone some actions until the changes are finished. */
//  public void startChanging() {
//    _changeState.switchStateTo(CHANGING_STATE);
//  }
  
//  /** Set the default state again. Not equipped to handle rapid changes. */
//  public void finishChanging() { _changeState.switchStateTo(DEFAULT_STATE); }
  
  /** Update the tree. This method adds significant overhead to displaying RegionsTreePanels.  We need to make it more
    * efficient. */
  public boolean requestFocusInWindow() {
    assert EventQueue.isDispatchThread();
    _updatePanel();
    return super.requestFocusInWindow();
  }

  /** Updates the tabbed panel if the time delay threshold has been exceeeded and no such update is already pending. */
  private void _updatePanel() {
    synchronized(_updateLock) { 
      if (_updatePending || _lastChangeTime == _frame.getLastChangeTime()) return; 
    }
    Thread updater = new Thread(new Runnable() {
      public void run() {
        try { _updateLock.wait(UPDATE_DELAY); }
        catch(InterruptedException e) { /* fall through */ }
        EventQueue.invokeLater(new Runnable() { 
          public void run() {
            updatePanel();  // resets _tabUpdatePending and _lastChangeTime
            updateButtons();
          }
        });
      }
    });
    updater.start();
  }
  
  protected void traversePanel() {
    Enumeration docNodes = _rootNode.children();
    while (docNodes.hasMoreElements()) {
      DefaultMutableTreeNode docNode = (DefaultMutableTreeNode) docNodes.nextElement();          
      // Find the correct start offset node for this region
      Enumeration regionNodes = docNode.children();
      while (regionNodes.hasMoreElements()) {
        DefaultMutableTreeNode regionNode = (DefaultMutableTreeNode) regionNodes.nextElement();
        _regTreeModel.reload(regionNode);
      }
      _regTreeModel.reload(docNode);  // file name may have changed
    }
  }
  
  /** Forces this panel to be completely updated. */
  protected void updatePanel() {

    synchronized(_updateLock) { 
      _updatePending = false; 
      _lastChangeTime = _frame.getLastChangeTime();
    }
//    traversePanel();
//    _regTreeModel.reload();
    revalidate();
    repaint();
//    expandTree();
//    repaint();
  }
  
  /** Forces the panel to be updated and requests focus in this panel. */
  protected boolean _requestFocusInWindow() {
    updatePanel();
    updateButtons();
    return super.requestFocusInWindow();
  }
  
  /** Creates the region tree. */
  private void _setupRegionTree() {
    _rootNode = new DefaultMutableTreeNode(_title);
    _regTreeModel = new DefaultTreeModel(_rootNode);
    _regTree = new RegionTree(_regTreeModel);
    _regTree.setEditable(false);
    _regTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    _regTree.setShowsRootHandles(true);
    _regTree.setRootVisible(false);
    _regTree.putClientProperty("JTree.lineStyle", "Angled");
    _regTree.setScrollsOnExpand(true);
    _regTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) { updateButtons(); }
    });
    _regTree.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) { performDefaultAction(); } } 
    });
    
    // Region tree cell renderer
    dtcr = new RegionRenderer();
    dtcr.setOpaque(false);
//    _setColors(dtcr);
    _regTree.setCellRenderer(dtcr);
    
    _leftPane.add(new JScrollPane(_regTree));
    
    _initPopup();
    
    ToolTipManager.sharedInstance().registerComponent(_regTree);
  }
  
  /** Update button state and text. _updateButtons should be overridden if additional buttons are added besides "Go To",
    * "Remove" and "Remove All". 
    */
  protected void updateButtons() { _updateButtons(); }
  
  protected void _updateButtons() { }
  
  /** Expand all tree nodes. */
  public void expandAll() {
    TreeNode root = (TreeNode)_regTree.getModel().getRoot();
    
    // Traverse tree from root
    expandRecursive(_regTree, new TreePath(root), true);
  }

  /** Collapse all tree nodes. */
  public void collapseAll() {
    TreeNode root = (TreeNode)_regTree.getModel().getRoot();
    
    // Traverse tree from root
    expandRecursive(_regTree, new TreePath(root), false);
  }
  
  private void expandRecursive(JTree tree, TreePath parent, boolean expand) {
    // Traverse children
    TreeNode node = (TreeNode)parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration e=node.children(); e.hasMoreElements(); ) {
        TreeNode n = (TreeNode)e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandRecursive(tree, path, expand);
      }
    }
    
    // Expansion or collapse must be done bottom-up
    if (expand) {
      tree.expandPath(parent);
    } else {
      tree.collapsePath(parent);
    }
  }
  
  /** Adds config color support to DefaultTreeCellEditor. */
  class RegionRenderer extends DefaultTreeCellRenderer {
    
    /* The following methods were commented out to minimize changes to the DefaultCellRenderer to help support
     * "reverse video highlighting" of selected nodes in the Plastic L&F family.
     */
//    public void setBackground(Color c) { setBackgroundNonSelectionColor(c); }
//    
//    public void setForeground(Color c) { setTextNonSelectionColor(c); }
//    
//    private RegionRenderer() {
//      this.setTextSelectionColor(Color.black);
//      setLeafIcon(null);
//      setOpenIcon(null);
//      setClosedIcon(null);
//    }
    
    /** Overrides the default renderer component to use proper coloring. */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, 
                                                  boolean leaf, int row, boolean hasFocus) {
      /*Component renderer = */ super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, leaf, row, hasFocus);

      // The following line was commented out as part of minimizing the changes to DefaultCellRenderer
//      if (renderer instanceof JComponent) { ((JComponent) renderer).setOpaque(false); }
      
//      _setColors(this);
      
      // set tooltip
      String tooltip = null;
      if (DrJava.getConfig().getSetting(OptionConstants.SHOW_CODE_PREVIEW_POPUPS).booleanValue()) {
        if (leaf) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
          Object o = node.getUserObject();
          
          if (node.getUserObject() instanceof RegionTreeUserObj) {
            @SuppressWarnings("unchecked")
            RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) o;
            R r = userObject.region();
            
            OpenDefinitionsDocument doc = r.getDocument();
            try {
              int lnr = doc.getLineOfOffset(r.getStartOffset()) + 1;
              int startOffset = doc._getOffset(lnr - 3);
              if (startOffset < 0) { startOffset = 0; }
              int endOffset = doc._getOffset(lnr + 3);
              if (endOffset < 0) { endOffset = doc.getLength() - 1; }
              
              // convert to HTML (i.e. < to &lt; and > to &gt; and newlines to <br>)
              String s = doc.getText(startOffset, endOffset - startOffset);
              
              // this highlights the actual region in red
              int rStart = r.getStartOffset() - startOffset;
              if (rStart < 0) { rStart = 0; }
              int rEnd = r.getEndOffset() - startOffset;
              if (rEnd > s.length()) { rEnd = s.length(); }
              if ((rStart <= s.length()) && (rEnd >= rStart)) {
                String t1 = StringOps.encodeHTML(s.substring(0, rStart));
                String t2 = StringOps.encodeHTML(s.substring(rStart,rEnd));
                String t3 = StringOps.encodeHTML(s.substring(rEnd));
                s = t1 + "<font color=#ff0000>" + t2 + "</font>" + t3;
              }
              else {
                s = StringOps.encodeHTML(s);
              }
              tooltip = "<html><pre>"+s+"</pre></html>";
            }
            catch(javax.swing.text.BadLocationException ble) { tooltip = null; /* just don't give a tool tip */ }
            setText(node.getUserObject().toString());
            setIcon(null);
//            renderer = this;
          }
        }
      }
      setToolTipText(tooltip);
      return /* renderer */ this;
    }
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() { }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {  return new JComponent[0];  }
  
  /** Creates the buttons for controlling the regions. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    JComponent[] buts = makeButtons();
    
    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);    
    for (JComponent b: buts) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    
    for (JComponent b: buts) { gbLayout.setConstraints(b, c); }
    
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
    if (acts != null) {
      for (AbstractAction a: acts) {
        _regionPopupMenu.add(a);
      }
      _regTree.addMouseListener(new RegionMouseAdapter());
    }
  }
  
  /** Gets the currently selected regions in the region tree, or an empty array if no regions are selected.
    * @return list of selected regions in the tree
    */
  protected ArrayList<R> getSelectedRegions() {
    ArrayList<R> regs = new ArrayList<R>();
    TreePath[] paths = _regTree.getSelectionPaths();  // Why not use getMin/MaxSelectionRow
    if (paths != null) {
      for (TreePath path: paths) {
        if (path != null && path.getPathCount() == 3) {
          DefaultMutableTreeNode lineNode = (DefaultMutableTreeNode)path.getLastPathComponent();
          @SuppressWarnings("unchecked") 
          R r = ((RegionTreeUserObj<R>) lineNode.getUserObject()).region();
          regs.add(r);
        }
      }
    }
    return regs;
  }
  
  /** Go to region. */
  protected void goToRegion() {
    ArrayList<R> r = getSelectedRegions();
    if (r.size() == 1) _frame.scrollToDocumentAndOffset(r.get(0).getDocument(), r.get(0).getStartOffset(), false);
  }
  
  /** Add a region to the tree. Must be executed in event thread.
    * @param r the region
    */
  public void addRegion(final R r) {
    try {
//    System.err.println("Adding region '" + r + "'");
    DefaultMutableTreeNode docNode;
    OpenDefinitionsDocument doc = r.getDocument();
    
    if (doc == _cachedDoc) docNode = _cachedDocNode;
    else {
      docNode = _docToTreeNode.get(doc);
      if (docNode == null) {
        // No matching document node was found, so create one
        docNode = new DefaultMutableTreeNode(doc.getRawFile());
        _regTreeModel.insertNodeInto(docNode, _rootNode, _rootNode.getChildCount());
        // Create link from doc to docNode
        _docToTreeNode.put(doc, docNode);
        _cachedDoc = doc;
        _cachedDocNode = docNode;
        _cachedStartOffset = -1;  // a sentinel value guaranteed to be less than r.getStartOffset()
        _cachedRegionIndex = -1;  // The next region in this document will have index 0
      }
    }
    
    if (doc == _cachedDoc & r.getStartOffset() >= _cachedStartOffset) { // insert new region after previous insert
      _cachedRegionIndex++;
      _cachedStartOffset = r.getStartOffset();
      insertNewRegionNode(r, docNode, _cachedRegionIndex);
    }
    else {
      @SuppressWarnings("unchecked")
      Enumeration<DefaultMutableTreeNode> regionNodes = (Enumeration<DefaultMutableTreeNode>) docNode.children();
      
      // Create a new region node in this document node list, where regions are sorted by start offset.
      int startOffset = r.getStartOffset();
      for (int index = 0; true ; index++) {  // infinite loop incrementing index on each iteration
        
        if (! regionNodes.hasMoreElements()) { // exhausted all elements; insert new region node at end
//          System.err.println("inserting " + r + " at end, unaided by caching");
          insertNewRegionNode(r, docNode, index);
          _cachedDoc = doc;
          _cachedDocNode = docNode;
          _cachedRegionIndex = index;
          _cachedStartOffset = startOffset;
          break;
        }
        DefaultMutableTreeNode node = regionNodes.nextElement();
        
        @SuppressWarnings("unchecked")
        RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) node.getUserObject();
        R nodeRegion = userObject.region();
        int nodeOffset = nodeRegion.getStartOffset();
        
//        if (nodeOffset == startOffset) {
//          // region with same start offset already exists
//          if (nodeRegion.getEndOffset() == r.getEndOffset()) {
//            // silently suppress inserting region; can this happen?  Caller should suppress it.
//            _changeState.scrollPathToVisible(new TreePath(node));
//            _changeState.setLastAdded(node);
//            break;
//          }
//          else { // new region is distinct from nodeRegion
//            insertNewRegionNode(r, docNode, index);
//            break;
//          }
//        }
//        else 
        if (nodeOffset >= startOffset) {
          insertNewRegionNode(r, docNode, index);
          _cachedDoc = null;  // insertion was not at the end of the region list for doc
          break;
        }
      }
    }
    _changeState.updateButtons();
  }
  catch(Exception e) { DrJavaErrorHandler.record(e); throw new UnexpectedException(e); }
  }

  private void insertNewRegionNode(R r, DefaultMutableTreeNode docNode, int pos) {
//    System.err.println("insertNewRegionNode(" + r + ", " + docNode + ", " + pos + ")");
    DefaultMutableTreeNode newRegionNode = new DefaultMutableTreeNode(makeRegionTreeUserObj(r));
    
    _regTreeModel.insertNodeInto(newRegionNode, docNode, pos);
    
    // Create link from region r to newRegionNode
    _regionToTreeNode.put(r, newRegionNode);
    
    // Make sure this node is visible
    _changeState.scrollPathToVisible(new TreePath(newRegionNode.getPath()));
    _changeState.setLastAdded(newRegionNode);
  }       
  
  public void expandTree() {
    int ct = _regTree.getRowCount();
    for (int i = 0; i < ct; i++) _regTree.expandRow(i);
  }
    
  /** Remove a region from this panel. Must be executed in event thread.
    * @param r the region
    */
  public void removeRegion(final R r) {
    assert EventQueue.isDispatchThread();
    _changeState.setLastAdded(null);
    DefaultMutableTreeNode regionNode = _regionToTreeNode.get(r);
    if (regionNode == null) throw new UnexpectedException("Region node for region " + r + " is null");
//    _regionManager.removeRegion(r);
    _regionToTreeNode.remove(r);
    
//    DefaultMutableTreeNode docNode = _regionManager.getTreeNode(doc);
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) regionNode.getParent();  // TreeNode for document
    _regTreeModel.removeNodeFromParent(regionNode);
    
    // check for empty subtree for this document (rooted at parent)
    if (parent.getChildCount() == 0) {
      // this document has no more regions, remove it
      OpenDefinitionsDocument doc = r.getDocument();  // r must not have bee disposed above
      _docToTreeNode.remove(doc);
      _regTreeModel.removeNodeFromParent(parent);
    }
//    expandTree();
    _changeState.updateButtons();
  }
  
  /** Remove all regions for the given document from the tree. Must be executed in event thread. */
  public void removeRegions(final OpenDefinitionsDocument odd) {
    assert EventQueue.isDispatchThread();
    _changeState.setLastAdded(null);
    
    DefaultMutableTreeNode docNode = _docToTreeNode.get(odd);
    
    // Find the document node for this region

    while(docNode.getChildCount() > 0) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)docNode.getFirstChild();
      _regTreeModel.removeNodeFromParent(node);
    }
    _regTreeModel.removeNodeFromParent(docNode);
    _regionManager.removeRegions(odd);
    _changeState.updateButtons();
  }
  
  /** Mouse adapter for the region tree. */
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
        performDefaultAction();
      }
    }
  }
  
  /** Factory method to create user objects put in the tree.
    * If subclasses extend RegionTreeUserObj, they need to override this method. */
  protected RegionTreeUserObj<R> makeRegionTreeUserObj(R r) { return new RegionTreeUserObj<R>(r); }
  
  protected class RegionTree extends JTree {
    
    public RegionTree(DefaultTreeModel s) { super(s); }  // narrows type of construction argument
    
    public void setForeground(Color c) {
      super.setForeground(c);
      if (dtcr != null) dtcr.setTextNonSelectionColor(c);
    }
    
    public void setBackground(Color c) {
      super.setBackground(c);
      if (RegionsTreePanel.this != null && dtcr != null) dtcr.setBackgroundNonSelectionColor(c);
    }
  }
  
  /** Class that is embedded in each leaf node. The toString() method determines what's displayed in the tree. */
  protected static class RegionTreeUserObj<R extends IDocumentRegion> {
    protected R _region;
    public int lineNumber() { return _region.getDocument().getLineOfOffset(_region.getStartOffset()) + 1; }
    public R region() { return _region; }
    public RegionTreeUserObj(R r) { _region = r; }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
        sb.append(lineNumber());
        try {
          sb.append(": ");
          int length = Math.min(120, _region.getEndOffset()-_region.getStartOffset());
          sb.append(_region.getDocument().getText(_region.getStartOffset(), length).trim());
        } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      return sb.toString();
    }
  }
  
  /** State pattern for improving performance during rapid updates. */
  protected interface IChangeState {
    public void scrollPathToVisible(TreePath tp);
    public void updateButtons();
    public void setLastAdded(DefaultMutableTreeNode node);
    public void switchStateTo(IChangeState newState);
  }
  
  /** Normal state, GUI changes not delayed. */
  protected class DefaultState implements IChangeState {
    public void scrollPathToVisible(TreePath tp) {
      _regTree.scrollPathToVisible(tp);
    }
    public void updateButtons() {
      RegionsTreePanel.this.updateButtons();
    }
    public void setLastAdded(DefaultMutableTreeNode node) { }
    public void switchStateTo(IChangeState newState) {
      _changeState = newState;
    }
    protected DefaultState() { }
  }

  /** Rapid changing state, GUI changes are delayed until the state is switched back to DefaultState. */
  protected class ChangingState implements IChangeState {
    private DefaultMutableTreeNode _lastAdded = null;
    public void scrollPathToVisible(TreePath tp) { }
    public void updateButtons() { }
    public void setLastAdded(DefaultMutableTreeNode node) {
      _lastAdded = node;
    }
    public void switchStateTo(IChangeState newState) {
      updateButtons();
      if (_lastAdded!=null) {
        TreePath pathToNewRegion = new TreePath(_lastAdded.getPath());
        _regTree.scrollPathToVisible(pathToNewRegion);
      }
      expandAll();
      _regTree.revalidate();
      _changeState = newState;
    }
  }
}
