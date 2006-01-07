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

package edu.rice.cs.util.classloader;

import java.util.*;

/** A class loader that does nothing but allow, at runtime, classes to be put on a list of "do not load" 
 *  classes, which will be rejected from loading, even if they are available.
 *  @version $Id$
 */
public class LimitingClassLoader extends ClassLoader {
  private List<String> _restrictedList = new LinkedList<String>();

  /** Creates a LimitingClassLoader.
   *  @param parent Parent class loader, which is used to load all classes  not restricted from loading.
   */
  public LimitingClassLoader(ClassLoader parent) { super(parent); }

  public void addToRestrictedList(String name) { _restrictedList.add(name); }

  public void clearRestrictedList() { _restrictedList.clear(); }

  /** Overrides {@link ClassLoader#loadClass(String,boolean)} to reject classes whose names are on the 
   *  restricted list.
   *  @param name Name of class to load
   *  @param resolve If true then resolve the class
   *  @return {@link Class} object for the loaded class
   *  @throws ClassNotFoundException if name is on the restricted list, or if the parent class loader couldn't
   *          find the class.
   */
  protected Class<?> loadClass(String name, boolean resolve)
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
    Class<?> clazz = getParent().loadClass(name);

    // Because we couldn't call the protected loadClass(String,boolean)
    // on the parent, here we handle resolution if needed.
    if (resolve) {
      resolveClass(clazz);
    }

    return clazz;
  }
}
