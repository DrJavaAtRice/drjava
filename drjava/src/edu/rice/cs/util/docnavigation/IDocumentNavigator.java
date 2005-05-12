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

import java.util.*;
import java.awt.Container;

/**
 * <code>IDocumentNavigator</code> provides a framework through which
 * individual <code>IDocuments</code> can be navigated.
 */ 
public interface IDocumentNavigator extends IAWTContainerNavigatorActor{
  /** @return an AWT component which interacts with this document navigator */
  public Container asContainer();
  
  /** Adds an <code>IDocuemnt</code> to this navigator.
   *  @param doc the document to be added into this navigator.
   */
  public void addDocument(INavigatorItem doc);
  
  /** Adds an <code>INavigatorItem</code> into this navigator in a position relative to a given path.
   *  @param doc the document to be added into this navigator.
   *  @param path the relative path to insert this INavigatorItem at.
   */
  public void addDocument(INavigatorItem doc, String path);
  
  /** Returns the currently selected navigator item, or null if no navigator item is selected. */
  public INavigatorItem getCurrentSelectedLeaf();
  
  /** Removes a given <code>INavigatorItem<code> from this navigator. Removes all <code>INavigatorItem</code>s 
   *  from this navigator that are "equal" (<code>.equals(...)</code>) to the passed argument. Any of the 
   *  removed documents may be returned by this method.
   *  @param doc the docment to be removed
   *  @return doc a document removed from this navigator as a result of invoking this method.
   *  @throws IllegalArgumentException if this navigator contains no document equal to the passed document.
   */
  public <T extends INavigatorItem> T removeDocument(T doc);
  
  /** Resets a given <code>INavigatorItem<code> in the tree.  This may affect the placement of the item or its 
   *  display to reflect any changes made in the model.
   *  @param doc the docment to be refreshed
   *  @throws IllegalArgumentException if this navigator contains no document that is equal to the passed document.
   */
  public void refreshDocument(INavigatorItem doc, String path);
  
  /** Sets the active document as specified.
   *  @param doc the document to select
   */
  public void setActiveDoc(INavigatorItem doc);
  
  
  /** Impose an ordering on the documents in the navigator to support setActiveNextDocument()
   *  @return the INavigatorItem which comes after doc
   *  @param doc the INavigatorItem of interest
   */
  public <T extends INavigatorItem> T getNext(T doc);
  
  /** Impose an ordering on the documents in the navigator to support setActivePrevDocument()
   *  @return the INavigatorItem which comes before doc
   *  @param doc the INavigatorItem of interest
   */
  public <T extends INavigatorItem> T getPrevious(T doc);
  
  /** Tests to see if a given document is contained in this navigator.
   *  @param doc the document to test for containment.
   *  @return <code>true</code> if this contains a document "equal" (<code>.equals(...)</code> method)
   *          to the passed document, else <code>false</code>.
   */
  public boolean contains(INavigatorItem doc);
  
  /** Returns all the <code>IDocuments</code> contained in this navigator</code>. Does not assert any 
   *  type of ordering on the returned structure.
   *  @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
   */
  public <T extends INavigatorItem> Enumeration<T> getDocuments();
  
  /** Returns the number of <code>INavigatorItem</code>s contained by this <code>IDocumentNavigator</code>
   *  @return the number of documents within this navigator.
   */
  public int getDocumentCount();
  
  /** Returns whether this <code>IDocumentNavigator</code> contains any <code>INavigatorItem</code>s.
   *  @return <code>true</code> if this navigator contains one or more documents, else <code>false</code>.
   */
  public boolean isEmpty();
  
  /**Removes all <code>INavigatorItem</code>s from this <code>IDocumentNavigator</code>. */
  public void clear();
  
  /** Adds an <code>INavigationListener</code> to this navigator. After invoking this method, the passed listener
   *  will observe events generated this navigator.  If the provided listener is already observing this navigator
   *  (<code>==</code>), no action is taken.
   *  @param listener the listener to be added to this navigator.
   */
  public void addNavigationListener(INavigationListener listener);
  
  /** Removes the given listener from observing this navigator. After invoking this method, all observers watching
   *  this navigator "equal" (<code>==</code>) will no longer receive observable dispatches.
   *
   * @param listener the listener to be removed from this navigator
   */
  public void removeNavigationListener(INavigationListener listener);
  
  /** Returns a collection of all listeners registered with this navigator.
   *  @return the collection of nav listeners listening to this navigator.
   */
  public Collection<INavigationListener> getNavigatorListeners();
  
  /** Selects the document at the x,y coordinates of the navigator pane and makes it the active document.
   *  @param x the x coordinate of the navigator pane
   *  @param y the y coordinate of the navigator pane
   */
  public boolean selectDocumentAt(int x, int y);
  
  /** Visitor pattern hook method.
   *  @param algo the algorithm to run on this navigator
   *  @param input the input to the algorithm
   */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input);
  
  /** @return true if a group if INavigatorItems selected. */
  public boolean isGroupSelected();
  
  /** @return true if the INavigatorItem is in the selected group, if a group is selected. */
  public boolean isSelectedInGroup(INavigatorItem i);
  
  /** Adds the top level group with the specified name and filter. */
  public void addTopLevelGroup(String name, INavigatorItemFilter f);
  
  /** Returns true if a top level group is selected, false otherwise. */
  public boolean isTopLevelGroupSelected();
  
  /** Returns the name of the top level group that is selected, throws
   *  a GroupNotSelectedException if a top level group is not selected
   */
  public String getNameOfSelectedTopLevelGroup() throws GroupNotSelectedException;
  
  /** Switches the selection to the given INavigatorItem if the current selection is not already on an 
   *  INavigatorItem.  Since it may be possible that the currently selected item in the navigator does not 
   *  correspond to an INavigatorItem, this method forces the navigator to select an item that does; 
   *  specifically the one given.  If the navigator already has an INavigatorItem selected, this method does
   *  nothing.
   *  @param ini The suggested current INavigatorItem.
   */
  
   public void requestSelectionUpdate(INavigatorItem i);
   
   /** The standard swing repaint() method. */
   public void repaint();
}
