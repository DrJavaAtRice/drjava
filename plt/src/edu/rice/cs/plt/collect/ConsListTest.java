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
import java.util.Iterator;
import java.util.NoSuchElementException;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.LambdaUtil;
import static edu.rice.cs.plt.collect.ConsList.*;

/** Tests the ConsList and ConsVisitor methods and visitors */
public class ConsListTest extends TestCase {
  
  private final ConsList<String> emptyStrings;
  private final ConsList<String> singletonStrings;
  private final ConsList<String> twoStrings;
  private final ConsList<String> threeStrings;
  private final ConsList<Number> emptyNumbers;
  private final ConsList<Number> singletonNumbers;
  private final ConsList<Number> twoNumbers;
  private final ConsList<Number> threeNumbers;

  public ConsListTest() {
    emptyStrings = empty();
    singletonStrings = singleton("hi");
    twoStrings = cons("hi", cons("mom", emptyStrings));
    threeStrings = cons("eat", cons("a", cons("sandwich", emptyStrings)));
    emptyNumbers = empty();
    singletonNumbers = ConsList.<Number>singleton(23);
    twoNumbers = cons(14.0, cons(11, emptyNumbers));
    threeNumbers = cons(-1, cons(-2.0, cons(-3.0f, emptyNumbers)));
  }
  
  public void testIsEmpty() {
    assertTrue(emptyStrings.isEmpty());
    assertFalse(singletonStrings.isEmpty());
    assertFalse(twoStrings.isEmpty());
    assertFalse(threeStrings.isEmpty());
    
    assertTrue(emptyNumbers.isEmpty());
    assertFalse(singletonNumbers.isEmpty());
    assertFalse(twoNumbers.isEmpty());
    assertFalse(threeNumbers.isEmpty());
  }
  
  public void testReverse() {
    assertEquals(emptyStrings, reverse(emptyStrings));
    assertEquals(singletonStrings, reverse(singletonStrings));
    assertEquals(cons("mom", singleton("hi")), reverse(twoStrings));
    assertEquals(cons("sandwich", cons("a", singleton("eat"))), reverse(threeStrings));
    
    // Use "value" just to make sure that works, too
    assertEquals(emptyNumbers, ConsVisitor.reverse().value(emptyNumbers));
    assertEquals(singletonNumbers, ConsVisitor.reverse().value(singletonNumbers));
    assertEquals(cons(11, singleton(14.0)), ConsVisitor.reverse().value(twoNumbers));
    assertEquals(cons(-3.0f, cons(-2.0, singleton(-1))), ConsVisitor.reverse().value(threeNumbers));
  }
  
  public void testAppend() {
    assertEquals(emptyStrings, append(emptyStrings, emptyStrings));
    assertEquals(threeStrings, append(emptyStrings, threeStrings));
    assertEquals(threeStrings, append(threeStrings, emptyStrings));
    assertEquals(cons("hi", singletonStrings), append(singletonStrings, singletonStrings));
    assertEquals(cons("hi", cons("mom", threeStrings)), append(twoStrings, threeStrings));

    assertEquals(emptyNumbers, append(emptyNumbers, emptyNumbers));
    assertEquals(threeNumbers, append(emptyNumbers, threeNumbers));
    assertEquals(threeNumbers, append(threeNumbers, emptyNumbers));
    assertEquals(cons(23, singletonNumbers), append(singletonNumbers, singletonNumbers));
    assertEquals(cons(14.0, cons(11, threeNumbers)), append(twoNumbers, threeNumbers));
  }
   
  public void testFilter() {
    Predicate<String> p1 = new Predicate<String>() {
      public boolean contains(String s) { return s.length() >= 3; }
    };
    Predicate<String> p2 = new Predicate<String>() {
      public boolean contains(String s) { return s.length() <= 3; }
    };
    
    assertEquals(emptyStrings, filter(emptyStrings, p1));
    assertEquals(emptyStrings, filter(emptyStrings, p2));
    assertEquals(emptyStrings, filter(singletonStrings, p1));
    assertEquals(singletonStrings, filter(singletonStrings, p2));
    assertEquals(singleton("mom"), filter(twoStrings, p1));
    assertEquals(twoStrings, filter(twoStrings, p2));
    assertEquals(cons("eat", cons("sandwich", empty())), filter(threeStrings, p1));
    assertEquals(cons("eat", cons("a", empty())), filter(threeStrings, p2));
  }
  
  public void testMap() {
    assertEquals(emptyStrings, map(emptyNumbers, LambdaUtil.TO_STRING));
    assertEquals(singleton("23"), map(singletonNumbers, LambdaUtil.TO_STRING));
    assertEquals(cons("14.0", singleton("11")), map(twoNumbers, LambdaUtil.TO_STRING));
    assertEquals(cons("-1", cons("-2.0", singleton("-3.0"))), map(threeNumbers, LambdaUtil.TO_STRING));
    
    ConsList<String> reverse2 = cons("mom", singleton("hi"));
    ConsList<String> reverse3 = cons("sandwich", cons("a", singleton("eat")));
    assertEquals(cons(emptyStrings, cons(singletonStrings, cons(reverse2, singleton(reverse3)))),
                 map(cons(emptyStrings, cons(singletonStrings, cons(twoStrings, singleton(threeStrings)))),
                     ConsVisitor.reverse()));
  }
  
  public void testSize() {
    assertEquals(0, emptyStrings.size());
    assertEquals(1, singletonStrings.size());
    assertEquals(2, twoStrings.size());
    assertEquals(3, threeStrings.size());
    
    assertEquals(0, emptyNumbers.size());
    assertEquals(1, singletonNumbers.size());
    assertEquals(2, twoNumbers.size());
    assertEquals(3, threeNumbers.size());
  }
  
  public void testIterator() {
    checkIterator(emptyStrings.iterator());
    checkIterator(singletonStrings.iterator(), "hi");
    checkIterator(twoStrings.iterator(), "hi", "mom");
    checkIterator(threeStrings.iterator(), "eat", "a", "sandwich");
    
    checkIterator(emptyNumbers.iterator());
    checkIterator(singletonNumbers.iterator(), 23);
    checkIterator(twoNumbers.iterator(), 14.0, 11);
    checkIterator(threeNumbers.iterator(), -1, -2.0, -3.0f);
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> void checkIterator(Iterator<T> iter, T... values) {
    for (int i = 0; i < values.length; i++) {
      assertTrue(iter.hasNext());
      assertEquals(values[i], iter.next());
    }
    assertFalse(iter.hasNext());
    try { iter.next(); }
    catch (NoSuchElementException e) { /* expected behavior */ }
  }
  
}
