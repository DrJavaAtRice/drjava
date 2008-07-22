package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

/** Represents a class declaration. */
public interface DJClass {
  
  public String packageName();
  
  /** Produces the binary name for the given class (as in {@link Class#getName}) */
  public String fullName();
  
  public boolean isAnonymous();
  
  /**
   * Produce the (unqualified) declared name of the given class
   * @throws IllegalArgumentException  If the class is anonymous
   */
  public String declaredName();
  
  public boolean isInterface();
  
  public boolean isStatic();
  
  public boolean isAbstract();
  
  public boolean isFinal();
  
  public Access accessibility();
  
  public boolean hasRuntimeBindingsParams();
  
  /** The class that declares this class, or {@code null} if this is declared at a top-level or local scope */
  public DJClass declaringClass();
  
  /** List all type variables declared by this class */
  public Iterable<VariableType> declaredTypeParameters();
  
  /** List the declared supertypes of this class */
  public Iterable<Type> declaredSupertypes();
  
  public Iterable<DJField> declaredFields();
  
  public Iterable<DJConstructor> declaredConstructors();
  
  public Iterable<DJMethod> declaredMethods();
  
  public Iterable<DJClass> declaredClasses();
  
  /**
   * @return  The type bound to {@code super} in the context of this class, or 
   *          {@code null} if {@code super} is not defined
   */
  public Type immediateSuperclass();
  
  /**
   * Produce the runtime representation of the class (as in {@link ClassLoader#loadClass},
   * repeated invocations should produce the same object).
   */
  public Class<?> load();
  
  /** Equality must be defined so that distinct DJClasses that wrap the same class are equal. */
  public boolean equals(Object o);
  
  /** Equality must be defined so that distinct DJClasses that wrap the same class are equal. */
  public int hashCode();

} 

//// Things not dealt with here from ReflectionAdapter:
//
//  
//  /** {@code true} iff {@code t} is an iterable */
//  public boolean isIterable(Type t);
//
//  /** {@code true} iff {@code t} is an enum */
//  public boolean isEnum(Type t);
//  
//  
//  
//  /**
//   * Create a typed expression, wrapping the given expression (assumed to represent a primitive)
//   * in conversion code that generates its corresponding boxed value.
//   */
//  public Expression box(Expression e, ClassType boxedType);
