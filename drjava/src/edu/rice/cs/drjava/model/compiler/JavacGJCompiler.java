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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.LinkedList;

import com.sun.tools.javac.v8.JavaCompiler;

import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;

import edu.rice.cs.drjava.model.Configuration;
import edu.rice.cs.util.FileOps;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * javac, provided that this is a javac that is based on GJ. This is the case
 * for javac in JDK 1.3+, as well as when using the JSR14 prototype compiler.
 *
 * @version $Id$
 */
public class JavacGJCompiler implements CompilerInterface {
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new JavacGJCompiler();

  public static final String COMPILER_CLASS_NAME =
    "com.sun.tools.javac.v8.JavaCompiler";

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
  protected OurLogI compilerLog;

  protected JavacGJCompiler() {
  }

  /*
  private boolean _testIfSupportsGenerics() {
    try {
      // make a temp source file with generics
      File file = FileOps.writeStringToNewTempFile("drjava",
                                                   ".java",
                                                   "class x<T> {}");

      // then try to compile it
      CompilerError[] errors = compile(file.getParentFile(), new File[] { file });

      file.delete();
      File classFile = new File(file.getParentFile(), "x.class");
      classFile.delete();

      return (errors.length == 0);
    }
    catch (IOException ioe) {
      // should never happen, but temp file couldn't be created
      return false;
    }
  }
  */


  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    initCompiler(sourceRoot);
    List<String> filesToCompile = new List<String>();

    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }

    try {
      compiler.compile(filesToCompile);
    }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      System.err.println("Compile error: " + t);
      t.printStackTrace();
      return new CompilerError[] {
        new CompilerError("Compile exception: " + t, false)
      };
    }

    return compilerLog.getErrors();
  }

  public boolean isAvailable() {
    try {
      Class.forName(COMPILER_CLASS_NAME);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  public String getName() {
    return "javac";
    /*
    if (_supportsGenerics) {
      return "javac (GJ)";
    }
    else {
      return "javac";
    }
    */
  }

  public String toString() {
    return getName();
  }

  protected Hashtable<String, String> createOptionsHashtable(File sourceRoot) {
    Hashtable<String, String> options = Hashtable.make();

    options.put("-warnunchecked", "");

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // turn on generics, if we have em
    options.put("-gj", "");

    // Set output classfile version to 1.1
    options.put("-target", "1.1");

    options.put("-sourcepath", sourceRoot.getAbsolutePath());

    String cp = System.getProperty("java.class.path");

    // add extra classpath to the classpath
    String[] ecp = Configuration.ONLY.getExtraClasspath();
    for (int i = 0; i < ecp.length; i++) {
      cp += System.getProperty("path.separator") + ecp[i];
    }

    options.put("-classpath", cp);

    return options;
  }

  protected void initCompiler(File sourceRoot) {
    Hashtable<String, String> options = createOptionsHashtable(sourceRoot);

    // sigh, the 1.4 log won't work on 1.3 so we need to try the 1.4 first
    // and fall back to the 1.3
    try {
      compilerLog = new OurLog14();
    }
    catch (NoSuchMethodError error) {
      compilerLog = new OurLog13();
    }

    //System.err.println(this + ": log=" + compilerLog);
    compiler = JavaCompiler.make((Log) compilerLog, options);
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.3.
   * (The other version only works on 1.4 and later since they
   * changed the constructors!)
   */
  private class OurLog13 extends Log implements OurLogI {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog13() {
      super(false, true);
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

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void print(String s) {}

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void println(String s) {}

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4 and later.
   * It won't work on 1.3 since the 5-arg constructor didn't exist!
   */
  private class OurLog14 extends Log implements OurLogI {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog14() {
      super(false, true, NULL_PRINT_WRITER, NULL_PRINT_WRITER, NULL_PRINT_WRITER);
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

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void print(String s) {}

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void println(String s) {}

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }

  private interface OurLogI {
    public CompilerError[] getErrors();
  }
}
