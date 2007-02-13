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
