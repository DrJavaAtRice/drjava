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

package edu.rice.cs.plt.iter;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

import edu.rice.cs.plt.lambda.Lambda;
import static edu.rice.cs.plt.iter.IterUtil.*;

/**
 * Tests for the IterUtil methods
 */
public class IterUtilTest extends TestCase {
  
  /**
   * Verify that the iterator contents exactly match the given sequence.  Tests both
   * {@code hasNext()} and {@code next()} at each step.
   */
  public static void assertIterator(Iterator<?> iter, Object... expected) {
    for (Object exp : expected) {
      assertTrue("Iterator shorter than expected", iter.hasNext());
      assertEquals("Unexpected iterator element", exp, iter.next());
    }
    assertFalse("Iterator longer than expected", iter.hasNext());
  }

  /**
   * Verify that the iterator contents exactly match the given sequence.  Does <em>not</em>
   * invoke {@code hasNext()} until iteration is complete.  This allows bugs related
   * to state change occurring in {@code hasNext()} to be detected.
   */
  public static void assertIteratorUnchecked(Iterator<?> iter, Object... expected) {
    for (Object exp : expected) {
      assertEquals("Unexpected iterator element", exp, iter.next());
    }
    assertFalse("Iterator longer than expected", iter.hasNext());
  }

  public void testRelax() {
    List<Integer> is = new LinkedList<Integer>();
    @SuppressWarnings("unused") Iterable<Number> ns = IterUtil.<Number>relax(is);
    is.add(1);
    is.add(2);
    is.add(3);
    Iterator<Integer> iIter = is.iterator();
    Iterator<Number> nIter = IterUtil.<Number>relax(iIter);
    assertIterator(nIter, 1, 2, 3);
  }
  
  public void testToArray() {
    String[] strings;
    
    strings = toArray(IterUtil.<String>empty(), String.class);
    assertTrue(Arrays.equals(new String[0], strings));
    
    strings = toArray(make("foo", "bar", "baz"), String.class);
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
    assertIterator(intIter, 1, 1, 2, 3, 5);
  }
  
  public void testCharSequenceIterable() {
    assertTrue(isEmpty(asIterable("")));

    Iterator<Character> iter = asIterable("Happy day").iterator();
    assertIterator(iter, 'H', 'a', 'p', 'p', 'y', ' ', 'd', 'a', 'y');
  }
  
  public void testReaderAsIterator() {
    assertFalse(asIterator(new StringReader("")).hasNext());
    
    Iterator<Character> iter = asIterator(new StringReader("Foo"));
    assertIterator(iter, 'F', 'o', 'o');
  }
  
  public void testInputStreamAsIterator() {
    assertFalse(asIterator(new ByteArrayInputStream(new byte[0])).hasNext());
    
    byte[] bytes = { 1, 15, 3 };
    Iterator<Byte> iter = asIterator(new ByteArrayInputStream(bytes));
    assertIterator(iter, (byte) 1, (byte) 15, (byte) 3);
  }
  
  public void testDistribute() {
    String[][] orig = { { "a", "b" }, { "c", "d", "e" } };
    String[][] expected = { { "a", "c" }, { "a", "d" }, { "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" } };
    class ArrayElements<T> implements Lambda<T[], Iterable<T>> {
      public Iterable<T> value(T[] arg) { return asIterable(arg); }
    }
    class MakeArray<T> implements Lambda<Iterable<T>, T[]> {
      private final Class<T> _c;
      public MakeArray(Class<T> c) { _c = c; }
      public T[] value(Iterable<T> arg) { return toArray(arg, _c); }
    }
    String[][] result = distribute(orig, new ArrayElements<String[]>(), new ArrayElements<String>(),
                                   new MakeArray<String>(String.class), new MakeArray<String[]>(String[].class));
    assertTrue(Arrays.deepEquals(expected, result));
  }
  
}
