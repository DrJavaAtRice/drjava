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
import java.net.URL;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.classloader.StickyClassLoader;

/**
 * A compiler interface to search a given 
 * @version $Id$
 */
public class CompilerProxy implements CompilerInterface {
  /**
   * The actual compiler interface. If it's null, we couldn't load it.
   */
  private CompilerInterface _realCompiler = null;

  private final String _className;
  private final ClassLoader _newLoader;

  /**
   * These classes will always be loaded using the previous classloader.
   * This is important to make sure there is only one instance of them, so
   * their values can be freely passed about the program.
   */
  private static final String[] _useOldLoader = {
    "edu.rice.cs.drjava.model.Configuration",
    "edu.rice.cs.drjava.model.compiler.CompilerInterface",
    "edu.rice.cs.drjava.model.compiler.CompilerError"
  };

  /**
   * A proxy compiler interface that tries to load the given class
   * from one of the given locations. It uses its own classloader, which will
   * even allow loading a second instance of the class!
   *
   * @param className Implementation of {@link CompilerInterface} to proxy for.
   * @param loader Classloader to use
   */

  public CompilerProxy(String className,
                       ClassLoader newLoader)
  {
    _className = className;
    _newLoader = newLoader;

    _recreateCompiler();
  }

  private void _recreateCompiler() {
    StickyClassLoader loader =
      new StickyClassLoader(_newLoader,
                            getClass().getClassLoader(),
                            _useOldLoader);

    try {
      Class c = loader.loadClass(_className);
      _realCompiler = CompilerRegistry.createCompiler(c);
      //DrJava.consoleErr().println("real compiler: " + _realCompiler + " this: " + this);
    }
    catch (Throwable t) {
      // don't do anything. realCompiler stays null.
      //DrJava.consoleErr().println("loadClass fails: " + t);
      //t.printStackTrace(DrJava.consoleErr());
    }
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
    //DrJava.consoleErr().println("proxy to compile: " + files[0]);

    CompilerError[] ret =  _realCompiler.compile(sourceRoot, files);
    _recreateCompiler();
    return ret;
  }

  /**
   * Indicates whether this compiler is actually available.
   * As in: Is it installed and located?
   * This method should load the compiler class, which should
   * hopefully prove whether the class can load.
   * If this method returns true, the {@link #compile} method
   * should not fail due to class not being found.
   */
  public boolean isAvailable() {
    if (_realCompiler == null) {
      return false;
    }
    else {
      return _realCompiler.isAvailable();
    }
  }

  /**
   * Returns the name of this compiler, appropriate to show to the user.
   */
  public String getName() {
    if (!isAvailable()) {
      return "(unavailable)";
    }

    return _realCompiler.getName();
  }

  /** Should return info about compiler, at least including name. */
  public String toString() {
    return getName();
  }
}



