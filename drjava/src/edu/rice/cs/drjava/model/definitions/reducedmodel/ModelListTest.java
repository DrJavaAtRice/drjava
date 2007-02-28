/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Tests the functionality of the ModelList list class.
 * @version $Id$
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
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertTrue("#0.1", fFull.isEmpty());
    assertEquals("#0.2", 0, fEmpty.length());
    assertEquals("#0.3", 0, fFull.length());
    assertTrue("#0.4", itEmpty.atStart());
    assertTrue("#0.5", itFull.atStart());
    itFull.insert(new Integer(5));
    assertTrue("#1.0", !itFull.atStart());
    assertEquals("#1.1", 1, fFull.length());
    assertEquals("#1.2", new Integer(5), itFull.current());
    assertTrue("#2.0", fEmpty.isEmpty());
    assertTrue("#2.1", !fFull.isEmpty());
    itFull.insert(new Integer(4));
    assertEquals("#2.2", 2, fFull.length());
    assertEquals("#2.3", new Integer(4), itFull.current());
    assertTrue("#2.4", !fFull.isEmpty());
  }

  public void testInsertFront() {
    fFull.insertFront(new Integer(3));
    fFull.insertFront(new Integer(2));
    fFull.insertFront(new Integer(1));
    fFull.insertFront(new Integer(0));
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    for (int i = 0; i < 4; i++) {
      itFull.next();
      assertEquals(new Integer(i), itFull.current());
    }
  }

  public void testRemove() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertEquals("#0.1", 0, fEmpty.length());
    assertEquals("#0.2", 0, fFull.length());
    try {
      itEmpty.remove();
      assertTrue("#1.0", false);
    }
    catch (Exception e) {
    }

    itFull.insert(new Integer(5));
    assertTrue("#2.0", !fFull.isEmpty());
    assertEquals("#2.1", 1, fFull.length());
    itFull.remove();
    assertTrue("#3.0", fFull.isEmpty());
    assertEquals("#3.1", 0, fFull.length());
  }

  public void testNext() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();

    //test going past end of list
    try {
      itEmpty.next();
      itEmpty.next();
      assertTrue("#0.0", false);
    }
    catch (Exception e) {
    }

    itFull.insert(new Integer(6));
    itFull.insert(new Integer(5));
    itFull.insert(new Integer(4));
    //now at start of list, after head.
    assertEquals("#1.0", new Integer(4), itFull.current());
    itFull.next();
    assertEquals("#1.1", new Integer(5), itFull.current());
    itFull.next();
    assertEquals("#1.2", new Integer(6), itFull.current());
    itFull.next();
    try {
      itFull.next();
      assertTrue("#1.4", false);
    }
    catch (Exception f) {
    }
  }

  public void testPrev() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();

    try {
      itEmpty.prev();
      assertTrue("#0.0", false);
    }
    catch (Exception e) {
    }

    itFull.insert(new Integer(6));
    itFull.insert(new Integer(5));
    itFull.insert(new Integer(4));
    itFull.next();
    itFull.next();
    itFull.next();

    itFull.prev();
    assertEquals("#1.1", new Integer(6), itFull.current());
    itFull.prev();
    assertEquals("#1.2", new Integer(5), itFull.current());
    itFull.prev();
    assertEquals("#1.3", new Integer(4), itFull.current());
    itFull.prev();

    try {
      itFull.prev();
      assertTrue("#1.5", false);
    }
    catch (Exception f) {
    }
  }

  public void testCurrent() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.current();
      fail("Current call in initial position did not fail.");
    }
    catch (RuntimeException e) {
      //This call was supposed to throw an exception
            assertEquals("current() throws exception when at end",
                   e.getMessage(),
                   "Attempt to call current on an iterator in the initial position");
    }
    itFull.next();
    try {
      itFull.current();
      fail("Current call in final position did not fail.");
    }
    catch (RuntimeException e) {
      //This call was supposed to throw an exception
      assertEquals("current() throws exception when at end",
                   e.getMessage(),
                   "Attempt to call current on an iterator in the final position");
    }
  }

  public void testPrevItem() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.prevItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }

    itFull.insert(new Integer(0));
    try {
      itFull.prevItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }
    itFull.insert(new Integer(1));
    itFull.next();
    assertEquals("#0.0", new Integer(1), itFull.prevItem());
  }

  public void testNextItem() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.nextItem();
      assertTrue(false);
    }
    catch (Exception e) { }

    itFull.insert(new Integer(0));
    try {
      itFull.nextItem();
      assertTrue(false);
    }
    catch (Exception e) { /* should reach here */ }
    assertEquals("#0.2", new Integer(0), itFull.current());
    itFull.insert(new Integer(1));
    assertEquals("#0.1", new Integer(1), itFull.current());
    assertEquals("#0.0", new Integer(0), itFull.nextItem());
  }
  
  public void testCollapse() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    ModelList<Integer>.Iterator itEmpty2 = itEmpty.copy();
    assertEquals("#0.0", 0, fEmpty.length());
    assertEquals("#0.1", 0, itEmpty.pos());
    itEmpty.collapse(itEmpty2);
    assertEquals("#0.0", 0, fEmpty.length());
    assertEquals("#0.2", 0, itFull.pos());

    itFull.insert(new Integer(6));
    assertEquals("#0.3", 1, itFull.pos());
    ModelList<Integer>.Iterator itFull2 = itFull.copy();
    assertEquals("#0.4", 1, itFull2.pos());
    
    assertEquals("#1.0", 1, fFull.length());
    itFull.collapse(itFull2);
    assertEquals("#1.1", 1, fFull.length());
    assertEquals("#1.2", 1, itFull2.pos());
    assertEquals("#1.3", 1, itFull.pos());

    itFull.insert(new Integer(5));
    assertEquals("#2.0", 2, fFull.length());
    assertEquals("#2.2", 1, itFull.pos());
    assertEquals("#2.3", 2, itFull2.pos());
    itFull.collapse(itFull2);
    assertEquals("#2.1", 2, fFull.length());

    //collapse to the right
    itFull.insert(new Integer(4));
    assertEquals("#3.0", 3, fFull.length());
    assertEquals("#3.0b",new Integer(4),itFull.current());
    assertEquals("#3.0a", new Integer(6), itFull2.current());
    assertEquals("#3.0h", 3, itFull2.pos());
    itFull.collapse(itFull2);
    assertEquals("3.0d", new Integer(6), itFull2.current());
    assertEquals("3.0e", 2, itFull2.pos());
    assertEquals("3.0f", new Integer(4), itFull.current());
    assertEquals("3.0g", 1, itFull.pos());
    itFull.next();
    assertEquals("#3.0c",new Integer(6),itFull.current());
    assertEquals("#3.1", 2, fFull.length());
    itFull.prev();
    assertEquals("#4.0", new Integer(4), itFull.current());
    assertEquals("#4.1", new Integer(6), itFull2.current());
    
    //collapse to the left
    itFull.insert(new Integer(7));
    assertEquals("#5.0a", 3, fFull.length());
    assertEquals("#5.0b", new Integer(7), itFull.current());
    assertEquals("#5.0c", new Integer(6), itFull2.current());
    itFull2.collapse(itFull);
    assertEquals("#5.1a", 2, fFull.length());
    assertEquals("#5.1b", new Integer(7), itFull.current());
    assertEquals("#5.1c", new Integer(6), itFull2.current());
    assertEquals("#5.2a", new Integer(6), itFull.nextItem());
    assertEquals("#5.2b", new Integer(7), itFull2.prevItem());
  }

  public void testNotifyInsert() {
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();

    itFull2.insert(new Integer(0));
    ModelList<Integer>.Iterator itFull = itFull2.copy();
    itFull2.insert(new Integer(1));
    assertEquals(new Integer(0), itFull.current());
  }

  public void testNotifyRemove() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();

    itFull2.insert(new Integer(0));
    itFull2.insert(new Integer(1));
    itFull.next();
    assertEquals("#0.1", new Integer(1), itFull.current());
    itFull2.remove();
    assertEquals("#0.0", new Integer(0), itFull.current());
  }

  public void testNotifyCollapse() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();
    ModelList<Integer>.Iterator itFull3 = fFull.getIterator();

    itFull2.insert(new Integer(0));
    itFull2.insert(new Integer(1));
    itFull2.insert(new Integer(2));
    itFull2.insert(new Integer(3));
    itFull2.insert(new Integer(4));

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
    assertEquals("#0.0", new Integer(2), itFull.current());
    assertEquals("#0.1", new Integer(0), itFull3.current());
    itFull2.collapse(itFull3);

    assertEquals("#1.0", new Integer(4), itFull2.current());
    assertEquals("#1.1", new Integer(0), itFull3.current());
    assertEquals("#1.2", new Integer(0), itFull.current());
  }
  
  public void testListenerCount() {
    ModelList<Character> testList = new ModelList<Character>();
    
    assertEquals("No iterators", 0, testList.listenerCount());
    
    ModelList<Character>.Iterator iter1 = testList.getIterator();
    
    assertEquals("One iterator", 1, testList.listenerCount());
    
    ModelList<Character>.Iterator iter2 = testList.getIterator();
    
    assertEquals("Two iterators", 2, testList.listenerCount());
    
    iter1.dispose();
    iter1 = null;
    
    assertEquals("Removed first iterator", 1, testList.listenerCount());
    
    iter2.dispose();
    iter2 = null;
    
    assertEquals("Removed second iterator", 0, testList.listenerCount());
  }
}








