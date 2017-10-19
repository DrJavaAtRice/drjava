///*BEGIN_COPYRIGHT_BLOCK
// *
// * This file is part of DrJava.  Download the current version of this project:
// * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
// *
// * DrJava Open Source License
// * 
// * Copyright (C) 2001-2010 JavaPLT group at Rice University (drjava@rice.edu)
// * All rights reserved.
// *
// * Developed by:   Java Programming Languages Team
// *                 Rice University
// *                 http://www.cs.rice.edu/~javaplt/
// * 
// * Permission is hereby granted, free of charge, to any person obtaining a 
// * copy of this software and associated documentation files (the "Software"),
// * to deal with the Software without restriction, including without 
// * limitation the rights to use, copy, modify, merge, publish, distribute, 
// * sublicense, and/or sell copies of the Software, and to permit persons to 
// * whom the Software is furnished to do so, subject to the following 
// * conditions:
// * 
// *     - Redistributions of source code must retain the above copyright 
// *       notice, this list of conditions and the following disclaimers.
// *     - Redistributions in binary form must reproduce the above copyright 
// *       notice, this list of conditions and the following disclaimers in the
// *       documentation and/or other materials provided with the distribution.
// *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
// *       names of its contributors may be used to endorse or promote products
// *       derived from this Software without specific prior written permission.
// *     - Products derived from this software may not be called "DrJava" nor
// *       use the term "DrJava" as part of their names without prior written
// *       permission from the JavaPLT group.  For permission, write to
// *       drjava@rice.edu.
// * 
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
// * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
// * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
// * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
// * OTHER DEALINGS WITH THE SOFTWARE.
// * 
//END_COPYRIGHT_BLOCK*/
//
//package koala.dynamicjava.util;
//
//import koala.dynamicjava.interpreter.error.WrongVersionException;
//
//import junit.framework.TestCase;
//import java.lang.reflect.*;
//import java.util.*;
//
///**
// * Tests the utility methods in the ReflectionUtilities class to
// * make sure they are working correctly.
// */
//public class ReflectionUtilitiesTest extends DynamicJavaTestCase {
//  public void testTigerUsageClass() {
//    ReflectionUtilities.TigerUsage tu;
//    tu = new ReflectionUtilities.TigerUsage();
//    assertFalse("Autoboxing was not used", tu.isAutoBoxingUsed());
//    assertFalse("Var Args was not used", tu.areVarArgsUsed());
//    tu.autoBoxingIsUsed();
//    assertTrue("Autoboxing was used", tu.isAutoBoxingUsed());
//    assertFalse("Var Args was not used", tu.areVarArgsUsed());
//    tu.varArgsAreUsed();
//    assertTrue("Autoboxing was used", tu.isAutoBoxingUsed());
//    assertTrue("Var Args were used", tu.areVarArgsUsed());
//    
//    setTigerEnabled(false);
//    try {
//      tu.checkForCompatibleUsage();
//      TigerUtilities.resetVersion();
//      fail("Should have thrown a WrongVersionException");
//    }
//    catch(WrongVersionException e) {
//      TigerUtilities.resetVersion();
//    }
//  }
//  
//  /**
//   * Tests the isBoxCompatible method (The three following private methods
//   * are there to factor out the repetitive code within the actual test method)
//   */
//  public void testIsBoxCompatible() {
//    _assertBoxCompatible(boolean.class, boolean.class, false);
//    _assertBoxCompatible(Boolean.class, Boolean.class, false);
//    _assertBoxCompatible(boolean.class, Boolean.class, true);
//    _exceptionBoxCompatible(boolean.class, Boolean.class, false);
//    _assertNotBoxCompatible(boolean.class, int.class, true);
//    _assertNotBoxCompatible(int.class, boolean.class, true);
//    _assertNotBoxCompatible(int.class, Boolean.class, true);
//    
//    _assertBoxCompatible(double.class, float.class, true);
//    _assertBoxCompatible(double.class, long.class, true);
//    _assertBoxCompatible(double.class, int.class, true);
//    _assertBoxCompatible(double.class, short.class, true);
//    _assertBoxCompatible(double.class, byte.class, true);
//    _assertBoxCompatible(double.class, double.class, true);
//    _assertNotBoxCompatible(double.class, boolean.class, true);
//    
//    _assertBoxCompatible(Object.class, boolean.class, true);
//    _assertBoxCompatible(Object.class, byte.class, true);
//    _assertBoxCompatible(Object.class, char.class, true);
//    _assertBoxCompatible(Object.class, short.class, true);
//    _assertBoxCompatible(Object.class, int.class, true);
//    _assertBoxCompatible(Object.class, long.class, true);
//    _assertBoxCompatible(Object.class, float.class, true);
//    _assertBoxCompatible(Object.class, double.class, true);
//    _assertBoxCompatible(Number.class, int.class, true);
//    _assertNotBoxCompatible(Number.class, char.class, true);
//    _exceptionBoxCompatible(Number.class, int.class, false);
//    _exceptionBoxCompatible(Object.class, long.class, false);
//    _assertNotBoxCompatible(char.class, int.class, true); 
//    _assertNotBoxCompatible(long.class, float.class, true);
//    
//    _assertNotBoxCompatible(Integer.class, byte.class, true);
//    _assertNotBoxCompatible(Long.class, int.class, true);
//    
//    _assertBoxCompatible(java.util.List.class, java.util.Vector.class, false);
//    _assertBoxCompatible(java.util.List.class, java.util.Vector.class, true);
//    _assertBoxCompatible(Object.class, Character.class, false);
//  }
//  private void _assertBoxCompatible(Class<?> c1, Class<?> c2, boolean boxEnabled) {
//    setTigerEnabled(boxEnabled);
//    ReflectionUtilities.TigerUsage tu = new ReflectionUtilities.TigerUsage();
//    assertTrue("Should have been compatible: " +
//               c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")", 
//               ReflectionUtilities.isBoxCompatible(c1, c2, tu));
//    assertFalse("Wrong version exception would have been thrown (bad)",
//                !boxEnabled && tu.isAutoBoxingUsed());
//  }
//  private void _assertNotBoxCompatible(Class<?> c1, Class<?> c2, boolean boxEnabled) {
//    setTigerEnabled(boxEnabled);
//    ReflectionUtilities.TigerUsage tu = new ReflectionUtilities.TigerUsage();
//    assertFalse("Shouldn't have been compatible: " + 
//               c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")", 
//                ReflectionUtilities.isBoxCompatible(c1, c2, tu));
//    assertFalse("Wrong version exception would have been thrown (bad)",
//                !boxEnabled && tu.isAutoBoxingUsed());
//  }
//  private void _exceptionBoxCompatible(Class<?> c1, Class<?> c2, boolean boxEnabled) {
//    setTigerEnabled(boxEnabled);
//    ReflectionUtilities.TigerUsage tu = new ReflectionUtilities.TigerUsage();
//    ReflectionUtilities.isBoxCompatible(c1, c2, tu);
//    assertTrue("Should not have worked: " + 
//               c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")",
//               tu.isAutoBoxingUsed() &&
//               !boxEnabled);
//  }
//  
//  public void testLookupMethod() 
//    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//    setTigerEnabled(false);
//    Method m;
//    try {
//      m = ReflectionUtilities.lookupMethod(Integer.class, "toString", new Class<?>[]{Integer.class});
//      fail("Didn't throw a wrong version exception!");
//    }
//    catch (WrongVersionException e) {
//    }
//    catch (NoSuchMethodException e) {
//      fail("Was supposed to throw WrongVersionException, not NoSuchMethodException");
//    }
//    
//    try {
//      m = ReflectionUtilities.lookupMethod(Integer.class, "toString", new Class<?>[]{Integer.class, int.class, int.class});
//      fail("Didn't throw an method exception!");
//    }
//    catch (NoSuchMethodException e) {
//    }
//    
//    // test to see that it can find overridden methods in classes that extend othre classes
//    Class<?>[] pTypes = new Class<?>[]{Object.class};
//    m = ReflectionUtilities.lookupMethod(LinkedList.class, "add", pTypes);
//    assertTrue("Incorrect parameter types", Arrays.equals(pTypes, m.getParameterTypes()));
//    assertEquals("Method was selected from wrong class", LinkedList.class, m.getDeclaringClass());
//    
//    // The following tests use 1.5 to assure correct semantics
//    setTigerEnabled(true);
//    
//    // Check to see that lookupMethod selects the corret method.
//    Integer ONE = new Integer(1);
//    int result;
//    
//    // test to see that it can find overridden methods in classes that extend othre classes
//    m = ReflectionUtilities.lookupMethod(Vector.class, "add", new Class<?>[]{int.class});
//    assertTrue("Incorrect parameter types", Arrays.equals(new Class<?>[]{Object.class}, m.getParameterTypes()));
//    assertEquals("Method was selected from wrong class", Vector.class, m.getDeclaringClass());
//    
//    // test(1,1) -> test(int x, int y)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test", new Class<?>[]{int.class, int.class});
//    result = ((Integer)m.invoke(null, new Object[]{ONE, ONE})).intValue();
//    assertEquals("lookup with test(int,int) found wrong method", TestClass.test(1, 1), result);
//    
//    // test(1,1,ONE) -> test(int x, int y, int z)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test", new Class<?>[]{int.class, int.class, Integer.class});
//    assertEquals("Wrong number of arguments chosen for (int,int,Integer): " + m, 3, m.getParameterTypes().length);
//    result = ((Integer)m.invoke(null, new Object[]{ONE, ONE, ONE})).intValue();
//    assertEquals("lookup with test(int,int,Integer) found wrong method", TestClass.test(1,1,ONE), result);
//    
//    // test0(1) -> test0(int)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test0", new Class<?>[]{int.class});
//    assertEquals("Wrong number of arguments chosen for test0: " + m, 1, m.getParameterTypes().length);
//    result = ((Integer)m.invoke(null, new Object[]{ONE})).intValue();
//    assertEquals("lookup with test0 found wrong method", TestClass.test0(1), result);
//    
//    // test1(1,1,1) -> ambiguous
//    try {
//      m = ReflectionUtilities.lookupMethod(TestClass.class, "test1", new Class<?>[]{int.class, int.class, int.class});
//      fail("test1(int,int,int) Didn't throw an ambiguous method exception");
//    }
//    catch(AmbiguousMethodException e) {  }
//    
//    // test2(1,1,1) -> test2(Integer,Integer,Integer)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test2", new Class<?>[]{int.class, int.class, int.class});
//    assertEquals("Wrong number of arguments chosen for test2: " + m, 3, m.getParameterTypes().length);
//    result = ((Integer)m.invoke(null, new Object[]{ONE, ONE, ONE})).intValue();
//    assertEquals("lookup with test2 found wrong method", TestClass.test2(1,1,1), result);
//    
//    // test3(1,1) -> test3(Integer,Integer,Integer)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test3", new Class<?>[]{int.class, int.class});
//    assertEquals("Wrong number of arguments chosen for test3: " + m, 2, m.getParameterTypes().length);
//    result = ((Integer)m.invoke(null, new Object[]{ONE, ONE})).intValue();
//    assertEquals("lookup with test3 ound wrong method", TestClass.test3(1,1), result);
//    
//    // test4(ONE) -> test4(int)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test4", new Class<?>[]{Integer.class});
//    result = ((Integer)m.invoke(null, new Object[]{ONE})).intValue();
//    assertEquals("lookup with test4 ound wrong method", TestClass.test4(ONE), result);
//    
//    // test5(ONE) -> test5(int,int...)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test5", new Class<?>[]{Integer.class});
//    assertEquals("Wrong number of arguments chosen for test5: " + m, 2, m.getParameterTypes().length);
//    result = ((Integer)m.invoke(null, new Object[]{ONE, new int[]{}})).intValue();
//    assertEquals("lookup with test5 ound wrong method", TestClass.test5(1), result);
//    
//    // test5(1,1) -> ambiguous
//    try {
//      m = ReflectionUtilities.lookupMethod(TestClass.class, "test5", new Class<?>[]{int.class, int.class});
//      fail("test5(int,int) Didn't throw an ambiguous method exception");
//    }
//    catch(AmbiguousMethodException e) {  }
//    
//    // test6() -> method not found
//    try {
//      m = ReflectionUtilities.lookupMethod(TestClass.class, "test6", new Class<?>[]{});
//      fail("test6() didn't throw a NoSuchMethodException");
//    }
//    catch(NoSuchMethodException e) { }
//      
//    // java.util.Arrays.asList(Object...) with Integer[]
//    m = ReflectionUtilities.lookupMethod(java.util.Arrays.class, "asList", new Class<?>[]{Integer[].class});
//    
//    
//    // test5(ONE) -> test5(int,int...)
//    m = ReflectionUtilities.lookupMethod(TestClass.class, "test7", new Class<?>[]{Integer.class});
//    result = ((Integer)m.invoke(null, new Object[]{ONE})).intValue();
//    assertEquals("lookup with test5 ound wrong method", TestClass.test5(new Integer(1)), result);
//  }
//  
//  
//  public void testLookupConstructor() 
//    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//   
//    Constructor c;
//    int result;
//    
//    try {
//      c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{int.class,int.class,int.class});
//      fail("(int,int,int) Didn't throw an AmbiguousMethodException");
//    }
//    catch(AmbiguousMethodException e) { }
//      
//    try {
//      c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{String.class,int.class,int.class});
//      fail("(String,int,int) Didn't throw an AmbiguousMethodException");
//    }
//    catch(AmbiguousMethodException e) { }
//    
//    try {
//      c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{Class.class,int.class});
//      fail("(Class,int) Didn't throw an AmbiguousMethodException");
//    }
//    catch(AmbiguousMethodException e) { }
//    
//    c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{Method.class,int.class});
//    result = ((TestClass)c.newInstance(new Object[] {null,new Integer(1)})).value();
//    assertEquals("(Method,int) Should have picked #7 (Method,Number",new TestClass((Method)null,1).value(),result);
//    
//    c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{Field.class,String.class,String.class,String.class});
//    result = ((TestClass)c.newInstance(new Object[] {null,new String[]{"a","b","c"}})).value();
//    assertEquals("(Field,String,String,String) should have found varargs constructor",new TestClass((Field)null,"a","b","c").value(),result);
//    
//    c = ReflectionUtilities.lookupConstructor(TestClass.class, new Class<?>[]{int.class});
//    result = ((TestClass)c.newInstance(new Object[] {new Integer(1)})).value();
//    assertEquals("(int) should have found correct constructor",new TestClass(1).value(),result);
//  }
//  
//  /**
//   * Tests 
//   */
//  public void testBridgeMethodLookup() 
//    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//    Method m;
//    String result;
//    C1 instance = new C1();
//    Method m1 = C1.class.getDeclaredMethods()[1];
//    //System.err.println(m1.getModifiers());
//    assertTrue(m1+" isBridge?", TigerUtilities.isBridge(m1));
//    m = ReflectionUtilities.lookupMethod(C1.class, "method1", new Class<?>[]{});
//    result = ((String)m.invoke(instance, new Object[]{}));
//    assertEquals("lookup c1.method1", instance.method1(), result);
//  }
//  
//}
