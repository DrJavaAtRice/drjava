package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.Type;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * The context of a function's (method, constructor, or local function) body, introducing the
 * function's parameters.
 */
public class FunctionContext extends DelegatingContext {
  
  private final Function _f;
  
  public FunctionContext(TypeContext next, Function f) {
    super(next);
    _f = f;
  }
  
  protected FunctionContext duplicate(TypeContext next) {
    return new FunctionContext(next, _f);
  }
  
  @Override public boolean variableExists(String name, TypeSystem ts) {
    return getParameter(name) != null || super.variableExists(name, ts);
  }
  
  @Override public boolean localVariableExists(String name, TypeSystem ts) {
    return getParameter(name) != null || super.localVariableExists(name, ts);
  }
  
  @Override public LocalVariable getLocalVariable(String name, TypeSystem ts) {
    LocalVariable result = getParameter(name);
    return result == null ? super.getLocalVariable(name, ts) : result;
  }
  
  @Override public boolean fieldExists(String name, TypeSystem ts) {
    return (getParameter(name) == null) ? super.fieldExists(name, ts) : false;
  }
  
  @Override public ClassType typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException {
    return (getParameter(name) == null) ? super.typeContainingField(name, ts) : null;
  }
  
  private LocalVariable getParameter(String name) {
    for (LocalVariable v : _f.parameters()) {
      if (v.declaredName().equals(name)) { return v; }
    }
    return null;
  }

  @Override public boolean functionExists(String name, TypeSystem ts) {
    return isLocalFunction(name) || super.functionExists(name, ts);
  }
  
  @Override public boolean localFunctionExists(String name, TypeSystem ts) {
    return isLocalFunction(name) || super.localFunctionExists(name, ts);
  }
  
  @Override public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts,
                                                             Iterable<LocalFunction> partial) {
    if (IterUtil.isEmpty(partial)) {
      if (isLocalFunction(name)) { partial = IterUtil.singleton((LocalFunction) _f); }
      return super.getLocalFunctions(name, ts, partial);
    }
    else { return partial; }
  }
  
  @Override public boolean methodExists(String name, TypeSystem ts) {
    return isLocalFunction(name) ? false : super.methodExists(name, ts);
  }
  
  @Override public Type typeContainingMethod(String name, TypeSystem ts) {
    return isLocalFunction(name) ? null : super.typeContainingMethod(name, ts);
  }
  
  private boolean isLocalFunction(String name) {
    return (_f instanceof LocalFunction) && ((LocalFunction) _f).declaredName().equals(name);
  }
  
  @Override public String makeClassName(String n) {
    return super.makeAnonymousClassName() + n;
  }
  
  @Override public DJClass getThis() {
    if (isStatic()) { return null; }
    else { return super.getThis(); }
  }
  
  @Override public DJClass getThis(String className) {
    if (isStatic()) { return null; }
    else { return super.getThis(className); }
  }
  
  @Override public DJClass getThis(Type expected, TypeSystem ts) {
    if (isStatic()) { return null; }
    else { return super.getThis(expected, ts); }
  }
  
  @Override public DJClass initializingClass() {
    return (_f instanceof DJConstructor) ? ((DJConstructor) _f).declaringClass() : null;
  }
  
  @Override public Type getReturnType() {
    if (_f instanceof DJConstructor) { return TypeSystem.VOID; }
    else { return _f.returnType(); }
  }
  
  @Override public Iterable<Type> getDeclaredThrownTypes() { return _f.thrownTypes(); }

  private boolean isStatic() {
    return _f instanceof DJMethod && ((DJMethod)_f).isStatic();
  }
  
}
