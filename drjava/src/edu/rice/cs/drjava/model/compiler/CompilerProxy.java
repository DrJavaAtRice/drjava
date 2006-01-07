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
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.classloader.StickyClassLoader;
import edu.rice.cs.util.ClasspathVector;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;

/** A compiler interface to search a given
 *  @version $Id$
 */
public class CompilerProxy implements CompilerInterface {
  
  /** The actual compiler interface. If it's null, we couldn't load it. */
  private CompilerInterface _realCompiler = null;

  private final String _className;
  private final ClassLoader _newLoader;
  private boolean _warningsEnabled = true;
  private File _buildDir;
  private ClasspathVector _extraClasspath = new ClasspathVector();
  
  /** These classes will always be loaded using the previous classloader. This is important to make sure there is
   *  only one instance of them, so their values can be freely passed about the program.
   */
  private static final String[] _useOldLoader = {
    "edu.rice.cs.drjava.model.Configuration",
    "edu.rice.cs.drjava.model.compiler.CompilerInterface",
    "edu.rice.cs.drjava.model.compiler.CompilerError"
  };

  /** A proxy compiler interface that tries to load the given class from one of the given locations. It uses its own
   *  classloader, which will even allow loading a second instance of the class!
   *  @param className Implementation of {@link CompilerInterface} to proxy for.
   *  @param newLoader Classloader to use
   */

  public CompilerProxy(String className, ClassLoader newLoader) {
    _className = className;
    _newLoader = newLoader;
    _recreateCompiler();
  }

  private void _recreateCompiler() {
    File collectionsPath = DrJava.getConfig().getSetting(OptionConstants.JSR14_COLLECTIONSPATH);

    StickyClassLoader loader = new StickyClassLoader(_newLoader, getClass().getClassLoader(), _useOldLoader);

    try {
      Class<?> c = loader.loadClass(_className);
      _realCompiler = CompilerRegistry.createCompiler(c);

      _realCompiler.setBuildDirectory(_buildDir);
      
      _realCompiler.setExtraClassPath(File.pathSeparator + _extraClasspath.toString());

      _realCompiler.setWarningsEnabled(_warningsEnabled);
      
      boolean allowAssertions =
        DrJava.getConfig().getSetting(OptionConstants.JAVAC_ALLOW_ASSERT).booleanValue();
      _realCompiler.setAllowAssertions(allowAssertions);

      String compilerClass = _realCompiler.getClass().getName();
      if ((compilerClass.equals("edu.rice.cs.drjava.model.compiler.JSR14v10Compiler") ||
           compilerClass.equals("edu.rice.cs.drjava.model.compiler.JSR14v12Compiler") ||
           compilerClass.equals("edu.rice.cs.drjava.model.compiler.JSR14v20Compiler")) &&
          collectionsPath != FileOption.NULL_FILE) {
        _realCompiler.addToBootClassPath(collectionsPath);
      }

//      DrJava.consoleErr().println("real compiler: " + _realCompiler + " this: " + this);
    }
    catch (Throwable t) {
        // don't do anything. realCompiler stays null.
//        DrJava.consoleErr().println("loadClass fails: " + t);
//        t.printStackTrace(DrJava.consoleErr());
    }
  }


  /** Compile the given files.
   *  @param files Source files to compile.
   *  @param sourceRoot Source root directory, the base of the package structure.
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    _recreateCompiler();
//    DrJava.consoleOut().println("realCompiler is " + _realCompiler.getClass());
    CompilerError[] ret =  _realCompiler.compile(sourceRoot, files);

    return ret;
  }

  /** Compile the given files.
   *  @param files Source files to compile.
   *  @param sourceRoots Array of source root directories, the base of the package structure for all files to compile.
   *
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  public CompilerError[] compile(File[] sourceRoots, File[] files) {
//    DrJava.consoleErr().println("proxy to compile: " + files[0]);

//    DrJava.consoleOut().println("-- In CompilerProxy: SourceRoots:");
//    for (int i = 0 ; i < sourceRoots.length; i ++) {
//      DrJava.consoleOut().println(sourceRoots[i]);
//    }

    _recreateCompiler();
//    DrJava.consoleOut().println("realCompiler is " + _realCompiler.getClass());
    CompilerError[] ret =  _realCompiler.compile(sourceRoots, files);

    return ret;
  }

  /** Indicates whether this compiler is actually available. As in: Is it installed and located? This method should
   *  load the compiler class, which should hopefully prove whether the class can load.  If this method returns true,
   *  the {@link #compile} method should not fail due to class not being found.
   */
  public boolean isAvailable() {
    if (_realCompiler == null) return false;
    else return _realCompiler.isAvailable();
  }

  /** Returns the name of this compiler, appropriate to show to the user. */
  public String getName() {
    if (!isAvailable())  return "(unavailable)";
    return _realCompiler.getName();
  }

  /** Should return info about compiler, at least including name. */
  public String toString() { return getName(); }

  /** Allows us to set the extra classpath for the compilers without referencing the
   *  config object in a loaded class file
   */
  public void setExtraClassPath( String extraClassPath) {
    _realCompiler.setExtraClassPath(extraClassPath);
  }
  
  /** Sets the extra classpath in the form of a ClasspathVector. This should include any classpath entries from the
   *  project's classpath, if any, and the entries from EXTRA_CLASSPATH.
   *  @param extraClassPath the classpath to use as the compiler's extra classpath
   */
  public void setExtraClassPath(ClasspathVector extraClassPath) {
    _extraClasspath = extraClassPath;
  }

  /** Sets whether to allow assertions in Java 1.4. */
  public void setAllowAssertions(boolean allow) {
    _realCompiler.setAllowAssertions(allow);
  }
  
  /** Sets whether or not warnings are allowed. */
  public void setWarningsEnabled(boolean warningsEnabled) {
    _realCompiler.setWarningsEnabled(warningsEnabled);
    _warningsEnabled = warningsEnabled;
  }

  /** This method allows us to set the JSR14 collections path across a class loader.
   *  (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */
  public void addToBootClassPath( File cp) {
    _realCompiler.addToBootClassPath(cp);
  }

  /** Sets the build directory for the compilers. */
  public void setBuildDirectory(File buildDir) {
    _realCompiler.setBuildDirectory(buildDir);
    _buildDir = buildDir;
  }
}



