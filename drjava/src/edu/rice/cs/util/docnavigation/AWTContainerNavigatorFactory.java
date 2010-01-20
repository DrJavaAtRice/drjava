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

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.swing.Utilities;

import java.util.List;
import java.util.*;
import java.awt.event.FocusListener;

public class AWTContainerNavigatorFactory<ItemT extends INavigatorItem> implements IDocumentNavigatorFactory<ItemT> {
  
  public AWTContainerNavigatorFactory() { }
  
  /** Creates a new List Navigator
    * @return a list navigator
    */
  public IDocumentNavigator<ItemT> makeListNavigator() { return new JListSortNavigator<ItemT>(); }
  
  /** Returns a new tree Navigator with the specified root
    * @param path the path name of the root node
    * @return a tree navigator
    */
  public IDocumentNavigator<ItemT> makeTreeNavigator(String path) { return new JTreeSortNavigator<ItemT>(path); }
  
  /** Creates a list navigator and migrates the navigator items from parent to the new navigator.  The migration
    * is asynchronous but it completes before any subsequent computation in the event thread.
    * @param parent the navigator to migrate from
    * @return the new list navigator
    */
  public IDocumentNavigator<ItemT> makeListNavigator(final IDocumentNavigator<ItemT> parent) {
    final IDocumentNavigator<ItemT> child = makeListNavigator();
    Utilities.invokeLater(new Runnable() { 
      public void run() {
//          synchronized (child.getModelLock()) { // dropped because of cost; each atomic action is still synchronized
        migrateNavigatorItems(child, parent);
        migrateListeners(child, parent);
      }
//        }
    });
    return child;
  }
  
  /** Creates a tree navigator and migrates the navigator items from the parent to the new navigator. The migration
    * is asynchronous but it completes before any subsequent computation in the event thread.
    * @param name the name of the root node
    * @param parent the navigator to migrate from
    * @return the new tree navigator
    */
  public IDocumentNavigator<ItemT> makeTreeNavigator(String name, final IDocumentNavigator<ItemT> parent, 
                                                     final List<Pair<String, INavigatorItemFilter<ItemT>>> l) {
    
    final IDocumentNavigator<ItemT> child = makeTreeNavigator(name);
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
//          synchronized (child.getModelLock()) { // dropped because of cost; each atomic action is still synchronized
        for(Pair<String, INavigatorItemFilter<ItemT>> p: l) { child.addTopLevelGroup(p.first(), p.second()); }
        migrateNavigatorItems(child, parent);
        migrateListeners(child, parent);
      }
//        }
    });     
    return child;
  }
  
  /** Migrates all the navigator items from parent to child
    * @param child the navigator to migrate to
    * @param parent the navigator to migrate from
    */
  // As a first step to weakening the restriction on parent's type, this allows parent to be based on an arbitrary item type, as
  // long as it extends ItemT.
  private void migrateNavigatorItems(IDocumentNavigator<ItemT> child, IDocumentNavigator<ItemT> parent) {
    ArrayList<ItemT> docs =  parent.getDocuments();
    for (ItemT item: docs) child.addDocument(item);

    parent.clear(); // Remove documents from old navigator (parent)
  }
  
  /** Migrates all the listeners from parent to child
    * @param child the navigator to migrate to
    * @param parent the navigator to migrate from
    */
  // As a first step to weakening the restriction on parent's type, this allows parent to be based on an arbitrary item type, as
  // long as it extends ItemT.
  private void migrateListeners(IDocumentNavigator<ItemT> child, IDocumentNavigator<ItemT> parent) {
    for (INavigationListener<? super ItemT> nl: parent.getNavigatorListeners())  child.addNavigationListener(nl);
    for (FocusListener fl: parent.getFocusListeners())  child.addFocusListener(fl);
  }
}
