package edu.rice.cs.drjava.util.classloader;

import java.util.*;

/**
 * A class loader that does nothing but allow, at runtime,
 * classes to be put on a list of "do not load" classes,
 * which will be rejected from loading, even if they are available.
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
