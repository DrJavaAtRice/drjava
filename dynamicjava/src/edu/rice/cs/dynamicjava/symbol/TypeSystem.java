package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.*;
import koala.dynamicjava.tree.Expression;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;

import java.io.Serializable;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A type system allows for variance in the typing rules of the system, while maintaining
  * a standard type checker.  It separates the type checker from most of the details of type
  * implementations.  For simplicity, it is also defined independently of most type-checker implementation
  * issues, such as syntax.  To enforce these relationships, the type checker should minimize
  * references to specific subtypes of {@link Type}; and the type system should minimize references
  * to specific subtypes of {@link Expression}.
  */
public abstract class TypeSystem {
  
  public static final BooleanType BOOLEAN = new BooleanType();
  public static final CharType CHAR = new CharType();
  public static final ByteType BYTE = new ByteType();
  public static final ShortType SHORT = new ShortType();
  public static final IntType INT = new IntType();
  public static final LongType LONG = new LongType();
  public static final FloatType FLOAT = new FloatType();
  public static final DoubleType DOUBLE = new DoubleType();
  public static final NullType NULL = new NullType();
  public static final VoidType VOID = new VoidType();
  public static final TopType TOP = new TopType();
  public static final BottomType BOTTOM = new BottomType();
  
  public static final SimpleClassType OBJECT = new SimpleClassType(SymbolUtil.wrapClass(Object.class));
  public static final SimpleClassType STRING = new SimpleClassType(SymbolUtil.wrapClass(String.class));
  public static final SimpleClassType CLONEABLE = new SimpleClassType(SymbolUtil.wrapClass(Cloneable.class));
  public static final SimpleClassType SERIALIZABLE = new SimpleClassType(SymbolUtil.wrapClass(Serializable.class));
  public static final SimpleClassType THROWABLE = new SimpleClassType(SymbolUtil.wrapClass(Throwable.class));
  public static final SimpleClassType EXCEPTION = new SimpleClassType(SymbolUtil.wrapClass(Exception.class));
  public static final SimpleClassType RUNTIME_EXCEPTION =
    new SimpleClassType(SymbolUtil.wrapClass(RuntimeException.class));
  public static final SimpleClassType BOOLEAN_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Boolean.class));
  public static final SimpleClassType CHARACTER_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Character.class));
  public static final SimpleClassType BYTE_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Byte.class));
  public static final SimpleClassType SHORT_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Short.class));
  public static final SimpleClassType INTEGER_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Integer.class));
  public static final SimpleClassType LONG_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Long.class));
  public static final SimpleClassType FLOAT_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Float.class));
  public static final SimpleClassType DOUBLE_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Double.class));
  public static final SimpleClassType VOID_CLASS = new SimpleClassType(SymbolUtil.wrapClass(Void.class));
  
  protected static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
  protected static final Iterable<Type> EMPTY_TYPE_ITERABLE = IterUtil.empty();
  protected static final Iterable<Expression> EMPTY_EXPRESSION_ITERABLE = IterUtil.empty();
  protected static final Option<Type> NONE_TYPE_OPTION = Option.none();
  
  public TypeWrapper wrap(Type t) { return new TypeWrapper(t); }
  
  public Iterable<TypeWrapper> wrap(Iterable<? extends Type> ts) {
    return IterUtil.map(ts, WRAP_TYPE);
  }
  
  private final Lambda<Type, TypeWrapper> WRAP_TYPE = new Lambda<Type, TypeWrapper>() {
    public TypeWrapper value(Type t) { return new TypeWrapper(t); }
  };

  public Option<TypeWrapper> wrap(Option<Type> t) {
    return t.isSome() ? Option.some(new TypeWrapper(t.unwrap())) : Option.<TypeWrapper>none();
  }
  
  /**
   * A wrapper for types that provides an alternate {@code toString()} and {@code equals()} implementation:
   * {@code toString()} is defined in terms of {@link #userRepresentation}; {@code equals()} is defined
   * in terms of {@link #isEqual}.  (Note that a corresponding {@code hashCode()} function is not implemented.)
   */
  public class TypeWrapper {
    private Type _t;
    public TypeWrapper(Type t) { _t = t; }
    /** Produce a string using {@link #userRepresentation}. */
    public String toString() { return userRepresentation(_t); }
    /** Compare two TypeWrappers using {@link #isEqual}. */
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof TypeWrapper)) { return false; }
      else { return isEqual(_t, ((TypeWrapper) o)._t); }
    }
    /** Throws an UnsupportedOperationException. */
    public int hashCode() { throw new UnsupportedOperationException(); }
  }
  
  
  /* Type Predicates */
  
  /** Determine if {@code t} is a primitive. */
  public abstract boolean isPrimitive(Type t);
  
  /** Determine if {@code t} is a reference. */
  public abstract boolean isReference(Type t);
  
  /** Determine if {@code t} is an array. */
  public abstract boolean isArray(Type t);


  /** Determine if the type is well-formed. */
  public abstract boolean isWellFormed(Type t);
  
  /**
   * Determine if the type can be used in an enhanced for loop.  {@code true} implies that an object of 
   * type {@code t} has member {@code iterator()}, which returns a {@link java.util.Iterator}.
   */
  public abstract boolean isIterable(Type t);

  /**
   * Determine if an object with type {@code t} is enumerable (and so can be used as the selector of a 
   * {@code switch} statement)
   */
  public abstract boolean isEnum(Type t);
  
  /** Determine if the type is available at runtime (via a {@link Class} object) */
  public abstract boolean isReifiable(Type t);

  /**
   * Determine if there exist values whose most specific type is {@code t} (ignoring
   * constructor-accessibility issues).  (Note that this implies that {@code t} is captured.)
   */
  public abstract boolean isConcrete(Type t);
  
  /**
   * Determine if values of type {@code t} are not dependent on an outer object (for example, a non-static 
   * inner class has such a dependency)
   */
  public abstract boolean isStatic(Type t);
  
  /** Determine if {@code t} is valid in the {@code extends} clause of a class definition */
  public abstract boolean isExtendable(Type t);
  
  /** Determine if {@code t} is valid in the {@code implements} clause of a class definition */
  public abstract boolean isImplementable(Type t);
  
  
  /* Fundamental Type Relationships */
  
  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public abstract boolean isEqual(Type t1, Type t2);
  
  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link #isEqual}), transitive relation.
   */
  public abstract boolean isSubtype(Type subT, Type superT);

  /** Determine if {@link #cast} would succeed given an expression of the given type */
  public abstract boolean isCastable(Type target, Type expT);

  /** Determine if {@link #assign} would succeed given a non-constant expression of the given type */
  public abstract boolean isAssignable(Type target, Type expT);

  /** Determine if {@link #assign} would succeed given a constant expression of the given type and value */
  public abstract boolean isAssignable(Type target, Type expT, Object expValue);
  
  /** Determine if {@link #makePrimitive} would succeed given an expression of the given type */
  public abstract boolean isPrimitiveConvertible(Type t);
  
  /** Determine if {@link #makeReference} would succeed given an expression of the given type */
  public abstract boolean isReferenceConvertible(Type t);
  
  /** Compute a common supertype of {@code t1} and {@code t2}. */
  public abstract Type join(Type t1, Type t2);
  
  /** Compute a common subtype of {@code t1} and {@code t2}. */
  public abstract Type meet(Type t1, Type t2);
  

  /* Unary Operations on Types */
  
  /** Compute the capture of {@code t}.  Capture eliminates wildcards in a {@link ParameterizedClassType}. */
  public abstract Type capture(Type t);
  
  /**
   * Compute the erased type of {@code t}.  The result is guaranteed to be reifiable (according
   * to {@link #isReifiable}) and a supertype of {@code t}.
   */
  public abstract Type erase(Type t);
  
  /**
   * Determine the class corresponding to the erasure of {@code t}.  To prevent over-eager loading of
   * user-defined classes, computation of the result is delayed by wrapping it in a thunk.  (A DJClass
   * return type would be incorrect, as there's no such thing (for example) as an array DJClass.)
   */
  public abstract Thunk<Class<?>> erasedClass(Type t);
  
  /**
   * Determine the type of the class object associated with t (for example, (informally)
   * {@code classOf(Integer) = Class<Integer>}).
   */
  public abstract Type reflectionClassOf(Type t);
  
  
  /**
   * Determine the element type of the given array type.  Assumes {@code t} is an array type (according to 
   * {@link #isArray}).
   */
  public abstract Type arrayElementType(Type t);

  /** Produce a string representing the type */
  public abstract String userRepresentation(Type t);
  
  
  /* Class Type Operations */
  
  /** Create a {@link SimpleClassType} or {@link RawClassType} corresponding to the given class. */
  public abstract ClassType makeClassType(DJClass c);
  
  /**
   * Create a {@link SimpleClassType}, {@link RawClassType}, or {@link ParameterizedClassType} 
   * corresponding to the given class with given type arguments.  If {@code args} is nonempty,
   * the result must be a {@code ParameterizedClassType} (or an error must occur).
   * 
   * @param c  The class to be instantiated
   * @param args  The type arguments for {@code c}
   * @throws InvalidTypeArgumentException  If the arguments do not correspond to the formal parameters of 
   *                                       {@code c}, or if the arguments are not within their bounds.
   */
  public abstract ClassType makeClassType(DJClass c, Iterable<? extends Type> args) 
    throws InvalidTypeArgumentException;
   
  
  /* Conversions on Expressions */
    
  /**
   * Convert the expression to a primitive.  The result is guaranteed to have a primitive type as its
   * TYPE property (according to {@link #isPrimitive}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a primitive type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a primitive
   */
  public abstract Expression makePrimitive(Expression e) throws UnsupportedConversionException;
  
  /**
   * Convert the expression to a reference.  The result is guaranteed to have a reference type as its
   * TYPE property (according to {@link #isReference}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a reference type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a reference
   */
  public abstract Expression makeReference(Expression e) throws UnsupportedConversionException;
  
  /**
   * Perform unary numeric promotion on an expression.
   * 
   * @param e  A typed expression with a primitive type
   * @return  A typed expression equivalent to {@code e} with the promoted type
   * @throws UnsupportedConversionException  If the expression cannot be used for numeric promotion
   */
  public abstract Expression unaryPromote(Expression e) throws UnsupportedConversionException;

  /**
   * Perform binary numeric promotion on a pair of expressions.  The resulting pair of expressions
   * are guaranteed to have the same type.
   * 
   * @param e1  A typed expression with a primitive type
   * @param e2  A typed expression with a primitive type
   * @return  Two typed expressions equivalent to {@code e1} and {@code e2} with the promoted type
   * @throws UnsupportedConversionException  If either expression cannot be used for numeric promotion
   */
  public abstract Pair<Expression, Expression> binaryPromote(Expression e1, Expression e2)
    throws UnsupportedConversionException;
  
  /**
   * Perform a join (as defined for the ? : operator) on a pair of expressions.  The resulting pair
   * of expressions are guaranteed to have the same type.  That type may contain uncaptured wildcards.
   * 
   * @param e1  A typed expression
   * @param e2  A typed expression
   * @return  Two typed expressions equivalent to {@code e1} and {@code e2} with the joined type
   * @throws UnsupportedConversionException  If the two types are incompatible.
   */
  public abstract Pair<Expression, Expression> join(Expression e1, Expression e2)
    throws UnsupportedConversionException;

  /**
   * Perform a cast on the given expression.  Any necessary conversions are performed.  If necessary,
   * the {@code CHECKED_TYPE} and {@code CONVERTED_TYPE} properties are set on the result.
   * 
   * @return  An expression equivalent to {@code e}, wrapped in any necessary conversions
   * @throws  UnsupportedConversionException  If the cast is to an incompatible type.
   */
  public abstract Expression cast(Type target, Expression e) throws UnsupportedConversionException;
  
  /**
   * Prepare the given expression for assignment, wrapping it in any necessary conversions.
   * 
   * @return  An expression equivalent to {@code e}, wrapped in any necessary conversions
   * @throws  UnsupportedConversionException  If assignment to the given type is incorrect.
   */
  public abstract Expression assign(Type target, Expression e) throws UnsupportedConversionException;

  
  /* Member lookup operations */
    
  /**
   * Lookup the constructor corresponding the the given invocation.
   * @param t  The type of the object to be constructed.
   * @param typeArgs  The type arguments for the constructor's type parameters.
   * @param args  A list of typed expressions corresponding to the constructor's parameters.
   * @param expected  The type expected in the invocation's calling context, if any.
   * @return  A {@link ConstructorInvocation} object representing the matched constructor.
   * @throws InvalidTargetException  If the type {@code t} cannot be constructed.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 constructor matches the given arguments and type 
   *                                   arguments.
   */
  // Must produce a reasonable value when looking up a constructor in an interface (for anonymous classes)
  public abstract ConstructorInvocation lookupConstructor(Type t, Iterable<? extends Type> typeArgs, 
                                                          Iterable<? extends Expression> args,
                                                          Option<Type> expected)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;
  
  
  public abstract boolean containsMethod(Type t, String name);
  
  public abstract boolean containsStaticMethod(Type t, String name);
  
  /**
   * Lookup the method corresponding the the given invocation.
   * @param object  A typed expression representing the object whose method is to be invoked.
   * @param name  The name of the method.
   * @param typeArgs  The type arguments for the method's type parameters.
   * @param args  A list of typed expressions corresponding to the method's parameters.
   * @param expected  The type expected in the invocation's calling context, if any.
   * @return  An {@link ObjectMethodInvocation} object representing the matched method.
   * @throws InvalidTargetException  If {@code object} cannot be used to invoke a method.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 method matches the given name, arguments, and type 
   *                                   arguments.
   */
  public abstract ObjectMethodInvocation lookupMethod(Expression object, String name,
                                                      Iterable<? extends Type> typeArgs, 
                                                      Iterable<? extends Expression> args,
                                                      Option<Type> expected)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;
    
  
  /**
   * Lookup the static method corresponding the the given invocation.
   * @param t  The type in which to search for a static method.
   * @param name  The name of the method.
   * @param typeArgs  The type arguments for the method's type parameters.
   * @param args  A list of typed expressions corresponding to the method's parameters.
   * @param expected  The type expected in the invocation's calling context, if any.
   * @return  A {@link StaticMethodInvocation} object representing the matched method.
   * @throws InvalidTargetException  If method invocation is not legal for the type {@code t}.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 method matches the given name, arguments, and type 
   *                                   arguments.
   */
  public abstract StaticMethodInvocation lookupStaticMethod(Type t, String name,
                                                            Iterable<? extends Type> typeArgs, 
                                                            Iterable<? extends Expression> args,
                                                            Option<Type> expected)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;
  
  
  public abstract boolean containsField(Type t, String name);
  
  public abstract boolean containsStaticField(Type t, String name);
  
  /**
   * Lookup the field with the given name in the given object.
   * @param object  A typed expression representing the object whose field is to be accessed.
   * @param name  The name of the field.
   * @return An {@link ObjectFieldReference} object representing the matched field.
   * @throws InvalidTargetException  If {@code object} cannot be used to access a field.
   * @throws UnmatchedLookupException  If 0 or more than 1 field matches the given name.
   */
  public abstract ObjectFieldReference lookupField(Expression object, String name)
    throws InvalidTargetException, UnmatchedLookupException;
  
  /**
   * Lookup the static field with the given name.
   * @param t  The type in which to search for a static field.
   * @param name  The name of the field.
   * @return A {@link StaticFieldReference} object representing the matched field.
   * @throws InvalidTargetException  If field access is not legal for the type {@code t}.
   * @throws UnmatchedLookupException  If 0 or more than 1 field matches the given name.
   */
  public abstract StaticFieldReference lookupStaticField(Type t, String name)
    throws InvalidTargetException, UnmatchedLookupException;
  
  
  public abstract boolean containsClass(Type t, String name);
  
  public abstract boolean containsStaticClass(Type t, String name);
  
  /**
   * Lookup the class with the given name in the given object.
   * @param object  A typed expression representing the object whose class is to be accessed.
   * @param name  The name of the class.
   * @param typeArgs  The type arguments for the class
   * @return A type representing the named class.
   * @throws InvalidTargetException  If {@code object} cannot be used to access a class.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid, do not correspond to the 
   *                                       class's formal parameters, or are not within their bounds.
   * @throws UnmatchedLookupException  If 0 or more than 1 class matches the given name.
   */  
  public abstract ClassType lookupClass(Expression object, String name, Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;
  
  /**
   * Lookup the class with the given name in the given type.
   * @param t  The type in which to search for a static class.
   * @param name  The name of the class.
   * @param typeArgs  The type arguments for the class
   * @return A type representing the named class.
   * @throws InvalidTargetException  If class access is not legal for the type {@code t}.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid, do not correspond to the 
   *                                       class's formal parameters, or are not within their bounds.
   * @throws UnmatchedLookupException  If 0 or more than 1 class matches the given name.
   */  
  public abstract ClassType lookupClass(Type t, String name, Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;

  /**
   * Lookup the static class with the given name.
   * @param t  The type in which to search for a static class.
   * @param name  The name of the class.
   * @param typeArgs  The type arguments for the class
   * @return A type representing the named class.
   * @throws InvalidTargetException  If class access is not legal for the type {@code t}.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid, do not correspond to the 
   *                                       class's formal parameters, or are not within their bounds.
   * @throws UnmatchedLookupException  If 0 or more than 1 class matches the given name.
   */
  public abstract ClassType lookupStaticClass(Type t, String name, Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException;
  
  
  /** Abstraction of the result of a method or constructor lookup */
  public static abstract class ProcedureInvocation {
    private final Iterable<? extends Type> _typeArgs;
    private final Iterable<? extends Expression> _args;
    private final Iterable<? extends Type> _thrown;

    protected ProcedureInvocation(Iterable<? extends Type> typeArgs, Iterable<? extends Expression> args, 
                                  Iterable<? extends Type> thrown) {
      _typeArgs = typeArgs;
      _args = args;
      _thrown = thrown;
    }
    
    /** @return  The (possible inferred) type arguments used in the invocation */
    public Iterable<? extends Type> typeArgs() { return _typeArgs; }
    
    /** 
     * @return  The arguments, wrapped in any necessary promotions so that each expression has the same
     *           type as its corresponding formal parameter.
     */
    public Iterable<? extends Expression> args() { return _args; }
    
    /** @return  The declared thrown types of the invocation */
    public Iterable<? extends Type> thrown() { return _thrown; }
  }
  
  
  /** The result of a constructor lookup */
  public static class ConstructorInvocation extends ProcedureInvocation {
    private final DJConstructor _constructor;
    
    public ConstructorInvocation(DJConstructor constructor, Iterable<? extends Type> typeArgs, 
                                 Iterable<? extends Expression> args, Iterable<? extends Type> thrown) {
      super(typeArgs, args, thrown);
      _constructor = constructor;
    }
    
    /** @return  The reflection object corresponding to the invoked constructor */
    public DJConstructor constructor() { return _constructor; }
  }
  
  
  /** Abstraction of the result of a static or non-static method lookup */
  public static abstract class MethodInvocation extends ProcedureInvocation {
    private final DJMethod _method;
    private final Type _returnType;
    
    protected MethodInvocation(DJMethod method, Type returnType, Iterable<? extends Type> typeArgs, 
                               Iterable<? extends Expression> args, Iterable<? extends Type> thrown) {
      super(typeArgs, args, thrown);
      _method = method;
      _returnType = returnType;
    }
    
    /** @return  The reflection object corresponding to the invoked method */
    public DJMethod method() { return _method; }
    
    /** @return  The return type of the invocation (before capture) */
    public Type returnType() { return _returnType; }
    
  }
  
  
  /** The result of a non-static method lookup */
  public static class ObjectMethodInvocation extends MethodInvocation {
    private final Expression _object;
    
    public ObjectMethodInvocation(DJMethod method, Type returnType, Expression object, 
                                  Iterable<? extends Type> typeArgs, Iterable<? extends Expression> args, 
                                  Iterable<? extends Type> thrown) {
      super(method, returnType, typeArgs, args, thrown);
      _object = object;
    }
    
    /** 
     * @return  The object whose method is invoked, wrapped in any necessary promotions so that
     *          the type is the type whose declared member is the matched method.
     */
    public Expression object() { return _object; }
  }
  
  
  /** The result of a static method lookup */
  public static class StaticMethodInvocation extends MethodInvocation {
    public StaticMethodInvocation(DJMethod method, Type returnType, Iterable<? extends Type> typeArgs, 
                                  Iterable<? extends Expression> args, Iterable<? extends Type> thrown) {
      super(method, returnType, typeArgs, args, thrown);
    }
  }
    
  
  /** Abstraction of the result of a static or non-static field lookup */
  public static abstract class FieldReference {
    private final DJField _field;
    private final Type _type;
    
    protected FieldReference(DJField field, Type type) {
      _field = field;
      _type = type;
    }
    
    /** @return  The reflection object corresponding to the accessed field */
    public DJField field() { return _field; }
    
    /** @return  The return type of the access (before capture) */
    public Type type() { return _type; }
  }
  
  
  /** The result of a non-static field lookup */
  public static class ObjectFieldReference extends FieldReference {
    private final Expression _object;
    
    public ObjectFieldReference(DJField field, Type type, Expression object) {
      super(field, type);
      _object = object;
    }
    
    /** 
     * @return  The object whose field is accessed, wrapped in any necessary promotions so that
     *          the type is the type whose declared member is the matched field.
     */
    public Expression object() { return _object; }
  }
  
  
  /** The result of a static field lookup */
  public static class StaticFieldReference extends FieldReference {
    public StaticFieldReference(DJField field, Type type) {
      super(field, type);
    }
  }
  
  public static class TypeSystemException extends Exception {
  }
  
  public static class InvalidTypeArgumentException extends TypeSystemException {
  }
  
  public static class UnsupportedConversionException extends TypeSystemException {
  }
  
  public static class InvalidTargetException extends TypeSystemException {
  }
  
  public static class UnmatchedLookupException extends TypeSystemException {
    private final int _matches;
    public UnmatchedLookupException(int matches) { _matches = matches; }
    public int matches() { return _matches; }
  }
  
    
}
