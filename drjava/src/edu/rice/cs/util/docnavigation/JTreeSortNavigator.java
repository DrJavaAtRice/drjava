/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/




package edu.rice.cs.util.docnavigation;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.io.File;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import edu.rice.cs.util.*;

public class JTreeSortNavigator extends JTree implements IAWTContainerNavigatorActor, TreeSelectionListener {
  
  /**
   * maps documents to tree nodes
   */
  private HashMap<INavigatorItem, DefaultMutableTreeNode> _doc2node =
    new HashMap<INavigatorItem, DefaultMutableTreeNode>();
  
  /**
   * maps path's to nodes and nodes to paths
   */
  private BidirectionalHashMap<String, DefaultMutableTreeNode> _path2node = 
    new BidirectionalHashMap<String, DefaultMutableTreeNode>();
  
  /**
   * the root of the tree
   */
  private DefaultMutableTreeNode _root;
  
  /**
   * the model of the tree
   */
  private DefaultTreeModel _model;
  
  /**
   * the currently selected item
   */
  private INavigatorItem _currSelected;
  
  /**
   * the node corresponding to the [external files] node in the tree
   * this will hold files that are not in the project directory
   */
  private DefaultMutableTreeNode _nonProjRoot = new DefaultMutableTreeNode("[External Files]");
  
  /**
   * flag if the nonproject node has children
   */
  private boolean _hasnonprojfilesopen = false;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  private Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  /**
   * the renderer for this JTree
   */
  protected CustomTreeCellRenderer _renderer;

  
  /**
   * sets the foreground color of this JTree
   * @param c the color to set to
   */
  public void setForeground(Color c){
    super.setForeground(c);
    _renderer.setTextNonSelectionColor(c);
  }
  
  /**
   * sets the background color of this tree
   * @param c the color to set to
   */
  public void setBackground(Color c){
    super.setBackground(c);
    if(_renderer != null)
      _renderer.setBackgroundNonSelectionColor(c);
  }
  
  /**
   * standard constructor
   * @param name the name of the root node
   */
  public JTreeSortNavigator(String name) {
    super(new DefaultTreeModel(new DefaultMutableTreeNode(name)));
    
    this.addTreeSelectionListener(this);
    
    _model = (DefaultTreeModel) this.getModel();
    _root = (DefaultMutableTreeNode) _model.getRoot();
    _renderer = new CustomTreeCellRenderer();
    _renderer.setOpaque(false);
//    _renderer.setJavaIcon(getIcon("javaicon.gif"));
    this.setCellRenderer(_renderer);

    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    //this.setShowsRootHandles(true);
  }
  
  /**
   * @return an AWT component which interacts with this document navigator
   */
  public Container asContainer() {
    return this;
  }
  
  /**
   * Adds an <code>IDocuemnt</code> to this navigator.
   *
   * @param doc the document to be added into this navigator.
   */
  public void addDocument(INavigatorItem doc) {
   
    
    if(!_hasnonprojfilesopen) {
      insertFolderSortedInto(_nonProjRoot, _root);
    }
    
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(doc.getName());
    node.setUserObject(doc);
    //_root.add(node);
    insertNodeSortedInto(node, _nonProjRoot);
    this.expandPath(new TreePath(_nonProjRoot.getPath()));
    _doc2node.put(doc, node);
    _hasnonprojfilesopen = true;
//    this.setActiveDoc(doc);
  }
  
  /**
   * Adds an <code>INavigatorItem</code> into this navigator in a position
   * relavite to a path. The actual behavior of
   * the navigator and implication of adding a document "relative to" the
   * second paramater is left up to the implementing class.
   *
   * @param doc        the document to be added into this navigator.
   * @param relativeto an existing document in the navigator.
   * @throws IllegalArgumentException if this navigator does not contain <code>relativeto</code> as tested by the
   *                                  <code>contains</code> method.
   */
  public void addDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    
    if (!path.equals(_topLevelPath) && !path.startsWith(_topLevelPath + File.separator) )
    {
      addDocument(doc);
      return;
    }   
    // if the file is at the top level
    if (path.equals(_topLevelPath)) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(doc.getName());
      node.setUserObject(doc);
      insertNodeSortedInto(node, _root);
      _doc2node.put(doc, node);
      return;
    }
    
    // strip out the absolute path info
    path = path.substring(_topLevelPath.length());
    
    StringTokenizer tok = new StringTokenizer(path, File.separator);
    //ArrayList<String> elements = new ArrayList<String>();
    String pathSoFar="";
    DefaultMutableTreeNode lastNode = _root;
    while(tok.hasMoreTokens()) {
      String element = tok.nextToken();
      pathSoFar += (element + "/");
      DefaultMutableTreeNode thisNode;
      //System.out.println("pathsofar = " + pathSoFar);
      // if the node is not in the hashmap yet
      if (!_path2node.containsKey(pathSoFar)) {
        // make a new node
        
        /* this inserts a folder node */
        thisNode = new DefaultMutableTreeNode(element);
        insertFolderSortedInto(thisNode, lastNode);
          this.expandPath(new TreePath(lastNode.getPath()));
        // associate the path so far with that node
        _path2node.put(pathSoFar, thisNode);
      }
      else {
       // System.out.println("path2node contains pathSoFar");
        thisNode = _path2node.getValue(pathSoFar);
      }
      
      lastNode = thisNode;
      
      
      //elements.add(element);
    }
    
    /* lastNode is the node of the folder to add into */
    
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(doc);
    _doc2node.put(doc, child);
    insertNodeSortedInto(child, lastNode);
//    _model.insertNodeInto(child, lastNode, lastNode.getChildCount());
    this.expandPath(new TreePath(lastNode.getPath()));
    child.setUserObject(doc);
  }
  
  
  /**
   * inserts the child node (INavigatorItem) into the sorted position as a parent node's child
   * @param child the node to add
   * @param parent the node to add under
   */
  private void insertNodeSortedInto(DefaultMutableTreeNode child, DefaultMutableTreeNode parent){
    int numChildren = parent.getChildCount();
    int i=0;
    int indexToAdd = i;
    String newName;
    if(child.getUserObject() instanceof String){
      newName = (String) child.getUserObject();
    }else
    if(child.getUserObject() instanceof INavigatorItem){
      newName = ((INavigatorItem)child.getUserObject()).getName();
    }else{
      newName = child.toString();
    }
    String oldName = (String)parent.getUserObject();
    
    DefaultMutableTreeNode parentsKid;

    while(i<numChildren){
      parentsKid = ((DefaultMutableTreeNode)parent.getChildAt(i));
      if(((DefaultMutableTreeNode)parentsKid).getUserObject()  instanceof INavigatorItem){
        oldName = ((INavigatorItem)((DefaultMutableTreeNode)parentsKid).getUserObject()).getName();
        if((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)){
          break;
        }
      }
      i++;
    }
    _model.insertNodeInto(child, parent, i);
  }
  
  /**
   * inserts a folder (String) into sorted position under the parent
   * @param child the folder to add
   * @param parent the folder to add under
   */
  private void insertFolderSortedInto(DefaultMutableTreeNode child, DefaultMutableTreeNode parent){
    int numChildren = parent.getChildCount();
    int i=0;
    int indexToAdd = i;
    String newName;
    if(child.getUserObject() instanceof String){
      newName = (String) child.getUserObject();
    }else
    if(child.getUserObject() instanceof INavigatorItem){
      newName = ((INavigatorItem)child.getUserObject()).getName();
    }else{
      newName = child.toString();
    }
    String oldName = (String)parent.getUserObject();
    
    DefaultMutableTreeNode parentsKid;

    int countFolders = 0;
    while(i<numChildren){
      parentsKid = ((DefaultMutableTreeNode)parent.getChildAt(i));
      if(((DefaultMutableTreeNode)parentsKid).getUserObject()  instanceof String){
        countFolders++;
        oldName = (String)((DefaultMutableTreeNode)parentsKid).getUserObject();
        if((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)){
          break;
        }
      }else{
        // we're no longer comparing to other folders, so break out of loop
        break;
      }
      i++;
    }
    
    _model.insertNodeInto(child, parent, i);
  }
  
  
  /**
   * the top level path of this tree
   */
  private String _topLevelPath = "";
  
  /**
   * sets the top level path of this tree
   * @param path the new top level path for this tree
   */
  public void setTopLevelPath(String path) {
    _topLevelPath = path;
  }
  
  
  /**
   * Removes a given <code>INavigatorItem<code> from this navigator. Removes
   * all <code>INavigatorItem</code>s from this navigator that are "equal" (as
   * tested by the <code>equals</code> method) to the passed argument. Any of
   * the removed documents may be returned by this method.
   *
   * @param doc the docment to be removed
   * @return doc a document removed from this navigator as a result of invoking this method.
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException {
    return removeNode(getNodeForDoc(doc));
  }
  
  
  private DefaultMutableTreeNode getNodeForDoc(INavigatorItem doc){
    return _doc2node.get(doc);
  }
  
  /**
   * only takes in nodes that have a inavigatoritem as their object
   */
  private INavigatorItem removeNode(DefaultMutableTreeNode toRemove){
    
    _model.removeNodeFromParent(toRemove);
    _doc2node.remove((INavigatorItem)toRemove.getUserObject());
   
    Enumeration enumeration = _root.depthFirstEnumeration();
    while(enumeration.hasMoreElements())
    {
      TreeNode next = (TreeNode)enumeration.nextElement();
      if(next.getChildCount() == 0 && !_doc2node.containsValue(next) && next != _root)
      {
        _model.removeNodeFromParent((MutableTreeNode)next); 
        _path2node.removeKey((DefaultMutableTreeNode)next);
      }
    }
   
  
    if(_nonProjRoot.getChildCount() == 0)
    {
      _hasnonprojfilesopen = false;
    }
    return (INavigatorItem)toRemove.getUserObject();
  }
    
  /**
   * Resets a given <code>INavigatorItem<code> in the tree.  This may affect the
   * placement of the item or its display to reflect any changes made in the model.
   * @param doc the docment to be refreshed
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public void refreshDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    /**
     * since we are modifying the model of this tree, and in the middle of this modification,
     * we have an unstable model, we need to synchronize around it.
     * Note: this solved the bug where compile all with modified documents would throw an array
     * index out of bounds exception when painting.
     */
    synchronized(this){
      DefaultMutableTreeNode node = getNodeForDoc(doc);
      removeNode(node);
      addDocument(doc, path);
    }
  }
  
  public void paint(Graphics g){
    /**
     * we don't want to paint if we're in the middle of some other
     * synchronized code, so we'll wait until we can aquire the lock
     * around ourself
     */
    synchronized(this){
      super.paint(g);
    }
  }
  
  /** sets the input document to be active */
  public void setActiveDoc(INavigatorItem doc){
    DefaultMutableTreeNode node = _doc2node.get(doc);
    if(this.contains(doc)){
      TreeNode[] nodes = node.getPath();
      TreePath path = new TreePath(nodes);
      this.expandPath(path);
      this.setSelectionPath(path);
      this.scrollPathToVisible(path);
    }
  }

  /**
   * Impose some ordering on the documents in the navigator, to facilitate
   * MainFrame's setActiveNextDocument()
   *
   * @param doc the INavigatorItem of interest
   * @return the INavigatorItem which comes after doc
   */
  public INavigatorItem getNext(INavigatorItem doc) {
    DefaultMutableTreeNode node = _doc2node.get(doc);
    // TODO: check for "package" case
    DefaultMutableTreeNode next = node.getNextLeaf();
    if (next == null || next == _root) return doc;
    return  (INavigatorItem) next.getUserObject();
  }
  
  /**
   * Impose some ordering on the documents in the navigator, to facilitate MainFrame's setActivePrevDocument()
   *
   * @param doc the INavigatorItem of interest
   * @return the INavigatorItem which comes before doc
   */
  public INavigatorItem getPrevious(INavigatorItem doc) {
    DefaultMutableTreeNode node = _doc2node.get(doc);
    // TODO: check for "package" case
    DefaultMutableTreeNode prev = node.getPreviousLeaf();
    if (prev == null || prev == _root) return doc;
    return  (INavigatorItem) prev.getUserObject();
  }
  
  /**
   * Tests to see if a given document is contained in this navigator.
   *
   * @param doc the document to test for containment.
   * @return <code>true</code> if this navigator contains a document that is "equal" (as tested by the
   *         <code>equals</code< method) to the passed document, else <code>false</code>.
   */
  public boolean contains(INavigatorItem doc) {
    return _doc2node.containsKey(doc);
  }
  
  /**
   * Returns all the <code>IDocuments</code> contained in this
   * navigator</code>. Does not assert any type of ordering on the returned
   * structure.
   *
   * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
   */
  public Enumeration<INavigatorItem> getDocuments() {
    
    final ArrayList<INavigatorItem> list = new ArrayList<INavigatorItem>();
    Enumeration e_tmp = _root.depthFirstEnumeration();
    while(e_tmp.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)e_tmp.nextElement();
      if (node.isLeaf() && node != _root) 
        list.add((INavigatorItem)node.getUserObject());
    }
    
    return new Enumeration<INavigatorItem>() {
      
      private Iterator<INavigatorItem> it = list.iterator();
      
      public boolean hasMoreElements() {
        return it.hasNext();
      }
      
      public INavigatorItem nextElement() {
        return it.next();
      }
    };
  }
  
  /**
   * Returns the number of <code>IDocuments</code> contained by this
   * <code>IDocumentNavigator</code>
   *
   * @return the number of documents within this navigator.
   */
  public int getDocumentCount() {
    return _doc2node.size();
  }
  
  /**
   * Returns whether this <code>IDocumentNavigator</code> contains any <code>IDocuments</code>.
   *
   * @return <code>true</code> if this navigator contains one or more documents, else <code>false</code>.
   */
  public boolean isEmpty() {
    return _doc2node.isEmpty();
  }
  
  /**
   * Removes all <code>IDocuments</code> from this <code>IDocumentNavigator</code>.
   */
  public void clear() {
    _doc2node.clear();
    _root.removeAllChildren();
  }
  
  /**
   * Adds an <code>INavigationListener</code> to this navigator. After
   * invoking this method, the passed listener will be eligable for observing
   * this navigator. If the provided listener is already observing this
   * navigator (as tested by the == operator), no action is taken.
   *
   * @param listener the listener to be added to this navigator.
   */
  public void addNavigationListener(INavigationListener listener) {
    navListeners.add(listener);
  }
  
  /**
   * Removes the given listener from observing this navigator. After invoking
   * this method, all observers observing this navigator "equal" (as tested by
   * the == operator) will no longer receive observable dispatches.
   *
   * @param listener the listener to be removed from this navigator
   */
  public void removeNavigationListener(INavigationListener listener) {
    navListeners.remove(listener);
  }
  
  /**
   * retuns a collection of all navigator listeners
   */
  public Collection<INavigationListener> getNavigatorListeners()
  {
    return navListeners;
  }
  
  /**
   * standard visitor pattern
   * @param algo the visitor to run
   * @param input the input for the visitor
   */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input) {
    return algo.forTree(this, input);
  }
  
  /**
   * Called whenever the value of the selection changes.
   *
   * @param e the event that characterizes the change.
   */
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode treenode = (DefaultMutableTreeNode)this.getLastSelectedPathComponent();
    if(treenode == null)
    {
      return;
    }
    else if(!treenode.isLeaf())
    {
      for(int i = 0; i<navListeners.size(); i++)
      {
        navListeners.elementAt(i).lostSelection(_currSelected);
      }
      _currSelected = null;
      return;
    }
    INavigatorItem newselection = (INavigatorItem)treenode.getUserObject();
    if(newselection == null)
    {
      return;
    }
    
    if(_currSelected != newselection)
    {
      for(int i = 0; i<navListeners.size(); i++)
      {
        navListeners.elementAt(i).lostSelection(_currSelected);
        navListeners.elementAt(i).gainedSelection(newselection);
      }
      _currSelected = newselection;
    }
  }
  
  
  /**
   * returns a renderer for this object
   */
  public Component getRenderer(){
    return _renderer;
  }
  
  /**
   * the cell renderer for this tree
   */
  private class CustomTreeCellRenderer extends DefaultTreeCellRenderer{
    private String    _filename;
    private ImageIcon _javaIcon;
    private ImageIcon _advancedMIcon;
    private ImageIcon _advancedIcon;
    private ImageIcon _intermediateMIcon;
    private ImageIcon _intermediateIcon;
    private ImageIcon _elementaryMIcon;
    private ImageIcon _elementaryIcon;
    private ImageIcon _otherIcon;
    private ImageIcon _javaMIcon;
    private ImageIcon _otherMIcon;
    
    /**
     * simple constructor
     */
    public CustomTreeCellRenderer(){
      _javaIcon   = _getIconResource("JavaIcon.gif");
      _javaMIcon  = _getIconResource("JavaMIcon.gif");
      _elementaryIcon   = _getIconResource("ElementaryIcon.gif");
      _elementaryMIcon  = _getIconResource("ElementaryMIcon.gif");
      _intermediateIcon   = _getIconResource("IntermediateIcon.gif");
      _intermediateMIcon  = _getIconResource("IntermediateMIcon.gif");
      _advancedIcon   = _getIconResource("AdvancedIcon.gif");
      _advancedMIcon  = _getIconResource("AdvancedMIcon.gif");
      _otherIcon  = _getIconResource("OtherIcon.gif");
      _otherMIcon = _getIconResource("OtherMIcon.gif");
    }
    
    private ImageIcon _getIconResource(String name) {
      URL url = JTreeSortNavigator.class.getResource("icons/" + name);
      if (url != null) {
        return new ImageIcon(url);
      }
      return null;
    }
    
    /**
     * returns the component for a cell
     * @param tree
     */
    public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if(node.getUserObject() instanceof INavigatorItem){
              INavigatorItem doc = (INavigatorItem)(node.getUserObject());
              _filename = doc.toString();
              if (leaf) {
                if (_javaIcon != null && _filename.endsWith(".java  ")) {
                  setIcon(_javaIcon);
                }
                else if (_javaMIcon != null && _filename.endsWith(".java *")) {
                  setIcon(_javaMIcon);
                }
                else if (_elementaryIcon != null && _filename.endsWith(".dj0  ")) {
                  setIcon(_elementaryIcon);
                }
                else if (_elementaryMIcon != null && _filename.endsWith(".dj0 *")) {
                  setIcon(_elementaryMIcon);
                }
                else if (_intermediateIcon != null && _filename.endsWith(".dj1  ")) {
                  setIcon(_intermediateIcon);
                }
                else if (_intermediateMIcon != null && _filename.endsWith(".dj1 *")) {
                  setIcon(_intermediateMIcon);
                }
                else if (_intermediateIcon != null && _filename.endsWith(".dj2  ")) {
                  setIcon(_advancedIcon);
                }
                else if (_advancedMIcon != null && _filename.endsWith(".dj2 *")) {
                  setIcon(_advancedMIcon);
                }
                else if (_otherMIcon != null && _filename.endsWith(" *")) {
                  setIcon(_otherMIcon);
                }
                else if (_otherIcon != null) {
                  setIcon(_otherIcon);
                }
              }
            }else if(value instanceof String){
              // a directory
            }
            
            return this;
    }
  }

  
  /**
   * Selects the document at the x,y coordinate of the navigator pane and sets it to be
   * the currently active document.
   * @param x the x coordinate of the navigator pane
   * @param y the y coordinate of the navigator pane
   */
  public boolean selectDocumentAt(int x, int y) {
    TreePath path = getPathForLocation(x, y);
    if(path == null){
      return false;
    }else{
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      if(node.getUserObject() instanceof INavigatorItem){
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      }else{
        return false;
      }
    }
  }


//  public static ImageIcon getIcon(String name) {
//    URL url = JTreeSortNavigator.class.getResource(ICON_PATH + name);
//    if (url != null) {
//      return new ImageIcon(url);
//    }
//    return null;
//  }
}
