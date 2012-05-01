/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.reflect;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.reflect.ReflectUtil.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ReflectUtilTest extends TestCase {
  
  private void assertEquals(int x, Integer y) { assertEquals((Integer) x, y); }
  
  private static final File ROOT = new File("testFiles/classLoading");
  private static final File INTBOX_DIR = new File(ROOT, "intbox");
  private static final File A_DIR = new File(ROOT, "a");
  private static final File B_DIR = new File(ROOT, "b");
  private static final File C_DIR = new File(ROOT, "c");
  private static final File D_DIR = new File(ROOT, "d");
  
  private static final RuntimeException ANONYMOUS_EXCEPTION = new RuntimeException() {};
  
  public void testBootClassLoader() {
    ClassLoaderTestCase.assertLoadsClass(BOOT_CLASS_LOADER, "java.lang.Object");
    ClassLoaderTestCase.assertLoadsClass(BOOT_CLASS_LOADER, "java.lang.String");
    ClassLoaderTestCase.assertLoadsClass(BOOT_CLASS_LOADER, "java.lang.Number");
    ClassLoaderTestCase.assertLoadsClass(BOOT_CLASS_LOADER, "javax.swing.JFrame");
    ClassLoaderTestCase.assertLoadsClass(BOOT_CLASS_LOADER, "javax.net.ssl.SSLSession");
    ClassLoaderTestCase.assertDoesNotLoadClass(BOOT_CLASS_LOADER, ReflectUtil.class.getName());
    ClassLoaderTestCase.assertDoesNotLoadClass(BOOT_CLASS_LOADER, ReflectUtilTest.class.getName());
    ClassLoaderTestCase.assertDoesNotLoadClass(BOOT_CLASS_LOADER, IterUtil.class.getName());
  }

  public void testSimpleName() {
    assertEquals("ReflectUtilTest", simpleName(ReflectUtilTest.class));
    assertEquals("int", simpleName(int.class));
    assertEquals("void", simpleName(void.class));
    assertEquals("int[]", simpleName(int[].class));
    assertEquals("int[][]", simpleName(int[][].class));
    assertEquals("ReflectUtilTest[]", simpleName(ReflectUtilTest[].class));
    assertEquals("ReflectUtilTest[][][]", simpleName(ReflectUtilTest[][][].class));
    assertEquals("anonymous RuntimeException", simpleName(ANONYMOUS_EXCEPTION.getClass()));
    Runnable localRunnable = new Runnable() { public void run() {} };
    assertEquals("anonymous Runnable", simpleName(localRunnable.getClass()));
    assertEquals("Entry", simpleName(java.util.Map.Entry.class));
    class MethodInner {}
    assertEquals("MethodInner", simpleName(MethodInner.class));
  }
  
  public void testIsAnonymousClass() {
    assertFalse(isAnonymousClass(ReflectUtilTest.class));
    assertFalse(isAnonymousClass(int.class));
    assertFalse(isAnonymousClass(void.class));
    assertFalse(isAnonymousClass(int[].class));
    assertTrue(isAnonymousClass(ANONYMOUS_EXCEPTION.getClass()));
    Runnable localRunnable = new Runnable() { public void run() {} };
    assertTrue(isAnonymousClass(localRunnable.getClass()));
  }
  
  public void testCastAndGetClass() {
    Object o1 = "x";
    Object o2 = 23;
    Object o3 = LambdaUtil.valueLambda("foo");
    String s = cast(String.class, o1);
    Integer i = cast(Integer.class, o2);
    Integer i2 = cast(int.class, o2);
    Thunk<?> t = cast(Thunk.class, o3);
    
    assertEquals("x", s);
    assertEquals((Integer) 23, i);
    assertEquals((Integer) 23, i2);
    assertEquals("foo", t.value());
    
    try { cast(String.class, o2); fail("Expected exception"); }
    catch (ClassCastException e) { /* expected behavior */ }
    try { cast(String.class, o3); fail("Expected exception"); }
    catch (ClassCastException e) { /* expected behavior */ }
    try { cast(Thunk.class, o1); fail("Expected exception"); }
    catch (ClassCastException e) { /* expected behavior */ }
    
    Class<? extends String> c1 = ReflectUtil.getClass("x");
    Class<? extends Integer> c2 = ReflectUtil.getClass(23);
    Class<? extends Thunk<String>> c3 = ReflectUtil.getClass(LambdaUtil.valueLambda("foo"));
    Class<? extends Thunk<Integer>> c4 = ReflectUtil.getClass(LambdaUtil.valueLambda(13));
    s = cast(c1, o1);
    i = cast(c2, o2);
    t = cast(c3, o3);
    Thunk<String> t2 = cast(c3, o3);
    Thunk<Integer> t3 = cast(c4, o3); // limitation of the cast function
    
    assertEquals("x", s);
    assertEquals((Integer) 23, i);
    assertEquals("foo", t.value());
    assertEquals("foo", t2.value());
    try { @SuppressWarnings("unused") Integer dummy = t3.value(); fail("Expected exception"); }
    catch (ClassCastException e) {/* expected behavior */ }
  }
  
  
  public void testBox() {
    assertSame(Boolean.class, box(boolean.class));
    assertSame(Character.class, box(char.class));
    assertSame(Byte.class, box(byte.class));
    assertSame(Short.class, box(short.class));
    assertSame(Integer.class, box(int.class));
    assertSame(Long.class, box(long.class));
    assertSame(Float.class, box(float.class));
    assertSame(Double.class, box(double.class));
    assertSame(Void.class, box(void.class));
    assertSame(Integer.class, box(Integer.class));
    assertSame(String.class, box(String.class));
  }
  
  
  public void testLoadObject() throws Exception {
    @SuppressWarnings("unused") ReflectUtilTest t =
      (ReflectUtilTest) loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest");
    PathClassLoader l = (PathClassLoader) loadObject("edu.rice.cs.plt.reflect.PathClassLoader",
                                                     new Object[]{new File[]{INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR}});
    Object d1 = loadObject(l, "D");
    Object d2 = loadObject(l, "D", new Class<?>[0], (Object[]) null);
    assertNotSame(d1, d2);
    assertSame(d1.getClass(), d2.getClass());
    assertEquals(4, d1.getClass().getMethod("get").invoke(d1));
    assertEquals(4, d2.getClass().getMethod("get").invoke(d2));

    try { loadObject("fishing.boats"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest", 23); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { loadObject("java.lang.Comparable"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try {
      loadObject("edu.rice.cs.plt.lambda.LazyThunk", LambdaUtil.nullLambda());
      fail("expected exception");
    }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }

    try { loadObject("java.lang.String", new char[]{'a', 'b'}, -1, 23); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }

    try {
      loadObject("edu.rice.cs.plt.reflect.ReflectUtilTest", new Class<?>[0], 23);
      fail("expected exception");
    }
    catch (ReflectException e) { assertCorrectException(e, "IllegalArgument"); }
    
    try {
      loadObject("edu.rice.cs.plt.lambda.LazyThunk", new Class<?>[]{Thunk.class}, "I'm not a thunk");
      fail("expected exception");
    }
    catch (ReflectException e) { assertCorrectException(e, "IllegalArgument"); }

    try {
      loadObject("java.lang.String", new Class<?>[]{char[].class, int.class, int.class}, new char[]{'a', 'b'}, -1, 23);
      fail("expected exception");
    }
    catch (ReflectException e) { assertCorrectException(e, "InvocationTarget"); }
  }
  
  
  public static final int STATIC_FIELD = 119;
  public final int nonstaticField = 120;
  
  public void testGetStaticField() throws Exception {
    assertEquals(STATIC_FIELD, getStaticField("edu.rice.cs.plt.reflect.ReflectUtilTest", "STATIC_FIELD"));
    
    ClassLoader l = new PathClassLoader(INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR);
    assertEquals("A", getStaticField(l, "pkg.A", "NAME"));
    assertEquals("B", getStaticField(l, "bpkg.B", "NAME"));
    assertEquals("C", getStaticField(l, "pkg.C", "NAME"));
    assertEquals("D", getStaticField(l, "D", "NAME"));
    
    try { getStaticField("pkg.A", "NAME"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { getStaticField("fishing.boats", "foo"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { getStaticField("edu.rice.cs.plt.reflect.ReflectUtilTest", "mom"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchField"); }
    
    try { getStaticField("edu.rice.cs.plt.reflect.ReflectUtilTest", "nonstaticField"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NullPointer"); }
  }
  
  
  public void testInvokeStaticMethod() throws Exception {
    Thunk<?> nullThunk = (Thunk<?>) invokeStaticMethod("edu.rice.cs.plt.lambda.LambdaUtil", "nullLambda");
    assertEquals(null, nullThunk.value());
    
    Object o = new Object();
    Thunk<?> valueThunk1 = (Thunk<?>) invokeStaticMethod("edu.rice.cs.plt.lambda.LambdaUtil", "valueLambda", o);
    assertSame(o, valueThunk1.value());
    
    Thunk<?> valueThunk2 = (Thunk<?>) invokeStaticMethod("edu.rice.cs.plt.lambda.LambdaUtil", "valueLambda",
                                                         new Class<?>[]{ Object.class }, 2.83);
    assertEquals(2.83, valueThunk2.value());
    
    assertEquals("123", invokeStaticMethod("java.lang.String", "valueOf", new Class<?>[]{ int.class }, 123));
    
    ClassLoader l = new PathClassLoader(INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR);
    assertEquals("A", invokeStaticMethod(l, "pkg.A", "getName"));
    assertEquals("B", invokeStaticMethod(l, "bpkg.B", "getName"));
    assertEquals("C", invokeStaticMethod(l, "pkg.C", "getName"));
    assertEquals("D", invokeStaticMethod(l, "D", "getName"));
    
    try { invokeStaticMethod("pkg.A", "getName"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { invokeStaticMethod("fishing.boats", "foo"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "ClassNotFound"); }
    
    try { invokeStaticMethod("edu.rice.cs.plt.reflect.ReflectUtilTest", "mom"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { invokeStaticMethod("edu.rice.cs.plt.lambda.LambdaUtil", "valueLambda"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { invokeStaticMethod("edu.rice.cs.plt.lambda.LambdaUtil", "valueLambda", 2.83); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    try { invokeStaticMethod("java.lang.String", "length"); fail("expected exception"); }
    catch (ReflectException e) { assertCorrectException(e, "NullPointer"); }
    
    try {
      invokeStaticMethod("java.lang.String", "valueOf", new Class<?>[]{ int.class }, 12.3);
      fail("expected exception");
    }
    catch (ReflectException e) { assertCorrectException(e, "IllegalArgument"); }
  }
  
      
  public void testLoadLibraryAdapter() throws Exception {
    PathClassLoader base = new PathClassLoader(INTBOX_DIR, D_DIR);
    Object d = loadLibraryAdapter(base, IterUtil.make(A_DIR, B_DIR, C_DIR), "D");
    assertNotSame(base, d.getClass().getClassLoader());
    assertEquals(4, d.getClass().getMethod("get").invoke(d));
  }
  
  
  public static String MUTABLE_STATIC_FIELD;
  public static String mutableNonstaticField;
  
  public void testStaticFieldAsBox() {
    Box<String> box = staticFieldAsBox(ReflectUtilTest.class, "MUTABLE_STATIC_FIELD", String.class);
    box.set("text1");
    assertEquals("text1", MUTABLE_STATIC_FIELD);
    assertEquals("text1", box.value());
    box.set("text2");
    assertEquals("text2", MUTABLE_STATIC_FIELD);
    assertEquals("text2", box.value());
    
    Box<Integer> box2 = staticFieldAsBox(ReflectUtilTest.class, "STATIC_FIELD", int.class);
    assertEquals(119, box2.value());
    try { box2.set(33); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "IllegalAccess"); }
    
    assertSame(System.out, staticFieldAsBox(System.class, "out", java.io.PrintStream.class).value());
    
    // creating a bad box should be safe -- exception only occurs on "value()"
    Box<Integer> box4 = staticFieldAsBox(ReflectUtilTest.class, "mom", int.class);
    try { box4.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchField"); }
    
    Box<Integer> box5 = staticFieldAsBox(ReflectUtilTest.class, "nonstaticField", int.class);
    try { box5.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NullPointer"); }
    
    Box<String> box6 = staticFieldAsBox(ReflectUtilTest.class, "STATIC_FIELD", String.class);
    try { box6.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "ClassCast"); }
  }
  
  
  public void testFieldAsBox() {
    Box<String> box = fieldAsBox(this, "mutableNonstaticField", String.class);
    box.set("text1");
    assertEquals("text1", mutableNonstaticField);
    assertEquals("text1", box.value());
    box.set("text2");
    assertEquals("text2", mutableNonstaticField);
    assertEquals("text2", box.value());
    
    Box<Integer> box2 = fieldAsBox(this, "nonstaticField", int.class);
    assertEquals(120, box2.value());
    try { box2.set(33); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "IllegalAccess"); }
    
    // creating a bad box should be safe -- exception only occurs on "value()"
    Box<String> box3 = fieldAsBox(this, "mom", String.class);
    try { box3.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchField"); }
    
    Box<String> box4 = fieldAsBox(this, "nonstaticField", String.class);
    try { box4.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "ClassCast"); }
  }
  
  
  public void testStaticMethodLambdas() {
    @SuppressWarnings({"unchecked", "rawtypes"}) Thunk<Thunk> nullLambda =
      staticMethodAsThunk(LambdaUtil.class, "nullLambda", Thunk.class);
    assertEquals(null, nullLambda.value().value());
    
    @SuppressWarnings({"unchecked", "rawtypes"}) Lambda<Object, Thunk> valueLambda =
      staticMethodAsLambda(LambdaUtil.class, "valueLambda", Object.class, Thunk.class);
    assertEquals("foo", valueLambda.value("foo").value());
    
    @SuppressWarnings({"unchecked", "rawtypes"}) Lambda2<Predicate, Predicate, Predicate> and =
      staticMethodAsLambda2(LambdaUtil.class, "and", Predicate.class, Predicate.class, Predicate.class);
    Predicate<?> p1 = and.value(LambdaUtil.IS_NULL, LambdaUtil.IS_NULL);
    assertTrue(p1.contains(null));
    Predicate<?> p2 = and.value(LambdaUtil.NOT_NULL, LambdaUtil.IS_NULL);
    assertFalse(p2.contains(null));
    Predicate<?> p3 = and.value(LambdaUtil.NOT_NULL, LambdaUtil.NOT_NULL);
    assertFalse(p3.contains(null));
    
    @SuppressWarnings({"unchecked", "rawtypes"}) Lambda3<Predicate, Predicate, Predicate, Predicate> or =
      staticMethodAsLambda3(LambdaUtil.class, "or", Predicate.class, Predicate.class, Predicate.class, Predicate.class);
    Predicate<?> p4 = or.value(LambdaUtil.IS_NULL, LambdaUtil.IS_NULL, LambdaUtil.IS_NULL);
    assertTrue(p4.contains(null));
    Predicate<?> p5 = or.value(LambdaUtil.NOT_NULL, LambdaUtil.IS_NULL, LambdaUtil.NOT_NULL);
    assertTrue(p5.contains(null));
    Predicate<?> p6 = or.value(LambdaUtil.NOT_NULL, LambdaUtil.NOT_NULL, LambdaUtil.NOT_NULL);
    assertFalse(p6.contains(null));
    
    // creating a bad thunk should be safe -- exception only occurs on "value()"
    Thunk<String> t = staticMethodAsThunk(LambdaUtil.class, "nullLambda", String.class);
    try { t.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "ClassCast"); }
    
    Lambda<Cloneable, String> l = staticMethodAsLambda(LambdaUtil.class, "fish", Cloneable.class, String.class);
    try { l.value(null); }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchMethod"); }
    try { l.value(null); /* make sure it still behaves correctly */ }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchMethod"); }
  }
  
  
  public void testMethodLambdas() {
    Lambda<String, Integer> length = methodAsLambda(String.class, "length", Integer.class);
    assertEquals(3, length.value("foo"));
    assertEquals(0, length.value(""));
    
    Thunk<Integer> lengthOfFoo = methodAsThunk("foo", "length", Integer.class);
    assertEquals(3, lengthOfFoo.value());
    
    Lambda<Object, String> toString = methodAsLambda(Object.class, "toString", String.class);
    assertEquals("45", toString.value(45));
    assertEquals("true", toString.value(Boolean.TRUE));
    try { System.out.println(toString.value(null)); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NullPointer"); }
    
    Lambda2<String, String, String> concat = methodAsLambda2(String.class, "concat", String.class, String.class);
    assertEquals("abcd", concat.value("ab", "cd"));
    
    Lambda<String, String> fooConcat = methodAsLambda("foo", "concat", String.class, String.class);
    assertEquals("foox", fooConcat.value("x"));
    
    Lambda<Integer, String> l = methodAsLambda("foo", "concat", Integer.class, String.class);
    try { l.value(13); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    Lambda<String, Integer> l2 = methodAsLambda("foo", "concat", String.class, Integer.class);
    try { l2.value("x"); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "ClassCast"); }
    
    Lambda3<String, Character, Character, String> replace =
      methodAsLambda3(String.class, "replace", char.class, char.class, String.class);
    assertEquals("a.c", replace.value("abc", 'b', '.'));
    assertEquals("...", replace.value("aaa", 'a', '.'));
    
    Lambda2<Character, Character, String> fooReplace =
      methodAsLambda2("foo", "replace", char.class, char.class, String.class);
    assertEquals("f..", fooReplace.value('o', '.'));
    
    Lambda4<StringBuffer, char[], Integer, Integer, StringBuffer> append =
      methodAsLambda4(StringBuffer.class, "append", char[].class, int.class, int.class, StringBuffer.class);
    assertEquals("hill", append.value(new StringBuffer("hi"), new char[]{'b','a','l','l'}, 2, 2).toString());
    
    Lambda3<char[], Integer, Integer, StringBuffer> fooAppend =
      methodAsLambda3(new StringBuffer("foo"), "append", char[].class, int.class, int.class, StringBuffer.class);
    assertEquals("food", fooAppend.value(new char[]{'c','d','e'}, 1, 1).toString());
  }
  
  
  public void testConstructorLambdas() {
    Thunk<Object> t1 = constructorAsThunk(Object.class);
    assertNotNull(t1.value());
    
    Thunk<String> t2 = constructorAsThunk(String.class);
    assertEquals("", t2.value());
    
    Lambda<char[], String> l1 = constructorAsLambda(String.class, char[].class);
    assertEquals("abcd", l1.value(new char[]{'a', 'b', 'c', 'd'}));
    
    Lambda3<char[], Integer, Integer, String> l2 =
      constructorAsLambda3(String.class, char[].class, int.class, int.class);
    assertEquals("bc", l2.value(new char[]{'a', 'b', 'c', 'd'}, 1, 2));
    
    @SuppressWarnings({"unchecked", "rawtypes"}) Lambda<Object, SimpleBox> l3 =
      constructorAsLambda(SimpleBox.class, Object.class);
    assertEquals(23, l3.value(23).value());
    
    Thunk<Cloneable> t3 = constructorAsThunk(Cloneable.class);
    try { t3.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "NoSuchMethod"); }
    
    Thunk<Process> t4 = constructorAsThunk(Process.class);
    try { t4.value(); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "Instantiation"); }
    
    Lambda<String, Integer> l4 = constructorAsLambda(Integer.class, String.class);
    assertEquals(144, l4.value("144"));
    try { l4.value("14fish"); fail("expected exception"); }
    catch (WrappedException e) { assertCorrectException(e, "InvocationTarget"); }
  }
  
  
  private void assertCorrectException(WrappedException e, String expected) {
    if (e.getCause() instanceof ReflectException) {
      assertCorrectException((ReflectException) e.getCause(), expected);
    }
    else { throw e; }
  }
  
  private void assertCorrectException(ReflectException e, final String expected) {
    e.apply(new ReflectExceptionVisitor<Void>() {
      public Void defaultCase(Exception e) { fail("Unexpected exception type"); return null; }
      public Void forClassNotFound(ClassNotFoundException e) { assertEquals(expected, "ClassNotFound"); return null; }
      public Void forNoSuchField(NoSuchFieldException e) { assertEquals(expected, "NoSuchField"); return null; }
      public Void forNoSuchMethod(NoSuchMethodException e) { assertEquals(expected, "NoSuchMethod"); return null; }
      public Void forIllegalArgument(IllegalArgumentException e) { assertEquals(expected, "IllegalArgument"); return null; }
      public Void forNullPointer(NullPointerException e) { assertEquals(expected, "NullPointer"); return null; }
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
