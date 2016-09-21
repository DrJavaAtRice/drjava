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

import java.io.InputStream;
import java.io.IOException;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

/**
 * <p>A class loader that claims a set of classes available in its parent as its own.  This allows classes 
 * that would otherwise be loaded by a grandparent or distant ancestor to be loaded in a context that 
 * gives them access to all classes available to the parent.  For example, if a library must be loaded 
 * with a {@link PathClassLoader}, while an adapter for that library is available on the static class
 * path (without any custom class loading), loading the adapter directly will lead to errors &mdash; the 
 * library will not be visible to it.  When loaded instead by this class loader, the adapter can see both the 
 * application classes and the library classes.</p>
 * 
 * <p>The implementation is somewhat limited: the only way to access a class definition in an arbitrary class 
 * loader without actually <em>defining</em> that class is to invoke {@link ClassLoader#getResource} with
 * the name of a {@code .class} file.  To work correctly, then, all preempted classes must be available 
 * as class files via {@code parent.getResource(classFileName)}.  Most class loaders (especially those 
 * that simply load class files from some repository) follow the convention that a class will be loaded 
 * only if a resource with the appropriate class file name exists.  But there is no guarantee that all 
 * class loaders (such as those that generate class definitions on the fly) will follow this convention.</p>  
 */
public class PreemptingClassLoader extends AbstractClassLoader {
  
  private Iterable<String> _prefixes;
  
  /**
   * @param parent  The source of all classes and resources to be loaded
   * @param prefixes  A set of class name prefixes for which all matching classes will be defined by 
   *                  this loader rather than a parent.  Each prefix must be a package or class name
   *                  (partial names, like {@code "java.lang.Stri"}, will not match the full class name).
   */
  public PreemptingClassLoader(ClassLoader parent, String... prefixes) {
    this(parent, IterUtil.asIterable(prefixes));
  }
  
  /**
   * @param parent  The source of all classes and resources to be loaded
   * @param prefixes  A set of class name prefixes for which all matching classes will be defined by 
   *                  this loader rather than a parent.  Each prefix must be a package or class name
   *                  (partial names, like {@code "java.lang.Stri"}, will not match the full class name).
   */
  public PreemptingClassLoader(ClassLoader parent, Iterable<? extends String> prefixes) {
    super(parent);
    _prefixes = IterUtil.snapshot(prefixes);
  }
  
  /**
   * Load a class by following the standard search strategy specified by 
   * {@link ClassLoader#loadClass(String, boolean)}, with one exception: if the name matches the set
   * of prefixes to preemptively load, the corresponding class file will be
   * loaded as a resource and defined here rather than allowing the parent to define the class.
   */
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (!shouldPreempt(name)) { return super.loadClass(name, resolve); }
    else {
      Class<?> result = findLoadedClass(name); // check if already loaded
      if (result == null) {
        String filename = name.replace('.', '/') + ".class";
        InputStream in = IOUtil.asBuffered(getResourceAsStream(filename));
        if (in == null) { throw new ClassNotFoundException("Resource not found: " + filename); }
        try {
          byte[] data = IOUtil.toByteArray(in);

          // define package
          definePackageForClass(name);
          
          result = defineClass(name, data, 0, data.length);
        }
        catch (IOException e) {
          throw new ClassNotFoundException("Error in reading " + filename, e);
        }
        finally {
          try { in.close(); }
          catch (IOException e) { /* ignore */ }
        }
      }
      // if no exceptions have occurred, result is no longer null
      if (resolve) { resolveClass(result); }
      return result;
    }
  }
  
  private boolean shouldPreempt(String name) {
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
