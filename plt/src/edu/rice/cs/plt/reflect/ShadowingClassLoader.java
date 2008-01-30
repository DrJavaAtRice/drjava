/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.net.URL;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

/**
 * A class loader that prevents the loading of a set of classes and related resources.  This allows classes 
 * with the same name (but perhaps a different implementation) to be cleanly loaded by a child loader.
 */
public class ShadowingClassLoader extends ClassLoader {
  
  private Iterable<String> _prefixes;
  
  /**
   * @param parent  The parent loader
   * @param prefixes  A set of class name prefixes for which all matching classes will <em>not</em> be loaded
   *                  by {@code parent}.  Each prefix must be a package or class name (partial names, like 
   *                  {@code "java.lang.Stri"}, will not match the full class name).
   */
  public ShadowingClassLoader(ClassLoader parent, String... prefixes) {
    this(parent, IterUtil.asIterable(prefixes));
  }
  
  /**
   * @param parent  The parent loader
   * @param prefixes  A set of class name prefixes for which all matching classes will <em>not</em> be loaded
   *                  by {@code parent}.  Each prefix must be a package or class name (partial names, like 
   *                  {@code "java.lang.Stri"}, will not match the full class name).
   */
  public ShadowingClassLoader(ClassLoader parent, Iterable<? extends String> prefixes) {
    super(parent);
    _prefixes = IterUtil.snapshot(prefixes);
  }
  
  /**
   * If the given class matches the list of prefixes to shadow, a {@code ClassNotFoundException} will
   * occur; otherwise, the method delegates to the parent class loader.
   */
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (shouldShadow(name)) { throw new ClassNotFoundException(name + " is being shadowed"); }
    else {
      Class<?> result = getParent().loadClass(name);
      if (resolve) { resolveClass(result); }
      return result;
    }
  }
  
  public URL getResource(String name) {
    if (shouldShadow(name.replace('/', '.'))) { return null; }
    else { return getParent().getResource(name); }
  }
  
  // Ideally, we should override getResources() as well.  Unfortunately, it's final in Java 1.4.
  
  private boolean shouldShadow(String name) {
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
