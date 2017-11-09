package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

// TODO: support references to methods within the body
/**
 * The context of a class declaration's signature, which includes its type variables and own name.
 */
public class FunctionSignatureContext extends DelegatingContext {
  
  private Function _f;
  
  public FunctionSignatureContext(TypeContext next, Function f) {
    super(next);
    _f = f;
  }
  
  protected FunctionSignatureContext duplicate(TypeContext next) {
    return new FunctionSignatureContext(next, _f);
  }
  
  @Override public boolean typeExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name) != null || super.typeExists(name, ts);
  }
  
  @Override public boolean typeVariableExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name) != null || super.typeVariableExists(name, ts);
  }
  
  @Override public VariableType getTypeVariable(String name, TypeSystem ts) {
    VariableType result = declaredTypeVariable(name);
    return (result == null) ? super.getTypeVariable(name, ts) : result;
  }
  
  @Override public boolean topLevelClassExists(String name, TypeSystem ts) {
    return (declaredTypeVariable(name) == null) ? super.topLevelClassExists(name, ts) : false;
  }
  
  @Override public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return (declaredTypeVariable(name) == null) ? super.getTopLevelClass(name, ts) : null;
  }
  
  @Override public boolean memberClassExists(String name, TypeSystem ts) {
    return (declaredTypeVariable(name) == null) ? super.memberClassExists(name, ts) : false;
  }
  
  @Override public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return (declaredTypeVariable(name) == null) ? super.typeContainingMemberClass(name, ts) : null;
  }
  
  private VariableType declaredTypeVariable(String name) {
    for (VariableType t : _f.typeParameters()) {
      if (t.symbol().name().equals(name)) { return t; }
    }
    return null;
  }
  
}
