package koala.dynamicjava.tree;

import junit.framework.TestCase;
//import koala.dynamicjava.tree.*;

/**
 * JUnit tests for the koala.dynamicjava.tree.ForEachStatement Class.
 */
public class ForEachStatementTest extends TestCase {
  
  /*Instantiate a ForEachStatement*/
  private Type _type = new LongType();
  private FormalParameter _formalParam = new FormalParameter(false,_type,"Test Parameter");
  private Expression _typeExpression = new TypeExpression(_type,"test",0,0,0,0);
  private ForEachStatement _testSubject = new ForEachStatement(_formalParam, _typeExpression,_type);
  
  /**
   * Simple test for the AddLabel and HasLabel methods.
   */
  public void testAddLabel_HasLabel() {
    assertEquals("",false,_testSubject.hasLabel("Test Label"));
    _testSubject.addLabel("Test Label");
    assertEquals("",true,_testSubject.hasLabel("Test Label"));
  }
  
}
