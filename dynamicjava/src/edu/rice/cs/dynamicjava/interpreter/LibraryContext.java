package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.DJClass;
import edu.rice.cs.dynamicjava.symbol.Library;
import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;
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

  @Override public boolean typeExists(String name, TypeSystem ts) {
    return hasClass(name) || super.typeExists(name, ts);
  }
  
  @Override public boolean topLevelClassExists(String name, TypeSystem ts) {
    return hasClass(name) || super.topLevelClassExists(name, ts);
  }
  
  @Override public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    Iterable<DJClass> matches = _library.declaredClasses(name);
    int size = IterUtil.sizeOf(matches, 2);
    switch (size) {
      case 0: return super.getTopLevelClass(name, ts);
      case 1: return IterUtil.first(matches);
      default: throw new AmbiguousNameException();
    }
  }
  
  @Override public boolean memberClassExists(String name, TypeSystem ts) {
    return hasClass(name) ? false : super.memberClassExists(name, ts);
  }
  
  @Override public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return hasClass(name) ? null : super.typeContainingMemberClass(name, ts);
  }
  
  @Override public boolean typeVariableExists(String name, TypeSystem ts) {
    return hasClass(name) ? false : super.typeVariableExists(name, ts);
  }
  
  @Override public VariableType getTypeVariable(String name, TypeSystem ts) {
    return hasClass(name) ? null : super.getTypeVariable(name, ts);
  }

  private boolean hasClass(String name) {
    return !IterUtil.isEmpty(_library.declaredClasses(name));
  }
  
  @Override public ClassLoader getClassLoader() { return _library.classLoader(); }
}
