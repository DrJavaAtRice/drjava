package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.plt.iter.IterUtil;

public class JavaLibrary implements Library {
  
  private final ClassLoader _loader;
  
  public JavaLibrary(ClassLoader loader) {
    _loader = loader;
  }

  public Iterable<DJClass> declaredClasses(String fullName) {
    try { Class<?> c = _loader.loadClass(fullName); return IterUtil.<DJClass>singleton(new JavaClass(c)); }
    catch (ClassNotFoundException e) { return IterUtil.empty(); }
    catch (LinkageError e) { return IterUtil.empty(); }
  }

}
