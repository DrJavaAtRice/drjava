package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * @version $Id$
 */
public class GapTest extends TestCase {

  /**
   * put your documentation comment here
   * @param     String name
   */
  public GapTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  public void setUp() {}

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(GapTest.class);
  }

  /**
   * put your documentation comment here
   */
  public void testGrow() {
    Gap gap0 = new Gap(0, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.grow(5);
    assertEquals(5, gap0.getSize());
    gap0.grow(0);
    assertEquals(5, gap0.getSize());
    gap1.grow(-6);
    assertEquals(1, gap1.getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testShrink() {
    Gap gap0 = new Gap(5, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.shrink(3);
    assertEquals(2, gap0.getSize());
    gap0.shrink(0);
    assertEquals(2, gap0.getSize());
    gap1.shrink(3);
    assertEquals(1, gap1.getSize());
    gap1.shrink(-1);
    assertEquals(1, gap1.getSize());
  }
}



