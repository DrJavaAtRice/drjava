/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Tests the OneToOneHashMap class (including its implementations of Sets and Iterators).
 * TODO: These tests could be better-organized and less redundant; a lot of functionality
 * is not tested.
 */
public class OneToOneHashMapTest extends TestCase {
  
  public void testSearch() {
    Double dbl1 = new Double(.1);
    Double dbl2 = new Double(.2);
    Double dbl3 = new Double(.3);
    
    Set<Double> dbls = new HashSet<Double>(Arrays.asList(dbl1, dbl2, dbl3));

    Integer int1 = new Integer(1);
    Integer int2 = new Integer(2);
    Integer int3 = new Integer(3);
    
    Set<Integer> ints = new HashSet<Integer>(Arrays.asList(int1, int2, int3));
     
    OneToOneHashMap<Integer, Double> iTod = new OneToOneHashMap<Integer, Double>();
    
    assertTrue("Empty map is empty", iTod.isEmpty());
    assertTrue("Empty map has no entries", iTod.entrySet().isEmpty());
    assertTrue("Empty map has no keys", iTod.keySet().isEmpty());
    assertTrue("Empty map has no values", iTod.values().isEmpty());

    assertEquals("Initial size of 0", iTod.size(), 0);

    assertFalse("Should not find non-existent key", iTod.containsKey(int1));
    assertFalse("Should not find non-existent key", iTod.containsKey(int2));
    assertFalse("Should not find non-existent key", iTod.containsKey(int3));

    assertFalse("Should not find non-existent value", iTod.containsValue(dbl1));
    assertFalse("Should not find non-existent value", iTod.containsValue(dbl2));
    assertFalse("Should not find non-existent value", iTod.containsValue(dbl3));

    iTod.put(int1, dbl1);
    
    assertFalse("NonEmpty map is not empty", iTod.isEmpty());
    assertFalse("NonEmpty map has some entries", iTod.entrySet().isEmpty());
    assertFalse("NonEmpty map has some keys", iTod.keySet().isEmpty());
    assertFalse("NonEmpty map has some values", iTod.values().isEmpty());
    
    assertTrue("Should find key", iTod.containsKey(int1));
    assertFalse("Should not find non-existent key", iTod.containsKey(int2));
    assertFalse("Should not find non-existent key", iTod.containsKey(int3));

    assertTrue("Should find value", iTod.containsValue(dbl1));
    assertFalse("Should not find non-existent value", iTod.containsValue(dbl2));
    assertFalse("Should not find non-existent value", iTod.containsValue(dbl3));

    iTod.put(int2, dbl2);
    iTod.put(int3, dbl3);
    
    assertEquals("Key set is as expected", ints, iTod.keySet());
    assertEquals("Value set is as expected", dbls, iTod.values());
    
               
    Iterator<Double> it = iTod.values().iterator();
    try {
      it.remove();
      fail("Removing non-existent element should generate IllegalStateException");
    } catch(IllegalStateException e) { }

    Double val = it.next();
    Integer key = iTod.getKey(val);
    iTod.removeKey(key);
    assertEquals("Size should be 2", 2, iTod.size());
    assertTrue("Iterator should be non empty", it.hasNext());

    assertFalse("Should not find non-existant key", iTod.containsKey(key));
    assertFalse("Should not find non-existant key", iTod.containsValue(val));

    it = iTod.values().iterator();
    val = it.next();
    key = iTod.getKey(val);
    it.remove();
    assertEquals("Size should be 1", 1, iTod.size());
    assertTrue("Iterator should be non empty", it.hasNext());

    assertFalse("Should not find non-existant key", iTod.containsKey(key));
    assertFalse("Should not find non-existant value", iTod.containsValue(val));


    iTod.clear();
    assertTrue("Map is empty after clear", iTod.isEmpty());
  }
  
  public void testRemove() {
    Double dbl1 = new Double(.1);
    Double dbl2 = new Double(.2);
    Double dbl3 = new Double(.3);

    Integer int1 = new Integer(1);
    Integer int2 = new Integer(2);
    Integer int3 = new Integer(3);
    OneToOneHashMap<Double, Integer> dToi = new OneToOneHashMap<Double, Integer>();

    assertEquals("Initial size of 0", dToi.size(), 0);
    dToi.clear();
    assertEquals("Initial size of 0", dToi.size(), 0);

    dToi.put(dbl1, int1);
    assertEquals("Size should be 1", dToi.size(), 1);
    dToi.put(dbl2, int2);
    assertEquals("Size should be 2", dToi.size(), 2);
    dToi.put(dbl3, int3);
    assertEquals("Size should be 3", dToi.size(), 3);

    dToi.removeValue(int1);
    assertEquals("Size should be 2", dToi.size(), 2);

    // Test of removeValue
    assertEquals("Removing key should return associated value", null, dToi.removeValue(int1));
    assertEquals("Size should be 2", dToi.size(), 2);
    dToi.put(dbl1, int1);
    assertEquals("Size should be 3", dToi.size(), 3);
    dToi.put(dbl3, int3);
    assertEquals("Size should be 3", dToi.size(), 3);

    // Test of removeKey
    dToi.removeKey(dbl3);
    assertEquals("Size should be 2", dToi.size(), 2);
    assertEquals("Removing value should return associated key", int2, dToi.removeKey(dbl2));

    assertEquals("Size should be 1", dToi.size(), 1);
    dToi.put(dbl3, int3);
    assertEquals("Size should be 2", dToi.size(), 2);

    dToi.clear();
    assertEquals("Cleared size of 0", dToi.size(), 0);

    assertFalse("Iterator to cleared list should be empty", dToi.values().iterator().hasNext());
    assertFalse("Iterator to cleared list should be empty", dToi.keySet().iterator().hasNext());
    assertFalse("Iterator to cleared list should be empty", dToi.entrySet().iterator().hasNext());
  }

  public void testPut() {
    
    String one = "1";
    String two = "2";
    String three = "3";
    
    Integer int1 = new Integer(1);
    Integer int2 = new Integer(2);
    Integer int3 = new Integer(3);
    
    OneToOneHashMap<String, Integer> myhash = new OneToOneHashMap<String, Integer>();

    assertEquals("Expected size of 0", 0, myhash.size());

    assertEquals("Expected null", null, myhash.getValue(one));
    assertEquals("Expected null", null, myhash.getValue(two));
    assertEquals("Expected null", null, myhash.getValue(three));

    assertEquals("Expected null", null, myhash.getKey(int1));
    assertEquals("Expected null", null, myhash.getKey(int2));
    assertEquals("Expected null", null, myhash.getKey(int3));

    myhash.put(one, int1);
    myhash.put(two, int2);
    myhash.put(three, int3);

    assertTrue("Given one, should get 1", myhash.getValue(one) == int1);
    assertTrue("Given two, should get 2", myhash.getValue(two) == int2);
    assertTrue("Given three, should get 3", myhash.getValue(three) == int3);
    
    assertTrue("Given 1, should get one", myhash.getKey(int1) == one);
    assertTrue("Given 2, should get two", myhash.getKey(int2) == two);
    assertTrue("Given 3, should get three", myhash.getKey(int3) == three);
  }
  
}