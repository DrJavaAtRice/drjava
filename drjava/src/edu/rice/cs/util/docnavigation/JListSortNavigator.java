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

import java.awt.dnd.*;
import edu.rice.cs.drjava.DrJavaRoot;

class JListSortNavigator<ItemT extends INavigatorItem> extends JListNavigator<ItemT> 
  implements DropTargetListener {
  
  /* Relies on the standard default constructor */
 
  /** Adds the document to the list.  Should only be executed from event thread.
    * @param doc the document to add
    */
  public void addDocument(ItemT doc) { insertDoc(doc); }
 
  /** Inserts the document into its sorted position. Should only be executed in the event thread.
    * @param doc the document to add
    */
  private int insertDoc(ItemT doc) {
    int i;
    synchronized(_model) {
      for (i = 0; i < _model.size(); i++) { 
        ItemT item = getFromModel(i);
        if (doc.getName().toUpperCase().compareTo(item.getName().toUpperCase()) <= 0) break;
      }
      _model.add(i, doc);
    }
    return i;
  }
  
  public String toString() { synchronized(_model) { return _model.toString(); } }
  
  /** Drag and drop target. */
  DropTarget dropTarget = new DropTarget(this, this);  
  
  /** User dragged something into the component. */
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    DrJavaRoot.dragEnter(dropTargetDragEvent);
  }
  
  public void dragExit(DropTargetEvent dropTargetEvent) { }
  public void dragOver(DropTargetDragEvent dropTargetDragEvent) { }
  public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){ }
  
  /** User dropped something on the component. */
  public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
    DrJavaRoot.drop(dropTargetDropEvent);
  }
}
