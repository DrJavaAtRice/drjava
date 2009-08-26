package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.dynamicjava.symbol.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** The context following a local class, variable, or function definition. */
public class LocalContext extends DelegatingContext {
  
  // may be null, indicating that the enclosing loader should be used (_classes should then be empty)
  // a cleaner approach would be to define different context types for classes/vars/functions...
  private ClassLoader _loader;
  private Iterable<DJClass> _classes;
  private Iterable<LocalVariable> _vars;
  private Iterable<LocalFunction> _functions;
  
  public LocalContext(TypeContext next, ClassLoader loader, Iterable<DJClass> classes, 
                      Iterable<LocalVariable> vars, Iterable<LocalFunction> functions) {
    super(next);
    _loader = loader;
    _classes = classes;
    _vars = vars;
    _functions = functions;
  }
  
  public LocalContext(TypeContext next, Iterable<LocalVariable> vars) {
    this(next, null, IterUtil.<DJClass>empty(), vars, IterUtil.<LocalFunction>empty());
  }
  
  public LocalContext(TypeContext next, LocalVariable var) {
    this(next, null, IterUtil.<DJClass>empty(), IterUtil.singleton(var),IterUtil.<LocalFunction>empty());
  }
  
  public LocalContext(TypeContext next, ClassLoader loader, DJClass c) {
    this(next, loader, IterUtil.singleton(c), IterUtil.<LocalVariable>empty(), IterUtil.<LocalFunction>empty());
  }
  
  public LocalContext(TypeContext next, LocalFunction f) {
    this(next, null, IterUtil.<DJClass>empty(), IterUtil.<LocalVariable>empty(), IterUtil.singleton(f));
  }
  
  protected LocalContext duplicate(TypeContext next) {
    return new LocalContext(next, _loader, _classes, _vars, _functions);
  }
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  @Override public boolean typeExists(String name, TypeSystem ts) {
    return declaredClass(name) != null || super.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope top-level class */
  @Override public boolean topLevelClassExists(String name, TypeSystem ts) {
    return declaredClass(name) != null || super.topLevelClassExists(name, ts);
  }
  
  /** Return the top-level class with the given name, or {@code null} if it does not exist. */
  @Override public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    DJClass result = declaredClass(name);
    return result == null ? super.getTopLevelClass(name, ts) : result;
  }
  
  private DJClass declaredClass(String name) {
    for (DJClass c : _classes) {
      if (!c.isAnonymous() && c.declaredName().equals(name)) { return c; }
    }
    return null;
  }
  
  /** Test whether {@code name} is an in-scope field or local variable */
  @Override public boolean variableExists(String name, TypeSystem ts) {
    return declaredVariable(name) != null || super.variableExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope local variable */
  @Override public boolean localVariableExists(String name, TypeSystem ts) {
    return declaredVariable(name) != null || super.localVariableExists(name, ts);
  }
  
  /** Return the variable object for the given name, or {@code null} if it does not exist. */
  @Override public LocalVariable getLocalVariable(String name, TypeSystem ts) {
    LocalVariable result = declaredVariable(name);
    return result == null ? super.getLocalVariable(name, ts) : result;
  }
  
  private LocalVariable declaredVariable(String name) {
    for (LocalVariable v : _vars) {
      if (v.declaredName().equals(name)) { return v; }
    }
    return null;
  }
  
  @Override public boolean functionExists(String name, TypeSystem ts) {
    return hasFunction(name) || super.functionExists(name, ts);
  }
  
  @Override public boolean localFunctionExists(String name, TypeSystem ts) {
    return hasFunction(name) || super.localFunctionExists(name, ts);
  }
  
  private boolean hasFunction(String name) {
    for (LocalFunction f : _functions) {
      if (f.declaredName().equals(name)) { return true; }
    }
    return false;
  }
  
  @Override public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts,
                                                             Iterable<LocalFunction> partial) {
    Iterable<LocalFunction> newPartial = partial;
    for (LocalFunction f : _functions) {
      if (f.declaredName().equals(name)) { newPartial = IterUtil.compose(partial, f); }
    }
    return super.getLocalFunctions(name, ts, newPartial);
  }
  
  @Override public ClassLoader getClassLoader() {
    if (_loader == null) { return super.getClassLoader(); }
    else { return _loader; }
  }
  
}
