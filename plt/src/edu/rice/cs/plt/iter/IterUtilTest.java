package edu.rice.cs.plt.iter;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;
import static edu.rice.cs.plt.iter.IterUtil.*;

/**
 * Tests for the IterUtil methods
 */
public class IterUtilTest extends TestCase {

  private void assertEquals(int expected, Integer actual) {
    assertEquals(expected, actual.intValue());
  }
  
  private void assertEquals(char expected, Character actual) {
    assertEquals(expected, actual.charValue());
  }
  
  public void testAsArray() {
    String[] strings;
    
    strings = asArray(IterUtil.<String>empty(), String.class);
    assertTrue(Arrays.equals(new String[0], strings));
    
    strings = asArray(make("foo", "bar", "baz"), String.class);
    assertTrue(Arrays.equals(new String[]{ "foo", "bar", "baz" }, strings));
    try { ((Object[]) strings)[0] = new Object(); }
    catch (ArrayStoreException e) { return; /* correct behavior */ }
    fail("Stored an Object in a String[]");
  }

  public void testArrayIterable() {
    assertTrue(isEmpty(asIterable(new String[0])));
    assertTrue(isEmpty(asIterable(new boolean[0])));
    assertTrue(isEmpty(asIterable(new char[0])));
    assertTrue(isEmpty(asIterable(new byte[0])));
    assertTrue(isEmpty(asIterable(new short[0])));
    assertTrue(isEmpty(asIterable(new int[0])));
    assertTrue(isEmpty(asIterable(new long[0])));
    assertTrue(isEmpty(asIterable(new float[0])));
    assertTrue(isEmpty(asIterable(new double[0])));
    
    assertTrue(isEmpty(arrayAsIterable((Object) new String[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new boolean[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new char[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new byte[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new short[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new int[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new long[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new float[0])));
    assertTrue(isEmpty(arrayAsIterable((Object) new double[0])));
    try {
      arrayAsIterable(new Object());
      fail("Expected an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {}
    
    int[] ints = { 1, 1, 2, 3, 5 };
    Iterator<Integer> intIter = asIterable(ints).iterator();
    assertTrue(intIter.hasNext());
    assertEquals(1, intIter.next());
    assertTrue(intIter.hasNext());
    assertEquals(1, intIter.next());
    assertTrue(intIter.hasNext());
    assertEquals(2, intIter.next());
    assertTrue(intIter.hasNext());
    assertEquals(3, intIter.next());
    assertTrue(intIter.hasNext());
    assertEquals(5, intIter.next());
    assertFalse(intIter.hasNext());
  }
  
  public void testCharSequenceIterable() {
    assertTrue(isEmpty(asIterable("")));

    Iterator<Character> iter = asIterable("Happy day").iterator();
    assertTrue(iter.hasNext());
    assertEquals('H', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('a', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('p', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('p', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('y', iter.next());
    assertTrue(iter.hasNext());
    assertEquals(' ', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('d', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('a', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('y', iter.next());
    assertFalse(iter.hasNext());
  }
  
  public void testReaderAsIterator() {
    assertFalse(asIterator(new StringReader("")).hasNext());
    
    Iterator<Character> iter = asIterator(new StringReader("Foo"));
    assertTrue(iter.hasNext());
    assertEquals('F', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('o', iter.next());
    assertTrue(iter.hasNext());
    assertEquals('o', iter.next());
    assertFalse(iter.hasNext());
  }
  
  public void testInputStreamAsIterator() {
    assertFalse(asIterator(new ByteArrayInputStream(new byte[0])).hasNext());
    
    byte[] bytes = { 1, 15, 3 };
    Iterator<Byte> iter = asIterator(new ByteArrayInputStream(bytes));
    assertTrue(iter.hasNext());
    assertEquals(1, (byte) iter.next());
    assertTrue(iter.hasNext());
    assertEquals(15, (byte) iter.next());
    assertTrue(iter.hasNext());
    assertEquals(3, (byte) iter.next());
    assertFalse(iter.hasNext());
  }
  
}
