package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

/**
 * A context for type checking.  Contexts are immutable, and provide the necessary
 * contextual information for static analysis.  To separate concerns, a context has no
 * knowledge of the type system, and requires a TypeSystem to be passed as an argument
 * wherever it may be useful.
 */
public interface TypeContext {
  
  /* PACKAGE AND IMPORT MANAGEMENT */
  
  /** Set the current package to the given package name */
  public TypeContext setPackage(String name);
  
  /** Import on demand all top-level classes in the given package */
  public TypeContext importTopLevelClasses(String pkg);
  
  /** Import on demand all member classes of the given class */
  public TypeContext importMemberClasses(DJClass outer);
  
  /** Import on demand all static members of the given class */
  public TypeContext importStaticMembers(DJClass c);
  
  /** Import the given top-level class */
  public TypeContext importTopLevelClass(DJClass c);
  
  /** Import the member class(es) of {@code outer} with the given name */
  public TypeContext importMemberClass(DJClass outer, String name);
  
  /** Import the field(s) of {@code c} with the given name */
  public TypeContext importField(DJClass c, String name);
  
  /** Import the method(s) of {@code c} with the given name */
  public TypeContext importMethod(DJClass c, String name);   
  
  
  /* TYPES: TOP-LEVEL CLASSES, MEMBER CLASSES, AND TYPE VARIABLES */
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts);
  
  /** Test whether {@code name} is an in-scope top-level class */
  public boolean topLevelClassExists(String name, TypeSystem ts);
  
  /** Return the top-level class with the given name, or {@code null} if it does not exist. */
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException;
  
  /** Test whether {@code name} is an in-scope member class */
  public boolean memberClassExists(String name, TypeSystem ts);
  
  /**
   * Return the most inner type containing a class with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException;
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts);
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public Type getTypeVariable(String name, TypeSystem ts);
  

  /* VARIABLES: FIELDS AND LOCAL VARIABLES */  
  
  /** Test whether {@code name} is an in-scope field or local variable */
  public boolean variableExists(String name, TypeSystem ts);
  
  /** Test whether {@code name} is an in-scope field */
  public boolean fieldExists(String name, TypeSystem ts);
  
  /**
   * Return the most inner type containing a field with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException;
  
  /** Test whether {@code name} is an in-scope local variable */
  public boolean localVariableExists(String name, TypeSystem ts);
  
  /** Return the variable object for the given name, or {@code null} if it does not exist. */
  public LocalVariable getLocalVariable(String name, TypeSystem ts);
  
  
  /* FUNCTIONS: METHODS AND LOCAL FUNCTIONS */
  
  /** Test whether {@code name} is an in-scope method or local function */
  public boolean functionExists(String name, TypeSystem ts);
  
  /** Test whether {@code name} is an in-scope method */
  public boolean methodExists(String name, TypeSystem ts);
  
  /**
   * Return the most inner type containing a method with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMethod(String name, TypeSystem ts) throws AmbiguousNameException;
  
  /** Test whether {@code name} is an in-scope local function */
  public boolean localFunctionExists(String name, TypeSystem ts);
  
  /** List all local functions that match the given name (empty if there are none) */
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts);
  
  /** Helper for getLocalFunctions: list all matching functions, including those provided. */
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts, Iterable<LocalFunction> partial);
  
  
  /* MISC CONTEXTUAL INFORMATION */
  
  /** Return a full name for a class with the given name declared here. */
  public String makeClassName(String declaredName);
  
  /** Return a full name for an anonymous class declared here. */
  public String makeAnonymousClassName();
    
  /**
   * Return the class of {@code this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis();
  
  /**
   * Return the class of {@code className.this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis(String className);
  
  /**
   * Return the type referenced by {@code super} in the current context, or {@code null}
   * if there is no such type (for example, in a static context).
   */
  public Type getSuperType(TypeSystem ts);
  
  /**
   * Return the class loader for the current scope.  Allows class loaders to be defined in
   * later scopes with the returned loader as a parent.
   */
  public ClassLoader getClassLoader();
  
}
