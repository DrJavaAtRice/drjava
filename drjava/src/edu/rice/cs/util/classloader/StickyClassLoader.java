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

package edu.rice.cs.util.classloader;

import java.util.Arrays;
import java.net.URL;
import java.io.IOException;

import edu.rice.cs.util.FileOps;

/**
 * Sticky class loader.
 *
 * @version $Id$
 */
public class StickyClassLoader extends ClassLoader {
  private final ClassLoader _newLoader;
  private final String[] _classesToLoadWithOld;

  public StickyClassLoader(final ClassLoader newLoader,
                           final ClassLoader oldLoader)
  {
    this(newLoader, oldLoader, new String[0]);
  }

  public StickyClassLoader(final ClassLoader newLoader,
                           final ClassLoader oldLoader,
                           final String[] classesToLoadWithOld)
  {
    super(oldLoader);
    _newLoader = newLoader;
    _classesToLoadWithOld = (String[]) classesToLoadWithOld.clone();
    Arrays.sort(_classesToLoadWithOld);
  }

  /**
   * Gets the requested resource, looking first in the new loader
   * and then in the old loader.
   *
   * @param name Name of resource to find
   * @return URL of the resource if found/accessible, or null if not.
   */
  public URL getResource(String name) {
    URL resource = _newLoader.getResource(name);

    if (resource == null) {
      resource = getParent().getResource(name);
    }

    //System.err.println("resource: " + name + " --> " + resource);

    return resource;
  }

  /**
   * Loads the given class, delegating first to the new class loader and
   * then second to the old class loader. The returned Class object will have
   * its ClassLoader ({@link Class#getClassLoader}) set to be this. This is very
   * important because it causes classes that are loaded due to this class 
   * being loaded (ancestor classes/interfaces, referenced classes, etc) to
   * use the same loader.
   *
   * There are a few exceptions to this explanation:
   * <OL>
   * <LI>If the class is in java.* or javax.*, it will be loaded using
   *     {@link ClassLoader#getSystemClassLoader}. This is because only
   *     the system loader is allowed to load system classes!
   *     Also: sun.*.
   * </LI>
   * <LI>If the class name is in the list of classes to load with the
   *     old class loader (passed to constructor), the new loader is not
   *     considered when trying to load the class.
   *     This is useful to make sure that certain classes (or interfaces)
   *     only have one copy in the system, to ensure that you can cast to that
   *     class/interface regardless of which loader loaded the other class.
   * </LI>
   * </OL>
   */
  protected Class loadClass(String name, boolean resolve) 
  throws ClassNotFoundException
  {
    Class clazz;
    
    if (name.startsWith("java.") ||
        name.startsWith("javax.") ||
        name.startsWith("sun."))
    {
      clazz = getSystemClassLoader().loadClass(name);
    }
    else if (Arrays.binarySearch(_classesToLoadWithOld, name) >= 0) {
      clazz = getParent().loadClass(name);
    }
    else {
      // we get the data using getResource because if we just delegate
      // the call to loadClass on old or new loader, it will use that
      // loader as the associated class loader for the class. that's bad.
      try {
        String fileName = name.replace('.', '/') + ".class";

        URL resource = getResource(fileName);
        if (resource == null) {
          throw new ClassNotFoundException("Resource not found: " + fileName);
        }

        byte[] data = FileOps.readStreamAsBytes(resource.openStream());
        clazz = defineClass(name, data, 0, data.length);
      }
      catch (IOException ioe) {
        throw new ClassNotFoundException(ioe.toString());
      }
    }

    if (resolve) {
      resolveClass(clazz);
    }

    return clazz;
  }
}
