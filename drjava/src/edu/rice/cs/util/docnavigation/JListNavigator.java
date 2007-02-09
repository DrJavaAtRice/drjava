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
import edu.rice.cs.util.swing.Utilities;
//import edu.rice.cs.util.swing.RightClickMouseAdapter;

/** This class is an extension of JList that adds data shadowing the model embedded in a JList.
 *  Since all changes to the model (except for the selected item!) must go through this interface,
 *  we can support access to methods from non-event threads as long as these methods do not modify
 *  the model.  However, all of the public methods that access and modify the model (the latter only running
 *  in the event thread) must be atomic relative to each other, so synchronization is required in most
 *  cases.
 * 
 *  TODO: generify this class and IDocumentNavigator with respect to its element type once JList is. 
 */

class JListNavigator<ItemT extends INavigatorItem> extends JList implements IDocumentNavigator<ItemT> {
  
  /** The list model (extending AbstractListModel) for this JList. */
  protected DefaultListModel _model;
  
  /** The current selection value.  A cached copy of getSelectedValue(). */
  private volatile ItemT _current = null;
  
//  /** The index of _current */
//  private int _currentIndex = -1;
  
  /** The cell renderer for this JList */
  private volatile CustomListCellRenderer _renderer;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  private final Vector<INavigationListener<? super ItemT>> navListeners = new Vector<INavigationListener<? super ItemT>>();
  
  /** Standard constructor. */
  public JListNavigator() { 
    super();
    init(new DefaultListModel());
  }
  
  private void init(DefaultListModel m) {
    _model = m;
    setModel(m);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addListSelectionListener(new ListSelectionListener() {
      /** Called when the list value has changed. Should only run in the event thread.
       *  @param e the event corresponding to the change
       */
      public void valueChanged(final ListSelectionEvent e) {
        Utilities.invokeLater( new Runnable() {
          public void run() {
            if (!e.getValueIsAdjusting() && !_model.isEmpty()) {
              @SuppressWarnings("unchecked") final ItemT newItem = (ItemT) getSelectedValue();
//              final int newIndex = getSelectedIndex();
              if (_current != newItem) {                                
                final ItemT oldItem = _current;                                
                NodeData<ItemT> oldData = new NodeData<ItemT>() {
                  public <Ret> Ret execute(NodeDataVisitor<? super ItemT, Ret> v, Object... p) { return v.itemCase(oldItem, p); }
                };
                NodeData<ItemT> newData = new NodeData<ItemT>() {
                  public <Ret> Ret execute(NodeDataVisitor<? super ItemT, Ret> v, Object... p) { return v.itemCase(newItem, p); }
                };
                for(INavigationListener<? super ItemT> listener: navListeners) {
                  if (oldItem != null) listener.lostSelection(oldData, isNextChangeModelInitiated());
                  if (newItem != null) listener.gainedSelection(newData, isNextChangeModelInitiated());
                }
                setNextChangeModelInitiated(false);
                _current = newItem;
//                _currentIndex = newIndex;
              }
            }
          }
        });
      }
    });
    
    _renderer = new CustomListCellRenderer();
    _renderer.setOpaque(true);
    this.setCellRenderer(_renderer);
  }
  
  /** Adds the document doc to this navigator.  Should only be executed in event thread.
   *  @param doc the document to add
   */
  public void addDocument(ItemT doc) { synchronized(_model) { _model.addElement(doc); } }
  
  /** Adds the document to this navigator and ignores the specified path.  Should only be
   *  executed in event thread.
   *  @param doc the document to add -- assumed to be of type T
   *  @param path  unused parameter in this class 
   */
  public void addDocument(ItemT doc, String path) { addDocument(doc); }
  
  /** A typesafe version of {@code _model.get(i)}.  This is a workaround for the
   * non-generic implementation of DefaultListModel, and should be removed once that
   * is fixed.
   */
  protected ItemT getFromModel(int i) {
    @SuppressWarnings("unchecked") ItemT result = (ItemT) _model.get(i);
    return result;
  }
  
  /** Gets the next document after doc in the series.
   *  @param doc the document to reference from
   *  @return the document after doc in the list; if doc is the last
   *  document, returns doc
   */
  public ItemT getNext(ItemT doc) { 
    synchronized(_model) {
      int i = _model.indexOf(doc);
      if (i == -1)
        throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
      if ( i + 1 == _model.size()) return doc;
      
      return getFromModel(i + 1);
    }
  }
  
  /** Gets the previous document in the series.
   *  @param doc to reference from
   *  @return the document which comes before doc in the list
   */
  public ItemT getPrevious(ItemT doc) {  
    synchronized(_model) {
      int i = _model.indexOf(doc);
      if (i == -1)
        throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
      if (i == 0) return doc;
      return getFromModel(i - 1);
    }
  }
  
  /** Gets the first document in the series.
   *  @return the first document in the collection
   */
  public ItemT getFirst() { synchronized(_model) { return getFromModel(0); } }
  
  /** Gets the first document in the series.
   *  @return the first document in the collection
   */
  public ItemT getLast() { synchronized(_model) { return getFromModel(_model.size() - 1); } }
  
  /** Returns the currently selected item, or null if none. */
  public ItemT getCurrent() { return _current; }
  
  /** Returns the model lock. */
  public Object getModelLock() { return _model; }
  
  /** Removes the document from the navigator.  Should only be executed in event thread.
   *  @param doc the document to remove
   */
  public ItemT removeDocument(ItemT doc) {
    synchronized(_model) {
      // System.err.println("removing from old list " + doc);
      int i = _model.indexOf(doc);
      if( i == -1 )
        throw new IllegalArgumentException("Document " + doc + " not found in Document Navigator");
      ItemT result = getFromModel(i);
      _model.remove(i);
      return result;
    }
  }

  /** Resets a given <code>INavigatorItem<code> in the tree.  This may affect the placement of the item or its
   *  display to reflect any changes made in the model.  Should only be executed in event thread.
   *  @param doc the docment to be refreshed
   *  @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public void refreshDocument(ItemT doc, String path) {
    synchronized(_model) {
      removeDocument(doc);
      addDocument(doc);
    }
  }
  
  /** Sets the specified document as selected.  Should only be called from event thread.
   *  @param doc the document to select
   */
  public void setActiveDoc(ItemT doc) { 
    synchronized(_model) {
      if (_current == doc) return; // doc is already _current (the active doc)
      if (_model.contains(doc)) {
        setSelectedValue(doc, true);   
//        _current = doc;  // already done by ListSelectionEvent listener created in init()
      }
    }
  }
    
  /** Returns whether or not the navigator contains the document
    * @param doc the document to find
    * @return true if this list contains doc (using identity as equality measure), false if not.
    */
  public boolean contains(ItemT doc) { 
    synchronized(_model) { return _model.contains(doc); }
  }
  
  /** @return an Enumeration of the documents in this list (ordering is consistent with getNext() and getPrev()).
    * This cast in this method required to work around the stupid partial generification of DefaultListModel in Java 1.5.
    * The class should be generic: DefaultListModel<T> { ... Enumeration<T> elements() {...} ... } instead of 
    * DefaultListModel { ... Enumeration<?> elements() {...} ... }.
    */
  public Enumeration<ItemT> getDocuments() { 
    synchronized(_model) {
//    Cast forced by lousy generic typing of DefaultListModel in Java 1.5
      @SuppressWarnings("unchecked") Enumeration<ItemT> result = (Enumeration<ItemT>) _model.elements();
      return result;  
    }
  }
  
  /** @return the number of documents in the navigator. */
  public int getDocumentCount() { return _model.size(); }
  
  /** @return whether or not the navigator is empty. */
  public boolean isEmpty() { return _model.isEmpty(); }
  
  /** Adds listener to the collection of listeners.
    * @param listener
    */
  public void addNavigationListener(INavigationListener<? super ItemT> listener) { 
    synchronized(_model) { navListeners.add(listener); }
  }
  
  /** Unregisters the listener listener
    * @param listener
    */
  public void removeNavigationListener(INavigationListener<? super ItemT> listener) { 
    synchronized(_model) { navListeners.remove(listener); }
  }
  
  /** @return the navigator listeners. */
  public Collection<INavigationListener<? super ItemT>> getNavigatorListeners() { return navListeners; }
  
  /** Clears the navigator and removes all documents. Should only be executed from event thread. */
  public void clear() { synchronized(_model) { _model.clear(); } }
  
  /** Executes the list case of the visitor.
   *  @param algo the visitor to execute
   *  @param input the input to run on the visitor
   */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<ItemT, InType, ReturnType> algo, InType input) {
    return algo.forList(this, input);
  }
  
  /** Returns a Container representation of this navigator */
  public Container asContainer() { return this; }
  
  /** Selects the document at the x,y coordinate of the navigator pane and sets it to be
   *  the currently active document.  Should only be called from event-handling thread.
   *  @param x the x coordinate of the navigator pane
   *  @param y the y coordinate of the navigator pane
   * 
   */
  public boolean selectDocumentAt(final int x, final int y) {
    synchronized(_model) {
      final int idx = locationToIndex(new java.awt.Point(x,y));
      java.awt.Rectangle rect = getCellBounds(idx, idx);
      if (rect.contains(x, y)) {
        setActiveDoc(getFromModel(idx));
        return true;
      }
      return false;
    }
  }
    

  /** @return the renderer for this object. */
  public Component getRenderer(){ return _renderer; }
  
  /** @return true if a group if INavigatorItems selected. */
  public boolean isGroupSelected() { return false; }
  
  /** @return true if the INavigatorItem is in the selected group, if a group is selected. */
  public boolean isSelectedInGroup(ItemT i) { return false; }
  
  public void addTopLevelGroup(String name, INavigatorItemFilter<? super ItemT> f) { /* noop */ }
  
  public boolean isTopLevelGroupSelected() { return false; }
  
  public String getNameOfSelectedTopLevelGroup() throws GroupNotSelectedException{
    throw new GroupNotSelectedException("A top level group is not selected");
  }
  
  /** Since in the JListNavigator it is impossible to select anything but an INavigatorItem,
   *  this method doesn't need to do anything.  See JTreeSortNavigator and IDocumentNavigator.
   */
  public void requestSelectionUpdate(ItemT doc) { /* nothing */ }
  
//  /** Notify this ListModel that doc has changed and may need updating (if it has changed
//   *  from modified to unmodified). Should only be performed in the event thread
//   */
//  public void activeDocumentModified() {
//    synchronized(_model) {
//      int current = _currentIndex;
//      fireSelectionValueChanged(current, current, false); 
//    }
//  }
//  
  public String toString() { synchronized(_model) { return _model.toString(); } }
  
  /** The cell renderer for this list. */
  private static class CustomListCellRenderer extends DefaultListCellRenderer {
    
    /** Rreturns the renderer component for a cell
     *  @param list
     *  @param value
     *  @param index
     *  @param isSelected
     *  @param hasFocus
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {

      super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
      setText(((INavigatorItem)value).getName());
//      repaint();  // appears to be required to repaint the text for this list item; inconsistent with JTree analog
      return this;
    }
  }
  
  /** Marks the next selection change as model-initiated (true) or user-initiated (false; default). */
  public void setNextChangeModelInitiated(boolean b) {
    putClientProperty(MODEL_INITIATED_PROPERTY_NAME, b?Boolean.TRUE:null);
  }
  
  /** @return whether the next selection change is model-initiated (true) or user-initiated (false). */
  public boolean isNextChangeModelInitiated() {
    return getClientProperty(MODEL_INITIATED_PROPERTY_NAME) != null;
  }
}
