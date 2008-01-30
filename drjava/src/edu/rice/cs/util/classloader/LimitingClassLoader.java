/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

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
