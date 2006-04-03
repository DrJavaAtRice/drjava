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
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.swing.Utilities;

import java.util.List;
import java.util.*;
import java.awt.event.FocusListener;

public class AWTContainerNavigatorFactory<ItemT extends INavigatorItem> implements IDocumentNavigatorFactory<ItemT> {
  
  public AWTContainerNavigatorFactory() { }

  /** Creates a new List Navigator
   *  @return a list navigator
   */
    public IDocumentNavigator<ItemT> makeListNavigator() { return new JListSortNavigator<ItemT>(); }

  /** Returns a new tree Navigator with the specified root
   *  @param path the path name of the root node
   *  @return a tree navigator
   */
    public IDocumentNavigator<ItemT> makeTreeNavigator(String path) { return new JTreeSortNavigator<ItemT>(path); }
    
  /** Creates a list navigator and migrates the navigator items from parent to the new navigator
   *  @param parent the navigator to migrate from
   *  @return the new list navigator
   */
    public IDocumentNavigator<ItemT> makeListNavigator(final IDocumentNavigator<ItemT> parent) {
      final IDocumentNavigator<ItemT> tbr = makeListNavigator();
      Utilities.invokeAndWait(new Runnable() { 
        public void run() { 
          migrateNavigatorItems(tbr, parent);
          migrateListeners(tbr, parent);
        }
      });
      return tbr;
    }
  
  /** Creates a tree navigator and migrates the navigator items from the parent to the new navigator
   *  @param name the name of the root node
   *  @param parent the navigator to migrate from
   *  @return the new tree navigator
   */
    public IDocumentNavigator<ItemT> makeTreeNavigator(String name, final IDocumentNavigator<ItemT> parent, 
                                                final List<Pair<String, INavigatorItemFilter<ItemT>>> l) {
      
      final IDocumentNavigator<ItemT> tbr = makeTreeNavigator(name);
      
      Utilities.invokeAndWait(new Runnable() { 
        public void run() { 
          
          for(Pair<String, INavigatorItemFilter<ItemT>> p: l) { tbr.addTopLevelGroup(p.getFirst(), p.getSecond()); }
          
          migrateNavigatorItems(tbr, parent);
          migrateListeners(tbr, parent);
        }
      });     
      return tbr;
    }
    
    /** Migrates all the navigator items from parent to child
     *  @param child the navigator to migrate to
     *  @param parent the navigator to migrate from
     */
    // As a first step to weakening the restriction on parent's type, this allows parent to be based on an arbitrary item type, as
    // long as it extends ItemT.
    private void migrateNavigatorItems(IDocumentNavigator<ItemT> child, IDocumentNavigator<ItemT> parent) {
      Enumeration<ItemT> enumerator =  parent.getDocuments();
      while (enumerator.hasMoreElements()) {
        ItemT navitem = enumerator.nextElement();
        child.addDocument(navitem);
      }
      parent.clear(); // Remove documents from old navigator (parent)
    }
    
    /** Migrates all the listeners from parent to child
     *  @param child the navigator to migrate to
     *  @param parent the navigator to migrate from
     */
    // As a first step to weakening the restriction on parent's type, this allows parent to be based on an arbitrary item type, as
    // long as it extends ItemT.
    private void migrateListeners(IDocumentNavigator<ItemT> child, IDocumentNavigator<ItemT> parent) {
      for (INavigationListener<? super ItemT> nl: parent.getNavigatorListeners())  child.addNavigationListener(nl);
      for (FocusListener fl: parent.getFocusListeners())  child.addFocusListener(fl);
    }
}
