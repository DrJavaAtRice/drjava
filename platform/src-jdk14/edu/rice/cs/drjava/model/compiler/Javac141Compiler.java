/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/


package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Uses Java 1.4.1+ / JSR-14 v1.2 compiler classes
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Context;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Options;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
//import com.sun.tools.javac.v8.util.List; Clashes with java.util.List
import com.sun.tools.javac.v8.util.Log;

import edu.rice.cs.plt.reflect.JavaVersion;

import edu.rice.cs.plt.debug.DebugUtil;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * javac 1.4.1+.
 *
 * This is cross-compiled against the Java 1.4 libraries, so 1.5 APIs and language
 * constructs are not allowed.
 *
 * @version $Id$
 */
public class Javac141Compiler extends JavacCompiler {
  
  /** A writer that discards its input. */
  private static final PrintWriter NULL_WRITER = new PrintWriter(new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  });

  public Javac141Compiler(JavaVersion.FullVersion version, String location, List/*<? extends File>*/ defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  public boolean isAvailable() {
    try {
      Class.forName("com.sun.tools.javac.v8.JavaCompiler");
      return _isValidVersion();
    }
    catch (Exception e) {
      return false;
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
  public List/*<? extends CompilerError>*/ compile(List/*<? extends File>*/ files, List/*<? extends File>*/ classPath, 
                                                   List/*<? extends File>*/ sourcePath, File destination, 
                                                   List/*<? extends File>*/ bootClassPath, String sourceVersion, boolean showWarnings) {
    DebugUtil.debug.logStart("compile()");
    DebugUtil.debug.logValues(new String[]{ "this", "files", "classPath", "sourcePath", "destination", "bootClassPath", 
                                            "sourceVersion", "showWarnings" },
                              new Object[]{ this, files, classPath, sourcePath, destination, bootClassPath, 
                                            sourceVersion, Boolean.valueOf(showWarnings) });
    
    Context context = _createContext(classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    OurLog log = new OurLog(context);
    JavaCompiler compiler = JavaCompiler.make(context);
    
    com.sun.tools.javac.v8.util.List filesToCompile = new com.sun.tools.javac.v8.util.List();
    Iterator/*<? extends File>*/ filesI = files.iterator();
    while (filesI.hasNext()) {
      // TODO: Can we assume the files are canonical/absolute?  If not, we should use the util methods here.
      File f = (File) filesI.next();
      filesToCompile = filesToCompile.prepend(f.getAbsolutePath());
    }

    try { compiler.compile(filesToCompile); }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      //Added to account for error in javac whereby a variable that was not declared will
      //cause an out of memory error. This change allows us to output both errors and not
      //just the out of memory error
      
      LinkedList/*<CompilerError>*/ errors = log.getErrors();
      errors.addFirst(new CompilerError("Compile exception: " + t, false));
      DebugUtil.error.log(t);
      DebugUtil.debug.logEnd("compile() (caught exception)");
      return errors;
    }

    DebugUtil.debug.logEnd("compile()");
    return log.getErrors();
  }
  
  /**
    * Uses reflection on the Log object to deduce which JDK is being used.
   * If the constructor for Log in this JDK does not match that of Java 1.4.1
   * or JSR-14 v1.2, then the version is not supported.
   */
  private boolean _isValidVersion() {
    
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
  
  private Context _createContext(List/*<? extends File>*/ classPath, List/*<? extends File>*/ sourcePath, File destination, 
                                List/*<? extends File>*/ bootClassPath, String sourceVersion, boolean showWarnings) {
    Context context = new Context();
    Options options = Options.instance(context);
    
    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }
    
    Iterator/*<Map.Entry<String, String>>*/ optsI = CompilerOptions.getOptions(showWarnings).entrySet().iterator();
    while (optsI.hasNext()) {
      Map.Entry/*<String, String>*/ entry = (Map.Entry) optsI.next();
      options.put((String) entry.getKey(), (String) entry.getValue());
    }
    
    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    if (classPath != null) { options.put("-classpath", _pathToString(classPath)); }
    if (sourcePath != null) { options.put("-sourcepath", _pathToString(sourcePath)); }
    if (destination != null) { options.put("-d", destination.getPath()); }
    if (bootClassPath != null) { options.put("-bootclasspath", _pathToString(bootClassPath)); }
    if (sourceVersion != null) { options.put("-source", sourceVersion); }
    if (!showWarnings) { options.put("-nowarn", ""); }
    
    return context;
  }
  
  /** Implemented here because IOUtil.pathToString works on Iterables, which are not available at compile time. */
  private static String _pathToString(List/*<? extends File>*/ path) {
    StringBuffer result = new StringBuffer();
    Iterator/*<? extends File>*/ pathI = path.iterator();
    boolean first = true;
    while (pathI.hasNext()) {
      File f = (File) pathI.next();
      if (!first) { result.append(File.pathSeparatorChar); }
      first = false;
      result.append(f.getPath());
    }
    return result.toString();
  }
  
  
  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4.1+
   * or JSR14 v1.2+.
   */
  private static class OurLog extends Log {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog(Context context) {
      super(context, NULL_WRITER, NULL_WRITER, NULL_WRITER);
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

    public LinkedList/*<CompilerError>*/ getErrors() { return _errors; }
  }
  
}
