/*BEGIN_COPYRIGHT_BLOCK*
 * 
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

import java.util.Set;
import java.util.HashSet;
import junit.framework.TestCase;
import edu.rice.cs.plt.tuple.Pair;

import static edu.rice.cs.plt.collect.CollectUtil.makeSet;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Tests for mutable relations (IndexedRelation, UnindexedRelation). */
public class MutableRelationTest extends TestCase {
  
  private static final Set<String> EMPTY = CollectUtil.<String>emptySet();
  
  public void testSecondIndexed() {
    prodRelation(new IndexedRelation<String, String>());
  }
  
  public void testSecondNotIndex() {
    prodRelation(new IndexedRelation<String, String>(false));
  }
  
  public void testUnindexed() {
    prodRelation(new UnindexedRelation<String, String>());
  }
  
  private void prodRelation(Relation<String, String> r) {
    Relation<String, String> inv = r.inverse();
    
    assertEquals(0, r.size());
    assertEquals(EMPTY, r);
    assertEquals(EMPTY, r.firstSet());
    assertEquals(EMPTY, r.secondSet());
    assertEquals(EMPTY, r.excludeFirsts());
    assertEquals(EMPTY, r.excludeSeconds());
    assertFalse(r.containsFirst("a"));
    assertFalse(r.containsSecond("1"));
    assertFalse(r.containsSecond("2"));
    assertEquals(EMPTY, r.matchFirst("a"));
    assertEquals(EMPTY, r.matchSecond("1"));
    assertEquals(EMPTY, r.matchSecond("2"));
    
    assertEquals(0, inv.size());
    assertEquals(EMPTY, inv);
    assertEquals(EMPTY, inv.firstSet());
    assertEquals(EMPTY, inv.secondSet());
    assertEquals(EMPTY, inv.excludeFirsts());
    assertEquals(EMPTY, inv.excludeSeconds());
    assertFalse(inv.containsFirst("1"));
    assertFalse(inv.containsFirst("2"));
    assertFalse(inv.containsSecond("a"));
    assertEquals(EMPTY, inv.matchFirst("1"));
    assertEquals(EMPTY, inv.matchFirst("2"));
    assertEquals(EMPTY, inv.matchSecond("a"));
    
    assertTrue(r.add("a", "1"));
    
    assertEquals(1, r.size());
    assertEquals(makePairSet("a", "1"), r);
    assertEquals(makeSet("a"), r.firstSet());
    assertEquals(makeSet("1"), r.secondSet());
    assertEquals(makeSet("1"), r.excludeFirsts());
    assertEquals(makeSet("a"), r.excludeSeconds());
    assertTrue(r.containsFirst("a"));
    assertTrue(r.containsSecond("1"));
    assertFalse(r.containsSecond("2"));
    assertEquals(makeSet("1"), r.matchFirst("a"));
    assertEquals(makeSet("a"), r.matchSecond("1"));
    assertEquals(EMPTY, r.matchSecond("2"));
    
    assertEquals(1, inv.size());
    assertEquals(makePairSet("1", "a"), inv);
    assertEquals(makeSet("1"), inv.firstSet());
    assertEquals(makeSet("a"), inv.secondSet());
    assertEquals(makeSet("a"), inv.excludeFirsts());
    assertEquals(makeSet("1"), inv.excludeSeconds());
    assertTrue(inv.containsFirst("1"));
    assertFalse(inv.containsFirst("2"));
    assertTrue(inv.containsSecond("a"));
    assertEquals(makeSet("a"), inv.matchFirst("1"));
    assertEquals(EMPTY, inv.matchFirst("2"));
    assertEquals(makeSet("1"), inv.matchSecond("a"));
    
    assertFalse(r.add("a", "1"));
    assertTrue(r.add("a", "2"));
    
    assertEquals(2, r.size());
    assertEquals(makePairSet("a", "1", "a", "2"), r);
    assertEquals(makeSet("a"), r.firstSet());
    assertEquals(makeSet("1", "2"), r.secondSet());
    assertEquals(makeSet("1", "2"), r.excludeFirsts());
    assertEquals(makeSet("a"), r.excludeSeconds());
    assertTrue(r.containsFirst("a"));
    assertTrue(r.containsSecond("1"));
    assertTrue(r.containsSecond("2"));
    assertEquals(makeSet("1", "2"), r.matchFirst("a"));
    assertEquals(makeSet("a"), r.matchSecond("1"));
    assertEquals(makeSet("a"), r.matchSecond("2"));
    
    assertEquals(2, inv.size());
    assertEquals(makePairSet("1", "a", "2", "a"), inv);
    assertEquals(makeSet("1", "2"), inv.firstSet());
    assertEquals(makeSet("a"), inv.secondSet());
    assertEquals(makeSet("a"), inv.excludeFirsts());
    assertEquals(makeSet("1", "2"), inv.excludeSeconds());
    assertTrue(inv.containsFirst("1"));
    assertTrue(inv.containsFirst("2"));
    assertTrue(inv.containsSecond("a"));
    assertEquals(makeSet("a"), inv.matchFirst("1"));
    assertEquals(makeSet("a"), inv.matchFirst("2"));
    assertEquals(makeSet("1", "2"), inv.matchSecond("a"));
    
    assertTrue(r.add("b", "1"));
    assertTrue(r.add("b", "3"));
    
    assertEquals(4, r.size());
    assertEquals(makePairSet("a", "1", "a", "2", "b", "1", "b", "3"), r);
    assertEquals(makeSet("a", "b"), r.firstSet());
    assertEquals(makeSet("1", "2", "3"), r.secondSet());
    assertEquals(makeSet("1", "2", "3"), r.excludeFirsts());
    assertEquals(makeSet("a", "b"), r.excludeSeconds());
    assertTrue(r.containsFirst("a"));
    assertTrue(r.containsFirst("b"));
    assertTrue(r.containsSecond("1"));
    assertTrue(r.containsSecond("2"));
    assertTrue(r.containsSecond("3"));
    assertEquals(makeSet("1", "2"), r.matchFirst("a"));
    assertEquals(makeSet("1", "3"), r.matchFirst("b"));
    assertEquals(makeSet("a", "b"), r.matchSecond("1"));
    assertEquals(makeSet("a"), r.matchSecond("2"));
    assertEquals(makeSet("b"), r.matchSecond("3"));
    
    assertEquals(4, inv.size());
    assertEquals(makePairSet("1", "a", "2", "a", "1", "b", "3", "b"), inv);
    assertEquals(makeSet("1", "2", "3"), inv.firstSet());
    assertEquals(makeSet("a", "b"), inv.secondSet());
    assertEquals(makeSet("a", "b"), inv.excludeFirsts());
    assertEquals(makeSet("1", "2", "3"), inv.excludeSeconds());
    assertTrue(inv.containsFirst("1"));
    assertTrue(inv.containsFirst("2"));
    assertTrue(inv.containsFirst("3"));
    assertTrue(inv.containsSecond("a"));
    assertTrue(inv.containsSecond("b"));
    assertEquals(makeSet("a", "b"), inv.matchFirst("1"));
    assertEquals(makeSet("a"), inv.matchFirst("2"));
    assertEquals(makeSet("b"), inv.matchFirst("3"));
    assertEquals(makeSet("1", "2"), inv.matchSecond("a"));
    assertEquals(makeSet("1", "3"), inv.matchSecond("b"));
    
    r.clear();
    
    assertEquals(0, r.size());
    assertEquals(EMPTY, r);
    assertEquals(EMPTY, r.firstSet());
    assertEquals(EMPTY, r.secondSet());
    assertEquals(EMPTY, r.excludeFirsts());
    assertEquals(EMPTY, r.excludeSeconds());
    assertFalse(r.containsFirst("a"));
    assertFalse(r.containsSecond("1"));
    assertFalse(r.containsSecond("2"));
    assertEquals(EMPTY, r.matchFirst("a"));
    assertEquals(EMPTY, r.matchSecond("1"));
    assertEquals(EMPTY, r.matchSecond("2"));
    
    assertEquals(0, inv.size());
    assertEquals(EMPTY, inv);
    assertEquals(EMPTY, inv.firstSet());
    assertEquals(EMPTY, inv.secondSet());
    assertEquals(EMPTY, inv.excludeFirsts());
    assertEquals(EMPTY, inv.excludeSeconds());
    assertFalse(inv.containsFirst("1"));
    assertFalse(inv.containsFirst("2"));
    assertFalse(inv.containsSecond("a"));
    assertEquals(EMPTY, inv.matchFirst("1"));
    assertEquals(EMPTY, inv.matchFirst("2"));
    assertEquals(EMPTY, inv.matchSecond("a"));
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Set<Pair<String, String>> makePairSet(String... elts) {
    Set<Pair<String, String>> result = new HashSet<Pair<String, String>>();
    for (int i = 0; i < elts.length; i += 2) {
      result.add(Pair.make(elts[i], elts[i+1]));
    }
    return result;
  }
  
}
