/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
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

class JListSortNavigator extends JListNavigator {
  
  /* Relies on the standard default constructor */
 
  /** Adds the document to the list.  Should only be executed from event thread.
   *  @param doc the document to add
   */
  public void addDocument(INavigatorItem doc) { insertDoc(doc); }
 
  /** Inserts the document into its sorted position. Should only be executed in the event thread.
   *  @param doc the document to add
   */
  private int insertDoc(INavigatorItem doc) {
    int i;
    synchronized (_model) {
      for (i = 0; i<_model.size(); i++) { 
        INavigatorItem item = (INavigatorItem) _model.get(i);
        if (doc.getName().toUpperCase().compareTo(item.getName().toUpperCase()) <= 0) break;
      }
      _model.add(i, doc);
    }
    return i;
  }
  
  public String toString() { 
    synchronized(_model) { return _model.toString(); } 
  }
}
