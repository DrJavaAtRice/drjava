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
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.LinkedList;

// Uses JSR-14 v2.0 compiler classes
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;

import gj.util.Vector;
import gj.util.Enumeration;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.newjvm.ExecJVM;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * JSR14v2.0, JSR14v2.2, or JSR14v2.3. This class can only be compiled with one of the 
 * versions because of the change in the syntax of a variable number of arguments
 * to a method. It was (Object[] args ...), now it's (Object ... args). Right now
 * we will use the JSR14v2.2 version since it is now the current version.  As far as
 * we are concerned here, JSR14v2.3 differs from JSR14v2.2 only in that the "make"
 * method in JavaCompiler is now called "instance".  Reflection is used to dynamically
 * get the appropriate method to use once the correct version is detected.
 *
 * @version $Id$
 */
public class JSR14v20Compiler implements CompilerInterface {
  
  private String _extraClassPath = "";

  protected boolean _allowAssertions = false;  
  
  private boolean _isJSR14v2_4;
    
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new JSR14v20Compiler();

  public static final String COMPILER_CLASS_NAME =
    "com.sun.tools.javac.main.JavaCompiler";
  
  protected Context context = null;

  /** A writer that discards its input. */
  private static final Writer NULL_WRITER = new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  };

  /**
   * A no-op printwriter to pass to the compiler to print error messages.
   */
  private static final PrintWriter NULL_PRINT_WRITER =
    new PrintWriter(NULL_WRITER);

  //private final boolean _supportsGenerics;
  protected JavaCompiler compiler;

  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */
  protected OurLog compilerLog;

  /**
   * Constructor for JSR14v20Compiler will throw a RuntimeException if an invalid version
   * of the JDK is in use. 
   */ 
  protected JSR14v20Compiler() {
    if (!_isValidVersion()) {
      throw new RuntimeException("Invalid version of Java compiler.");
    }
    _isJSR14v2_4 = _isJSR14v2_4();
  }
  
  /**
   * Uses reflection on the Log object to deduce which JDK is being used.
   * If the constructor for Log in this JDK does not match that of 
   * JSR-14 v2.0, then the version is not supported.
   */
  protected boolean _isValidVersion() {
    
    Class log = com.sun.tools.javac.util.Log.class;
    // The JSR14 1.2 version of the Log instance method
    Class[] validArgs1 = {
      Context.class
    };
    
    try { 
      log.getMethod("instance", validArgs1);
      // succeeds, therefore must be correct version
      return true;
    }
    catch (NoSuchMethodException e) {
      // Didn't have expected method, so we can't use this compiler.
      return false;
    }
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
    //DrJava.consoleOut().println("-- In JavacGJCompiler: SourceRoots:");
    //for (int i = 0 ; i < sourceRoots.length; i ++) {
    //  DrJava.consoleOut().println(sourceRoots[i]);
    //}
    initCompiler(sourceRoots);
    List<String> filesToCompile = new List<String>();

    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }

    try {
      compiler.compile(filesToCompile);
    }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      //System.err.println("Compile error: " + t);
      //t.printStackTrace();
      return new CompilerError[] {
        new CompilerError("Compile exception: " + t, false)
      };
    }

    CompilerError[] errors = compilerLog.getErrors();

    // null out things to not keep pointers to dead data
    compiler = null;
    compilerLog = null;
    return errors;
  }

  public boolean isAvailable() {
    try {
      Class.forName(COMPILER_CLASS_NAME);
      Class.forName("java.lang.Enum");
      return _isValidVersion();
    }
    catch (Exception e) {
      return false;
    }
  }

  public String getName() {
    if (_isJSR14v2_4) {
      return "JSR-14 v2.4";
    }
    else {
      return "JSR-14 v2.0/2.2";// + com.sun.tools.javac.Main.class.getResource("Main.class");
    }
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
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */
  public void addToBootClassPath( File cp) {
    throw new UnexpectedException( new Exception("Method only implemented in JSR14Compiler"));
  }

  /**
   * Sets whether to allow assert statements.
   */
  public void setAllowAssertions(boolean allow) {
    _allowAssertions = allow;
  }
  
  protected Context createContext(File[] sourceRoots) {
    Context context = new Context();
    Options options = Options.instance(context);
    
    options.put("-warnunchecked", "");

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // Set output target version
    _addSourceAndTargetOptions(options);   

    String sourceRootString = getSourceRootString(sourceRoots);
    options.put("-sourcepath", sourceRootString /*sourceRoot.getAbsolutePath()*/);

    String cp = System.getProperty("java.class.path");
    // Adds extra.classpath to the classpath.
    cp += _extraClassPath;
    cp += File.pathSeparator + sourceRootString;
    
    options.put("-classpath", cp);    
    return context;
  }
  
  /**
   * Adds the appropriate values for the source and target arguments.
   */
  protected void _addSourceAndTargetOptions(Options options) {
    options.put("-source", "1.5");
    if (_isJSR14v2_4) {
      options.put("-target", "jsr14");
    }
//    options.put("-target", "1.5");
    options.put("-fork", "on");
  }
  
  /**
   * Package protected utility method for getting a properly formatted
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

  protected void initCompiler(File[] sourceRoots) {
    context = createContext(sourceRoots);
    compilerLog = new OurLog(context);

    
    // Using reflection to allow for JSR14v2.3 since the "make"
    // method was changed to "instance".
    Class javaCompilerClass = JavaCompiler.class;
    Class[] validArgs1 = {
      Context.class
    };
    Method m;    
    if (_isJSR14v2_4) {    
      try { 
        m = javaCompilerClass.getMethod("instance", validArgs1);
        compiler = (JavaCompiler)m.invoke(null, new Object[] {context});
      }
      catch (NoSuchMethodException e) {
        throw new UnexpectedException(e);
      }
      catch (IllegalAccessException e) {
        throw new UnexpectedException(e);
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
        throw new UnexpectedException(e);
      }      
    }
    else {
      try { 
        m = javaCompilerClass.getMethod("make", validArgs1);
        compiler = (JavaCompiler)m.invoke(null, new Object[] {context});
      }
      catch (NoSuchMethodException e) {
        throw new UnexpectedException(e);
      }
      catch (IllegalAccessException e) {
        throw new UnexpectedException(e);
      }
      catch (InvocationTargetException e) {
        throw new UnexpectedException(e);
      }
//      compiler = JavaCompiler.make(context);
    }
  }
  
  /**
   * Check if we're using JSR14v2.4.  We're skipping version 2.3
   * because it will never be officially released.
   */
  private boolean _isJSR14v2_4() {
    try {
      Class.forName("com.sun.tools.javac.main.Main$14");
      return true;
    }
    catch (Exception e) {
      try {
        Class.forName("com.sun.tools.javac.main.Main+1");
        return true;
      }
      catch (Exception e2) {
        return false;
      }
    }    
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4.1+
   * or JSR14 v1.2+.
   */
  private class OurLog extends Log {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog(Context context) {
      super(context, NULL_PRINT_WRITER, NULL_PRINT_WRITER, NULL_PRINT_WRITER);
    }

    /**
     * JSR14 uses this crazy signature on warning method because it localizes
     * the warning message.
     */
    public void warning(int pos, String key, Object ... args)
    {
      super.warning(pos, key, args);
      //System.out.println("warning: pos = " + pos);

      String msg = getText("compiler.warn." + key, args);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        true));
    }

    /**
     * JSR14 uses this crazy signature on error method because it localizes
     * the error message.
     */
    public void error(int pos, String key, Object ... args)
    {
      super.error(pos, key, args);
      //System.out.println("error: pos = " + pos);

      String msg = getText("compiler.err." + key, args);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        false));
    }

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }
}
