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
   * a koala.dynamicjava.tree.Type was manually created.
   */ 
  public void testClassToType() {
    assertEquals("",true,new IntType().
                   equals(TreeUtilities.classToType(int.class)));
    assertEquals("",true,new DoubleType().
                   equals(TreeUtilities.classToType(double.class)));
    assertEquals("",true,new LongType().
                   equals(TreeUtilities.classToType(long.class)));
    assertEquals("",true,new FloatType().
                   equals(TreeUtilities.classToType(float.class)));
    assertEquals("",true,new CharType().
                   equals(TreeUtilities.classToType(char.class)));
    assertEquals("",true,new ByteType().
                   equals(TreeUtilities.classToType(byte.class)));
    assertEquals("",true,new ShortType().
                   equals(TreeUtilities.classToType(short.class)));
    assertEquals("",true,new BooleanType().
                   equals(TreeUtilities.classToType(boolean.class)));
    assertEquals("",true,new VoidType().
                   equals(TreeUtilities.classToType(void.class)));
    assertEquals("",true,new ArrayType(new IntType(),1).
                   equals(TreeUtilities.classToType(int[].class)));
    assertEquals("",true,new ReferenceType(Integer.class.getName()).
                   equals(TreeUtilities.classToType(Integer.class)));
  }
  
}
