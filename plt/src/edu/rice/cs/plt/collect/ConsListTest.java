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
    assertTrue(isEmpty(emptyStrings));
    assertFalse(isEmpty(singletonStrings));
    assertFalse(isEmpty(twoStrings));
    assertFalse(isEmpty(threeStrings));
    
    assertTrue(isEmpty(emptyNumbers));
    assertFalse(isEmpty(singletonNumbers));
    assertFalse(isEmpty(twoNumbers));
    assertFalse(isEmpty(threeNumbers));
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
      public Boolean value(String s) { return s.length() >= 3; }
    };
    Predicate<String> p2 = new Predicate<String>() {
      public Boolean value(String s) { return s.length() <= 3; }
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
