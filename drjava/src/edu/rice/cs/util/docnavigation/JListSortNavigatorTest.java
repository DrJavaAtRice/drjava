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

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.*;

public class JListSortNavigatorTest extends DrJavaTestCase {
  
  protected JListSortNavigator<DummyINavigatorItem> list;
  protected DummyINavigatorItem i1, i2, i3, i4;
  
  public void setUp() throws Exception {
    super.setUp();

    list = new JListSortNavigator<DummyINavigatorItem>();
    
    i1 = new DummyINavigatorItem("item1");
    i2 = new DummyINavigatorItem("item2");
    i3 = new DummyINavigatorItem("item3");
    i4 = new DummyINavigatorItem("item4");
    list.addDocument(i1);
    list.addDocument(i2);
    list.addDocument(i3);
    list.addDocument(i4);
  }
  
  public void testTraversalOps() {
    assertEquals("doc count test", 4, list.getDocumentCount());
    assertSame("getFirst test", i1, list.getFirst());
    assertSame("getLast test", i4, list.getLast());
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        list.setNextChangeModelInitiated(true);
        list.selectDocument(i1); 
      } 
    });

    assertSame("getCurrent test", i1, list.getCurrent());
    assertSame("getNext test 1", i2, list.getNext(i1));
    assertSame("getNext test 2", i3, list.getNext(i2));
    assertSame("getNext test 3", i4, list.getNext(i3));

    assertSame("getPrevious test 1", i3, list.getPrevious(i4));
    assertSame("getPrevious test 2", i2, list.getPrevious(i3));
    assertSame("getPrevious test 3", i1, list.getPrevious(i2));
    
    assertTrue("contains test 1", list.contains(i1));
    assertTrue("contains test 2", list.contains(i2));
    assertTrue("contains test 3", list.contains(i3));
    assertTrue("contains test 4", list.contains(i4));
    
    assertFalse("contains test 5", list.contains(new DummyINavigatorItem("item1")));
    
    ArrayList<DummyINavigatorItem> docs = list.getDocuments();
    DummyINavigatorItem[] docsArray = docs.toArray(new DummyINavigatorItem[0]);
    assertTrue("getDocuments test", Arrays.equals(docsArray, new DummyINavigatorItem[] {i1, i2, i3, i4}));
  }
  
  /** Test of getting the list of selected items.
   * Commented out when changes from revision 4171 were reverted.
   */
//  public void testGetSelectedDocuments() {
//    list.clearSelection();
//    list.addSelectionInterval(0, 1);
//    assertEquals("Two items should be selected", 2, list.getSelectionCount());
//    assertEquals("Two items should be selected", 2, list.getDocumentSelectedCount());
//    assertEquals("Zero groups should be selected", 0, list.getGroupSelectedCount());
//    java.util.List<DummyINavigatorItem> l = list.getSelectedDocuments();
//    assertEquals("Two items should be selected", 2, l.size());
//    assertEquals("Wrong item 1", i1, l.get(0));
//    assertEquals("Wrong item 2", i2, l.get(1));
//
//    list.clearSelection();
//    list.addSelectionInterval(0, 3);
//    assertEquals("Four items should be selected", 4, list.getSelectionCount());
//    assertEquals("Four items should be selected", 4, list.getDocumentSelectedCount());
//    assertEquals("Zero groups should be selected", 0, list.getGroupSelectedCount());
//    l = list.getSelectedDocuments();
//    assertEquals("Four items should be selected", 4, l.size());
//    assertEquals("Wrong item 1", i1, l.get(0));
//    assertEquals("Wrong item 2", i2, l.get(1));
//    assertEquals("Wrong item 3", i3, l.get(2));
//    assertEquals("Wrong item 4", i4, l.get(3));
//
//    list.clearSelection();
//    list.addSelectionInterval(0, 1);
//    list.addSelectionInterval(2, 3);
//    assertEquals("Four items should be selected", 4, list.getSelectionCount());
//    assertEquals("Four items should be selected", 4, list.getDocumentSelectedCount());
//    assertEquals("Zero groups should be selected", 0, list.getGroupSelectedCount());
//    l = list.getSelectedDocuments();
//    assertEquals("Four items should be selected", 4, l.size());
//    assertEquals("Wrong item 1", i1, l.get(0));
//    assertEquals("Wrong item 2", i2, l.get(1));
//    assertEquals("Wrong item 3", i3, l.get(2));
//    assertEquals("Wrong item 4", i4, l.get(3));
//    
//    list.clearSelection();
//    assertEquals("Zero items should be selected", 0, list.getSelectionCount());
//    assertEquals("Zero items should be selected", 0, list.getDocumentSelectedCount());
//    assertEquals("Zero groups should be selected", 0, list.getGroupSelectedCount());
//    l = list.getSelectedDocuments();
//    assertEquals("Zero items should be selected", 0, l.size());
//  }
}