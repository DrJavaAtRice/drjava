package edu.rice.cs.plt.reflect;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.util.LinkedList;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.debug.DebugUtil;

/**
 * A class loader that mimics the standard application system loader by loading classes from
 * a file path of directories and jar files.
 */
public class PathClassLoader extends URLClassLoader {
  
  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(File... path) { this(IterUtil.asIterable(path)); }
  
  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(Iterable<? extends File> path) { super(makeURLs(path)); }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, File... path) { this(parent, IterUtil.asIterable(path)); }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, Iterable<? extends File> path) {
    super(makeURLs(path), parent);
  }
  
  private static URL[] makeURLs(Iterable<? extends File> path) {
    LinkedList<URL> result = new LinkedList<URL>();
    for (File f : path) {
      try { result.add(f.toURI().toURL()); }
      catch (IllegalArgumentException e) { DebugUtil.error.log(e); }
      catch (MalformedURLException e) { DebugUtil.error.log(e); }
      // just skip the path element if there's an error
    }
    return result.toArray(new URL[result.size()]);
  }
  
}
