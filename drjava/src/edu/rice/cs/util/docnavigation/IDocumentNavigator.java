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
public interface IDocumentNavigator {
    /** @return an AWT component which interacts with this document navigator */
    public Container asContainer();

    /**
     * Adds an <code>IDocuemnt</code> to this navigator.
     *
     * @param doc the document to be added into this navigator.
     */
    public void addDocument(INavigatorItem doc);

    /**
     * Adds an <code>INavigatorItem</code> into this navigator in a position relavite
     * to a given path.
     *
     * @param doc the document to be added into this navigator.
     * @param path the relative path to insert this INavigatorItem at.
     */
    public void addDocument(INavigatorItem doc, String path);
    
    /**
     * Sets the top level path that all INavigatorItems should have in common.
     *
     * @param path the top most path that all INavigatorItems will have in common
     */
    public void setTopLevelPath(String path);

    /**
     * Removes a given <code>INavigatorItem<code> from this navigator. Removes all
     * <code>INavigatorItem</code>s from this navigator that are "equal" (as tested by
     * the <code>equals</code> method) to the passed argument. Any of the removed
     * documents may be returned by this method.
     * @param doc the docment to be removed
     * @return doc a document removed from this navigator as a result of invoking this method.
     * @throws IllegalArgumentException if this navigator contains no document
     *  that is equal to the passed document.
     */
    public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException;

  /**
   * Resets a given <code>INavigatorItem<code> in the tree.  This may affect the
   * placement of the item or its display to reflect any changes made in the model.
   * @param doc the docment to be refreshed
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public void refreshDocument(INavigatorItem doc, String path) throws IllegalArgumentException;
   
    /**
     * sets the input document as selected
     * @param doc the document to select
     * @return void
     */
    public void setActiveDoc(INavigatorItem doc);
    
    
    /**
     * Impose some ordering on the documents in the navigator, to facilitate
     * MainFrame's setActiveNextDocument()
     * @return the INavigatorItem which comes after doc
     * @param doc the INavigatorItem of interest
     */
    public INavigatorItem getNext(INavigatorItem doc);

    /**
     * Impose some ordering on the documents in the navigator, to facilitate
     * MainFrame's setActivePrevDocument()
     * @return the INavigatorItem which comes before doc
     * @param doc the INavigatorItem of interest
     */
    public INavigatorItem getPrevious(INavigatorItem doc);

    /**
     * Tests to see if a given document is contained in this navigator.
     *
     * @param doc the document to test for containment.
     * @return <code>true</code> if this navigator contains a document that is
     *  "equal" (as tested by the <code>equals</code< method)
     * to the passed document, else <code>false</code>.
     */
    public boolean contains(INavigatorItem doc);

    /**
     * Returns all the <code>IDocuments</code> contained in this navigator</code>.
     * Does not assert any type of ordering on the returned structure.
     *
     * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
     */
    public Enumeration<INavigatorItem> getDocuments();

    /**
     * Returns the number of <code>INavigatorItem</code>s contained by this <code>IDocumentNavigator</code>
     *
     * @return the number of documents within this navigator.
     */
    public int getDocumentCount();

    /**
     * Returns whether this <code>IDocumentNavigator</code> contains any <code>INavigatorItem</code>s.
     *
     * @return <code>true</code> if this navigator contains one or more documents, else <code>false</code>.
     */
    public boolean isEmpty();

    /**
     * Removes all <code>INavigatorItem</code>s from this <code>IDocumentNavigator</code>.
     */
    public void clear();

    /**
     * Adds an <code>INavigationListener</code> to this navigator.
     * After invoking this method, the passed listener will be eligable for
     * observing this navigator.  If the provided listener is already observing this
     * navigator (as tested by the == operator), no action is taken.
     *
     * @param listener the listener to be added to this navigator.
     */
    public void addNavigationListener(INavigationListener listener);

    /**
     * Removes the given listener from observing this navigator.
     * After invoking this method, all observers observing this navigator
     * "equal" (as tested by the == operator) will no longer receive observable dispatches.
     *
     * @param listener the listener to be removed from this navigator
     */
    public void removeNavigationListener(INavigationListener listener);

    /**
     * Returns a collection of all listeners registered with this navigator.
     * 
     * @return the collection of nav listeners listening to this navigator.
     */
    public Collection<INavigationListener> getNavigatorListeners();

    /**
     * Selects the document at the x,y coordinate of the navigator pane and sets it to be
     * the currently active document.
     * @param x the x coordinate of the navigator pane
     * @param y the y coordinate of the navigator pane
     */
    public boolean selectDocumentAt(int x, int y);
    
    /**
     * visitor pattern
     * @param algo the algorithm to run on this navigator
     * @param input the input to the algorithm
     */
    public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input);
 
    /**
     * @return true if a group if INavigatorItems selected
     */
    public boolean isGroupSelected();
    
    /**
     * @return true if the INavigatorItem is in the selected group, if a group is selected
     */
    public boolean isSelectedInGroup(INavigatorItem i);
      
     
}
