package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

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
    return matchesClass(name) || matchesTypeVariable(name) || super.typeExists(name, ts);
  }
  
  private boolean matchesTopLevelClass(String name) {
    return !_c.isAnonymous() && _c.declaringClass() == null && _c.declaredName().equals(name);
  }
  
  private boolean matchesMemberClass(String name) {
    return !_c.isAnonymous() && _c.declaringClass() != null && _c.declaredName().equals(name);
  }
  
  private boolean matchesClass(String name) {
    return !_c.isAnonymous() && _c.declaredName().equals(name);
  }
  
  private boolean matchesTypeVariable(String name) {
    return declaredTypeVariable(name) != null;
  }
  
  private VariableType declaredTypeVariable(String name) {
    for (VariableType t : _c.declaredTypeParameters()) {
      if (t.symbol().name().equals(name)) { return t; }
    }
    return null;
  }
  
  /** Test whether {@code name} is an in-scope top-level class */
  public boolean topLevelClassExists(String name, TypeSystem ts) {
    return matchesTopLevelClass(name) ||
          (!matchesMemberClass(name) && !matchesTypeVariable(name) && super.topLevelClassExists(name, ts));
  }
  
  /** Return the top-level class with the given name, or {@code null} if it does not exist. */
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    if (matchesTopLevelClass(name)) {
      return _c;
    }
    else if (!matchesMemberClass(name) && !matchesTypeVariable(name)) {
      return super.getTopLevelClass(name, ts);
    }
    else { return null; }
  }
  
  /** Test whether {@code name} is an in-scope member class */
  public boolean memberClassExists(String name, TypeSystem ts) {
    return matchesMemberClass(name) ||
          (!matchesTopLevelClass(name) && !matchesTypeVariable(name) && super.memberClassExists(name, ts));
  }
  
  /**
   * Return the most inner type containing a class with the given name, or {@code null}
   * if there is no such type.
   */
  public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    if (matchesMemberClass(name)) {
      return SymbolUtil.thisType(_c.declaringClass());
    }
    else if (!matchesTopLevelClass(name) && !matchesTypeVariable(name)) {
      return super.typeContainingMemberClass(name, ts);
    }
    else { return null; }
  }
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts) {
    return matchesTypeVariable(name) ||
          (!matchesClass(name) && super.typeVariableExists(name, ts));
  }
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public VariableType getTypeVariable(String name, TypeSystem ts) {
    VariableType result = declaredTypeVariable(name);
    if (result != null) { return result; }
    else if (!matchesClass(name)) { return super.getTypeVariable(name, ts); }
    else { return null; }
  }
  
  public ClassLoader getClassLoader() { return _loader; }
  
}
