package edu.rice.cs.drjava.model.coverage;

import java.util.Map;
import java.util.HashMap;
import java.lang.ClassLoader;

import edu.rice.cs.util.Log;

/** A class loader that loads classes from in-memory data. The parent class loader must be a ShadowingClassLoader
  * that fails to load classes for the class names in memory.  */
public class MemoryClassLoader extends ClassLoader {
  
  private static final Log _log = new Log("JUnitTestManager.txt", true);
  
  private ClassLoader _parent;
  public MemoryClassLoader(ClassLoader parent) { 
    super(parent); 
    _parent = parent;
    _log.log("Creating MemoryClassLoader with parent = " + parent);
  }
  
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    _log.log("MemoryClassLoader.loadClass(" + name + ", " + resolve + ") called");
    return super.loadClass(name, resolve);
  }
  
  private final Map<String, byte[]> definitions =  new HashMap<String, byte[]>();
  
  /** Add a in-memory representation of a class. 
    * @param name  name of the class
    * @param bytes array containing class definition
    */
  public void addDefinition(final String name, final byte[] bytes) {
    definitions.put(name, bytes);
  }
  
  /** Looks for a class in memory before delegating to the parent class loader to find the class.  */
  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    _log.log("MemoryClassLoader.findClass(" + name + ", " + name + ") called");
    final byte[] bytes = definitions.get(name);
    if (bytes != null) {
      _log.log("MemoryClassLoader is loading class " + name + " from memory");
      return defineClass(name, bytes, 0, bytes.length); // converts bytes[0:bytes.length) to a Class<?> object
    }
    _log.log("MemoryClassLoader is throwing a ClassNotFoundException");
    throw new ClassNotFoundException("class " + name + " not found by MemoryClassLoader with class definitions for " +
                                     definitions.keySet());                         
  } 
}


