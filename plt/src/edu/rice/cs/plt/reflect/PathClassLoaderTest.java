package edu.rice.cs.plt.reflect;

import java.io.File;

public class PathClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = PreemptingClassLoaderTest.class.getClassLoader();
  private static final File ROOT = new File("testFiles/classLoading");
  private static final File INTBOX_DIR = new File(ROOT, "intbox");
  private static final File A_DIR = new File(ROOT, "a");
  private static final File B_DIR = new File(ROOT, "b");
  private static final File C_DIR = new File(ROOT, "c");
  private static final File D_DIR = new File(ROOT, "d");
  
  public void testLoadsPath() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR);
    assertLoadsClassAsLoader(l, "IntBox");
    assertLoadsClassAsLoader(l, "A");
    assertLoadsClassAsLoader(l, "B");
    assertLoadsClassAsLoader(l, "C");
    assertLoadsClassAsLoader(l, "D");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.PathClassLoaderTest");
    assertCanGet(l, "A", 1);
    assertCanGet(l, "D", 4);
  }
  
  public void testLoadsJumbledPath() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, D_DIR, INTBOX_DIR, B_DIR, C_DIR, A_DIR);
    assertLoadsClassAsLoader(l, "IntBox");
    assertLoadsClassAsLoader(l, "A");
    assertLoadsClassAsLoader(l, "B");
    assertLoadsClassAsLoader(l, "C");
    assertLoadsClassAsLoader(l, "D");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.PathClassLoaderTest");
    assertCanGet(l, "A", 1);
    assertCanGet(l, "D", 4);
  }
  
  public void testNestedLoaders() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR);
    PathClassLoader lA = new PathClassLoader(l, A_DIR);
    PathClassLoader lB = new PathClassLoader(lA, B_DIR);
    PathClassLoader lC = new PathClassLoader(lB, C_DIR);
    PathClassLoader lD = new PathClassLoader(lC, D_DIR);
    
    assertLoadsClassAsLoader(l, "IntBox");
    assertDoesNotLoadClass(l, "A");
    assertDoesNotLoadClass(l, "B");
    assertDoesNotLoadClass(l, "C");
    assertDoesNotLoadClass(l, "D");
    
    assertLoadsSameClass(l, lA, "IntBox");
    assertLoadsClassAsLoader(lA, "A");
    assertDoesNotLoadClass(lA, "B");
    assertDoesNotLoadClass(lA, "C");
    assertDoesNotLoadClass(lA, "D");
    
    assertLoadsSameClass(l, lB, "IntBox");
    assertLoadsSameClass(lA, lB, "A");
    assertLoadsClassAsLoader(lB, "B");
    assertDoesNotLoadClass(lB, "C");
    assertDoesNotLoadClass(lB, "D");
    
    assertLoadsSameClass(l, lC, "IntBox");
    assertLoadsSameClass(lA, lC, "A");
    assertLoadsSameClass(lB, lC, "B");
    assertLoadsClassAsLoader(lC, "C");
    assertDoesNotLoadClass(lC, "D");
    
    assertLoadsSameClass(l, lD, "IntBox");
    assertLoadsSameClass(lA, lD, "A");
    assertLoadsSameClass(lB, lD, "B");
    assertLoadsSameClass(lC, lD, "C");
    assertLoadsClassAsLoader(lD, "D");
    
    assertCanGet(lD, "A", 1);
    assertCanGet(lD, "D", 4);
  }
  
  public void testPoorlyNestedLoaders() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR);
    PathClassLoader lAB = new PathClassLoader(l, A_DIR, B_DIR);
    PathClassLoader lD = new PathClassLoader(lAB, D_DIR);
    PathClassLoader lC = new PathClassLoader(lD, C_DIR);
    
    assertCanGet(lC, "C", 3);
    assertCannotGet(lC, "D");
  }
  
  private void assertCanGet(ClassLoader l, String className, int value) throws Exception {
    Class<?> c = l.loadClass(className);
    Object instance = c.newInstance();
    int result = (Integer) c.getMethod("get").invoke(instance);
    assertEquals(value, result);
  }
  
  private void assertCannotGet(ClassLoader l, String className) {
    try {
      Class<?> c = l.loadClass(className);
      Object instance = c.newInstance();
      int result = (Integer) c.getMethod("get").invoke(instance);
    }
    catch (Exception e) { return; }
    fail("Able to invoke get() in class " + className);
  }
  
}
