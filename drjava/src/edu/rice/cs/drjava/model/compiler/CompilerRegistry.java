/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

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
    "edu.rice.cs.drjava.model.compiler.JSR14FromSetLocation",
    "edu.rice.cs.drjava.model.compiler.JavacFromSetLocation",
    "edu.rice.cs.drjava.model.compiler.JavacFromClasspath",
    "edu.rice.cs.drjava.model.compiler.JavacFromToolsJar",
    "edu.rice.cs.drjava.model.compiler.GJv6FromClasspath",
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
        if (compiler.isAvailable()) {
          availableCompilers.add(compiler);
        }
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

  public boolean isNoCompilerAvailable() {
    return getActiveCompiler() == NoCompilerAvailable.ONLY;
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
