package edu.rice.cs.dynamicjava.symbol;

import java.util.*;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.recur.*;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.Order;
import edu.rice.cs.plt.collect.PredicateSet;
import edu.rice.cs.plt.reflect.JavaVersion;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.TypeUtil;
import koala.dynamicjava.interpreter.NodeProperties;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.symbol.type.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Abstract parent class for TypeSystems that stick to the standard Java notions of types, conversions,
 * class members, etc.  Subclasses are responsible for providing core type operations: subtyping, inference,
 * well-formedness checking, etc.
 */
public abstract class StandardTypeSystem extends TypeSystem {
  
  private final Options _opt;
  
  protected StandardTypeSystem(Options opt) { _opt  = opt; }
  
  /** Determine if the type is well-formed. */
  public abstract boolean isWellFormed(Type t);

  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public abstract boolean isEqual(Type t1, Type t2);

  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link #isEqual}), transitive relation.
   */
  public abstract boolean isSubtype(Type subT, Type superT);
  
  /** Compute a common supertype of the given list of types. */
  public abstract Type join(Iterable<? extends Type> ts);

  /** Compute a common subtype of the given list of types. */
  public abstract Type meet(Iterable<? extends Type> ts);
  
  /** Produce types that are bounded by the corresponding type argument and parameter. */
  protected abstract Iterable<Type> captureTypeArgs(Iterable<? extends Type> targs,
                                                    Iterable<? extends VariableType> params);
  
  /**
   * Top-level entry point for type inference.  Produces the set of types corresponding to the given
   * type parameters, given that {@code args} were provided where {@code params} were expected
   * ({@code args} and {@code params} are assumed to have the same length), and {@code returned} will
   * be returned where {@code expected} is expected.
   * 
   * @return  A set of inferred type arguments for {@code tparams}, or {@code null} if the parameters
   *          are overconstrained
   */
  protected abstract Iterable<Type> inferTypeArguments(Iterable<? extends VariableType> tparams, 
                                                       Iterable<? extends Type> params, Type returned,
                                                       Iterable<? extends Type> args, Option<Type> expected);


  protected static final Type CLONEABLE_AND_SERIALIZABLE = 
    new IntersectionType(IterUtil.make(CLONEABLE, SERIALIZABLE));
  
  // non-static because it depends on makeClassType
  private final Type ITERABLE;
  {
    Class<?> c;
    try { c = Class.forName("java.lang.Iterable"); }
    catch (ClassNotFoundException e) { c = null; }
    ITERABLE = (c == null) ? null : makeClassType(SymbolUtil.wrapClass(c));
  }
  
  // non-static because it depends on makeClassType
  private final Type COLLECTION = makeClassType(SymbolUtil.wrapClass(Collection.class));
  
  // non-static because it depends on makeClassType
  private final Type ENUM;
  {
    Class<?> c;
    try { c = Class.forName("java.lang.Enum"); }
    catch (ClassNotFoundException e) { c = null; }
    ENUM = (c == null) ? null : makeClassType(SymbolUtil.wrapClass(c));
  }
  
  private final DJClass CLASS = SymbolUtil.wrapClass(Class.class);
  
  public TypePrinter typePrinter() { return new StandardTypePrinter(); }
  
  /** Determine if {@code t} is a primitive. */
  public boolean isPrimitive(Type t) { return t.apply(IS_PRIMITIVE); }
  
  private static final TypeVisitorLambda<Boolean> IS_PRIMITIVE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forPrimitiveType(PrimitiveType t) { return true; }
  };
  
  /** Determine if {@code t} is a reference. */
  public boolean isReference(Type t) { return t.apply(IS_REFERENCE); }
  
  private static final TypeVisitorLambda<Boolean> IS_REFERENCE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forReferenceType(ReferenceType t) { return true; }
    @Override public Boolean forVariableType(VariableType t) { return true; }
    @Override public Boolean forIntersectionType(IntersectionType t) { return true; }
    @Override public Boolean forUnionType(UnionType t) { return true; }
  };
  
  /** Determine if {@code t} is an array. */
  public boolean isArray(Type t) { return t.apply(IS_ARRAY); }
  
  private static final TypeVisitorLambda<Boolean> IS_ARRAY = new TypeAbstractVisitor<Boolean>() {
    private final Predicate<Type> PRED = LambdaUtil.asPredicate(this);
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forArrayType(ArrayType t) { return true; }
    @Override public Boolean forVariableType(VariableType t) { return t.symbol().upperBound().apply(this); }
    @Override public Boolean forIntersectionType(IntersectionType t) { return IterUtil.or(t.ofTypes(), PRED); }
    @Override public Boolean forUnionType(UnionType t) {
      // BOTTOM is not an array type
      return !IterUtil.isEmpty(t.ofTypes()) && IterUtil.and(t.ofTypes(), PRED);
    }
  };

  /**
   * Determine if the type can be used in an enhanced for loop.  {@code true} implies that an object of 
   * type {@code t} has member {@code iterator()}, which returns a {@link java.util.Iterator}.
   */
  public boolean isIterable(Type t) { return isSubtype(t, ITERABLE == null ? COLLECTION : ITERABLE); }
  
  
  /**
   * Determine if an object with type {@code t} is enumerable (and so can be used as the selector of a 
   * {@code switch} statement)
   */
  public boolean isEnum(Type t) { return ENUM != null && isSubtype(t, ENUM); }
  
  /** Determine if the type is available at runtime (via a {@link Class} object) */
  public boolean isReifiable(Type t) { return t.apply(IS_REIFIABLE); }
  
  // cannot be defined statically, because it relies on the definition of non-static "IS_UNBOUNDED_WILDCARD"
  private final TypeVisitorLambda<Boolean> IS_REIFIABLE = new TypeAbstractVisitor<Boolean>() {
    
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forPrimitiveType(PrimitiveType t) { return true; }
    @Override public Boolean forNullType(NullType t) { return true; }
    @Override public Boolean forArrayType(ArrayType t) { return t.ofType().apply(this); }
    @Override public Boolean forSimpleClassType(SimpleClassType t) { return true; }
    @Override public Boolean forRawClassType(RawClassType t) { return true; }
    @Override public Boolean forVoidType(VoidType t) { return true; }
    
    @Override public Boolean forParameterizedClassType (ParameterizedClassType t) {
      for (Type targ : t.typeArguments()) {
        if (!targ.apply(IS_UNBOUNDED_WILDCARD)) { return false; }
      }
      return true;
    }
  };
  
  // cannot be defined statically, because it relies on the definition of non-static "isEqual"
  private final TypeVisitorLambda<Boolean> IS_UNBOUNDED_WILDCARD = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forWildcard(Wildcard t) { 
      return isEqual(t.symbol().upperBound(), OBJECT) && isEqual(t.symbol().lowerBound(), NULL);
    }
  };
  
  /**
   * Determine if there exist values whose most specific type is {@code t} (ignoring
   * constructor-accessibility issues).  (Note that this implies that {@code t} is captured.)
   */
  public boolean isConcrete(Type t) { return t.apply(IS_CONCRETE); }
  
  private static final TypeVisitorLambda<Boolean> IS_CONCRETE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forPrimitiveType(PrimitiveType t) { return true; }
    @Override public Boolean forArrayType(ArrayType t) { return true; }
    @Override public Boolean forSimpleClassType(SimpleClassType t) { return isConcreteClass(t.ofClass()); }
    @Override public Boolean forRawClassType(RawClassType t) { return isConcreteClass(t.ofClass()); }
    
    @Override public Boolean forParameterizedClassType(ParameterizedClassType t) {
      if (!isConcreteClass(t.ofClass())) { return false; }
      for (Type targ : t.typeArguments()) {
        if (targ instanceof Wildcard) { return false; }
      }
      return true;
    }
    
    private boolean isConcreteClass(DJClass c) {
      return !c.isInterface() && !c.isAbstract();
    }
  };
  
  public Option<Type> dynamicallyEnclosingType(Type t) { return t.apply(DYNAMICALLY_ENCLOSING); }
  
  private static final TypeVisitorLambda<Option<Type>> DYNAMICALLY_ENCLOSING =
      new TypeAbstractVisitor<Option<Type>>() {
    public Option<Type> defaultCase(Type t) { return Option.none(); }
    
    @Override public Option<Type> forClassType(ClassType t) {
      return Option.<Type>wrap(SymbolUtil.dynamicOuterClassType(t));
    }
  };
  
  /** Determine if {@code t} is valid in the {@code extends} clause of a class definition */
  public boolean isExtendable(Type t) { return t.apply(IS_EXTENDABLE); }
  
  private static final TypeVisitorLambda<Boolean> IS_EXTENDABLE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    
    @Override public Boolean forClassType(ClassType t) {
      return !t.ofClass().isInterface() && !t.ofClass().isFinal();
    }
  };
  
  
  /** Determine if {@code t} is valid in the {@code implements} clause of a class definition */
  public boolean isImplementable(Type t) { return t.apply(IS_IMPLEMENTABLE); }
  
  private static final TypeVisitorLambda<Boolean> IS_IMPLEMENTABLE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forClassType(ClassType t) { return t.ofClass().isInterface(); }
  };
  
  
  /** Whether two types are known to be disjoint.  (Standard version is implicitly defined in JLS 5.5.)  */
  public boolean isDisjoint(final Type s, final Type t) {
    // By default, returns null for arrays and classes
    abstract class Visitor extends TypeAbstractVisitor<Boolean> {
      private final Type _other;
      public Visitor(Type other) { _other = other; }
      public abstract boolean recur(Type that);
      @Override public Boolean forPrimitiveType(PrimitiveType that) {
        return !isSubtype(that, _other) && !isSubtype(_other, that);
      }
      @Override public Boolean forNullType(NullType that) {
        return !isSubtype(that, _other);
      }
      @Override public Boolean forArrayType(ArrayType that) { return null; }
      @Override public Boolean forClassType(ClassType that) { return null; }
      @Override public Boolean forIntersectionType(IntersectionType that) {
        for (Type elt : that.ofTypes()) { if (recur(elt)) return true; }
        return false;
      }
      @Override public Boolean forUnionType(UnionType that) {
        for (Type elt : that.ofTypes()) { if (!recur(elt)) return false; }
        return true;
      }
      @Override public Boolean forVariableType(VariableType that) {
        // TODO: to be correct, we would need to recur (checked by a RecursionStack)
        return false;
      }
      @Override public Boolean forTopType(TopType s) { return false; }
      @Override public Boolean forBottomType(BottomType s) { return true; }
    }
    
    Boolean sResult = s.apply(new Visitor(t) {
      public boolean recur(Type that) { return isDisjoint(that, t); }
    });
    if (sResult != null) { return sResult; }
    else {
      return t.apply(new Visitor(s) {
        public boolean recur(Type that) { return isDisjoint(s, that); }
        @Override public Boolean forArrayType(ArrayType t) {
          if (s instanceof ArrayType) { return isDisjoint(((ArrayType) s).ofType(), t.ofType()); }
          else { return !isSubtype(t, s); }
        }
        @Override public Boolean forClassType(ClassType t) {
          if (s instanceof ArrayType) { return !isSubtype(s, t); }
          else {
            ClassType sAsClass = (ClassType) s;
            if (sAsClass.ofClass().isFinal() || t.ofClass().isFinal() ||
                (!sAsClass.ofClass().isInterface() && !t.ofClass().isInterface())) {
              // either one of them is a final class or both are non-final classes
              if (!isSubtype(s, erase(t)) && !isSubtype(t, erase(s))) { return true; }
            }
            // TODO: The JLS also checks for disjoint type arguments (comparing *all* common superclasses)
            return false;
          }
        }
      });
    }
  }
  
  /** Determine if {@link #assign} would succeed given a non-constant expression of the given type */
  public boolean isAssignable(Type target, Type expT) {
    // TODO: Handle unchecked warnings -- perhaps at the call site
    try { 
      Expression e = TypeUtil.makeEmptyExpression();
      NodeProperties.setType(e, expT);
      assign(target, e);
      return true;
    }
    catch (UnsupportedConversionException e) { return false; }
  }
  
  /** Determine if {@link #assign} would succeed given a constant expression of the given type and value */
  public boolean isAssignable(Type target, Type expT, Object expValue) {
    // TODO: Handle unchecked warnings -- perhaps at the call site
    try { 
      Expression e = TypeUtil.makeEmptyExpression();
      NodeProperties.setType(e, expT);
      NodeProperties.setValue(e, expValue);
      assign(target, e);
      return true;
    }
    catch (UnsupportedConversionException e) { return false; }
  }
  
  public boolean isPrimitiveConvertible(Type t) {
    return isPrimitive(t) ||
      (!_opt.prohibitBoxing() && !isSubtype(t, NULL) &&
       (isSubtype(t, BOOLEAN_CLASS) || 
        isSubtype(t, CHARACTER_CLASS) ||
        isSubtype(t, BYTE_CLASS) ||
        isSubtype(t, SHORT_CLASS) ||
        isSubtype(t, INTEGER_CLASS) ||
        isSubtype(t, LONG_CLASS) ||
        isSubtype(t, FLOAT_CLASS) ||
        isSubtype(t, DOUBLE_CLASS)));
  }
  
  
  public boolean isReferenceConvertible(Type t) {
    return isReference(t) || !_opt.prohibitBoxing() && t instanceof PrimitiveType;
  }
  
  
  public Type immediateSuperclass(Type t) {
    if (t instanceof ClassType) { return ((ClassType) t).ofClass().immediateSuperclass(); }
    else { return null; }
  }
  
  public Type capture(Type t) { return t.apply(CAPTURE); }
  
  // cannot be defined statically, because it relies on the definition of non-static "capture"
  private final TypeVisitorLambda<Type> CAPTURE = new TypeAbstractVisitor<Type>() {
    public Type defaultCase(Type t) { return t; }
    public Type forVarargArrayType(VarargArrayType t) { return new SimpleArrayType(t.ofType()); }
    @Override public Type forParameterizedClassType(ParameterizedClassType t) { return capture(t); }
  };
  
  protected ParameterizedClassType capture(ParameterizedClassType t) {
    boolean ground = true; // optimization: simply return non-wildcard cases
    for (Type arg : t.typeArguments()) {
      if (arg instanceof Wildcard) { ground = false; break; }
    }
    if (ground) { return t; }
    else {
      Iterable<VariableType> params = SymbolUtil.allTypeParameters(t.ofClass());
      Iterable<Type> captureArgs = captureTypeArgs(t.typeArguments(), params);
      return new ParameterizedClassType(t.ofClass(), captureArgs);
    }
  }
  
  /**
   * Compute the erased type of {@code t}.  The result is guaranteed to be reifiable (according
   * to {@link #isReifiable}) and a supertype of {@code t}.
   */
  public Type erase(Type t) { return t.apply(ERASE); }
  
  private static final TypeVisitorLambda<Type> ERASE = new TypeAbstractVisitor<Type>() {
    public Type defaultCase(Type t) { return t; }
    
    @Override public Type forNullType(NullType t) { return OBJECT; }
    
    @Override public Type forSimpleArrayType(SimpleArrayType t) {
      Type newElementType = t.ofType().apply(this);
      return (t.ofType() == newElementType) ? t : new SimpleArrayType(newElementType);
    }
    
    @Override public Type forVarargArrayType(VarargArrayType t) {
      Type newElementType = t.ofType().apply(this);
      return (t.ofType() == newElementType) ? t : new VarargArrayType(newElementType);
    }
    
    @Override public Type forParameterizedClassType(ParameterizedClassType t) {
      return new RawClassType(t.ofClass());
    }
    
    @Override public Type forVariableType(VariableType t) { return t.symbol().upperBound().apply(this); }
    
    @Override public Type forIntersectionType(IntersectionType t) {
      if (IterUtil.isEmpty(t.ofTypes())) { return OBJECT; }
      else { return IterUtil.first(t.ofTypes()).apply(this); }
    }
    
    @Override public Type forUnionType(UnionType t) {
      // TODO: improve this result by performing a join on the erased class hierarchy
      return OBJECT;
    }
    
    @Override public Type forWildcard(Wildcard t) { throw new IllegalArgumentException(); }
    @Override public Type forTopType(TopType t) { throw new IllegalArgumentException(); }
    @Override public Type forBottomType(BottomType t) { throw new IllegalArgumentException(); }
  };
  
  /**
   * Determine the class corresponding to the erasure of {@code t}, or {@code null} if no such class object 
   * exists.  To prevent over-eager loading of user-defined classes, computation of the result
   * is delayed by wrapping it in a thunk.  (A DJClass return type would be incorrect, as there's no such
   * thing (for example) as an array DJClass.)
   */
  public Thunk<Class<?>> erasedClass(Type t) { return t.apply(ERASED_CLASS); }
  
  private static final TypeVisitorLambda<Thunk<Class<?>>> ERASED_CLASS = new TypeVisitorLambda<Thunk<Class<?>>>() {
    public Thunk<Class<?>> forBooleanType(BooleanType t) { return LambdaUtil.<Class<?>>valueLambda(boolean.class); }
    public Thunk<Class<?>> forCharType(CharType t) { return LambdaUtil.<Class<?>>valueLambda(char.class); }
    public Thunk<Class<?>> forByteType(ByteType t) { return LambdaUtil.<Class<?>>valueLambda(byte.class); }
    public Thunk<Class<?>> forShortType(ShortType t) { return LambdaUtil.<Class<?>>valueLambda(short.class); }
    public Thunk<Class<?>> forIntType(IntType t) { return LambdaUtil.<Class<?>>valueLambda(int.class); }
    public Thunk<Class<?>> forLongType(LongType t) { return LambdaUtil.<Class<?>>valueLambda(long.class); }
    public Thunk<Class<?>> forFloatType(FloatType t) { return LambdaUtil.<Class<?>>valueLambda(float.class); }
    public Thunk<Class<?>> forDoubleType(DoubleType t) { return LambdaUtil.<Class<?>>valueLambda(double.class); }
    public Thunk<Class<?>> forNullType(NullType t) { return forSimpleClassType(OBJECT); }
    
    public Thunk<Class<?>> forSimpleArrayType(SimpleArrayType t) {
      Thunk<Class<?>> elementType = t.ofType().apply(this);
      return (elementType == null) ? null : SymbolUtil.arrayClassThunk(elementType);
    }
    
    public Thunk<Class<?>> forVarargArrayType(VarargArrayType t) {
      Thunk<Class<?>> elementType = t.ofType().apply(this);
      return (elementType == null) ? null : SymbolUtil.arrayClassThunk(elementType);
    }
    
    public Thunk<Class<?>> forSimpleClassType(SimpleClassType t) { return wrapDJClass(t.ofClass()); }
    
    public Thunk<Class<?>> forRawClassType(RawClassType t) { return wrapDJClass(t.ofClass()); }
    
    public Thunk<Class<?>> forParameterizedClassType(ParameterizedClassType t) {
      return wrapDJClass(t.ofClass());
    }
    
    public Thunk<Class<?>> forVariableType(VariableType t) {
      return t.symbol().upperBound().apply(this);
    }
    
    public Thunk<Class<?>> forIntersectionType(IntersectionType t) {
      Iterator<? extends Type> sups = t.ofTypes().iterator();
      if (!sups.hasNext()) { return null; }
      else { return sups.next().apply(this); }
    }
    
    public Thunk<Class<?>> forUnionType(UnionType t) { return forSimpleClassType(OBJECT); }
    
    public Thunk<Class<?>> forWildcard(Wildcard t) { throw new IllegalArgumentException(); }
    public Thunk<Class<?>> forVoidType(VoidType t) { return LambdaUtil.<Class<?>>valueLambda(void.class); }
    public Thunk<Class<?>> forTopType(TopType t) { throw new IllegalArgumentException(); }
    public Thunk<Class<?>> forBottomType(BottomType t) { throw new IllegalArgumentException(); }
    
    private Thunk<Class<?>> wrapDJClass(final DJClass c) {
      return new Thunk<Class<?>>() {
        public Class<?> value() { return c.load(); }
      };
    }
    
  };
  
  /** Convert a raw class type to its wildcard-parameterized equivalent. */
  protected ParameterizedClassType parameterize(final RawClassType t) {
    Iterable<VariableType> tparams = SymbolUtil.allTypeParameters(t.ofClass());
    return new ParameterizedClassType(t.ofClass(), IterUtil.mapSnapshot(tparams, new Lambda<VariableType, Type>() {
      public Type value(VariableType param) {
        // identity is determined by t and param -- result should be identical when repeatedly invoked
        return new Wildcard(new BoundedSymbol(Pair.make(t, param), OBJECT, NULL));
      }
    }));
  }
  
  /**
   * @return  The type of the Class object associated with t (for example, (informally)
   * {@code reflectionClassOf(java.lang.Integer) = Class<Integer>}).
   */
  public Type reflectionClassOf(Type t) {
    if (IterUtil.isEmpty(SymbolUtil.allTypeParameters(CLASS))) { return makeClassType(CLASS); }
    else {
      try { return makeClassType(CLASS, IterUtil.make(t)); }
      catch (InvalidTypeArgumentException e) {
        throw new RuntimeException("java.lang.Class has unexpected type parameter(s)");
      }
    }
  }
  
  /**
   * Determine the element type of the given array type.  Assumes {@code t} is an array type (according to 
   * {@link #isArray}).
   */
  public Type arrayElementType(Type t) {
    return t.apply(ARRAY_ELEMENT_TYPE);
  }
  
  // not defined statically because it relies on non-static meet() and join()
  private final TypeVisitorLambda<Type> ARRAY_ELEMENT_TYPE = new TypeAbstractVisitor<Type>() {
    public Type defaultCase(Type t) { throw new IllegalArgumentException(); }
    @Override public Type forArrayType(ArrayType t) { return t.ofType(); }
    @Override public Type forVariableType(VariableType t) { return t.symbol().upperBound().apply(this); }
    @Override public Type forIntersectionType(IntersectionType t) {
      // at least one element has an array type
      return meet(IterUtil.map(t.ofTypes(), new Lambda<Type, Type>() {
        public Type value(Type arrayT) {
          return arrayT.apply(IS_ARRAY) ? arrayT.apply(ARRAY_ELEMENT_TYPE) : TOP;
        }
      }));
    }
    @Override public Type forUnionType(UnionType t) {
      // there is at least one element, and all have array types
      return join(IterUtil.map(t.ofTypes(), this)); 
    }
  };
  
  
  protected static class SubstitutionMap {
    private Map<VariableType, Type> _sigma;
    private Iterable<? extends VariableType> _vars;
    private Iterable<? extends Type> _values;
    
    public static final SubstitutionMap EMPTY = new SubstitutionMap(IterUtil.<VariableType>empty(),
                                                                    EMPTY_TYPE_ITERABLE);
    
    public SubstitutionMap(Iterable<? extends VariableType> vars, Iterable<? extends Type> values) {
      _sigma = null;
      _vars = vars;
      _values = values;
    }
    
    public SubstitutionMap(Map<? extends VariableType, ? extends Type> map) {
      // make a copy to prevent mutation
      _sigma = new HashMap<VariableType, Type>(map);
    }
    
    public boolean isEmpty() {
      if (_sigma == null) { return IterUtil.isEmpty(_vars); }
      else { return _sigma.isEmpty(); }
    }
    
    public Type get(VariableType v) {
      if (_sigma == null) { initSigma(); }
      return _sigma.get(v);
    }
    
    // Initialize the map lazily, because in same cases it may not be used at all.
    private void initSigma() {
      _sigma = new HashMap<VariableType, Type>();
      for (Pair<VariableType, Type> pair : IterUtil.zip(_vars, _values)) {
        _sigma.put(pair.first(), pair.second());
      }
      _vars = null;
      _values = null;
    }
  }
  
  /**
   * Assumes each paramater is a unique variable, and that the length of params
   * is consistent with the length of args.
   */
  protected Type substitute(Type t, Iterable<? extends VariableType> params, Iterable<? extends Type> args) {
    return substitute(t, new SubstitutionMap(params, args));
  }
  
  protected Type substitute(Type t, Map<? extends VariableType, ? extends Type> map) {
    return substitute(t, new SubstitutionMap(map));
  }
  
  protected Type substitute(Type t, final SubstitutionMap sigma) {
    if (sigma.isEmpty()) { return t; }
    else {
      final PrecomputedRecursionStack<Type, Type> stack = PrecomputedRecursionStack.make();
      
      return t.apply(new TypeUpdateVisitor() {
        
        @Override public Type forVariableType(VariableType t) {
          Type result = sigma.get(t);
          return (result == null) ? t : result;
        }
        
        @Override public Type forWildcard(final Wildcard t) {
          final Wildcard newWildcard = new Wildcard(new BoundedSymbol(new Object()));
          Thunk<Type> substituteBounds = new Thunk<Type>() {
            public Type value() {
              BoundedSymbol bounds = t.symbol();
              Type newUpper = recur(bounds.upperBound());
              Type newLower = recur(bounds.lowerBound());
              if (newUpper == bounds.upperBound() && newLower == bounds.lowerBound()) { return t; }
              else {
                newWildcard.symbol().initializeUpperBound(newUpper);
                newWildcard.symbol().initializeLowerBound(newLower);
                return newWildcard;
              }
            }
          };
          return stack.apply(substituteBounds, newWildcard, t);
        }
        
      });
    }
  }
  
  private Iterable<? extends Type> substitute(Iterable<? extends Type> ts, 
                                              Iterable<? extends VariableType> vars, 
                                              Iterable<? extends Type> values) {
    return substitute(ts, new SubstitutionMap(vars, values));
  }
  
  private Iterable<? extends Type> substitute(Iterable<? extends Type> ts, final SubstitutionMap sigma) {
    if (sigma.isEmpty()) { return ts; }
    else {
      return IterUtil.mapSnapshot(ts, new Lambda<Type, Type>() {
        public Type value(Type t) { return substitute(t, sigma); }
      });
    }
  }
  
  /**
   * Create a {@link SimpleClassType} or {@link RawClassType} corresponding to the given class.
   */
  public ClassType makeClassType(DJClass c) {
    if (IterUtil.isEmpty(SymbolUtil.allTypeParameters(c))) { return new SimpleClassType(c); }
    else { return new RawClassType(c); }
  }
  
  /**
   * Create a {@link SimpleClassType}, {@link RawClassType}, or {@link ParameterizedClassType} 
   * corresponding to the given class with given type arguments.  If {@code args} is nonempty,
   * the result must be a {@code ParameterizedClassType} (or an error must occur).
   * 
   * @param c  The class to be instantiated
   * @param args  The type arguments for {@code c}
   * @throws InvalidTypeArgumentException  If the arguments do not correspond to the formal parameters of 
   *                                        {@code c} (bounds are not checked, so the result may not be
   *                                        well-formed).
   */
  public ClassType makeClassType(DJClass c, Iterable<? extends Type> args) throws InvalidTypeArgumentException {
    if (IterUtil.isEmpty(args)) { return makeClassType(c); }
    else {
      Iterable<VariableType> params = SymbolUtil.allTypeParameters(c);
      if (IterUtil.sizeOf(params) != IterUtil.sizeOf(args)) { throw new InvalidTypeArgumentException(); }
      else {
        ParameterizedClassType result = new ParameterizedClassType(c, args);
        return result;
      }
    }
  }
  
  
  /**
   * Convert the expression to a primitive.  The result is guaranteed to have a primitive type as its
   * TYPE property (according to {@link #isPrimitive}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a primitive type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a primitive
   */
  public Expression makePrimitive(Expression e) throws UnsupportedConversionException {
    Type t = NodeProperties.getType(e);
    if (isPrimitive(t)) { return e; }
    else if (_opt.prohibitBoxing()) { throw new UnsupportedConversionException(); }
    // Note: The spec is not clear about whether a *subtype* (such as a variable) can
    //       be unboxed.  We allow it here unless the type is null, because that seems
    //       like the correct approach.
    else if (isSubtype(t, NULL)) { throw new UnsupportedConversionException(); }
    else if (isSubtype(t, BOOLEAN_CLASS)) { return unbox(e, "booleanValue"); }
    else if (isSubtype(t, CHARACTER_CLASS)) { return unbox(e, "charValue"); }
    else if (isSubtype(t, BYTE_CLASS)) { return unbox(e, "byteValue"); }
    else if (isSubtype(t, SHORT_CLASS)) { return unbox(e, "shortValue"); }
    else if (isSubtype(t, INTEGER_CLASS)) { return unbox(e, "intValue"); }
    else if (isSubtype(t, LONG_CLASS)) { return unbox(e, "longValue"); }
    else if (isSubtype(t, FLOAT_CLASS)) { return unbox(e, "floatValue"); }
    else if (isSubtype(t, DOUBLE_CLASS)) { return unbox(e, "doubleValue"); }
    else { throw new UnsupportedConversionException(); }
  }
  
  private Expression unbox(Expression exp, String methodName) {
    ObjectMethodCall result = new ObjectMethodCall(exp, methodName, null, exp.getSourceInfo());
    try {
      ObjectMethodInvocation inv = lookupMethod(exp, methodName, EMPTY_TYPE_ITERABLE, EMPTY_EXPRESSION_ITERABLE,
                                                NONE_TYPE_OPTION, new TopLevelAccessModule(""));
      result.setExpression(inv.object());
      result.setArguments(CollectUtil.makeList(inv.args()));
      NodeProperties.setMethod(result, inv.method());
      NodeProperties.setType(result, capture(inv.returnType()));
      if (NodeProperties.hasValue(exp)) { NodeProperties.setValue(result, NodeProperties.getValue(exp)); }
      return result;
    }
    catch (TypeSystemException e) { throw new RuntimeException("Unboxing method inaccessible", e); }
  }
  
  /**
   * Convert the expression to a reference.  The result is guaranteed to have a reference type as its
   * TYPE property (according to {@link #isReference}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a reference type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a reference
   */
  public Expression makeReference(final Expression e) throws UnsupportedConversionException {
    Type t = NodeProperties.getType(e);
    if (isReference(t)) { return e; }
    else if (_opt.prohibitBoxing()) { throw new UnsupportedConversionException(); }
    else {
      Expression result = t.apply(new TypeAbstractVisitor<Expression>() {
        public Expression defaultCase(Type t) {  return null; }
        @Override public Expression forBooleanType(BooleanType t) { return box(e, BOOLEAN_CLASS); }
        @Override public Expression forCharType(CharType t) { return box(e, CHARACTER_CLASS); }
        @Override public Expression forByteType(ByteType t) { return box(e, BYTE_CLASS); }
        @Override public Expression forShortType(ShortType t) { return box(e, SHORT_CLASS); }
        @Override public Expression forIntType(IntType t) { return box(e, INTEGER_CLASS); }
        @Override public Expression forLongType(LongType t) { return box(e, LONG_CLASS); }
        @Override public Expression forFloatType(FloatType t) { return box(e, FLOAT_CLASS); }
        @Override public Expression forDoubleType(DoubleType t) { return box(e, DOUBLE_CLASS); }
      });
      if (result == null) { throw new UnsupportedConversionException(); }
      else { return result; }
    }
  }
  
  private Expression box(Expression exp, ClassType boxedType) {
    ReferenceTypeName boxedTypeName = new ReferenceTypeName("java", "lang", boxedType.ofClass().declaredName());
    NodeProperties.setType(boxedTypeName, boxedType);
    List<Expression> arguments = Collections.singletonList(exp);
    if (JavaVersion.CURRENT.supports(JavaVersion.JAVA_5)) {
      StaticMethodCall m = new StaticMethodCall(boxedTypeName, "valueOf", arguments, exp.getSourceInfo());
      try {
        MethodInvocation inv = lookupStaticMethod(boxedType, "valueOf", EMPTY_TYPE_ITERABLE, arguments,
                                                  NONE_TYPE_OPTION, new TopLevelAccessModule(""));
        m.setArguments(CollectUtil.makeList(inv.args()));
        NodeProperties.setMethod(m, inv.method());
        NodeProperties.setType(m, capture(inv.returnType()));
        if (NodeProperties.hasValue(exp)) { NodeProperties.setValue(m, NodeProperties.getValue(exp)); }
        return m;
      }
      catch (TypeSystemException e) { throw new RuntimeException("Boxing method inaccessible", e); }
    }
    else {
      SimpleAllocation k = new SimpleAllocation(boxedTypeName, arguments, exp.getSourceInfo());
      try {
        ConstructorInvocation inv = lookupConstructor(boxedType, EMPTY_TYPE_ITERABLE, arguments, NONE_TYPE_OPTION,
                                                      new TopLevelAccessModule("")); 
        k.setArguments(CollectUtil.makeList(inv.args()));
        NodeProperties.setConstructor(k, inv.constructor());
        NodeProperties.setType(k, boxedType);
        if (NodeProperties.hasValue(exp)) { NodeProperties.setValue(k, NodeProperties.getValue(exp)); }
        return k;
      }
      catch (TypeSystemException e) { throw new RuntimeException("Boxing constructor inaccessible", e); }
    }
  }
  
  /**
   * Perform unary numeric promotion on an expression.
   * 
   * @param e  A typed expression with a primitive type
   * @return  A typed expression equivalent to {@code e} with the promoted type
   * @throws UnsupportedConversionException  If the expression cannot be used for numeric promotion
   */
  public Expression unaryPromote(final Expression e) throws UnsupportedConversionException {
    // Note: Variables with primitive bounds are not supported
    Expression result = NodeProperties.getType(e).apply(new TypeAbstractVisitor<Expression>() {
      public Expression defaultCase(Type t) { return null; }
      @Override public Expression forNumericType(NumericType t) { return e; }
      @Override public Expression forCharType(CharType t) { return makeCast(INT, e); }
      @Override public Expression forByteType(ByteType t) { return makeCast(INT, e); }
      @Override public Expression forShortType(ShortType t) { return makeCast(INT, e); }
    });
    if (result == null) { throw new UnsupportedConversionException(); }
    else { return result; }
  }
  
  /**
   * Perform binary numeric promotion on a pair of expressions.  The resulting pair of expressions
   * are guaranteed to have the same type.
   * 
   * @param e1  A typed expression with a primitive type
   * @param e2  A typed expression with a primitive type
   * @return  Two typed expressions equivalent to {@code e1} and {@code e2} with the promoted type
   * @throws UnsupportedConversionException  If either expression cannot be used for numeric promotion
   */
  public Pair<Expression, Expression> binaryPromote(final Expression e1, final Expression e2) 
    throws UnsupportedConversionException {
    // Note: Variables with primitive bounds are not fully supported
    final Type t1 = NodeProperties.getType(e1);
    final Type t2 = NodeProperties.getType(e2);
    Pair<Expression, Expression> result =
      join(t1, t2).apply(new TypeAbstractVisitor<Pair<Expression, Expression>>() {
      public Pair<Expression, Expression> defaultCase(Type commonT) {
        if (!(t1 instanceof NumericType) || !(t2 instanceof NumericType)) {
          return null;
        }
        else { throw new IllegalArgumentException("Unexpected join result"); }
      }
      
      @Override public Pair<Expression, Expression> forDoubleType(DoubleType commonT) {
        return Pair.make(t1 instanceof DoubleType ? e1 :makeCast(DOUBLE, e1),
                         t2 instanceof DoubleType ? e2 : makeCast(DOUBLE, e2));
      }
      
      @Override public Pair<Expression, Expression> forFloatType(FloatType commonT) {
        return Pair.make(t1 instanceof FloatType ? e1 : makeCast(FLOAT, e1),
                         t2 instanceof FloatType ? e2 : makeCast(FLOAT, e2));
      }
      
      @Override public Pair<Expression, Expression> forLongType(LongType commonT) {
        return Pair.make(t1 instanceof LongType ? e1 : makeCast(LONG, e1),
                         t2 instanceof LongType ? e2 : makeCast(LONG, e2));
      }
      
      @Override public Pair<Expression, Expression> forIntType(IntType commonT) {
        return Pair.make(t1 instanceof IntType ? e1 : makeCast(INT, e1),
                         t2 instanceof IntType ? e2 : makeCast(INT, e2));
      }
      
      @Override public Pair<Expression, Expression> forShortType(ShortType commonT) {
        return Pair.make(makeCast(INT, e1), makeCast(INT, e2));
      }
      
      @Override public Pair<Expression, Expression> forByteType(ByteType commonT) {
        return Pair.make(makeCast(INT, e1), makeCast(INT, e2));
      }
      
      @Override public Pair<Expression, Expression> forCharType(CharType commonT) {
        return Pair.make(makeCast(INT, e1), makeCast(INT, e2));
      }
    });
    if (result == null) { throw new UnsupportedConversionException(); }
    else { return result; }
  }
  
  /**
   * Perform a join (as defined for the ? : operator) on a pair of expressions.  The resulting pair
   * of expressions are guaranteed to have the same type.  That type may contain non-captured wildcards.
   * 
   * @param e1  A typed expression
   * @param e2  A typed expression
   * @return  Two typed expressions equivalent to {@code e1} and {@code e2} with the joined type
   * @throws UnsupportedConversionException  If the two types are incompatible.
   */
  public Pair<Expression, Expression> mergeConditional(final Expression e1, final Expression e2) 
    throws UnsupportedConversionException {
    return NodeProperties.getType(e1).apply(new TypeAbstractVisitor<Pair<Expression, Expression>>() {
      public Pair<Expression, Expression> defaultCase(Type t1) {
        if (isNumericReference(t1)) { return checkForNumericE2(); }
        else if (isBooleanReference(t1) && NodeProperties.getType(e2) instanceof BooleanType) {
          try { return Pair.make(makePrimitive(e1), e2); }
          catch (UnsupportedConversionException e) { throw new RuntimeException("isBooleanReference() lied"); }
        }
        else { return joinReferences(); }
      }
      
      @Override public Pair<Expression, Expression> forBooleanType(BooleanType t1) {
        Type t2 = NodeProperties.getType(e2);
        if (t2 instanceof BooleanType) { return Pair.make(e1, e2); }
        else if (isBooleanReference(t2)) {
          try { return Pair.make(e1, makePrimitive(e2)); }
          catch (UnsupportedConversionException e) { throw new RuntimeException("isBooleanReference() lied"); }
        }
        else { return joinReferences(); }
      }
      
      @Override public Pair<Expression, Expression> forNumericType(NumericType t1) { return checkForNumericE2(); }
      
      private boolean isNumericReference(Type t) {
        return !_opt.prohibitBoxing() && !isSubtype(t, NULL) &&
          (isSubtype(t, CHARACTER_CLASS) || 
           isSubtype(t, BYTE_CLASS) || 
           isSubtype(t, SHORT_CLASS) || 
           isSubtype(t, INTEGER_CLASS) || 
           isSubtype(t, LONG_CLASS) ||
           isSubtype(t, FLOAT_CLASS) || 
           isSubtype(t, DOUBLE_CLASS));
      }
      
      private boolean isBooleanReference(Type t) {
        return !_opt.prohibitBoxing() && isSubtype(t, BOOLEAN_CLASS) && !isSubtype(t, NULL);
      }
      
      private Pair<Expression, Expression> checkForNumericE2() {
        return NodeProperties.getType(e2).apply(new TypeAbstractVisitor<Pair<Expression, Expression>>() {
          public Pair<Expression, Expression> defaultCase(Type t2) {
            if (isNumericReference(t2)) { return joinNumbers(); }
            else { return joinReferences(); }
          }
          @Override public Pair<Expression, Expression> forNumericType(NumericType t2) { return joinNumbers(); }
        });
      }
      
      private Pair<Expression, Expression> joinNumbers() {
        try {
          Expression unboxed1 = makePrimitive(e1);
          Expression unboxed2 = makePrimitive(e2);
          Type numT1 = NodeProperties.getType(unboxed1);
          Type numT2 = NodeProperties.getType(unboxed2);
          Type joined = null;
          if (NodeProperties.hasValue(unboxed1) && numT1 instanceof IntType) {
            joined = inRange(NodeProperties.getValue(unboxed1), numT2) ? numT2 : null;
          }
          if (joined == null && NodeProperties.hasValue(unboxed2) && numT2 instanceof IntType) {
            joined = inRange(NodeProperties.getValue(unboxed2), numT1) ? numT1 : null;
          }
          if (joined == null) { joined = join(numT1, numT2); }
          Expression result1 = isEqual(numT1, joined) ? unboxed1 : makeCast(joined, unboxed1);
          Expression result2 = isEqual(numT2, joined) ? unboxed2 : makeCast(joined, unboxed2);
          return Pair.make(result1, result2);
        }
        catch (UnsupportedConversionException e) { throw new IllegalArgumentException(e); }
      }
      
      private Pair<Expression, Expression> joinReferences() {
        try {
          Expression boxed1 = makeReference(e1);
          Expression boxed2 = makeReference(e2);
          Type refT1 = NodeProperties.getType(boxed1);
          Type refT2 = NodeProperties.getType(boxed2);
          Type joined = join(refT1, refT2);
          Expression result1 = isEqual(refT1, joined) ? boxed1 : makeCast(joined, boxed1);
          Expression result2 = isEqual(refT2, joined) ? boxed2 : makeCast(joined, boxed2);
          return Pair.make(result1, result2);
        }
        catch (UnsupportedConversionException e) { throw new IllegalArgumentException(); }
      }
      
    });
  }
  
  /**
   * Perform a cast on the given expression.  Any necessary conversions are performed.  If necessary,
   * the {@code CHECKED_TYPE} and {@code CONVERTED_TYPE} properties are set on the result.
   * 
   * @return  An expression equivalent to {@code e}, wrapped in any necessary conversions
   * @throws  UnsupportedConversionException  If the cast is to an incompatible type.
   */
  public Expression cast(final Type target, final Expression e) throws UnsupportedConversionException {
    Expression result = target.apply(new TypeAbstractVisitor<Expression>() {
      
      @Override public Expression forPrimitiveType(PrimitiveType target) {
        try {
          Expression result = makePrimitive(e);
          Type source = NodeProperties.getType(result);
          if (!isEqual(target, source)) { NodeProperties.setConvertedType(result, erasedClass(target)); }
          return result;
        }
        catch (UnsupportedConversionException e) { return null; }
      }
      
      @Override public Expression defaultCase(Type target) {
        try {
          Expression result = makeReference(e);
          Type source = NodeProperties.getType(result);
          if (!isSubtype(source, target)) {
            if (!isDisjoint(source, target) && (!_opt.prohibitUncheckedCasts() || validCheckedCast(target, source))) {
              NodeProperties.setCheckedType(result, erasedClass(target));
            }
            else { throw new UnsupportedConversionException(); }
          }
          return result;
        }
        catch (UnsupportedConversionException e) { return null; }
      }
    });
    if (result == null) { throw new UnsupportedConversionException(); }
    else { return result; }
  }
  
  /**
   * Whether a reference down-cast is valid and guaranteed safe. May assume that
   * source is not a subtype of target and that the two are not disjoint.  See JLS 5.5.
   */
  private boolean validCheckedCast(Type target, final Type source) {
    return target.apply(new TypeAbstractVisitor<Boolean>() {
      @Override public Boolean defaultCase(Type target) { return isReifiable(target); }
      @Override public Boolean forParameterizedClassType(ParameterizedClassType target) {
        if (isReifiable(target)) { return true; }
        else if (isSubtype(target, source)) {
          // Verify that that, given a value of type source & erase(target), it must have type target.
          // Must show, where target=Target<T1..Tn>, forall X1..Xn, Target<X1..Xn> <: source implies Xi=Ti.
          ParameterizedClassType wildCapt = capture(parameterize(new RawClassType(target.ofClass())));
          Iterable<VariableType> unboundArgs =
            IterUtil.filterInstances(IterUtil.relax(wildCapt.typeArguments()), VariableType.class);
          Iterable<Type> boundArgs = inferTypeArguments(unboundArgs, EMPTY_TYPE_ITERABLE, wildCapt,
                                                        EMPTY_TYPE_ITERABLE, Option.some(source));
          return boundArgs != null && IterUtil.and(boundArgs, target.typeArguments(), new Predicate2<Type, Type>() {
            public boolean contains(Type inferred, Type orig) { return isEqual(inferred, orig); }
          });
        }
        else { return false; }
      }
      @Override public Boolean forArrayType(ArrayType target) {
        if (isArray(source)) { return validCheckedCast(target.ofType(), arrayElementType(source)); }
        else { return defaultCase(target); }
      }
    });
  }
  
  /**
   * Prepare the given expression for assignment, wrapping it in any necessary conversions.
   * 
   * @return  An expression equivalent to {@code e}, wrapped in any necessary conversions
   * @throws  UnsupportedConversionException  If assignment to the given type is incorrect.
   */
  public Expression assign(final Type target, final Expression exp) throws UnsupportedConversionException {
    try {
      return target.apply(new TypeAbstractVisitor<Expression>() {
        
        public Expression defaultCase(final Type target) {
          return NodeProperties.getType(exp).apply(new TypeAbstractVisitor<Expression>() {
            public Expression defaultCase(Type t) {
              // TODO: Allow unchecked conversions from raw types (matching the spec is not trivial)
              if (isSubtype(t, target)) { return exp; }
              else { throw new WrappedException(new UnsupportedConversionException()); }
            }
            
            @Override public Expression forPrimitiveType(PrimitiveType t) {
              try {
                Expression boxed = makeReference(exp);
                if (isSubtype(NodeProperties.getType(boxed), target)) { return exp; }
                else { throw new UnsupportedConversionException(); }
              }
              catch (UnsupportedConversionException e) { throw new WrappedException(e); }
            }
            
            @Override public Expression forCharType(CharType t) {
              try {
                if (NodeProperties.hasValue(exp)) {
                  if (isEqual(target, BYTE_CLASS) && inRange(NodeProperties.getValue(exp), BYTE)) {
                    return makeReference(makeCast(BYTE, exp));
                  }
                  else if (isEqual(target, SHORT_CLASS) && inRange(NodeProperties.getValue(exp), SHORT)) {
                    return makeReference(makeCast(SHORT, exp));
                  }
                }
                return forPrimitiveType(t);
              }
              catch (UnsupportedConversionException e) { throw new WrappedException(e); }
            }
            
            @Override public Expression forByteType(ByteType t) {
              try {
                if (NodeProperties.hasValue(exp)) {
                  if (isEqual(target, CHARACTER_CLASS) && inRange(NodeProperties.getValue(exp), CHAR)) {
                    return makeReference(makeCast(CHAR, exp));
                  }
                }
                return forPrimitiveType(t);
              }
              catch (UnsupportedConversionException e) { throw new WrappedException(e); }
            }
            
            @Override public Expression forShortType(ShortType t) {
              try {
                if (NodeProperties.hasValue(exp)) {
                  if (isEqual(target, BYTE_CLASS) && inRange(NodeProperties.getValue(exp), BYTE)) {
                    return makeReference(makeCast(BYTE, exp));
                  }
                  else if (isEqual(target, CHARACTER_CLASS) && inRange(NodeProperties.getValue(exp), CHAR)) {
                    return makeReference(makeCast(CHAR, exp));
                  }
                }
                return forPrimitiveType(t);
              }
              catch (UnsupportedConversionException e) { throw new WrappedException(e); }
            }
            
            @Override public Expression forIntType(IntType t) {
              try {
                if (NodeProperties.hasValue(exp)) {
                  if (isEqual(target, BYTE_CLASS) && inRange(NodeProperties.getValue(exp), BYTE)) {
                    return makeReference(makeCast(BYTE, exp));
                  }
                  else if (isEqual(target, SHORT_CLASS) && inRange(NodeProperties.getValue(exp), SHORT)) {
                    return makeReference(makeCast(SHORT, exp));
                  }
                  else if (isEqual(target, CHARACTER_CLASS) && inRange(NodeProperties.getValue(exp), CHAR)) {
                    return makeReference(makeCast(CHAR, exp));
                  }
                }
                return forPrimitiveType(t);
              }
              catch (UnsupportedConversionException e) { throw new WrappedException(e); }
            }
          });
        }
        
        @Override public Expression forPrimitiveType(PrimitiveType target) {
          try {
            Expression unboxed = makePrimitive(exp);
            Type t = NodeProperties.getType(unboxed);
            if (isEqual(t, target)) { return unboxed; }
            else if (isSubtype(t, target)) { return makeCast(target, unboxed); }
            else { throw new UnsupportedConversionException(); }
          }
          catch (UnsupportedConversionException e) { throw new WrappedException(e); }
        }
        
        @Override public Expression forCharType(CharType target) { return handleSmallPrimitive(target); }
        @Override public Expression forByteType(ByteType target) { return handleSmallPrimitive(target); }
        @Override public Expression forShortType(ShortType target) { return handleSmallPrimitive(target); }
        
        private Expression handleSmallPrimitive(PrimitiveType target) {
          try {
            Expression unboxed = makePrimitive(exp);
            Type t = NodeProperties.getType(unboxed);
            if (NodeProperties.hasValue(unboxed) && t instanceof IntType && 
                inRange(NodeProperties.getValue(unboxed), target)) { return makeCast(target, unboxed); }
            else if (isEqual(t, target)) { return unboxed; }
            else if (isSubtype(t, target)) { return makeCast(target, unboxed); }
            else { throw new UnsupportedConversionException(); }
          }
          catch (UnsupportedConversionException e) { throw new WrappedException(e); }
        }
        
      });
    }
    catch (WrappedException e) {
      if (e.getCause() instanceof UnsupportedConversionException) {
        throw (UnsupportedConversionException) e.getCause();
      }
      else { throw e; }
    }
  }
  
  
  /**
   * Wrap {@code e} in a cast to type {@code target}.  The cast is assumed to be legal.  The
   * result will be a {@link CastExpression} with a {@code null} value for its {@code TypeName},
   * and with the {@code TYPE} property set.  {@code CONVERTED_TYPE} and {@code CHECKED_TYPE} may
   * also be set on {@code e}, as necessary.
   */
  private Expression makeCast(Type target, Expression e) {
    Expression result = new CastExpression(null, e, e.getSourceInfo());
    if (isPrimitive(target)) {
      if (!isEqual(target, NodeProperties.getType(e))) {
        NodeProperties.setConvertedType(result, erasedClass(target));
        if (NodeProperties.hasValue(e)) {
          Object orig = NodeProperties.getValue(e);
          Class<?> t = NodeProperties.getConvertedType(result).value();
          NodeProperties.setValue(result, ExpressionEvaluator.convert(orig, t));
        }
      }
      else {
        if (NodeProperties.hasValue(e)) { NodeProperties.setValue(result, NodeProperties.getValue(e)); }
      }
    }
    else {
      // TODO: Add a checked type property, if necessary
    }
    
    NodeProperties.setType(result, target);
    return result;
  }
  
  private Expression makeArray(ArrayType arrayType, Iterable<? extends Expression> elements) {
    Thunk<Class<?>> erasedType = erasedClass(arrayType);
    TypeName tn = TypeUtil.makeEmptyTypeName();
    // TODO: Is it necessary to create a type name that corresponds to the element type (this is not
    // possible in general, but possible in situations in which this method is called), or
    // is an "empty" type name sufficient?
    NodeProperties.setType(tn, arrayType.ofType());
    ArrayInitializer init = new ArrayInitializer(CollectUtil.makeList(elements));
    NodeProperties.setType(init, arrayType);
    NodeProperties.setErasedType(init, erasedType);
    Expression result = new ArrayAllocation(tn, new ArrayAllocation.TypeDescriptor(new ArrayList<Expression>(0), 
                                                                                   1, init, SourceInfo.NONE));
    NodeProperties.setType(result, arrayType);
    NodeProperties.setErasedType(result, erasedType);
    return result;
  }
  
  /**
   * True iff the given value (from a constant) is in range of the given type (regardless of whether
   * an assignment to type {@code t} would require a narrowing conversion)
   */
  private boolean inRange(final Object value, Type t) {
    if (isReference(t)) { return value == null; }
    else return t.apply(new TypeAbstractVisitor<Boolean>() {
      public Boolean defaultCase(Type t) { return false; }
      @Override public Boolean forBooleanType(BooleanType t) { return value instanceof Boolean; }
      @Override public Boolean forCharType(CharType t) { return checkNumber(Character.MIN_VALUE, Character.MAX_VALUE); }
      @Override public Boolean forByteType(ByteType t) { return checkNumber(Byte.MIN_VALUE, Byte.MAX_VALUE); }
      @Override public Boolean forShortType(ShortType t) { return checkNumber(Short.MIN_VALUE, Short.MAX_VALUE); }
      @Override public Boolean forIntType(IntType t) { return checkNumber(Integer.MIN_VALUE, Integer.MAX_VALUE); }
      @Override public Boolean forLongType(LongType t) { return checkNumber(Long.MIN_VALUE, Long.MAX_VALUE); }
      private Boolean checkNumber(long lowerBound, long upperBound) {
        if (value instanceof Number && !(value instanceof Float) && !(value instanceof Double)) {
          long val = ((Number) value).longValue();
          return lowerBound <= val && val <= upperBound;
        }
        else { return false; }
      }
    });
  }
  
  /** Get a class type's immediate supertype.  The result may be null. */
  protected Type immediateSupertype(ClassType t) {
    if (t.equals(OBJECT)) { return null; }
    else {
      final Iterable<Type> declaredSupers = t.ofClass().declaredSupertypes();
      if (IterUtil.isEmpty(declaredSupers)) { return OBJECT; }
      else {
        Iterable<? extends Type> instantiatedSupers = t.apply(new TypeAbstractVisitor<Iterable<? extends Type>>() {
          @Override public Iterable<? extends Type> defaultCase(Type t) { return declaredSupers; }
          @Override public Iterable<? extends Type> forRawClassType(RawClassType t) {
            return IterUtil.mapSnapshot(declaredSupers, ERASE);
          }
          @Override public Iterable<? extends Type> forParameterizedClassType(ParameterizedClassType t) {
            ParameterizedClassType tCap = capture(t);
            DJClass c = tCap.ofClass();
            return substitute(c.declaredSupertypes(), SymbolUtil.allTypeParameters(c), tCap.typeArguments());
          }
        });
        if (IterUtil.sizeOf(instantiatedSupers, 2) > 1) { return new IntersectionType(instantiatedSupers); }
        else { return IterUtil.first(instantiatedSupers); }
      }
    }
  }
    
  /**
   * Lookup the constructor corresponding the the given invocation.
   * @param t  The type of the object to be constructed.
   * @param typeArgs  The type arguments for the constructor's type parameters.
   * @param args  A list of typed expressions corresponding to the constructor's parameters.
   * @param expected  The type expected in the invocation's calling context, if any.
   * @return  A {@link TypeSystem.ConstructorInvocation} object representing the matched constructor.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 constructor matches the given arguments and type 
   *                                   arguments.
   */
  // TODO: Must produce a reasonable value when looking up a constructor in an interface (for anonymous classes)
  public ConstructorInvocation lookupConstructor(final Type t, final Iterable<? extends Type> typeArgs, 
                                                 final Iterable<? extends Expression> args,
                                                 final Option<Type> expected, final Access.Module accessModule)
    throws InvalidTypeArgumentException, UnmatchedLookupException {
    debug.logStart(new String[]{"t","typeArgs","arg types","expected"},
                   wrap(t), wrap(typeArgs), wrap(IterUtil.map(args, NodeProperties.NODE_TYPE)), wrap(expected)); try {
                     
    Iterable<DJConstructor> constructors = 
      t.apply(new TypeAbstractVisitor<Iterable<DJConstructor>>() {
        @Override public Iterable<DJConstructor> defaultCase(Type t) { return IterUtil.empty(); }
        @Override public Iterable<DJConstructor> forSimpleClassType(SimpleClassType t) {
          return t.ofClass().declaredConstructors();
        }
        @Override public Iterable<DJConstructor> forRawClassType(RawClassType t) {
          return IterUtil.mapSnapshot(t.ofClass().declaredConstructors(), new Lambda<DJConstructor, DJConstructor>() {
            public DJConstructor value(DJConstructor k) {
              // TODO: raw member access warnings
              return new ErasedConstructor(k);
            }
          });
        }
        @Override public Iterable<DJConstructor> forParameterizedClassType(ParameterizedClassType t) {
          final Iterable<VariableType> classTParams = SymbolUtil.allTypeParameters(t.ofClass());
          final Iterable<? extends Type> classTArgs = capture(t).typeArguments();
          return IterUtil.mapSnapshot(t.ofClass().declaredConstructors(), new Lambda<DJConstructor, DJConstructor>() {
            public DJConstructor value(DJConstructor k) {
              return new InstantiatedConstructor(k, classTParams, classTArgs);
            }
          });
        }
      });
    
    Iterable<DJConstructor> accessible = IterUtil.filter(constructors, new Predicate<DJConstructor>() {
      public boolean contains(DJConstructor k) { return accessible(k, accessModule); }
    });
    Iterable<FunctionInvocationCandidate<DJConstructor>> cs = bestInvocations(accessible, typeArgs, args, expected);
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(cs);
    if (matches == 0) { throw new UnmatchedFunctionLookupException(constructors); }
    else if (matches > 1) {
      Iterable<DJConstructor> ks = IterUtil.map(cs, new Lambda<FunctionInvocationCandidate<DJConstructor>,
                                                               DJConstructor>() {
        public DJConstructor value(FunctionInvocationCandidate<DJConstructor> c) { return c.function(); }
      });
      throw new AmbiguousFunctionLookupException(ks);
    }
    else {
      FunctionInvocationCandidate<DJConstructor> c = IterUtil.first(cs);
      DJConstructor k = c.function();
      SubstitutionMap sigma = c.substitution();
      return new ConstructorInvocation(k, c.typeArguments(), c.arguments(), substitute(k.thrownTypes(), sigma));
    }
    
    } finally { debug.logEnd(); }
  }
  
  public boolean containsMethod(Type t, String name, Access.Module accessModule) {
    return new MethodFinder(name, accessModule, false).hasMatch(t);
  }
  public boolean containsStaticMethod(Type t, String name, Access.Module accessModule) {
    return new MethodFinder(name, accessModule, true).hasMatch(t);
  }
  
  public ObjectMethodInvocation lookupMethod(Expression object, String name, 
                                             Iterable<? extends Type> typeArgs, 
                                             Iterable<? extends Expression> args,
                                             Option<Type> expected, Access.Module accessModule)
      throws InvalidTypeArgumentException, UnmatchedLookupException {
    Type t = NodeProperties.getType(object);
    FunctionInvocationCandidate<DJMethod> result =
        new MethodFinder(name, accessModule, false).findSingleMethod(t, typeArgs, args, expected);
    DJMethod m = result.function();
    SubstitutionMap sigma = result.substitution();
    // TODO: Is there any reason to invoke makeCast on the receiver?
    return new ObjectMethodInvocation(m, substitute(m.returnType(), sigma), object, result.typeArguments(),
                                      result.arguments(), substitute(m.thrownTypes(), sigma));
  }
  
  public StaticMethodInvocation lookupStaticMethod(Type t, String name, 
                                                   Iterable<? extends Type> typeArgs, 
                                                   Iterable<? extends Expression> args,
                                                   Option<Type> expected, Access.Module accessModule)
    throws InvalidTypeArgumentException, UnmatchedLookupException {
    FunctionInvocationCandidate<DJMethod> result =
        new MethodFinder(name, accessModule, true).findSingleMethod(t, typeArgs, args, expected);
    DJMethod m = result.function();
    SubstitutionMap sigma = result.substitution();
    return new StaticMethodInvocation(m, substitute(m.returnType(), sigma), result.typeArguments(),
                                      result.arguments(), substitute(m.thrownTypes(), sigma));
  }
  
  public boolean containsField(Type t, String name, Access.Module accessModule) {
    return containsField(t, name, accessModule, false);
  }
  public boolean containsStaticField(Type t, String name, Access.Module accessModule) {
    return containsField(t, name, accessModule, true);
  }
  private boolean containsField(Type t, String name, Access.Module accessModule, boolean onlyStatic) {
    FieldFinder<FieldReference> finder = new FieldFinder<FieldReference>(name, accessModule, onlyStatic) {
      protected FieldReference makeFieldReference(Type t, DJField f) {
        return new FieldReference(f, BOTTOM) {}; // anonymous stub just used for inheritance checking
      }
    };
    return finder.hasMatch(t);
  }
  
  public ObjectFieldReference lookupField(final Expression object, String name, Access.Module accessModule)
    throws UnmatchedLookupException {
    FieldFinder<ObjectFieldReference> finder = new FieldFinder<ObjectFieldReference>(name, accessModule, false) {
      public ObjectFieldReference makeFieldReference(Type t, DJField f) {
        return new ObjectFieldReference(f, fieldType(f, t), makeCast(t, object));
      }
    };
    return finder.findSingleField(NodeProperties.getType(object));
  }
  
  public StaticFieldReference lookupStaticField(Type t, String name, Access.Module accessModule)
    throws UnmatchedLookupException {
    FieldFinder<StaticFieldReference> finder = new FieldFinder<StaticFieldReference>(name, accessModule, true) {
      public StaticFieldReference makeFieldReference(Type t, DJField f) {
        return new StaticFieldReference(f, fieldType(f, t));
      }
    };
    return finder.findSingleField(t);
  }
  
  private Type fieldType(final DJField f, Type declaringType) {
    Type dynamicContext;
    if (f.isStatic()) {
      if (declaringType instanceof ClassType) {
        dynamicContext = SymbolUtil.dynamicOuterClassType((ClassType) declaringType);
      }
      else { dynamicContext = null; }
    }
    else { dynamicContext = declaringType; }
    if (dynamicContext == null) { return f.type(); }
    else {
      return dynamicContext.apply(new TypeAbstractVisitor<Type>() {
        @Override public Type defaultCase(Type dynamicContext) { return f.type(); }
        @Override public Type forRawClassType(RawClassType dynamicContext) {
          // TODO: raw member access warnings
          return erase(f.type());
        }
        @Override public Type forParameterizedClassType(ParameterizedClassType dynamicContext) {
          ParameterizedClassType dynamicContextCap = capture(dynamicContext);
          Iterable<VariableType> tparams = SymbolUtil.allTypeParameters(dynamicContextCap.ofClass());
          return substitute(f.type(), tparams, dynamicContextCap.typeArguments());
        }
      });
    }
  }
  
  public boolean containsClass(Type t, String name, Access.Module accessModule) {
    return new ClassFinder(name, EMPTY_TYPE_ITERABLE, accessModule, false).hasMatch(t);
  }
  
  public boolean containsStaticClass(Type t, String name, Access.Module accessModule) {
    return new ClassFinder(name, EMPTY_TYPE_ITERABLE, accessModule, true).hasMatch(t);
  }
  
  public ClassType lookupClass(Expression object, String name, Iterable<? extends Type> typeArgs,
                               Access.Module accessModule)
    throws InvalidTypeArgumentException, UnmatchedLookupException {
    return new ClassFinder(name, typeArgs, accessModule, false).findSingleClass(NodeProperties.getType(object));
  }
  
  public ClassType lookupClass(Type t, String name, Iterable<? extends Type> typeArgs, Access.Module accessModule)
    throws InvalidTypeArgumentException, UnmatchedLookupException {
    return new ClassFinder(name, typeArgs, accessModule, false).findSingleClass(t);
  }
  
  public ClassType lookupStaticClass(Type t, final String name, final Iterable<? extends Type> typeArgs,
                                     Access.Module accessModule)
    throws InvalidTypeArgumentException, UnmatchedLookupException {
    return new ClassFinder(name, typeArgs, accessModule, true).findSingleClass(t);
  }
  
  /**
   * Determine whether the given symbol should be considered accessible from the given accessing context
   * (for the purposes of lookup).
   */
  private boolean accessible(Access.Limited symbol, Access.Module accessingModule) {
    switch (symbol.accessibility()) {
      case PRIVATE:
        return (!_opt.enforcePrivateAccess() && !_opt.enforceAllAccess()) ||
                symbol.accessModule().equals(accessingModule);
      case PACKAGE:
        return !_opt.enforceAllAccess() ||
                symbol.accessModule().packageName().equals(accessingModule.packageName());
      case PROTECTED:
        // TODO: implement protected-access checks
        return !_opt.enforceAllAccess() || true;
      default:
        return true; // public access is always valid
    }
  }
  
  /**
   * Test whether the given arguments are within the bounds of the corresponding parameters.  Assumes
   * that {@code params} and {@code args} have matching arity.
   * 
   * @return  {@code true} iff the given arguments are within the bounds of the given parameters
   */
  protected boolean inBounds(Iterable<? extends VariableType> params, Iterable<? extends Type> args) {
    SubstitutionMap sigma = new SubstitutionMap(params, args);
    for (Pair<VariableType, Type> pair : IterUtil.zip(params, args)) {
      VariableType param = pair.first();
      Type arg = pair.second();
      if (!isSubtype(substitute(param.symbol().lowerBound(), sigma), arg)) { return false; }
      if (!isSubtype(arg, substitute(param.symbol().upperBound(), sigma))) { return false; }
    }
    return true;
  }
  
  
  private class StandardTypePrinter implements TypePrinter {
    
    private final Map<VariableType, String> _names = new HashMap<VariableType, String>();
    int _captureVars = 0;
  
    public String print(Type t) {
      Visitor v = new Visitor();
      v.run(t);
      v.appendConstraints();
      return v.result();
    }
    
    public String print(Iterable<? extends Type> ts) {
      Visitor v = new Visitor();
      v.runOnList(ts, ", ");
      v.appendConstraints();
      return v.result();
    }
    
    public String print(Function f) {
      Visitor v = new Visitor();
      if (!IterUtil.isEmpty(f.typeParameters())) {
        v.append("<");
        v.runOnList(f.typeParameters(), ", ");
        v.append("> ");
      }
      if (!(f instanceof DJConstructor)) {
        v.run(f.returnType());
        v.append(" ");
      }
      v.append(f.declaredName());
      v.append("(");
      v.runOnList(SymbolUtil.parameterTypes(f), ", ");
      v.append(")");
      v.appendConstraints();
      return v.result();
    }
    
    private class Visitor extends TypeVisitorRunnable1 {
      private final StringBuilder _result = new StringBuilder();
      private final RecursionStack<Type> _stack = new RecursionStack<Type>();
      // variables that have bounds that should be printed in appendConstraints()
      private final List<VariableType> _boundedVars = new ArrayList<VariableType>();
      private final Set<VariableType> _seenVars = new HashSet<VariableType>();
      
      public String result() { return _result.toString(); }
      
      public void append(String s) { _result.append(s); }
  
      public void appendConstraints() {
        if (!_boundedVars.isEmpty()) {
          _result.append(" [");
          // not using an iterator because the list may grow during iteration
          for (int i = 0; i < _boundedVars.size(); i++) {
            if (i > 0) { _result.append("; "); }
            VariableType v = _boundedVars.get(i);
            Type upper = v.symbol().upperBound();
            Type lower = v.symbol().lowerBound();
            boolean printUpper = !isEqual(upper, OBJECT) /*&& !isEqual(upper, TOP)*/;
            boolean printLower = !isEqual(lower, NULL) /*&& !isEqual(lower, BOTTOM)*/;
            if (printUpper) {
              _result.append(nameForVariable(v));
              _result.append(" <: ");
              run(upper); // may increase the size of _vars
            }
            if (printLower) {
              if (printUpper) { _result.append(", "); }
              _result.append(nameForVariable(v));
              _result.append(" :> ");
              run(lower); // may increase the size of _vars
            }
          }
          _result.append("]");
        }
      }
      
      private String nameForVariable(VariableType t) {
        String name = _names.get(t);
        if (name == null) {
          if (t.symbol().generated()) { _captureVars++; name = "?T" + _captureVars; }
          else { name = t.symbol().name(); }
          _names.put(t, name);
        }
        if (!_seenVars.contains(t)) {
          _seenVars.add(t);
          // check to see if bounds need to be printed
          Type upper = t.symbol().upperBound();
          Type lower = t.symbol().lowerBound();
          boolean printUpper = !isEqual(upper, OBJECT) /*&& !isEqual(upper, TOP)*/;
          boolean printLower = !isEqual(lower, NULL) /*&& !isEqual(lower, BOTTOM)*/;
          if (printUpper || printLower) { _boundedVars.add(t); }
        }
        return name;
      }
      
      /**
       * Running is preferred over applying the visitor, as invoking this will put the
       * value being processed on the stack, and avoid unnecessary repetition
       */
      @Override public void run(final Type t) {
  //        String prefix = ""; for (int i = 0; i < _stack.size(); i++) { prefix += "  "; }
  //        System.out.println(prefix + "Running on id " + System.identityHashCode(t) + ": " + t);
        Runnable recur = new Runnable() { public void run() { t.apply(Visitor.this); } };
        Runnable dontRecur = new Runnable() { public void run() { _result.append("..."); } };
        //Threshold of 2 causes the loop to be printed twice
        _stack.run(recur, dontRecur, t/*, 2*/); 
      }
      
      public void runOnList(Iterable<? extends Type> ts, String delim) {
        boolean first = true;
        for (Type t : ts) {
          if (!first) { _result.append(delim); }
          first = false;
          run(t);
        }
      }
      
      public void forBooleanType(BooleanType t) { _result.append("boolean"); }
      public void forCharType(CharType t) { _result.append("char"); }
      public void forByteType(ByteType t) { _result.append("byte"); }
      public void forShortType(ShortType t) { _result.append("short"); }
      public void forIntType(IntType t) { _result.append("int"); }
      public void forLongType(LongType t) { _result.append("long"); }
      public void forFloatType(FloatType t) { _result.append("float"); }
      public void forDoubleType(DoubleType t) { _result.append("double"); }
      public void forNullType(NullType t) { _result.append("(null)"); }
      public void forVoidType(VoidType t) { _result.append("void"); }
      public void forTopType(TopType t) { _result.append("(top)"); }
      public void forBottomType(BottomType t) { _result.append("(bottom)"); }
      
      public void forSimpleArrayType(SimpleArrayType t) {
        run(t.ofType());
        _result.append("[]");
      }
      
      public void forVarargArrayType(VarargArrayType t) {
        run(t.ofType());
        _result.append("...");
      }
      
      public void forSimpleClassType(SimpleClassType t) { appendClassName(t.ofClass()); }
      
      public void forRawClassType(RawClassType t) {
        _result.append("raw ");
        appendClassName(t.ofClass());
      }
      
      public void forParameterizedClassType(ParameterizedClassType t) {
        Iterator<DJClass> classes = SymbolUtil.outerClassChain(t.ofClass()).iterator();
        Iterator<? extends Type> targs = t.typeArguments().iterator();
        DJClass c = classes.next();
        appendClassName(c);
        DJClass inner;
        while (c != null) {
          inner = classes.hasNext() ? classes.next() : null; // next in the chain, or null if c is last
          if (inner == null || !inner.isStatic()) {
            Iterable<VariableType> params = c.declaredTypeParameters();
            if (!IterUtil.isEmpty(params)) {
              _result.append("<");
              boolean firstParam = true;
              for (VariableType param : params) { // param is ignored -- it's just a counter
                if (!firstParam) { _result.append(", "); }
                firstParam = false;
                run(targs.next());
              }
              _result.append(">");
            }
          }
          if (inner != null) { _result.append("."); _result.append(inner.declaredName()); }
          c = inner;
        }
      }
      
      private void appendClassName(DJClass c) {
        if (c.isAnonymous()) {
          _result.append("anonymous ");
          runOnList(c.declaredSupertypes(), " & ");
        }
        else { _result.append(SymbolUtil.shortName(c)); }
      }
      
      public void forVariableType(VariableType t) {
        _result.append(nameForVariable(t));
      }
      
      public void forIntersectionType(IntersectionType t) {
        int size = IterUtil.sizeOf(t.ofTypes());
        if (size == 0) { _result.append("(empty intersect)"); }
        else if (size == 1) {
          _result.append("(intersect ");
          run(IterUtil.first(t.ofTypes()));
          _result.append(")");
        }
        else { runOnList(t.ofTypes(), " & "); }
      }
      
      public void forUnionType(UnionType t) {
        int size = IterUtil.sizeOf(t.ofTypes());
        if (size == 0) { _result.append("(empty union)"); }
        else if (size == 1) {
          _result.append("(union ");
          run(IterUtil.first(t.ofTypes()));
          _result.append(")");
        }
        else { runOnList(t.ofTypes(), " | "); }
      }
      
      public void forWildcard(Wildcard t) {
        _result.append("?");
        if (!isEqual(t.symbol().upperBound(), OBJECT)) {
          _result.append(" extends ");
          run(t.symbol().upperBound());
        }
        if (!isEqual(t.symbol().lowerBound(), NULL)) {
          _result.append(" super ");
          run(t.symbol().lowerBound());
        }
      }
      
    }
    
  }

  /**
   * Implementation of member lookup.  Subclasses provide an implementation of {@link #declaredMatches},
   * which produces the set of matches for a particular type.  Traversal of the type hierarchy
   * is managed here.
   */
  private abstract class MemberFinder<T> {
    /** Test whether a search of the given type produces at least one result. */
    public boolean hasMatch(Type t) { return !IterUtil.isEmpty(findFirst(t)); }
    /**
     * Produce the set of matching members least-removed from type t.  If t declares at least one
     * match, no supertype recursion takes place.
     */
    public PredicateSet<T> findFirst(Type t) { return find(t, false); }
    
    /** Produce all matching members of t (both declared and inherited). */
    public PredicateSet<T> findAll(Type t) { return find(t, true); }
    
    /** Get a list of all declared members of t that match. */
    protected abstract Iterable<T> declaredMatches(Type t);
    
    /** Whether the child type inherits the given match from a supertype. */
    protected abstract boolean inherits(Type child, PredicateSet<T> childMatches, T match); 
    
    private PredicateSet<T> find(final Type t, final boolean findAll) {
      debug.logStart("t", wrap(t)); try {
        
      final PredicateSet<T> childMatches = CollectUtil.asPredicateSet(declaredMatches(t));
      if (!findAll && !IterUtil.isEmpty(childMatches)) {
        // take snapshot of lazily-constructed set
        return CollectUtil.makeSet(childMatches);
      }
      else {
        PredicateSet<T> fromSupers = t.apply(new TypeAbstractVisitor<PredicateSet<T>>() {
          
          public PredicateSet<T> defaultCase(Type t) { return CollectUtil.emptySet(); }
          
          @Override public PredicateSet<T> forArrayType(ArrayType t) {
            return find(CLONEABLE_AND_SERIALIZABLE, findAll);
          }
          
          @Override public PredicateSet<T> forClassType(ClassType t) {
            Type superT = immediateSupertype(t);
            if (superT == null) { return CollectUtil.emptySet(); }
            else { return find(superT, findAll); }
          }
          
          @Override public PredicateSet<T> forVariableType(VariableType t) {
            return find(t.symbol().upperBound(), findAll);
          }
          
          @Override public PredicateSet<T> forIntersectionType(IntersectionType t) {
            PredicateSet<T> result = CollectUtil.emptySet();
            for (Type tSup : t.ofTypes()) {
              PredicateSet<T> forSup = find(tSup, findAll);
              result = CollectUtil.union(result, forSup);
            }
            return result;
          }
          
          @Override public PredicateSet<T> forUnionType(UnionType t) {
            Iterable<? extends Type> sups = t.ofTypes();
            if (IterUtil.isEmpty(sups)) { return CollectUtil.emptySet(); }
            else {
              PredicateSet<T> result = find(IterUtil.first(sups), findAll);
              for (Type tSup : IterUtil.skipFirst(sups)) {
                PredicateSet<T> forSup = find(tSup, findAll);
                result = CollectUtil.intersection(result, forSup);
                // don't short-circuit when empty, because the empty test is expensive
              }
              return result;
            }
          }
          
        });
        PredicateSet<T> result = CollectUtil.union(childMatches, CollectUtil.filter(fromSupers, new Predicate<T>() {
          public boolean contains(T match) { return inherits(t, childMatches, match); }
        }));
        // take snapshot of lazily-constructed sets
        return CollectUtil.makeSet(result);
      }
      
      } finally { debug.logEnd(); }
    }
  }
  
  private class MethodFinder extends MemberFinder<DJMethod> {
    private final String _name;
    private final Access.Module _accessModule;
    private final boolean _onlyStatic;
    protected MethodFinder(String name, Access.Module accessModule, boolean onlyStatic) {
      _name = name;
      _accessModule = accessModule;
      _onlyStatic = onlyStatic;
    }
    
    /** Search for matching methods and determine the best applicable instance, if any. */
    public FunctionInvocationCandidate<DJMethod>
        findSingleMethod(Type t, Iterable<? extends Type> targs, Iterable<? extends Expression> args,
                         Option<Type> expected) throws UnmatchedLookupException {
      debug.logStart(new String[]{"t","name","onlyStatic"}, wrap(t), _name, _onlyStatic); try {
        
      PredicateSet<DJMethod> candidates = findAll(t);
      Iterable<FunctionInvocationCandidate<DJMethod>> best = bestInvocations(candidates, targs, args, expected);
      // TODO: provide more error-message information
      int matches = IterUtil.sizeOf(best);
      if (matches == 0) { throw new UnmatchedFunctionLookupException(candidates); }
      else if (matches > 1) {
        Iterable<DJMethod> ms = IterUtil.map(best, new Lambda<FunctionInvocationCandidate<DJMethod>, DJMethod>() {
          public DJMethod value(FunctionInvocationCandidate<DJMethod> c) { return c.function(); }
        });
        throw new AmbiguousFunctionLookupException(ms);
      }
      else { return IterUtil.first(best); }
      
      } finally { debug.logEnd(); }
    }
    
    protected Iterable<DJMethod> declaredMatches(Type t) {
      return t.apply(new TypeAbstractVisitor<Iterable<DJMethod>>() {
        private boolean matches(DJMethod m) {
          return m.declaredName().equals(_name) && !(_onlyStatic && !m.isStatic()) && accessible(m, _accessModule);
        }
        @Override public Iterable<DJMethod> defaultCase(Type t) { return IterUtil.empty(); }
        @Override public Iterable<DJMethod> forClassType(ClassType t) {
          List<DJMethod> result = new LinkedList<DJMethod>();
          for (DJMethod m : t.ofClass().declaredMethods()) {
            if (matches(m)) { result.add(instantiateMethod(m, t)); }
          }
          return result;
        }
      });
    }

    protected boolean inherits(Type child, PredicateSet<DJMethod> childMatches, DJMethod match) {
      // TODO: follow the JLS definition of method inheritance (8.4.8)
      if (match.accessibility().equals(Access.PRIVATE)) { return false; }
      else {
        for (DJMethod childMethod : childMatches) {
          if (overrides(childMethod, match)) { return false; }
        }
        return true;
      }
    }

    /** True iff child is override-compatible with the parent.  (See JLS 8.4.2.) */
    private boolean overrides(DJMethod child, DJMethod parent) {
      if (child.declaredName().equals(parent.declaredName())) {
        Iterable<Type> subParams = SymbolUtil.parameterTypes(child);
        Iterable<Type> supParams = SymbolUtil.parameterTypes(parent);
        Iterable<VariableType> subTParams = child.typeParameters();
        Iterable<VariableType> supTParams = parent.typeParameters();
        if (IterUtil.sizeOf(subParams) == IterUtil.sizeOf(supParams)) {
          Iterable<? extends Type> supParamsToCompare;
          if (IterUtil.isEmpty(subTParams) && !IterUtil.isEmpty(supTParams)) {
            supParamsToCompare = IterUtil.map(supParams, ERASE);
          }
          else if (IterUtil.sizeOf(subTParams) == IterUtil.sizeOf(supTParams)) {
            supParamsToCompare = substitute(supParams, supTParams, subTParams);
          }
          else { return false; }
          for (Pair<Type, Type> p : IterUtil.zip(subParams, supParamsToCompare)) {
            if (!isEqual(p.first(), p.second())) { return false; }
          }
          return true;
        }
        else { return false; }
      }
      else { return false; }
    }
    
  }
  
  private abstract class FieldFinder<T extends FieldReference> extends MemberFinder<T> {
    private final String _name;
    private final Access.Module _accessModule;
    private final boolean _onlyStatic;
    protected FieldFinder(String name, Access.Module accessModule, boolean onlyStatic) {
      _name = name;
      _accessModule = accessModule;
      _onlyStatic = onlyStatic;
    }
    
    /** If a single field is produced by findFirst(t), return it; otherwise, throw an exception. */
    public T findSingleField(Type t) throws UnmatchedLookupException {
      debug.logStart(new String[]{"t","name","onlyStatic"}, wrap(t), _name, _onlyStatic); try {
        
      Iterable<T> results = findFirst(t);
      // TODO: provide more error-message information
      int matches = IterUtil.sizeOf(results);
      if (matches != 1) { throw new UnmatchedLookupException(matches); }
      else { return IterUtil.first(results); }
      
      } finally { debug.logEnd(); }
    }
    
    protected abstract T makeFieldReference(Type declaringType, DJField field);
    
    protected Iterable<T> declaredMatches(Type t) {
      Iterable<T> result = t.apply(new TypeAbstractVisitor<Iterable<T>>() {
        private boolean matches(DJField f) {
          return f.declaredName().equals(_name) && !(_onlyStatic && !f.isStatic()) && accessible(f, _accessModule);
        }
        @Override public Iterable<T> defaultCase(Type t) { return IterUtil.empty(); }
        @Override public Iterable<T> forArrayType(ArrayType t) {
          if (_name.equals("length") && !_onlyStatic) {
            return IterUtil.make(makeFieldReference(t, ArrayLengthField.INSTANCE));
          }
          else { return IterUtil.empty(); }
        }
        @Override public Iterable<T> forClassType(ClassType t) {
          for (DJField f : t.ofClass().declaredFields()) {
            if (matches(f)) { return IterUtil.make(makeFieldReference(t, f)); }
          }
          return IterUtil.empty();
        }
      });
      return result;
    }

    protected boolean inherits(Type child, PredicateSet<T> childMatches, T match) {
      // TODO: follow the JLS definition of field inheritance (8.3)
      return childMatches.isEmpty() && !match.field().accessibility().equals(Access.PRIVATE);
    }

  }
  
  private class ClassFinder extends MemberFinder<ClassType> {
    private final String _name;
    private final Iterable<? extends Type> _typeArgs;
    private final Access.Module _accessModule;
    private final boolean _onlyStatic;
    protected ClassFinder(String name, Iterable<? extends Type> typeArgs, Access.Module accessModule,
                          boolean onlyStatic) {
      _name = name;
      _typeArgs = typeArgs;
      _accessModule = accessModule;
      _onlyStatic = onlyStatic;
    }
    
    /**
     * If a single class is produced by findFirst(t) and the type arguments are valid, return the
     * type; otherwise, throw an exception.
     */
    public ClassType findSingleClass(Type t) throws InvalidTypeArgumentException, UnmatchedLookupException {
      debug.logStart(new String[]{"t","name","typeArgs", "onlyStatic"},
                     wrap(t), _name, wrap(_typeArgs), _onlyStatic); try {
                       
      Iterable<ClassType> results = findFirst(t);
      // TODO: provide more error-message information
      int matches = IterUtil.sizeOf(results);
      if (matches != 1) { throw new UnmatchedLookupException(matches); }
      else {
        ClassType result = IterUtil.first(results);
        final Iterable<VariableType> params = SymbolUtil.allTypeParameters(result.ofClass());
        try {
          return result.apply(new TypeAbstractVisitor<ClassType>() {
            public ClassType defaultCase(Type t) { throw new IllegalArgumentException(); }
            
            @Override public ClassType forSimpleClassType(SimpleClassType t) {
              if (IterUtil.isEmpty(params)) { return t; }
              else { return new RawClassType(t.ofClass()); }
            }
            
            @Override public ClassType forRawClassType(RawClassType t) {
              return t;
            }
            
            @Override public ClassType forParameterizedClassType(ParameterizedClassType t) {
              try {
                if (IterUtil.sizeOf(params) != IterUtil.sizeOf(t.typeArguments())) {
                  throw new InvalidTypeArgumentException();
                }
                return t;
              }
              catch (InvalidTypeArgumentException e) { throw new WrappedException(e); }
            }
          });
        }
        catch (WrappedException e) {
          if (e.getCause() instanceof InvalidTypeArgumentException) {
            throw (InvalidTypeArgumentException) e.getCause();
          }
          else { throw e; }
        }
      }
      
      } finally { debug.logEnd(); }
    }
    
    /**
     * The given type arguments are applied to the result, but no checks are
     * made for their correctness (a ParameterizedClassType result may have the wrong number of
     * arguments, including the case of missing arguments from a raw outer type; a SimpleClassType
     * result may be missing arguments).
     */
    protected Iterable<ClassType> declaredMatches(Type t) {
      return t.apply(new TypeAbstractVisitor<Iterable<ClassType>>() {
        
        @Override public Iterable<ClassType> defaultCase(Type t) { return IterUtil.empty(); }
        
        @Override public Iterable<ClassType> forClassType(final ClassType t) {
            
          Predicate<DJClass> matchInner = new Predicate<DJClass>() {
            public boolean contains(DJClass c) {
              return !c.isAnonymous() && c.declaredName().equals(_name) && !(_onlyStatic && !c.isStatic()) &&
                      accessible(c, _accessModule);
            }
          };
          Lambda<DJClass, ClassType> makeType = new Lambda<DJClass, ClassType>() {
            public ClassType value(DJClass c) {
              ClassType dynamicOuter; // may be null
              if (c.isStatic()) { dynamicOuter = SymbolUtil.dynamicOuterClassType(t); }
              else { dynamicOuter = t; }
              if (dynamicOuter instanceof ParameterizedClassType) {
                Iterable<? extends Type> outerTypeArgs = ((ParameterizedClassType) dynamicOuter).typeArguments();
                return new ParameterizedClassType(c, IterUtil.compose(outerTypeArgs, _typeArgs));
              }
              else if (dynamicOuter instanceof RawClassType) {
                // malformed if type args is nonempty -- that should be caught by findSingleClass
                return IterUtil.isEmpty(_typeArgs) ? new RawClassType(c) : new ParameterizedClassType(c, _typeArgs);
              }
              else {
                return IterUtil.isEmpty(_typeArgs) ? new SimpleClassType(c) : new ParameterizedClassType(c, _typeArgs);
              }
            }
          };
          return IterUtil.map(IterUtil.filter(t.ofClass().declaredClasses(), matchInner), makeType);
        }
          
      });
    }

    protected boolean inherits(Type child, PredicateSet<ClassType> childMatches, ClassType match) {
      // TODO: follow the JLS definition of class inheritance (8.5)
      return childMatches.isEmpty() && !match.ofClass().accessibility().equals(Access.PRIVATE);
    }

  }
  
  private class FunctionInvocationCandidate<F extends Function> {
    private final F _f;
    private final SignatureMatcher _matcher;
    
    public FunctionInvocationCandidate(F f, Iterable<? extends Type> targs,
                                       Iterable<? extends Expression> args, Option<Type> expected) {
      _f = f;
      _matcher = makeMatcher(f.typeParameters(), targs, SymbolUtil.parameterTypes(f), args, f.returnType(), expected);
    }
    
    public F function() { return _f; }
    public Iterable<? extends Type> typeArguments() { return _matcher.typeArguments(); }
    public Iterable<? extends Expression> arguments() { return _matcher.arguments(); }
    
    public SubstitutionMap substitution() {
      return new SubstitutionMap(_f.typeParameters(), _matcher.typeArguments());
    }
    
    private SignatureMatcher makeMatcher(Iterable<? extends VariableType> tparams,
                                         Iterable<? extends Type> targs,
                                         Iterable<? extends Type> params,
                                         Iterable<? extends Expression> args,
                                         Type returned, Option<Type> expected) {
      // Note: per the JLS, we allow the presence of (ignored) targs when tparams is empty
      int argCount = IterUtil.sizeOf(args);
      int paramCount = IterUtil.sizeOf(params);
      if (argCount == paramCount - 1) {
        if (IterUtil.isEmpty(tparams)) {
          return new EmptyVarargMatcher(params, args, tparams, EMPTY_TYPE_ITERABLE);
        }
        else if (IterUtil.isEmpty(targs)) {
          return new EmptyVarargInferenceMatcher(params, args, tparams, returned, expected);
        }
        else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) {
          return new EmptyVarargMatcher(substitute(params, tparams, targs), args, tparams, targs);
        }
        else { return NullMatcher.INSTANCE; }
      }
      else if (argCount == paramCount) {
        if (IterUtil.isEmpty(tparams)) { 
          return new SimpleMatcher(params, args, tparams, EMPTY_TYPE_ITERABLE);
        }
        else if (IterUtil.isEmpty(targs)) {
          return new InferenceMatcher(params, args, tparams, returned, expected);
        }
        else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) { 
          return new SimpleMatcher(substitute(params, tparams, targs), args, tparams, targs);
        }
        else { return NullMatcher.INSTANCE; }
      }
      else if (argCount > paramCount && paramCount >= 1) {
        if (IterUtil.isEmpty(tparams)) { 
          return new MultiVarargMatcher(params, args, tparams, EMPTY_TYPE_ITERABLE);
        }
        else if (IterUtil.isEmpty(targs)) {
          return new MultiVarargInferenceMatcher(params, args, tparams, returned, expected);
        }
        else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) {
          return new MultiVarargMatcher(substitute(params, tparams, targs), args, tparams, targs);
        }
        else { return NullMatcher.INSTANCE; }
      }
      else { return NullMatcher.INSTANCE; }
    }
    
    /**
     * True iff this declaration's parameters could be used to invoke c.  Must only be invoked with matched
     * SignatureMatchers.  This relation is defined in JLS 15.12.2.5.
     */
    public boolean moreSpecificThan(FunctionInvocationCandidate<F> c) {
      Iterable<Type> supParams = SymbolUtil.parameterTypes(c._f);
      Iterable<Type> subParams = SymbolUtil.parameterTypes(_f);
      if (SymbolUtil.isVararg(c._f)) {
        // Adjust param type lists to match arities, if possible.  Can't use SignatureMatcher.matchesWithVarargs()
        // because it uses boxing, which is not allowed here.
        int supArity = IterUtil.sizeOf(supParams);
        int subArity = IterUtil.sizeOf(subParams);
        if (SymbolUtil.isVararg(_f)) {
          if (subArity < supArity) { // fill in extra sub args
            Iterable<Type> prefixSubs = IterUtil.skipLast(subParams);
            Type lastSub = IterUtil.last(subParams);
            Iterable<Type> subExtras = IterUtil.copy(((ArrayType) lastSub).ofType(), supArity-subArity);
            subParams = IterUtil.compose(IterUtil.compose(prefixSubs, subExtras), lastSub);
          }
          else if (subArity > supArity) {
            Iterable<Type> prefixSups = IterUtil.skipLast(supParams);
            Type lastSup = IterUtil.last(supParams);
            Iterable<Type> supExtras = IterUtil.copy(((ArrayType) lastSup).ofType(), subArity-supArity);
            supParams = IterUtil.compose(IterUtil.compose(prefixSups, supExtras), lastSup);
          }
          // if they're equal, no adjustments are needed
        }
        else {
          // This case doesn't occur in practice, because "more specific" is only used to compare
          // methods that all match in the same phase (directly, with boxing, or with varargs).
          if (subArity < supArity) { supParams = IterUtil.skipLast(supParams); } // allow sub to elide the vararg param
          else if (subArity > supArity) {
            Iterable<Type> prefixSups = IterUtil.skipLast(supParams);
            Type lastSup = IterUtil.last(supParams);
            int varargArgs = subArity-(supArity-1); // expect this many args of the vararg type
            supParams = IterUtil.compose(prefixSups, IterUtil.copy(((ArrayType) lastSup).ofType(), varargArgs));
          }
          // if they're equal, no adjustments are needed
          // (If lastSup is T..., lastSub might be a subtype of T or of T[].  For the purposes of
          // "more specific", we arbitrarily require it to be a subtype of T[].)
        }
      }
      SignatureMatcher m = makeMatcher(c._f.typeParameters(), EMPTY_TYPE_ITERABLE, supParams, 
                                       IterUtil.mapSnapshot(subParams, EMPTY_EXPRESSION_FOR_TYPE),
                                       BOTTOM, NONE_TYPE_OPTION);
      return m.matches();
    }
    
  }
  
  /**
   * Get a minimal sublist of the invocation candidates that are best matches.  Only candidates that
   * match in the same stage ({@code matches()}, {@code matchesWithBoxing()}, or {@code matchesWithVarargs()})
   * may appear in the result.  Of those that match, a list with most specific signatures is returned. 
   */
  private <F extends Function>
      Iterable<FunctionInvocationCandidate<F>> bestInvocations(Iterable<F> functions,
                                                               final Iterable<? extends Type> targs,
                                                               final Iterable<? extends Expression> args,
                                                               final Option<Type> expected) {
    // This would be a static member of FunctionInvocationCandidate if that were legal;
    // it accesses the _matcher field as if it were.
    Iterable<FunctionInvocationCandidate<F>> candidates = IterUtil.mapSnapshot(functions,
                                                                     new Lambda<F, FunctionInvocationCandidate<F>>() {
      public FunctionInvocationCandidate<F> value(F f) {
        return new FunctionInvocationCandidate<F>(f, targs, args, expected);
      }
    });
    List<FunctionInvocationCandidate<F>> matches = new LinkedList<FunctionInvocationCandidate<F>>();
    for (FunctionInvocationCandidate<F> c : candidates) {
      if (c._matcher.matches()) { matches.add(c); }
    }
    if (matches.isEmpty()) {
      for (FunctionInvocationCandidate<F> c : candidates) {
        if (c._matcher.matchesWithBoxing()) { matches.add(c); }
      }
    }
    if (matches.isEmpty()) {
      for (FunctionInvocationCandidate<F> c : candidates) {
        if (c._matcher.matchesWithVarargs()) { matches.add(c); }
      }
    }
    return CollectUtil.minList(matches, new Order<FunctionInvocationCandidate<F>>() {
      public boolean contains(FunctionInvocationCandidate<F> c1, FunctionInvocationCandidate<F> c2) {
        return c1.moreSpecificThan(c2);
      }
    });
  }
  
  private static final Lambda<Type, Expression> EMPTY_EXPRESSION_FOR_TYPE = new Lambda<Type, Expression>() {
    public Expression value(Type t) {
      Expression result = TypeUtil.makeEmptyExpression();
      NodeProperties.setType(result, t);
      return result;
    }
  };
  
  /**
   * Instantiate a method based on its enclosing type.  Would be static in MethodInvocationCandidate
   * if that were allowed.
   */
  private DJMethod instantiateMethod(final DJMethod declaredMethod, Type declaringType) {
    Type dynamicContext;
    if (declaredMethod.isStatic()) {
      if (declaringType instanceof ClassType) {
        dynamicContext = SymbolUtil.dynamicOuterClassType((ClassType) declaringType);
      }
      else { dynamicContext = null; }
    }
    else { dynamicContext = declaringType; }
    if (dynamicContext == null) { return declaredMethod; }
    else {
      return dynamicContext.apply(new TypeAbstractVisitor<DJMethod>() {
        @Override public DJMethod defaultCase(Type dynamicContext) { return declaredMethod; }
        @Override public DJMethod forRawClassType(RawClassType dynamicContext) {
          // TODO: raw member access warnings
          return new ErasedMethod(declaredMethod);
        }
        @Override public DJMethod forParameterizedClassType(ParameterizedClassType dynamicContext) {
          ParameterizedClassType dynamicContextCap = capture(dynamicContext);
          Iterable<VariableType> tparams = SymbolUtil.allTypeParameters(dynamicContextCap.ofClass());
          return new InstantiatedMethod(declaredMethod, tparams, dynamicContextCap.typeArguments());
        }
      });
    }
  }

  private static abstract class DelegatingFunction<T extends Function> implements Function {
    protected final T _delegate;
    protected DelegatingFunction(T delegate) { _delegate = delegate; }
    
    public String declaredName() { return _delegate.declaredName(); }
    public Iterable<LocalVariable> parameters() {
      return IterUtil.mapSnapshot(IterUtil.zip(_delegate.parameters(), parameterTypes()),
                                               new Lambda<Pair<LocalVariable, Type>, LocalVariable>() {
        public LocalVariable value(Pair<LocalVariable, Type> p) {
          return new LocalVariable(p.first().declaredName(), p.second(), p.first().isFinal());
        }
      });
    }

    public abstract Iterable<VariableType> typeParameters();
    public abstract Type returnType();
    public abstract Iterable<Type> thrownTypes();
    protected abstract Iterable<? extends Type> parameterTypes();
  }
  
  private static abstract class DelegatingMethod extends DelegatingFunction<DJMethod> implements DJMethod {
    protected DelegatingMethod(DJMethod delegate) { super(delegate); }
    public DJClass declaringClass() { return _delegate.declaringClass(); }
    public boolean isStatic() { return _delegate.isStatic(); }
    public boolean isAbstract() { return _delegate.isAbstract(); }
    public boolean isFinal() { return _delegate.isFinal(); }
    public Access accessibility() { return _delegate.accessibility(); }
    public Access.Module accessModule() { return _delegate.accessModule(); }
    public DJMethod declaredSignature() { return _delegate.declaredSignature(); }
    public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, Options options) 
        throws EvaluatorException {
      return _delegate.evaluate(receiver, args, bindings, options); 
    }
  }
  
  private class ErasedMethod extends DelegatingMethod {
    public ErasedMethod(DJMethod m) { super(m); }
    public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
    public Type returnType() { return erase(_delegate.returnType()); }
    public Iterable<Type> thrownTypes() { return IterUtil.mapSnapshot(_delegate.thrownTypes(), ERASE); }
    protected Iterable<Type> parameterTypes() {
      return IterUtil.mapSnapshot(SymbolUtil.parameterTypes(_delegate), ERASE);
    }
  }
  
  private class InstantiatedMethod extends DelegatingMethod {
    private final SubstitutionMap _sigma;
    private final Iterable<VariableType> _tparams;
    public InstantiatedMethod(DJMethod m, Iterable<VariableType> classTParams, Iterable<? extends Type> classTArgs) {
      super(m);
      Pair<Iterable<VariableType>, SubstitutionMap> p = instantiateTypeParameters(m, classTParams, classTArgs);
      _tparams = p.first();
      _sigma = p.second();
    }
    
    public Type returnType() { return substitute(_delegate.returnType(), _sigma); }
    public Iterable<VariableType> typeParameters() { return _tparams; }
    public Iterable<Type> thrownTypes() { return IterUtil.relax(substitute(_delegate.thrownTypes(), _sigma)); }
    public Iterable<? extends Type> parameterTypes() {
      return substitute(SymbolUtil.parameterTypes(_delegate), _sigma);
    }
  }
  
  private static abstract class DelegatingConstructor extends DelegatingFunction<DJConstructor>
                                                      implements DJConstructor {
    protected DelegatingConstructor(DJConstructor delegate) { super(delegate); }
    public DJClass declaringClass() { return _delegate.declaringClass(); }
    public Type returnType() { return _delegate.returnType(); }
    public Access accessibility() { return _delegate.accessibility(); }
    public Access.Module accessModule() { return _delegate.accessModule(); }
    public DJConstructor declaredSignature() { return _delegate.declaredSignature(); }
    public Object evaluate(Object outer, Iterable<Object> args, RuntimeBindings bindings, Options options) 
        throws EvaluatorException {
      return _delegate.evaluate(outer, args, bindings, options); 
    }
  }
  
  private class ErasedConstructor extends DelegatingConstructor {
    public ErasedConstructor(DJConstructor k) { super(k); }
    public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
    public Iterable<Type> thrownTypes() { return IterUtil.mapSnapshot(_delegate.thrownTypes(), ERASE); }
    protected Iterable<Type> parameterTypes() {
      return IterUtil.mapSnapshot(SymbolUtil.parameterTypes(_delegate), ERASE);
    }
  }
  
  private class InstantiatedConstructor extends DelegatingConstructor {
    private final SubstitutionMap _sigma;
    private final Iterable<VariableType> _tparams;
    public InstantiatedConstructor(DJConstructor k, Iterable<VariableType> classTParams,
                                   Iterable<? extends Type> classTArgs) {
      super(k);
      Pair<Iterable<VariableType>, SubstitutionMap> p = instantiateTypeParameters(k, classTParams, classTArgs);
      _tparams = p.first();
      _sigma = p.second();
    }
    
    public Iterable<VariableType> typeParameters() { return _tparams; }
    public Iterable<Type> thrownTypes() { return IterUtil.relax(substitute(_delegate.thrownTypes(), _sigma)); }
    public Iterable<? extends Type> parameterTypes() {
      return substitute(SymbolUtil.parameterTypes(_delegate), _sigma);
    }
  }
  
  /** Create new type parameters for function {@code f} with bounds instantiated by the given substitution. */
  private Pair<Iterable<VariableType>, SubstitutionMap>
      instantiateTypeParameters(Function f,
                                Iterable<? extends VariableType> enclosingTParams,
                                Iterable<? extends Type> enclosingTArgs) {
    Iterable<VariableType> origTParams = f.typeParameters();
    Iterable<VariableType> tparams = IterUtil.mapSnapshot(origTParams, new Lambda<VariableType, VariableType>() {
      public VariableType value(VariableType var) {
        return new VariableType(new BoundedSymbol(new Object(), var.symbol().name()));
      }
    });
    SubstitutionMap sigma = new SubstitutionMap(IterUtil.compose(enclosingTParams, origTParams),
                                                IterUtil.compose(enclosingTArgs, tparams));
    for (Pair<VariableType, VariableType> p : IterUtil.zip(origTParams, tparams)) {
      VariableType origParam = p.first();
      VariableType newParam = p.second();
      newParam.symbol().initializeUpperBound(substitute(origParam.symbol().upperBound(), sigma));
      newParam.symbol().initializeLowerBound(substitute(origParam.symbol().lowerBound(), sigma));
    }
    return Pair.make(tparams, sigma);
  }
  
  /**
   * A wrapper to optimize the three-stage method-signature matching algorithm.  Clients are
   * assumed to invoke the methods {@code matches}, {@code matchesWithBoxing}, and 
   * {@code matchesWithVarargs} in that order, and only if the previous match attempt failed; 
   * other accessor methods provide the results of a match <em>after</em> one of the match methods 
   * returns {@code true}.
   */
  private static abstract class SignatureMatcher {
    /** Must be invoked first */
    public abstract boolean matches();
    
    /** Must only be invoked after {@link #matches()} */
    public abstract boolean matchesWithBoxing();
    
    /** Must only be invoked after {@link #matchesWithBoxing} */
    public abstract boolean matchesWithVarargs();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends Type> typeArguments();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends Expression> arguments();
  }
  
  private static class NullMatcher extends SignatureMatcher {
    public static final NullMatcher INSTANCE = new NullMatcher();
    private NullMatcher() {}
    public boolean matches() { return false; }
    public boolean matchesWithBoxing() { return false; }
    public boolean matchesWithVarargs() { return false; }
    public Iterable<? extends Type> typeArguments() { throw new IllegalStateException(); }
    public Iterable<? extends Expression> arguments() { throw new IllegalStateException(); }
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class SimpleMatcher extends SignatureMatcher {
    protected Iterable<? extends Type> _params;
    protected Iterable<? extends Expression> _args;
    protected Iterable<? extends VariableType> _tparams;
    protected Iterable<? extends Type> _targs;
    protected Type _paramForVarargs; // set at some point before matchesWithVarargs()
    protected Expression _argForVarargs; // set at some point before matchesWithVarargs(); not boxed
    protected boolean _matchesAllButLast; // true if matchesWithBoxing() is true on all but the last arg
    
    /**  Assumes {@code params.size() == args.size()} */
    public SimpleMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                         Iterable<? extends VariableType> tparams, Iterable<? extends Type> targs) {
      _params = params;
      _args = args;
      _tparams = tparams;
      _targs = targs;
    }
    
    public Iterable<? extends Type> typeArguments() { return _targs; }
    public Iterable<? extends Expression> arguments() { return _args; }
    
    public boolean matches() {
      Iterator<? extends Type> pI = _params.iterator();
      Iterator<? extends Expression> aI = _args.iterator();
      while (pI.hasNext()) {
        // TODO: Allow unchecked conversion of raw types
        if (!isSubtype(NodeProperties.getType(aI.next()), pI.next())) { 
          _matchesAllButLast = !pI.hasNext();
          return false;
        }
      }
      return true;
    }
    
    public boolean matchesWithBoxing() { return boxArgs() && matches(); }
    
    public boolean matchesWithVarargs() {
      if (_matchesAllButLast && _paramForVarargs instanceof VarargArrayType) {
        ArrayType arrayT = (ArrayType) substitute(_paramForVarargs, _tparams, _targs);
        Type elementT = arrayT.ofType();
        _argForVarargs = boxingConvert(_argForVarargs, elementT);
        // TODO: Allow unchecked conversion of raw types?
        if (isSubtype(NodeProperties.getType(_argForVarargs), elementT)) {
          Expression newArg = makeArray(arrayT, IterUtil.make(_argForVarargs));
          _args = IterUtil.compose(IterUtil.skipLast(_args), newArg);
          return true;
        }
      }
      return false;
    }
    
    /** 
     * Update {@code _args} with the boxed or unboxed versions of the arguments; set {@link #_paramForVarargs}
     * and {@link #_argForVarargs} before performing the conversion on the last argument
     * 
     * @return  {@code true} iff the result of the conversion is different from the original set of arguments
     */
    protected boolean boxArgs() {
      Iterable<Expression> newArgs = EMPTY_EXPRESSION_ITERABLE;
      boolean result = false;
      Iterator<? extends Type> pI = _params.iterator();
      Iterator<? extends Expression> aI = _args.iterator();
      while (pI.hasNext()) {
        Type pT = pI.next();
        Expression aE = aI.next();
        if (!pI.hasNext()) { _paramForVarargs = pT; _argForVarargs = aE; }
        Expression newArg = boxingConvert(aE, pT);
        if (newArg != aE) { result = true; }
        newArgs = IterUtil.compose(newArgs, newArg);
      }
      if (result) { _args = newArgs; }
      return result;
    }
    
    /** @return  The boxed or unboxed version of {@code e}, as necessary, or {@code e}, untouched */
    protected Expression boxingConvert(Expression exp, Type target) {
      Type t = NodeProperties.getType(exp);
      if (isPrimitive(target) && isPrimitiveConvertible(t)) {
        try { return makePrimitive(exp); }
        catch (UnsupportedConversionException e) { throw new RuntimeException("isPrimitiveConvertible() lied"); }
      }
      else if (isReference(target) && isReferenceConvertible(t)) {
        try { return makeReference(exp); }
        catch (UnsupportedConversionException e) { throw new RuntimeException("isReferenceConvertible() lied"); }
      }
      else { return exp; }
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class InferenceMatcher extends SimpleMatcher {
    
    protected final Type _returned;
    protected final Option<Type> _expected;
    
    /**  Assumes {@code params.size() == args.size()} */
    public InferenceMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                            Iterable<? extends VariableType> tparams,
                            Type returned, Option<Type> expected) {
      super(params, args, tparams, null);
      _returned = returned;
      if (expected.isSome()) {
        Expression exp = TypeUtil.makeEmptyExpression();
        NodeProperties.setType(exp, expected.unwrap());
        _expected = Option.some(NodeProperties.getType(boxingConvert(exp, _returned)));
      }
      else { _expected = expected; }
    }
    
    @Override public boolean matches() {
      Iterable<Type> argTypes = IterUtil.mapSnapshot(_args, NodeProperties.NODE_TYPE);
      _targs = inferTypeArguments(_tparams, _params, _returned, argTypes, _expected);
      return (_targs != null);
    }
    
    @Override public boolean matchesWithBoxing() {
      // We perform boxing based on the uninstantiated parameter types, but that's okay --
      // free variables with reference bounds will be treated as references
      return boxArgs() && matches(); // matches() infers new type arguments
    }
    
    @Override public boolean matchesWithVarargs() {
      // We ignore _matchesAllButLast -- it's possible that the new targs will allow a match that
      // didn't work before
      if (_paramForVarargs instanceof VarargArrayType) {
        ArrayType arrayT = (ArrayType) _paramForVarargs;
        Type elementT = arrayT.ofType();
        _argForVarargs = boxingConvert(_argForVarargs, elementT);
        Iterable<Expression> inferenceArgs = IterUtil.compose(IterUtil.skipLast(_args), _argForVarargs);
        Iterable<Type> argTypes = IterUtil.mapSnapshot(inferenceArgs, NodeProperties.NODE_TYPE);
        Iterable<Type> paramTypes = IterUtil.compose(IterUtil.skipLast(_params), elementT);
        _targs = inferTypeArguments(_tparams, paramTypes, _returned, argTypes, _expected);
        if (_targs != null) {
          Expression newArg = makeArray((ArrayType) substitute(arrayT, _tparams, _targs), 
                                        IterUtil.make(_argForVarargs));
          _args = IterUtil.compose(IterUtil.skipLast(_args), newArg);
          return true;
        }
      }
      return false;
    }
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class EmptyVarargMatcher extends SimpleMatcher {
    
    // Can't use _paramForVarargs, because SimpleMatcher doesn't know about the final parameter
    private Type _varargParam;
    
    /**  Assumes {@code params.size() - 1 == args.size()} */
    public EmptyVarargMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                              Iterable<? extends VariableType> tparams, Iterable<? extends Type> targs) {
      super(params, args, tparams, targs);
      _varargParam = IterUtil.last(_params);
      _params = IterUtil.skipLast(_params);
    }
    
    @Override public boolean matches() { return false; }
    @Override public boolean matchesWithBoxing() { return false; }
    
    @Override public boolean matchesWithVarargs() {
      if (_varargParam instanceof VarargArrayType) {
        boxArgs();
        if (super.matches()) {
          _params = IterUtil.compose(_params, _varargParam);
          ArrayType arrayT = (ArrayType) substitute(_varargParam, _tparams, _targs);
          _args = IterUtil.compose(_args, makeArray(arrayT, EMPTY_EXPRESSION_ITERABLE));
          return true;
        }
      }
      return false;
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class EmptyVarargInferenceMatcher extends InferenceMatcher {
    
    // Can't use _paramForVarargs, because SimpleMatcher doesn't know about the final parameter
    private Type _varargParam;
    
    /**  Assumes {@code params.size() - 1 == args.size()} */
    public EmptyVarargInferenceMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                                       Iterable<? extends VariableType> tparams,
                                       Type returned, Option<Type> expected) {
      super(params, args, tparams, returned, expected);
      _varargParam = IterUtil.last(_params);
      _params = IterUtil.skipLast(_params);
    }
    
    @Override public boolean matches() { return false; }
    @Override public boolean matchesWithBoxing() { return false; }
    
    @Override public boolean matchesWithVarargs() {
      if (_varargParam instanceof VarargArrayType) {
        boxArgs();
        if (super.matches()) {
          _params = IterUtil.compose(_params, _varargParam);
          ArrayType arrayT = (ArrayType) substitute(_varargParam, _tparams, _targs);
          _args = IterUtil.compose(_args, makeArray(arrayT, EMPTY_EXPRESSION_ITERABLE));
          return true;
        }
      }
      return false;
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class MultiVarargMatcher extends SimpleMatcher {
    
    private Type _varargParam;
    private Iterable<Expression> _varargArgs;
    
    /**  Assumes {@code 1 <= params.size() < args.size()} */
    public MultiVarargMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                              Iterable<? extends VariableType> tparams, Iterable<? extends Type> targs) {
      super(params, args, tparams, targs);
      _varargParam = IterUtil.last(_params);
      _params = IterUtil.skipLast(_params);
      Pair<? extends Iterable<Expression>, ? extends Iterable<Expression>> splitArgs = 
        IterUtil.split(_args, IterUtil.sizeOf(_params));
      _args = splitArgs.first();
      _varargArgs = splitArgs.second();
    }
    
    @Override public boolean matches() { return false; }
    @Override public boolean matchesWithBoxing() { return false; }
    
    @Override public boolean matchesWithVarargs() {
      if (_varargParam instanceof VarargArrayType) {
        boxArgs();
        if (super.matches()) {
          ArrayType arrayT = (ArrayType) substitute(_varargParam, _tparams, _targs);
          Type elementT = arrayT.ofType();
          Iterable<Expression> boxedVarargArgs = EMPTY_EXPRESSION_ITERABLE;
          for (Expression arg : _varargArgs) {
            Expression boxed = boxingConvert(arg, elementT);
            // TODO: Allow unchecked conversion of raw types?
            if (!isSubtype(NodeProperties.getType(boxed), elementT)) { return false; }
            boxedVarargArgs = IterUtil.compose(boxedVarargArgs, boxed);
          }
          _params = IterUtil.compose(_params, _varargParam);
          _args = IterUtil.compose(_args, makeArray(arrayT, boxedVarargArgs));
          return true;
        }
      }
      return false;
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class MultiVarargInferenceMatcher extends InferenceMatcher {
    
    private Type _varargParam;
    private Iterable<Expression> _varargArgs;
    
    /**  Assumes {@code 1 <= params.size() < args.size()} */
    public MultiVarargInferenceMatcher(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                                       Iterable<? extends VariableType> tparams,
                                       Type returned, Option<Type> expected) {
      super(params, args, tparams, returned, expected);
      _varargParam = IterUtil.last(_params);
      _params = IterUtil.skipLast(_params);
      Pair<? extends Iterable<Expression>, ? extends Iterable<Expression>> splitArgs = 
        IterUtil.split(_args, IterUtil.sizeOf(_params));
      _args = splitArgs.first();
      _varargArgs = splitArgs.second();
    }
    
    @Override public boolean matches() { return false; }
    @Override public boolean matchesWithBoxing() { return false; }
    
    @Override public boolean matchesWithVarargs() {
      if (_varargParam instanceof VarargArrayType) {
        boxArgs();
        ArrayType arrayT = (ArrayType) _varargParam;
        final Type elementT = arrayT.ofType();
        Lambda<Expression, Expression> makeBoxed = new Lambda<Expression, Expression>() {
          public Expression value(Expression e) { return boxingConvert(e, elementT); }
        };
        Iterable<Expression> boxedVarargArgs = IterUtil.map(_varargArgs, makeBoxed);
        Iterable<Expression> inferenceArgs = IterUtil.compose(_args, boxedVarargArgs);
        Iterable<Type> argTypes = IterUtil.mapSnapshot(inferenceArgs, NodeProperties.NODE_TYPE);
        Iterable<Type> varargParams = IterUtil.copy(elementT, IterUtil.sizeOf(_varargArgs));
        Iterable<Type> paramTypes = IterUtil.compose(_params, varargParams);
        _targs = inferTypeArguments(_tparams, paramTypes, _returned, argTypes, _expected);
        if (_targs != null) {
          _params = IterUtil.compose(_params, _varargParam);
          Expression newArg = makeArray((ArrayType) substitute(arrayT, _tparams, _targs), boxedVarargArgs);
          _args = IterUtil.compose(_args, newArg);
          return true;
        }
      }
      return false;
    }
    
  }
  
}
