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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.plt.reflect.ReflectException.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public final class ReflectUtil {
  
  /** Prevents instance creation */
  private ReflectUtil() {}
  
  /**
   * A ClassLoader for the bootstrap classes -- those provided by the bootstrap class path in Sun
   * JVMs.  Note that this is not the actual loader used for bootstrap classes, but rather an immediate
   * child; for any available class {@code c} ({@code "java.lang.Number"} or {@code "javax.swing.JFrame"},
   * for example), {@code BOOT_CLASS_LOADER.loadClass(c).getClassLoader()} has value
   * {@code null}.  When constructing a class loader, there is no need to use this object as a parent --
   * simply use {@code null} as the parent parameter.
   */
  public static final ClassLoader BOOT_CLASS_LOADER = new ClassLoader(null) {};
  
  /**
   * The value of system property "java.class.path", parsed as a list of files.  Consistent with most
   * other uses of JVM properties in Java libraries, does not reflect subsequent changes to the property.
   */
  public static final Iterable<File> SYSTEM_CLASS_PATH = IOUtil.parsePath(System.getProperty("java.class.path", ""));
  
  /**
   * Produce the simple name of the given class, as specified by {@link Class#getSimpleName},
   * with an improved scheme for anonymous classes.  The simple name of a class is generally
   * the unqualified name used to declare it.  Arrays evaluate to the simple name of their
   * element type, followed by a pair of brackets.  Anonymous classes, rather than evaluating to an 
   * empty string, produce something like "anonymous Foo" (where Foo is the supertype).  Assumes
   * non-anonymous classes follow a naming convention in which the simple name is the suffix
   * of the full class name following all '.' and '$' characters, and immediately following a
   * (possibly empty) sequence of digits.
   */
  public static String simpleName(Class<?> c) {
    if (c.isArray()) { return simpleName(c.getComponentType()) + "[]"; }
    else if (isAnonymousClass(c)) {
      if (c.getInterfaces().length > 0) { return "anonymous " + simpleName(c.getInterfaces()[0]); }
      else { return "anonymous " + simpleName(c.getSuperclass()); }
    }
    else {
      String fullName = c.getName();
      int dot = fullName.lastIndexOf('.');
      int dollar = fullName.lastIndexOf('$');
      int nameStart = (dot > dollar) ? dot+1 : dollar+1;
      int length = fullName.length();
      while (nameStart < length && Character.isDigit(fullName.charAt(nameStart))) { nameStart++; }
      return fullName.substring(nameStart);
    }
  }
  
  /** An implementation of {@link Class#isAnonymousClass}, which is unavailable prior to Java 5.0 */
  public static boolean isAnonymousClass(Class<?> c) {
    String name = c.getName();
    String nameEnd = name.substring(name.lastIndexOf('$') + 1); // index is -1 if there is none
    for (int i = 0; i < nameEnd.length(); i++) {
      if (Character.isJavaIdentifierStart(nameEnd.charAt(i))) { return false; }
    }
    return true;
  }
  
  /**
   * Gets the base component type of an array of arbitrary dimensions.  For example, for each of {@code String},
   * {@code String[]}, and {@code String[][]}, this is {@code String}.
   */
  public static Class<?> arrayBaseClass(Class<?> c) {
    Class<?> result = c;
    while (result.isArray()) { result = result.getComponentType(); }
    return result;
  }
  
  /**
   * Return the number of array dimensions represented by the given class.  For {@code String[][]}, this is 2.
   * For {@code String}, this is 0.
   */
  public static int arrayDimensions(Class<?> c) {
    Class<?> rest = c;
    int result = 0;
    while (rest.isArray()) { rest = rest.getComponentType(); result++; }
    return result;
  }
  
  /**
   * <p>An implementation of {@link Class#cast}, which is unavailable prior to Java 5.0.  Unlike the
   * Java API method, this version allows a boxed value to be cast to its unboxed equivalent &mdash; for
   * example, {@code ReflectUtil.cast(int.class, 23)} will succeed (see SDN bug 6456930).</p>
   * 
   * <p>The method is only capable of checking that {@code o} is an instance of the class {@code c} &mdash; not 
   * that {@code o} is an instance of type {@code T} (for example, casting with a {@code Class<List<Integer>>} would 
   * only check that {@code o} is a {@code List<?>}, but the return type would by {@code List<Integer>}).
   * Such discrepancies are rare in practice, because class literals ({@code Foo.class}) will only produce classes
   * for which {@code T} is unparameterized or raw.</p>
   * 
   * <p>Following {@code Class.cast()}, a boxed type cannot be cast to its unboxed equivalent, despite the fact
   * that such a cast would be safe.  (From a practical point of view, we would prefer to avoid incurring the
   * overhead of {@link #box} on every cast.)</li>
   * </ul>
   * @throws ClassCastException  If the object cannot be cast to the given type.
   */
  public static <T> T cast(Class<? extends T> c, Object o) throws ClassCastException {
    if (box(c).isInstance(o)) {
      @SuppressWarnings("unchecked") T result = (T) o;
      return result;
    }
    else { throw new ClassCastException("Casting to " + c.getName() + " from " + o.getClass().getName()); }
  }
  
  /**
   * Produce a correctly-typed class corresponding to {@code obj}.  {@code obj.getClass()}, in contrast,
   * returns a {@code Class<?>}.
   */
  @SuppressWarnings("unchecked") public static <T> Class<? extends T> getClass(T obj) {
    return (Class<? extends T>) obj.getClass();
  }
  
  /** If {@code c} is a primitive type, return its boxed counterpart; otherwise, return {@code c}. */
  @SuppressWarnings("unchecked") public static <T> Class<T> box(Class<T> c) {
    if (c.isPrimitive()) {
      if (c == Boolean.TYPE) { return (Class<T>) Boolean.class; }
      if (c == Character.TYPE) { return (Class<T>) Character.class; }
      if (c == Byte.TYPE) { return (Class<T>) Byte.class; }
      if (c == Short.TYPE) { return (Class<T>) Short.class; }
      if (c == Integer.TYPE) { return (Class<T>) Integer.class; }
      if (c == Long.TYPE) { return (Class<T>) Long.class; }
      if (c == Float.TYPE) { return (Class<T>) Float.class; }
      if (c == Double.TYPE) { return (Class<T>) Double.class; }
      if (c == Void.TYPE) { return (Class<T>) Void.class; }
    }
    return c;
  }
  

  private static final ClassLoader CURRENT_LOADER = ReflectUtil.class.getClassLoader();
  
  /** Apply getClass() to an array of objects */
  private static Class<?>[] getClasses(Object[] args) {
    if (args.length > 0) {
      Class<?>[] sig = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) { sig[i] = args[i].getClass(); }
      return sig;
    }
    else { return null; }
  }
  
  /**
   * <p>Create an instance of the given class.  This is a convenience method that uses the class loader of
   * {@code ReflectUtil} to load {@code className}, and that infers the constructor signature from the
   * given arguments.</p>
   * 
   * <p>Note that the inference process for the constructor signature is potentially error-prone, because it 
   * uses the concrete runtime types of the objects, which may be more specific than what is needed.  It is
   * impossible, for example, to use this method to invoke a constructor whose parameters include
   * interface, abstract class, or primitive types.  In such cases, 
   * {@link #loadObject(String, Class[], Object[])} should be used instead.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefFoundError} may occur), reflection must be used to load 
   * the object.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   * @see #loadObject(String, Class[], Object[])
   * @see #loadObject(ClassLoader, String, Class[], Object[])
   */
  public static Object loadObject(String className, Object... constructorArgs) throws ReflectException {
    return loadObject(CURRENT_LOADER, className, getClasses(constructorArgs), constructorArgs);
  }
  
  /**
   * <p>Create an instance of the given class.  This is a convenience method that uses the class loader of
   * {@code ReflectUtil} to load {@code className}.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefFoundError} may occur), reflection must be used to load 
   * the object.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   * @see #loadObject(String, Object[])
   * @see #loadObject(ClassLoader, String, Class[], Object[])
   */
  public static Object loadObject(String className, Class<?>[] constructorSig, Object... constructorArgs) 
    throws ReflectException {
    return loadObject(CURRENT_LOADER, className, constructorSig, constructorArgs);
  }
  
  /**
   * <p>Create an instance of the given class.  This is a convenience method that infers the constructor 
   * signature from the given arguments.</p>
   * 
   * <p>Note that the inference process for the constructor signature is potentially error-prone, because it 
   * uses the concrete runtime types of the objects, which may be more specific than what is needed.  It is
   * impossible, for example, to use this method to invoke a constructor whose parameters include
   * interface, abstract class, or primitive types.  In such cases, 
   * {@link #loadObject(ClassLoader, String, Class[], Object[])} should be used instead.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefFoundError} may occur), reflection must be used to load 
   * the object.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   * @see #loadObject(String, Object[])
   * @see #loadObject(ClassLoader, String, Class[], Object[])
   */
  public static Object loadObject(ClassLoader loader, String className, Object... constructorArgs) 
    throws ReflectException {
    return loadObject(loader, className, getClasses(constructorArgs), constructorArgs);
  }
  
  /**
   * <p>Create an instance of the given class.  This method invokes
   * {@link Class#forName(String, boolean, ClassLoader)} (with {@code initialize} set to {@code true}), 
   * {@link Class#getConstructor(Class[])}, and {@link Constructor#newInstance}.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefFoundError} may occur), reflection must be used to load 
   * the object.</p>
   * 
   * @param loader  A class loader used to load the specified class
   * @param className  The name of the class to be instantiated
   * @param constructorSig  The types of the desired constructor's parameters; these must exactly match the
   *                        the constructor's declared signature (may not be {@code null})
   * @param constructorArgs  The arguments to pass to the constructor ({@code null} is also acceptable where
   *                         there are no parameters)
   *
   * @throws ReflectException  This operation may trigger any of the following, which is wrapped as a
   *                           {@code ReflectException}:<ul>
   *                           <li>A {@link ClassNotFoundException} if {@code className} cannot be found</li>
   *                           <li>A {@link NoSuchMethodException} if no constructor with the given signature 
   *                           exists</li>
   *                           <li>An {@link IllegalArgumentException} if the cardinality and types of
   *                           {@code constructorArgs} are inconsistent with {@code constructorSig}</li>
   *                           <li>An {@link InvocationTargetException} in any throwable is thrown by the
   *                           constructor</li>
   *                            <li>An {@link InstantiationException} if the class is abstract</li>
   *                           <li>An {@link IllegalAccessException} if the constructor is inaccessible</li>
   *                           <li>A {@link SecurityException} if the security manager denies access to the 
   *                           constructor</li>
   *                           
   * @see #loadObject(String, Object[])
   * @see #loadObject(String, Class[], Object[])
   */
  public static Object loadObject(ClassLoader loader, String className, Class<?>[] constructorSig, 
                                  Object... constructorArgs) throws ReflectException {
    try {
      Class<?> c = Class.forName(className, true, loader);
      Constructor<?> k = c.getConstructor(constructorSig);
      return k.newInstance(constructorArgs);
    }
    catch (ClassNotFoundException e) { throw new ClassNotFoundReflectException(e); }
    catch (NoSuchMethodException e) { throw new NoSuchMethodReflectException(e); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentReflectException(e); }
    catch (InvocationTargetException e) { throw new InvocationTargetReflectException(e); }
    catch (InstantiationException e) { throw new InstantiationReflectException(e); }
    catch (IllegalAccessException e) { throw new IllegalAccessReflectException(e); }
    catch (SecurityException e) { throw new SecurityReflectException(e); }
  }
  
  public static Object getStaticField(String className, String fieldName) throws ReflectException {
    return getStaticField(CURRENT_LOADER, className, fieldName);
  }
  
  public static Object getStaticField(ClassLoader loader, String className, String fieldName) throws ReflectException {
    try {
      Class<?> c = Class.forName(className, true, loader);
      Field f = c.getField(fieldName);
      try { return f.get(null); }
      // catch null pointer here to prevent silly null pointers (e.g. fieldName is null) from being caught
      catch (NullPointerException e) { throw new NullPointerReflectException(e); }
    }
    catch (ClassNotFoundException e) { throw new ClassNotFoundReflectException(e); }
    catch (NoSuchFieldException e) { throw new NoSuchFieldReflectException(e); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentReflectException(e); }
    catch (IllegalAccessException e) { throw new IllegalAccessReflectException(e); }
    catch (SecurityException e) { throw new SecurityReflectException(e); }
  }
  
  public static Object invokeStaticMethod(String className, String methodName, Object... args) 
    throws ReflectException {
    return invokeStaticMethod(CURRENT_LOADER, className, methodName, getClasses(args), args);
  }
  
  public static Object invokeStaticMethod(ClassLoader loader, String className, String methodName, Object... args) 
    throws ReflectException {
    return invokeStaticMethod(loader, className, methodName, getClasses(args), args);
  }
  
  public static Object invokeStaticMethod(String className, String methodName, Class<?>[] signature, Object... args) 
    throws ReflectException {
    return invokeStaticMethod(CURRENT_LOADER, className, methodName, signature, args);
  }
  
  public static Object invokeStaticMethod(ClassLoader loader, String className, String methodName, Class<?>[] signature,
                                          Object... args) throws ReflectException {
    try {
      Class<?> c = Class.forName(className, true, loader);
      Method m = c.getMethod(methodName, signature);
      try { return m.invoke(null, args); }
      // catch null pointer here to prevent silly null pointers (e.g. methodName is null) from being caught
      catch (NullPointerException e) { throw new NullPointerReflectException(e); }
    }
    catch (ClassNotFoundException e) { throw new ClassNotFoundReflectException(e); }
    catch (NoSuchMethodException e) { throw new NoSuchMethodReflectException(e); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentReflectException(e); }
    catch (InvocationTargetException e) { throw new InvocationTargetReflectException(e); }
    catch (IllegalAccessException e) { throw new IllegalAccessReflectException(e); }
    catch (SecurityException e) { throw new SecurityReflectException(e); }
  }
  
  
  /**
   * <p>Create an adapter object which provides an interface to a library that is not available statically on the
   * class path (or, more generally, to the {@code baseLoader}).  This assumes the relevant code may be organized
   * into three groups: the base classes, which are available to the {@code baseLoader} and make
   * no direct references to the library or adapter classes; the library classes, which are located in the path
   * represented by {@code libraryPath}, and make no direct references to the adapter class; and the adapter
   * class (including its inner classes), which is available to the {@code baseLoader}, and which can 
   * directly refer to both the library and base classes.</p>
   * 
   * <p>This is a convenience method that uses the class loader of {@code ReflectUtil} as {@code baseLoader}, and 
   * that infers the constructor signature from the given arguments.  Note that the inference process for the 
   * constructor signature is potentially error-prone, because it uses the concrete runtime types of the objects, 
   * which may be more specific than what is needed.  It is impossible, for example, to use this method to invoke a 
   * constructor whose parameters include interface, abstract class, or primitive types.  In such cases, 
   * {@link #loadLibraryAdapter(Iterable, String, Class[], Object[])} should be used instead.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   */
  public static Object loadLibraryAdapter(Iterable<? extends File> libraryPath, String adapterName, 
                                          Object... constructorArgs) throws ReflectException {
    return loadLibraryAdapter(CURRENT_LOADER, libraryPath, adapterName, getClasses(constructorArgs), constructorArgs);
  }

  /**
   * <p>Create an adapter object which provides an interface to a library that is not available statically on the
   * class path (or, more generally, to the {@code baseLoader}).  This assumes the relevant code may be organized
   * into three groups: the base classes, which are available to the {@code baseLoader} and make
   * no direct references to the library or adapter classes; the library classes, which are located in the path
   * represented by {@code libraryPath}, and make no direct references to the adapter class; and the adapter
   * class (including its inner classes), which is available to the {@code baseLoader}, and which can 
   * directly refer to both the library and base classes.</p>
   * 
   * <p>This is a convenience method that uses the class loader of {@code ReflectUtil} to load {@code className}.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   */
  public static Object loadLibraryAdapter(Iterable<? extends File> libraryPath, String adapterName, 
                                          Class<?>[] constructorSig,Object... constructorArgs) throws ReflectException {
    return loadLibraryAdapter(CURRENT_LOADER, libraryPath, adapterName, constructorSig, constructorArgs);
  }

  /**
   * <p>Create an adapter object which provides an interface to a library that is not available statically on the
   * class path (or, more generally, to the {@code baseLoader}).  This assumes the relevant code may be organized
   * into three groups: the base classes, which are available to the {@code baseLoader} and make
   * no direct references to the library or adapter classes; the library classes, which are located in the path
   * represented by {@code libraryPath}, and make no direct references to the adapter class; and the adapter
   * class (including its inner classes), which is available to the {@code baseLoader}, and which can 
   * directly refer to both the library and base classes.</p>
   * 
   * <p>This is a convenience method that infers the constructor signature from the given arguments.  Note that the 
   * inference process for the constructor signature is potentially error-prone, because it uses the concrete runtime 
   * types of the objects, which may be more specific than what is needed.  It is impossible, for example, to use this 
   * method to invoke a constructor whose parameters include interface, abstract class, or primitive types.  In such 
   * cases, {@link #loadLibraryAdapter(ClassLoader, Iterable, String, Class[], Object[])} should be used instead.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   */
  public static Object loadLibraryAdapter(ClassLoader baseLoader, Iterable<? extends File> libraryPath, 
                                          String adapterName, Object... constructorArgs) throws ReflectException {
    return loadLibraryAdapter(baseLoader, libraryPath, adapterName, getClasses(constructorArgs), constructorArgs);
  }

  /** <p>Create an adapter object which provides an interface to a library that is not available statically on the
    * class path (or, more generally, to the {@code baseLoader}).  This assumes the relevant code may be organized
    * into three groups: the base classes, which are available to the {@code baseLoader} and make
    * no direct references to the library or adapter classes; the library classes, which are located in the path
    * represented by {@code libraryPath}, and make no direct references to the adapter class; and the adapter
    * class (including its inner classes), which is available to the {@code baseLoader}, and which can 
    * directly refer to both the library and base classes.</p>
    * 
    * <p>This method constructs an appropriate class loader and then invokes 
    * {@link #loadObject(ClassLoader, String, Class[], Object[])}.</p>
    * 
    * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
    * 
    */
  public static Object loadLibraryAdapter(ClassLoader baseLoader, Iterable<? extends File> libraryPath, 
                                          String adapterName, Class<?>[] constructorSig, Object... constructorArgs) 
    throws ReflectException {
    ClassLoader libraryLoader = new PathClassLoader(baseLoader, libraryPath);
    ClassLoader adapterLoader = new PreemptingClassLoader(libraryLoader, adapterName);
    return loadObject(adapterLoader, adapterName, constructorSig, constructorArgs);
  }
  
  /**
   * Combine two class loaders by first matching classes in {@code first}, then delegating to {@code second}.
   */
  public static ComposedClassLoader mergeLoaders(ClassLoader first, ClassLoader second) {
    return new ComposedClassLoader(first, second);
  }
  
  /**
   * Combine two class loaders by matching specific classes (or class prefixes) from {@code first}, and delegating
   * all other searches to {@code second}.  Bootstrap classes will not be filtered from {@code first}.
   */
  public static ComposedClassLoader mergeLoaders(ClassLoader first, ClassLoader second, String... firstIncludes) {
    return mergeLoaders(first, second, false, firstIncludes);
  }
  
  /**
   * Combine two class loaders by matching a subset of those in {@code first}, followed by a search in {@code second}.
   * The nature of the subset is defined by parameters {@code blackList} and {@code firstPrefixes}.  Bootstrap classes
   * will not be filtered from {@code first}.
   * @param blackList  Whether classes matching {@code firstPrefixes} should be shadowed in {@code first}; otherwise,
   *                   all classes <em>except</em> those that match will be shadowed.
   * @param firstPrefixes  Class or package prefix to match in determining which classes or {@code first} are shadowed
   */
  public static ComposedClassLoader mergeLoaders(ClassLoader first, ClassLoader second, boolean blackList,
                                                 String... firstPrefixes) {
    ClassLoader filteredFirst = new ShadowingClassLoader(first, blackList, IterUtil.asIterable(firstPrefixes), false);
    return new ComposedClassLoader(filteredFirst, second);
  }
  
  /**
   * Wrap a static field in a box.  The field will be accessed via reflection, and any resulting errors will be
   * thrown as {@link ReflectException}s nested in {@link WrappedException}s by the box's {@code value()} and
   * {@code set()} methods.  If the field is known statically, and performance is important, consider defining a 
   * custom box instead.
   */
  public static <T> Box<T> staticFieldAsBox(Class<?> c, String fieldName, Class<T> fieldType) {
    return new FieldBox<T>(c, fieldName, fieldType, null);
  }
  
  /**
   * Wrap a field of the given object in a box.  The field will be accessed via reflection, and any resulting errors 
   * will be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the box's {@code value()} and
   * {@code set()} methods.  If the field is known statically, and performance is important, consider defining a 
   * custom box instead.
   */
  public static <T> Box<T> fieldAsBox(Object object, String fieldName, Class<T> fieldType) {
    return new FieldBox<T>(object.getClass(), fieldName, fieldType, object);
  }
  
  private static final class FieldBox<T> implements Box<T>, Serializable {
    private final Class<?> _objClass;
    private final String _name;
    private final Class<T> _type;
    private final Object _obj;
    private transient Field _field; // optimization; transient because Field isn't serializable
    
    public FieldBox(Class<?> objClass, String name, Class<T> type, Object obj) {
      _objClass = objClass;
      _name = name;
      _type = type;
      _obj = obj;
      _field = null;
    }
    
    public T value() {
      try {
        if (_field == null) { _field = _objClass.getField(_name); }
        try { return cast(_type, _field.get(_obj)); }
        // catch null pointer here to prevent silly null pointers (e.g. _name is null) from being caught
        catch (NullPointerException e) { throw new WrappedException(new NullPointerReflectException(e)); }
      }
      catch (NoSuchFieldException e) { throw new WrappedException(new NoSuchFieldReflectException(e)); }
      catch (IllegalArgumentException e) { throw new WrappedException(new IllegalArgumentReflectException(e)); }
      catch (IllegalAccessException e) { throw new WrappedException(new IllegalAccessReflectException(e)); }
      catch (ClassCastException e) { throw new WrappedException(new ClassCastReflectException(e)); }
      catch (SecurityException e) { throw new WrappedException(new SecurityReflectException(e)); }
    }
    
    public void set(T value) {
      try {
        if (_field == null) { _field = _objClass.getField(_name); }
        _field.set(_obj, value);
      }
      catch (NoSuchFieldException e) { throw new WrappedException(new NoSuchFieldReflectException(e)); }
      catch (IllegalArgumentException e) { throw new WrappedException(new IllegalArgumentReflectException(e)); }
      catch (IllegalAccessException e) { throw new WrappedException(new IllegalAccessReflectException(e)); }
      catch (SecurityException e) { throw new WrappedException(new SecurityReflectException(e)); }
    }
  }
      

  /**
   * Wrap a static method in a thunk.  The method will be invoked via reflection, and any resulting errors will be
   * thrown as {@link ReflectException}s nested in {@link WrappedException}s by the thunk's {@code value()} method.
   * If the method is known statically, and performance is important, consider defining a custom thunk instead.
   */
  public static <O, R> Thunk<R> staticMethodAsThunk(Class<? super O> c, String methodName, Class<? extends R> retT) {
    return LambdaUtil.bindFirst(new MethodLambda<O, R>(c, methodName, retT), null);
  }
  
  /**
   * Wrap a method of the given object in a thunk.  The method will be invoked via reflection, and any resulting 
   * errors will be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the thunk's 
   * {@code value()} method.  If the method is known statically, and performance is important, consider defining 
   * a custom thunk instead.
   */
  public static <R> Thunk<R> methodAsThunk(Object object, String methodName, Class<? extends R> retT) {
    return new ObjectMethodThunk<R>(object, methodName, retT);
  }
  
  /**
   * Wrap a method in a lambda, with the receiver object as the first argument.  The method will be invoked via 
   * reflection, and any resulting errors will be thrown as {@link ReflectException}s nested in 
   * {@link WrappedException}s by the lambda's {@code value()} method.  If the method is known statically, and 
   * performance is important, consider defining a custom lambda instead.
   */
  public static <O, R> Lambda<O, R> methodAsLambda(Class<? super O> c, String methodName, Class<? extends R> retT) {
    return new MethodLambda<O, R>(c, methodName, retT);
  }
  
  /**
   * Wrap a static method in a lambda.  The method will be invoked via reflection, and any resulting errors will be
   * thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} method.
   * If the method is known statically, and performance is important, consider defining a custom lambda instead.
   */
  public static <O, T, R> Lambda<T, R> staticMethodAsLambda(Class<? super O> c, String methodName, 
                                                            Class<? super T> argT, Class<? extends R> retT) {
    return LambdaUtil.bindFirst(new MethodLambda2<O, T, R>(c, methodName, argT, retT), null);
  }
  
  /**
   * Wrap a method of the given object in a lambda.  The method will be invoked via reflection, and any resulting 
   * errors will be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's 
   * {@code value()} method.  If the method is known statically, and performance is important, consider defining 
   * a custom lambda instead.
   */
  public static <T, R> Lambda<T, R> methodAsLambda(Object object, String methodName, Class<? super T> argT,
                                                   Class<? extends R> retT) {
    return new ObjectMethodLambda<T, R>(object, methodName, argT, retT);
  }
  
  /**
   * Wrap a method in a lambda, with the receiver object as the first argument.  The method will be invoked via 
   * reflection, and any resulting errors will be thrown as {@link ReflectException}s nested in 
   * {@link WrappedException}s by the lambda's {@code value()} method.  If the method is known statically, and 
   * performance is important, consider defining a custom lambda instead.
   */
  public static <O, T, R> Lambda2<O, T, R> methodAsLambda2(Class<? super O> c, String methodName, 
                                                           Class<? super T> argT, Class<? extends R> retT) {
    return new MethodLambda2<O, T, R>(c, methodName, argT, retT);
  }
  
  /**
   * Wrap a static method in a lambda.  The method will be invoked via reflection, and any resulting errors will be
   * thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} method.
   * If the method is known statically, and performance is important, consider defining a custom lambda instead.
   */
  public static <O, T1, T2, R> 
    Lambda2<T1, T2, R> staticMethodAsLambda2(Class<? super O> c, String methodName, Class<? super T1> arg1T, 
                                             Class<? super T2> arg2T, Class<? extends R> retT) {
    return LambdaUtil.bindFirst(new MethodLambda3<O, T1, T2, R>(c, methodName, arg1T, arg2T, retT), null);
  }
  
  /**
   * Wrap a method of the given object in a lambda.  The method will be invoked via reflection, and any resulting 
   * errors will be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's 
   * {@code value()} method.  If the method is known statically, and performance is important, consider defining 
   * a custom lambda instead.
   */
  public static <T1, T2, R>
    Lambda2<T1, T2, R> methodAsLambda2(Object object, String methodName, Class<? super T1> arg1T, 
                                       Class<? super T2> arg2T, Class<? extends R> retT) {
    return new ObjectMethodLambda2<T1, T2, R>(object, methodName, arg1T, arg2T, retT);
  }
  
  /**
   * Wrap a method in a lambda, with the receiver object as the first argument.  The method will be invoked via 
   * reflection, and any resulting errors will be thrown as {@link ReflectException}s nested in 
   * {@link WrappedException}s by the lambda's {@code value()} method.  If the method is known statically, and 
   * performance is important, consider defining a custom lambda instead.
   */
  public static <O, T1, T2, R>
    Lambda3<O, T1, T2, R> methodAsLambda3(Class<? super O> c, String methodName, Class<? super T1> arg1T,
                                          Class<? super T2> arg2T, Class<? extends R> retT) {
    return new MethodLambda3<O, T1, T2, R>(c, methodName, arg1T, arg2T, retT);
  }
  
  /**
   * Wrap a static method in a lambda.  The method will be invoked via reflection, and any resulting errors will be
   * thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} method.
   * If the method is known statically, and performance is important, consider defining a custom lambda instead.
   */
  public static <O, T1, T2, T3, R> 
    Lambda3<T1, T2, T3, R> staticMethodAsLambda3(Class<? super O> c, String methodName, Class<? super T1> arg1T, 
                                                 Class<? super T2> arg2T, Class<? super T3> arg3T,
                                                 Class<? extends R> retT) {
    return LambdaUtil.bindFirst(new MethodLambda4<O, T1, T2, T3, R>(c, methodName, arg1T, arg2T, arg3T, retT), null);
  }
  
  /**
   * Wrap a method of the given object in a lambda.  The method will be invoked via reflection, and any resulting 
   * errors will be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's 
   * {@code value()} method.  If the method is known statically, and performance is important, consider defining 
   * a custom lambda instead.
   */
  public static <T1, T2, T3, R>
    Lambda3<T1, T2, T3, R> methodAsLambda3(Object object, String methodName, Class<? super T1> arg1T, 
                                           Class<? super T2> arg2T, Class<? super T3> arg3T, Class<? extends R> retT) {
    return new ObjectMethodLambda3<T1, T2, T3, R>(object, methodName, arg1T, arg2T, arg3T, retT);
  }
  
  /**
   * Wrap a method in a lambda, with the receiver object as the first argument.  The method will be invoked via 
   * reflection, and any resulting errors will be thrown as {@link ReflectException}s nested in 
   * {@link WrappedException}s by the lambda's {@code value()} method.  If the method is known statically, and 
   * performance is important, consider defining a custom lambda instead.
   */
  public static <O, T1, T2, T3, R>
    Lambda4<O, T1, T2, T3, R> methodAsLambda4(Class<? super O> c, String methodName, Class<? super T1> arg1T,
                                              Class<? super T2> arg2T, Class<? super T3> arg3T, 
                                              Class<? extends R> retT) {
    return new MethodLambda4<O, T1, T2, T3, R>(c, methodName, arg1T, arg2T, arg3T, retT);
  }
  
  
  /** Common code for method-wrapping lambdas. */
  private static abstract class MethodWrapper<R> implements Serializable {
    private final Class<?> _objClass;
    private final String _name;
    private final Class<? extends R> _returnType;
    private final Class<?>[] _signature;
    private transient Method _method; // optimization; transient because Method isn't serializable
    
    protected MethodWrapper(Class<?> objClass, String name, Class<? extends R> returnType, Class<?>... signature) {
      _objClass = objClass;
      _name = name;
      _returnType = returnType;
      _signature = signature;
      _method = null;
    }
    
    /** Invoke the method with the given arguments.  Throws a wrapped ReflectException. */
    protected R invoke(Object obj, Object... args) {
      try {
        if (_method == null) { _method = _objClass.getMethod(_name, _signature); }
        try { return cast(_returnType, _method.invoke(obj, args)); }
        // catch null pointer here to prevent silly null pointers (e.g. _name is null) from being caught
        catch (NullPointerException e) { throw new WrappedException(new NullPointerReflectException(e)); }
      }
      catch (NoSuchMethodException e) { throw new WrappedException(new NoSuchMethodReflectException(e)); }
      catch (IllegalArgumentException e) { throw new WrappedException(new IllegalArgumentReflectException(e)); }
      catch (InvocationTargetException e) { throw new WrappedException(new InvocationTargetReflectException(e)); }
      catch (IllegalAccessException e) { throw new WrappedException(new IllegalAccessReflectException(e)); }
      catch (ClassCastException e) { throw new WrappedException(new ClassCastReflectException(e)); }
      catch (SecurityException e) { throw new WrappedException(new SecurityReflectException(e)); }
    }
  }

  private static final class ObjectMethodThunk<R> extends MethodWrapper<R> implements Thunk<R> {
    private final Object _obj;
    public ObjectMethodThunk(Object obj, String name, Class<? extends R> returnT) {
      super(obj.getClass(), name, returnT); _obj = obj;
    }
    public R value() { return invoke(_obj); }
  }
  
  private static final class MethodLambda<O, R> extends MethodWrapper<R> implements Lambda<O, R> {
    public MethodLambda(Class<? super O> objT, String name, Class<? extends R> returnT) {
      super(objT, name, returnT);
    }
    public R value(O obj) { return invoke(obj); }
  }
  
  private static final class ObjectMethodLambda<T, R> extends MethodWrapper<R> implements Lambda<T, R> {
    private final Object _obj;
    public ObjectMethodLambda(Object obj, String name, Class<? super T> argT, Class<? extends R> returnT) {
      super(obj.getClass(), name, returnT, argT); _obj = obj;
    }
    public R value(T arg) { return invoke(_obj, arg); }
  }
  
  private static final class MethodLambda2<O, T, R> extends MethodWrapper<R> implements Lambda2<O, T, R> {
    public MethodLambda2(Class<? super O> objT, String name, Class<? super T> argT, Class<? extends R> returnT) {
      super(objT, name, returnT, argT);
    }
    public R value(O obj, T arg) { return invoke(obj, arg); }
  }
  
  private static final class ObjectMethodLambda2<T1, T2, R> extends MethodWrapper<R> implements Lambda2<T1, T2, R> {
    private final Object _obj;
    public ObjectMethodLambda2(Object obj, String name, Class<? super T1> arg1T, Class<? super T2> arg2T,
                               Class<? extends R> returnT) {
      super(obj.getClass(), name, returnT, arg1T, arg2T); _obj = obj;
    }
    public R value(T1 arg1, T2 arg2) { return invoke(_obj, arg1, arg2); }
  }
  
  private static final class MethodLambda3<O, T1, T2, R> extends MethodWrapper<R> implements Lambda3<O, T1, T2, R> {
    public MethodLambda3(Class<? super O> objT, String name, Class<? super T1> arg1T, Class<? super T2> arg2T,
                         Class<? extends R> returnT) {
      super(objT, name, returnT, arg1T, arg2T);
    }
    public R value(O obj, T1 arg1, T2 arg2) { return invoke(obj, arg1, arg2); }
  }
  
  private static final class ObjectMethodLambda3<T1, T2, T3, R> extends MethodWrapper<R> 
    implements Lambda3<T1, T2, T3, R> {
    private final Object _obj;
    public ObjectMethodLambda3(Object obj, String name, Class<? super T1> arg1T, Class<? super T2> arg2T,
                               Class<? super T3> arg3T, Class<? extends R> returnT) {
      super(obj.getClass(), name, returnT, arg1T, arg2T, arg3T); _obj = obj;
    }
    public R value(T1 arg1, T2 arg2, T3 arg3) { return invoke(_obj, arg1, arg2, arg3); }
  }
  
  private static final class MethodLambda4<O, T1, T2, T3, R> extends MethodWrapper<R> 
    implements Lambda4<O, T1, T2, T3, R> {
    public MethodLambda4(Class<? super O> objT, String name, Class<? super T1> arg1T, Class<? super T2> arg2T,
                         Class<? super T3> arg3T, Class<? extends R> returnT) {
      super(objT, name, returnT, arg1T, arg2T, arg3T);
    }
    public R value(O obj, T1 arg1, T2 arg2, T3 arg3) { return invoke(obj, arg1, arg2, arg3); }
  }
  
  
  /**
   * Wrap a constructor in a thunk.  The constructor will be invoked via reflection, and any resulting errors will 
   * be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the thunk's {@code value()} 
   * method.  If the constructor is known statically, and performance is important, consider defining a custom 
   * thunk instead.
   */
  public static <R> Thunk<R> constructorAsThunk(Class<? extends R> c) {
    return new ConstructorThunk<R>(c);
  }
  
  /**
   * Wrap a constructor in a lambda.  The constructor will be invoked via reflection, and any resulting errors will 
   * be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} 
   * method.  If the constructor is known statically, and performance is important, consider defining a custom 
   * lambda instead.
   */
  public static <T, R> Lambda<T, R> constructorAsLambda(Class<? extends R> c, Class<? super T> argT) {
    return new ConstructorLambda<T, R>(c, argT);
  }
  
  /**
   * Wrap a constructor in a lambda.  The constructor will be invoked via reflection, and any resulting errors will 
   * be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} 
   * method.  If the constructor is known statically, and performance is important, consider defining a custom 
   * lambda instead.
   */
  public static <T1, T2, R> Lambda2<T1, T2, R> constructorAsLambda2(Class<? extends R> c, Class<? super T1> arg1T, 
                                                                    Class<? super T2> arg2T) {
    return new ConstructorLambda2<T1, T2, R>(c, arg1T, arg2T);
  }
  
  /**
   * Wrap a constructor in a lambda.  The constructor will be invoked via reflection, and any resulting errors will 
   * be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} 
   * method.  If the constructor is known statically, and performance is important, consider defining a custom 
   * lambda instead.
   */
  public static <T1, T2, T3, R> Lambda3<T1, T2, T3, R>
    constructorAsLambda3(Class<? extends R> c, Class<? super T1> arg1T, Class<? super T2> arg2T, 
                         Class<? super T3> arg3T) {
    return new ConstructorLambda3<T1, T2, T3, R>(c, arg1T, arg2T, arg3T);
  }
  
  /**
   * Wrap a constructor in a lambda.  The constructor will be invoked via reflection, and any resulting errors will 
   * be thrown as {@link ReflectException}s nested in {@link WrappedException}s by the lambda's {@code value()} 
   * method.  If the constructor is known statically, and performance is important, consider defining a custom 
   * lambda instead.
   */
  public static <T1, T2, T3, T4, R> Lambda4<T1, T2, T3, T4, R>
    constructorAsLambda4(Class<? extends R> c, Class<? super T1> arg1T, Class<? super T2> arg2T, 
                         Class<? super T3> arg3T, Class<? super T4> arg4T) {
    return new ConstructorLambda4<T1, T2, T3, T4, R>(c, arg1T, arg2T, arg3T, arg4T);
  }
  
  
  /** Common code for constructor-wrapping lambdas. */
  private static abstract class ConstructorWrapper<R> implements Serializable {
    private final Class<? extends R> _c;
    private final Class<?>[] _signature;
    private transient Constructor<? extends R> _k; // optimization; transient because Constructor isn't serializable
    
    protected ConstructorWrapper(Class<? extends R> c, Class<?>... signature) {
      _c = c;
      _signature = signature;
      _k = null;
    }
    
    /** Invoke the constructor with the given arguments.  Throws a wrapped ReflectException. */
    protected R invoke(Object... args) {
      try {
        if (_k == null) { _k = _c.getConstructor(_signature); }
        return _k.newInstance(args);
      }
      catch (NoSuchMethodException e) { throw new WrappedException(new NoSuchMethodReflectException(e)); }
      catch (IllegalArgumentException e) { throw new WrappedException(new IllegalArgumentReflectException(e)); }
      catch (InvocationTargetException e) { throw new WrappedException(new InvocationTargetReflectException(e)); }
      catch (IllegalAccessException e) { throw new WrappedException(new IllegalAccessReflectException(e)); }
      catch (InstantiationException e) { throw new WrappedException(new InstantiationReflectException(e)); }
      catch (SecurityException e) { throw new WrappedException(new SecurityReflectException(e)); }
    }
  }

  private static final class ConstructorThunk<R> extends ConstructorWrapper<R> implements Thunk<R> {
    public ConstructorThunk(Class<? extends R> c) { super(c); }
    public R value() { return invoke(); }
  }
  
  private static final class ConstructorLambda<T, R> extends ConstructorWrapper<R> implements Lambda<T, R> {
    public ConstructorLambda(Class<? extends R> c, Class<? super T> argT) { super(c, argT); }
    public R value(T arg) { return invoke(arg); }
  }
  
  private static final class ConstructorLambda2<T1, T2, R> extends ConstructorWrapper<R>
    implements Lambda2<T1, T2, R> {
    public ConstructorLambda2(Class<? extends R> c, Class<? super T1> arg1T, Class<? super T2> arg2T) {
      super(c, arg1T, arg2T);
    }
    public R value(T1 arg1, T2 arg2) { return invoke(arg1, arg2); }
  }
  
  private static final class ConstructorLambda3<T1, T2, T3, R> extends ConstructorWrapper<R>
    implements Lambda3<T1, T2, T3, R> {
    public ConstructorLambda3(Class<? extends R> c, Class<? super T1> arg1T, Class<? super T2> arg2T,
                              Class<? super T3> arg3T) {
      super(c, arg1T, arg2T, arg3T);
    }
    public R value(T1 arg1, T2 arg2, T3 arg3) { return invoke(arg1, arg2, arg3); }
  }
  
  private static final class ConstructorLambda4<T1, T2, T3, T4, R> extends ConstructorWrapper<R>
    implements Lambda4<T1, T2, T3, T4, R> {
    public ConstructorLambda4(Class<? extends R> c, Class<? super T1> arg1T, Class<? super T2> arg2T,
                              Class<? super T3> arg3T, Class<? super T4> arg4T) {
      super(c, arg1T, arg2T, arg3T, arg4T);
    }
    public R value(T1 arg1, T2 arg2, T3 arg3, T4 arg4) { return invoke(arg1, arg2, arg3, arg4); }
  }
}
