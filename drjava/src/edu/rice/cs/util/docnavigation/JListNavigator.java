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

class JListNavigator extends JList implements IAWTContainerNavigatorActor, ListSelectionListener {
  //private final DefaultListModel _docs = new DefaultListModel();
  
  protected Vector<INavigatorItem> _docs = new Vector<INavigatorItem>();
  
  protected INavigatorItem currentselected = null;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  protected Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  public JListNavigator() 
  {
    super();
    this.setListData(_docs);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.addListSelectionListener(this);
    /*
    listmodel.addListDataListener(new ListDataListener(){
      public void contentsChanged(ListDataEvent e)
      {
      }
      
      public void intervalAdded(ListDataEvent e){
        int i = e.getIndex0();
        _docs.add(i,  listmodel.getElementAt(i));
      }
      
      public void intervalRemoved(ListDataEvent e){
        int i = e.getIndex0();
        _docs.remove(i);
      }
    });
    setModel(_docs);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        if( !e.getValueIsAdjusting() ) {
          // we enforce that you can only select one document
          int index = getSelectedIndex();
          if( _docs.size() <= 0 || index == -1 ) return;
          INavigatorItem doc = (INavigatorItem)_docs.get(index);
          
          // no need for error checking, shouldn't be possible for index to be invalid here
          Iterator<INavigationListener> listeners = navListeners.iterator();   
          while(listeners.hasNext()) {
            INavigationListener l = listeners.next();
            l.gainedSelection(doc);
          }
        }
      }
    });
    
    this.addMouseListener(new MouseAdapter() {
      
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          setSelectedIndex(locationToIndex(e.getPoint()));
        }
      }
      
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          setSelectedIndex(locationToIndex(e.getPoint()));
        }
      }
      
    });
    
    this.setCellRenderer(new InternalCellRenderer(this));
    */
  }
  
  public void addDocument(INavigatorItem doc) {
    _docs.add(doc);
     this.setListData(_docs);
    //System.out.println("%" + doc == _docs.elementAt(0));
    //System.out.println(doc + " got index " + _docs.indexOf(doc) + "(size was " + _docs.size() + ")");
  }
  
  public void addDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    _docs.add(doc);
     this.setListData(_docs);
  }
  
  /**
   * @return the document which comes after doc in the list
   * @param doc
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
   * @return the document which comes before doc in the list
   * @param doc
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
  
  public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException {
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
  
  public void setActiveDoc(INavigatorItem doc){
    if(this.contains(doc)){
        this.setSelectedValue(doc, true);
    }
  }
  

  
  public void setTopLevelPath(String path)
  {
  }
  
  /**
   * @return true if this list contains doc (using identity as equality
   * measure), false if not.
   * @param doc
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
  
  public int getDocumentCount()
  {
    return _docs.size();
  }
  
  public boolean isEmpty()
  {
    return _docs.isEmpty();
  }
  
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
  
  public Collection<INavigationListener> getNavigatorListeners()
  {
      return navListeners;
  }
  
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input) {
    return algo.forList(this, input);
  }
  
  private class InternalCellRenderer extends JLabel implements ListCellRenderer {
    private JList _list = null;
    
    public InternalCellRenderer(JList list) {
      _list = list;
      setOpaque(true);
    }
    
    public Component getListCellRendererComponent(
                                                  JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      setFont(_list.getFont());
      setText(value.toString());
      setBackground(isSelected ? _list.getSelectionBackground() : _list.getBackground());
      setForeground(isSelected ? _list.getSelectionForeground() : _list.getForeground());
      return this;
    }
  }
  
  public Container asContainer()
  {
    return this;
  }
  
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
}
