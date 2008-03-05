package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

// TODO: support references to this class within the signature or body
/**
 * The context of a class declaration's signature, which includes its type variables and own name
 * but excludes its members.
 */
public class ClassSignatureContext extends DelegatingContext {
  
  private DJClass _c;
  private ClassLoader _loader;
  
  public ClassSignatureContext(TypeContext next, DJClass c, ClassLoader loader) {
    super(next);
    _c = c;
    _loader = loader;
  }
  
  protected ClassSignatureContext duplicate(TypeContext next) {
    return new ClassSignatureContext(next, _c, _loader);
  }
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name, ts) != null || super.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts) {
    return declaredTypeVariable(name, ts) != null || super.typeVariableExists(name, ts);
  }
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public VariableType getTypeVariable(String name, TypeSystem ts) {
    VariableType result = declaredTypeVariable(name, ts);
    return (result == null) ? super.getTypeVariable(name, ts) : result;
  }
  
  private VariableType declaredTypeVariable(String name, TypeSystem ts) {
    for (VariableType t : _c.declaredTypeParameters()) {
      if (t.symbol().name().equals(name)) { return t; }
    }
    return null;
  }
  
  public ClassLoader getClassLoader() { return _loader; }
  
}
