/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.util.LinkedList;
import gjc.v6.JavaCompiler;
import gjc.v6.util.Name;
import gjc.v6.util.Position;
import gjc.v6.util.Hashtable;
import gjc.v6.util.List;
import gjc.v6.util.Log;
import edu.rice.cs.util.UnexpectedException;

/**
 * The GJ compiler used by DrJava.
 * @version $Id$
 */
public class GJv6Compiler implements CompilerInterface{

  private String _extraClassPath = "";

  /** Singleton instance. */
  public static final CompilerInterface ONLY = new GJv6Compiler();

  public static final String COMPILER_CLASS_NAME = "gj.v6.JavaCompiler";

  private JavaCompiler _compiler;
  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */
  private OurLog _compilerLog;

  /**
   * Create the compiler. Private because of singleton.
   * Fail if we are on a JDK that won't work with GJv6!
   */
  private GJv6Compiler () {
    float javaVersion = Float.parseFloat(System.getProperty("java.specification.version"));
    if (javaVersion > 1.3f) {
      throw new RuntimeException("GJv6 can't work with java version > 1.3!");
    }
  }

  /**
   * Set up new instance of the GJ compiler.
   */
  private void _initCompiler (File[] sourceRoots) {
    _compilerLog = new OurLog();
    // To use the GJ compiler, we build up the GJ options hashtable.
    Hashtable<String, String> options = Hashtable.make();
    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // Build up classpath attribute
    // Since GJ doesn't have a sourcepath attribute, we just
    // put the sourcepath into the classpath instead
    String oldclasspath = System.getProperty("java.class.path");
    String sourceRootString = getSourceRootString(sourceRoots);
    StringBuffer newclasspath = new StringBuffer(sourceRootString /*sourceRoot.getAbsolutePath()*/);
    if (oldclasspath.length() > 0) {
      newclasspath.append(File.pathSeparator);
      newclasspath.append(oldclasspath);
    }

    newclasspath.append(_extraClassPath);

    options.put("-classpath", newclasspath.toString());

    _compiler = JavaCompiler.make(_compilerLog, options);
  }

  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    File[] sourceRoots = new File[] { sourceRoot };
    return compile(sourceRoots, files);
  }

  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoots Array of source root directories, the base of
   *  the package structure for all files to compile.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File[] sourceRoots, File[] files) {
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    _initCompiler(sourceRoots);
    List<String> filesToCompile = new List<String>();
    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }
    try {
      _compiler.compile(filesToCompile);
    } catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      System.err.println("Compile error: " + t);
      t.printStackTrace();
      return  new CompilerError[] {
        new CompilerError("Compile exception: " + t, false)
      };
    }
    return  _compilerLog.getErrors();
  }

  public boolean isAvailable() {
    return true;
  }

  public String getName() {
    return "GJ compiler v6";
  }

  public String toString() {
    return getName();
  }


  /**
   * Allows us to set the extra classpath for the compilers without referencing the
   * config object in a loaded class file
   */
  public void setExtraClassPath( String extraClassPath) {
      _extraClassPath = extraClassPath;
  }

  /**
   * Sets whether to allow assertions in Java 1.4.
   */
  public void setAllowAssertions(boolean allow) {
    // Nothing to do: GJ is Java 1.3, and has no assertions
  }

  /**
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */
  public void addToBootClassPath( File cp) {
    throw new UnexpectedException( new Exception("Method only implemented in JSR14Compiler"));
  }

  /**
   * Utility method for getting a properly formatted
   * string with several source paths from an array of files.
   */
  protected String getSourceRootString(File[] sourceRoots) {
    StringBuffer roots = new StringBuffer();
    for (int i = 0; i < sourceRoots.length; i++) {
      roots.append(sourceRoots[i].getAbsolutePath());
      if (i < sourceRoots.length - 1) {
        roots.append(File.pathSeparator);
      }
    }
    return roots.toString();
  }

  public void setBuildDirectory(File builddir){
    // !!!
    // fill in soon
  }
  
   /**
   * Sets whether or not warnings are allowed
   */
  public void setWarningsEnabled(boolean warningsEnabled) {
    //Do we even need this file any more?
  }
  
  /**
   * put your documentation comment here
   */
  private static class OurLog extends Log {
    // List of CompilerError
    private LinkedList<CompilerError> _errors = new LinkedList<CompilerError>();
    private String _sourceName = "";

    /**
     * put your documentation comment here
     * @param source
     * @return {@inheritDoc}
     */
    public Name useSource(Name source) {
      _sourceName = source.toString();
      return  super.useSource(source);
    }

    /**
     * Overrides Log.print, making it a no-op.
     * This prevents extraneous prints of compiler error messages
     * to the console.
     */
    public void print(String s) {}

    /**
     * put your documentation comment here
     * @param pos
     * @param msg
     */
    public void warning(int pos, String msg) {
      super.warning(pos, msg);
      _errors.addLast(new CompilerError(new File(_sourceName),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1, msg, true));
    }

    /**
     * put your documentation comment here
     * @param pos
     * @param msg
     */
    public void error(int pos, String msg) {
      super.error(pos, msg);
      _errors.addLast(new CompilerError(new File(_sourceName),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1, msg, false));
    }

    /**
     * put your documentation comment here
     */
    public CompilerError[] getErrors() {
      return _errors.toArray(new CompilerError[0]);
    }
  }
}



