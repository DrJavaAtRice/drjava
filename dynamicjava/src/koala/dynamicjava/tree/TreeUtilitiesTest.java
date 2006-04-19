package koala.dynamicjava.tree;

import junit.framework.TestCase;
//import koala.dynamicjava.tree.*;

/**
 * JUnit tests for the koala.dynamicjava.tree.TreeUtilities class
 */ 
public class TreeUtilitiesTest extends TestCase {
  

  /**
   * Simple tests for the classToType method.
   * There is one test for each case in the method.
   * It is assumed that the output of this method should be the same as if
   * a koala.dynamicjava.tree.TypeName was manually created.
   */ 
  public void testClassToType() {
    assertEquals("",true,new IntTypeName().
                   equals(TreeUtilities.classToType(int.class)));
    assertEquals("",true,new DoubleTypeName().
                   equals(TreeUtilities.classToType(double.class)));
    assertEquals("",true,new LongTypeName().
                   equals(TreeUtilities.classToType(long.class)));
    assertEquals("",true,new FloatTypeName().
                   equals(TreeUtilities.classToType(float.class)));
    assertEquals("",true,new CharTypeName().
                   equals(TreeUtilities.classToType(char.class)));
    assertEquals("",true,new ByteTypeName().
                   equals(TreeUtilities.classToType(byte.class)));
    assertEquals("",true,new ShortTypeName().
                   equals(TreeUtilities.classToType(short.class)));
    assertEquals("",true,new BooleanTypeName().
                   equals(TreeUtilities.classToType(boolean.class)));
    assertEquals("",true,new VoidTypeName().
                   equals(TreeUtilities.classToType(void.class)));
    assertEquals("",true,new ArrayTypeName(new IntTypeName(),1).
                   equals(TreeUtilities.classToType(int[].class)));
    assertEquals("",true,new ReferenceTypeName(Integer.class.getName()).
                   equals(TreeUtilities.classToType(Integer.class)));
  }
  
}
