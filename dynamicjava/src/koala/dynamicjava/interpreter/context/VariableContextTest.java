package koala.dynamicjava.interpreter.context;

import junit.framework.TestCase;

/**
 * Tests the functionality of the VariableContext.
 */
public class VariableContextTest extends TestCase {
  VariableContext<Object> _ctx;
  
  public void setUp() { _ctx = new VariableContext<Object>(); }
  
  /** Tests to make sure that the correct bindings are truely
   *  removed when revert is called
   */
  public void testRevert() {
    _ctx.define("a", new Integer(1));
    _ctx.define("b", new Integer(2));
    _ctx.defineConstant("c", new Integer(3));
    _ctx.setRevertPoint();
    _ctx.define("d", new Integer(4));
    _ctx.define("e", new Integer(5));
    _ctx.defineConstant("f", new Integer(6));
    
    assertEquals("'a' should be 1", new Integer(1), _ctx.get("a"));
    assertEquals("'b' should be 2", new Integer(2), _ctx.get("b"));
    assertEquals("'c' should be 3", new Integer(3), _ctx.get("c"));
    assertEquals("'d' should be 4", new Integer(4), _ctx.get("d"));
    assertEquals("'e' should be 5", new Integer(5), _ctx.get("e"));
    assertEquals("'f' should be 6", new Integer(6), _ctx.get("f"));
    
    _ctx.revert();
    assertTrue("'a' should exist", _ctx.isDefinedVariable("a"));
    assertTrue("'b' should exist", _ctx.isDefinedVariable("b"));
    assertTrue("'c' should exist", _ctx.isDefinedVariable("c"));
    assertFalse("'d' should not exist", _ctx.isDefinedVariable("d"));
    assertFalse("'e' should not exist", _ctx.isDefinedVariable("e"));
    assertFalse("'f' should not exist", _ctx.isDefinedVariable("f"));
  }
  // We need to test nested scopes to make sure that binding in non-local scope are found
  
}
