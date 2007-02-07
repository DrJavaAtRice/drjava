/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Uses JDK 1.6.0 tools classes
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

// Uses JDK 1.6.0 compiler classes
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Main;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DefaultFileManager;
//import com.sun.tools.javac.util.JCDiagnostic;
//import com.sun.tools.javac.util.JCDiagnostic.DiagnosticType;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;
// import com.sun.tools.javac.util.List; Clashes with java.util.List
import com.sun.tools.javac.util.Log;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.reflect.JavaVersion;

/** An implementation of the CompilerInterface that supports compiling with the java 1.6.0 compiler.  Must be compiled
 *  using javac 1.6.0.  
 *
 *  @version $Id$
 */
public class Javac160Compiler implements CompilerInterface {
  
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new Javac160Compiler();

  public static final String COMPILER_CLASS_NAME = "com.sun.tools.javac.main.JavaCompiler";
  
  private final JavaVersion.FullVersion _version;
  private final List<? extends File> _defaultBootClassPath;
  
  /** Constructor for Javac160Compiler will throw a RuntimeException if an invalid version of the JDK is in use. */ 
  public Javac160Compiler(JavaVersion.FullVersion version, List<? extends File> defaultBootClassPath) {
    _version = version;
    _defaultBootClassPath = defaultBootClassPath;
  }
  
  /** Uses reflection on the class Diagnostic, introduced in the Java 1.6 compiler, to confirm
   *  that the compiler is version 1.6.
   */
  private boolean _isValidVersion() {
    
    Class diagnostic = javax.tools.Diagnostic.class;
    
    try { 
      diagnostic.getMethod("getKind");
      // succeeds: must be a Java 1.6 (or later?) compiler
//      Utilities.show("isValid in Java160Compiler returns " + true);
      return true;
    }
    catch (NoSuchMethodException e) {
      // fails: not a Java 1.6 compiler
//      Utilities.show("isValid in Java160Compiler returns " + false);
      return false;
    }
  }
  
  public boolean isAvailable() { return _isValidVersion(); }
  

  /** Compile the given files.
    *  @param files  Source files to compile.
    *  @param classPath  Support jars or directories that should be on the classpath.  If @code{null}, the default is used.
  *  @param sourcePath  Location of additional sources to be compiled on-demand.  If @code{null}, the default is used.
  *  @param destination  Location (directory) for compiled classes.  If @code{null}, the default in-place location is used.
  *  @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with @code{sourceVersion} 
  *                        If @code{null}, the default is used.
  *  @param sourceVersion  The language version of the sources.  Should be consistent with @code{bootClassPath}.  If @code{null},
    *                        the default is used.
  *  @param showWarnings  Whether compiler warnings should be shown or ignored.
    *  @return Errors that occurred. If no errors, should be zero length (not null).
    */
  public List<? extends CompilerError> compile(List<? extends File> files, List<? extends File> classPath, 
                                               List<? extends File> sourcePath, File destination, 
                                               List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }
    Context context = _createContext(classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    new CompilerErrorListener(context, errors);
    
    JavaCompiler compiler = JavaCompiler.instance(context);
    
    /** Default FileManager provided by Context class */
    DefaultFileManager fileManager = (DefaultFileManager) context.get(JavaFileManager.class);
    com.sun.tools.javac.util.List<JavaFileObject> fileObjects = com.sun.tools.javac.util.List.nil();
    for (File f : files) fileObjects = fileObjects.prepend(fileManager.getRegularFile(f));
    
    try { compiler.compile(fileObjects); }
    catch(Throwable t) {  // compiler threw an exception/error (typically out of memory error)
      errors.addFirst(new CompilerError("Compile exception: " + t, false));
    }
    
    return errors;
  }
  
  
  public String toString() { return "JDK " + _version.versionString(); }
  
  public String getName() { return toString(); }

  private Context _createContext(List<? extends File> classPath, List<? extends File> sourcePath, File destination, 
                                 List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
    Context context = new Context();
    Options options = Options.instance(context);
    for (Map.Entry<String, String> e : CompilerOptions.getOptions(showWarnings).entrySet()) {
      options.put(e.getKey(), e.getValue());
    }
    
    //Should be setable some day?
    options.put("-g", "");
    
    if (classPath != null) { options.put("-classpath", _pathToString(classPath)); }
    if (sourcePath != null) { options.put("-sourcepath", _pathToString(sourcePath)); }
    if (destination != null) { options.put("-d", destination.getPath()); }
    if (bootClassPath != null) { options.put("-bootclasspath", _pathToString(bootClassPath)); }
    if (sourceVersion != null) { options.put("-source", sourceVersion); }
    if (!showWarnings) { options.put("-nowarn", ""); }
    
    return context;
  }
  
  private static String _pathToString(List<? extends File> path) {
    StringBuffer result = new StringBuffer();
    boolean first = true;
    for (File f : path) {
      if (!first) { result.append(File.pathSeparatorChar); }
      first = false;
      result.append(f.getPath());
    }
    return result.toString();
  }
    
  
  /** We need to embed a DiagnosticListener in our own Context.  This listener will build a CompilerError list. */
  private static class CompilerErrorListener implements DiagnosticListener<JavaFileObject> {
    
    private List<? super CompilerError> _errors;
    
    public CompilerErrorListener(Context context, List<? super CompilerError> errors) {
      _errors = errors;
      context.put(DiagnosticListener.class, this);
    }
    
    public void report(Diagnostic<? extends JavaFileObject> d) {
      
      Diagnostic.Kind dt = d.getKind();
      boolean isWarning = false;  // init required by javac
      
      switch (dt) {
        case OTHER:             return;
        case NOTE:              return;
        case MANDATORY_WARNING: isWarning = true; break;
        case WARNING:           isWarning = true; break;
        case ERROR:             isWarning = false; break;
      }
      
      /* The new Java 6.0 Diagnostic interface appears to be broken.  The expression d.getSource().getName() returns a 
        * non-existent path--the name of the test file (allocated as a TEMP file) appended to the source root for 
        * DrJava--in GlobalModelCompileErrorsTest.testCompileFailsCorrectLineNumbers().  The expression 
        * d.getSource().toUri().getPath() returns the correct result as does ((JCDiagnostic) d).getSourceName(). */
      
      
      _errors.add(new CompilerError(new File(d.getSource().toUri().getPath()), // d.getSource().getName() fails! 
                                    ((int) d.getLineNumber()) - 1,  // javac starts counting at 1
                                    ((int) d.getColumnNumber()) - 1, 
                                    d.getMessage(null),    // null is the locale
                                    isWarning));
    }
  }
  
}
