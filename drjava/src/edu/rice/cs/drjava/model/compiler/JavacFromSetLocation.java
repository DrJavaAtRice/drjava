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
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import edu.rice.cs.drjava.model.Configuration;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;

/**
 * A compiler interface to find Javac from the location
 * specified in Configuration.
 *
 * @version $Id$
 */
public class JavacFromSetLocation extends CompilerProxy {
  // To implement #523222, we had to make this not a singleton,
  // to allow it to re-determine the location of the compiler multiple times.
  //public static final CompilerInterface ONLY = new JavacFromSetLocation();

  /** Private constructor due to singleton. */
  public JavacFromSetLocation() {
    super("edu.rice.cs.drjava.model.compiler.JavacGJCompiler",
          _getClassLoader());
  }

  private static ClassLoader _getClassLoader() {
    String loc = Configuration.ONLY.getJavacLocation();
    if (loc == null) {
      throw new RuntimeException("javac location not set");
    }

    try {
      URL url = new File(loc).toURL();
      return new URLClassLoader(new URL[] { url });
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("malformed url exception");
    }
  }

  /**
   * Returns the name of this compiler, appropriate to show to the user.
   */
  public String getName() {
    return super.getName() + " (user)";
  }
}
