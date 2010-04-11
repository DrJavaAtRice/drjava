package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class Java5Library implements Library {
  
  private final ClassLoader _loader;
  
  public Java5Library(ClassLoader loader) {
    _loader = loader;
  }

  public Iterable<DJClass> declaredClasses(String fullName) {
    try { Class<?> c = _loader.loadClass(fullName); return IterUtil.<DJClass>singleton(new Java5Class(c)); }
    catch (ClassNotFoundException e) { return IterUtil.empty(); }
    catch (LinkageError e) { return IterUtil.empty(); }
  }

  public ClassLoader classLoader() { return _loader; }
}
