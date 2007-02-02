package edu.rice.cs.plt.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.File;

import static edu.rice.cs.plt.reflect.ReflectException.*;

public final class ReflectUtil {
  
  /** Prevents instance creation */
  private ReflectUtil() {}
  
  /**
   * Produce the simple name of the given class, as specified by {@link Class#getSimpleName},
   * with an improved scheme for anonymous classes.  The simple name of a class is generally
   * the unqualified name used to declare it.  Arrays evaluate to the simple name of their
   * element type, followed by a pair of brackets.  Anonymous classes, rather than evaluating to an 
   * empty string, produce something like "anonymous Foo" (where Foo is the supertype).
   */
  public static String simpleName(Class<?> c) {
    if (c.isArray()) { return simpleName(c.getComponentType()) + "[]"; }
    else if (isAnonymousClass(c)) {
      if (c.getInterfaces().length > 0) { return "anonymous " + simpleName(c.getInterfaces()[0]); }
      else { return "anonymous " + simpleName(c.getSuperclass()); }
    }
    else {
      String fullName = c.getName();
      return fullName.substring(fullName.lastIndexOf('.') + 1);
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
  
  /** An implementation of {@link Class#cast}, which is unavailable prior to Java 5.0 */
  public static <T> T cast(Class<T> c, Object o) {
    if (c.isInstance(o)) {
      @SuppressWarnings("unchecked") T result = (T) o;
      return result;
    }
    else { throw new ClassCastException(o.getClass().getName()); }
  }
  

  private static final int JAVA_VERSION = parseJavaVersion(System.getProperty("java.class.version"));
  
  /**
   * Convert the given class version string to a number.  The major version is multiplied by 100 and
   * added to the minor version.  If there is an error in parsing, the result is 0.
   */
  private static int parseJavaVersion(String text) {
    StringTokenizer t = new StringTokenizer(text, ".");
    try {
      int result = Integer.parseInt(t.nextToken()) * 100;
      result += Integer.parseInt(t.nextToken());
      return result;
    }
    catch (NoSuchElementException e) { return 0; }
    catch (NumberFormatException e) { return 0; }
  }
  
  /**
   * Tests whether the given version of Java is supported in the currently-running JVM.  Each version 
   * supports all of its predecessors.  Ideally, a {@code true} result means that all APIs associated with  
   * that version are available at runtime.  However, this method does not attempt to (and cannot, in general) 
   * guarantee that the boot class path or Java installation have not been modified to only support certain 
   * API classes, or that certain classes and methods available in the given version have not been removed 
   * (after a few cycles of deprecation, for example) from a newer version's APIs.
   */
  public static boolean isSupported(Version v) {
    return v.supportedUnder(JAVA_VERSION);
  }
  
  /**
   * Tests whether the given version of Java is the version of the currently-running JVM.  Ideally, a 
   * {@code true} result means that all APIs associated with that version are available at runtime.  However, 
   * this method does not attempt to (and cannot, in general) guarantee that the boot class path or Java 
   * installation have not been modified to only support certain API classes.
   */
  public static boolean isCurrent(Version v) {
    return v.supportedUnder(JAVA_VERSION) && v.newUnder(JAVA_VERSION);
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
   * {@link loadObject(String, Class[], Object[])} should be used instead.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefError} may occur), reflection must be used to load the object.</p>
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
   * (otherwise, a {@link NoClassDefError} may occur), reflection must be used to load the object.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   * @see #loadObject(String, Object[])
   * @see #loadObject(ClassLoader, String, Class[], Object[])
   */
  public static Object loadObject(String className, Class[] constructorSig, Object... constructorArgs) 
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
   * {@link loadObject(ClassLoader, String, Class[], Object[])} should be used instead.</p>
   * 
   * <p>A typical use of this method is to instantiate an object that belongs to or directly refers to
   * a library that is not guaranteed to be statically available at runtime.  Since no direct reference
   * can by made to that object's class or any of the library's classes in the main body of code 
   * (otherwise, a {@link NoClassDefError} may occur), reflection must be used to load the object.</p>
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
   * (otherwise, a {@link NoClassDefError} may occur), reflection must be used to load the object.</p>
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
  public static Object loadObject(ClassLoader loader, String className, Class[] constructorSig, 
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
   * that infers the constructor signature from the given arguments.  Note that the inference process for the constructor 
   * signature is potentially error-prone, because it uses the concrete runtime types of the objects, which may be more 
   * specific than what is needed.  It is impossible, for example, to use this method to invoke a constructor whose parameters 
   * include interface, abstract class, or primitive types.  In such cases, 
   * {@link loadLibraryAdapter(Iterable, String, Class[], Object[])} should be used instead.</p>
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
  public static Object loadLibraryAdapter(Iterable<? extends File> libraryPath, String adapterName, Class[] constructorSig,
                                          Object... constructorArgs) throws ReflectException {
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
   * inference process for the constructor signature is potentially error-prone, because it uses the concrete runtime types 
   * of the objects, which may be more specific than what is needed.  It is impossible, for example, to use this method to 
   * invoke a constructor whose parameters include interface, abstract class, or primitive types.  In such cases, 
   * {@link loadLibraryAdapter(ClassLoader, Iterable, String, Class[], Object[])} should be used instead.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   */
  public static Object loadLibraryAdapter(ClassLoader baseLoader, Iterable<? extends File> libraryPath, String adapterName, 
                                          Object... constructorArgs) throws ReflectException {
    return loadLibraryAdapter(baseLoader, libraryPath, adapterName, getClasses(constructorArgs), constructorArgs);
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
   * <p>This method constructs an appropriate class loader and then invokes 
   * {@link #loadObject(ClassLoader, String, Class[], Object[])}.</p>
   * 
   * @throws ReflectException  As specified by {@link #loadObject(ClassLoader, String, Class[], Object[])}
   * 
   */
  public static Object loadLibraryAdapter(ClassLoader baseLoader, Iterable<? extends File> libraryPath, String adapterName, 
                                          Class[] constructorSig, Object... constructorArgs) throws ReflectException {
    ClassLoader libraryLoader = new PathClassLoader(baseLoader, libraryPath);
    ClassLoader adapterLoader = new PreemptingClassLoader(libraryLoader, adapterName);
    return loadObject(adapterLoader, adapterName, constructorSig, constructorArgs);
  }
  
  
  /** A representation of a major Java version.  Used with {@link #isSupported} and {@link #isCurrent}. */
  public enum Version {
    JAVA_1_1 {
      protected boolean supportedUnder(int num) { return num >= 4503; }
      protected boolean newUnder(int num) { return num < 4600; }
    },
    JAVA_1_2 {
      protected boolean supportedUnder(int num) { return num >= 4600; }
      protected boolean newUnder(int num) { return num < 4700; }
    },
    JAVA_1_3 {
      protected boolean supportedUnder(int num) { return num >= 4700; }
      protected boolean newUnder(int num) { return num < 4800; }
    },
    JAVA_1_4 {
      protected boolean supportedUnder(int num) { return num >= 4800; }
      protected boolean newUnder(int num) { return num < 4900; }
    },
    JAVA_5 {
      protected boolean supportedUnder(int num) { return num >= 4900; }
      protected boolean newUnder(int num) { return num < 5000; }
    },
    JAVA_6 {
      protected boolean supportedUnder(int num) { return num >= 5000; }
      protected boolean newUnder(int num) { return num < 5100; }
    };
    
    protected abstract boolean supportedUnder(int num);
    protected abstract boolean newUnder(int num);
  }
  
}
