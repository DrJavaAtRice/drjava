package edu.rice.cs.plt.reflect;

import java.net.URL;

import junit.framework.TestCase;

/** Helpful assert methods for testing class loaders */
public class ClassLoaderTestCase extends TestCase {
  
  public static void assertLoadsClass(ClassLoader l, String name) {
    try { l.loadClass(name); }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertLoadsClassAsLoader(ClassLoader l, String name) {
    try {
      Class<?> c = l.loadClass(name);
      assertSame(l, c.getClassLoader());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertDoesNotLoadClass(ClassLoader l, String name) {
    try { l.loadClass(name); }
    catch (ClassNotFoundException e) { return; }
    fail("Found class " + name);
  }
  
  public static void assertLoadsSameClass(ClassLoader l1, ClassLoader l2, String name) {
    try {
      Class<?> c1 = l1.loadClass(name);
      Class<?> c2 = l2.loadClass(name);
      assertSame(c1, c2);
      assertEquals(name, c1.getName());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertLoadsDifferentClass(ClassLoader l1, ClassLoader l2, String name) {
    try {
      Class<?> c1 = l1.loadClass(name);
      Class<?> c2 = l2.loadClass(name);
      assertNotSame(c1, c2);
      assertEquals(name, c1.getName());
      assertEquals(name, c2.getName());
      assertNotSame(c1.getClassLoader(), c2.getClassLoader());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertHasResource(ClassLoader l, String name) {
    assertNotNull(l.getResource(name));
  }
  
  public static void assertDoesNotHaveResource(ClassLoader l, String name) {
    assertNull(l.getResource(name));
  }
  
  public static void assertHasSameResource(ClassLoader l1, ClassLoader l2, String name) {
    URL r1 = l1.getResource(name);
    URL r2 = l2.getResource(name);
    assertNotNull(r1);
    assertNotNull(r2);
    assertEquals(r1, r2);
  }
  
  public static void assertHasDifferentResource(ClassLoader l1, ClassLoader l2, String name) {
    URL r1 = l1.getResource(name);
    URL r2 = l2.getResource(name);
    assertNotNull(r1);
    assertNotNull(r2);
    assertFalse(r1.equals(r2));
  }
  
}
