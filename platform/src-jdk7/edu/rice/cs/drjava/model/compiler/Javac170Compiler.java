/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

// Uses JDK 7/8 tools classes
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;

import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** An implementation of JavacCompiler that supports compiling with the java 7 or 8 compiler.  Must be compiled
  * using javac 7 or 8.
  * **TODO** Rename this class as Javac8Compiler.java; all Java version older than 8 are deprecated and unsupported.  
  * Java 9+ in incompatible at the JDK level.  Eliminate code that refers to Java versions older than 8.
  * @version $Id$
  */
public class Javac170Compiler extends JavacCompiler { // Javac170FilteringCompiler {
  
  public Javac170Compiler(JavaVersion.FullVersion version, String location, List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  // TODO change the code to look for a field that does not exist prior to Java 8.  Will RELEASE_8 work?  Is there a
  // field in JavaCompiler that was introduced in Java 8?  If so we only need to load class JavaCompiler and look for
  // that field.
  public boolean isAvailable() {
    try {
      // Diagnostic was introduced in Java 6
      Class<?> diagnostic = Class.forName("javax.tools.Diagnostic");
      diagnostic.getMethod("getKind");
      // javax.lang.model.SourceVersion.RELEASE_7 field introduced in Java 7
      Class<?> sourceVersion = Class.forName("javax.lang.model.SourceVersion");
      sourceVersion.getField("RELEASE_7");  // only exists in Java 7 and later releases
      // javax.tools.Diagnostic and javax.lang.model.SourceVersion are also found in rt.jar;
      // to test if tools.jar is available, we need to test for a class only found in tools.jar
      Class.forName("com.sun.tools.javac.main.JavaCompiler");
      
      // This is the class that javax.tools.ToolProvider.getSystemJavaCompiler() uses
      // We create an instance of that class directly, bypassing ToolProvider, because ToolProvider returns null
      // if DrJava is started with just the JRE, instead of with the JDK, even if tools.jar is later made available
      // to the class loader.
      JavaCompiler compiler = (JavaCompiler)(Class.forName("com.sun.tools.javac.api.JavacTool").newInstance());
      
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
  // TODO strip out debugging other than Log
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
//      System.err.println("Calling '" + compiler + "' with options " + options);
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
        if (d.getSource() != null) {
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
    /* The following line is commented out because it does not work for Java 8. */
//    else { options.add("-target"); options.add("1.7"); }

    return options;
  }

}
