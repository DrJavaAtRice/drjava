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

import java.util.*;

/**
 * A class loader that does nothing but allow, at runtime,
 * classes to be put on a list of "do not load" classes,
 * which will be rejected from loading, even if they are available.
 *
 * @version $Id$
 */
public class LimitingClassLoader extends ClassLoader {
  private List _restrictedList = new LinkedList();

  /**
   * Creates a LimitingClassLoader.
   * @param parent Parent class loader, which is used to load all classes
   *               not restricted from loading.
   */
  public LimitingClassLoader(ClassLoader parent) {
    super(parent);
  }

  public void addToRestrictedList(String name) {
    _restrictedList.add(name);
  }

  public void clearRestrictedList() {
    _restrictedList.clear();
  }

  /**
   * Overrides {@link ClassLoader#loadClass(String,boolean)} to
   * reject classes whose names are on the restricted list.
   * 
   * @param name Name of class to load
   * @param resolve If true then resolve the class
   *
   * @return {@link Class} object for the loaded class
   * @throws ClassNotFoundException if name is on the restricted list,
   *                                or if the parent class loader couldn't
   *                                find the class.
   */
  protected Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    ListIterator itor = _restrictedList.listIterator();

    while (itor.hasNext()) {
      String current = (String) itor.next();
      if (current.equals(name)) {
        throw new ClassNotFoundException("Class " + name +
                                         " is on the restricted list.");
      }
    }

    // If we got here, the class was not restricted.
    Class clazz = getParent().loadClass(name);

    // Because we couldn't call the protected loadClass(String,boolean)
    // on the parent, here we handle resolution if needed.
    if (resolve) {
      resolveClass(clazz);
    }

    return clazz;
  }
}
