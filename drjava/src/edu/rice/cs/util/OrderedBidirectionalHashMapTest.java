package edu.rice.cs.util;
import junit.framework.TestCase;
import java.util.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class OrderedBidirectionalHashMapTest extends TestCase {
  
  
  /**
   * A test method.
   * (Replace "X" with a name describing the test.  You may write as
   * many "testSomething" methods in this class as you wish, and each
   * one will be called when running JUnit over this class.)
   */
  public void testPut() {
    
    String one = "1";
    String two = "2";
    String three = "3";
    
    Integer int1 = new Integer(1);
    Integer int2 = new Integer(2);
    Integer int3 = new Integer(3);
    
    OrderedBidirectionalHashMap<String, Integer> myhash = new OrderedBidirectionalHashMap<String, Integer>();
    
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
    
    Iterator<Integer> it = myhash.valuesIterator();
    try { 
      it.remove();
      fail("Removing non-existent element should generate IllegalStateException");
    } catch(IllegalStateException e) {}
    
    Integer value = it.next();
    String key = myhash.getKey(value);
    assertEquals("key and value should match", value.toString(), key);
    it.remove();
    assertEquals("After removing key, it should not appear in map", null, myhash.getValue(key));
    assertEquals("After removing value, it should not appear in map", null, myhash.getKey(value));
    
    value = it.next();
    key = myhash.getKey(value);
    assertEquals("key and value should match", value.toString(), key);
    it.remove();
    assertEquals("After removing key, it should not appear in map", null, myhash.getValue(key));
    assertEquals("After removing value, it should not appear in map", null, myhash.getKey(value));
    
    value = it.next();
    key = myhash.getKey(value);
    assertEquals("key and value should match", value.toString(), key);
    it.remove();
    assertEquals("After removing key, it should not appear in map", null, myhash.getValue(key));
    assertEquals("After removing value, it should not appear in map", null, myhash.getKey(value));
    
    /* myhash should be empty now */
    it = myhash.valuesIterator();
    assertFalse("Map should be empty", it.hasNext());
  }
  
}
