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

import java.util.LinkedList;
import java.util.Arrays;

// Uses Java 1.4.1+ / JSR-14 v1.2 compiler classes
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Context;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Options;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;

//import edu.rice.cs.drjava.DrJava;
import gj.util.Vector;
import gj.util.Enumeration;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * javac 1.4.1+.
 *
 * @version $Id$
 */
public class Javac141Compiler implements CompilerInterface {
  
  private String _extraClassPath = "";

  protected boolean _allowAssertions = false;
    
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new Javac141Compiler();

  public static final String COMPILER_CLASS_NAME =
    "com.sun.tools.javac.v8.JavaCompiler";
  
  protected Context context = null;

  private String _builtPath = "";
  
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
   * Constructor for Javac141Compiler will throw a RuntimeException if an invalid version
   * of the JDK is in use. 
   */ 
  protected Javac141Compiler() {
    if (!_isValidVersion()) {
      throw new RuntimeException("Invalid version of Java compiler.");
    } 
  }
  
  /**
   * Uses reflection on the Log object to deduce which JDK is being used.
   * If the constructor for Log in this JDK does not match that of Java 1.4.1
   * or JSR-14 v1.2, then the version is not supported.
   */
  protected boolean _isValidVersion() {
    
    Class log = com.sun.tools.javac.v8.util.Log.class;
    // The JDK 1.4.1/JSR14 1.2 version of the Log instance method
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
    List filesToCompile = new List();

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
      
      
      //Added to account for error in javac whereby a variable that was not declared will
      //cause an out of memory error. This change allows us to output both errors and not
      //just the out of memory error
      
      CompilerError[] errorArray = new CompilerError[compilerLog.getErrors().length + 1];
      for(int i = 0; i < compilerLog.getErrors().length; i++) {
        errorArray[i+1] = compilerLog.getErrors()[i];
      }
      errorArray[0] = new CompilerError("Compile exception: " + t, false);
      return errorArray; 
      
//      return compilerLog.getErrors();
//      return new CompilerError[] {
//        new CompilerError("Compile exception: " + t, false)
//      };
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
      return _isValidVersion();
    }
    catch (Exception e) {
      return false;
    }
  }

  public String getName() {
    return "javac 1.4.x";
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
    
    if(CompilerWarnings.SHOW_UNCHECKED) {
      options.put("-Xlint:unchecked","");
    }
    
    if(CompilerWarnings.SHOW_DEPRECATION) {
      options.put("-Xlint:deprecation","");
    }

    if(CompilerWarnings.SHOW_PATH) {
      options.put("-Xlint:path","");
    }
    
    if(CompilerWarnings.SHOW_SERIAL) {
      options.put("-Xlint:serial","");
    }
    
    if(CompilerWarnings.SHOW_FINALLY) {
      options.put("-Xlint:finally","");
    }
    
    if(CompilerWarnings.SHOW_FALLTHROUGH) {
      options.put("-Xlint:fallthrough","");
      options.put("-Xlint:switchcheck",""); //Some compilers appear to use this option instead. Anyone know anything about this?
    }

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // turn on generics, if we have em
    _addGenericsOption(options);

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
   * Adds the appropriate switch for generics, if available.
   */
  protected void _addGenericsOption(Options options) {
    // No generics support
  }
  
  /**
   * Adds the appropriate values for the source and target arguments.
   */
  protected void _addSourceAndTargetOptions(Options options) {
    // Set output classfile version to 1.1
    options.put("-target", "1.1");
    
    // Allow assertions in 1.4 if configured and in Java >= 1.4
    String version = System.getProperty("java.version");
    if ((_allowAssertions) && (version != null) &&
        ("1.4.0".compareTo(version) <= 0)) {
      options.put("-source", "1.4");
    }
    if(! _builtPath.equals(""))
      options.put("-d",_builtPath);
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

    compiler = JavaCompiler.make(context);
  }

   
  public void setBuildDirectory(File dir){
    if(dir == null)
      _builtPath = "";
    else
      _builtPath=dir.getAbsolutePath(); 
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
    public void warning(int pos, String key, String arg0, String arg1,
                        String arg2, String arg3)
    {
      super.warning(pos, key, arg0, arg1, arg2, arg3);

      String msg = getText("compiler.warn." + key,
        arg0, arg1, arg2, arg3, null, null, null);

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
    public void error(int pos, String key, String arg0, String arg1,
                      String arg2, String arg3, String arg4, String arg5,
                      String arg6)
    {
      super.error(pos, key, arg0, arg1, arg2, arg3, arg4, arg5, arg6);

      String msg = getText("compiler.err." + key,
                           arg0, arg1, arg2, arg3,
                           arg4, arg5, arg6);

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
