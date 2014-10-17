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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.NoSuchElementException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.OrderedDocumentRegion;
import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.RightClickMouseAdapter;

import edu.rice.cs.plt.lambda.Thunk;

/** Panel for displaying regions in a tree sorted by class name and line number.  Only accessed from event thread.
  * The volatile declarations are included because the event-thread-only invariant is not enforced. TODO: fix this.
  * @version $Id: RegionsTreePanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class RegionsTreePanel<R extends OrderedDocumentRegion> extends TabbedPanel {
  protected volatile JPanel _leftPane;
  
  private volatile DefaultMutableTreeNode _rootNode;
  private volatile DefaultTreeModel _regTreeModel;
  private volatile JTree _regTree;
  
  private volatile String _title;
  private volatile RegionManager<R> _regionManager;
  private volatile JPopupMenu _regionPopupMenu;
  private final SingleDisplayModel _model;
  protected final MainFrame _frame;
  protected volatile JPanel _buttonPanel;
  
  protected volatile DefaultTreeCellRenderer dtcr;
  
  protected volatile boolean _hasNextPrevButtons = true;
  
  /** button to go to the previous region (or null if _hasNextPrevButtons==false). */
  protected volatile JButton _prevButton;
  /** button to go to the next region (or null if _hasNextPrevButtons==false). */
  protected volatile JButton _nextButton;
  /** the region that was last selected (may be null). */ 
  protected volatile R _lastSelectedRegion = null;
  
  /* _ */
  
//  /** Cached values from last region insertion. _cachedDoc is non-null iff the last added region occurred at the end of
//    * the list of regions for its document. If _cachedDoc is null, the other cached values are invalid. */
//  protected OpenDefinitionsDocument _cachedDoc = null;
//  protected DefaultMutableTreeNode _cachedDocNode = null;
//  protected int _cachedRegionIndex = 0;
//  protected int _cachedStartOffset = 0;
  
  /** State pattern to improve performance when rapid changes are made. */
  protected final IChangeState DEFAULT_STATE = new DefaultState();
//  protected final IChangeState CHANGING_STATE = new ChangingState();
  protected volatile IChangeState _changeState = DEFAULT_STATE;
  
  /** A table mapping each document entered in this panel to its corresponding MutableTreeNode in _regTreeModel. */
  protected volatile HashMap<OpenDefinitionsDocument, DefaultMutableTreeNode> _docToTreeNode = 
    new HashMap<OpenDefinitionsDocument, DefaultMutableTreeNode>();
  
  /** A table mapping each region entered in this panel to its corresponding MutableTreeNode in _regTreeModel. */
  protected volatile IdentityHashMap<R, DefaultMutableTreeNode> _regionToTreeNode = 
    new IdentityHashMap<R, DefaultMutableTreeNode>();
  
  /** State variable used to control the granular updating of the tabbed panel. */
//  private volatile long _lastChangeTime;
//  private volatile Object _updateLock = new Object();  // commented out when update delay in this class was disabled
//  private volatile boolean _updatePending = false;
//  public static final long UPDATE_DELAY = 2000L;  // update delay threshold in milliseconds
  
  /** Constructs a new panel to display regions in a tree. This is swing view class and hence should only be accessed 
    * from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    * @param regionManager the region manager associated with this panel
    */
  public RegionsTreePanel(MainFrame frame, String title, RegionManager<R> regionManager) {
    this(frame, title, regionManager, true);
  }
  
  /** Constructs a new panel to display regions in a tree. This is swing view class and hence should only be accessed 
    * from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    * @param regionManager the region manager associated with this panel
    * @param hasNextPrevButtons whether this panel should have next/previous buttons
    */
  public RegionsTreePanel(MainFrame frame, String title, RegionManager<R> regionManager,
                          boolean hasNextPrevButtons) {
    super(frame, title);
    _title = title;
    _regionManager = regionManager;
    _hasNextPrevButtons = hasNextPrevButtons;
    setLayout(new BorderLayout());
    
    _lastSelectedRegion = null;
    
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

//    _lastChangeTime = _frame.getLastChangeTime();
    
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
  
  /** Close the panel and update buttons. */
  @Override
  protected void _close() {
//    System.err.println("RegionsTreePanel.close() called");
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
  
  /** Update the JTree. */
  public boolean requestFocusInWindow() {
    assert EventQueue.isDispatchThread();
    updatePanel();  // formerly _updatePanel()
    return super.requestFocusInWindow();
  }

//  /** Updates the tabbed panel if the time delay threshold has been exceeeded and no such update is already pending. */
//  private void _updatePanel() {
//    synchronized(_updateLock) { 
//      if (_updatePending || _lastChangeTime == _frame.getLastChangeTime()) return; 
//    }
//    Thread updater = new Thread(new Runnable() {
//      public void run() {
//        try { _updateLock.wait(UPDATE_DELAY); }
//        catch(InterruptedException e) { /* fall through */ }
//        EventQueue.invokeLater(new Runnable() { 
//          public void run() {
//            updatePanel();  // resets _tabUpdatePending and _lastChangeTime
//            updateButtons();
//          }
//        });
//      }
//    });
//    updater.start();
//  }
  
  // Not currently used.
//  protected void traversePanel() {
//    Enumeration docNodes = _rootNode.children();
//    while (docNodes.hasMoreElements()) {
//      DefaultMutableTreeNode docNode = (DefaultMutableTreeNode) docNodes.nextElement();          
//      // Find the correct start offset node for this region
//      Enumeration regionNodes = docNode.children();
//      while (regionNodes.hasMoreElements()) {
//        DefaultMutableTreeNode regionNode = (DefaultMutableTreeNode) regionNodes.nextElement();
//        _regTreeModel.reload(regionNode);
//      }
//      _regTreeModel.reload(docNode);  // file name may have changed
//    }
//  }
  
  /** Forces this panel to be completely updated. */
  protected void updatePanel() {
    // The following lines were commented out when update delays in this class were disabled
//    synchronized(_updateLock) { 
//      _updatePending = false; 
//      _lastChangeTime = _frame.getLastChangeTime();
//    }
//    traversePanel();
    _regTreeModel.reload();
//    revalidate(); //
    expandTree();
    repaint();
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
  
  public DefaultMutableTreeNode getRootNode() { return _rootNode; }
  public JTree getRegTree() { return _regTree; }
  public DefaultTreeModel getRegTreeModel() { return _regTreeModel; }
  public String getTitle() { return _title; }
  public RegionManager<R> getRegionManager() { return _regionManager; }
  public SingleDisplayModel getGlobalModel() { return _model; }
  
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
  
  /** Remove the selected regions. */
  protected void _remove() {   
    int[] rows = _regTree.getSelectionRows();
    // bugfix for 3040733: "If nothing is selected null or an empty array will be returned,
    // based on the TreeSelectionModel implementation."
    // Still does not allow right-click selection+popup menu, at least on Linux. Works on the Mac.
    if (rows == null) { rows = new int[0]; }
//    System.err.println("_remove() called with rows " + StringOps.toString(rows));
    int len = rows.length;
    int row = (len > 0) ? rows[0] : 0;
    _frame.removeCurrentLocationHighlight();
    for (R r: getSelectedRegions()) {
      _regionManager.removeRegion(r); // removes r from region manager and the panel node for r from the tree model
    }
    int rowCount = _regTree.getRowCount();
    if (rowCount == 0) return; // removed last region from panel
    
//    System.err.println("rowCount = " + rowCount);
    if (row >= rowCount) row = Math.max(0, rowCount - 1);  // ensure row is in range
    _requestFocusInWindow();
    _regTree.scrollRowToVisible(row);
    
    //Set selection row; must be done after preceding too lines for selection highlight to persist
    _regTree.setSelectionRow(row);
//    System.err.println("Setting selection row = " + row);
    // Ensure that a leaf (region node) is selected  (Is there a simpler way to determine if selected node is a leaf?)
    if (_regTree.getLeadSelectionPath().getPathCount() < 2) _regTree.setSelectionRow(row + 1);
//    System.err.println("Resetting selection row = " + (row + 1));
  }
  
  private void expandRecursive(JTree tree, TreePath parent, boolean expand) {
    // Traverse children
    TreeNode node = (TreeNode)parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration<?> e=node.children(); e.hasMoreElements(); ) {
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
      
      // set tooltip as thunk
      Thunk<String> tooltip = null;
      if (DrJava.getConfig().getSetting(OptionConstants.SHOW_CODE_PREVIEW_POPUPS).booleanValue()) {
        if (leaf) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
          final Object o = node.getUserObject();
          
          if (o instanceof RegionTreeUserObj) {
            tooltip = new Thunk<String>() {
              public String value() {
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
                  return "<html><pre>" + s + "</pre></html>";
                }
                catch(javax.swing.text.BadLocationException ble) { return ""; /* just display an empty tool tip*/ }
              }
            };
            setText(node.getUserObject().toString());
            setIcon(null);
//            renderer = this;
          }
        }
      }
      setToolTipText(tooltip);
      return /* renderer */ this;
    }
    
    /** Alternative version of setToolTipText that accepts a thunk. */
    public void setToolTipText(Thunk<String> text) {
      Object oldText = getClientProperty(TOOL_TIP_TEXT_KEY);
      putClientProperty(TOOL_TIP_TEXT_KEY, text);
      ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
      if (text != null) {
        if (oldText == null) {
          toolTipManager.registerComponent(this);
        }
      } else {
        toolTipManager.unregisterComponent(this);
      }
    }
    
    /** Overridden version of getToolTipText that evaluates a thunk if necessary. */
    @SuppressWarnings("unchecked")
    public String getToolTipText() {
      Object o = getClientProperty(TOOL_TIP_TEXT_KEY);
      if (o instanceof Thunk) {
        String s = ((Thunk<String>)o).value();
        putClientProperty(TOOL_TIP_TEXT_KEY, s);
        return s;
      }
      return (String)o;
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
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    
    if (_hasNextPrevButtons) {
      _prevButton = new JButton(new AbstractAction("Previous") {
        public void actionPerformed(ActionEvent ae) {
          goToPreviousRegion();
        }
      });
      _nextButton = new JButton(new AbstractAction("Next") {
        public void actionPerformed(ActionEvent ae) {
          goToNextRegion();
        }
      });
      mainButtons.add(_prevButton);
      gbLayout.setConstraints(_prevButton, c);
      mainButtons.add(_nextButton);
      gbLayout.setConstraints(_nextButton, c);
      updateNextPreviousRegionButtons(null);
    }
    for (JComponent b: buts) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
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
  protected AbstractAction[] makePopupMenuActions() { return null; }
  
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
  
  /** Gets the tree node for the given document. */
  DefaultMutableTreeNode getNode(OpenDefinitionsDocument doc) { return _docToTreeNode.get(doc); }
  
    /** Gets the tree node for the given region. */
  DefaultMutableTreeNode getNode(R region) { return _regionToTreeNode.get(region); }
  
  /** Gets the currently selected regions in the region tree, or an empty array if no regions are selected.
    * @return list of selected regions in the tree
    * TODO: change this code to use getMinSelectionRow and getMaxSelectionRow
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
    if (r.size() == 1) {
      updateNextPreviousRegionButtons(r.get(0));
      _frame.scrollToDocumentAndOffset(_lastSelectedRegion.getDocument(), _lastSelectedRegion.getStartOffset(), false);
    }
  }
  
  /** Update the enabled/disabled state of the next/previous region buttons.
    * Doesn't change _lastSelectedRegion.
    * Safe to call even if _hasNextPrevButtons==false. */
  protected void updateNextPreviousRegionButtons() {
    updateNextPreviousRegionButtons(_lastSelectedRegion);
  }
  
  /** Update the enabled/disabled state of the next/previous region buttons.
    * Safe to call even if _hasNextPrevButtons==false.
    * @param lastSelectedRegion new region selected */
  protected void updateNextPreviousRegionButtons(R lastSelectedRegion) {
    _lastSelectedRegion = lastSelectedRegion;
    if (_hasNextPrevButtons) {
      int count = _regionManager.getRegionCount();
      if (count>0) {
        if (_lastSelectedRegion==null) {
          // nothing selected, but we have at least one region
          _prevButton.setEnabled(false); // no "prev"
          _nextButton.setEnabled(true); // "next" will go to the first region
        }
        else {
          // a region was selected
          _prevButton.setEnabled(getPrevRegionInTree(_lastSelectedRegion)!=null);
          _nextButton.setEnabled(getNextRegionInTree(_lastSelectedRegion)!=null);
        }
      }
    }
  }

  /** Go to previous region. Must be run in event thread. */
  public void goToPreviousRegion() {
    assert EventQueue.isDispatchThread();
    
    int count = _regionManager.getRegionCount();
    if (count>0) {
      R newRegion = null; // initially not set
      if (_lastSelectedRegion!=null) {
        // there are elements and something was selected
        newRegion = getPrevRegionInTree(_lastSelectedRegion);
      }
      else {
        // nothing selected, go to first region
        newRegion = _regionManager.getRegions().get(0);
      }
      if (newRegion!=null) {
        // a new region was found, select it
        updateNextPreviousRegionButtons(newRegion);
        selectRegion(_lastSelectedRegion);
        _frame.scrollToDocumentAndOffset(_lastSelectedRegion.getDocument(),
                                         _lastSelectedRegion.getStartOffset(), false);
      }
    }
  }
  
  /** Return the region preceding r in the tree, or null if there isn't one. */
  protected R getPrevRegionInTree(R r) {
    DefaultMutableTreeNode regionNode = _regionToTreeNode.get(r);
    if (regionNode != null) {
      DefaultMutableTreeNode prevSibling = regionNode.getPreviousSibling();
      if (prevSibling!=null) {
        // there is a previous sibling, go there
        // root--+
        //       +--doc1--+
        //       |        +---foo
        //       +--doc2--+
        //                +---prevSibling
        //                +---_lastSelectedRegion
        @SuppressWarnings("unchecked")
        RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) prevSibling.getUserObject();
        return userObject.region();
      }
      else {
        // no previous sibling, go to the parent's previous sibling's last child (olderCousin)
        // root--+
        //       +--doc1--+
        //       |        +---olderCousin
        //       +--doc2--+
        //                +---_lastSelectedRegion
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)regionNode.getParent();
        if (parent!=null) {
          DefaultMutableTreeNode parentsPrevSibling = parent.getPreviousSibling();
          if (parentsPrevSibling!=null) {
            try {
              DefaultMutableTreeNode olderCousin = (DefaultMutableTreeNode)parentsPrevSibling.getLastChild();
              if (olderCousin!=null) {
                @SuppressWarnings("unchecked")
                RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) olderCousin.getUserObject();
                return userObject.region();
              }
            }
            catch(NoSuchElementException nsee) {
              throw new UnexpectedException(nsee, "Document node without children, shouldn't exist");
            }
          }
        }
      }
    }
    return null;
  }

  /** Go to next region. */
  public void goToNextRegion() {
    int count = _regionManager.getRegionCount();
    if (count > 0) {
      R newRegion = null; // initially not set
      if (_lastSelectedRegion != null) {
        // there are elements and something was selected
        newRegion = getNextRegionInTree(_lastSelectedRegion);
      }
      else {
        // nothing selected, go to first region
        newRegion = _regionManager.getRegions().get(0);
      }
      if (newRegion != null) {
        // a new region was found, select it
        updateNextPreviousRegionButtons(newRegion);
        selectRegion(_lastSelectedRegion);
        _frame.scrollToDocumentAndOffset(_lastSelectedRegion.getDocument(),
                                         _lastSelectedRegion.getStartOffset(), false);
      }
    }
  }
  
  /** Return the region following r in the tree, or null if there isn't one. */
  protected R getNextRegionInTree(R r) {
    DefaultMutableTreeNode regionNode = _regionToTreeNode.get(r);
    if (regionNode != null) {
      DefaultMutableTreeNode nextSibling = regionNode.getNextSibling();
      if (nextSibling!=null) {
        // there is a previous sibling, go there
        // root--+
        //       +--doc1--+
        //       |        +---_lastSelectedRegion
        //       |        +---nextSibling
        //       +--doc2--+
        //                +---foo
        @SuppressWarnings("unchecked")
        RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) nextSibling.getUserObject();
        return userObject.region();
      }
      else {
        // no next sibling, go to the parent's next sibling's first child (youngerCousin)
        // root--+
        //       +--doc1--+
        //       |        +---_lastSelectedRegion
        //       +--doc2--+
        //                +---youngerCousin
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)regionNode.getParent();
        if (parent!=null) {
          DefaultMutableTreeNode parentsNextSibling = parent.getNextSibling();
          if (parentsNextSibling!=null) {
            try {
              DefaultMutableTreeNode youngerCousin = (DefaultMutableTreeNode)parentsNextSibling.getFirstChild();
              if (youngerCousin!=null) {
                @SuppressWarnings("unchecked")
                RegionTreeUserObj<R> userObject = (RegionTreeUserObj<R>) youngerCousin.getUserObject();
                return userObject.region();
              }
            }
            catch(NoSuchElementException nsee) {
              throw new UnexpectedException(nsee, "Document node without children, shouldn't exist");
            }
          }
        }
      }
    }
    return null;
  }
  
  /** Add a region to the tree. Must be executed in event thread.
    * @param r the region
    */
  public void addRegion(final R r) {
    try {
//    System.err.println("Adding region '" + r + "'");
      DefaultMutableTreeNode docNode;
      OpenDefinitionsDocument doc = r.getDocument();
      
//    if (doc == _cachedDoc) docNode = _cachedDocNode;
//    else {
      docNode = _docToTreeNode.get(doc);
      if (docNode == null) {
        // No matching document node was found, so create one
        docNode = new DefaultMutableTreeNode(doc.getRawFile());
        _regTreeModel.insertNodeInto(docNode, _rootNode, _rootNode.getChildCount());
        // Create link from doc to docNode
        _docToTreeNode.put(doc, docNode);
//        _cachedDoc = doc;
//        _cachedDocNode = docNode;
//        _cachedStartOffset = -1;  // a sentinel value guaranteed to be less than r.getStartOffset()
//        _cachedRegionIndex = -1;  // The next region in this document will have index 0
      }
//    }
      
//    if (doc == _cachedDoc & r.getStartOffset() >= _cachedStartOffset) { // insert new region after previous insert
//      _cachedRegionIndex++;
//      _cachedStartOffset = r.getStartOffset();
//      insertNewRegionNode(r, docNode, _cachedRegionIndex);
//    }
//    else {
      @SuppressWarnings("unchecked")
      Enumeration<DefaultMutableTreeNode> regionNodes = docNode.children();
      
      // Create a new region node in this document node list, where regions are sorted by start offset.
      int startOffset = r.getStartOffset();
      for (int index = 0; true ; index++) {  // infinite loop incrementing index on each iteration
        
        if (! regionNodes.hasMoreElements()) { // exhausted all elements; insert new region node at end
//          System.err.println("inserting " + r + " at end, unaided by caching");
          insertNewRegionNode(r, docNode, index);
//          _cachedDoc = doc;
//          _cachedDocNode = docNode;
//          _cachedRegionIndex = index;
//          _cachedStartOffset = startOffset;
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
//          _cachedDoc = null;  // insertion was not at the end of the region list for doc
          break;
        }
      }
//    }
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
  
  /** Expands all nodes in a two-level tree. */
  public void expandTree() {
    int ct = _regTree.getRowCount();
    for (int i = ct - 1; i >= 0; i--) _regTree.expandRow(i);
  }
    
  /** Remove a region from this panel. Must be executed in event thread.
    * @param r the region
    */
  public void removeRegion(final R r) {
//    System.err.println("RegionsTreePanel.removeRegion(" + r + ") called");
    assert EventQueue.isDispatchThread();
    _changeState.setLastAdded(null);
    
    if ((_lastSelectedRegion!=null) && (_lastSelectedRegion.equals(r))) {
      // we need to change the _lastSelectedRegion
      R newLast = getPrevRegionInTree(_lastSelectedRegion);
      if (newLast==null) newLast = getNextRegionInTree(_lastSelectedRegion);
      _lastSelectedRegion = newLast;
      if (_lastSelectedRegion!=null) {
        selectRegion(_lastSelectedRegion);
      }
    }
    
    DefaultMutableTreeNode regionNode = _regionToTreeNode.get(r);
//    if (regionNode == null) throw new UnexpectedException("Region node for region " + r + " is null");  // should not happen but it does
    if (regionNode != null) {

//    _regionManager.removeRegion(r);
      _regionToTreeNode.remove(r);
      
//    DefaultMutableTreeNode docNode = _regionManager.getTreeNode(doc);
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) regionNode.getParent();  // TreeNode for document
      _regTreeModel.removeNodeFromParent(regionNode);
//    System.err.println("panel region count in " + r.getDocument() + " = " + parent.getChildCount());
      // check for empty subtree for this document (rooted at parent)
      if (parent.getChildCount() == 0) {
        // this document has no more regions, remove it
        OpenDefinitionsDocument doc = r.getDocument();  // r must not have been disposed above
        _docToTreeNode.remove(doc);
        _regTreeModel.removeNodeFromParent(parent);
//      if (parent == _cachedDocNode) _cachedDoc = null;
      }
    }
//    expandTree();
    _changeState.updateButtons();
//    System.err.println("_regionManager.getDocuments() = " + _regionManager.getDocuments());
    closeIfEmpty();
  }
  
  /** Select a region in this panel. Must be executed in event thread.
    * @param r the region
    */
  protected void selectRegion(final R r) {
    assert EventQueue.isDispatchThread();
    DefaultMutableTreeNode regionNode = _regionToTreeNode.get(r);
    if (regionNode != null) {
      _regTree.setSelectionPath(new TreePath(regionNode.getPath()));
    }
  }
  
  /** Close the panel if the tree becomes empty. */
  protected void closeIfEmpty() {
    if (_regionManager.getDocuments().isEmpty()) _close(); // _regTreeModel.getChildCount(_regTreeModel.getRoot()) == 0
  }
  
  // Reloads regions between starting and endRegion inclusive.  Assumes startRegion, endRegion are in the same document.
  public void reload(R startRegion, R endRegion) {
    SortedSet<R> tail = _regionManager.getTailSet(startRegion);
    Iterator<R> iterator = tail.iterator();
    
    while (iterator.hasNext()) {
      R r = iterator.next();
      if (r.compareTo(endRegion) > 0) break; 
//      System.err.println("Reloading region '" + r.getString() + "'");
      _regTreeModel.reload(getNode(r));
    }
  }
  
//  /** Remove all regions for the given document from the tree. Must be executed in event thread. */
//  public void removeRegions(final OpenDefinitionsDocument odd) {
//    assert EventQueue.isDispatchThread();
//    _changeState.setLastAdded(null);
//    
//    DefaultMutableTreeNode docNode = _docToTreeNode.get(odd);
//    
//    // Find the document node for this region
//
//    while(docNode.getChildCount() > 0) {
//      DefaultMutableTreeNode node = (DefaultMutableTreeNode)docNode.getFirstChild();
//      _regTreeModel.removeNodeFromParent(node);
//    }
//    _regTreeModel.removeNodeFromParent(docNode);
////    if (docNode == _cachedDocNode) _cachedDoc = null;
//    _regionManager.removeRegions(odd);
//    _changeState.updateButtons();
//  }
  
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
  protected static class RegionTreeUserObj<R extends OrderedDocumentRegion> {
    protected volatile R _region;
    public int lineNumber() { return _region.getDocument().getLineOfOffset(_region.getStartOffset()) + 1; }
    public R region() { return _region; }
    public RegionTreeUserObj(R r) { _region = r; }

    // TODO: change 120 to a defined constand (must search for 119 as well as 120 in code)
    public String toString() {
      final StringBuilder sb = new StringBuilder(120);
      sb.append("<html>");
      sb.append(lineNumber());
      sb.append(": ");
      String text = _region.getString(); // limited to 124 chars (120 chars of text + " ...")  
      int len = text.length();
      if (text.lastIndexOf('\n') != len - 1) sb.append(StringOps.flatten(text));  // multiline label
      else sb.append(text);  
      sb.append("</html>");
//      System.err.println("Returning node label: " + sb.toString());
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
      RegionsTreePanel.this.updateNextPreviousRegionButtons();
    }
    public void setLastAdded(DefaultMutableTreeNode node) { }
    public void switchStateTo(IChangeState newState) {
      _changeState = newState;
    }
    protected DefaultState() { }
  }
}
