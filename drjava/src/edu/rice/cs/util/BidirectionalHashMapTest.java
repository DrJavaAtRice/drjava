package edu.rice.cs.util;
import junit.framework.TestCase;
import java.util.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class BidirectionalHashMapTest extends TestCase {
  
  
  /**
   * A test method.
   * (Replace "X" with a name describing the test.  You may write as
   * many "testSomething" methods in this class as you wish, and each
   * one will be called when running JUnit over this class.)
   */
  public void testPut() {
    
    String dog = "dog";
    String cat = "cat";
    String mouse = "mouse";
    
    Vector vdog = new Vector();
    Vector vcat = new Vector();
    Vector vmouse = new Vector();
    
    vdog.add(dog);
    vcat.add(cat);
    vmouse.add(mouse);
    
    BidirectionalHashMap<String, Vector> myhash = new BidirectionalHashMap<String, Vector>();
    
    assertEquals("Expected null", null, myhash.getValue(dog));
    assertEquals("Expected null", null, myhash.getValue(cat));
    assertEquals("Expected null", null, myhash.getValue(mouse));
    
    assertEquals("Expected null", null, myhash.getKey(vdog));
    assertEquals("Expected null", null, myhash.getKey(vcat));
    assertEquals("Expected null", null, myhash.getKey(vmouse));
                 
    myhash.put(dog, vdog);
    myhash.put(cat, vcat);
    myhash.put(mouse, vmouse);
    
    assertTrue("Given dog, should get vdog", myhash.getValue(dog) == vdog);
    assertTrue("Given cat, should get vcat", myhash.getValue(cat) == vcat);
    assertTrue("Given mouse, get vmouse", myhash.getValue(mouse) == vmouse);
    
  }
  
}
