package edu.rice.cs.drjava.model.compiler;

import java.util.LinkedList;
import java.util.ListIterator;

import java.net.*;
import java.io.*;

import java.lang.reflect.Field;

import edu.rice.cs.drjava.DrJava;

/**
 * Registry for all CompilerInterface implementations.
 * Allows registration, by class name, of {@link CompilerInterface}
 * implementations. Later, the list of these registered compilers (but only
 * those that successfully loaded) can be retrieved.
 *
 * @version $Id$
 */
public class CompilerRegistry {
  /**
   * The list of compiler interfaces that are distributed with DrJava.
   */
  public static final String[] DEFAULT_COMPILERS = {
    "edu.rice.cs.drjava.model.compiler.JavacGJCompiler",
    "edu.rice.cs.drjava.model.compiler.JavacFromToolsJar",
    "edu.rice.cs.drjava.model.compiler.GJv6Compiler"
  };

  /** Singleton instance. */
  public static final CompilerRegistry ONLY = new CompilerRegistry();

  /** Class loader to use to fetch compiler classes. */
  private ClassLoader _baseClassLoader;

  /** Linked list of class names of registered compilers. */
  private LinkedList _registeredCompilers = new LinkedList();

  /** The active compiler. Must never be null. */
  private CompilerInterface _activeCompiler = NoCompilerAvailable.ONLY;

  /** Private constructor due to singleton. */
  private CompilerRegistry() {
    _baseClassLoader = getClass().getClassLoader();
    _registerDefaultCompilers();
  }

  /**
   * Sets the base class loader used to load compiler classes.
   */
  public void setBaseClassLoader(ClassLoader l) {
    _baseClassLoader = l;
  }

  /**
   * Gets the base class loader used to load compiler classes.
   */
  public ClassLoader getBaseClassLoader() {
    return _baseClassLoader;
  }

  /**
   * Register the given compiler, adding it to the list of potential
   * compilers. This function adds the compiler to the list, regardless
   * of whether the compiler is actualy available.
   * This method will not add a duplicate instance of the same compiler.
   *
   * @param name Name of the {@link CompilerInterface} implementation class.
   */
  public void registerCompiler(String name) {
    if (! _registeredCompilers.contains(name) ) {
      _registeredCompilers.add(name);
    }
  }

  /**
   * Returns all registered compilers that are actually available.
   * That is, for all elements in the returned array, .isAvailable()
   * is true.
   * <P>
   * This method will never return null or a zero-length array.
   * Instead, if no compiler is registered and available, this will return
   * a one-element array containing an instance of
   * {@link NoCompilerAvailable}.
   */
  public CompilerInterface[] getAvailableCompilers() {
    LinkedList availableCompilers = new LinkedList();
    ListIterator itor = _registeredCompilers.listIterator();

    while (itor.hasNext()) {
      String name = (String) itor.next();

      try {
        CompilerInterface compiler = _instantiateCompiler(name);
        availableCompilers.add(compiler);
      }
      catch (Throwable t) {
        // This compiler didn't load. Keep on going.
      }
    }

    if (availableCompilers.size() == 0) {
      availableCompilers.add(NoCompilerAvailable.ONLY);
    }

    return (CompilerInterface[])
           availableCompilers.toArray(new CompilerInterface[0]);
  }

  /**
   * Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   *
   * @see #getActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    _activeCompiler = compiler;
  }

  /**
   * Gets the compiler is the "active" compiler.
   * If there is no "active" compiler or if the active compiler is
   * not available, this will return {@link NoCompilerAvailable}.
   *
   * @see #setActiveCompiler
   */
  public CompilerInterface getActiveCompiler() {
    // If no compiler is available now, try to see if we can get one
    if (_activeCompiler == NoCompilerAvailable.ONLY) {
      _setFirstAvailableCompilerActive();
    }

    //DrJava.consoleErr().println("active compiler: " + _activeCompiler);

    if (_activeCompiler.isAvailable()) {
      return _activeCompiler;
    }
    else {
      return NoCompilerAvailable.ONLY;
    }
  }

  private void _registerDefaultCompilers() {
    for (int i = 0; i < DEFAULT_COMPILERS.length; i++) {
      registerCompiler(DEFAULT_COMPILERS[i]);
    }
  }

  /**
   * Sets as active the first compiler that's available.
   * If no compiler is available, the active compiler will not be changed.
   */
  private void _setFirstAvailableCompilerActive() {
    ListIterator itor = _registeredCompilers.listIterator();

    while (itor.hasNext()) {
      String current = (String) itor.next();

      try {
        CompilerInterface compiler = _instantiateCompiler(current);
        _activeCompiler = compiler;
        return;
      }
      catch (Throwable t) {
        // This compiler didn't load. Keep on going.
      }
    }
  }

  /**
   * Instantiate the given compiler.
   *
   * @param name Fully qualified class name of the compiler to instantiate.
   *             This class must implement {@link CompilerInterface}.
   *
   * @return Instance of {@link CompilerInterface}. This will either be
   *         the value of the .ONLY field of the class (if it exists and
   *         is an implementation of CompilerInterface) or a new instance
   *         of the given class.
   *
   * @throws Throwable If the compiler would not load, some type of exception
   *                   will be thrown. Which particular one depends on how
   *                   it failed. But either way, it is non-recoverable;
   *                   the exception is thrown just to indicate failure.
   */
  private CompilerInterface _instantiateCompiler(String name) throws Throwable {
    Class clazz = _baseClassLoader.loadClass(name);
    return createCompiler(clazz);
  }

  public static CompilerInterface createCompiler(Class clazz) throws Throwable {
    try {
      Field field = clazz.getField("ONLY");
      // null is passed to get since it's a static field
      Object val = field.get(null);

      return (CompilerInterface) val;
    }
    catch (Throwable t) {
      return (CompilerInterface) clazz.newInstance();
    }
  }

  /** Returns reasonable location guesses for tools jar file. */
  private static URL[] _getToolsJarURLs() {
    File home = new File(System.getProperty("java.home"));
    File libDir = new File(home, "lib");
    File libDir2 = new File(home.getParentFile(), "lib");

    try {
      return new URL[] {
        new File(libDir, "tools.jar").toURL(),
        new File(libDir2, "tools.jar").toURL()
      };
    }
    catch (MalformedURLException e) {
      return new URL[0];
    }
  }
}
