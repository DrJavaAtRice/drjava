/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.reflect;

import java.net.URL;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A class loader that hides a set of classes and related resources.  This allows classes 
  * with the same name (but perhaps a different implementation) to be cleanly loaded by a child loader.
  */
public class ShadowingClassLoader extends ClassLoader {
  
  private final Iterable<? extends String> _prefixes;
  private final boolean _blackList;
  private final boolean _filterBootClasses;
  
  /**
   * Create a ShadowingClassLoader that will hide non-bootstrap classes matching the given prefixes.
   * @param parent  The parent loader
   * @param excludePrefixes  A set of class name prefixes to match.  Each prefix must be a package or class
   *                         name (partial names, like {@code "java.lang.Stri"}, will not match the full class
   *                         name).
   */
  public static ShadowingClassLoader blackList(ClassLoader parent, String... excludePrefixes) {
    return new ShadowingClassLoader(parent, true, IterUtil.asIterable(excludePrefixes), false);
  }
    
  /**
   * Create a ShadowingClassLoader that will hide all non-bootstrap classes except those matching the
   * given prefixes.
   * @param parent  The parent loader
   * @param includePrefixes  A set of class name prefixes to match.  Each prefix must be a package or class
   *                         name (partial names, like {@code "java.lang.Stri"}, will not match the full class
   *                         name).
   */
  public static ShadowingClassLoader whiteList(ClassLoader parent, String... includePrefixes) {
    return new ShadowingClassLoader(parent, false, IterUtil.asIterable(includePrefixes), false);
  }
  
  /**
   * @param parent  The parent loader
   * @param blackList  If {@code true}, classes matching {@code prefixes} are prevented from loading; otherwise,
   *                   all classes <em>except</em> those matching {@code prefixes} are prevented from loading.
   * @param prefixes  A set of class name prefixes to which class names will be compared.  Each prefix must
   *                  be a package or class name (partial names, like {@code "java.lang.Stri"}, will not 
   *                  match the full class name).
   * @param filterBootClasses  Whether classes and resources available to the bootstrap class loader should
   *                           be hidden.  If {@code true}, care must be taken to ensure that essential
   *                           classes (like {@code java.lang.Object}) are available when needed.
   */
  public ShadowingClassLoader(ClassLoader parent, boolean blackList, Iterable<? extends String> prefixes,
                              boolean filterBootClasses) {
    super(parent);
    _blackList = blackList;
    _prefixes = prefixes;
    _filterBootClasses = filterBootClasses;
  }
  
  /** If the given class is shadowed, a {@code ClassNotFoundException} will
    * occur; otherwise, the method delegates to the parent class loader.
    */
  @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if ((_filterBootClasses || !isBootClass(name)) && matchesPrefixes(name) == _blackList) {
      throw new ClassNotFoundException(name + " is being shadowed");
    }
    else {
      // can't use getParent().loadClass(name) because parent may be null
      return super.loadClass(name, resolve);
    }
  }
  
  @Override public URL getResource(String name) {
    if ((_filterBootClasses || !isBootResource(name)) &&
        matchesPrefixes(name.replace('/', '.')) == _blackList) {
      return null;
    }
    else {
      // can't use getParent().getResource(name) because parent may be null
      return super.getResource(name);
    }
  }
  
  // Ideally, we should override getResources() as well.  Unfortunately, it's final in Java 1.4.
  
  /** As a side effect, loads the given class in the bootstrap class loader. */
  private boolean isBootClass(String name) {
    try { ReflectUtil.BOOT_CLASS_LOADER.loadClass(name); return true; }
    catch (ClassNotFoundException e) { return false; }
  }
  
  private boolean isBootResource(String name) {
    return ReflectUtil.BOOT_CLASS_LOADER.getResource(name) != null;
  }
  
  private boolean matchesPrefixes(String name) {
    // TODO: improve efficiency by using a sorted data structure
    for (String p : _prefixes) {
      if (name.startsWith(p)) {
        if (name.equals(p) || name.startsWith(p + ".") || name.startsWith(p + "$")) {
          return true;
        }
      }
    }
    return false;
  }
  
}
