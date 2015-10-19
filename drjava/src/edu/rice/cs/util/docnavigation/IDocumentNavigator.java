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
import java.util.*;
import java.awt.Container;
import java.awt.event.FocusListener;

/** {@code IDocumentNavigator} provides a framework through which individual {@code IDocument}s can be 
  * navigated. */ 
public interface IDocumentNavigator<ItemT extends INavigatorItem> extends IAWTContainerNavigatorActor {
  /** @return an AWT component which interacts with this document navigator */
  public Container asContainer();
  
  /** Adds an {@code IDocuemnt} to this navigator.
    * @param doc the document to be added into this navigator.
    */
  public void addDocument(ItemT doc);
  
  /** Adds an {@code INavigatorItem} into this navigator in a position relative to a given path.
    * @param doc the document to be added into this navigator.
    * @param path the relative path to insert this INavigatorItem at.
    */
  public void addDocument(ItemT doc, String path);
  
  /** @return the currently selected navigator item, or null if no navigator item is selected. */
  public ItemT getCurrent();
  
  /** @return the model lock for this navigator.  Only used for locking in code external to the implementing class. */
  public Object getModelLock();
  
  /** Removes a given {@code INavigatorItem>} from this navigator. Removes all {@code INavigatorItem}s 
    * from this navigator that are "equal" ({@code .equals(...)}) to the passed argument. Any of the 
    * removed documents may be returned by this method.
    * @param doc the docment to be removed
    * @return doc a document removed from this navigator as a result of invoking this method.
    * @throws IllegalArgumentException if this navigator contains no document equal to the passed document.
    */
  public ItemT removeDocument(ItemT doc);
  
  /** Resets a given {@code INavigatorItem} in the tree.  This may affect the placement of the item or its 
    * display to reflect any changes made in the model.
    * @param doc the docment to be refreshed
    * @param path path to the document
    * @throws IllegalArgumentException if this navigator contains no document that is equal to the passed document.
    */
  public void refreshDocument(ItemT doc, String path);
  
  /** Sets the active document as specified.
    * @param doc the document to select
    */
  public void selectDocument(ItemT doc);
  
  /** The following five operations impose a natural ordering on the documents in the navigator.
    * For lists, it is order of insertion. For trees, it is depth-first enumeration.
    * This convention supports operations setActiveNextDocument() in the global model of DrJava
    * @return the INavigatorItem which comes after doc
    * @param doc the INavigatorItem of interest
    */
  public ItemT getNext(ItemT doc);
  
  /** @return the INavigatorItem which comes before doc
    * @param doc the INavigatorItem of interest
    */
  public ItemT getPrevious(ItemT doc);
  
  /** @return the INavigatorItem which comes first in the enumeration. */
  public ItemT getFirst();
  
  /** @return the INavigatorItem which comes last in the enumeration
    */
  public ItemT getLast();
  
  /** Returns all the {@code IDocuments} in the collection in enumeration order.
    * @return an {@code INavigatorItem} enumeration of this navigator's contents.
    */
  public ArrayList<ItemT> getDocuments();
  
  /** Returns all the {@code IDocuments} contained in the specified bin.
    * @param binName name of bin
    * @return an {@code INavigatorItem} enumeration of this navigator's contents.
    */
  public ArrayList<ItemT> getDocumentsInBin(String binName);
  
  /** Tests to see if a given document is contained in this navigator.
    * @param doc the document to test for containment.
    * @return {@code true} if this contains a document "equal" ({@code .equals(...)} method)
    *         to the passed document, else {@code false}.
    */
  public boolean contains(ItemT doc);
  
  /** Returns the number of {@code INavigatorItem}s contained by this {@code IDocumentNavigator}
    * @return the number of documents within this navigator.
    */
  public int getDocumentCount();
  
  /** Returns whether this {@code IDocumentNavigator} contains any {@code INavigatorItem}s.
    * @return {@code true} if this navigator contains one or more documents, else {@code false}.
    */
  public boolean isEmpty();
  
  /** Removes all {@code INavigatorItem}s from this {@code IDocumentNavigator}. */
  public void clear();
  
  /** Adds an {@code INavigationListener} to this navigator. After invoking this method, the passed listener
    * will observe events generated this navigator.  If the provided listener is already observing this navigator
    * ({@code ==}), no action is taken.
    * @param listener the listener to be added to this navigator.
    */
  public void addNavigationListener(INavigationListener<? super ItemT> listener);
  
  /** Removes the given listener from observing this navigator. After invoking this method, all observers watching
    * this navigator "equal" ({@code ==}) will no longer receive observable dispatches.
    * @param listener the listener to be removed from this navigator
    */
  public void removeNavigationListener(INavigationListener<? super ItemT> listener);
  
  /** 
   * Add FocusListener to navigator. 
   * @param e the listener to be added
   */
  public void addFocusListener(FocusListener e);
  
  /** 
   * Remove FocusListener from navigator. 
   * @param e the listener to be removed
   */
  public void removeFocusListener(FocusListener e);
  
  /** @return the FocustListeners. */
  public FocusListener[] getFocusListeners();
  
  /** Returns a collection of all listeners registered with this navigator.
    * @return the collection of nav listeners listening to this navigator.
    */
  public Collection<INavigationListener<? super ItemT>> getNavigatorListeners();
  
  /** 
   * Selects the document at the x,y coordinates of the navigator pane and makes it the active document.
   * @param x the x coordinate of the navigator pane
   * @param y the y coordinate of the navigator pane
   * @return boolean status
   */
  public boolean selectDocumentAt(int x, int y);
  
  /** Returns true if the item at the x,y coordinate of the navigator pane is currently selected.
    * @param x the x coordinate of the navigator pane
    * @param y the y coordinate of the navigator pane
    * @return true if the item is currently selected
    */
  public boolean isSelectedAt(int x, int y);
  
  /** Visitor pattern hook method.
    * @param <InType> the input type
    * @param <ReturnType> the return type
    * @param algo the algorithm to run on this navigator
    * @param input the input to the algorithm
    * @return result
    */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<ItemT, InType, ReturnType> algo, InType input);
  
  /** @return the number of selected items. */
  public int getSelectionCount();
  
  /** @return true if at least one group of INavigatorItems is selected. */
  public boolean isGroupSelected();
  
  /** @return the number of groups selected. */
  public int getGroupSelectedCount();
  
  /** @return the folders currently selected. */
  public java.util.List<File> getSelectedFolders();
  
  /** @return true if at least one document is selected. */
  public boolean isDocumentSelected();
  
  /** @return the number of documents selected. */
  public int getDocumentSelectedCount();
  
  /** @return the documents currently selected. */
  public java.util.List<ItemT> getSelectedDocuments();
  
  /** @return true if the root is selected. Only runs in event thread. */
  public boolean isRootSelected();
  
  /** 
   * @param i item to check
   * @return true if the INavigatorItem is in a selected group, if
   * at least one group is selected. 
   */
  public boolean isSelectedInGroup(ItemT i);
  
  /** 
   * Adds the top level group with the specified name and filter. 
   * @param name name
   * @param f filter 
   */
  public void addTopLevelGroup(String name, INavigatorItemFilter<? super ItemT> f);
  
  /** @return true if at least one top level group is selected, false otherwise. */
  public boolean isTopLevelGroupSelected();
  
  /** @return the names of the top level groups that the selected items descend from.
    * @throws GroupNotSelectedException if no top level group is selected
    */
  public java.util.Set<String> getNamesOfSelectedTopLevelGroup() throws GroupNotSelectedException;
  
  /** Switches the selection to the given INavigatorItem if the current selection is not already on an 
    * INavigatorItem.  Since it may be possible that the currently selected item in the navigator does not 
    * correspond to an INavigatorItem, this method forces the navigator to select an item that does; 
    * specifically the one given.  If the navigator already has an INavigatorItem selected, this method does
    * nothing.
    * @param i The suggested current INavigatorItem.
    */
  public void requestSelectionUpdate(ItemT i);
  
  /** The standard swing repaint() method. */
  public void repaint();
  
  /** 
   * Marks the next selection change as model-initiated (true) or user-initiated (false; default). 
   * @param b value to set
   */
  public void setNextChangeModelInitiated(boolean b);
  
  /** @return whether the next selection change is model-initiated (true) or user-initiated (false). */
  public boolean isNextChangeModelInitiated();
  
  /** The name of the client property that determines whether a change is model- or user-initiated. */
  public static final String MODEL_INITIATED_PROPERTY_NAME = "ModelInitiated";
  
//   public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("browser.txt", false);
}
