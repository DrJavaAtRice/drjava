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

import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/* Question (Corky): Is this adapter obsolete? */
/** An implementation of the CompilerInterface (in DrJava) that supports compiling with
  * OpenJDK 6.0. It is a Java 6 compiler, but uses the Java 5 interface from DrJava.
  *
  * @version $Id$
  */
public class Javac160OpenJDKCompiler extends JavacCompiler {
  public static final String COMPILER_CLASS_NAME = "com.sun.tools.javac.main.JavaCompiler";
  
  /** A writer that discards its input. */
  private static final PrintWriter NULL_WRITER = new PrintWriter(new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  });

  public Javac160OpenJDKCompiler(JavaVersion.FullVersion version, String location, List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }

  public String getName() {
    String versionString = _version.toString();
    String prefix = "Java ";
    if (versionString.startsWith(prefix)) versionString = versionString.substring(prefix.length());
    return "OpenJDK " + versionString;
  }
  
  public boolean isAvailable() {
    try {
      Class.forName("com.sun.tools.javac.main.JavaCompiler");
      try { Class.forName("java.lang.Enum"); }
      catch (Exception e) {
        // If java.lang.Enum is not found, there's a chance the user specified JSR14v2.5 
        // For some reason, java.lang.Enum got moved to collect.jar which we can't put on the
        // bootclasspath.  Look for something 2.5 specific.
        Class.forName("com.sun.tools.javac.main.Main$14");
      }
      return _isValidVersion();
    }
    catch (Exception e) { return false; }
    catch (LinkageError e) { return false; }
  }
  
  /** Uses reflection on the Log object to deduce which JDK is being used. If the constructor for Log in this JDK 
   *  does not match that of JSR-14 v2.0, then the version is not supported.
   */
  @SuppressWarnings("unchecked")
  private boolean _isValidVersion() {
    
    Class log = com.sun.tools.javac.util.Log.class;

    // The JSR14 1.2 version of the Log instance method
    Class[] validArgs1 = { com.sun.tools.javac.util.Context.class };
    
    try { 
      // TODO
      log.getMethod("instance", validArgs1);  // found in Java 5.0 compilers (and JSR14 prototypes >= 1.2)
      return true;
    }
    catch (NoSuchMethodException e) {
      return false;  // does not support Java 5.0 Log functionality (added in JSR14 1.2 prototype)
    }
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
    
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    
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
    else { options.add("-target"); options.add("1.5"); }

    return options;
  }

}
