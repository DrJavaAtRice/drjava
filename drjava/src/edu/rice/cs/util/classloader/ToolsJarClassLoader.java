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

package edu.rice.cs.util.classloader;

import java.net.*;
import java.io.File;

/**
 * A class loader that tries to load classes from tools.jar.
 * It will never delegate to the system loader.
 *
 * NOTE: I am not sure if this loader will work perfectly correctly
 * if you use loadClass. Currently its purpose is to be used from
 * {@link StickyClassLoader}, which just needs getResource.
 *
 * @version $Id$
 */
public class ToolsJarClassLoader extends URLClassLoader {
  public ToolsJarClassLoader() {
    super(_getURLs());
  }

  private static URL[] _getURLs() {
    File home = new File(System.getProperty("java.home"));
    File libDir = new File(home, "lib");
    File libDir2 = new File(home.getParentFile(), "lib");

    try {
      return new URL[] {
        new File(libDir, "tools.jar").toURL(),
        new File(libDir2, "tools.jar").toURL()
      };
    }
    catch (MalformedURLException e) {
      return new URL[0];
    }
  }

  /**
   * Gets the requested resource, bypassing the parent classloader.
   */
  public URL getResource(String name) {
    return findResource(name);
  }
}
