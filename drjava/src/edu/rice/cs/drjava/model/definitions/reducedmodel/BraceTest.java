package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * @version $Id$
 */
public class BraceTest extends TestCase {
  protected Brace rparen;
  protected Brace lparen;

  /**
   * put your documentation comment here
   * @param     String name
   */
  public BraceTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  public void setUp() {
    lparen = Brace.MakeBrace("(", ReducedToken.FREE);
    rparen = Brace.MakeBrace(")", ReducedToken.FREE);
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(BraceTest.class);
  }

  /**
   * put your documentation comment here
   */
  public void testMakeBraceSuccess() {
    Brace brace = Brace.MakeBrace("{", ReducedToken.FREE);
    assertEquals("{", brace.getType());
    assertEquals(1, brace.getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testMakeBraceFailure() {
    Brace brace;
    try {
      brace = Brace.MakeBrace("k", ReducedToken.FREE);
    } catch (BraceException e) {
      assertEquals("Invalid brace type \"k\"", e.getMessage());
    }
  }

  /**
   * put your documentation comment here
   */
  public void testGetType() {
    assertEquals("(", lparen.getType());
    assertEquals(")", rparen.getType());
  }

  /**
   * put your documentation comment here
   */
  public void testIsShadowed() {
    assertTrue("#0.0", !lparen.isShadowed());
    lparen.setState(ReducedToken.INSIDE_QUOTE);
    assertEquals("#0.0.1", ReducedToken.INSIDE_QUOTE, lparen.getState());
    assertTrue("#0.1", lparen.isShadowed());
    rparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", rparen.isShadowed());
    rparen.setState(ReducedToken.FREE);
    assertTrue("#0.3", !rparen.isShadowed());
  }

  /**
   * put your documentation comment here
   */
  public void testIsQuoted() {
    assertTrue("#0.0", !lparen.isQuoted());
    lparen.setState(ReducedToken.INSIDE_QUOTE);
    assertTrue("#0.1", lparen.isQuoted());
    lparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", !lparen.isQuoted());
  }

  /**
   * put your documentation comment here
   */
  public void testIsCommented() {
    assertTrue("#0.0", !lparen.isCommented());
    lparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
    assertTrue("#0.1", lparen.isCommented());
    lparen.setState(ReducedToken.INSIDE_QUOTE);
    assertTrue("#0.2", !lparen.isCommented());
  }

  /**
   * put your documentation comment here
   */
  public void testToString() {
    assertEquals(" (", lparen.toString());
    assertEquals(" )", rparen.toString());
  }

  /**
   * put your documentation comment here
   */
  public void testFlip() {
    lparen.flip();
    rparen.flip();
    assertEquals("(", rparen.getType());
    assertEquals(")", lparen.getType());
  }

  /**
   * put your documentation comment here
   */
  public void testOpenClosed() {
    assertTrue(lparen.isOpen());
    assertTrue(rparen.isClosed());
  }

  /**
   * put your documentation comment here
   */
  public void testIsMatch() {
    Brace bracket = Brace.MakeBrace("]", ReducedToken.FREE);
    Brace dummy = Brace.MakeBrace("", ReducedToken.FREE);
    assertTrue(lparen.isMatch(rparen));
    assertTrue(!lparen.isMatch(bracket));
    assertTrue(!lparen.isMatch(dummy));
    assertTrue(!dummy.isMatch(lparen));
  }
}



