package edu.rice.cs.plt.reflect;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.reflect.ReflectUtil.*;

public class ReflectUtilTest extends TestCase {
  
  private static final File ROOT = new File("testFiles/classLoading");
  private static final File INTBOX_DIR = new File(ROOT, "intbox");
  private static final File A_DIR = new File(ROOT, "a");
  private static final File B_DIR = new File(ROOT, "b");
  private static final File C_DIR = new File(ROOT, "c");
  private static final File D_DIR = new File(ROOT, "d");

  public void testSimpleName() {
    assertEquals("ReflectUtilTest", simpleName(ReflectUtilTest.class));
    assertEquals("int", simpleName(int.class));
    assertEquals("void", simpleName(void.class));
    assertEquals("int[]", simpleName(int[].class));
    assertEquals("int[][]", simpleName(int[][].class));
    assertEquals("ReflectUtilTest[]", simpleName(ReflectUtilTest[].class));
    assertEquals("ReflectUtilTest[][][]", simpleName(ReflectUtilTest[][][].class));
    assertEquals("anonymous Predicate", simpleName(Predicate.TRUE.getClass()));
  }
  
  public void testIsAnonymousClass() {
    assertFalse(isAnonymousClass(ReflectUtilTest.class));
    assertFalse(isAnonymousClass(int.class));
    assertFalse(isAnonymousClass(void.class));
    assertFalse(isAnonymousClass(int[].class));
    assertTrue(isAnonymousClass(Predicate.TRUE.getClass()));
    assertTrue(isAnonymousClass(LambdaUtil.nullThunk().getClass()));
  }
  
  public void testCast() {
    Object o1 = "x";
    Object o2 = 23;
    Object o3 = LambdaUtil.nullThunk();
    assertEquals("x", cast(String.class, o1));
    assertEquals((Integer) 23, cast(Integer.class, o2));
    Thunk<?> t = cast(Thunk.class, o3);
    assertEquals(null, t.value());
  }
  
  public void testLoadObject() throws Exception {
    ReflectUtilTest t = (ReflectUtilTest) loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest");
    PathClassLoader l = (PathClassLoader) loadObject("edu.rice.cs.plt.reflect.PathClassLoader",
                                                     new Object[]{new File[]{INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR}});
    Object d1 = loadObject(l, "D");
    Object d2 = loadObject(l, "D", new Class[0], (Object[]) null);
    assertNotSame(d1, d2);
    assertSame(d1.getClass(), d2.getClass());
    assertEquals(4, d1.getClass().getMethod("get").invoke(d1));
    assertEquals(4, d2.getClass().getMethod("get").invoke(d2));
  }
  
  public void testLoadObjectThrowsCorrectException() {
    try { loadObject("fishing.boats"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest", 23); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { loadObject("java.lang.Comparable"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { loadObject("edu.rice.cs.plt.lambda.LazyThunk", LambdaUtil.nullThunk()); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }

    try { loadObject("java.lang.String", new char[]{'a', 'b'}, -1, 23); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }

    try { loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest", new Class[0], 23); }
    catch (ReflectException e) { assertCorrectException(e, "IllegalArgument"); }
    
    try { loadObject("edu.rice.cs.plt.lambda.LazyThunk", new Class<?>[]{Thunk.class}, LambdaUtil.nullLambda()); }
    catch (ReflectException e) { assertCorrectException(e, "IllegalArgument"); }

    try {
      loadObject("java.lang.String", new Class<?>[]{char[].class, int.class, int.class}, new char[]{'a', 'b'}, -1, 23);
    }
    catch (ReflectException e) { assertCorrectException(e, "InvocationTarget"); }
  }
  
  public void testLoadLibraryAdapter() throws Exception {
    PathClassLoader base = new PathClassLoader(INTBOX_DIR, D_DIR);
    Object d = loadLibraryAdapter(base, IterUtil.makeIterable(A_DIR, B_DIR, C_DIR), "D");
    assertNotSame(base, d.getClass().getClassLoader());
    assertEquals(4, d.getClass().getMethod("get").invoke(d));
  }
  
  
  private void assertCorrectException(ReflectException e, final String expected) {
    e.apply(new ReflectExceptionVisitor<Void>() {
      public Void defaultCase(Exception e) { fail("Unexpected exception type"); return null; }
      public Void forClassNotFound(ClassNotFoundException e) { assertEquals(expected, "ClassNotFound"); return null; }
      public Void forNoSuchField(NoSuchFieldException e) { assertEquals(expected, "NoSuchField"); return null; }
      public Void forNoSuchMethod(NoSuchMethodException e) { assertEquals(expected, "NoSuchMethod"); return null; }
      public Void forIllegalArgument(IllegalArgumentException e) { assertEquals(expected, "IllegalArgument"); return null; }
      public Void forClassCast(ClassCastException e) { assertEquals(expected, "ClassCast"); return null; }
      public Void forInvocationTarget(InvocationTargetException e) { assertEquals(expected, "InvocationTarget"); return null; }
      public Void forInstantiation(InstantiationException e) { assertEquals(expected, "Instantiation"); return null; }
      public Void forIllegalAccess(IllegalAccessException e) { assertEquals(expected, "IllegalAccess"); return null; }
      public Void forSecurity(SecurityException e) { assertEquals(expected, "Security"); return null; }
    });
    
    // Just to make sure ReflectExceptionVisitor works properly, do this as well:
    e.apply(new ReflectExceptionVisitor<Void>() {
      public Void defaultCase(Exception e) { assertEquals(expected + "Exception", simpleName(e.getClass())); return null; }
    });
  }
  
}
