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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
//import edu.rice.cs.drjava.model.*;
//import edu.rice.cs.drjava.ui.RightClickMouseAdapter;

class JListNavigator extends JList implements IDocumentNavigator, ListSelectionListener {
  /**
   * the documents in this navigator
   */
  protected Vector<INavigatorItem> _docs = new Vector<INavigatorItem>();
  
  /**
   * the currently selected document, or null if none
   */
  protected INavigatorItem currentselected = null;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  protected Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  /**
   * the cell renderer for this JList
   */
  protected DefaultListCellRenderer _renderer;
  
  /**
   * standard constructor
   */
  public JListNavigator() 
  {
    super();
    this.setListData(_docs);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.addListSelectionListener(this);
    
    
    _renderer = new DefaultListCellRenderer();
    _renderer.setOpaque(true);
    this.setCellRenderer(_renderer);
    
  }
  
  /**
   * adds the document to this navigator
   * @param doc the document to add
   */
  public void addDocument(INavigatorItem doc) {
    _docs.add(doc);
     this.setListData(_docs);
    //System.out.println("%" + doc == _docs.elementAt(0));
    //System.out.println(doc + " got index " + _docs.indexOf(doc) + "(size was " + _docs.size() + ")");
  }
  
  /**
   * adds the document to this navigator to the specified path
   * @param doc the document to add
   * @param path the path the document should be added to
   */
  public void addDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    _docs.add(doc);
     this.setListData(_docs);
  }
  
  /**
   * gets the next document in the series
   * @param doc the document to reference from
   * @return the document which comes after doc in the list
   */
  public INavigatorItem getNext(INavigatorItem doc) {
    int i = _docs.indexOf(doc);
    //System.out.println("Current size is " + _docs.size() + " and passed index is " + i);
    if( i == -1 ) {
      throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
    }
    else if ((i + 1) < _docs.size()){
      return (INavigatorItem)_docs.get(i + 1);
    }
    else {
      return doc;
    }
  }
  
  /**
   * gets the previous document in the series
   * @param doc to reference from
   * @return the document which comes before doc in the list
   */
  public INavigatorItem getPrevious(INavigatorItem doc) {
    int i = _docs.indexOf(doc);
    
    if( i == -1 ) {
      throw new IllegalArgumentException("No such document " + doc.toString() + " found in collection of open documents");
    }
    else if ((i - 1) >= 0){
      return (INavigatorItem)_docs.get(i - 1);
    }
    else {
      return doc;
    }
  }
  
  /**
   * removes the document from the navigator
   * @param doc the document to remove
   */
  public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException {
    System.err.println("removing from old list " + doc);
    int i = _docs.indexOf(doc);
    if( i == -1 ) {
      throw new IllegalArgumentException("Document " + doc + " not found in Document Navigator");
    }
    else {
      INavigatorItem next = null;
      if(currentselected == doc)
      {
        next = getNext(doc);
        if(next == doc)
        {
          next = null;
        }
      } 
      INavigatorItem tbr = _docs.remove(i);
      this.setListData(_docs);
      if(next != null)
      {
        this.setSelectedValue(next, true);
      }
      return tbr;
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
    int i = _docs.indexOf(doc);
    if( i == -1 ) {
      throw new IllegalArgumentException("Document " + doc + " not found in Document Navigator");
    }
    else {
      INavigatorItem tbr = _docs.remove(i);
      _docs.add(doc);
      this.setListData(_docs);
    }
  }
  
  /**
   * sets the document as selected
   * @param doc the document to select
   */
  public void setActiveDoc(INavigatorItem doc){
    if(this.contains(doc)){
        this.setSelectedValue(doc, true);
    }
  }
  

  
  /**
   * returns whether or not the navigator contains the document
   * @param doc the document to find
   * @return true if this list contains doc (using identity as equality
   * measure), false if not.
   */
  public boolean contains(INavigatorItem doc) {
    return (_docs.indexOf(doc) != -1 );
  }
  
  /**
   * @return an Enumeration of the documents in this list (ordering is
   * consistent with getNext() and getPrev()).
   */
  public Enumeration<INavigatorItem> getDocuments() {
    return _docs.elements();
  }
  
  /**
   * returns the number of documents in the navigator
   * @return the number of documents
   */
  public int getDocumentCount()
  {
    return _docs.size();
  }
  
  /**
   * returns whether or not the navigator is empty
   * @return boolean whether or not the navigator is empty
   */
  public boolean isEmpty()
  {
    return _docs.isEmpty();
  }
  
  /**
   * clears the navigator and removes all documents
   */
  public void clear()
  {
    _docs.clear();
    this.setListData(_docs);
  }
  
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
  public Collection<INavigationListener> getNavigatorListeners()
  {
      return navListeners;
  }
  
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
  public Container asContainer()
  {
    return this;
  }
  
  /**
   * called when the list value has changed
   * @param e the event corresponding to the change
   */
  public void valueChanged(ListSelectionEvent e)
  {
    if(!e.getValueIsAdjusting() && !_docs.isEmpty())
    {
      if(currentselected != this.getSelectedValue())
      {
        for(int i = 0; i<navListeners.size(); i++)
        {
          navListeners.elementAt(i).lostSelection(currentselected);
          if(this.getSelectedValue() != null)
          {
            navListeners.elementAt(i).gainedSelection((INavigatorItem)this.getSelectedValue());
          }
        }
        currentselected = (INavigatorItem)this.getSelectedValue();
      } 
    }
  }

  
  /**
   * Selects the document at the x,y coordinate of the navigator pane and sets it to be
   * the currently active document.
   * @param x the x coordinate of the navigator pane
   * @param y the y coordinate of the navigator pane
   */
  public boolean selectDocumentAt(int x, int y) {
    int idx = locationToIndex(new java.awt.Point(x,y));
    java.awt.Rectangle rect = getCellBounds(idx, idx);
    
    if(rect.contains(x, y)){
      setActiveDoc(_docs.get(idx));
      return true;
    }else{
      return false;
    }
  }



  /**
   * returns a renderer for this object
   */
  public Component getRenderer(){
    return _renderer;
  }
  
  /**
   * @return true if a group if INavigatorItems selected
   */
  public boolean isGroupSelected(){
    return false;
  }
  
  /**
   * @return true if the INavigatorItem is in the selected group, if a group is selected
   */
  public boolean isSelectedInGroup(INavigatorItem i){
    return false;
  }
  
  public void addTopLevelGroup(String name, INavigatorItemFilter f){
    // noop
  }

}
