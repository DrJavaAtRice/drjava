package edu.rice.cs.util;

import junit.framework.*;

/**
 * Test cases for {@link PreventExitSecurityManager}.
 *
 * @version $Id$
 */
public class PreventExitSecurityManagerTest extends TestCase {
  private PreventExitSecurityManager _manager;

  /**
   * Constructor.
   * @param  String name
   */
  public PreventExitSecurityManagerTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(PreventExitSecurityManagerTest.class);
  }

  public void setUp() {
    _manager = PreventExitSecurityManager.activate();
  }

  public void tearDown() {
    _manager.deactivate();
  }

  public void testSystemExitPrevented() {
    try {
      System.exit(1);
      fail("System.exit passed?!");
    }
    catch (ExitingNotAllowedException se) {
      // good.
    }
  }

  public void testExitVMRespectsBlock() {
    _manager.setBlockExit(true);
    try {
      _manager.exitVM(-1);
      fail("exitVM passed while blocked!");
    }
    catch (ExitingNotAllowedException se) {
      // good
    }
  }

  public void testCanNotChangeSecurityManager() {
    try {
      System.setSecurityManager(null);
      fail("setSecurityManager passed!");
    }
    catch (SecurityException se) {
      // good
    }
  }
}
