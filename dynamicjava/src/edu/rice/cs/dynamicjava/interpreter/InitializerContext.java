package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** The body of an initializer. */
public class InitializerContext extends DelegatingContext {
  
  private final boolean _isStatic;
  private final DJClass _c;
  
  public InitializerContext(TypeContext next, boolean isStatic, DJClass c) {
    super(next);
    _isStatic = isStatic;
    _c = c;
  }
  
  protected InitializerContext duplicate(TypeContext next) {
    return new InitializerContext(next, _isStatic, _c);
  }
  
  @Override public String makeClassName(String n) {
    return super.makeAnonymousClassName() + n;
  }
  
  @Override public DJClass getThis() {
    if (_isStatic) { return null; }
    else { return super.getThis(); }
  }
  
  @Override public DJClass getThis(String className) {
    if (_isStatic) { return null; }
    else { return super.getThis(className); }
  }
  
  @Override public DJClass getThis(Type expected, TypeSystem ts) {
    if (_isStatic) { return null; }
    else { return super.getThis(expected, ts); }
  }
  
  @Override public DJClass initializingClass() { return _c; }
  
  @Override public Type getReturnType() { return null; }
  
  @Override public Iterable<Type> getDeclaredThrownTypes() { return IterUtil.empty(); }

}
