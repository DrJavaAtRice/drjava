package edu.rice.cs.drjava.model.coverage;

import java.util.Map;
import java.util.HashMap;
import java.lang.ClassLoader;

import edu.rice.cs.util.Log;

/** A class loader that loads classes from in-memory data. */
public class MemoryClassLoader extends ClassLoader {
  
  private static final Log _log = new Log("JUnitTestManager.txt", true);
  
  public MemoryClassLoader(ClassLoader parent) { super(parent); }

    private final Map<String, byte[]> definitions = 
        new HashMap<String, byte[]>();

    /** Add a in-memory representation of a class. 
      * @param name  name of the class
      * @param bytes class definition
      */
    public void addDefinition(final String name, final byte[] bytes) {
        definitions.put(name, bytes);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
        final byte[] bytes = definitions.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        _log.log("Calling loadClass(" + name + ", " + resolve + ")");
        return super.loadClass(name, resolve);
    }
}


