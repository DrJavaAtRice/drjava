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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;

/**
 * A compiler interface to find jsr14 v1.2 from the location
 * specified in Configuration.
 *
 * @version $Id$
 */
public class JSR14v12FromSetLocation extends CompilerProxy implements OptionConstants {
  
  /**
   * No longer a Singleton in order to re-determine the compiler's location multiple times.
   */
  public JSR14v12FromSetLocation() {
    super("edu.rice.cs.drjava.model.compiler.JSR14v12Compiler",
          _getClassLoader());
  }
  
  private static ClassLoader _getClassLoader() {
    File loc = DrJava.getConfig().getSetting(JSR14_LOCATION);
    if (loc == FileOption.NULL_FILE) {
      throw new RuntimeException("jsr14 location not set");
    }
    
    try {
      //URL url = new File(loc).toURL();
      URL url = loc.toURL();
      // Create a URLClassLoader with a null parent so it only looks
      // in the URL for classes (not in system's class loader).
      return new URLClassLoader(new URL[] { url }, null) {
        /**
         * Override getResource to not look at bootclasspath.
         */
        public URL getResource(String name) {
          return findResource(name);
        }
      };
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
