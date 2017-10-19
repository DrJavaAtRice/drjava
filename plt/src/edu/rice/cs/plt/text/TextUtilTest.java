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

package edu.rice.cs.plt.text;

import java.util.ArrayList;
import java.util.List;

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
  
  public void testPrefixSuffix() {
    assertEquals("pre", prefix("pre:body:suf", ':'));
    assertEquals("body:suf", removePrefix("pre:body:suf", ':'));
    assertEquals("suf", suffix("pre:body:suf", ':'));
    assertEquals("pre:body", removeSuffix("pre:body:suf", ':'));

    assertEquals("abc", prefix("abc", ':'));
    assertEquals("abc", removePrefix("abc", ':'));
    assertEquals("abc", suffix("abc", ':'));
    assertEquals("abc", removeSuffix("abc", ':'));

    assertEquals("", prefix("", ':'));
    assertEquals("", removePrefix("", ':'));
    assertEquals("", suffix("", ':'));
    assertEquals("", removeSuffix("", ':'));

    assertEquals("", prefix(":", ':'));
    assertEquals("", removePrefix(":", ':'));
    assertEquals("", suffix(":", ':'));
    assertEquals("", removeSuffix(":", ':'));

    assertEquals("pre", prefix("pre:", ':'));
    assertEquals("", removePrefix("pre:", ':'));
    assertEquals("", suffix("pre:", ':'));
    assertEquals("pre", removeSuffix("pre:", ':'));
  }
  
  public void testSplit() {
    assertSplitString(split("abc", "\\$"), "abc");
    
    assertSplitString(split("a$b$c", "\\$"), "a", "$", "b", "$", "c");
    assertSplitString(split("a$b$c", "\\$", 0), "a", "$", "b", "$", "c");
    assertSplitString(split("a$b$c", "\\$", 1), "a$b$c");
    assertSplitString(split("a$b$c", "\\$", 2), "a", "$", "b$c");
    assertSplitString(split("a$b$c", "\\$", 3), "a", "$", "b", "$", "c");
    assertSplitString(split("a$b$c", "\\$", 4), "a", "$", "b", "$", "c");
    
    assertSplitString(split("a$(b$c)$d", "\\$"), "a", "$", "(b", "$", "c)", "$", "d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$"), "a", "$", "(b$c)", "$", "d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$", 0), "a", "$", "(b$c)", "$", "d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$", 1), "a$(b$c)$d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$", 2), "a", "$", "(b$c)$d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$", 3), "a", "$", "(b$c)", "$", "d");
    assertSplitString(splitWithParens("a$(b$c)$d", "\\$", 4), "a", "$", "(b$c)", "$", "d");

    assertSplitString(split("a$((b$c)$d)$e", "\\$"), "a", "$", "((b", "$", "c)", "$", "d)", "$", "e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$"), "a", "$", "((b$c)$d)", "$", "e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$", 0), "a", "$", "((b$c)$d)", "$", "e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$", 1), "a$((b$c)$d)$e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$", 2), "a", "$", "((b$c)$d)$e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$", 3), "a", "$", "((b$c)$d)", "$", "e");
    assertSplitString(splitWithParens("a$((b$c)$d)$e", "\\$", 4), "a", "$", "((b$c)$d)", "$", "e");
    
    // TODO: use multiple brackets, nontrivial delimiters/brackets
  }
  
  private void assertSplitString(SplitString result, String... expected) {
    List<String> splits = new ArrayList<String>();
    List<String> delims = new ArrayList<String>();
    for (int i = 0; i < expected.length - 1; i++) {
      ((i%2 == 0) ? splits : delims).add(expected[i]);
    }
    String rest = expected[expected.length-1];
    assertEquals(splits, result.splits());
    assertEquals(delims, result.delimiters());
    assertEquals(rest, result.rest());
  }
  
}
