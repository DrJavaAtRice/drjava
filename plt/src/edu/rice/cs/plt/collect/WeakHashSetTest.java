/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.collect;

import junit.framework.TestCase;

import edu.rice.cs.plt.lambda.Condition;

/**
 * A JUnit test case class for {@link WeakHashSet}.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class WeakHashSetTest extends TestCase {
  /** Maximum number of GCs that will be requested before failing the test */
  private static final int MAX_GC_COUNT = 12;
  
  /** The {@code WeakHashSet} used during testing. */
  private WeakHashSet<Integer> intSet;
  
  /** Initalizes the test environment */
  public void setUp() {
    intSet = new WeakHashSet<Integer>();
  }
  
  /**
   * Tests that the {@code add} and {@code contains} methods work.
   */
  public void testAddAndContains() {
    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);
    
    assertFalse("Does not contain 1", intSet.contains(one));
    assertFalse("Does not contain 2", intSet.contains(two));
    assertFalse("Does not contain 3", intSet.contains(three));
    
    assertTrue("Added one", intSet.add(one));
    
    assertTrue("Contains 1", intSet.contains(one));
    assertFalse("Does not contain 2", intSet.contains(two));
    assertFalse("Does not contain 3", intSet.contains(three));
    
    assertTrue("Added three", intSet.add(three));
    
    assertTrue("Contains 1", intSet.contains(one));
    assertTrue("Contains 3", intSet.contains(three));
    assertFalse("Does not contain 2", intSet.contains(two));
    
    assertTrue("Added two", intSet.add(two));
    
    assertTrue("Contains 1", intSet.contains(one));
    assertTrue("Contains 3", intSet.contains(three));
    assertTrue("Contains 2", intSet.contains(two));
    
    assertFalse("Did not add two again", intSet.add(two));
    assertFalse("Did not add three again", intSet.add(three));
    
    assertTrue("Contains 1", intSet.contains(one));
    assertTrue("Contains 2", intSet.contains(two));
    assertTrue("Contains 3", intSet.contains(three));
  }
  
  /**
   * Tests that the {@code size} method works.
   */
  public void testSize() {
    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);
    
    assertSame("Empty set", 0, intSet.size());
    
    intSet.add(one);
    assertSame("One element", 1, intSet.size());
    
    intSet.add(two);
    intSet.add(three);
    assertSame("Three elements", 3, intSet.size());
    
    intSet.add(two);
    assertSame("Still three elements", 3, intSet.size());
  }
  
  /**
   * Tests that the {@code clear()} method works.
   */
  public void testClear() {
    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);
    
    assertSame("Empty set", 0, intSet.size());
    
    intSet.add(one);
    intSet.add(two);
    intSet.add(three);
    assertSame("Three elements", 3, intSet.size());
    
    intSet.clear();
    assertSame("No more elements", 0, intSet.size());
  }
  
  /**
   * Tests that the {@code remove} method works.
   */
  public void testRemove() {
    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);
    
    assertSame("Empty set", 0, intSet.size());
    
    intSet.add(one);
    intSet.add(two);
    intSet.add(three);
    
    assertSame("Three elements", 3, intSet.size());
    assertTrue("Contains 1", intSet.contains(one));
    assertTrue("Contains 2", intSet.contains(two));
    assertTrue("Contains 3", intSet.contains(three));
    
    assertTrue("Removed two", intSet.remove(two));
    assertSame("Two elements", 2, intSet.size());
    assertFalse("Contains 2", intSet.contains(two));
    assertTrue("Contains 1", intSet.contains(one));
    assertTrue("Contains 3", intSet.contains(three));
    
    assertFalse("No need to remove two", intSet.remove(two));
    
    assertTrue("Removed one", intSet.remove(one));
    assertSame("One element", 1, intSet.size());
    assertFalse("Contains 1", intSet.contains(one));
    assertFalse("Contains 2", intSet.contains(two));
    assertTrue("Contains 3", intSet.contains(three));
  }
  
  /**
   * Tests that the set's {@link java.util.Iterator} returns all the elements
   * from the set and no more.
   */
  public void testIterator() {
    Integer[] ints = {new Integer(1), new Integer(2), new Integer(3)};
  
    for (int j = 0; j < ints.length; ++j) {
      intSet.add(ints[j]);
    }
    
    assertSame("Three elements", 3, intSet.size());
    assertTrue("Contains 1", intSet.contains(ints[0]));
    assertTrue("Contains 2", intSet.contains(ints[1]));
    assertTrue("Contains 3", intSet.contains(ints[2]));
    
    for (Integer intInSet : intSet) {
      assertTrue(intInSet != null);
      
      int idx = -1;
      for (int j = 0; j < ints.length; ++j) {
        if (ints[j] != null && ints[j].equals(intInSet)) {
          idx = j;
          break;
        }
      }
      
      if (idx >= 0) {
        ints[idx] = null; //set as found
      } else {
        fail("There was an int in the set that was not in the array.");
      }
    }
    
    for (Integer i : ints) {
      if (i != null) {
        fail("Not all ints were found.");
      }
    }
  }
  
  /**
   * Tests that objects with no strong references are automatically
   * removed from the set by a garbage collection.
   */
  public void testAutomaticRemoval() { 
    Integer one = new Integer(1);
    Integer two = new Integer(2);
    Integer three = new Integer(3);
    
    assertSame("Empty set", 0, intSet.size());
    
    intSet.add(one);
    intSet.add(two);
    intSet.add(three);
    
    assertSame("Three elements", 3, intSet.size());
    
    one = null;
    final WeakHashSet<Integer> intSetForThunk1 = intSet;
    runGCUntil(new Condition() {
      public boolean isTrue() { return intSetForThunk1.size() == 2; }
    });
    
    assertSame("one removed", 2, intSet.size());
    assertTrue("two still there", intSet.contains(two));
    assertTrue("three still there", intSet.contains(three));
    
    two = null;
    three = null;
    final WeakHashSet<Integer> intSetForThunk2 = intSet;
    runGCUntil(new Condition() {
      public boolean isTrue() { return intSetForThunk2.size() == 0; }
    });
    
    assertSame("Set empty again", 0, intSet.size());
  }
  
  /**
   * Asks the system to GC until doneP returns {@code true} or
   * a GC has been requested more than MAX_GC_COUNT times.
   * 
   * @param done A thunk that should return {@code true} when enough GCs have occurred
   * @see System#gc()
   */
  private void runGCUntil(Condition done) {
    int gcCount = 0;
    
    while (!done.isTrue() && gcCount < MAX_GC_COUNT) {
      System.runFinalization();
      System.gc();
      gcCount++;
    }
    
    if (gcCount >= MAX_GC_COUNT) {
      fail("Too many GCs required.");
    }
  }
}
