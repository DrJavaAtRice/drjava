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
 * 
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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import edu.rice.cs.util.UnexpectedException;

//import edu.rice.cs.drjava.ui.RightClickMouseAdapter;

/** This class is an extension of JList that adds a shadow version of the associated list model.
 *  Since all of the new fields are conceptually associated with the model rather than the GUI
 *  JList object (the view), the only synchronization constraints are exactly those of the view
 *  associated with JList content (superclass fields) of this.
 */


class JListNavigator extends JList implements IDocumentNavigator {
  
  /** The list model (extending AbstractListModel) for this JList. */
  protected DefaultListModel _model;
  
  /** The current selection value.  A cached copy of getSelectedValue(). */
  private INavigatorItem current = null;
  
  /** The cell renderer for this JList */
  private DefaultListCellRenderer _renderer;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  private final Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  /** Standard constructors */
  
  public JListNavigator() { 
    super();
    init(new DefaultListModel());
  }

  public void init(DefaultListModel m) {
    _model = m;
    setModel(m);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addListSelectionListener(new ListSelectionListener() {
      /**
       * called when the list value has changed
       * @param e the event corresponding to the change
       */
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && !_model.isEmpty()) {
          final INavigatorItem newItem = (INavigatorItem) getSelectedValue();
          if (current != newItem) {
            final INavigatorItem oldItem = current;
            NodeData oldData = new NodeData() {
              public <T> T execute(NodeDataVisitor<T> v) {
                return v.itemCase(oldItem);
              }
            };
            NodeData newData = new NodeData() {
              public <T> T execute(NodeDataVisitor<T> v) {
                return v.itemCase(newItem);
              }
            };
            for(INavigationListener listener: navListeners) {
              if (oldItem != null) listener.lostSelection(oldData);
              if (newItem != null) listener.gainedSelection(newData);
            }
            current = newItem;
          }
        }
      }
    });
    
    _renderer = new DefaultListCellRenderer();
    _renderer.setOpaque(true);
    this.setCellRenderer(_renderer);
  }
  
  /**
   * Adds the document to this navigator.
   * @param doc the document to add
   */
  public void addDocument(INavigatorItem doc) { _model.addElement(doc); }
  
  /**
   * Adds the document to this navigator with the specified path.
   * @param doc the document to add
   * @param path the path the document should be added to
   */
  public void addDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    _model.addElement(doc);
  }
  
  /**
   * Gets the next document after doc in the series.
   * @param doc the document to reference from
   * @return the document after doc in the list; if doc is the last
   *  document, returns doc
   */
  public INavigatorItem getNext(INavigatorItem doc) { 
    synchronized (_model) {
      int i = _model.indexOf(doc);
      if (i == -1)
        throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
      if ( i + 1 == _model.size()) return doc;
      return (INavigatorItem) _model.get(i + 1);
    }
  }
  
  /**
   * Gets the previous document in the series.
   * @param doc to reference from
   * @return the document which comes before doc in the list
   */
  public INavigatorItem getPrevious(INavigatorItem doc) {  
    synchronized (_model) {
      int i = _model.indexOf(doc);
      if ( i == -1 )
        throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
      if ( i == 0) return doc;
      return (INavigatorItem) _model.get(i - 1);
    }
  }
  
  /**
   * Removes the document from the navigator.
   * @param doc the document to remove
   */
  public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException {
    synchronized (_model) {
      // System.err.println("removing from old list " + doc);
      int i = _model.indexOf(doc);
      if( i == -1 )
        throw new IllegalArgumentException("Document " + doc + " not found in Document Navigator");
      INavigatorItem item = (INavigatorItem) _model.remove(i);
      return item;
    }
  }


  /**
   * Resets a given <code>INavigatorItem<code> in the tree.  This may affect the
   * placement of the item or its display to reflect any changes made in the model.
   * @param doc the docment to be refreshed
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public void refreshDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    synchronized (_model) {
      removeDocument(doc);
      addDocument(doc);
    }
  }
  
  /**
   * Sets the specified document as selected.  Should only be called from event-handling thread.
   * @param doc the document to select
   */
  public void setActiveDoc(INavigatorItem doc) {
    synchronized(_model) {
      if(_model.contains(doc)) {
        setSelectedValue(doc, true);
        // current = doc;  // already done by ListSelectionEvent listener created in init()
      }
    }
  }
  
  /**
   * returns whether or not the navigator contains the document
   * @param doc the document to find
   * @return true if this list contains doc (using identity as equality
   * measure), false if not.
   */
  public boolean contains(INavigatorItem doc) { return _model.contains(doc); }
  
  /**
   * @return an Enumeration of the documents in this list (ordering is
   * consistent with getNext() and getPrev()).
   * 
   * This cast in this method required to work around the stupid partial generification of DefaultListModel in Java 1.5.
   * The class should be generic: DefaultListModel<T> { ... Enumeration<T> elements() {...} ... } instead of 
   * DefaultListModel { ... Enumeration<?> elements() {...} ... }.
   */
  public Enumeration<INavigatorItem> getDocuments() { return (Enumeration) _model.elements(); }  
  
  /**
   * returns the number of documents in the navigator
   * @return the number of documents
   */
  public int getDocumentCount() { return _model.size(); }
  
  /**
   * returns whether or not the navigator is empty
   * @return boolean whether or not the navigator is empty
   */
  public boolean isEmpty() { return _model.isEmpty(); }
  
  /**
   * Add listener to the collection of listeners
   * @param listener
   */
  public void addNavigationListener(INavigationListener listener) {
    navListeners.add(listener);
  }
  
  /**
   * Unregister the listener listener
   * @param listener
   */
  public void removeNavigationListener(INavigationListener listener) {
    navListeners.remove(listener);
  }
  
  /**
   * returns all the navigator listeners
   * @return the navigator listeners
   */
  public Collection<INavigationListener> getNavigatorListeners() { return navListeners; }
  
  /**
   * clears the navigator and removes all documents
   */
  public void clear() { _model.clear(); }
  
  /**
   * executes the list case of the visitor
   * @algo the visitor to execute
   * @input the input to run on the visitor
   */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input) {
    return algo.forList(this, input);
  }
  
  /**
   * returns a Container representation of this navigator
   */
  public Container asContainer() { return this; }
  
  /**
   * Selects the document at the x,y coordinate of the navigator pane and sets it to be
   * the currently active document.  Should only be called from event-handling thread.
   * @param x the x coordinate of the navigator pane
   * @param y the y coordinate of the navigator pane
   * 
   */
  public boolean selectDocumentAt(final int x, final int y) {
    final int idx = locationToIndex(new java.awt.Point(x,y));
    java.awt.Rectangle rect = getCellBounds(idx, idx);
    if (rect.contains(x, y)) {
      setActiveDoc((INavigatorItem) _model.getElementAt(idx));
      return true;
    }
    return false;
  }
  
  /**
   * returns the currently selected item, or null if none
   */
  public INavigatorItem getCurrentSelectedLeaf() { return current; }
  
  /**
   * returns a renderer for this object
   */
  public Component getRenderer(){ return _renderer; }
  
  /**
   * @return true if a group if INavigatorItems selected
   */
  public boolean isGroupSelected() { return false; }
  
  /**
   * @return true if the INavigatorItem is in the selected group, if a group is selected
   */
  public boolean isSelectedInGroup(INavigatorItem i) { return false; }
  
  public void addTopLevelGroup(String name, INavigatorItemFilter f) { /* noop */ }
  
  public boolean isTopLevelGroupSelected() { return false; }
  
  public String getNameOfSelectedTopLevelGroup() throws GroupNotSelectedException{
    throw new GroupNotSelectedException("A top level group is not selected");
  }
  
  /**
   * Since in the JListNavigator it is impossible to select anything but an INavigatorItem,
   * this method doesn't need to do anything.
   */
  public void requestSelectionUpdate(INavigatorItem ini) { /* nothing */ }
  
  public String toString() { return "JListNavigator" + _model.toString(); }
  
}
