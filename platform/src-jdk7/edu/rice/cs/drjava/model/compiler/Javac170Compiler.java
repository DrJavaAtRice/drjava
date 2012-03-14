/*BEGIN_COPYRIGHT_BLOCK
*
* This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
* or http://sourceforge.net/projects/drjava/
*
* DrJava Open Source License
* 
* Copyright (C) 2001-2010 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

// Uses JDK 1.7.0 tools classes
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** An implementation of JavacCompiler that supports compiling with the java 1.7.0 compiler.  Must be compiled
 *  using javac 1.7.0.  
 *
 *  @version $Id$
 */
public class Javac170Compiler extends JavacCompiler { // Javac170FilteringCompiler {
  
  public Javac170Compiler(JavaVersion.FullVersion version, String location, List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  public boolean isAvailable() {
    try {
      // Diagnostic was introduced in the Java 1.6 compiler
      Class<?> diagnostic = Class.forName("javax.tools.Diagnostic");
      diagnostic.getMethod("getKind");
      // javax.lang.model.SourceVersion.RELEASE_7 field introduced in Java 7
      Class<?> sourceVersion = Class.forName("javax.lang.model.SourceVersion");
      sourceVersion.getField("RELEASE_7");
      // javax.tools.Diagnostic and javax.lang.model.SourceVersion are also found in rt.jar;
      // to test if tools.jar is available, we need to test for a class only found in tools.jar
      Class.forName("com.sun.tools.javac.main.JavaCompiler");
      
      // This is the class that javax.tools.ToolProvider.getSystemJavaCompiler() uses
      // We create an instance of that class directly, bypassing ToolProvider, because ToolProvider returns null
      // if DrJava is started with just the JRE, instead of with the JDK, even if tools.jar is later made available
      // to the class loader.
      JavaCompiler compiler = (JavaCompiler)(Class.forName("com.sun.tools.javac.api.JavacTool").newInstance());
//      Utilities.show("isAvailable() for JDK7.0 is returning " + (compiler != null));
      return (compiler != null);
    }
    catch (Exception e) { return false; }
    catch (LinkageError e) { return false; }
  }

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
  public List<? extends DJError> compile(List<? extends File> files, List<? extends File> classPath, 
                                               List<? extends File> sourcePath, File destination, 
                                               List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
    debug.logStart("compile()");
    debug.logValues(new String[]{ "this", "files", "classPath", "sourcePath", "destination", "bootClassPath", 
                                  "sourceVersion", "showWarnings" },
                    this, files, classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);

    Iterable<String> options = _createOptions(classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    LinkedList<DJError> errors = new LinkedList<DJError>();

    // This is the class that javax.tools.ToolProvider.getSystemJavaCompiler() uses.
    // We create an instance of that class directly, bypassing ToolProvider, because ToolProvider returns null
    // if DrJava is started with just the JRE, instead of with the JDK, even if tools.jar is later made available
    // to the class loader.
    JavaCompiler compiler = null;
    try {
      compiler = (JavaCompiler)(Class.forName("com.sun.tools.javac.api.JavacTool").newInstance());
    }
    catch(ClassNotFoundException e) {
      errors.addFirst(new DJError("Compile exception: " + e, false));
      error.log(e);
      return errors;
    }
    catch(InstantiationException e) {
      errors.addFirst(new DJError("Compile exception: " + e, false));
      error.log(e);
      return errors;
    }
    catch(IllegalAccessException e) {
      errors.addFirst(new DJError("Compile exception: " + e, false));
      error.log(e);
      return errors;
    }
    
    /** Default FileManager provided by Context class */
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);    
    Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromFiles(files);
    
    try {
      compiler.getTask(null, fileManager, diagnostics, options, null, fileObjects).call();
      for (Diagnostic<? extends JavaFileObject> d: diagnostics.getDiagnostics()) {
        Diagnostic.Kind dt = d.getKind();
        boolean isWarning = false;  // init required by javac
        
        switch (dt) {
          case OTHER:             continue; // skip, do not record
          case NOTE:              continue; // skip, do not record
          case MANDATORY_WARNING: isWarning = true; break;
          case WARNING:           isWarning = true; break;
          case ERROR:             isWarning = false; break;
        }
        
        /* The new Java 6.0 Diagnostic interface appears to be broken.  The expression d.getSource().getName() returns a 
         * non-existent path--the name of the test file (allocated as a TEMP file) appended to the source root for 
         * DrJava--in GlobalModelCompileErrorsTest.testCompileFailsCorrectLineNumbers().  The expression 
         * d.getSource().toUri().getPath() returns the correct result as does ((JCDiagnostic) d).getSourceName(). */
        if (d.getSource()!=null) {
          errors.add(new DJError(new File(d.getSource().toUri().getPath()), // d.getSource().getName() fails! 
                                 ((int) d.getLineNumber()) - 1,  // javac starts counting at 1
                                 ((int) d.getColumnNumber()) - 1, 
                                 d.getMessage(null),    // null is the locale
                                 isWarning));
        }
        else {
          errors.add(new DJError(d.getMessage(null), isWarning));
        }
      }
      fileManager.close();
    }
    catch(Throwable t) {  // compiler threw an exception/error (typically out of memory error)
      errors.addFirst(new DJError("Compile exception: " + t, false));
      error.log(t);
    }
    
    debug.logEnd("compile()");
    return errors;
  }
  
  private Iterable<String> _createOptions(List<? extends File> classPath, List<? extends File> sourcePath, File destination, 
                                          List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {    
    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }

    LinkedList<String> options = new LinkedList<String>();
    for (Map.Entry<String, String> e : CompilerOptions.getOptions(showWarnings).entrySet()) {
      options.add(e.getKey());
      if (e.getValue().length()>0) options.add(e.getValue());
    }
    options.add("-g");

    if (classPath != null) { options.add("-classpath"); options.add(IOUtil.pathToString(classPath)); }
    if (sourcePath != null) { options.add("-sourcepath"); options.add(IOUtil.pathToString(sourcePath)); }
    if (destination != null) { options.add("-d"); options.add(destination.getPath()); }
    if (bootClassPath != null) { options.add("-bootclasspath"); options.add(IOUtil.pathToString(bootClassPath)); }
    if (sourceVersion != null) { options.add("-source"); options.add(sourceVersion); }
    if (!showWarnings) { options.add("-nowarn"); }
    
    // Bug fix: if "-target" is not present, Iterables in for-each loops cause compiler errors
    if (sourceVersion != null) { options.add("-target"); options.add(sourceVersion); }
    else { options.add("-target"); options.add("1.7"); }

    return options;
  }

}
