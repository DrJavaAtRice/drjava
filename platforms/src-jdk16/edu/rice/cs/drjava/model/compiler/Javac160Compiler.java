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

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.DiagnosticListener;
import javax.tools.DiagnosticMessage;

// Uses JDK 1.6.0 compiler classes
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Main;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Diagnostic;
import com.sun.tools.javac.util.Diagnostic.DiagnosticType;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.swing.Utilities;

/** An implementation of the CompilerInterface that supports compiling with the java 1.6.0 compiler.  Must be compiled
 *  using javac 1.6.0.  
 *
 *  @version $Id$
 */
public class Javac160Compiler implements CompilerInterface {
  
  private LinkedList<CompilerError> _errors = new LinkedList<CompilerError>();
  
  private String _extraClassPath = "";

  protected boolean _allowAssertions = false;  
  protected boolean _warningsEnabled = true;
  
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new Javac160Compiler();

  public static final String COMPILER_CLASS_NAME = "com.sun.tools.javac.main.JavaCompiler";
  
  private Context _context = null;
  
  private String _builtPath = "";
  
  /** A writer that discards its input.  TODO: move this constant to CompilerInterface. */
  private static final Writer NULL_WRITER = new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  };

  /** A no-op printwriter to pass to the compiler to print error messages. TODO: move to CompilerInterface.*/
  private static final PrintWriter NULL_PRINT_WRITER = new PrintWriter(NULL_WRITER);

  private JavaCompiler _compiler;

  /** We need to embed a DiagnosticListener in our own Context.  This listener will build a CompilerError list. */
  private DiagnosticListener diagnosticListener = new DiagnosticListener() {
    public void report(DiagnosticMessage msg) {
      
      Diagnostic d = (Diagnostic) msg;
      DiagnosticType dt = d.getType();
      boolean isWarning;  // initi
      
      switch (dt) {
        case FRAGMENT: return;
        case NOTE:     return;
        case WARNING:  isWarning = true; break;
        case ERROR:    isWarning = false; break;
        default:
          throw new AssertionError("Unknown diagnostic type: " + dt);
    }

      _errors.addLast(new CompilerError(new File(d.getSourceName()),
                                        d.getLineNumber() - 1,  // javac starts counting at 1
                                        d.getColumnNumber() - 1, 
                                        d.toString(),
                                        isWarning));
    }
  };

  /** Constructor for Javac160Compiler will throw a RuntimeException if an invalid version of the JDK is in use. */ 
  private Javac160Compiler() {
    if (! _isValidVersion()) throw new RuntimeException("Invalid version of Java 1.6 compiler.");
  }
  
  /** Uses reflection on the class Diagnostic, introduced in the Java 1.6 compiler, to confirm
   *  that the compiler is version 1.6.
   */
  private boolean _isValidVersion() {
    
    Class diagnostic = com.sun.tools.javac.util.Diagnostic.class;
    
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
   *  @param files Source files to compile.
   *  @param sourceRoot Source root directory, the base of the package structure.
   *
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    File[] sourceRoots = new File[] { sourceRoot };
    return compile(sourceRoots, files);
  }
  
  /** Compile the given files.
   *  @param files Source files to compile.
   *  @param sourceRoots Array of source root directories, the base of the package structure for all files to compile.
   *
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  public CompilerError[] compile(File[] sourceRoots, File[] files) {
    
    initCompiler(sourceRoots);  // binds compiler to a compiler with our DiagnosticListener
    
    /** Default FileManager provided by Context class */
    JavaFileManager fileManager = _context.get(JavaFileManager.class);
    
    /** List of FileObjects to compile. */
    List<JavaFileObject> fileObjects = List.nil();
    for (File f : files) {
      try { fileObjects = fileObjects.prepend(fileManager.getFileForInput(f.getAbsolutePath())); }
      catch(IOException e) { /* Problem opening file; ignore it */ }
    }
    
//    Utilities.show("Compiling " + fileObjects);
//    Utilities.show("Javac160Compiler is: " + _compiler); 
    try { _compiler.compile(fileObjects); }
    
    catch(Throwable t) {  // compiler threw an exception/error (typically out of memory error)
      _errors.addFirst(new CompilerError("Compile exception: " + t, false));
//      Utilities.showTrace(t);
    }

    // null out things to not keep pointers to dead data
    _compiler = null;
    return _errors.toArray(new CompilerError[_errors.size()]);
  }

  public String toString() { return "JDK 1.6"; }
  
  public String getName() { return toString(); }

  /** Sets the extra classpath for the compilers without referencing the config object in a loaded class file. */ 
  public void setExtraClassPath(String extraClassPath) { _extraClassPath = extraClassPath; }

  public void setExtraClassPath(ClassPathVector extraClassPath) { setExtraClassPath(extraClassPath.toString()); }
  
  /** Sets the JSR14 collections path across a class loader.  We cannot cast a loaded class to a subclass (?), so all
   *  compiler interfaces must have this method)
   */
  public void addToBootClassPath( File cp) {
    throw new UnexpectedException( new Exception("Method only implemented in JSR14 Compiler"));
  }

  /** Sets whether to allow assert statements. */
  public void setAllowAssertions(boolean allow) { _allowAssertions = allow; }
  
  /** Sets whether or not warnings are allowed. */
  public void setWarningsEnabled(boolean warningsEnabled) { _warningsEnabled = warningsEnabled; }
  
  private Context createContext(File[] sourceRoots) {
    Context _context = new Context();
    Options options = Options.instance(_context);
    options.putAll(CompilerOptions.getOptions(_warningsEnabled));
    //Should be setable some day?
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
    return _context;
  }

  /** Adds the appropriate value for the -d argument. */
  protected void _addSourceAndTargetOptions(Options options) {
    
    options.put("-source", "1.6");
    options.put("-target", "1.6");
    
    if (!_builtPath.equals("")) {
      options.put("-d", _builtPath);
    }
  }

  /** Package protected utility method for getting a properly formatted string with several source paths from an array
   *  of files.
   */
  private String getSourceRootString(File[] sourceRoots) {
    StringBuffer roots = new StringBuffer();
    for (int i = 0; i < sourceRoots.length; i++) {
      roots.append(sourceRoots[i].getAbsolutePath());
      if (i < sourceRoots.length - 1) {
        roots.append(File.pathSeparator);
      }
    }
    return roots.toString();
  }

   /* Creates an instance of the 6.0 javac compiler with a context that includes our diagnostic listener. */
   private void initCompiler(File[] sourceRoots) {
     
    _context = createContext(sourceRoots);
    _context.put(DiagnosticListener.class, diagnosticListener);
    
    _compiler = JavaCompiler.instance(_context);
//    Utilities.show("Compiler version " + _compiler.version() + " created");
  }
    
  public void setBuildDirectory(File dir) {
    if (dir == null) _builtPath = "";
    else _builtPath = dir.getAbsolutePath(); 
  }
}
