package reflectionTests;

import java.lang.reflect.*;

/**
 * This class is created with methods that the tests can look for.
 * They return numbers so that the result of invoking the returned 
 * method can be checked against what the VM chooses during runtime.
 */
public class TestClass {
  private int value;
  
  // Test with (int,int,int), ambiguous
  public TestClass(int i, double d, float f) { value = 1; }
  public TestClass(int i, float f, double d) { value = 2; }
  // Test with (String,int,int), ambiguous
  public TestClass(String s, Integer a, int b) { value = 3; }
  public TestClass(String s, int a, Integer b) { value = 4; }
  // Test with (Class,int), ambiguous
  public TestClass(Class<?> c, int... rest) { value = 5; }
  public TestClass(Class<?> c, int i, int... rest) { value = 6; }
  // Test with (Method,int), expect 7
  public TestClass(Method m, Number n) { value = 7; }
  public TestClass(Method m, Object o) { value = 8; }
  // Test with (Field,String,String,String), assert found 9
  public TestClass(Field f, String... msg) { value = 9; }
  // Test with (boolean, int), expect not found
  public TestClass(boolean b, Float f) { value = 10; }
  
  public TestClass(int i) { value = 11; }
  public TestClass(Integer i) { value = 12; }
  
  public static int test(int x, int y){ return 4; }
  public static int test(Integer x, Integer y){ return 5; }
  public static int test(int x, int y, int z){ return 6; }
  public static int test(int... i){ return 3; }
  
  public static int test0(int x) { return 1; }
  public static int test0(Integer x) { return 2; }
  
  public static int test1(double a, int b, float c){ return 1; }
  public static int test1(float a, int b, double c){ return 2; }
  
  public static int test2(int a, int b, int c, int... rest) { return 1; }
  public static int test2(Integer a, Integer b, Integer c) { return 2; }
  
  public static int test3(Integer a, double b) { return 1; }
  public static int test3(Integer a, long b) { return 2; }
  
  public static int test4(Number b) { return 1; }
  public static int test4(int b) { return 2; }
  
  public static int test5(int a, int... rest) { return 1; }
  public static int test5(int a, int b, int... rest) { return 2; }
  
  public static int test6(int a) { return 1; }
  
  public static int test7(Number i) { return 1; }
  public static int test8(Object o) { return 2; }
  
  public int value() { return value; }
}
