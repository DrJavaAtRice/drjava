/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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
  
  public void testRelax() {
    List<Integer> is = new LinkedList<Integer>();
    Iterable<Number> ns = IterUtil.<Number>relax(is);
    is.add(1);
    is.add(2);
    is.add(3);
    Iterator<Integer> iIter = is.iterator();
    Iterator<Number> nIter = IterUtil.<Number>relax(iIter);
    assertEquals(Integer.valueOf(1), nIter.next());
    assertEquals(Integer.valueOf(2), nIter.next());
    assertEquals(Integer.valueOf(3), nIter.next());
    assertFalse(nIter.hasNext());
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
