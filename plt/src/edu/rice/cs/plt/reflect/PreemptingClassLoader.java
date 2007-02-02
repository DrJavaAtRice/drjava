package edu.rice.cs.plt.reflect;

import java.util.Set;
import java.util.HashSet;
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
public class PreemptingClassLoader extends ClassLoader {
  
  private Set<String> _classNames;
  
  /**
   * @param parent  The source of all classes and resources to be loaded
   * @param classNames  A set of classes to be defined by this loader rather than a parent.
   *                    (To simplify the interface, all inner classes will be loaded by this
   *                    loader as well.)
   */
  public PreemptingClassLoader(ClassLoader parent, String... classNames) {
    this(parent, IterUtil.arrayIterable(classNames));
  }
  
  /**
   * @param parent  The source of all classes and resources to be loaded
   * @param classNames  A set of classes to be defined by this loader rather than a parent.
   *                    (To simplify the interface, all inner classes will be loaded by this
   *                    loader as well.)
   */
  public PreemptingClassLoader(ClassLoader parent, Iterable<? extends String> classNames) {
    super(parent);
    _classNames = new HashSet<String>();
    for (String s : classNames) { _classNames.add(s); }
  }
  
  /**
   * Load a class by following the standard search strategy specified by 
   * {@link ClassLoader#loadClass(String, boolean)}, with one exception: if the name is in the set
   * of classes to preemptively load, or if it represents an inner class of one of those classes
   * (that is, {@code name.startsWith(preemptName + "$")}), the corresponding class file will be
   * loaded as a resource and defined here rather than allowing the parent to define the class.
   */
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> result = findLoadedClass(name); // check if already loaded
    if (result == null) {
      if (shouldPreempt(name)) {
        String filename = name.replace('.', '/') + ".class";
        InputStream in = IOUtil.makeBuffered(getResourceAsStream(filename));
        if (in == null) { throw new ClassNotFoundException("Resource not found: " + filename); }
        try {
          byte[] data = IOUtil.toByteArray(in);
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
      else { result = getParent().loadClass(name); }
      // if no exceptions have occurred, result is no longer null
    }
    if (resolve) { resolveClass(result); }
    return result;
  }
  
  private boolean shouldPreempt(String name) {
    if (_classNames.contains(name)) { return true; }
    int dollar = name.indexOf('$');
    while (dollar != -1) {
      if (_classNames.contains(name.substring(0, dollar))) { return true; }
      dollar = name.indexOf('$', dollar + 1);
    }
    return false;
  }
  
}
