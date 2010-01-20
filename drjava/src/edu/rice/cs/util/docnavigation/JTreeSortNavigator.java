/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.docnavigation;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.*;
import java.io.File;
import java.awt.*;
import java.util.*;
import java.awt.dnd.*;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.plt.collect.OneToOneRelation;
import edu.rice.cs.plt.collect.IndexedOneToOneRelation;

import edu.rice.cs.drjava.DrJavaRoot;

public class JTreeSortNavigator<ItemT extends INavigatorItem> extends JTree 
  implements IDocumentNavigator<ItemT>, TreeSelectionListener, TreeExpansionListener,
  DropTargetListener {
  
  /** The model of the tree. */
  private final DefaultTreeModel _model;
  
  /** The currently selected item.  Updated by a listener. It is not volatile because all accessed are protected by 
    * explicit synchronization.
    */
  private volatile NodeData<ItemT> _current;
  
  /** Maps documents to tree nodes. */
  private final HashMap<ItemT, LeafNode<ItemT>> _doc2node = new HashMap<ItemT, LeafNode<ItemT>>();
  
  /** Maps path's to nodes and nodes to paths. */
  private final OneToOneRelation<String, InnerNode<?, ItemT>> _path2node =
    new IndexedOneToOneRelation<String, InnerNode<?, ItemT>>();
  
  /** The collection of INavigationListeners listening to this JListNavigator */
  private final ArrayList<INavigationListener<? super ItemT>> navListeners =
    new ArrayList<INavigationListener<? super ItemT>>();
  
  /** The renderer for this JTree. */
  private final CustomTreeCellRenderer _renderer;
  
  private volatile DisplayManager<? super ItemT> _displayManager;
  private volatile Icon _rootIcon;
  
  private java.util.List<GroupNode<ItemT>> _roots = new LinkedList<GroupNode<ItemT>>();
  
  /** Sets the foreground color of this JTree
    * @param c the color to set to
    */
  public void setForeground(Color c) {
    super.setForeground(c);
    if (_renderer != null) _renderer.setTextNonSelectionColor(c);
  }
  
  /** Sets the background color of this tree
    * @param c the color for the background 
    */
  public void setBackground(Color c) {
    super.setBackground(c);
    if (_renderer != null) _renderer.setBackgroundNonSelectionColor(c);
  }
  
  /** Standard constructor.
    * @param projRoot the path identifying the root node for the project
    */
  public JTreeSortNavigator(String projRoot) {
    
    super(new DefaultTreeModel(new RootNode<ItemT>(projRoot.substring(projRoot.lastIndexOf(File.separator) + 1))));
    
    addTreeSelectionListener(this);
    addTreeExpansionListener(this);
    
    _model = (DefaultTreeModel) getModel();
    _renderer = new CustomTreeCellRenderer();
    _renderer.setOpaque(false);
    setCellRenderer(_renderer);
//    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setRowHeight(18);
//    System.err.println(isEditable());
  }
  
  /** Alternate constructor specifying the display manager that provides icons for the navigator.
    * @param projRoot the path identifying the root node for the project
    * @param dm the display manager for the navigagtor
    */
  public JTreeSortNavigator(String projRoot, DisplayManager<? super ItemT> dm) {
    this(projRoot);
    _displayManager = dm;
  }
  
  /** Sets the display manager that is used to select icons for the leaves of the tree.
    * This does not apply to the inner nodes or the root.
    */
  public void setDisplayManager(DisplayManager<? super ItemT> manager) { _displayManager = manager; }
  
  /** Sets the icon to be displayed at the root of the tree */
  public void setRootIcon(Icon ico) { _rootIcon = ico; }
  
  /** @return an AWT component which interacts with this document navigator */
  public Container asContainer() { return this; }
  
  /** Adds an <code>IDocument</code> to this navigator. Should only executed from event thread.
    * @param doc the document to be added into this navigator.
    */
  public void addDocument(ItemT doc) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    addDocument(doc, "");
  }
  /** Adds an <code>INavigatorItem</code> to this navigator in the specified position. The behavior of this navigator
    * and the position associated with a path are left to the implementing class.  Only runs in event-handling thread.
    * @param doc the document to be added into this navigator.
    * @param path in navigator to parent directory for doc
    * @throws IllegalArgumentException if this navigator does not contain <code>relativeto</code> as tested by the
    *         <code>contains</code> method.
    */
  public void addDocument(ItemT doc, String path) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) { // lock for mutation
      
      /* Identify root matching doc if any */
      GroupNode<ItemT> root = null;
      
      for (GroupNode<ItemT> r: _roots) {
        if (r.getFilter().accept(doc)) {
          root = r;
          break;
        }
      }
      
      if (root == null) return;
      
      /* Embed path in matching root, creating folder nodes if necessary */
      StringTokenizer tok = new StringTokenizer(path, File.separator);
      //ArrayList<String> elements = new ArrayList<String>();
      final StringBuilder pathSoFarBuf = new StringBuilder();
      InnerNode<?, ItemT> lastNode = root;
      while (tok.hasMoreTokens()) {
        String element = tok.nextToken();
        pathSoFarBuf.append(element).append('/');
        String pathSoFar = pathSoFarBuf.toString();
        InnerNode<?, ItemT> thisNode;
        //System.out.println("pathsofar = " + pathSoFar);
        // if the node is not in the hashmap yet
        if (!_path2node.containsFirst(pathSoFar)) {
          // make a new node
          
          /* this inserts a folder node */
          thisNode = new FileNode<ItemT>(new File(pathSoFar));
          insertFolderSortedInto(thisNode, lastNode);
          this.expandPath(new TreePath(lastNode.getPath()));
          // associate the path so far with that node
          _path2node.add(pathSoFar, thisNode);
        }
        else {
          // System.out.println("path2node contains pathSoFar");
          thisNode = _path2node.value(pathSoFar);
        }
        
        lastNode = thisNode;
        
        //elements.add(element);
      }
      
      /* lastNode is the node of the folder to add into */
      
      LeafNode<ItemT> child = new LeafNode<ItemT>(doc);
      _doc2node.put(doc, child);
      insertNodeSortedInto(child, lastNode);
//      _hasNonProjFilesOpen = (lastNode == root); 
      //    _model.insertNodeInto(child, lastNode, lastNode.getChildCount());
      this.expandPath(new TreePath(lastNode.getPath()));
    }
  }
  
  private void addTopLevelGroupToRoot(InnerNode<?, ItemT> parent) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) { // lock for mutation
      int indexInRoots = _roots.indexOf(parent);
      int num = _model.getChildCount(_model.getRoot());
      int i;
      for (i = 0; i < num; i++) {
        TreeNode n = (TreeNode)_model.getChild(_model.getRoot(), i);
        if(_roots.indexOf(n) > indexInRoots) break;
      }
      _model.insertNodeInto(parent, (MutableTreeNode)_model.getRoot(), i);
    }
  }
  
  /** Inserts the child node (INavigatorItem) into the sorted position as a parent node's child.  Only executes in the 
    * event thread.  Assumes that _model lock is already held.
    * @param child the node to add
    * @param parent the node to add under
    */
  private void insertNodeSortedInto(LeafNode<ItemT> child, InnerNode<?, ItemT> parent) {
    int numChildren = parent.getChildCount();
    String newName = child.toString();
    String oldName = parent.getUserObject().toString();
    DefaultMutableTreeNode parentsKid;
    
    /** Make sure that if the parent is a top level group, it is added to the tree model group. */
    if (((DefaultMutableTreeNode)_model.getRoot()).getIndex(parent) == -1 && _roots.contains(parent)) {
      addTopLevelGroupToRoot(parent);
    }
    int i;
    for (i = 0; i < numChildren; i++ ) {
      parentsKid = ((DefaultMutableTreeNode) parent.getChildAt(i));
      if (parentsKid instanceof InnerNode<?,?>) {
        // do nothing, it's a folder
      } else if(parentsKid instanceof LeafNode<?>) {
        oldName = ((LeafNode<?>)parentsKid).getData().getName();
        if ((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)) break;
      } else throw new IllegalStateException("found a node in navigator that is not an InnerNode or LeafNode");
    }
    _model.insertNodeInto(child, parent, i);
  }
  
  /** Inserts a folder (String) into sorted position under the parent.  Only executes in event thread. Assumes that
    * _model lock is already held
    * @param child the folder to add
    * @param parent the folder to add under
    */
  private void insertFolderSortedInto(InnerNode<?, ItemT> child, InnerNode<?, ItemT> parent) {
    int numChildren = parent.getChildCount();
    String newName = child.toString();
    String oldName = parent.getUserObject().toString();
    DefaultMutableTreeNode parentsKid;
    
    if (((DefaultMutableTreeNode)_model.getRoot()).getIndex(parent) == -1 && _roots.contains(parent)) {
      addTopLevelGroupToRoot(parent);
    }
    
    int countFolders = 0;
    int i;
    for (i = 0; i < numChildren; i++) {
      parentsKid = ((DefaultMutableTreeNode)parent.getChildAt(i));
      if (parentsKid instanceof InnerNode<?,?>) {
        countFolders++;
        oldName = parentsKid.toString();
        if ((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)) break;
      } 
      else if (parentsKid instanceof LeafNode<?>) break;
      // we're out of folders, and starting into the files, so just break out.
      else throw new IllegalStateException("found a node in navigator that is not an InnerNode or LeafNode");
    }
    _model.insertNodeInto(child, parent, i);
  }
  
  /** Removes a given <code>INavigatorItem<code> from this navigator. Removes all <code>INavigatorItem</code>s
    * from this navigator that are "equal" (using <code>.equals(...)</code>) to the passed argument. Any of
    * the removed documents may be returned by this method. If the NavigatorItem is found in the navigator, null
    * is returned.  Only executes from event thread.
    * @param doc the docment to be removed
    * @return doc a document removed from this navigator as a result of invoking this method.
    * @throws IllegalArgumentException if this navigator contains no document equal to doc
    */
  public ItemT removeDocument(ItemT doc) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) { // lock for mutation
      LeafNode<ItemT> toRemove = getNodeForDoc(doc);
      if (toRemove == null) return null;
      return removeNode(getNodeForDoc(doc));
    }
  } 
  
  /** Assumes lock on _model is already held or that it is being run in the event thread. */
  private LeafNode<ItemT> getNodeForDoc(ItemT doc) { 
//    synchronized(_model) { 
    return _doc2node.get(doc); 
//    }
  }
  
  /** Only takes in nodes that have an INavigatorItem as their object; assumes _model lock is already held.
    * Only executes in event thread. */
  private ItemT removeNode(LeafNode<ItemT> node) {
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
    _model.removeNodeFromParent(node);
    _doc2node.remove(node.getData());
    cleanFolderNode(parent);
    return node.getData();
  }
  
  /** If the given node is an InnerNode with no children, it removes it from the tree.  If the given node is a leaf or
    * the root, it does nothing to it.  Assumes that _model lock is already held.  Only executes in the event thread.
    */
  private void cleanFolderNode(DefaultMutableTreeNode node) {
    if (node instanceof InnerNode<?,?> && node.getChildCount() == 0) {
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
      _model.removeNodeFromParent(node);
      @SuppressWarnings("unchecked") InnerNode<?, ItemT> typedNode = (InnerNode<?, ItemT>) node;
      _path2node.remove(_path2node.antecedent(typedNode), typedNode);
      cleanFolderNode(parent);
    }
  }
  
  /** Resets a given <code>INavigatorItem<code> in the tree.  Updates the placement of the item and its display
    * to reflect any changes made in the model.  Only executes in the event thread.
    * Note: full synchronization commented out because this operation is only performed in the event thread.  The 
    * synchronized sections must be atomic but the rest of the code can run concurrently with read operations in 
    * other threads.
    * @param doc the document to be refreshed
    * @param path the path to the parent folder for this document
    * @throws IllegalArgumentException if this navigator contains no document equal to doc.
    */
  public void refreshDocument(ItemT doc, String path) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
//    synchronized(_model) {
    LeafNode<ItemT> node = _doc2node.get(doc);
    InnerNode<?, ?> oldParent;
    if (node == null) { // document has not yet been entered in tree
      addDocument(doc, path);
      return;
    }
    
    InnerNode<?, ?> p = (InnerNode<?, ?>) node.getParent();
    oldParent = p;
    
    // Check to see if the new parent (could be same) exists already
    String newPath = path;
    
    if (newPath.length() > 0) {
      if (newPath.substring(0,1).equals("/")) newPath = newPath.substring(1);
      if (! newPath.substring(newPath.length() - 1).equals("/")) newPath = newPath + "/";
    }
    
    InnerNode<?, ItemT> newParent = _path2node.value(newPath); // node that should be parent
    
    if (newParent == oldParent) { // no mutation has occurred before this point because oldParent != null
      if (! node.toString().equals(doc.getName())) { // document has changed name?
        synchronized(_model) {
          LeafNode<ItemT> newLeaf = new LeafNode<ItemT>(doc);
          _doc2node.put(doc, newLeaf);
          insertNodeSortedInto(newLeaf, newParent);
          _model.removeNodeFromParent(node);
        }
      }
      // don't do anything if its name or parents haven't changed
    } 
    else { // document has moved within tree
      synchronized(_model) {
        removeNode(node);
        addDocument(doc, path);
      }
    }
//    }
  }
  
  /** Sets the specified document to be active (current).  Only executes in the event thread. */
  public void selectDocument(ItemT doc) {
    assert EventQueue.isDispatchThread();
//    synchronized (_model) {  // lock out mutation
    DefaultMutableTreeNode node = _doc2node.get(doc);
    if (node == null) return; // doc is not in the navigator
    if (node == _current) return;  // current doc is the active doc
//      if (_doc2node.containsKey(doc);) {  // this test is obviously true since node == _doc2node.get(doc)
    TreeNode[] nodes = node.getPath();
    TreePath path = new TreePath(nodes);
    expandPath(path);
    setSelectionPath(path);  // fires _gainVisitor in AbstractGlobalModel
    scrollPathToVisible(path);
//      }
//    }
  }
  
  /** Returns a typed equivalent to {@code next.getUserObject()}.  Assumes the DefaultMutableTreeNode
    * is a leaf node in _model and thus, if parameterized, would have type ItemT.  This is a workaround for
    * the lack of a generic implementation of TreeModel and TreeNode.  If those classes become generified,
    * this code will no longer be necessary.
    */
  private ItemT getNodeUserObject(DefaultMutableTreeNode n) {
    @SuppressWarnings("unchecked") ItemT result = (ItemT) n.getUserObject();
    return result;
  }
  
  /** Returns the next document in the collection (using enumeration order).  Executes in any thread.
    * @param doc the INavigatorItem of interest
    * @return the INavigatorItem which comes after doc
    */
  public ItemT getNext(ItemT doc) {
    synchronized(_model) { // locks out mutation
      DefaultMutableTreeNode node = _doc2node.get(doc);
      if (node == null) return doc; // doc may not be contained in navigator
      // TODO: check for "package" case
      DefaultMutableTreeNode next = node.getNextLeaf();
      if (next == null || next == _model.getRoot()) { return doc; }
      else { return getNodeUserObject(next); }
    }
  }
  
  /** Returns the previous document in the collection (using enumeration order).  Executes in any thread.
    * @param doc the INavigatorItem of interest
    * @return the INavigatorItem which comes before doc
    */
  public ItemT getPrevious(ItemT doc) {
    synchronized(_model) { // locks out mutation
      DefaultMutableTreeNode node = _doc2node.get(doc);
      if (node == null) return doc; // doc may not be contained in navigator
      // TODO: check for "package" case
      DefaultMutableTreeNode prev = node.getPreviousLeaf();
      if (prev == null || prev == _model.getRoot()) { return doc; }
      else { return getNodeUserObject(prev); }
    }
  }
  
  /** Returns the first document in the collection (using enumeration order).  Executes in any thread.
    * @return the INavigatorItem which comes before doc
    */
  public ItemT getFirst() {
    synchronized(_model) { // locks out mutation
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) _model.getRoot();
      return getNodeUserObject(root.getFirstLeaf());
    }
  }
  
  /** Returns the last document in the collection (using enumeration order).  Executes in any thread.
    * @return the INavigatorItem which comes before doc
    */
  public ItemT getLast() {
    synchronized(_model) { // locks out mutation
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) _model.getRoot();
      return getNodeUserObject(root.getLastLeaf());
    }
  }
  
  /** Tests to see if a given document is contained in this navigator.  Executes in any thread.
    * @param doc the document to test for containment.
    * @return <code>true</code> if this navigator contains a document that is "equal" (as tested by the
    *         <code>equals</code< method) to the passed document, else <code>false</code>.
    */
  public boolean contains(ItemT doc) { 
    synchronized(_model) { return _doc2node.containsKey(doc); }  // locks out mutation
  }
  
  /** Tests to see if a given document is contained in this navigator.  Only executes in event thread.*/
  public boolean _contains(ItemT doc) { return _doc2node.containsKey(doc); }
  
  /** Returns all the <code>IDocuments</code> contained in this navigator. Does not assert any type of ordering on 
    * the returned structure.  Executes in any thread.
    * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
    */
  public ArrayList<ItemT> getDocuments() {
    
    final ArrayList<ItemT> list = new ArrayList<ItemT>(getDocumentCount()); // Use Vector because it implements an Enumeration
    
    synchronized(_model) { // locks out mutation
      // e has a raw type because depthFirstEnumeration() has a raw type signature
      Enumeration<?> e = ((DefaultMutableTreeNode)_model.getRoot()).depthFirstEnumeration();
      
      while(e.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
        if (node.isLeaf() && node != _model.getRoot()) {
          list.add(getNodeUserObject(node));
        }
      }
    }
    return list;
  }
  
  /** Returns all the <code>IDocuments</code> contained in the specified bin.
    * @param binName name of bin
    * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
    */
  public ArrayList<ItemT> getDocumentsInBin(String binName) {
    final ArrayList<ItemT> list = new ArrayList<ItemT>();
    
    synchronized(_model) { // locks out mutation
      for(GroupNode<ItemT> gn: _roots) {
        if (gn.getData().equals(binName)) {
          // found the bin with the right name
          // e has a raw type because depthFirstEnumeration() has a raw type signature
          Enumeration<?> e = gn.depthFirstEnumeration();
          
          while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.isLeaf() && node != _model.getRoot()) {
              list.add(getNodeUserObject(node));
            }
          }
        }
      }
    }
    
    return list;
  }
  
  /** Returns the number of <code>IDocuments</code> contained by this <code>IDocumentNavigator</code>
    * Not synchronized on the assumption that size field of a HashMap always has a legitimate
    * value (either the size of the current state or the size of its state before some concurrent
    * operation started.  Executes in any thread.  Assume size() always returns a valid (perhaps stale) value.
    * @return the number of documents within this navigator.
    */
  public int getDocumentCount() { return _doc2node.size(); }
  
  /** Returns whether this <code>IDocumentNavigator</code> contains any <code>IDocuments</code>.
    * @return <code>true</code> if this navigator contains one or more documents, else <code>false</code>.
    * Executes in any thread.  Assume isEmpty() always returns a valid (perhaps stale) value.
    */
  public boolean isEmpty() { return _doc2node.isEmpty(); }
  
  /** Removes all <code>IDocuments</code> from this <code>IDocumentNavigator</code>.  Only executes in event thread. */
  public void clear() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) {
      _doc2node.clear();
      ((DefaultMutableTreeNode)_model.getRoot()).removeAllChildren();
    }
  }
  
  /** Adds an <code>INavigationListener</code> to this navigator. After invoking this method, the passed 
    * listener will be eligible for observing this navigator. If the provided listener is already observing 
    * this navigator (as tested by the == operator), no action is taken.  Only executes in event thread.
    * @param listener the listener to be added to this navigator.
    */
  public void addNavigationListener(INavigationListener<? super ItemT> listener) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) { navListeners.add(listener); }  // locks out access during mutation
  }
  
  /** Removes the given listener from observing this navigator. After invoking this method, all observers 
    * watching this navigator "equal" (as tested by the == operator) will no longer receive observable dispatches.
    * Only executes in event thread.
    * @param listener the listener to be removed from this navigator
    */
  public void removeNavigationListener(INavigationListener<? super ItemT> listener) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    synchronized(_model) { navListeners.remove(listener); }
  }
  
  /** Returns a collection of all navigator listeners. Note: this is a dangerous method since it exposes a shared data 
    * structure that must be synchronized with _model.
    */
  public Collection<INavigationListener<? super ItemT>> getNavigatorListeners() { return navListeners; }
  
  /** Standard visitor pattern.   Only used within this class.
    * @param algo the visitor to run
    * @param input the input for the visitor
    */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<ItemT, InType, ReturnType> algo, InType input) {
    return algo.forTree(this, input);
  }
  
  /** Called whenever the value of the selection changes.  Only runs in event thread. Runs _gainVisitor in global model
    * @param e the event that characterizes the change.
    */
  public void valueChanged(TreeSelectionEvent e) {
    Object treeNode = this.getLastSelectedPathComponent();
    if (treeNode == null || ! (treeNode instanceof NodeData<?>)) return;
    @SuppressWarnings("unchecked") NodeData<ItemT> newSelection = (NodeData<ItemT>) treeNode;
    if (_current != newSelection) {
      for(INavigationListener<? super ItemT> listener : navListeners) {
        listener.lostSelection(_current, isNextChangeModelInitiated());
        listener.gainedSelection(newSelection, isNextChangeModelInitiated());
      }
      _current = newSelection;
    }
    
    setNextChangeModelInitiated(false);
  }
  
  /** Returns a renderer for this object. */
  public Component getRenderer() { return _renderer; }
  
  /** The cell renderer for this tree. Only runs in event thread. */
  private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    
    /** Rreturns the component for a cell
      * @param tree
      */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean isExpanded,
                                                  boolean leaf, int row, boolean hasFocus) {
      
      // changing last argument from hasFocus to false appears to fix a focus bug when selecting a new document
      super.getTreeCellRendererComponent(tree, value, sel, isExpanded, leaf, row, false);  
      
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      if (node instanceof RootNode<?> && _rootIcon != null) setIcon(_rootIcon);
      
      else if (node instanceof LeafNode<?>) {
        ItemT doc = getNodeUserObject(node);
        if (leaf && _displayManager != null) {
          setIcon(_displayManager.getIcon(doc));
          setText(_displayManager.getName(doc));
        }
      }
      return this;
    }
  }
  
  /** Selects the document at the x,y coordinate of the navigator pane and installs it as the active document.  Only
    * runs in event thread.
    * @param x the x coordinate of the navigator pane
    * @param y the y coordinate of the navigator pane
    */
  public boolean selectDocumentAt(int x, int y) {
    TreePath path = getPathForLocation(x, y);
    if (path == null) return false;
    else {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      if (node instanceof LeafNode<?>) {
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      } 
      else if (node instanceof InnerNode<?,?>) {
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      } 
      else if (node instanceof RootNode<?>) {
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      } 
      else return false;
    }
//    }
  }
  
  /** Returns true if the item at the x,y coordinate of the navigator pane is currently selected.
    * Only runs in event thread. O
    * @param x the x coordinate of the navigator pane
    * @param y the y coordinate of the navigator pane
    * @return true if the item is currently selected
    */
  public boolean isSelectedAt(int x, int y) {
    TreePath path = getPathForLocation(x, y);
    if (path == null) return false;
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return false; }
    for(TreePath p: ps) {
      if (path.equals(p)) { return true; }
    }
    return false;
  }
  
  /** @return true if at least one group of INavigatorItems is selected.  Only runs in event thread. */
  public boolean isGroupSelected() { return getGroupSelectedCount() != 0; }
  
  /** @return the number of groups selected. Only runs in event thread. */
  public int getGroupSelectedCount() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    int count = 0;
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return 0; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n instanceof InnerNode<?,?>) { ++count; }
    }
    return count;
  }
  
  /** @return the folders currently selected. Only runs in event thread. */
  public java.util.List<File> getSelectedFolders() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    ArrayList<File> l = new ArrayList<File>();
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return l; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n instanceof FileNode<?>) {
        l.add(((FileNode<?>)n).getData());
      }
    }
    return l;
  }
  
  /** @return true if at least one document is selected.  Only runs in event thread. */
  public boolean isDocumentSelected() { return getDocumentSelectedCount() != 0; }
  
  /** @return the number of documents selected. Only runs in event thread. */
  public int getDocumentSelectedCount() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    int count = 0;
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return 0; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n instanceof LeafNode<?>) {
        ++count;
      }
    }
    return count;
  }
  
  /** @return the documents currently selected. Only runs in event thread. */
  @SuppressWarnings("unchecked") public java.util.List<ItemT> getSelectedDocuments() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    ArrayList<ItemT> l = new ArrayList<ItemT>();
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return l; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n instanceof LeafNode) {
        l.add((ItemT)((LeafNode)n).getData());
      }
    }
    return l;
  }
  
  /** Returns true if at least one top level group is selected.  Only runs in event thread. */
  public boolean isTopLevelGroupSelected() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return false; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n instanceof GroupNode<?>) { return true; }
    }
    return false;
  }
  
  /** Returns true if the root is selected. Only runs in event thread. */
  public boolean isRootSelected() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return false; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      if (n == _model.getRoot()) { return true; }
    }
    return false;
  }
  
  /** Returns the names of the top level groups that the selected items descend from.  Only runs in event thread. */
  public java.util.Set<String> getNamesOfSelectedTopLevelGroup() throws GroupNotSelectedException {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    HashSet<String> names = new HashSet<String>();
    LinkedList<GroupNode<ItemT>> roots = new LinkedList<GroupNode<ItemT>>(_roots);
    
    TreePath[] ps = getSelectionPaths();
    if (ps != null) {
      for(TreePath p: ps) {
        if (p.getLastPathComponent() instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
          
          for(GroupNode<ItemT> gn: roots) {
            if (gn.isNodeDescendant(n)) {
              // n is a descendent of gn; add the name of the group node
              names.add(gn.getData());
              // this group node definitely contains selected items, no need to check it again;
              // remove it from the list of roots to consider
              roots.remove(gn);
              break;
            }
          }
        }
      }
    }
    
    if (names.isEmpty()) { throw new GroupNotSelectedException("there is no top level group for the root of the tree"); }
    
    return names;
  }
  
  /** Returns the currently selected leaf node, or null if the selected node is not a leaf. Only reads a single
    * volatile field that always has a valid value.  Thread safe. 
    */
  public ItemT getCurrent() {
    NodeData<ItemT> current = _current;
    if (current == null) return null;
    return current.execute(_leafVisitor);
  }
  
  /** Returns the model lock. */
  public Object getModelLock() { return _model; }
  
  // This visitor is executed in getCurrent(). The case methods for files and strings return
  // null, which caused DrJava to revert to the previous selection in requestSelectionUpdate().
  // That code has now been commented out; this note is only for documentation purposes.
  private final NodeDataVisitor<ItemT, ItemT> _leafVisitor = new NodeDataVisitor<ItemT, ItemT>() {
    public ItemT fileCase(File f, Object... p){ return null; }
    public ItemT stringCase(String s, Object... p){ return null; }
    public ItemT itemCase(ItemT ini, Object... p){ return ini; }
  };
  
  /** @return true if the INavigatorItem is in a selected group, if at least
    * one group is selected.  Only runs in event thread. */
  public boolean isSelectedInGroup(ItemT i) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    TreePath[] ps = getSelectionPaths();
    if (ps == null) { return false; }
    for(TreePath p: ps) {
      TreeNode n = (TreeNode) p.getLastPathComponent();
      TreeNode l = _doc2node.get(i);
      
      if (n == _model.getRoot()) return true;
      
      while (l.getParent() != _model.getRoot()) {
        if(l.getParent() == n) return true;
        l = l.getParent();
      }
    }
    
    return false;
  }
  
  /** Adds a top level group to the navigator.  Only runs in event thread. */
  public void addTopLevelGroup(String name, INavigatorItemFilter<? super ItemT> f){
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    if (f == null)
      throw new IllegalArgumentException("parameter 'f' is not allowed to be null");
    GroupNode<ItemT> n = new GroupNode<ItemT>(name, f);
    _roots.add(n);
  }
  
  /******* Methods that handle expansion/collapsing of folders in tree **********/
  
  /**  Called whenever an item in the tree has been collapsed.   Only runs in event thread (except when testing). */
  public void treeCollapsed(TreeExpansionEvent event) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof InnerNode<?,?>) ((InnerNode<?,?>)o).setCollapsed(true);
  }
  
  /** Called whenever an item in the tree has been expanded.  Only runs in event thread. */
  public void treeExpanded(TreeExpansionEvent event) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof InnerNode<?,?>) ((InnerNode<?,?>)o).setCollapsed(false);
  }
  
  /** Collapses all the paths in the tree that match one of the path strings included in the given hash set.  Path 
    * strings must follow a specific format in order for them to work. See the documentation of 
    * <code>generatePathString</code> for information on the format of the path strings.  Only executes in event thread.
    * @param paths A hash set of path strings. 
    */
  public void collapsePaths(String[] paths) {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    HashSet<String> set = new HashSet<String>();
    for (String s : paths) { set.add(s); }
    collapsePaths(set);
  }
  
  /** Set variation of collapsePaths(String ...).  Private except for testing code. Only runs in event thread except
    * for testing code. 
    */
  void collapsePaths(HashSet<String> paths) {
    
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)_model.getRoot();
    // We use a raw type here because depthFirstEnumeration() has a raw type signature
    Enumeration<?> nodes = rootNode.depthFirstEnumeration();
//    ArrayList<String> list = new ArrayList<String>();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)nodes.nextElement();
      if (tn instanceof InnerNode<?,?>) {
        TreePath tp = new TreePath(tn.getPath());
        String s = generatePathString(tp);
        boolean shouldCollapse = paths.contains(s);
        if (shouldCollapse) { collapsePath(tp); }
      }
    }
  }
  
  /** @return an array of path strings corresponding to the paths of the tree nodes that
    * are currently collapsed. See the documentation of <code>generatePathString</code>
    * for information on the format of the path strings.  Only runs in event thread (except when testing).
    */
  public String[] getCollapsedPaths() {
    assert (EventQueue.isDispatchThread() || Utilities.TEST_MODE);
    
    ArrayList<String> list = new ArrayList<String>();
//    synchronized (_model) {
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)_model.getRoot();
    // We use a raw type here because depthFirstEnumeration() has a raw type signature
    Enumeration<?> nodes = rootNode.depthFirstEnumeration(); /** This warning is expected **/
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)nodes.nextElement();
      if (tn instanceof InnerNode<?,?> && ((InnerNode<?,?>)tn).isCollapsed()) {
        TreePath tp = new TreePath(tn.getPath());
        list.add(generatePathString(tp));
      }
    }
//    }
    return list.toArray(new String[list.size()]);
  }
  
  /** Generates a path string for the given tree node. <p>The path string does not include the project 
    * root node, but rather a period in its place. Following the "./" is one of the 3 main groups, 
    * "[ Source Files ]", "[ Auxiliary ]", "[ External ]".  The nodes in the path are represented by their
    * names delimited by the forward slash ("/").  The path ends with a final delimeter.
    * (e.g. "./[ Source Files ]/util/docnavigation/")  Only runs in event thread.
    * @return the path string for the given node in the JTree
    */
  public String generatePathString(TreePath tp) {
    String path = "";
//    synchronized (_model) {
    TreeNode root = (TreeNode) _model.getRoot();
    
    while (tp != null) {
      TreeNode curr = (TreeNode) tp.getLastPathComponent();
      if (curr == root) path = "./" + path;
      else path = curr + "/" + path;
      tp = tp.getParentPath();
//      }
    }
    
    return path;
  }
  
  /** If the currently selected item is not an INavigatorItem, select the one given. Only runs in event thread. */
  public void requestSelectionUpdate(ItemT ini) {
//    synchronized (_model) {
    // This code checked if the selected node was a leaf, i.e. a Java source file,
    // and if it wasn't, reverts the selection. The code was commented out to allow
    // selection of file nodes (i.e. folders) and string nodes (i.e. [ Source Files ], etc.)
//    if (getCurrent() == null) { // the currently selected node is not a leaf
//      setActiveDoc(ini);
//    }
//    }
  }  
  
  /** Marks the next selection change as model-initiated (true) or user-initiated (false; default). */
  public void setNextChangeModelInitiated(boolean b) {
    putClientProperty(MODEL_INITIATED_PROPERTY_NAME, b?Boolean.TRUE:null);
  }
  
  /** @return whether the next selection change is model-initiated (true) or user-initiated (false). */
  public boolean isNextChangeModelInitiated() {
    return getClientProperty(MODEL_INITIATED_PROPERTY_NAME) != null;
  }
  
//  /** Unnecessary since "modified" mark is added by the cell renderer */
//  public void activeDocumentModified() { }  
  
  /** Drag and drop target. */
  DropTarget dropTarget = new DropTarget(this, this);  
  
  /** User dragged something into the component. */
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    DrJavaRoot.dragEnter(dropTargetDragEvent);
  }
  
  public void dragExit(DropTargetEvent dropTargetEvent) { }
  public void dragOver(DropTargetDragEvent dropTargetDragEvent) { }
  public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){ }
  
  /** User dropped something on the component. */
  public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
    DrJavaRoot.drop(dropTargetDropEvent);
  }
}

