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

package edu.rice.cs.plt.text;

import junit.framework.TestCase;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.text.TextUtil.*;

public class TextUtilTest extends TestCase {
  
  public void testToString() {
    assertEquals("null", TextUtil.toString(null));
    assertEquals("fish", TextUtil.toString("fish"));
    assertEquals("12", TextUtil.toString(12));
    assertEquals("{ 1, 2, 3 }", TextUtil.toString(new int[]{ 1, 2, 3 }));
    assertEquals("{ null, null, null, null }", TextUtil.toString(new Object[4]));
    Object[] array = new Object[4];
    array[2] = array;
    assertEquals("{ null, null, { ... }, null }", TextUtil.toString(array));
  }
  
  public void testGetLines() {
    assertTrue(IterUtil.isEqual(IterUtil.empty(), getLines("")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a"), getLines("a")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a"), getLines("a\n")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a", "", "", ""), getLines("a\n\n\n\n")));
    assertTrue(IterUtil.isEqual(IterUtil.make("", "a"), getLines("\na")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a", "b", "c", "d", "", "e"), getLines("a\nb\r\nc\rd\n\re")));
  }
  
  public void testRepeat() {
    assertEquals("ababab", repeat("ab", 3));
    assertEquals("", repeat("fish are fun", 0));
    assertEquals("bbbbbb", repeat('b', 6));
    assertEquals("", repeat('x', 0));
  }
  
  public void testContains() {
    assertTrue(contains("foo", 'f'));
    assertTrue(contains("foo", 'o'));
    assertFalse(contains("foo", 'p'));
    assertTrue(contains("foo", "f"));
    assertTrue(contains("foo", ""));
    assertTrue(contains("foo", "oo"));
    assertTrue(contains("foo", "foo"));
    assertFalse(contains("foo", "food"));
    assertFalse(contains("foo", "F"));
    assertFalse(contains("Foo", "f"));
  }
  
  public void testContainsIgnoreCase() {
    assertTrue(containsIgnoreCase("foo", "f"));
    assertTrue(containsIgnoreCase("foo", "F"));
    assertTrue(containsIgnoreCase("Foo", "F"));
    assertTrue(containsIgnoreCase("fOo", "oO"));
    assertTrue(containsIgnoreCase("Foo", ""));
    assertTrue(containsIgnoreCase("Foo", "foo"));
    assertFalse(containsIgnoreCase("Foo", "Food"));
  }
  
}
