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
import java.util.Arrays;
import java.util.List;

import static edu.rice.cs.plt.collect.CollectUtil.*;

/**
 * Tests for the CollectUtil methods
 */
public class CollectUtilTest extends TestCase {
  
  public void assertList(List<?> actual, Object... expected) {
    assertEquals(Arrays.asList(expected), actual);
  }
  
  private List<String> list(String... ss) {
    return Arrays.asList(ss);
  }
  
  public void testMaxList() {
    assertList(maxList(list(), STRING_PREFIX_ORDER));
    assertList(maxList(list("b"), STRING_PREFIX_ORDER), "b");
    assertList(maxList(list("a", "b", "ab"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(maxList(list("ab", "b", "a"), STRING_PREFIX_ORDER), "ab", "b");
    assertList(maxList(list("abc", "ab", "a"), STRING_PREFIX_ORDER), "abc");
    assertList(maxList(list("a", "ab", "abc"), STRING_PREFIX_ORDER), "abc");
    assertList(maxList(list("a", "ab", "a", "b", "ab"), STRING_PREFIX_ORDER), "ab", "b");
  }
  
  public void testComposeMaxLists() {
    assertList(composeMaxLists(list(), list(), STRING_PREFIX_ORDER));
    assertList(composeMaxLists(list(), list("a", "b", "c"), STRING_PREFIX_ORDER), "a", "b", "c");
    assertList(composeMaxLists(list("a", "b", "c"), list(), STRING_PREFIX_ORDER), "a", "b", "c");
    assertList(composeMaxLists(list("b", "ab"), list("b"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMaxLists(list("ab", "b"), list("b"), STRING_PREFIX_ORDER), "ab", "b");
    assertList(composeMaxLists(list("b"), list("b", "ab"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMaxLists(list("b"), list("ab", "b"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMaxLists(list("ab", "cd"), list("ef", "c"), STRING_PREFIX_ORDER), "ab", "cd", "ef");
    assertList(composeMaxLists(list("ef", "c"), list("ab", "cd"), STRING_PREFIX_ORDER), "ef", "ab", "cd");
  }
  
  public void testMinList() {
    assertList(minList(list(), STRING_PREFIX_ORDER));
    assertList(minList(list("b"), STRING_PREFIX_ORDER), "b");
    assertList(minList(list("a", "b", "ab"), STRING_PREFIX_ORDER), "a", "b");
    assertList(minList(list("ab", "b", "a"), STRING_PREFIX_ORDER), "b", "a");
    assertList(minList(list("abc", "ab", "a"), STRING_PREFIX_ORDER), "a");
    assertList(minList(list("a", "ab", "abc"), STRING_PREFIX_ORDER), "a");
    assertList(minList(list("a", "ab", "a", "b", "ab"), STRING_PREFIX_ORDER), "a", "b");
  }
  
  public void testComposeMinLists() {
    assertList(composeMinLists(list(), list(), STRING_PREFIX_ORDER));
    assertList(composeMinLists(list(), list("a", "b", "c"), STRING_PREFIX_ORDER), "a", "b", "c");
    assertList(composeMinLists(list("a", "b", "c"), list(), STRING_PREFIX_ORDER), "a", "b", "c");
    assertList(composeMinLists(list("b", "ab"), list("b"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMinLists(list("ab", "b"), list("b"), STRING_PREFIX_ORDER), "ab", "b");
    assertList(composeMinLists(list("b"), list("b", "ab"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMinLists(list("b"), list("ab", "b"), STRING_PREFIX_ORDER), "b", "ab");
    assertList(composeMinLists(list("ab", "cd"), list("ef", "c"), STRING_PREFIX_ORDER), "ab", "ef", "c");
    assertList(composeMinLists(list("ef", "c"), list("ab", "cd"), STRING_PREFIX_ORDER), "ef", "c", "ab");
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
