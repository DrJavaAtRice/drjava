package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * @version $Id$
 */
public class BraceTest extends TestCase implements ReducedModelStates {
 
  protected Brace rparen;
  protected Brace lparen;

  /**
   * Constructor.
   * @param     String name name of test
   */
  public BraceTest(String name) {
    super(name);
  }

  /**
   * Set up Braces for testing.
   */
  public void setUp() {
    lparen = Brace.MakeBrace("(", FREE);
    rparen = Brace.MakeBrace(")", FREE);
  }

  /**
   * Create the test suite.
   * @return BraceTest test suite
   */
  public static Test suite() {
    return  new TestSuite(BraceTest.class);
  }

  /**
   * Tests the successful construction of a Brace using the MakeBrace method.
   */
  public void testMakeBraceSuccess() {
    Brace brace = Brace.MakeBrace("{", FREE);
    assertEquals("{", brace.getType());
    assertEquals(1, brace.getSize());
  }

  /**
   * Tests the failure to make a Brace with a non-special character.
   */
  public void testMakeBraceFailure() {
    Brace brace;
    try {
      brace = Brace.MakeBrace("k", FREE);
    } catch (BraceException e) {
      assertEquals("Invalid brace type \"k\"", e.getMessage());
    }
  }

  /**
   * Test the getType function for Braces.
   */
  public void testGetType() {
    assertEquals("(", lparen.getType());
    assertEquals(")", rparen.getType());
  }

  /**
   * Test the isShadowed() function.
   */
  public void testIsShadowed() {
    assertTrue("#0.0", !lparen.isShadowed());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertEquals("#0.0.1", INSIDE_DOUBLE_QUOTE, lparen.getState());
    assertTrue("#0.1", lparen.isShadowed());
    rparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", rparen.isShadowed());
    rparen.setState(FREE);
    assertTrue("#0.3", !rparen.isShadowed());
  }

  /**
   * Test the isQuoted() function.
   */
  public void testIsQuoted() {
    assertTrue("#0.0", !lparen.isQuoted());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.1", lparen.isQuoted());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", !lparen.isQuoted());
  }

  /**
   * Test the isCommented() function.
   */
  public void testIsCommented() {
    assertTrue("#0.0", !lparen.isCommented());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.1", lparen.isCommented());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.2", !lparen.isCommented());
  }

  /**
   * Test the toString method.
   */
  public void testToString() {
    assertEquals(" (", lparen.toString());
    assertEquals(" )", rparen.toString());
  }

  /**
   * Test the flip() method.
   */
  public void testFlip() {
    lparen.flip();
    rparen.flip();
    assertEquals("(", rparen.getType());
    assertEquals(")", lparen.getType());
  }

  /**
   * Test isOpen() and isClosed().
   */
  public void testOpenClosed() {
    assertTrue(lparen.isOpen());
    assertTrue(rparen.isClosed());
  }

  /**
   * Test isMatch(Brace) method.
   */
  public void testIsMatch() {
    Brace bracket = Brace.MakeBrace("]", FREE);
    Brace dummy = Brace.MakeBrace("", FREE);
    assertTrue(lparen.isMatch(rparen));
    assertTrue(!lparen.isMatch(bracket));
    assertTrue(!lparen.isMatch(dummy));
    assertTrue(!dummy.isMatch(lparen));
  }
}



