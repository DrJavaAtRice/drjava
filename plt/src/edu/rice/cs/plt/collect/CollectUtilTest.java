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

package edu.rice.cs.plt.collect;

import junit.framework.TestCase;
import java.util.Arrays;
import java.util.List;

import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.collect.CollectUtil.*;

/**
 * Tests for the CollectUtil methods
 */
public class CollectUtilTest extends TestCase {

  public void assertList(List<?> actual, Object... expected) {
    assertEquals(Arrays.asList(expected), actual);
  }
  
  public void testMaxList() {
    assertList(maxList(IterUtil.<String>make(), STRING_PREFIX_ORDER));
    assertList(maxList(IterUtil.make("b"), STRING_PREFIX_ORDER), "b");
    assertList(maxList(IterUtil.make("a", "b", "ab"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(maxList(IterUtil.make("ab", "b", "a"), STRING_PREFIX_ORDER), "ab", "b");
    assertList(maxList(IterUtil.make("abc", "ab", "a"), STRING_PREFIX_ORDER), "abc");
    assertList(maxList(IterUtil.make("a", "ab", "abc"), STRING_PREFIX_ORDER), "abc");
    assertList(maxList(IterUtil.make("a", "ab", "a", "b", "ab"), STRING_PREFIX_ORDER), "ab", "b");
  }
  
  public void testMinList() {
    assertList(minList(IterUtil.<String>make(), STRING_PREFIX_ORDER));
    assertList(minList(IterUtil.make("b"), STRING_PREFIX_ORDER), "b");
    assertList(minList(IterUtil.make("a", "b", "ab"), STRING_PREFIX_ORDER), "a", "b");
    assertList(minList(IterUtil.make("ab", "b", "a"), STRING_PREFIX_ORDER), "b", "a");
    assertList(minList(IterUtil.make("abc", "ab", "a"), STRING_PREFIX_ORDER), "a");
    assertList(minList(IterUtil.make("a", "ab", "abc"), STRING_PREFIX_ORDER), "a");
    assertList(minList(IterUtil.make("a", "ab", "a", "b", "ab"), STRING_PREFIX_ORDER), "a", "b");
  }
  
  public void testPrefixStringOrder() {
    assertTrue(STRING_PREFIX_ORDER.contains("a", "abc"));
    assertTrue(STRING_PREFIX_ORDER.contains("ab", "abc"));
    assertTrue(STRING_PREFIX_ORDER.contains("abc", "abc"));
    assertTrue(STRING_PREFIX_ORDER.contains("", "abc"));
    assertFalse(STRING_PREFIX_ORDER.contains("abcd", "abc"));
    assertFalse(STRING_PREFIX_ORDER.contains("bc", "abc"));
    assertFalse(STRING_PREFIX_ORDER.contains("a", "b"));
  }
  
  public void testSubstringOrder() {
    assertTrue(SUBSTRING_ORDER.contains("a", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("b", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("c", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("ab", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("abc", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("bc", "abc"));
    assertTrue(SUBSTRING_ORDER.contains("", "abc"));
    assertFalse(SUBSTRING_ORDER.contains("abcd", "abc"));
    assertFalse(SUBSTRING_ORDER.contains("ac", "abc"));
  }
  
  public void testSubsetOrder() {
    assertTrue(SUBSET_ORDER.contains(Arrays.<Integer>asList(), Arrays.<Integer>asList()));
    assertTrue(SUBSET_ORDER.contains(Arrays.<Integer>asList(), Arrays.asList(1, 2)));
    assertTrue(SUBSET_ORDER.contains(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 1)));
    assertTrue(SUBSET_ORDER.contains(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 1)));
    assertFalse(SUBSET_ORDER.contains(Arrays.asList(1, 2, 3), Arrays.asList(2, 3)));
  }
  
}
