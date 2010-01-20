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

import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import edu.rice.cs.util.swing.Utilities;
//import edu.rice.cs.util.swing.RightClickMouseAdapter;

/** This class is an extension of JList that adds data shadowing the model embedded in a JList.
  * Since all changes to the model (except for the selected item!) must go through this interface,
  * we can support access to methods from non-event threads as long as these methods do not modify
  * the model.  However, all of the public methods that access and modify the model (the latter only running
  * in the event thread) must be atomic relative to each other, so synchronization is required in most
  * cases.
  * 
  * TODO: generify this class and IDocumentNavigator with respect to its element type once JList is. 
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
  private final ArrayList<INavigationListener<? super ItemT>> navListeners = new ArrayList<INavigationListener<? super ItemT>>();
  
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
        * @param e the event corresponding to the change
        */
      public void valueChanged(final ListSelectionEvent e) {
//        assert EventQueue.isDispatchThread();
//        Utilities.invokeLater( new Runnable() {
//          public void run() {
        if (!e.getValueIsAdjusting() && !_model.isEmpty()) {
          @SuppressWarnings("unchecked") final ItemT newItem = (ItemT) getSelectedValue();
//              final int newIndex = getSelectedIndex();
          if (_current != newItem) {                                
            final ItemT oldItem = _current;                                
            NodeData<ItemT> oldData = new NodeData<ItemT>() {
              public <Ret> Ret execute(NodeDataVisitor<? super ItemT, Ret> v, Object... p) { 
                return v.itemCase(oldItem, p); 
              }
            };
            NodeData<ItemT> newData = new NodeData<ItemT>() {
              public <Ret> Ret execute(NodeDataVisitor<? super ItemT, Ret> v, Object... p) { 
                return v.itemCase(newItem, p); 
              }
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
//          }
//        });
      }
    });
    
    _renderer = new CustomListCellRenderer();
    _renderer.setOpaque(true);
    this.setCellRenderer(_renderer);
  }
  
  /** Adds the document doc to this navigator.  Should only be executed in event thread.
    * @param doc the document to add
    */
  public void addDocument(ItemT doc) { synchronized(_model) { _model.addElement(doc); } }
  
  /** Adds the document to this navigator and ignores the specified path.  Should only be
    * executed in event thread.
    * @param doc the document to add -- assumed to be of type T
    * @param path  unused parameter in this class 
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
    * @param doc the document to reference from
    * @return the document after doc in the list; if doc is the last document, returns doc
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
    * @param doc to reference from
    * @return the document which comes before doc in the list
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
    * @return the first document in the collection
    */
  public ItemT getFirst() { synchronized(_model) { return getFromModel(0); } }
  
  /** Gets the first document in the series.
    * @return the first document in the collection
    */
  public ItemT getLast() { synchronized(_model) { return getFromModel(_model.size() - 1); } }
  
  /** Returns the currently selected item, or null if none. */
  public ItemT getCurrent() { return _current; }
  
  /** Returns the model lock. */
  public Object getModelLock() { return _model; }
  
  /** Removes the document from the navigator.  Should only be executed in event thread.
    * @param doc the document to remove
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
    * display to reflect any changes made in the model.  Should only be executed in event thread.
    * @param doc the docment to be refreshed
    * @throws IllegalArgumentException if this navigator contains no document that is equal to the passed document.
    */
  public void refreshDocument(ItemT doc, String path) {
    synchronized(_model) {
      removeDocument(doc);
      addDocument(doc);
    }
  }
  
  /** Sets the specified document as selected in the navigator.  Only executes in event thread.
    * @param doc the document to select
    */
  public void selectDocument(ItemT doc) { 
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    boolean found;
//    synchronized(_model) {
    if (_current == doc) return; // doc is already _current (the active doc)
    found = _model.contains(doc);
//    }
    if (found) setSelectedValue(doc, true);   
//        _current = doc;  // already done by ListSelectionEvent listener created in init()
//    }
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
  public ArrayList<ItemT> getDocuments() { 
    synchronized(_model) {
//    Cast forced by lousy generic typing of DefaultListModel in Java 1.5
      @SuppressWarnings("unchecked") Enumeration<ItemT> items = (Enumeration<ItemT>) _model.elements();
      ArrayList<ItemT> result = new ArrayList<ItemT>(_model.size());
      while (items.hasMoreElements()) result.add(items.nextElement());
      return result;                               
    }
  }
  
  /** Returns all the <code>IDocuments</code> contained in the specified bin. Always empty.
    * @param binName name of bin
    * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
    */
  public ArrayList<ItemT> getDocumentsInBin(String binName) { return new ArrayList<ItemT>(0); }
  
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
    * @param algo the visitor to execute
    * @param input the input to run on the visitor
    */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<ItemT, InType, ReturnType> algo, InType input) {
    return algo.forList(this, input);
  }
  
  /** Returns a Container representation of this navigator */
  public Container asContainer() { return this; }
  
  /** Selects the document at the x,y coordinate of the navigator pane and sets it to be
    * the currently active document.  Should only be called from event-handling thread.
    * @param x the x coordinate of the navigator pane
    * @param y the y coordinate of the navigator pane
    */
  public boolean selectDocumentAt(final int x, final int y) {
    synchronized(_model) {
      final int idx = locationToIndex(new java.awt.Point(x,y));
      java.awt.Rectangle rect = getCellBounds(idx, idx);
      if (rect.contains(x, y)) {
        selectDocument(getFromModel(idx));
        return true;
      }
      return false;
    }
  }
  
  /** Returns true if the item at the x,y coordinate of the navigator pane is currently selected.
    * Always false for JListSortNavigator
    * Only runs in event thread.
    * @param x the x coordinate of the navigator pane
    * @param y the y coordinate of the navigator pane
    * @return true if the item is currently selected
    */
  public boolean isSelectedAt(int x, int y) { return false;
//    synchronized(_model) {
//      final int idx = locationToIndex(new java.awt.Point(x,y));
//      if (idx == -1) return false;
//      return isSelectedIndex(idx);
//    }
  }
  
  /** @return the renderer for this object. */
  public Component getRenderer(){ return _renderer; }
  
  /** @return the number of selected items. Always 1 for JListNavigator */
  public int getSelectionCount() { return 1; } // return getSelectedIndices().length; }
  
  /** @return true if at least one group of INavigatorItems is selected; always false for JListNavigator */
  public boolean isGroupSelected() { return false; }
  
  /** @return the number of groups selected. Always 0 for JListSortNavigator */
  public int getGroupSelectedCount() { return 0; }
  
  /** @return the folders currently selected. Always empty for JListSortNavigator */
  public java.util.List<File> getSelectedFolders() { return new ArrayList<File>(); }
  
  /** @return true if at least one document is selected; always true for JListNavigator */
  public boolean isDocumentSelected() { return true; }
  
  /** @return the number of documents selected. Same as getSelectionCount for JListSortNavigator. */
  public int getDocumentSelectedCount() { return getSelectionCount(); }
  
  /** @return the documents currently selected. Only runs in event thread. */
  @SuppressWarnings("unchecked") public java.util.List<ItemT> getSelectedDocuments() {
//    Object[] selected = getSelectedValues();
//    ArrayList<ItemT> l = new ArrayList<ItemT>(selected.length);
//    for (Object o: selected) { l.add((ItemT)o); }
    ArrayList<ItemT> l = new ArrayList<ItemT>(1);
    l.add((ItemT)getSelectedValue());
    return l;
  }
  
  /** Returns true if the root is selected. Only runs in event thread. */
  public boolean isRootSelected() { return false; }
  
  /** @return true if the INavigatorItem is in the selected group, if a group is selected. */
  public boolean isSelectedInGroup(ItemT i) { return false; }
  
  public void addTopLevelGroup(String name, INavigatorItemFilter<? super ItemT> f) { /* noop */ }
  
  public boolean isTopLevelGroupSelected() { return false; }
  
  /** Returns the names of the top level groups that the selected items descend from.
    * Always throws a GroupNotSelectedException for JListSortNavigator
    */
  public java.util.Set<String> getNamesOfSelectedTopLevelGroup() throws GroupNotSelectedException{
    throw new GroupNotSelectedException("A top level group is not selected");
  }
  
  /** Since in the JListNavigator it is impossible to select anything but an INavigatorItem,
    * this method doesn't need to do anything.  See JTreeSortNavigator and IDocumentNavigator.
    */
  public void requestSelectionUpdate(ItemT doc) { /* nothing */ }
  
//  /** Notify this ListModel that doc has changed and may need updating (if it has changed
//    * from modified to unmodified). Should only be performed in the event thread
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
      * @param list
      * @param value
      * @param index
      * @param isSelected
      * @param hasFocus
      */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
      
      super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
      setText(((INavigatorItem)value).getName());
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
