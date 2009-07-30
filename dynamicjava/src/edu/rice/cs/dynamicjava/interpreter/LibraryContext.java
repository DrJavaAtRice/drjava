package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.DJClass;
import edu.rice.cs.dynamicjava.symbol.Library;
import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.plt.iter.IterUtil;

/** A context that can access top-level classes from a {@link Library}. */
public class LibraryContext extends DelegatingContext {
  
  private final Library _library;
  
  /** Create a LibraryContext that delegates to the given context (typically another LibraryContext). */
  public LibraryContext(TypeContext next, Library lib) {
    super(next);
    _library = lib;
  }
  
  /** Create a LibraryContext that delegates to the BaseContext. */
  public LibraryContext(Library lib) { this(BaseContext.INSTANCE, lib); }

  protected TypeContext duplicate(TypeContext next) {
    return new LibraryContext(next, _library);
  }

  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts) {
    return !(IterUtil.isEmpty(_library.declaredClasses(name))) ||
           super.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope top-level class */
  public boolean topLevelClassExists(String name, TypeSystem ts) {
    return !(IterUtil.isEmpty(_library.declaredClasses(name))) ||
           super.topLevelClassExists(name, ts);
  }
  
  /** Return the top-level class with the given name, or {@code null} if it does not exist. */
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    Iterable<DJClass> matches = _library.declaredClasses(name);
    int size = IterUtil.sizeOf(matches, 2);
    switch (size) {
      case 0: return super.getTopLevelClass(name, ts);
      case 1: return IterUtil.first(matches);
      default: throw new AmbiguousNameException();
    }
  }
  
  public ClassLoader getClassLoader() { return _library.classLoader(); }
}
