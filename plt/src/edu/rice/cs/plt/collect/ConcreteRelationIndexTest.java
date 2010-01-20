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

import java.util.*;
import junit.framework.TestCase;
import edu.rice.cs.plt.debug.EventSequence;
import edu.rice.cs.plt.tuple.Pair;

import static java.util.Arrays.asList;
import static edu.rice.cs.plt.collect.CollectUtil.makeSet;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ConcreteRelationIndexTest extends TestCase {
  
  private static final Set<String> EMPTY = CollectUtil.<String>emptySet();
  
  public void testDirectManipulation() {
    StubIndex index = new StubIndex();
    
    assertFalse(index.contains("a", "1"));
    assertTrue(index.isEmpty());
    assertEquals(0, index.size());
    assertIterator(index); // empty iterator
    assertEquals(EMPTY, index.keys());
    assertEquals(EMPTY, index.match("a"));

    index.added("a", "1");
    index.events.assertEmpty();
    
    assertTrue(index.contains("a", "1"));
    assertFalse(index.isEmpty());
    assertEquals(1, index.size());
    assertIterator(index, "a", "1");
    assertEquals(makeSet("a"), index.keys());
    assertEquals(makeSet("1"), index.match("a"));
    
    index.removed("a", "1");
    index.events.assertEmpty();
    
    assertFalse(index.contains("a", "1"));
    assertTrue(index.isEmpty());
    assertEquals(0, index.size());
    assertIterator(index); // empty iterator

    Set<String> keys = index.keys();
    Set<String> as = index.match("a");
    assertEquals(EMPTY, keys);
    assertEquals(EMPTY, as);
    
    index.added("a", "2");
    index.events.assertEmpty();
    
    assertTrue(index.contains("a", "2"));
    assertFalse(index.isEmpty());
    assertEquals(1, index.size());
    assertEquals(makeSet("a"), index.keys());
    assertEquals(makeSet("a"), keys);
    assertEquals(makeSet("2"), index.match("a"));
    assertEquals(makeSet("2"), as);
    assertIterator(index, "a", "2");
    
    index.added("a", "3");
    index.added("a", "4");
    index.events.assertEmpty();
    
    assertTrue(index.contains("a", "2"));
    assertTrue(index.contains("a", "3"));
    assertTrue(index.contains("a", "4"));
    assertFalse(index.isEmpty());
    assertEquals(3, index.size());
    assertIterator(index, "a", "2", "a", "3", "a", "4");
    assertEquals(makeSet("a"), index.keys());
    assertEquals(makeSet("a"), keys);
    assertEquals(makeSet("2", "3", "4"), index.match("a"));
    assertEquals(makeSet("2", "3", "4"), as);
    assertEquals(EMPTY, index.match("b"));
    
    index.added("b", "1");
    index.added("b", "2");
    index.added("a", "1");
    index.events.assertEmpty();
    
    assertTrue(index.contains("a", "1"));
    assertTrue(index.contains("a", "2"));
    assertTrue(index.contains("a", "3"));
    assertTrue(index.contains("a", "4"));
    assertTrue(index.contains("b", "1"));
    assertTrue(index.contains("b", "2"));
    assertFalse(index.isEmpty());
    assertEquals(6, index.size());
    assertIterator(index, "a", "1", "a", "2", "a", "3", "a", "4", "b", "1", "b", "2");
    assertEquals(makeSet("a", "b"), index.keys());
    assertEquals(makeSet("a", "b"), keys);
    assertEquals(makeSet("1", "2", "3", "4"), index.match("a"));
    assertEquals(makeSet("1", "2", "3", "4"), as);
    assertEquals(makeSet("1", "2"), index.match("b"));
    
    index.cleared();
    index.events.assertEmpty();
    
    assertFalse(index.contains("a", "1"));
    assertTrue(index.isEmpty());
    assertEquals(0, index.size());
    assertIterator(index); // empty iterator
    assertEquals(EMPTY, keys);
    assertEquals(EMPTY, as);
  }
  
  public void testKeySetMutation() {
    StubIndex index = new StubIndex();
    Set<String> keys = index.keys();
    
    assertEquals(EMPTY, keys);

    index.added("a", "1");
    index.added("b", "1");
    index.added("b", "2");
    index.events.assertEmpty();
    
    assertEquals(keys, makeSet("a", "b"));
    assertEquals(3, index.size());
    
    keys.remove("a");
    index.events.assertContents("validateRemoveKey(a, [1])", "removeFromRelation(a, 1)");
    
    assertEquals(2, index.size());
    assertIterator(index, "b", "1", "b", "2");
    assertEquals(makeSet("b"), keys);
    assertEquals(EMPTY, index.match("a"));
    assertEquals(makeSet("1", "2"), index.match("b"));
    
    keys.clear();
    index.events.assertContents("validateClear()", "clearRelation()");
    
    assertEquals(0, index.size());
    assertIterator(index); // empty iterator
    assertEquals(EMPTY, keys);
    assertEquals(EMPTY, index.match("a"));
    assertEquals(EMPTY, index.match("b"));
    
    index.added("a", "1");
    index.added("b", "1");
    index.added("b", "2");
    index.added("c", "1");
    index.added("d", "1");
    index.added("e", "1");
    index.events.assertEmpty();
    
    Iterator<String> keysIter = keys.iterator();
    assertEquals("a", keysIter.next());
    keysIter.remove();
    index.events.assertContents("validateRemoveKey(a, [1])", "removeFromRelation(a, 1)");
    
    assertEquals(5, index.size());
    assertEquals(EMPTY, index.match("a"));
    
    keys.removeAll(asList("b", "c", "e"));
    index.events.assertContents("validateRemoveKey(b, [1, 2])", "removeFromRelation(b, 1)",
                                "removeFromRelation(b, 2)",
                                "validateRemoveKey(c, [1])", "removeFromRelation(c, 1)",
                                "validateRemoveKey(e, [1])", "removeFromRelation(e, 1)");
    
    assertEquals(1, index.size());
    assertIterator(index, "d", "1");
    assertEquals(makeSet("d"), keys);
    assertEquals(EMPTY, index.match("b"));
    assertEquals(makeSet("1"), index.match("d"));
  }
  
  public void testValueSetMutation() {
    StubIndex index = new StubIndex();
    Set<String> as = index.match("a");
    
    assertEquals(EMPTY, as);
    
    index.added("a", "1");
    index.added("b", "1");
    index.added("a", "2");
    index.events.assertEmpty();
    
    assertEquals(3, index.size());
    assertIterator(index, "a", "1", "a", "2", "b", "1");
    assertEquals(makeSet("1", "2"), as);
    
    as.add("3");
    index.events.assertContents("validateAdd(a, 3)", "addToRelation(a, 3)");
    
    assertEquals(4, index.size());
    assertIterator(index, "a", "1", "a", "2", "a", "3", "b", "1");
    assertEquals(makeSet("1", "2", "3"), as);
    
    as.addAll(asList("4", "5"));
    index.events.assertContents("validateAdd(a, 4)", "addToRelation(a, 4)",
                                "validateAdd(a, 5)", "addToRelation(a, 5)");
    
    assertEquals(6, index.size());
    assertIterator(index, "a", "1", "a", "2", "a", "3", "a", "4", "a", "5", "b", "1");
    assertEquals(makeSet("1", "2", "3", "4", "5"), as);
    
    as.remove("1");
    index.events.assertContents("validateRemove(a, 1)", "removeFromRelation(a, 1)");
    
    assertEquals(5, index.size());
    assertIterator(index, "a", "2", "a", "3", "a", "4", "a", "5", "b", "1");
    assertEquals(makeSet("2", "3", "4", "5"), as);
    
    as.removeAll(asList("2", "3"));
    index.events.assertContents("validateRemove(a, 2)", "removeFromRelation(a, 2)",
                                "validateRemove(a, 3)", "removeFromRelation(a, 3)");
    
    assertEquals(3, index.size());
    assertIterator(index, "a", "4", "a", "5", "b", "1");
    assertEquals(makeSet("4", "5"), as);
    
    Iterator<String> asIter = as.iterator();
    assertEquals("4", asIter.next());
    asIter.remove();
    index.events.assertContents("validateRemove(a, 4)", "removeFromRelation(a, 4)");
    
    assertEquals(2, index.size());
    assertIterator(index, "a", "5", "b", "1");
    assertEquals(makeSet("5"), as);
    
    as.clear();
    index.events.assertContents("validateRemoveKey(a, [5])", "removeFromRelation(a, 5)");
    
    assertEquals(1, index.size());
    assertIterator(index, "b", "1");
    assertEquals(EMPTY, as);
  }
  
  private void assertIterator(StubIndex index, String... contents) {
    int i = 0;
    for (Pair<String, String> pair : index) {
      if (i >= contents.length) { fail("Unexpected iterator element: " + index); }
      assertEquals(contents[i], pair.first());
      assertEquals(contents[i+1], pair.second());
      i+=2;
    }
    if (i < contents.length) {
      fail("Missing iterator element: (" + contents[i] + ", " + contents[i+1] + ")");
    }
  }
  
  private class StubIndex extends ConcreteRelationIndex<String, String> {
    public final EventSequence<String> events;
    
    public StubIndex() {
      super(CollectUtil.<String, PredicateSet<String>>treeMapFactory(),
            CollectUtil.<String>treeSetFactory());
      events = new EventSequence<String>();
    }
    
    protected void validateAdd(String k, String v) {
      events.record("validateAdd(" + k + ", " + v + ")");
    }
    protected void addToRelation(String k, String v) {
      events.record("addToRelation(" + k + ", " + v + ")");
    }
    protected void validateRemove(String k, String v) {
      events.record("validateRemove(" + k + ", " + v + ")");
    }
    protected void validateRemoveKey(String k, PredicateSet<String> v) {
      events.record("validateRemoveKey(" + k + ", " + v + ")");
    }
    protected void removeFromRelation(String k, String v) {
      events.record("removeFromRelation(" + k + ", " + v + ")");
    }
    protected void validateClear() {
      events.record("validateClear()");
    }
    protected void clearRelation() {
      events.record("clearRelation()");
    }
  }
  
}
