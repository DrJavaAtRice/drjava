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
  public void testParse() {
    assertEquals("LongLiteral.testParse failed:",true,LongLiteral.testParse());  
  }
  
  /**
   * A more riqorous test on the ParseHexadecimal method.  Tests a wide range of possible
   * input numbers.
   */
  public void testParseHexadecimal() {
    assertEquals("LongLiteral.testParseHexadecimal failed:",true,
                 LongLiteral.testParseHexadecimal());  
  }
  
  /**
   * A more riqorous test on the ParseOctal method.  Tests a wide range of possible
   * input numbers.
   */ 
  public void testParseOctal() {
    assertEquals("LongLiteral.testParseOctal failed:",true,LongLiteral.testParseOctal());
  }
}
