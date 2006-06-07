/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.util.LinkedList;
import java.lang.reflect.Field;

import edu.rice.cs.util.Log;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/** Registry for all CompilerInterface implementations. Allows registration, by class name, of {@link CompilerInterface}
 *  implementations. Later, the list of these registered compilers (but only those that successfully loaded) can be 
 *  retrieved.
 *  @version $Id$
 */
public class CompilerRegistry {
  
  /* classes that load and adapt various compilers */
  
  public static final String[] JAVA_16_COMPILERS = {
    // javac 1.6 "compiler interfaces"
    "edu.rice.cs.drjava.model.compiler.Javac160FromSetLocation",
    "edu.rice.cs.drjava.model.compiler.Javac160FromClasspath",
    "edu.rice.cs.drjava.model.compiler.Javac160FromToolsJar"
  };
  
  public static final String[] JAVA_15_COMPILERS = {
    // javac 1.5 "compiler interfaces"
    "edu.rice.cs.drjava.model.compiler.Javac150FromSetLocation",
    "edu.rice.cs.drjava.model.compiler.Javac150FromClasspath",
    "edu.rice.cs.drjava.model.compiler.Javac150FromToolsJar"
  };

  /** A subset of DEFAULT_COMPILERS that support Raw (non-generic) Java. */
  public static final String[] JAVA_14_COMPILERS = {
    // javac 1.4 "compiler interfaces"
    "edu.rice.cs.drjava.model.compiler.Javac141FromSetLocation",
    "edu.rice.cs.drjava.model.compiler.Javac141FromClasspath",
    "edu.rice.cs.drjava.model.compiler.Javac141FromToolsJar"
  };

  /** The list of compiler loading classes that are distributed with DrJava. */
  static final String[][] DEFAULT_COMPILERS = {
    // javac 1.6 
    JAVA_16_COMPILERS,
    // javac 1.5 
    JAVA_15_COMPILERS,
    // javac 1.4
    JAVA_14_COMPILERS
  };
    
  /** Singleton instance. */
  public static final CompilerRegistry ONLY = new CompilerRegistry();
  
  private final static Log _log = new Log("Compiler.txt", false);

  /** Class loader to use to fetch compiler classes. */
  private volatile ClassLoader _baseClassLoader;
  
  /** The candidate compilers give the version of Java being executed */
  private final String[] _candidateCompilers;

  /** The active compiler. Must never be null. */
  private CompilerInterface _activeCompiler = NoCompilerAvailable.ONLY;

  /** Private constructor due to singleton. */
  private CompilerRegistry() { 
    _baseClassLoader = getClass().getClassLoader(); 
    
    String version = CompilerProxy.VERSION; // version of executing JVM: 1.4, 1.5, 1.6
    
    if (version.equals("1.4")) _candidateCompilers = JAVA_14_COMPILERS;
    else if (version.equals("1.5")) _candidateCompilers = JAVA_15_COMPILERS;
    else if (version.equals("1.6")) _candidateCompilers = JAVA_16_COMPILERS;
    else _candidateCompilers = null;
  }
    
  /** Sets the base class loader used to load compiler classes. */
  public void setBaseClassLoader(ClassLoader l) { _baseClassLoader = l; }

  /**  Gets the base class loader used to load compiler classes. */
  public ClassLoader getBaseClassLoader() { return _baseClassLoader; }

  /** Returns all registered compilers that are actually available. That is, for all elements in the returned array,
   *  .isAvailable() is true. <P> This method will never return null or a zero-length array. Instead, if no compiler
   *  is registered and available, this will return a one-element array containing an instance of {@link 
   *  NoCompilerAvailable}.
   */
  public CompilerInterface[] getAvailableCompilers() {
    LinkedList<CompilerInterface> availableCompilers = new LinkedList<CompilerInterface>();
    
    if (_candidateCompilers == null) throw new 
      UnexpectedException("Java specification version " + CompilerProxy.VERSION + "is not supported.  Must be 1.4, 1.5, or 1.6");

    for (String name : _candidateCompilers) {
      _log.log("CompilerRegistry.getAvailableCompilers is checking compiler: " + name);
      try { if (_createCompiler(name, availableCompilers)) continue; }
      catch (Throwable t) {
        // This compiler didn't load. Keep on going.
        _log.log("Compiler " + name + " failed to load:");
        //t.printStackTrace(DrJava.consoleOut());
        //System.err.println();
      }
    }

    if (availableCompilers.size() == 0) availableCompilers.add(NoCompilerAvailable.ONLY);
    
   _log.log("CompilerRegistry.getAvailableCompilers() returning " + availableCompilers);
    
    return availableCompilers.toArray(new CompilerInterface[availableCompilers.size()]);
  }

  private boolean _createCompiler(String name, LinkedList<CompilerInterface> availableCompilers) throws Throwable {
    _log.log("CompilerRegistry._createCompiler(" + name + ", " + availableCompilers +") called");
    CompilerInterface compiler = _instantiateCompiler(name);
    if (compiler.isAvailable()) {
      _log.log("Compiler " + this + " is available: added to compile list");

      // can't use getActiveCompiler() because it will call back to
      // getAvailableCompilers, forming an infinite recursion!!
      if (_activeCompiler == NoCompilerAvailable.ONLY) {
        //System.err.println("\tset to active.");
        _activeCompiler = compiler;
      }
//      Utilities.show("Adding compiler " + compiler);
      availableCompilers.add(compiler);
      return true;
    }
    else {
      _log.log("Compiler " + this + " is NOT available.");
      return false;
    }
  }

  public boolean isNoCompilerAvailable() { return getActiveCompiler() == NoCompilerAvailable.ONLY; }

  /** Sets which compiler is the "active" compiler.
   *  @param compiler Compiler to set active.  Cannot be null.
   *  @throws IllegalArgumentException if compiler is null.
   *
   *  @see #getActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    if (compiler == null) {
      // Can't let active compiler be null
      throw new IllegalArgumentException("Cannot set active compiler to null.");
    }
    else _activeCompiler = compiler;
  }

  /** Gets the compiler is the "active" compiler. If there is no "active" compiler or if the active compiler is
   *  not available, this will return {@link NoCompilerAvailable}.
   *  @see #setActiveCompiler
   */
  public CompilerInterface getActiveCompiler() {
    // If no compiler is available now, try to see if we can get one
    if (_activeCompiler == NoCompilerAvailable.ONLY) getAvailableCompilers();

    //DrJava.consoleErr().println("active compiler: " + _activeCompiler);

    if (_activeCompiler.isAvailable()) return _activeCompiler;
    return NoCompilerAvailable.ONLY;
  }

  /** Instantiate the given compiler.
   *  @param name Full class name of compiler to instantiate. This class must implement {@link CompilerInterface}.
   *  @return Instance of {@link CompilerInterface}. This will either be the value of the .ONLY field of the class
   *    (if it exists and is an implementation of CompilerInterface) or a new instance of the given class.
   *  @throws Throwable If the compiler does not load, some type of exception will be thrown. Which particular one 
   *    depends on how it failed.  It is non-recoverable; the exception is thrown just to indicate failure.
   */
  private CompilerInterface _instantiateCompiler(String name) throws Throwable {
    _log.log("CompilerRegistry._instantiateCompiler using class loader " + _baseClassLoader + " to load " + name);
    Class<?> clazz = _baseClassLoader.loadClass(name);
    _log.log("Loaded compiler named " + name + " with class name " + clazz);
    return createCompiler(clazz);
  }

  public static CompilerInterface createCompiler(Class clazz) throws Throwable {
    try {
      _log.log("CompilerRegistry.createCompiler(" + clazz + ") called");
      Field field = clazz.getField("ONLY");
      _log.log(clazz + ".ONLY = " + field);
      Object val = field.get(null);  // null is passed to get since it's a static field
      _log.log("createCompiler(" + clazz + ") returning " + val);
      return (CompilerInterface) val;
    }
    catch (Throwable t) {
      // Not a compiler provided by ToolsJarClassLoader
      _log.log("createCompiler threw exception " + t);
      return (CompilerInterface) clazz.newInstance();
    }
  }
}
