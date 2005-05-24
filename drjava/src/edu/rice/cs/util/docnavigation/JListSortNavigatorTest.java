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

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

import javax.swing.tree.*;

public class JListSortNavigatorTest extends TestCase {
  
  protected JListSortNavigator list;
  protected INavigatorItem i1, i2, i3, i4;
  
  public void setUp() throws IOException {
    
    list = new JListSortNavigator();
    
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
    
    list.setActiveDoc(i1);
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
    
    Enumeration<INavigatorItem> docs = list.getDocuments();
    INavigatorItem[] docsArray = new INavigatorItem[4];
    for (int i = 0; i < 4; i++) docsArray[i] = docs.nextElement();
    assertTrue("getDocuments test", Arrays.equals(docsArray, new INavigatorItem[] {i1, i2, i3, i4}));
  }
  
}