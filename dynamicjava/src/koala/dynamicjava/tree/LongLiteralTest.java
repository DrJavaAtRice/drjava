package koala.dynamicjava.tree;

import junit.framework.TestCase;
//import koala.dynamicjava.tree.*;

/**
 * JUnit tests for the koala.dynamicjava.tree.LongLiteralTest class.
 * This test class depends on static test methods written inside the
 * LongLiteralTest class.
 */ 
public class LongLiteralTest extends TestCase {
  
  /**
   * Test to make sure the testParse method correctly interprets various inputs
   * and processes them appropriately.
   */ 
  public void testLongLiteral()
  {
    LongLiteral ll;
    
    //Test parse refactored into JUnit
    ll = new LongLiteral("0x138");
    assertTrue("Parse 0x138", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0470");
    assertTrue("Parse 0470", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("312");
    assertTrue("Parse 312", new Long("312").compareTo((Long)ll.getValue()) == 0);
    
    //Test parse hexadecimal refactored into JUnit
    ll = new LongLiteral("0x0");
    assertTrue("Parse 0", new Long("0").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0x7fffffff");
    assertTrue("Parse 7fffffff", new Long("2147483647").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0x80000000");
    assertTrue("Parse 80000000 Hexadecimal", new Long("2147483648").compareTo((Long)ll.getValue()) == 0);

    //Test parse octal refactored into JUnit
    ll = new LongLiteral("0");
    assertTrue("Parse 0 Octal", new Long("0").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("017777777777");
    assertTrue("Parse 17777777777 Octal", new Long("2147483647").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("020000000000");
    assertTrue("Parse 20000000000 Octal", new Long("2147483648").compareTo((Long)ll.getValue()) == 0);
    
    //Testing of negative numbers added
    ll = new LongLiteral("0xffffffffffffffff");
    assertTrue("Parse -1 Hexadecimal", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("01777777777777777777777");
    assertTrue("Parse -1 Hexadecimal", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("-1");
    assertTrue("Parse -1", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    
    }
}