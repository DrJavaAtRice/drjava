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
import java.util.*;
import edu.rice.cs.util.*;

public class JTreeNavigator extends JTree implements IAWTContainerNavigatorActor, TreeSelectionListener {
  
  private HashMap<INavigatorItem, DefaultMutableTreeNode> _doc2node =
    new HashMap<INavigatorItem, DefaultMutableTreeNode>();
  
  private BidirectionalHashMap<String, DefaultMutableTreeNode> _path2node = 
    new BidirectionalHashMap<String, DefaultMutableTreeNode>();
  
  private DefaultMutableTreeNode _root;
  
  private DefaultTreeModel _model;
  
  private INavigatorItem _currSelected;
  
  private DefaultMutableTreeNode _nonProjRoot = new DefaultMutableTreeNode("[External Files]");
  
  private boolean _hasnonprojfilesopen = false;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  private Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  protected DefaultTreeCellRenderer _renderer;

  public JTreeNavigator(String name) {
    super(new DefaultTreeModel(new DefaultMutableTreeNode(name)));
    
    this.addTreeSelectionListener(this);
    
    _model = (DefaultTreeModel) this.getModel();
    _root = (DefaultMutableTreeNode) _model.getRoot();
      
    _renderer = new DefaultTreeCellRenderer();
    _renderer.setOpaque(true);
    this.setCellRenderer(_renderer);
    
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
   
    
    if(!_hasnonprojfilesopen)
    {
      _model.insertNodeInto(_nonProjRoot, _root, _root.getChildCount());
    }
    
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(doc.getName());
    node.setUserObject(doc);
    //_root.add(node);
    _model.insertNodeInto(node, _nonProjRoot, _nonProjRoot.getChildCount());
    this.expandPath(new TreePath(_nonProjRoot.getPath()));
    _doc2node.put(doc, node);
    _hasnonprojfilesopen = true;
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
   
    if (!path.startsWith(_topLevelPath))
    {
      addDocument(doc);
      return;
    }   
    // if the file is at the top level
    if (path.equals(_topLevelPath)) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(doc.getName());
      node.setUserObject(doc);
      _model.insertNodeInto(node, _root, _root.getChildCount());
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
        //System.out.println("path2node does not contain pathSoFar");
        // make a new node
        thisNode = new DefaultMutableTreeNode(element);
        //System.out.println("attempting to insert " + thisNode + " to " + lastNode);
        _model.insertNodeInto(thisNode, lastNode, lastNode.getChildCount());
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
    
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(doc);
    _doc2node.put(doc, child);
    _model.insertNodeInto(child, lastNode, lastNode.getChildCount());
    this.expandPath(new TreePath(lastNode.getPath()));
    child.setUserObject(doc);
  }
  
  private String _topLevelPath = "";
  
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
    DefaultMutableTreeNode toRemove = _doc2node.get(doc);
    //toRemove.removeFromParent();
   /*
    if(toRemove.getSiblingCount() == 1 && toRemove.getParent().getParent() != null)
    {
       
        _model.removeNodeFromParent((MutableTreeNode)toRemove.getParent()); 
        _path2node.removeKey((DefaultMutableTreeNode)toRemove.getParent());
        
        
    }
    */
    _model.removeNodeFromParent(toRemove);
    _doc2node.remove(doc);
   
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
    return doc;
  }
  
  /* sets the input document to be active */
  public void setActiveDoc(INavigatorItem doc){
    DefaultMutableTreeNode node = _doc2node.get(doc);
    if(this.contains(doc)){
      TreeNode[] nodes = node.getPath();
      TreePath path = new TreePath(nodes);
      this.expandPath(path);
      this.setSelectionPath(path);
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
  
  public Collection<INavigationListener> getNavigatorListeners()
  {
    return navListeners;
  }
  
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
}
