package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** An abstract context that delegates to an enclosing context by default. */
public abstract class DelegatingContext implements TypeContext {
  
  private TypeContext _next;
  
  protected DelegatingContext(TypeContext next) {
    _next = next;
  }
  
  /** Create a copy of this context with the given context enclosing it. */
  protected abstract TypeContext duplicate(TypeContext next);

  
  /* PACKAGE AND IMPORT MANAGEMENT */
  
  /** Set the current package to the given package name */
  public TypeContext setPackage(String name) {
    return duplicate(_next.setPackage(name));
  }
  
  /** Import on demand all top-level classes in the given package */
  public TypeContext importTopLevelClasses(String pkg) {
    return duplicate(_next.importTopLevelClasses(pkg));
  }
  
  /** Import on demand all member classes of the given class */
  public TypeContext importMemberClasses(DJClass outer) {
    return duplicate(_next.importMemberClasses(outer));
  }
  
  /** Import on demand all static members of the given class */
  public TypeContext importStaticMembers(DJClass c) {
    return duplicate(_next.importStaticMembers(c));
  }
  
  /** Import the given top-level class */
  public TypeContext importTopLevelClass(DJClass c) {
    return duplicate(_next.importTopLevelClass(c));
  }
  
  /** Import the member class(es) of {@code outer} with the given name */
  public TypeContext importMemberClass(DJClass outer, String name) {
    return duplicate(_next.importMemberClass(outer, name));
  }
  
  /** Import the field(s) of {@code c} with the given name */
  public TypeContext importField(DJClass c, String name) {
    return duplicate(_next.importField(c, name));
  }
  
  /** Import the method(s) of {@code c} with the given name */
  public TypeContext importMethod(DJClass c, String name) {
    return duplicate(_next.importMethod(c, name));
  }
  
  
  /* TYPES: TOP-LEVEL CLASSES, MEMBER CLASSES, AND TYPE VARIABLES */
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts) {
    return _next.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope top-level class */
  public boolean topLevelClassExists(String name, TypeSystem ts) {
    return _next.topLevelClassExists(name, ts);
  }
  
  /** Return the top-level class with the given name, or {@code null} if it does not exist. */
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return _next.getTopLevelClass(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope member class */
  public boolean memberClassExists(String name, TypeSystem ts) {
    return _next.memberClassExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a class with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return _next.typeContainingMemberClass(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts) {
    return _next.typeVariableExists(name, ts);
  }
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public Type getTypeVariable(String name, TypeSystem ts) {
    return _next.getTypeVariable(name, ts);
  }
  

  /* VARIABLES: FIELDS AND LOCAL VARIABLES */  
  
  /** Test whether {@code name} is an in-scope field or local variable */
  public boolean variableExists(String name, TypeSystem ts) {
    return _next.variableExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope field */
  public boolean fieldExists(String name, TypeSystem ts) {
    return _next.fieldExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a field with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException {
    return _next.typeContainingField(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope local variable */
  public boolean localVariableExists(String name, TypeSystem ts) {
    return _next.localVariableExists(name, ts);
  }
  
  /** Return the variable object for the given name, or {@code null} if it does not exist. */
  public LocalVariable getLocalVariable(String name, TypeSystem ts) {
    return _next.getLocalVariable(name, ts);
  }
  
  
  /* FUNCTIONS: METHODS AND LOCAL FUNCTIONS */
  
  /** Test whether {@code name} is an in-scope method or local function */
  public boolean functionExists(String name, TypeSystem ts) {
    return _next.functionExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope method */
  public boolean methodExists(String name, TypeSystem ts) {
    return _next.methodExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a method with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMethod(String name, TypeSystem ts) throws AmbiguousNameException {
    return _next.typeContainingMethod(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope local function */
  public boolean localFunctionExists(String name, TypeSystem ts) {
    return _next.localFunctionExists(name, ts);
  }
  
  /**
   * List all local functions that match the given name (empty if there are none).  Overridden for
   * convenience, delegating to a trivial instance of
   * {@link getLocalFunctions(String, TypeSystem, Iterable)}.
   */
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts) {
    return getLocalFunctions(name, ts, IterUtil.<LocalFunction>empty());
  }
  
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts, Iterable<LocalFunction> partial) {
    return _next.getLocalFunctions(name, ts, partial);
  }
    
  
  /* MISC CONTEXTUAL INFORMATION */
  
  /** Return a full name for a class with the given name declared here. */
  public String makeClassName(String n) {
    return _next.makeClassName(n);
  }
  
  /** Return a full name for an anonymous class declared here. */
  public String makeAnonymousClassName() {
    return _next.makeAnonymousClassName();
  }
  
  /**
   * Return the class of {@code this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis() {
    return _next.getThis();
  }
  
  /**
   * Return the class of {@code className.this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis(String className) {
    return _next.getThis(className);
  }
  
  /**
   * Return the type referenced by {@code super} in the current context, or {@code null}
   * if there is no such type (for example, in a static context).
   */
  public Type getSuperType(TypeSystem ts) {
    return _next.getSuperType(ts);
  }
  
  /**
   * The expected type of a {@code return} statement in the given context, or {@code null}
   * if {@code return} statements should not appear here.
   */
  public Type getReturnType() {
    return _next.getReturnType();
  }
  
  /**
   * The types that are allowed to be thrown in the current context.  If there is no
   * such declaration, the list will be empty.
   */
  public Iterable<Type> getDeclaredThrownTypes() {
    return _next.getDeclaredThrownTypes();
  }
  
  public ClassLoader getClassLoader() {
    return _next.getClassLoader();
  }
  
}
