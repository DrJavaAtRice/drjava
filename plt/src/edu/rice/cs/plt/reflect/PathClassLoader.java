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
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * A class loader that mimics the standard application system loader by loading classes from
 * a file path of directories and jar files.  This class also supports a <em>dynamic</em>
 * class path: an {@code Iterable} provided as input to the constructor is held, not copied,
 * and subsequent changes to that iterable are reflected in the path that is searched.  Of 
 * course, once a class is loaded, subsequent changes to the path will not change the class 
 * bound to that name.  The dynamic nature of the search path makes possible unusual errors --
 * a class may be valid in the path in which it is initially loaded, but when the JVM 
 * later transitively resolves the referenced classes, they may no longer exist, or may be
 * shadowed.  This is not a unique problem, however -- the standard system class loader is
 * based on an underlying file system that may also change in arbitrary ways at any time.
 */
public class PathClassLoader extends AbstractClassLoader {
  
  /**
   * Locate a resource in the given path.  Returns {@code null} if the resource is not found.
   * If multiple queries will be performed on the same path, a PathClassLoader instance
   * should be created for better performance.
   */
  public static URL getResourceInPath(String name, File... path) {
    return getResourceInPath(name, IterUtil.asIterable(path));
  }

  /**
   * Locate a resource in the given path.  Returns {@code null} if the resource is not found.
   * If multiple queries will be performed on the same path, a PathClassLoader instance
   * should be created for better performance.
   */
  public static URL getResourceInPath(String name, Iterable<File> path) {
    return new PathClassLoader(EmptyClassLoader.INSTANCE, path).getResource(name);
  }

  /**
   * Locate a resource in the given path.  Returns {@code null} if the resource is not found.
   * If multiple queries will be performed on the same path, a PathClassLoader instance
   * should be created for better performance.
   */
  public static InputStream getResourceInPathAsStream(String name, File... path) {
    return getResourceInPathAsStream(name, IterUtil.asIterable(path));
  }

  /**
   * Locate a resource in the given path.  Returns {@code null} if the resource is not found.
   * If multiple queries will be performed on the same path, a PathClassLoader instance
   * should be created for better performance.
   */
  public static InputStream getResourceInPathAsStream(String name, Iterable<File> path) {
    return new PathClassLoader(EmptyClassLoader.INSTANCE, path).getResourceAsStream(name);
  }
  

  private final Iterable<? extends File> _path;
  private URLClassLoader _urlLoader;
  private Iterable<File> _urlLoaderPath;

  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(File... path) { this(IterUtil.asIterable(path)); }
  
  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(Iterable<? extends File> path) {
    super();
    _path = path;
    updateURLLoader();
  }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, File... path) { this(parent, IterUtil.asIterable(path)); }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, Iterable<? extends File> path) {
    super(parent);
    _path = path;
    updateURLLoader();
  }

  private void updateURLLoader() {
    _urlLoaderPath = IterUtil.snapshot(_path);
    List<URL> urls = new LinkedList<URL>();
    for (File f : _urlLoaderPath) {
      try { urls.add(f.toURI().toURL()); }
      catch (IllegalArgumentException e) { error.log(e); }
      catch (MalformedURLException e) { error.log(e); }
      // just skip the path element if there's an error
    }
    _urlLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), EmptyClassLoader.INSTANCE);
  }
  
  @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
    URL resource = findResource(name.replace('.', '/') + ".class");
    if (resource == null) { throw new ClassNotFoundException(); }
    else {
      try {
        InputStream stream = resource.openStream();
        try {
          byte[] bytes = IOUtil.toByteArray(stream);
          Class<?> result = defineClass(name, bytes, 0, bytes.length);
          definePackageForClass(name);
          return result;
        }
        finally { stream.close(); }
      }
      catch (IOException e) { throw new ClassNotFoundException("Can't access class file", e); }
    }
  }
  
  @Override protected URL findResource(String name) {
    if (!IterUtil.isEqual(_path, _urlLoaderPath)) { updateURLLoader(); }
    return _urlLoader.findResource(name);
  }
  
  @Override protected Enumeration<URL> findResources(String name) throws IOException {
    if (!IterUtil.isEqual(_path, _urlLoaderPath)) { updateURLLoader(); }
    return _urlLoader.findResources(name);
  }
  
}
