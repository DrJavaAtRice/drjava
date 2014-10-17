/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Tests the functionality of the ModelList list class.
 * @version $Id: ModelListTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class ModelListTest extends DrJavaTestCase {
  protected ModelList<Integer> fEmpty;
  protected ModelList<Integer> fFull;

  protected void setUp() throws Exception {
    super.setUp();
    fFull = new ModelList<Integer>();
    fEmpty = new ModelList<Integer>();
  }

  public void testInsert() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    ModelList<Integer>.ModelIterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertTrue("#0.1", fFull.isEmpty());
    assertEquals("#0.2", 0, fEmpty.length());
    assertEquals("#0.3", 0, fFull.length());
    assertTrue("#0.4", itEmpty.atStart());
    assertTrue("#0.5", itFull.atStart());
    itFull.insert(Integer.valueOf(5));
    assertTrue("#1.0", !itFull.atStart());
    assertEquals("#1.1", 1, fFull.length());
    assertEquals("#1.2", Integer.valueOf(5), itFull.current());
    assertTrue("#2.0", fEmpty.isEmpty());
    assertTrue("#2.1", !fFull.isEmpty());
    itFull.insert(Integer.valueOf(4));
    assertEquals("#2.2", 2, fFull.length());
    assertEquals("#2.3", Integer.valueOf(4), itFull.current());
    assertTrue("#2.4", !fFull.isEmpty());
  }

  public void testInsertFront() {
    fFull.insertFront(Integer.valueOf(3));
    fFull.insertFront(Integer.valueOf(2));
    fFull.insertFront(Integer.valueOf(1));
    fFull.insertFront(Integer.valueOf(0));
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    for (int i = 0; i < 4; i++) {
      itFull.next();
      assertEquals(Integer.valueOf(i), itFull.current());
    }
  }

  public void testRemove() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    //ModelList<Integer>.ModelIterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertEquals("#0.1", 0, fEmpty.length());
    assertEquals("#0.2", 0, fFull.length());

    itFull.insert(Integer.valueOf(5));
    assertTrue("#2.0", !fFull.isEmpty());
    assertEquals("#2.1", 1, fFull.length());
    itFull.remove();
    assertTrue("#3.0", fFull.isEmpty());
    assertEquals("#3.1", 0, fFull.length());
  }

  public void testNext() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    //ModelList<Integer>.ModelIterator itEmpty = fEmpty.getIterator();

    itFull.insert(Integer.valueOf(6));
    itFull.insert(Integer.valueOf(5));
    itFull.insert(Integer.valueOf(4));
    //now at start of list, after head.
    assertEquals("#1.0", Integer.valueOf(4), itFull.current());
    itFull.next();
    assertEquals("#1.1", Integer.valueOf(5), itFull.current());
    itFull.next();
    assertEquals("#1.2", Integer.valueOf(6), itFull.current());
    itFull.next();
  }

  public void testPrev() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    //ModelList<Integer>.ModelIterator itEmpty = fEmpty.getIterator();

    itFull.insert(Integer.valueOf(6));
    itFull.insert(Integer.valueOf(5));
    itFull.insert(Integer.valueOf(4));
    itFull.next();
    itFull.next();
    itFull.next();

    itFull.prev();
    assertEquals("#1.1", Integer.valueOf(6), itFull.current());
    itFull.prev();
    assertEquals("#1.2", Integer.valueOf(5), itFull.current());
    itFull.prev();
    assertEquals("#1.3", Integer.valueOf(4), itFull.current());
    itFull.prev();
  }

  public void testCurrent() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    itFull.next();
  }

  public void testPrevItem() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    itFull.insert(Integer.valueOf(0));
    itFull.insert(Integer.valueOf(1));
    itFull.next();
    assertEquals("#0.0", Integer.valueOf(1), itFull.prevItem());
  }

  public void testNextItem() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    itFull.insert(Integer.valueOf(0));
    assertEquals("#0.2", Integer.valueOf(0), itFull.current());
    itFull.insert(Integer.valueOf(1));
    assertEquals("#0.1", Integer.valueOf(1), itFull.current());
    assertEquals("#0.0", Integer.valueOf(0), itFull.nextItem());
  }
  
  public void testCollapse() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    ModelList<Integer>.ModelIterator itEmpty = fEmpty.getIterator();
    ModelList<Integer>.ModelIterator itEmpty2 = itEmpty.copy();
    assertEquals("#0.0", 0, fEmpty.length());
    assertEquals("#0.1", 0, itEmpty.pos());
    itEmpty.collapse(itEmpty2);
    assertEquals("#0.0", 0, fEmpty.length());
    assertEquals("#0.2", 0, itFull.pos());

    itFull.insert(Integer.valueOf(6));
    assertEquals("#0.3", 1, itFull.pos());
    ModelList<Integer>.ModelIterator itFull2 = itFull.copy();
    assertEquals("#0.4", 1, itFull2.pos());
    
    assertEquals("#1.0", 1, fFull.length());
    itFull.collapse(itFull2);
    assertEquals("#1.1", 1, fFull.length());
    assertEquals("#1.2", 1, itFull2.pos());
    assertEquals("#1.3", 1, itFull.pos());

    itFull.insert(Integer.valueOf(5));
    assertEquals("#2.0", 2, fFull.length());
    assertEquals("#2.2", 1, itFull.pos());
    assertEquals("#2.3", 2, itFull2.pos());
    itFull.collapse(itFull2);
    assertEquals("#2.1", 2, fFull.length());

    //collapse to the right
    itFull.insert(Integer.valueOf(4));
    assertEquals("#3.0", 3, fFull.length());
    assertEquals("#3.0b",Integer.valueOf(4),itFull.current());
    assertEquals("#3.0a", Integer.valueOf(6), itFull2.current());
    assertEquals("#3.0h", 3, itFull2.pos());
    itFull.collapse(itFull2);
    assertEquals("3.0d", Integer.valueOf(6), itFull2.current());
    assertEquals("3.0e", 2, itFull2.pos());
    assertEquals("3.0f", Integer.valueOf(4), itFull.current());
    assertEquals("3.0g", 1, itFull.pos());
    itFull.next();
    assertEquals("#3.0c",Integer.valueOf(6),itFull.current());
    assertEquals("#3.1", 2, fFull.length());
    itFull.prev();
    assertEquals("#4.0", Integer.valueOf(4), itFull.current());
    assertEquals("#4.1", Integer.valueOf(6), itFull2.current());
    
    //collapse to the left
    itFull.insert(Integer.valueOf(7));
    assertEquals("#5.0a", 3, fFull.length());
    assertEquals("#5.0b", Integer.valueOf(7), itFull.current());
    assertEquals("#5.0c", Integer.valueOf(6), itFull2.current());
    itFull2.collapse(itFull);
    assertEquals("#5.1a", 2, fFull.length());
    assertEquals("#5.1b", Integer.valueOf(7), itFull.current());
    assertEquals("#5.1c", Integer.valueOf(6), itFull2.current());
    assertEquals("#5.2a", Integer.valueOf(6), itFull.nextItem());
    assertEquals("#5.2b", Integer.valueOf(7), itFull2.prevItem());
  }

  public void testNotifyInsert() {
    ModelList<Integer>.ModelIterator itFull2 = fFull.getIterator();

    itFull2.insert(Integer.valueOf(0));
    ModelList<Integer>.ModelIterator itFull = itFull2.copy();
    itFull2.insert(Integer.valueOf(1));
    assertEquals(Integer.valueOf(0), itFull.current());
  }

  public void testNotifyRemove() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    ModelList<Integer>.ModelIterator itFull2 = fFull.getIterator();

    itFull2.insert(Integer.valueOf(0));
    itFull2.insert(Integer.valueOf(1));
    itFull.next();
    assertEquals("#0.1", Integer.valueOf(1), itFull.current());
    itFull2.remove();
    assertEquals("#0.0", Integer.valueOf(0), itFull.current());
  }

  public void testNotifyCollapse() {
    ModelList<Integer>.ModelIterator itFull = fFull.getIterator();
    ModelList<Integer>.ModelIterator itFull2 = fFull.getIterator();
    ModelList<Integer>.ModelIterator itFull3 = fFull.getIterator();

    itFull2.insert(Integer.valueOf(0));
    itFull2.insert(Integer.valueOf(1));
    itFull2.insert(Integer.valueOf(2));
    itFull2.insert(Integer.valueOf(3));
    itFull2.insert(Integer.valueOf(4));

    assertTrue("#0.0.0",itFull.atStart());
    // we have (4 3 2 1 0), itFull2 points at 4, itFull and itFull3
    // point at 0.  We want to move itFull back to point at 2 to show
    // notifyCollapse works

    for (int i = 0; i < 3; i++) {
      itFull.next();
    }
    for (int j = 0; j < 5; j++) {
      itFull3.next();
    }
    assertEquals("#0.0", Integer.valueOf(2), itFull.current());
    assertEquals("#0.1", Integer.valueOf(0), itFull3.current());
    itFull2.collapse(itFull3);

    assertEquals("#1.0", Integer.valueOf(4), itFull2.current());
    assertEquals("#1.1", Integer.valueOf(0), itFull3.current());
    assertEquals("#1.2", Integer.valueOf(0), itFull.current());
  }
  
  public void testListenerCount() {
    ModelList<Character> testList = new ModelList<Character>();
    
    assertEquals("No iterators", 0, testList.listenerCount());
    
    ModelList<Character>.ModelIterator iter1 = testList.getIterator();
    
    assertEquals("One iterator", 1, testList.listenerCount());
    
    ModelList<Character>.ModelIterator iter2 = testList.getIterator();
    
    assertEquals("Two iterators", 2, testList.listenerCount());
    
    iter1.dispose();
    iter1 = null;
    
    assertEquals("Removed first iterator", 1, testList.listenerCount());
    
    iter2.dispose();
    iter2 = null;
    
    assertEquals("Removed second iterator", 0, testList.listenerCount());
  }
}








