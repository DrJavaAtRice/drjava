/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
import gj.util.Vector;
import gj.util.Enumeration;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
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
  
  /**
   * put your documentation comment here
   */
  private class OurLog extends Log {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    /**
     * put your documentation comment here
     * @param source
     * @return
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
     * @return
     */
    public CompilerError[] getErrors() {
      return  (CompilerError[])_errors.toArray(new CompilerError[0]);
    }
  }
}



