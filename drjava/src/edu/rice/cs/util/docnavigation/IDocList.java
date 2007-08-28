/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

//import edu.rice.cs.drjava.model.*;
import javax.swing.AbstractListModel;
import java.util.*;

class IDocList<ItemT extends INavigatorItem> extends AbstractListModel {
    private Vector<ItemT> _docs = new Vector<ItemT>();

    public Enumeration<ItemT> elements() {
        return _docs.elements();
    }

    public void clear() { _docs.clear(); }

    public boolean isEmpty() { return _docs.isEmpty(); }

    public ItemT get(int index) {
        return _docs.get(index);
    }

    public Object getElementAt(int i) {
        return _docs.get(i); 
    }

    public int size() { return _docs.size(); }
    public int getSize() { return size(); }

    public void add(ItemT d) {
        _docs.addElement(d);
        fireIntervalAdded(this, size() - 1, size() - 1);
    }

    /**
     * Remove and return the INavigatorItem doc (and notify ListDataListeners)
     * @return the removed INavigatorItem
     * @exception NoSuchElementException if doc is not present in the DocumentNavigator
     */
    public INavigatorItem remove(ItemT doc) {
        int index = _docs.indexOf(doc);
        if( index == -1 ) {
            return null;
        }

        ItemT ret = _docs.remove(index);
        fireIntervalRemoved(this, index, index);
        return ret;
    }
}
