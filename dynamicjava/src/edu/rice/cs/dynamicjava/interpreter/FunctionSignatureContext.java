package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
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
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name) != null || super.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name) != null || super.typeVariableExists(name, ts);
  }
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public VariableType getTypeVariable(String name, TypeSystem ts) {
    VariableType result = declaredTypeVariable(name);
    return (result == null) ? super.getTypeVariable(name, ts) : result;
  }
  
  private VariableType declaredTypeVariable(String name) {
    for (VariableType t : _f.declaredTypeParameters()) {
      if (t.symbol().name().equals(name)) { return t; }
    }
    return null;
  }
  
}
