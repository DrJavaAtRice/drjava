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

import java.util.LinkedList;

import com.sun.tools.javac.v8.JavaCompiler;

import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;

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

  private JavaCompiler _compiler;

  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */
  private OurLog _compilerLog;

  private JavacGJCompiler() {
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
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    _initCompiler(sourceRoot);
    List<String> filesToCompile = new List<String>();

    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }

    try {
      _compiler.compile(filesToCompile);
    }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      System.err.println("Compile error: " + t);
      t.printStackTrace();
      return new CompilerError[] {
        new CompilerError("Compile exception: " + t, false)
      };
    }

    return _compilerLog.getErrors();
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
    return "javac compiler (1.3+)";
  }

  public String toString() {
    return getName();
  }


  private void _initCompiler(File sourceRoot) {
    _compilerLog = new OurLog();

    // To use the GJ compiler, we build up the GJ options hashtable.
    Hashtable<String, String> options = Hashtable.make();

    // Enable GJ extensions
    options.put("-gj", "");

    options.put("-warnunchecked", "");

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // Set output classfile version to 1.1
    options.put("-target", "1.1");

    options.put("-sourcepath", sourceRoot.getAbsolutePath());

    _compiler = JavaCompiler.make(_compilerLog, options);
  }

  /** TODO: Add support for note(), whatever it is. */
  private class OurLog extends Log {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    /** JSR14 defines log constructor like this.
    /*  public Log(boolean promptOnError,
     *  boolean emitWarnings,
     *  PrintWriter errWriter,
     *  PrintWriter warnWriter,
     *  PrintWriter noticeWriter)
     */
    public OurLog() {
      //super(false, true, NULL_PRINT_WRITER,NULL_PRINT_WRITER,NULL_PRINT_WRITER);
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

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }
}
