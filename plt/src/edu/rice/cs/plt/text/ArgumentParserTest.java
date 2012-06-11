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

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.text.ArgumentParser.Result;
import junit.framework.TestCase;

public class ArgumentParserTest extends TestCase {
  
  public void testParseEmpty() {
    ArgumentParser empty = new ArgumentParser();

    assertEqualIterable(empty.parse().params());
    assertEqualIterable(empty.parse("a", "b", "c").params(), "a", "b", "c");
    assertBadArgs(empty, "-apple");
    assertBadArgs(empty, "a", "b", "-apple");
    assertBadArgs(empty, "a", "b", "-apple", "c");
    
    empty.requireParams(3);
    assertBadArgs(empty);
    assertBadArgs(empty, "a", "b");
    assertEqualIterable(empty.parse("a", "b", "c").params(), "a", "b", "c");
  }
  
  public void testParseWithOptions() {
    ArgumentParser p = new ArgumentParser();
    p.supportOption("apple", 1);
    p.supportOption("banana", 0);
    p.supportOption("guava", 2);
    
    Result r = p.parse();
    assertEqualIterable(r.params());
    assertFalse(r.hasOption("apple"));
    assertFalse(r.hasOption("banana"));
    assertFalse(r.hasOption("guava"));
    assertNull(r.getOption("apple"));
    assertNull(r.getOption("banana"));
    assertNull(r.getOption("guava"));
    
    r = p.parse("x1", "x2", "x3");
    assertEqualIterable(r.params(), "x1", "x2", "x3");
    assertFalse(r.hasOption("apple"));
    assertFalse(r.hasOption("banana"));
    assertFalse(r.hasOption("guava"));
    
    r = p.parse("-apple", "a");
    assertEqualIterable(r.params());
    assertTrue(r.hasOption("apple"));
    assertFalse(r.hasOption("banana"));
    assertFalse(r.hasOption("guava"));
    assertEqualIterable(r.getOption("apple"), "a");
    assertEquals("a", r.getUnaryOption("apple"));
    assertFalse(r.hasNullaryOption("apple"));
    assertNull(r.getBinaryOption("apple"));
    assertNull(r.getTernaryOption("apple"));
    assertNull(r.getQuaternaryOption("apple"));
    assertNull(r.getOption("banana"));
    assertNull(r.getOption("guava"));
    
    r = p.parse("-banana", "-guava", "g1", "g2");
    assertEqualIterable(r.params());
    assertFalse(r.hasOption("apple"));
    assertTrue(r.hasOption("banana"));
    assertTrue(r.hasOption("guava"));
    assertNull(r.getOption("apple"));
    assertEqualIterable(r.getOption("banana"));
    assertTrue(r.hasNullaryOption("banana"));
    assertEqualIterable(r.getOption("guava"), "g1", "g2");
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    r = p.parse("-guava", "g1", "g2", "-apple", "a", "-banana");
    assertEqualIterable(r.params());
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    r = p.parse("x1", "-guava", "g1", "g2", "x2", "-apple", "a", "-banana", "x3");
    assertEqualIterable(r.params(), "x1", "x2", "x3");
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    assertBadArgs(p, "-guava");
    assertBadArgs(p, "-guava", "g1");
    assertBadArgs(p, "-apple", "-banana");
    assertBadArgs(p, "-banana", "-apple");
    
    p.requireParams(1);
    
    assertBadArgs(p, "-guava", "g1", "g2", "-apple", "a", "-banana");
    
    r = p.parse("-guava", "g1", "g2", "x1", "-apple", "a", "-banana");
    assertEqualIterable(r.params(), "x1");
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    p.requireStrictOrder();
    assertBadArgs(p, "-guava", "g1", "g2", "-apple", "a", "-banana");
    assertBadArgs(p, "-guava", "g1", "g2", "x1", "-apple", "a", "-banana");
    
    r = p.parse("-guava", "g1", "g2", "-apple", "a", "-banana", "x1");
    assertEqualIterable(r.params(), "x1");
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
  }
  
  public void testParseVarargs() {
    ArgumentParser p = new ArgumentParser();
    p.supportOption("banana", 0);
    p.supportVarargOption("grape");
    
    Result r = p.parse();
    assertEqualIterable(r.params());
    assertFalse(r.hasOption("grape"));
    
    r = p.parse("-grape");
    assertEqualIterable(r.params());
    assertEqualIterable(r.getOption("grape"));
    
    r = p.parse("-grape", "a", "b", "c");
    assertEqualIterable(r.params());
    assertEqualIterable(r.getOption("grape"), "a", "b", "c");
    
    r = p.parse("a", "b", "c", "-grape");
    assertEqualIterable(r.params(), "a", "b", "c");
    assertEqualIterable(r.getOption("grape"));
    
    r = p.parse("-grape", "a", "b", "-banana", "c");
    assertEqualIterable(r.params(), "c");
    assertEqualIterable(r.getOption("grape"), "a", "b");
  }
  
  public void testParseWithDefaults() {
    ArgumentParser p = new ArgumentParser();
    p.supportOption("apple", "defa");
    p.supportOption("banana");
    p.supportOption("guava", "defg1", "defg2");
    
    Result r = p.parse();
    assertEqualIterable(r.params());
    assertFalse(r.hasNullaryOption("banana"));
    assertEquals("defa", r.getUnaryOption("apple"));
    assertEquals(Pair.make("defg1", "defg2"), r.getBinaryOption("guava"));
    
    r = p.parse("x1", "x2", "x3");
    assertEqualIterable(r.params(), "x1", "x2", "x3");
    assertFalse(r.hasNullaryOption("banana"));
    assertEquals("defa", r.getUnaryOption("apple"));
    assertEquals(Pair.make("defg1", "defg2"), r.getBinaryOption("guava"));
    
    r = p.parse("-apple", "a");
    assertEqualIterable(r.params());
    assertFalse(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("defg1", "defg2"), r.getBinaryOption("guava"));
    
    r = p.parse("-banana", "-guava", "g1", "g2");
    assertEqualIterable(r.params());
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("defa", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    r = p.parse("-guava", "g1", "g2", "-apple", "a", "-banana");
    assertEqualIterable(r.params());
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
    
    r = p.parse("x1", "-guava", "g1", "g2", "x2", "-apple", "a", "-banana", "x3");
    assertEqualIterable(r.params(), "x1", "x2", "x3");
    assertTrue(r.hasNullaryOption("banana"));
    assertEquals("a", r.getUnaryOption("apple"));
    assertEquals(Pair.make("g1", "g2"), r.getBinaryOption("guava"));
  }
  
  
  private void assertBadArgs(ArgumentParser p, String... args) {
    try { p.parse(args); fail("expected exception"); }
    catch (IllegalArgumentException e) { /* expected */ }
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> void assertEqualIterable(Iterable<T> actual, T... expected) {
    // convert to AbstractIterable so that we can get a useful error message
    assertEquals(IterUtil.asIterable(expected), IterUtil.snapshot(actual));
  }
  
  
}