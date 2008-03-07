package edu.rice.cs.dynamicjava.symbol;

import java.util.*;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Triple;
import edu.rice.cs.plt.recur.*;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.PermutationIterable;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.reflect.JavaVersion;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.TypeUtil;
import koala.dynamicjava.interpreter.NodeProperties;
import edu.rice.cs.dynamicjava.symbol.type.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ExtendedTypeSystem extends TypeSystem {
  
  public static final ExtendedTypeSystem INSTANCE = new ExtendedTypeSystem();
  
  private static final Type CLONEABLE_AND_SERIALIZABLE = 
    new IntersectionType(IterUtil.make(CLONEABLE, SERIALIZABLE));
  
  // nonstatic because it depends on makeClassType
  private final Type ITERABLE;
  {
    Class<?> c;
    try { c = Class.forName("java.lang.Iterable"); }
    catch (ClassNotFoundException e) { c = null; }
    ITERABLE = c == null ? null : makeClassType(SymbolUtil.wrapClass(c));
  }
  
  // nonstatic because it depends on makeClassType
  private final Type COLLECTION = makeClassType(SymbolUtil.wrapClass(Collection.class));
  
  // nonstatic because it depends on makeClassType
  private final Type ENUM;
  {
    Class<?> c;
    try { c = Class.forName("java.lang.Enum"); }
    catch (ClassNotFoundException e) { c = null; }
    ENUM = (c == null) ? null : makeClassType(SymbolUtil.wrapClass(c));
  }
  
  private final DJClass CLASS = SymbolUtil.wrapClass(Class.class);
  
  /** Determine if {@code t} is a primitive. */
  public boolean isPrimitive(Type t) { return t.apply(IS_PRIMITIVE); }
  
  private static final TypeVisitor<Boolean> IS_PRIMITIVE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forPrimitiveType(PrimitiveType t) { return true; }
  };
  
  /** Determine if {@code t} is a reference. */
  public boolean isReference(Type t) { return t.apply(IS_REFERENCE); }
  
  private static final TypeVisitor<Boolean> IS_REFERENCE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forReferenceType(ReferenceType t) { return true; }
    @Override public Boolean forVariableType(VariableType t) { return true; }
    @Override public Boolean forIntersectionType(IntersectionType t) { return true; }
    @Override public Boolean forUnionType(UnionType t) { return true; }
  };
  
  /** Determine if {@code t} is an array. */
  public boolean isArray(Type t) { return t.apply(IS_ARRAY); }
  
  private static final TypeVisitor<Boolean> IS_ARRAY = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forArrayType(ArrayType t) { return true; }
    @Override public Boolean forVariableType(VariableType t) { return t.symbol().upperBound().apply(this); }
    @Override public Boolean forIntersectionType(IntersectionType t) { return IterUtil.or(t.ofTypes(), IS_ARRAY_PRED); }
    @Override public Boolean forUnionType(UnionType t) { return IterUtil.and(t.ofTypes(), IS_ARRAY_PRED); }
  };

  private static final Predicate<Type> IS_ARRAY_PRED = new Predicate<Type>() {
    public Boolean value(Type t) { return t.apply(IS_ARRAY); }
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
  private final TypeVisitor<Boolean> IS_REIFIABLE = new TypeAbstractVisitor<Boolean>() {
    
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
  private final TypeVisitor<Boolean> IS_UNBOUNDED_WILDCARD = new TypeAbstractVisitor<Boolean>() {
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
  
  private static final TypeVisitor<Boolean> IS_CONCRETE = new TypeAbstractVisitor<Boolean>() {
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
  
  /**
   * Determine if values of type {@code t} are not dependent on an outer object (for example, a non-static 
   * inner class has such a dependency)
   */
  public boolean isStatic(Type t) { return t.apply(IS_STATIC); }
  
  private static final TypeVisitor<Boolean> IS_STATIC = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return true; }
    
    @Override public Boolean forClassType(ClassType t) {
      DJClass outer = null;
      for (DJClass inner : SymbolUtil.outerClassChain(t.ofClass())) {
        if (outer != null && !inner.isStatic()) { return false; }
        outer = inner;
      }
      return true;
    }
  };
  
  /** Determine {@code t} is valid in the {@code extends} clause of a class definition */
  public boolean isExtendable(Type t) { return t.apply(IS_EXTENDABLE); }
  
  private static final TypeVisitor<Boolean> IS_EXTENDABLE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    
    @Override public Boolean forClassType(ClassType t) {
      return !t.ofClass().isInterface() && !t.ofClass().isFinal();
    }
  };
  
  
  /** Determine if {@code t} is valid in the {@code implements} clause of a class definition */
  public boolean isImplementable(Type t) { return t.apply(IS_IMPLEMENTABLE); }
  
  private static final TypeVisitor<Boolean> IS_IMPLEMENTABLE = new TypeAbstractVisitor<Boolean>() {
    public Boolean defaultCase(Type t) { return false; }
    @Override public Boolean forClassType(ClassType t) { return t.ofClass().isInterface(); }
  };
  
  
  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public boolean isEqual(Type t1, Type t2) { return t1.equals(t2) || (isSubtype(t1, t2) && isSubtype(t2, t1)); }
  
  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link isEqual}), transitive relation.
   */
  public boolean isSubtype(Type subT, Type superT) {
    return isSubtype(subT, superT, new RecursionStack2<Type, Type>());
  }
  
  private boolean isSubtype(final Type subT, final Type superT, final RecursionStack2<Type, Type> stack) {
    //debug.logStart();
    //debug.logValues(new String[]{"subT", "superT"}, subT, superT); try {
            
    if (subT.equals(superT)) { return true; } // what follows assumes the types are not syntactically equal
    
    // Handle easy superT cases:
    Boolean result = superT.apply(new TypeAbstractVisitor<Boolean>() {
      public Boolean defaultCase(Type superT) { return null; }
      
      @Override public Boolean forVariableType(final VariableType superT) {
        return subT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(final Type subT) {
            Thunk<Boolean> checkLowerBound = new Thunk<Boolean>() {
              public Boolean value() { return isSubtype(subT, superT.symbol().lowerBound(), stack); }
            };
            return stack.apply(checkLowerBound, false, subT, superT);
          }
          @Override public Boolean forVariableType(VariableType subT) {
            return defaultCase(subT) ? true : null;
          }
          @Override public Boolean forIntersectionType(IntersectionType subT) {
            return defaultCase(subT) ? true : null;
          }
          @Override public Boolean forBottomType(BottomType subT) { return true; }
        });
      }
      
      @Override public Boolean forIntersectionType(IntersectionType superT) {
        if (subT instanceof BottomType) { return true; }
        else {
          return IterUtil.and(superT.ofTypes(), new Predicate<Type>() {
            public Boolean value(Type t) { return isSubtype(subT, t, stack); }
          });
        }
      }
      
      @Override public Boolean forUnionType(final UnionType superT) {
        return subT.apply(new TypeAbstractVisitor<Boolean>() {
          @Override public Boolean defaultCase(Type t) {
            return IterUtil.or(superT.ofTypes(), new Predicate<Type>() {
              public Boolean value(Type t) { return isSubtype(subT, t, stack); }
            });
          }
          public Boolean forVariableType(VariableType t) { return defaultCase(subT) ? true : null; }
          public Boolean forUnionType(UnionType t) { return null; }
          public Boolean forBottomType(BottomType t) { return true; }
        });
      }
      
      @Override public Boolean forTopType(TopType superT) { return true; }
    });
    
    if (result != null) { return result; }
    
    // Handle subT-based cases:
    return subT.apply(new TypeAbstractVisitor<Boolean>() {
      
      public Boolean defaultCase(Type t) { return false; }
      
      public Boolean forCharType(CharType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forCharType(CharType superT) { return true; }
          @Override public Boolean forIntType(IntType superT) { return true; }
          @Override public Boolean forLongType(LongType superT) { return true; }
          @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
        });
      }
      
      public Boolean forByteType(ByteType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forIntegerType(IntegerType superT) { return true; }
          @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
        });
      }
      
      public Boolean forShortType(ShortType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forShortType(ShortType superT) { return true; }
          @Override public Boolean forIntType(IntType superT) { return true; }
          @Override public Boolean forLongType(LongType superT) { return true; }
          @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
        });
      }
      
      public Boolean forIntType(IntType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forIntType(IntType superT) { return true; }
          @Override public Boolean forLongType(LongType superT) { return true; }
          @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
        });
      }
      
      public Boolean forLongType(LongType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forLongType(LongType superT) { return true; }
          @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
        });
      }
      
      public Boolean forFloatType(FloatType subT) { return superT instanceof FloatingPointType; }
      
      public Boolean forNullType(NullType subT) { return isReference(superT); }
      
      public Boolean forSimpleArrayType(SimpleArrayType subT) { return handleArrayType(subT); }
      
      public Boolean forVarargArrayType(VarargArrayType subT) { return handleArrayType(subT); }
      
      private Boolean handleArrayType(final ArrayType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          
          @Override public Boolean forArrayType(ArrayType superT) {
            if (isPrimitive(subT.ofType())) { return false; }
            else { return isSubtype(subT.ofType(), superT.ofType(), stack); }
          }
          
          @Override public Boolean forClassType(ClassType superT) { 
            return isSubtype(CLONEABLE_AND_SERIALIZABLE, superT, stack);
          }
          
        });
      }
      
      public Boolean forClassType(final ClassType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          @Override public Boolean forClassType(ClassType superT) {
            Type newSub = immediateSupertype(subT);
            if (newSub == null) { return false; }
            else { return isSubtype(newSub, superT, stack); }
          }
        });
      }
      
      public Boolean forParameterizedClassType(final ParameterizedClassType subT) {
        return superT.apply(new TypeAbstractVisitor<Boolean>() {
          public Boolean defaultCase(Type superT) { return false; }
          
          @Override public Boolean forParameterizedClassType(final ParameterizedClassType superT) {
            if (subT.ofClass().equals(superT.ofClass())) {
              
              Thunk<Boolean> containedArgs = new Thunk<Boolean>() {
                public Boolean value() {
                  boolean result = true;
                  ParameterizedClassType subCapT = capture(subT);
                  for (final Pair<Type, Type> args : IterUtil.zip(subCapT.typeArguments(), 
                                                                  superT.typeArguments())) {
                    result &= args.second().apply(new TypeAbstractVisitor<Boolean>() {
                      public Boolean defaultCase(Type superArg) { return isEqual(args.first(), superArg); }
                      @Override public Boolean forWildcard(Wildcard superArg) {
                        return isSubtype(superArg.symbol().lowerBound(), args.first(), stack) &&
                          isSubtype(args.first(), superArg.symbol().upperBound(), stack);
                      }
                    });
                    if (!result) { break; }
                  }
                  return result;
                }
              };
              
              return stack.apply(containedArgs, true, subT, superT) || forClassType(superT);
            }
            else { return forClassType(superT); }
          }
          
          @Override public Boolean forClassType(ClassType superT) {
            Type newSub = immediateSupertype(subT);
            if (newSub == null) { return false; }
            else { return isSubtype(meet(newSub, erase(subT)), superT, stack); }
          }
          
        });
      }
      
      public Boolean forVariableType(final VariableType subT) {
        // If variables are always Objects, we should test that superT is Object in the infinite case
        Thunk<Boolean> checkUpperBound = new Thunk<Boolean>() {
          public Boolean value() { return isSubtype(subT.symbol().upperBound(), superT, stack); }
        };
        return stack.apply(checkUpperBound, false, subT, superT);
      }
      
      public Boolean forIntersectionType(IntersectionType subT) {
        return IterUtil.or(subT.ofTypes(), new Predicate<Type>() {
          public Boolean value(Type t) { return isSubtype(t, superT, stack); }
        });
      }
      
      public Boolean forUnionType(UnionType subT) {
        return IterUtil.and(subT.ofTypes(), new Predicate<Type>() {
          public Boolean value(Type t) { return isSubtype(t, superT, stack); }
        });
      }
      
      public Boolean forBottomType(BottomType subT) { return true; }
    });
    //} finally { debug.logEnd(); }
  }
  
  /** Determine if {@link cast()} would succeed given an expression of the given type */
  public boolean isCastable(Type target, Type expT) {
    // TODO: Handle unchecked warnings -- perhaps at the call site
    try {
      Expression e = TypeUtil.makeEmptyExpression();
      NodeProperties.setType(e, expT);
      cast(target, e);
      return true;
    }
    catch (UnsupportedConversionException e) { return false; }
  }
  
  /** Determine if {@link assign()} would succeed given a non-constant expression of the given type */
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
  
  /** Determine if {@link assign()} would succeed given a constant expression of the given type and value */
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
      isSubtype(t, BOOLEAN_CLASS) || 
      isSubtype(t, CHARACTER_CLASS) ||
      isSubtype(t, BYTE_CLASS) ||
      isSubtype(t, SHORT_CLASS) ||
      isSubtype(t, INTEGER_CLASS) ||
      isSubtype(t, LONG_CLASS) ||
      isSubtype(t, FLOAT_CLASS) ||
      isSubtype(t, DOUBLE_CLASS);
  }
  
  
  public boolean isReferenceConvertible(Type t) { return isReference(t) || t instanceof PrimitiveType; }
  
  
  /** Compute a common supertype of {@code t1} and {@code t2}. */
  public Type join(Type t1, Type t2) {
    //debug.logValues(new String[]{ "t1", "t2" }, wrap(t1), wrap(t2));
    if (isSubtype(t1, t2)) { return t2; }
    else if (isSubtype(t2, t1)) { return t1; }
    else { return new UnionType(IterUtil.make(t1, t2)); }
    // TODO: This solution ignores the possibility that neither is a subtype of the other,
    // but that some of the types of two unions are identical or subtypes
  }
  
  /** Compute a common subtype of {@code t1} and {@code t2}. */
  public Type meet(Type t1, Type t2) {
    if (isSubtype(t1, t2)) { return t1; }
    else if (isSubtype(t2, t1)) { return t2; }
    else { return new IntersectionType(IterUtil.make(t1, t2)); }
  }
  
  /**
   * Determine the type bound to {@code super} in the body of the given type's declaration, or
   * {@code null} if no such type exists.
   */
  public Type immediateSuperclass(Type t) {
    if (t instanceof ClassType) { return ((ClassType) t).ofClass().immediateSuperclass(); }
    else { return null; }
  }
  
  /**
   * @return  The capture {@code t}.  Capture eliminates wildcards in a 
   *          {@link ParameterizedClassType}.
   */
  public Type capture(Type t) { return t.apply(CAPTURE); }
  
  // cannot be defined statically, because it relies on the definition of non-static "capture"
  private final TypeVisitor<Type> CAPTURE = new TypeAbstractVisitor<Type>() {
    public Type defaultCase(Type t) { return t; }
    @Override public Type forParameterizedClassType(ParameterizedClassType t) { return capture(t); }
  };
  
  private ParameterizedClassType capture(ParameterizedClassType t) {
    List<BoundedSymbol> captureVars = new LinkedList<BoundedSymbol>();
    List<Type> newArgs = new LinkedList<Type>();
    boolean ground = true;
    for (Type arg : t.typeArguments()) {
      if (arg instanceof Wildcard) {
        ground = false;
        BoundedSymbol s = new BoundedSymbol(new Object());
        captureVars.add(s);
        newArgs.add(new VariableType(s));
      }
      else { captureVars.add(null); newArgs.add(arg); }
    }
    
    if (ground) { return t; }
    else {
      Iterable<VariableType> params = SymbolUtil.allTypeParameters(t.ofClass());
      final SubstitutionMap sigma = new SubstitutionMap(params, newArgs);
      for (Triple<BoundedSymbol, Type, VariableType> triple : IterUtil.zip(captureVars, t.typeArguments(), params)) {
        Type arg = triple.second();
        if (arg instanceof Wildcard) {
          Wildcard argW = (Wildcard) arg;
          VariableType param = triple.third();
          Type paramU = substitute(param.symbol().upperBound(), sigma);
          Type paramL = substitute(param.symbol().lowerBound(), sigma);
          triple.first().initializeUpperBound(new IntersectionType(IterUtil.make(argW.symbol().upperBound(),
                                                                                 paramU)));
          triple.first().initializeLowerBound(new UnionType(IterUtil.make(argW.symbol().lowerBound(), paramL)));
        }
      }
      return new ParameterizedClassType(t.ofClass(), newArgs);
    }
  }
  
  /**
   * Compute the erased type of {@code t}.  The result is guaranteed to be reifiable (according
   * to {@link isReifiable}) and a supertype of {@code t}.
   */
  public Type erase(Type t) { return t.apply(ERASE); }
  
  private static final TypeVisitor<Type> ERASE = new TypeAbstractVisitor<Type>() {
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
    
    @Override public Type forWildcard(Wildcard t) { return null; }
    
  };
  
  // Not defined statically because it depends on non-static "erase" (allowing subclasses to override erase)
  private final Lambda<Type, Type> ERASE_LAMBDA = new Lambda<Type, Type>() {
    public Type value(Type t) { return erase(t); }
  };
  
  /**
   * Determine the class corresponding to the erasure of {@code t}, or {@code null} if no such class object 
   * exists.  To prevent over-eager loading of user-defined classes, computation of the result
   * is delayed by wrapping it in a thunk.  (A DJClass return type would be incorrect, as there's no such
   * thing (for example) as an array DJClass.)
   */
  public Thunk<Class<?>> erasedClass(Type t) { return t.apply(ERASED_CLASS); }
  
  private static final TypeVisitor<Thunk<Class<?>>> ERASED_CLASS = new TypeVisitor<Thunk<Class<?>>>() {
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
    
    public Thunk<Class<?>> forWildcard(Wildcard t) { return null; }
    public Thunk<Class<?>> forVoidType(VoidType t) { return LambdaUtil.<Class<?>>valueLambda(void.class); }
    public Thunk<Class<?>> forTopType(TopType t) { return null; }
    public Thunk<Class<?>> forBottomType(BottomType t) { return null; }
    
    private Thunk<Class<?>> wrapDJClass(final DJClass c) {
      return new Thunk<Class<?>>() {
        public Class<?> value() { return c.load(); }
      };
    }
    
  };
  
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
   * {@link isArray}).
   */
  public Type arrayElementType(Type t) {
    return t.apply(ARRAY_ELEMENT_TYPE);
  }
  
  // not defined statically because it relies on non-static meet() and join()
  private final TypeVisitor<Type> ARRAY_ELEMENT_TYPE = new TypeAbstractVisitor<Type>() {
    public Type defaultCase(Type t) { throw new IllegalArgumentException(); }
    @Override public Type forArrayType(ArrayType t) { return t.ofType(); }
    @Override public Type forVariableType(VariableType t) { return t.symbol().upperBound().apply(this); }
    @Override public Type forIntersectionType(IntersectionType t) {
      Type result = OBJECT;
      for (Type componentT : t.ofTypes()) {
        if (componentT.apply(IS_ARRAY)) { result = meet(result, componentT.apply(this)); }
      }
      return result;
    }
    @Override public Type forUnionType(UnionType t) {
      Type result = NULL;
      for (Type componentT : t.ofTypes()) { // each component is guaranteed to be an array
        result = join(result, componentT.apply(this));
      }
      return result;
    }
  };
  
  
  private static class SubstitutionMap {
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
    
    public boolean isEmpty() {
      if (_sigma == null) { return IterUtil.isEmpty(_vars); }
      else { return _sigma.isEmpty(); }
    }
    
    public Type get(VariableType v) {
      if (_sigma == null) { initSigma(); }
      return _sigma.get(v);
    }
    
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
  private Type substitute(Type t, Iterable<? extends VariableType> params, Iterable<? extends Type> args) {
    return substitute(t, new SubstitutionMap(params, args));
  }
  
  private Type substitute(Type t, final SubstitutionMap sigma) {
    if (sigma.isEmpty()) { return t; }
    else {
      final PrecomputedRecursionStack<Type, Type> stack = PrecomputedRecursionStack.make();
      
      // TODO: Make sure CopyDepthFirstVisitor isn't making unnecessary copies
      return t.apply(new TypeCopyDepthFirstVisitor() {
        
        // TODO: This should be automatically defined in TypeCopyDepthFirstVisitor
        public Type defaultCase(Type t) { 
          throw new IllegalArgumentException("Visitor unexpectedly reached default case");
        }
        
        @Override public Type forParameterizedClassType(ParameterizedClassType t) {
          Iterable<Type> newArgs = applyToList(t.typeArguments());
          return (newArgs == null) ? t : new ParameterizedClassType(t.ofClass(), newArgs);
        }
        
        @Override public Type forVariableType(VariableType t) {
          Type result = sigma.get(t);
          return (result == null) ? t : result;
        }
        
        @Override public Type forIntersectionType(IntersectionType t) {
          Iterable<Type> newTypes = applyToList(t.ofTypes());
          return (newTypes == null) ? t : new IntersectionType(newTypes);
        }
        
        @Override public Type forWildcard(final Wildcard t) {
          final Wildcard newWildcard = new Wildcard(new BoundedSymbol(new Object()));
          final TypeVisitor<Type> visitor = this;
          Thunk<Type> substituteBounds = new Thunk<Type>() {
            public Type value() {
              BoundedSymbol bounds = t.symbol();
              Type newUpper = bounds.upperBound().apply(visitor);
              Type newLower = bounds.lowerBound().apply(visitor);
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
        
        // returns null if no change is necessary
        private Iterable<Type> applyToList(Iterable<? extends Type> ts) {
          Iterable<Type> newTs = EMPTY_TYPE_ITERABLE;
          boolean changed = false;
          for (Type t : ts) {
            Type newT = t.apply(this);
            newTs = IterUtil.compose(newTs, newT);
            changed = changed || (t != newT);
          }
          return changed ? newTs : null;
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
   * Reduces all redundant intersections in the given type to their simplest form.  The
   * invariant isEqual(t, simplify(t)) must hold.
   */
  public Type simplify(Type t) {
    final PrecomputedRecursionStack<Type, Type> stack = PrecomputedRecursionStack.make();
      // TODO: Make sure CopyDepthFirstVisitor isn't making unnecessary copies
      return t.apply(new TypeCopyDepthFirstVisitor() {
        
        // TODO: This should be automatically defined in TypeCopyDepthFirstVisitor
        public Type defaultCase(Type t) { 
          throw new IllegalArgumentException("Visitor unexpectedly reached default case");
        }
        
        @Override public Type forParameterizedClassType(final ParameterizedClassType t) {
          Iterable<Type> newArgs = applyToList(t.typeArguments());
          return (newArgs == null) ? t : new ParameterizedClassType(t.ofClass(), newArgs);
        }
        
        @Override public Type forVariableType(VariableType t) {
          return t;
        }
        
        @Override public Type forIntersectionType(IntersectionType t) {
          Iterable<? extends Type> newTypes = applyToList(t.ofTypes());
          if (newTypes == null) newTypes = t.ofTypes();
          Type result = OBJECT;
          for (Type componentT : newTypes) { result = meet(result, componentT); }
          return result;
        }
        
        @Override public Type forUnionType(UnionType t) {
          Iterable<? extends Type> newTypes = applyToList(t.ofTypes());
          if (newTypes == null) newTypes = t.ofTypes();
          Type result = NULL;
          for (Type componentT : newTypes) { result = join(result, componentT); }
          return result;
        }
        
        @Override public Type forWildcard(final Wildcard t) {
          final Wildcard newWildcard = new Wildcard(new BoundedSymbol(new Object()));
          final TypeVisitor<Type> visitor = this;
          Thunk<Type> simplifyBounds = new Thunk<Type>() {
            public Type value() {
              BoundedSymbol bounds = t.symbol();
              Type newUpper = bounds.upperBound().apply(visitor);
              Type newLower = bounds.lowerBound().apply(visitor);
              if (newUpper == bounds.upperBound() && newLower == bounds.lowerBound()) { return t; }
              else {
                newWildcard.symbol().initializeUpperBound(newUpper);
                newWildcard.symbol().initializeLowerBound(newLower);
                return newWildcard;
              }
            }
          };
          return stack.apply(simplifyBounds, newWildcard, t);
        }
        
        // returns null if no change is necessary
        private Iterable<Type> applyToList(Iterable<? extends Type> ts) {
          Iterable<Type> newTs = EMPTY_TYPE_ITERABLE;
          boolean changed = false;
          for (Type t : ts) {
            Type newT = t.apply(this);
            newTs = IterUtil.compose(newTs, newT);
            changed = changed || (t != newT);
          }
          return changed ? newTs : null;
        }
    });
  }
  
  /**
   * @return  A string representing the type
   */
  public String userRepresentation(Type t) {
    final StringBuilder result = new StringBuilder();
    
    class VariableHandler {
      // could use a LinkedHashMap, but that doesn't allow indexed access (iterators
      // lead to ConcurrentModificationExceptions)
      private final List<VariableType> _vars = new ArrayList<VariableType>();
      private final Map<VariableType, String> _names = new HashMap<VariableType, String>();
      int _captureVars = 0;
      
      public boolean isEmpty() {
        for (VariableType v : _vars) {
          Type upper = v.symbol().upperBound();
          Type lower = v.symbol().lowerBound();
          boolean printUpper = !isEqual(upper, OBJECT) /*&& !isEqual(upper, TOP)*/;
          boolean printLower = !isEqual(lower, NULL) /*&& !isEqual(lower, BOTTOM)*/;
          if (printUpper || printLower) { return false; }
        }
        return true;
      }
      
      public String registerVariable(VariableType v) {
        String result = _names.get(v);
        if (result == null) {
          if (v.symbol().generated()) { _captureVars++; result = "?" + _captureVars; }
          else { result = v.symbol().name(); }
          _vars.add(v);
          _names.put(v, result);
        }
        return result;
      }
      
      public void dumpBounds(Runnable1<Type> dumpType) {
        boolean printedFirst = false;
        for (int i = 0; i < _vars.size(); i++) {
          VariableType v = _vars.get(i);
          Type upper = v.symbol().upperBound();
          Type lower = v.symbol().lowerBound();
          boolean printUpper = !isEqual(upper, OBJECT) /*&& !isEqual(upper, TOP)*/;
          boolean printLower = !isEqual(lower, NULL) /*&& !isEqual(lower, BOTTOM)*/;
          if (printUpper || printLower) {
            if (printedFirst) { result.append("; "); }
            else { printedFirst = true; }
          }
          if (printUpper) { 
            result.append(_names.get(v));
            result.append(" <: ");
            dumpType.run(upper); // may increase the size of _vars
          }
          if (printLower) {
            if (printUpper) { result.append(", "); }
            result.append(_names.get(v));
            result.append(" :> ");
            dumpType.run(lower); // may increase the size of _vars
          }
        }
      }
      
    }
    
    final VariableHandler variableHandler = new VariableHandler();
    
    class DumpType implements TypeVisitor_void, Runnable1<Type> {
      
      final RecursionStack<Type> _stack = new RecursionStack<Type>();
      
      /**
       * Running is preferred over applying the visitor, as invoking this will put the
       * value being processed on the stack, and avoid unnecessary repetition
       */
      public void run(final Type t) {
//        String prefix = ""; for (int i = 0; i < _stack.size(); i++) { prefix += "  "; }
//        System.out.println(prefix + "Running on id " + System.identityHashCode(t) + ": " + t);
        Runnable recur = new Runnable() { public void run() { t.apply(DumpType.this); } };
        Runnable dontRecur = new Runnable() { public void run() { result.append("..."); } };
        //Threshold of 2 causes the loop to be printed twice
        _stack.run(recur, dontRecur, t/*, 2*/); 
      }
      
      public void forBooleanType(BooleanType t) { result.append("boolean"); }
      public void forCharType(CharType t) { result.append("char"); }
      public void forByteType(ByteType t) { result.append("byte"); }
      public void forShortType(ShortType t) { result.append("short"); }
      public void forIntType(IntType t) { result.append("int"); }
      public void forLongType(LongType t) { result.append("long"); }
      public void forFloatType(FloatType t) { result.append("float"); }
      public void forDoubleType(DoubleType t) { result.append("double"); }
      public void forNullType(NullType t) { result.append("(null)"); }
      public void forVoidType(VoidType t) { result.append("void"); }
      public void forTopType(TopType t) { result.append("(top)"); }
      public void forBottomType(BottomType t) { result.append("(bottom)"); }
      
      public void forSimpleArrayType(SimpleArrayType t) {
        run(t.ofType());
        result.append("[]");
      }
      
      public void forVarargArrayType(VarargArrayType t) {
        run(t.ofType());
        result.append("[]");
      }
      
      public void forSimpleClassType(SimpleClassType t) {
        result.append(SymbolUtil.shortName(t.ofClass()));
      }
      
      public void forRawClassType(RawClassType t) {
        result.append("raw ");
        result.append(SymbolUtil.shortName(t.ofClass()));
      }
      
      public void forParameterizedClassType(ParameterizedClassType t) {
        Iterator<? extends Type> targs = t.typeArguments().iterator();
        boolean first = true;
        for (DJClass c : SymbolUtil.outerClassChain(t.ofClass())) {
          if (first) {
            result.append(SymbolUtil.shortName(c));
            first = false;
          }
          else { result.append("."); result.append(c.declaredName()); }
          Iterable<VariableType> params = c.declaredTypeParameters();
          if (!IterUtil.isEmpty(params)) {
            result.append("<");
            boolean firstParam = true;
            for (VariableType param : params) { // param is ignored -- it's just a counter
              if (!firstParam) { result.append(", "); }
              firstParam = false;
              run(targs.next());
            }
            result.append(">");
          }
        }
      }
      
      public void forVariableType(VariableType t) {
        String name = variableHandler.registerVariable(t);
        result.append(name);
      }
      
      public void forIntersectionType(IntersectionType t) {
        int size = IterUtil.sizeOf(t.ofTypes());
        if (size == 0) { result.append("(empty intersect)"); }
        else if (size == 1) {
          result.append("(intersect ");
          run(IterUtil.first(t.ofTypes()));
          result.append(")");
        }
        else {
          boolean first = true;
          for (Type componentT : t.ofTypes()) {
            if (first) { first = false; }
            else { result.append(" & "); }
            run(componentT);
          }
        }
      }
      
      public void forUnionType(UnionType t) {
        int size = IterUtil.sizeOf(t.ofTypes());
        if (size == 0) { result.append("(empty union)"); }
        else if (size == 1) {
          result.append("(union ");
          run(IterUtil.first(t.ofTypes()));
          result.append(")");
        }
        else {
          boolean first = true;
          for (Type componentT : t.ofTypes()) {
            if (first) { first = false; }
            else { result.append(" | "); }
            run(componentT);
          }
        }
      }
      
      public void forWildcard(Wildcard t) {
        result.append("?");
        if (!isEqual(t.symbol().upperBound(), OBJECT)) {
          result.append(" extends ");
          run(t.symbol().upperBound());
        }
        if (!isEqual(t.symbol().lowerBound(), NULL)) {
          result.append(" super ");
          run(t.symbol().lowerBound());
        }
      }
      
    };
    
    Runnable1<Type> dumpType = new DumpType();
    dumpType.run(t);
    if (!variableHandler.isEmpty()) {
      result.append(" (");
      variableHandler.dumpBounds(dumpType);
      result.append(")");
    }
    return result.toString();
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
   *                                       {@code c}, or if the arguments are not within their bounds.
   */
  public ClassType makeClassType(DJClass c, Iterable<? extends Type> args) throws InvalidTypeArgumentException {
    if (IterUtil.isEmpty(args)) { return makeClassType(c); }
    else {
      Iterable<VariableType> params = SymbolUtil.allTypeParameters(c);
      if (IterUtil.sizeOf(params) != IterUtil.sizeOf(args)) { throw new InvalidTypeArgumentException(); }
      else {
        ParameterizedClassType result = new ParameterizedClassType(c, args);
        checkBounds(params, capture(result).typeArguments());
        return result;
      }
    }
  }
  
  
  /**
   * Convert the expression to a primitive.  The result is guaranteed to have a primitive type as its
   * TYPE property (according to {@link isPrimitive}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a primitive type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a primitive
   */
  public Expression makePrimitive(Expression e) throws UnsupportedConversionException {
    Type t = NodeProperties.getType(e);
    if (isPrimitive(t)) { return e; }
    // Note: The spec is not clear about whether a *subtype* (such as a variable) can
    //       be unboxed.  We allow it here, because that seems like the correct approach.
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
    ObjectMethodCall result = new ObjectMethodCall(exp, methodName, null, 
                                                   exp.getFilename(), exp.getBeginLine(), exp.getBeginColumn(), 
                                                   exp.getEndLine(), exp.getEndColumn());
    try {
      ObjectMethodInvocation inv = lookupMethod(exp, methodName, EMPTY_TYPE_ITERABLE, EMPTY_EXPRESSION_ITERABLE);
      result.setExpression(inv.object());
      result.setArguments(IterUtil.asList(inv.args()));
      NodeProperties.setMethod(result, inv.method());
      NodeProperties.setType(result, capture(inv.returnType()));
      return result;
    }
    catch (TypeSystemException e) { throw new RuntimeException("Unboxing method inaccessible", e); }
  }
  
  /**
   * Convert the expression to a reference.  The result is guaranteed to have a reference type as its
   * TYPE property (according to {@link isReferene}).
   * 
   * @param e  A typed expression
   * @return  A typed expression equivalent to {@code e} that has a reference type
   * @throws UnsupportedConversionException  If the expression cannot be converted to a reference
   */
  public Expression makeReference(final Expression e) throws UnsupportedConversionException {
    Type t = NodeProperties.getType(e);
    if (isReference(t)) { return e; }
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
      StaticMethodCall m = new StaticMethodCall(boxedTypeName, "valueOf", arguments, 
                                                exp.getFilename(), exp.getBeginLine(), exp.getBeginColumn(), 
                                                exp.getEndLine(), exp.getEndColumn());
      try {
        MethodInvocation inv = lookupStaticMethod(boxedType, "valueOf", EMPTY_TYPE_ITERABLE, arguments);
        m.setArguments(IterUtil.asList(inv.args()));
        NodeProperties.setMethod(m, inv.method());
        NodeProperties.setType(m, capture(inv.returnType()));
        return m;
      }
      catch (TypeSystemException e) { throw new RuntimeException("Boxing method inaccessible", e); }
    }
    else {
      SimpleAllocation k = new SimpleAllocation(boxedTypeName, arguments, exp.getFilename(), exp.getBeginLine(),
                                                exp.getBeginColumn(), exp.getEndLine(), exp.getEndColumn());
      try {
        ConstructorInvocation inv = lookupConstructor(boxedType, EMPTY_TYPE_ITERABLE, arguments); 
        k.setArguments(IterUtil.asList(inv.args()));
        NodeProperties.setConstructor(k, inv.constructor());
        NodeProperties.setType(k, boxedType);
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
   * of expressions are guaranteed to have the same type.  That type may contain uncaptured wildcards.
   * 
   * @param e1  A typed expression
   * @param e2  A typed expression
   * @return  Two typed expressions equivalent to {@code e1} and {@code e2} with the joined type
   * @throws UnsupportedConversionException  If the two types are incompatible.
   */
  public Pair<Expression, Expression> join(final Expression e1, final Expression e2) 
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
        return 
          isSubtype(t, CHARACTER_CLASS) || 
          isSubtype(t, BYTE_CLASS) || 
          isSubtype(t, SHORT_CLASS) || 
          isSubtype(t, INTEGER_CLASS) || 
          isSubtype(t, LONG_CLASS) ||
          isSubtype(t, FLOAT_CLASS) || 
          isSubtype(t, DOUBLE_CLASS);
      }
      
      private boolean isBooleanReference(Type t) { return isSubtype(t, BOOLEAN_CLASS); }
      
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
        catch (UnsupportedConversionException e) { throw new IllegalArgumentException(); }
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
  public Expression cast(Type target, final Expression e) 
    throws UnsupportedConversionException {
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
      
      public Expression defaultCase(Type target) {
        try {
          Expression result = makeReference(e);
          Type source = NodeProperties.getType(result);
          if (!isSubtype(source, target)) {
            // TODO: implement correctly instead of this simplified version (join might come in handy):           
            if (isSubtype(target, source)) {
              NodeProperties.setCheckedType(result, erasedClass(target));
              // TODO: unchecked warnings
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
              catch (UnsupportedConversionException e) { throw new RuntimeException("Error while boxing", e); }
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
              catch (UnsupportedConversionException e) { throw new RuntimeException("Error while boxing", e); }
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
              catch (UnsupportedConversionException e) { throw new RuntimeException("Error while boxing", e); }
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
              catch (UnsupportedConversionException e) { throw new RuntimeException("Error while boxing", e); }
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
    Expression result = new CastExpression(null, e, e.getFilename(), e.getBeginLine(), e.getBeginColumn(),
                                           e.getEndLine(), e.getEndColumn());
    if (isPrimitive(target)) {
      if (!isEqual(target, NodeProperties.getType(e))) {
        NodeProperties.setConvertedType(e, erasedClass(target));
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
    ArrayInitializer init = new ArrayInitializer(IterUtil.asList(elements));
    NodeProperties.setType(init, arrayType);
    NodeProperties.setErasedType(init, erasedType);
    Expression result = new ArrayAllocation(tn, new ArrayAllocation.TypeDescriptor(new ArrayList<Expression>(0), 
                                                                                   1, init, 0, 0));
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
  
  private Type immediateSupertype(ClassType t) {
    return t.apply(new TypeAbstractVisitor<Type>() {
      public Type defaultCase(Type t) { throw new IllegalArgumentException(); }
      public Type forSimpleClassType(SimpleClassType t) { return immediateSupertype(t); }
      public Type forRawClassType(RawClassType t) { return immediateSupertype(t); }
      public Type forParameterizedClassType(ParameterizedClassType t) { return immediateSupertype(t); }
    });
  }
  
  private Type immediateSupertype(SimpleClassType t) {
    if (t.equals(OBJECT)) { return null; }
    else {
      Type result = null;
      for (Type sup : t.ofClass().declaredSupertypes()) {
        result = (result == null) ? sup : meet(result, sup);
      }
      if (result == null) result = OBJECT;
      return result;
    }
  }
  
  private Type immediateSupertype(RawClassType t) {
    Type result = null;
    for (Type sup : t.ofClass().declaredSupertypes()) {
      Type erasedSup = erase(sup);
      result = (result == null) ? erasedSup : meet(result, erasedSup);
    }
    if (result == null) result = OBJECT;
    return result;
  }
  
  private Type immediateSupertype(ParameterizedClassType t) {
    ParameterizedClassType tCap = capture(t);
    SubstitutionMap sigma = 
      new SubstitutionMap(SymbolUtil.allTypeParameters(tCap.ofClass()), tCap.typeArguments());
    Type result = null;
    for (Type sup : t.ofClass().declaredSupertypes()) {
      Type instantiatedSup = substitute(sup, sigma);
      result = (result == null) ? instantiatedSup : meet(result, instantiatedSup);
    }
    if (result == null) result = OBJECT;
    return result;
  }
  
  
  /**
   * A set of constraints on TypeVariables used to perform variable inference.  The sets are
   * immutable.  A ConstraintSet may be created either by using the singletons {@code EMPTY} 
   * and {@code UNSATISFIABLE} or by invoking {@code andLowerBound}, {@code andUpperBound}, 
   * {@code and}, or {@code or}.
   */
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class ConstraintSet {
    
    private Map<VariableType, Type> _lowerBounds;
    private Map<VariableType, Type> _upperBounds;
    
    protected ConstraintSet() {
      _lowerBounds = new HashMap<VariableType, Type>();
      _upperBounds = new HashMap<VariableType, Type>();
    }
    
    protected ConstraintSet(ConstraintSet copy) {
      _lowerBounds = new HashMap<VariableType, Type>(copy._lowerBounds);
      _upperBounds = new HashMap<VariableType, Type>(copy._upperBounds);
    }
    
    public boolean isSatisfiable() { return true; }
    
    public boolean isEmpty() { return false; }
    
    /** @return  The lower bound on {@code var}.  Guaranteed to be a subtype of {@code upperBound(var)} */
    public Type lowerBound(VariableType var) { 
      Type result = _lowerBounds.get(var);
      return (result == null) ? NULL : result; // for full generality, this would use BOTTOM instead
    }
    
    /** @return  The upper bound on {@code var}.  Guaranteed to be a supertype of {@code lowerBound(var)} */
    public Type upperBound(VariableType var) { 
      Type result = _upperBounds.get(var);
      return (result == null) ? OBJECT : result; // for full generality, this would use TOP instead
    }
    
    public ConstraintSet and(ConstraintSet s) {
      if (!s.isSatisfiable()) { return s; }
      else if (s.isEmpty()) { return this; }
      else {
        ConstraintSet result = new ConstraintSet(this);
        for (Map.Entry<VariableType, Type> entry : s._lowerBounds.entrySet()) {
          result = result.andLowerBound(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<VariableType, Type> entry : s._upperBounds.entrySet()) {
          result = result.andUpperBound(entry.getKey(), entry.getValue());
        }
        return result;
      }
    }
    
    public ConstraintSet or(ConstraintSet s) {
      if (!s.isSatisfiable()) { return this; }
      else if (s.isEmpty()) { return s; }
      else {
        return and(s); // We err on the side of caution -- if X *and* Y hold, then X *or* Y holds
        // TODO: relax this constraint
      }
    }
    
    public ConstraintSet andLowerBound(VariableType var, Type bound) {
      if (isSubtype(bound, upperBound(var))) {
        // The join is also a subtype of upperBound(var)
        Type currentLower = lowerBound(var);
        Type newLower = join(currentLower, bound);
        if (currentLower == newLower) { return this; }
        else {
          ConstraintSet result = new ConstraintSet(this);
          result._lowerBounds.put(var, newLower);
          return result;
        }
      }
      else { return UNSATISFIABLE_CONSTRAINTS; }
    }
    
    public ConstraintSet andUpperBound(VariableType var, Type bound) {
      if (isSubtype(lowerBound(var), bound)) {
        // The meet is also a supertype of lowerBound(var)
        Type currentUpper = upperBound(var);
        Type newUpper = meet(currentUpper, bound);
        if (currentUpper == newUpper) { return this; }
        else {
          ConstraintSet result = new ConstraintSet(this);
          result._upperBounds.put(var, newUpper);
          return result;
        }
      }
      else { return UNSATISFIABLE_CONSTRAINTS; }
    }
    
    public String toString() {
      if (isEmpty()) { return "{}"; }
      else if (!isSatisfiable()) { return "{ false }"; }
      else {
        StringBuilder result = new StringBuilder();
        result.append("{ ");
        boolean first = true;
        
        for (Map.Entry<VariableType, Type> entry : _lowerBounds.entrySet()) {
          if (!first) { result.append(", "); }
          first = false;
          result.append(entry.getKey() + " :> " + userRepresentation(entry.getValue()));
        }
        for (Map.Entry<VariableType, Type> entry : _upperBounds.entrySet()) {
          if (!first) { result.append(", "); }
          first = false;
          result.append(entry.getKey() + " <: " + userRepresentation(entry.getValue()));
        }
        result.append(" }");
        return result.toString();
      }
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private final ConstraintSet EMPTY_CONSTRAINTS = new ConstraintSet() {
    @Override public boolean isEmpty() { return true; }
    @Override public ConstraintSet and(ConstraintSet s) { return s; }
    @Override public ConstraintSet or(ConstraintSet s) { return this; }
  };
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  public final ConstraintSet UNSATISFIABLE_CONSTRAINTS = new ConstraintSet() {
    @Override public boolean isSatisfiable() { return false; }
    @Override public ConstraintSet and(ConstraintSet s) { return this; }
    @Override public ConstraintSet or(ConstraintSet s) { return s; }
    @Override public ConstraintSet andLowerBound(VariableType var, Type bound) { return this; }
    @Override public ConstraintSet andUpperBound(VariableType var, Type bound) { return this; }
  };
    
  /** Used to keep track of the inference method called in the inference recursion stack */
  private static enum InferenceMode { SUBTYPE, EQUAL, SUPERTYPE };
  
  /**
   * Top-level entry point for type inference.  Produces the set of types corresponding to the given
   * type parameters, given that {@code args} were provided where {@code params} were expected
   * ({@code args} and {@code params} are assumed to have the same length).  The inference algorithm
   * is <em>sound</em> but not <em>complete</em>: it is possible that the algorithm will return 
   * {@code null} when there exists some substitution that would make each argument a subtype of its 
   * corresponding parameter; but whenever the algorithm returns a solution, that solution is guaranteed
   * to produce a valid subtype relationship between the arguments and parameters.
   * 
   * @return  A set of inferred type arguments for {@code tparams}, or {@code null} if the parameters
   *          are overconstrained
   */
  private Iterable<Type> inferTypeArguments(Iterable<? extends VariableType> tparams, 
                                            Iterable<? extends Type> params,
                                            Iterable<? extends Type> args) {
    //debug.logValues("Beginning inferTypeArguments", new String[]{ "tparams", "params", "args" },
    //                wrap(tparams), wrap(params), wrap(args));
    RecursionStack3<Type, Type, InferenceMode> stack = RecursionStack3.make();
    Set<? extends VariableType> tparamSet = CollectUtil.asSet(tparams);
    
    ConstraintSet constraintsBuilder = EMPTY_CONSTRAINTS;
    for (Pair<Type, Type> pair : IterUtil.zip(args, params)) {
      constraintsBuilder = constraintsBuilder.and(inferFromSubtype(pair.first(), pair.second(), 
                                                                   tparamSet, stack));
      if (!constraintsBuilder.isSatisfiable()) { break; }
    }
    
    final ConstraintSet constraints = constraintsBuilder; // constraints must be redeclared as final
//    System.out.println("Final inference result: " + constraints);
    if (constraints.isSatisfiable()) {
      return IterUtil.mapSnapshot(tparams, new Lambda<VariableType, Type>() {
        public Type value(VariableType param) { return constraints.lowerBound(param); }
        // TODO: Handle the case where the lower bound is BOTTOM (following the JLS)
      });
    }
    else { return null; }
  }
  
  private boolean containsInferenceVariable(Type t, Set<? extends VariableType> vars) {
    return containsInferenceVariable(t, vars, new RecursionStack<Type>());
  }
  
  private boolean containsInferenceVariable(Type t, final Set<? extends VariableType> vars, 
                                            final RecursionStack<Type> stack) {
    return t.apply(new TypeAbstractVisitor<Boolean>() {
      public Boolean defaultCase(Type t) { return false; }
      
      @Override public Boolean forArrayType(ArrayType t) { 
        return containsInferenceVariable(t.ofType(), vars, stack);
      }
      
      @Override public Boolean forParameterizedClassType(ParameterizedClassType t) { 
        return checkList(t.typeArguments());
      }
      
      @Override public Boolean forIntersectionType(IntersectionType t) { 
        return checkList(t.ofTypes());
      }
      
      private Boolean checkList(Iterable<? extends Type> types) {
        for (Type t : types) { 
          if (containsInferenceVariable(t, vars, stack)) { return true; }
        }
        return false;
      }
      
      @Override public Boolean forVariableType(final VariableType t) {
        if (vars.contains(t)) { return true; }
        else {
          Thunk<Boolean> handleBounds = new Thunk<Boolean>() {
            public Boolean value() {
              return containsInferenceVariable(t.symbol().lowerBound(), vars, stack) ||
                containsInferenceVariable(t.symbol().upperBound(), vars, stack);
            }
          };
          return stack.apply(handleBounds, false, t);
        }
      }
      
      @Override public Boolean forWildcard(final Wildcard t) {
        Thunk<Boolean> handleBounds = new Thunk<Boolean>() {
          public Boolean value() {
            return containsInferenceVariable(t.symbol().lowerBound(), vars, stack) ||
              containsInferenceVariable(t.symbol().upperBound(), vars, stack);
          }
        };
        return stack.apply(handleBounds, false, t);
      }
      
    });
  }
  
  /** 
   * Produce the constraints on {@code vars} that may be inferred, assuming {@code arg} is a subtype
   * of {@code param}
   */
  private ConstraintSet inferFromSubtype(final Type arg, final Type param,
                                         final Set<? extends VariableType> vars, 
                                         final RecursionStack3<Type, Type, InferenceMode> stack) {
    //debug.logValues(new String[]{ "arg", "param" }, wrap(arg), wrap(param));
    if (containsInferenceVariable(param, vars)) {
      return param.apply(new TypeAbstractVisitor<ConstraintSet>() {
        public ConstraintSet defaultCase(Type param) { throw new IllegalArgumentException(); }
        
        @Override public ConstraintSet forArrayType(final ArrayType param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forNullType(NullType arg) { return EMPTY_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forBottomType(BottomType arg) { return EMPTY_CONSTRAINTS; }
            
            @Override public ConstraintSet forArrayType(ArrayType arg) {
              if (isPrimitive(arg.ofType())) { 
                return inferFromEqual(arg.ofType(), param.ofType(), vars, stack);
              }
              else { return inferFromSubtype(arg.ofType(), param.ofType(), vars, stack); }
            }
          });
        }
        
        @Override public ConstraintSet forParameterizedClassType(final ParameterizedClassType param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forNullType(NullType arg) { return EMPTY_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forBottomType(BottomType arg) { return EMPTY_CONSTRAINTS; }
            
            @Override public ConstraintSet forArrayType(ArrayType arg) {
              return inferFromSubtype(CLONEABLE_AND_SERIALIZABLE, param, vars, stack);
            }
            
            @Override public ConstraintSet forSimpleClassType(SimpleClassType arg) {
              Type argSuper = immediateSupertype(arg);
              if (argSuper == null) { return UNSATISFIABLE_CONSTRAINTS; }
              else { return inferFromSubtype(argSuper, param, vars, stack); }
            }
            
            @Override public ConstraintSet forRawClassType(RawClassType arg) {
              Type argSuper = immediateSupertype(arg);
              if (argSuper == null) { return UNSATISFIABLE_CONSTRAINTS; }
              else { return inferFromSubtype(argSuper, param, vars, stack); }
            }
            
            @Override public ConstraintSet forParameterizedClassType(final ParameterizedClassType arg) {
              ConstraintSet matchConstraints = UNSATISFIABLE_CONSTRAINTS;
              if (param.ofClass().equals(arg.ofClass())) {
                Thunk<ConstraintSet> recurOnTargs = new Thunk<ConstraintSet>() {
                  public ConstraintSet value() {
                    ParameterizedClassType argCap = capture(arg);
                    ConstraintSet result = EMPTY_CONSTRAINTS;
                    for (Pair<Type, Type> pair : IterUtil.zip(argCap.typeArguments(), param.typeArguments())) {
                      final Type argArg = pair.first();
                      final Type paramArg = pair.second();
                      result = result.and(paramArg.apply(new TypeAbstractVisitor<ConstraintSet>() {
                        public ConstraintSet defaultCase(Type paramArg) { 
                          return inferFromEqual(argArg, paramArg, vars, stack);
                        }
                        @Override public ConstraintSet forWildcard(Wildcard paramArg) {
                          ConstraintSet cs = inferFromSupertype(argArg, paramArg.symbol().lowerBound(),
                                                                vars, stack);
                          if (cs.isSatisfiable()) {
                            cs = cs.and(inferFromSubtype(argArg, paramArg.symbol().upperBound(), vars, stack));
                          }
                          return cs;
                        }
                      }));
                      if (!result.isSatisfiable()) { break; }
                    }
                    return result;
                  }
                };
                matchConstraints = stack.apply(recurOnTargs, EMPTY_CONSTRAINTS, arg, param, 
                                               InferenceMode.SUBTYPE);
              }
              
              ConstraintSet superConstraints = UNSATISFIABLE_CONSTRAINTS;
              Type argSuper = immediateSupertype(arg);
              if (argSuper != null) { superConstraints = inferFromSubtype(argSuper, param, vars, stack); }
              
              return matchConstraints.or(superConstraints);
            }
          });
        }
        
        @Override public ConstraintSet forVariableType(final VariableType param) {
          // Note that this might be a capture variable with an inference-variable bound
          if (vars.contains(param)) { return EMPTY_CONSTRAINTS.andLowerBound(param, arg); }
          else {
            Thunk<ConstraintSet> recurOnLowerBound = new Thunk<ConstraintSet>() {
              public ConstraintSet value() {
                return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
                  public ConstraintSet defaultCase(Type arg) {
                    return inferFromSubtype(arg, param.symbol().lowerBound(), vars, stack);
                  }
                  
                  @Override public ConstraintSet forVariableType(VariableType arg) {
                    ConstraintSet cs1 = inferFromSubtype(arg, param.symbol().lowerBound(), vars, stack);
                    ConstraintSet cs2 = inferFromSubtype(arg.symbol().upperBound(), param, vars, stack);
                    return cs1.or(cs2);
                  }
                  
                  @Override public ConstraintSet forIntersectionType(IntersectionType arg) {
                    ConstraintSet result = inferFromSubtype(arg, param.symbol().lowerBound(), vars, stack);
                    for (Type supArg : arg.ofTypes()) {
                      if (result.isEmpty()) { break; }
                      result = result.or(inferFromSubtype(supArg, param, vars, stack));
                    }
                    return result;
                  }
                  
                  @Override public ConstraintSet forBottomType(BottomType arg) { return EMPTY_CONSTRAINTS; }
                });
              }
            };
            return stack.apply(recurOnLowerBound, UNSATISFIABLE_CONSTRAINTS, arg, param, 
                               InferenceMode.SUBTYPE);
          }
        }
        
        @Override public ConstraintSet forIntersectionType(IntersectionType param) {
          if (arg instanceof BottomType) { return EMPTY_CONSTRAINTS; }
          else {
            ConstraintSet result = EMPTY_CONSTRAINTS;
            for (Type supParam : param.ofTypes()) {
              result = result.and(inferFromSubtype(arg, supParam, vars, stack));
              if (!result.isSatisfiable()) { break; }
            }
            return result;
          }
        }
        
        @Override public ConstraintSet forWildcard(final Wildcard param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forWildcard(Wildcard arg) {
              return inferFromEqual(arg, param, vars, stack);
            }
            @Override public ConstraintSet forBottomType(BottomType arg) { return EMPTY_CONSTRAINTS; }
          });
        }
        
        private ConstraintSet handleVariableArg(final VariableType arg) {
          Thunk<ConstraintSet> recurOnBound = new Thunk<ConstraintSet>() {
            public ConstraintSet value() {
              return inferFromSubtype(arg.symbol().upperBound(), param, vars, stack);
            }
          };
          return stack.apply(recurOnBound, UNSATISFIABLE_CONSTRAINTS, arg, param, InferenceMode.SUBTYPE);
        }
        
        private ConstraintSet handleIntersectionArg(IntersectionType arg) {
          ConstraintSet result = UNSATISFIABLE_CONSTRAINTS;
          for (Type supArg : arg.ofTypes()) {
            result = result.or(inferFromSubtype(supArg, param, vars, stack));
            if (result.isEmpty()) { break; }
          }
          return result;
        }
      });
    }
    else { return isSubtype(arg, param) ? EMPTY_CONSTRAINTS : UNSATISFIABLE_CONSTRAINTS; }
  }
  
  /** 
   * Produce the constraints on {@code vars} that may be inferred, assuming {@code arg} is a supertype
   * of {@code param}
   */
  private ConstraintSet inferFromSupertype(final Type arg, final Type param,
                                           final Set<? extends VariableType> vars, 
                                           final RecursionStack3<Type, Type, InferenceMode> stack) {
    //debug.logValues(new String[]{ "arg", "param" }, wrap(arg), wrap(param));
    if (containsInferenceVariable(param, vars)) {
      return param.apply(new TypeAbstractVisitor<ConstraintSet>() {
        public ConstraintSet defaultCase(Type param) { throw new IllegalArgumentException(); }
        
        @Override public ConstraintSet forArrayType(final ArrayType param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forTopType(TopType arg) { return EMPTY_CONSTRAINTS; }
            
            @Override public ConstraintSet forArrayType(ArrayType arg) {
              if (isPrimitive(arg.ofType())) { 
                return inferFromEqual(arg.ofType(), param.ofType(), vars, stack);
              }
              else { return inferFromSupertype(arg.ofType(), param.ofType(), vars, stack); }
            }
            
            @Override public ConstraintSet forClassType(ClassType arg) {
              return inferFromSupertype(arg, CLONEABLE_AND_SERIALIZABLE, vars, stack);
            }
          });
        }
        
        @Override public ConstraintSet forParameterizedClassType(final ParameterizedClassType param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forTopType(TopType arg) { return EMPTY_CONSTRAINTS; }
            
            @Override public ConstraintSet forSimpleClassType(SimpleClassType arg) {
              Type paramSuper = immediateSupertype(param);
              if (paramSuper == null) { return EMPTY_CONSTRAINTS; }
              else { return inferFromSupertype(arg, paramSuper, vars, stack); }
            }
            
            @Override public ConstraintSet forRawClassType(RawClassType arg) {
              Type paramSuper = immediateSupertype(param);
              if (paramSuper == null) { return EMPTY_CONSTRAINTS; }
              else { return inferFromSupertype(arg, paramSuper, vars, stack); }
            }
            
            @Override public ConstraintSet forParameterizedClassType(final ParameterizedClassType arg) {
              ConstraintSet matchConstraints = UNSATISFIABLE_CONSTRAINTS;
              if (param.ofClass().equals(arg.ofClass())) {
                Thunk<ConstraintSet> recurOnTargs = new Thunk<ConstraintSet>() {
                  public ConstraintSet value() {
                    ParameterizedClassType paramCap = capture(arg);
                    ConstraintSet result = EMPTY_CONSTRAINTS;
                    for (Pair<Type, Type> pair : IterUtil.zip(arg.typeArguments(), paramCap.typeArguments())) {
                      Type argArg = pair.first();
                      final Type paramArg = pair.second();
                      result = result.and(argArg.apply(new TypeAbstractVisitor<ConstraintSet>() {
                        public ConstraintSet defaultCase(Type argArg) {
                          return inferFromEqual(argArg, paramArg, vars, stack);
                        }
                        @Override public ConstraintSet forWildcard(Wildcard argArg) {
                          ConstraintSet cs = inferFromSubtype(argArg.symbol().lowerBound(), paramArg, vars, stack);
                          if (cs.isSatisfiable()) {
                            cs = cs.and(inferFromSupertype(argArg.symbol().upperBound(), paramArg, vars, stack));
                          }
                          return cs;
                        }
                      }));
                      if (!result.isSatisfiable()) { break; }
                    }
                    return result;
                  }
                };
                matchConstraints = stack.apply(recurOnTargs, EMPTY_CONSTRAINTS, arg, param, 
                                               InferenceMode.SUPERTYPE);
              }
              
              ConstraintSet superConstraints = UNSATISFIABLE_CONSTRAINTS;
              Type paramSuper = immediateSupertype(param);
              if (paramSuper != null) { superConstraints = inferFromSupertype(arg, paramSuper, vars, stack); }
              
              return matchConstraints.or(superConstraints);
            }
          });
        }
        
        @Override public ConstraintSet forVariableType(final VariableType param) {
          // Note that this might be a capture variable with an inference-variable bound
          if (vars.contains(param)) {
            return EMPTY_CONSTRAINTS.andUpperBound(param, arg);
          }
          else {
            Thunk<ConstraintSet> recurOnUpperBound = new Thunk<ConstraintSet>() {
              public ConstraintSet value() {
                return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
                  public ConstraintSet defaultCase(Type arg) {
                    return inferFromSupertype(arg, param.symbol().upperBound(), vars, stack);
                  }
                  
                  @Override public ConstraintSet forVariableType(VariableType arg) {
                    ConstraintSet cs1 = inferFromSupertype(arg, param.symbol().upperBound(), vars, stack);
                    ConstraintSet cs2 = inferFromSupertype(arg.symbol().lowerBound(), param, vars, stack);
                    return cs1.or(cs2);
                  }
                  
                  @Override public ConstraintSet forIntersectionType(IntersectionType arg) { 
                    return handleIntersectionArg(arg);
                  }
                  
                  @Override public ConstraintSet forTopType(TopType arg) { return EMPTY_CONSTRAINTS; }
                });
              }
            };
            return stack.apply(recurOnUpperBound, UNSATISFIABLE_CONSTRAINTS, arg, param, 
                               InferenceMode.SUPERTYPE);
          }
        }
        
        @Override public ConstraintSet forIntersectionType(final IntersectionType param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            
            public ConstraintSet defaultCase(Type arg) {
              ConstraintSet result = UNSATISFIABLE_CONSTRAINTS;
              for (Type supParam : param.ofTypes()) {
                result = result.or(inferFromSupertype(arg, supParam, vars, stack));
                if (result.isEmpty()) { break; }
              }
              return result;
            }
            
            @Override public ConstraintSet forVariableType(final VariableType arg) {
              Thunk<ConstraintSet> recurOnBound = new Thunk<ConstraintSet>() {
                public ConstraintSet value() {
                  return inferFromSupertype(arg.symbol().lowerBound(), param, vars, stack);
                }
              };
              ConstraintSet result = stack.apply(recurOnBound, UNSATISFIABLE_CONSTRAINTS, arg, param,
                                                 InferenceMode.SUPERTYPE);
              for (Type supParam : param.ofTypes()) {
                if (result.isEmpty()) { break; }
                result = result.or(inferFromSupertype(arg, supParam, vars, stack));
              }
              return result;
            }
            
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            
            @Override public ConstraintSet forTopType(TopType arg) { return EMPTY_CONSTRAINTS; }
          });
        }
        
        @Override public ConstraintSet forWildcard(final Wildcard param) {
          return arg.apply(new TypeAbstractVisitor<ConstraintSet>() {
            public ConstraintSet defaultCase(Type arg) { return UNSATISFIABLE_CONSTRAINTS; }
            @Override public ConstraintSet forVariableType(VariableType arg) { return handleVariableArg(arg); }
            @Override public ConstraintSet forIntersectionType(IntersectionType arg) { return handleIntersectionArg(arg); }
            @Override public ConstraintSet forWildcard(Wildcard arg) { 
              return inferFromEqual(arg, param, vars, stack);
            }
            @Override public ConstraintSet forTopType(TopType arg) { return EMPTY_CONSTRAINTS; }
          });
        }
        
        private ConstraintSet handleVariableArg(final VariableType arg) {
          Thunk<ConstraintSet> recurOnBound = new Thunk<ConstraintSet>() {
            public ConstraintSet value() {
              return inferFromSupertype(arg.symbol().lowerBound(), param, vars, stack);
            }
          };
          return stack.apply(recurOnBound, UNSATISFIABLE_CONSTRAINTS, arg, param, InferenceMode.SUPERTYPE);
        }
        
        private ConstraintSet handleIntersectionArg(IntersectionType arg) {
          ConstraintSet result = EMPTY_CONSTRAINTS;
          for (Type supArg : arg.ofTypes()) { 
            result = result.and(inferFromSupertype(supArg, param, vars, stack));
            if (!result.isSatisfiable()) { break; }
          }
          return result;
        }
      });
    }
    else {
      return (isSubtype(param, arg)) ? EMPTY_CONSTRAINTS : UNSATISFIABLE_CONSTRAINTS;
    }
  }
  
  /** 
   * Produce the constraints on {@code vars} that may be inferred, assuming {@code arg} is equal to
   * {@code param}
   */
  private ConstraintSet inferFromEqual(final Type arg, final Type param,
                                       final Set<? extends VariableType> vars, 
                                       final RecursionStack3<Type, Type, InferenceMode> stack) {
    //debug.logValues(new String[]{ "arg", "param" }, wrap(arg), wrap(param));
    if (vars.contains(param)) {
      return param.apply(new TypeAbstractVisitor<ConstraintSet>() {
        public ConstraintSet defaultCase(Type param) { return UNSATISFIABLE_CONSTRAINTS; }
        
        @Override public ConstraintSet forArrayType(ArrayType param) {
          if (arg instanceof ArrayType) { 
            return inferFromEqual(((ArrayType) arg).ofType(), param.ofType(), vars, stack);
          }
          else { return UNSATISFIABLE_CONSTRAINTS; }
        }
        
        @Override public ConstraintSet forParameterizedClassType(ParameterizedClassType param) {
          if (arg instanceof ParameterizedClassType) {
            ParameterizedClassType argCast = (ParameterizedClassType) arg;
            if (param.ofClass().equals(argCast.ofClass())) {
              ConstraintSet result = EMPTY_CONSTRAINTS;
              for (Pair<Type, Type> pair : IterUtil.zip(argCast.typeArguments(), param.typeArguments())) {
                result = result.and(inferFromEqual(pair.first(), pair.second(), vars, stack));
                if (!result.isSatisfiable()) { break; }
              }
              return result;
            }
            else { return UNSATISFIABLE_CONSTRAINTS; }
          }
          else { return UNSATISFIABLE_CONSTRAINTS; }
        }
        
        @Override public ConstraintSet forVariableType(VariableType param) {
          if (vars.contains(param)) {
            return EMPTY_CONSTRAINTS.andLowerBound(param, arg).andUpperBound(param, arg);
          }
          else { return UNSATISFIABLE_CONSTRAINTS; }
        }
        
        @Override public ConstraintSet forIntersectionType(IntersectionType param) {
          if (arg instanceof IntersectionType) {
            Iterable<? extends Type> argSups = ((IntersectionType) arg).ofTypes();
            if (IterUtil.sizeOf(argSups) != IterUtil.sizeOf(param.ofTypes())) {
              return UNSATISFIABLE_CONSTRAINTS;
            }
            else {
              ConstraintSet result = UNSATISFIABLE_CONSTRAINTS;
              for (Iterable<Type> paramSups : PermutationIterable.make(param.ofTypes())) {
                ConstraintSet thisPerm = EMPTY_CONSTRAINTS;
                for (Pair<Type, Type> pair : IterUtil.zip(argSups, paramSups)) {
                  thisPerm = thisPerm.and(inferFromEqual(pair.first(), pair.second(), vars, stack));
                  if (!thisPerm.isSatisfiable()) { break; }
                }
                result = result.or(thisPerm);
                if (result.isEmpty()) { break; }
              }
              return result;
            }
          }
          else { return UNSATISFIABLE_CONSTRAINTS; }
        }
        
        @Override public ConstraintSet forWildcard(final Wildcard param) {
          if (arg instanceof Wildcard) {
            Thunk<ConstraintSet> recurOnBounds = new Thunk<ConstraintSet>() {
              public ConstraintSet value() {
                Wildcard argCast = (Wildcard) arg;
                ConstraintSet result = inferFromEqual(argCast.symbol().upperBound(), param.symbol().upperBound(),
                                                      vars, stack);
                if (result.isSatisfiable()) { 
                  result = result.and(inferFromEqual(argCast.symbol().lowerBound(), param.symbol().lowerBound(),
                                                     vars, stack));
                }
                return result;
              }
            };
            return stack.apply(recurOnBounds, EMPTY_CONSTRAINTS, arg, param, InferenceMode.EQUAL);
          }
          else { return UNSATISFIABLE_CONSTRAINTS; }
        }
      });
    }
    else {
      return (isEqual(arg, param)) ? EMPTY_CONSTRAINTS : UNSATISFIABLE_CONSTRAINTS;
    }
  }
  
  
  /**
   * A wrapper to optimize the three-stage method-signature matching algorithm.  Clients are
   * assumed to invoke the methods {@code matches}, {@code matchesWithBoxing}, and 
   * {@code matchesWithVarargs} in that order, and only if the previous match attempt failed; 
   * other accessor methods provide the results of a match <em>after</em> one of the match methods 
   * returns {@code true}.
   */
  private static abstract class SignatureChecker {
    /** Must be invoked first */
    public abstract boolean matches();
    
    /** Must only be invoked after {@link matches()} */
    public abstract boolean matchesWithBoxing();
    
    /** Must only be invoked after {@link matchesWithAutoboxing} */
    public abstract boolean matchesWithVarargs();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends VariableType> typeParameters();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends Type> typeArguments();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends Type> parameters();
    
    /** Must only be invoked after one of the match() methods returns {@code true} */
    public abstract Iterable<? extends Expression> arguments();
  }
  
  private static class NullChecker extends SignatureChecker {
    public static final NullChecker INSTANCE = new NullChecker();
    private NullChecker() {}
    public boolean matches() { return false; }
    public boolean matchesWithBoxing() { return false; }
    public boolean matchesWithVarargs() { return false; }
    public Iterable<? extends VariableType> typeParameters() { throw new IllegalStateException(); }
    public Iterable<? extends Type> typeArguments() { throw new IllegalStateException(); }
    public Iterable<? extends Type> parameters() { throw new IllegalStateException(); }
    public Iterable<? extends Expression> arguments() { throw new IllegalStateException(); }
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class SimpleChecker extends SignatureChecker {
    protected Iterable<? extends Type> _params;
    protected Iterable<? extends Expression> _args;
    protected Iterable<? extends VariableType> _tparams;
    protected Iterable<? extends Type> _targs;
    protected Type _paramForVarargs; // set at some point before matchesWithVarargs()
    protected Expression _argForVarargs; // set at some point before matchesWithVarargs(); not boxed
    protected boolean _matchesAllButLast; // true if matchesWithBoxing() is true on all but the last arg
    
    /**  Assumes {@code params.size() == args.size()} */
    public SimpleChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                         Iterable<? extends VariableType> tparams, Iterable<? extends Type> targs) {
      _params = params;
      _args = args;
      _tparams = tparams;
      _targs = targs;
    }
    
    public Iterable<? extends VariableType> typeParameters() { return _tparams; }
    public Iterable<? extends Type> typeArguments() { return _targs; }
    public Iterable<? extends Type> parameters() { return _params; }
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
        ArrayType arrayT = (ArrayType) substitute((ArrayType) _paramForVarargs, _tparams, _targs);
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
     * Update {@code _args} with the boxed or unboxed versions of the arguments; set {@link _paramForVarargs}
     * and {@link _argForVarargs} before performing the conversion on the last argument
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
  private class InferenceChecker extends SimpleChecker {
    
    /**  Assumes {@code params.size() == args.size()} */
    public InferenceChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                            Iterable<? extends VariableType> tparams) {
      super(params, args, tparams, null);
    }
    
    @Override public boolean matches() {
      Iterable<Type> argTypes = IterUtil.mapSnapshot(_args, TYPE_OF_EXPRESSION);
      _targs = inferTypeArguments(_tparams, _params, argTypes);
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
        Iterable<Type> argTypes = IterUtil.mapSnapshot(inferenceArgs, TYPE_OF_EXPRESSION);
        Iterable<Type> paramTypes = IterUtil.compose(IterUtil.skipLast(_params), elementT);
        _targs = inferTypeArguments(_tparams, paramTypes, argTypes);
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
  private class EmptyVarargChecker extends SimpleChecker {
    
    // Can't use _paramForVarargs, because SimpleChecker doesn't know about the final parameter
    private Type _varargParam;
    
    /**  Assumes {@code params.size() - 1 == args.size()} */
    public EmptyVarargChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
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
          ArrayType arrayT = (ArrayType) substitute((ArrayType) _varargParam, _tparams, _targs);
          _args = IterUtil.compose(_args, makeArray(arrayT, EMPTY_EXPRESSION_ITERABLE));
          return true;
        }
      }
      return false;
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class EmptyVarargInferenceChecker extends InferenceChecker {
    
    // Can't use _paramForVarargs, because SimpleChecker doesn't know about the final parameter
    private Type _varargParam;
    
    /**  Assumes {@code params.size() - 1 == args.size()} */
    public EmptyVarargInferenceChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                                       Iterable<? extends VariableType> tparams) {
      super(params, args, tparams);
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
          ArrayType arrayT = (ArrayType) substitute((ArrayType) _varargParam, _tparams, _targs);
          _args = IterUtil.compose(_args, makeArray(arrayT, EMPTY_EXPRESSION_ITERABLE));
          return true;
        }
      }
      return false;
    }
    
  }
  
  // cannot be defined statically, because it relies on the definition of non-static methods
  private class MultiVarargChecker extends SimpleChecker {
    
    private Type _varargParam;
    private Iterable<Expression> _varargArgs;
    
    /**  Assumes {@code 1 <= params.size() < args.size()} */
    public MultiVarargChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
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
        if (matches()) {
          ArrayType arrayT = (ArrayType) substitute((ArrayType) _varargParam, _tparams, _targs);
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
  private class MultiVarargInferenceChecker extends InferenceChecker {
    
    private Type _varargParam;
    private Iterable<Expression> _varargArgs;
    
    /**  Assumes {@code 1 <= params.size() < args.size()} */
    public MultiVarargInferenceChecker(Iterable<? extends Type> params, Iterable<? extends Expression> args, 
                                       Iterable<? extends VariableType> tparams) {
      super(params, args, tparams);
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
        Iterable<Type> argTypes = IterUtil.mapSnapshot(inferenceArgs, TYPE_OF_EXPRESSION);
        Iterable<Type> varargParams = IterUtil.copy(elementT, IterUtil.sizeOf(_varargArgs));
        Iterable<Type> paramTypes = IterUtil.compose(_params, varargParams);
        _targs = inferTypeArguments(_tparams, paramTypes, argTypes);
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
  
  
  private SignatureChecker makeChecker(Iterable<? extends VariableType> tparams, Iterable<? extends Type> targs,
                                       Iterable<? extends Type> params, Iterable<? extends Expression> args) {
    // Note: per the JLS, we allow the presense of (ignored) targs when tparams is empty
    int argCount = IterUtil.sizeOf(args);
    int paramCount = IterUtil.sizeOf(params);
    if (argCount == paramCount - 1) {
      if (IterUtil.isEmpty(tparams)) {
        return new EmptyVarargChecker(params, args, tparams, EMPTY_TYPE_ITERABLE);
      }
      else if (IterUtil.isEmpty(targs)) { return new EmptyVarargInferenceChecker(params, args, tparams); }
      else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) {
        return new EmptyVarargChecker(substitute(params, tparams, targs), args, tparams, targs);
      }
      else { return NullChecker.INSTANCE; }
    }
    else if (argCount == paramCount) {
      if (IterUtil.isEmpty(tparams)) { 
        return new SimpleChecker(params, args, tparams, EMPTY_TYPE_ITERABLE);
      }
      else if (IterUtil.isEmpty(targs)) { return new InferenceChecker(params, args, tparams); }
      else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) { 
        return new SimpleChecker(substitute(params, tparams, targs), args, tparams, targs);
      }
      else { return NullChecker.INSTANCE; }
    }
    else if (argCount > paramCount && paramCount >= 1) {
      if (IterUtil.isEmpty(tparams)) { 
        return new MultiVarargChecker(params, args, tparams, EMPTY_TYPE_ITERABLE);
      }
      else if (IterUtil.isEmpty(targs)) { return new MultiVarargInferenceChecker(params, args, tparams); }
      else if (IterUtil.sizeOf(tparams) == IterUtil.sizeOf(targs) && inBounds(tparams, targs)) {
        return new MultiVarargChecker(substitute(params, tparams, targs), args, tparams, targs);
      }
      else { return NullChecker.INSTANCE; }
    }
    else { return NullChecker.INSTANCE; }
  }
  
  /**
   * Given a list of signatures, find those that match the arguments.  The process of creating
   * SignatureCheckers and result values is deferred to two lambdas provided as arguments.
   * The sequence of matching first on explicit signatures, then boxing-compatible signatures, and 
   * finally vararg-compatible signatures is handled by this method.
   * 
   * @param T  The signature type ({@code DJMethod}, for example)
   * @param R  The result type ({@code MethodInvocation}, for example)
   * @param sigs  A list of signatures
   * @param makeChecker  Factory to create a new SignatureChecker from a signature
   * @param makeResult  Factory to create a result value from a signature and its matching
   *                    SignatureChecker
   */
  private <T, R> Iterable<R> findSignatureMatches(Iterable<? extends T> sigs, 
                                                  Lambda<? super T, ? extends SignatureChecker> makeChecker,
                                                  Lambda2<? super T, ? super SignatureChecker, 
                                                  ? extends R> makeResult) {
    Iterable<? extends SignatureChecker> checkers = IterUtil.mapSnapshot(sigs, makeChecker);
    Iterable<Pair<T, SignatureChecker>> pairs = IterUtil.zip(sigs, checkers);
    Iterable<Pair<T, SignatureChecker>> resultPairs = IterUtil.empty();
    for (Pair<T, SignatureChecker> pair : pairs) {
      if (pair.second().matches()) { resultPairs = IterUtil.compose(resultPairs, pair); }
    }
    if (IterUtil.isEmpty(resultPairs)) {
      for (Pair<T, SignatureChecker> pair : pairs) {
        if (pair.second().matchesWithBoxing()) { resultPairs = IterUtil.compose(resultPairs, pair); }
      }
    }
    if (IterUtil.isEmpty(resultPairs)) {
      for (Pair<T, SignatureChecker> pair : pairs) {
        if (pair.second().matchesWithVarargs()) { resultPairs = IterUtil.compose(resultPairs, pair); }
      }
    }
    resultPairs = mostSpecificSignatures(resultPairs);
    return IterUtil.map(IterUtil.pairFirsts(resultPairs), IterUtil.pairSeconds(resultPairs),
                        makeResult);
  }
  
  private static final Lambda<Expression, Type> TYPE_OF_EXPRESSION = new Lambda<Expression, Type>() {
    public Type value(Expression e) { return NodeProperties.getType(e); }
  };
  
  private static final Lambda<Type, Expression> EMPTY_EXPRESSION_FOR_TYPE = new Lambda<Type, Expression>() {
    public Expression value(Type t) {
      Expression result = TypeUtil.makeEmptyExpression();
      NodeProperties.setType(result, t);
      return result;
    }
  };
  
  /** Compute the most specific signatures in the list. */
  private <T extends Pair<?, SignatureChecker>> Iterable<T> mostSpecificSignatures(Iterable<T> allSigs) {
//    System.out.println("All matching signatures: " + allSigs);
    Iterable<T> result = IterUtil.empty();
    for (T sig : allSigs) {
      boolean keep = true;
      for (T sig2 : allSigs) {
        keep &= (sig == sig2) || isMoreSpecific(sig.second(), sig2.second());
        if (!keep) { break; }
      }
      if (keep) { result = IterUtil.compose(result, sig); }
    }
//    System.out.println("Most specific signatures: " + result);
    if (IterUtil.isEmpty(result)) { return allSigs; }
    else { return result; }
  }
  
  /**
   * True iff sig1's parameters could be used to invoke sig2.  This relation is defined in 
   * JLS 15.12.2.5.
   */
  private boolean isMoreSpecific(SignatureChecker sig1, SignatureChecker sig2) {
    SignatureChecker c = makeChecker(sig2.typeParameters(), EMPTY_TYPE_ITERABLE, sig2.parameters(), 
                                     IterUtil.mapSnapshot(sig1.parameters(), EMPTY_EXPRESSION_FOR_TYPE));
    return c.matches();
  }
  
  /**
   * Lookup the constructor corresponding the the given invocation.
   * @param t  The type of the object to be constructed.
   * @param typeArgs  The type arguments for the constructor's type parameters.
   * @param args  A list of typed expressions corresponding to the constructor's parameters.
   * @return  A {@link ConstructorInvocation} object representing the matched constructor.
   * @throws InvalidTargetException  If the type {@code t} cannot be constructed.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 constructor matches the given arguments and type 
   *                                   arguments.
   */
  // TODO: Must produce a reasonable value when looking up a constructor in an interface (for anonymous classes)
  public ConstructorInvocation lookupConstructor(final Type t, final Iterable<? extends Type> typeArgs, 
                                                 final Iterable<? extends Expression> args)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
//    System.out.println("\nLooking up constructor in type " + userRepresentation(t) +
//                       " with typeArgs (" + userRepresentation(typeArgs) + ") and args (" +
//                       userRepresentation(IterUtil.map(args, TYPE_OF_EXPRESSION)) + ")");
    Iterable<ConstructorInvocation> results = 
      t.apply(new TypeAbstractVisitor<Iterable<ConstructorInvocation>>() {
      
      public Iterable<ConstructorInvocation> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<ConstructorInvocation> forSimpleClassType(SimpleClassType t) {
        Iterable<DJConstructor> allConstructors = t.ofClass().declaredConstructors();
//        System.out.println("All constructors in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(allConstructors));
        Lambda<DJConstructor, SignatureChecker> makeChecker = 
          new Lambda<DJConstructor, SignatureChecker>() {
          public SignatureChecker value(DJConstructor k) {
            return makeChecker(k.declaredTypeParameters(), typeArgs, SymbolUtil.declaredParameterTypes(k), args);
          }
        };
        Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation> makeResult = 
          new Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation>() {
          public ConstructorInvocation value(DJConstructor k, SignatureChecker checker) {
            SubstitutionMap sigma = new SubstitutionMap(checker.typeParameters(),
                                                        checker.typeArguments());
            // TODO: Handle the thrown types
            return new ConstructorInvocation(k, checker.typeArguments(), checker.arguments(), 
                                             k.thrownTypes());
          }
        };
        return findSignatureMatches(allConstructors, makeChecker, makeResult);
      }
      
      @Override public Iterable<ConstructorInvocation> forRawClassType(RawClassType t) {
        // TODO: Handle raw member access warnings; make sure this is correct
        Iterable<DJConstructor> allConstructors = t.ofClass().declaredConstructors();
//        System.out.println("All constructors in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(allConstructors));
        Lambda<DJConstructor, SignatureChecker> makeChecker = 
          new Lambda<DJConstructor, SignatureChecker>() {
          public SignatureChecker value(DJConstructor k) {
            return makeChecker(IterUtil.<VariableType>empty(), typeArgs, 
                               IterUtil.map(SymbolUtil.declaredParameterTypes(k), ERASE_LAMBDA), args);
          }
        };
        Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation> makeResult = 
          new Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation>() {
          public ConstructorInvocation value(DJConstructor k, SignatureChecker checker) {
            // TODO: Handle the thrown types
            return new ConstructorInvocation(k, checker.typeArguments(), checker.arguments(), 
                                             k.thrownTypes());
          }
        };
        return findSignatureMatches(allConstructors, makeChecker, makeResult);
      }
      
      @Override public Iterable<ConstructorInvocation> forParameterizedClassType(ParameterizedClassType t) {
        final SubstitutionMap classSigma = 
          new SubstitutionMap(t.ofClass().declaredTypeParameters(), t.typeArguments());
        Iterable<DJConstructor> allConstructors = t.ofClass().declaredConstructors();
//        System.out.println("All constructors in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(allConstructors));
        Lambda<DJConstructor, SignatureChecker> makeChecker = 
          new Lambda<DJConstructor, SignatureChecker>() {
          public SignatureChecker value(DJConstructor k) {
            // TODO: substitute out class type parameters from the method's parameters' bounds
            //       (how does the JLS handle this?)
            return makeChecker(k.declaredTypeParameters(), typeArgs, 
                               substitute(SymbolUtil.declaredParameterTypes(k), classSigma), args);
          }
        };
        Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation> makeResult = 
          new Lambda2<DJConstructor, SignatureChecker, ConstructorInvocation>() {
          public ConstructorInvocation value(DJConstructor k, SignatureChecker checker) {
            // TODO: Handle the thrown types
            return new ConstructorInvocation(k, checker.typeArguments(), checker.arguments(), 
                                             k.thrownTypes());
          }
        };
        return findSignatureMatches(allConstructors, makeChecker, makeResult);
      }
    });
    
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(results);
    if (matches != 1) { throw new UnmatchedLookupException(matches); }
    else { return IterUtil.first(results); }
  }
  
  public boolean containsMethod(Type t, String name) {
    return containsMethod(t, name, false);
  }
  
  public boolean containsStaticMethod(Type t, String name) {
    return containsMethod(t, name, true);
  }
  
  private boolean containsMethod(Type t, final String name, final boolean requireStatic) {
//    System.out.println("Testing if method " + name + " exists in type " + userRepresentation(t));
    class LookupMethod extends TypeAbstractVisitor<Iterable<Object>> {
      
      private boolean _includePrivate;
      
      public LookupMethod(boolean includePrivate) {
        _includePrivate = includePrivate;
      }
      
      private boolean validMethod(DJMethod m) { 
        return
          (_includePrivate || !m.accessibility().equals(Access.PRIVATE)) &&
          (!requireStatic || m.isStatic());
      }
      
      public Iterable<Object> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<Object> forClassType(ClassType t) {
        for (DJMethod m : t.ofClass().declaredMethods()) {
          if (m.declaredName().equals(name) && validMethod(m)) {
            return IterUtil.singleton(null);
          }
        }
        return IterUtil.empty();
      }
    }
    Iterable<? extends Object> results = lookupMember(t, new LookupMethod(true), new LookupMethod(false));
    return !IterUtil.isEmpty(results);
  }
  
  /**
   * Lookup the method corresponding the the given invocation.
   * @param object  A typed expression representing the object whose method is to be invoked.
   * @param name  The name of the method.
   * @param typeArgs  The type arguments for the method's type parameters.
   * @param args  A list of typed expressions corresponding to the method's parameters.
   * @return  An {@link ObjectMethodInvocation} object representing the matched method.
   * @throws InvalidTargetException  If {@code object} cannot be used to invoke a method.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 method matches the given name, arguments, and type 
   *                                   arguments.
   */
  public ObjectMethodInvocation lookupMethod(final Expression object, final String name, 
                                             final Iterable<? extends Type> typeArgs, 
                                             final Iterable<? extends Expression> args)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
//    System.out.println("\nLooking up method " + name + " in type " + userRepresentation(NodeProperties.getType(object)) +
//                       " with typeArgs (" + userRepresentation(typeArgs) + ") and args (" +
//                       userRepresentation(IterUtil.map(args, TYPE_OF_EXPRESSION)) + ")");
    class LookupMethod extends TypeAbstractVisitor<Iterable<ObjectMethodInvocation>> {
      
      private Predicate<? super DJMethod> _matchMethod;
      
      public LookupMethod(final boolean includePrivate) {
        _matchMethod = new Predicate<DJMethod>() {
          public Boolean value(DJMethod m) {
            if (m.declaredName().equals(name)) {
              return includePrivate || !m.accessibility().equals(Access.PRIVATE);
            }
            else { return false; }
          }
        };
      }
      
      public Iterable<ObjectMethodInvocation> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<ObjectMethodInvocation> forSimpleClassType(final SimpleClassType t) {
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            return makeChecker(m.declaredTypeParameters(), typeArgs, SymbolUtil.declaredParameterTypes(m), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation>() {
          public ObjectMethodInvocation value(DJMethod m, SignatureChecker checker) {
            SubstitutionMap sigma = new SubstitutionMap(checker.typeParameters(), checker.typeArguments());
            Type returned = substitute(m.returnType(), sigma);
            // TODO: Handle the thrown types
            return new ObjectMethodInvocation(m, returned, makeCast(t, object), checker.typeArguments(), 
                                              checker.arguments(), m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
      @Override public Iterable<ObjectMethodInvocation> forRawClassType(final RawClassType t) {
        // TODO: Handle raw member access warnings; make sure this is correct
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            return makeChecker(IterUtil.<VariableType>empty(), typeArgs, 
                               IterUtil.map(SymbolUtil.declaredParameterTypes(m), ERASE_LAMBDA), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation>() {
          public ObjectMethodInvocation value(DJMethod m, SignatureChecker checker) {
            Type returned = erase(m.returnType());
            // TODO: Handle the thrown types
            return new ObjectMethodInvocation(m, returned, makeCast(t, object), checker.typeArguments(), 
                                              checker.arguments(), m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
      @Override public Iterable<ObjectMethodInvocation> forParameterizedClassType(final ParameterizedClassType t) {
        final SubstitutionMap classSigma =
          new SubstitutionMap(SymbolUtil.allTypeParameters(t.ofClass()), t.typeArguments());
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            // TODO: substitute out class type parameters from the method's parameters' bounds
            //       (how does the JLS handle this?)
            return makeChecker(m.declaredTypeParameters(), typeArgs, 
                               substitute(SymbolUtil.declaredParameterTypes(m), classSigma), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, ObjectMethodInvocation>() {
          public ObjectMethodInvocation value(DJMethod m, SignatureChecker checker) {
            SubstitutionMap sigma = new SubstitutionMap(checker.typeParameters(), checker.typeArguments());
            Type rawReturned = m.returnType();
            Type returned = substitute(substitute(rawReturned, classSigma), sigma);
            // TODO: Handle the thrown types
            return new ObjectMethodInvocation(m, returned, makeCast(t, object), checker.typeArguments(), 
                                              checker.arguments(), m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
    }
    Iterable<? extends ObjectMethodInvocation> results = lookupMember(NodeProperties.getType(object), 
                                                                      new LookupMethod(true), 
                                                                      new LookupMethod(false));
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(results);
    if (matches != 1) { throw new UnmatchedLookupException(matches); }
    else { return IterUtil.first(results); }
  }
  
  /**
   * Lookup the static method corresponding the the given invocation.
   * @param t  The type in which to search for a static method.
   * @param name  The name of the method.
   * @param typeArgs  The type arguments for the method's type parameters.
   * @param args  A list of typed expressions corresponding to the method's parameters.
   * @return  A {@link StaticMethodInvocation} object representing the matched method.
   * @throws InvalidTargetException  If method invocation is not legal for the type {@code t}.
   * @throws InvalidTypeArgumentException  If the type arguments are invalid (for example, a primitive type).
   * @throws UnmatchedLookupException  If 0 or more than 1 method matches the given name, arguments, and type 
   *                                   arguments.
   */
  public StaticMethodInvocation lookupStaticMethod(Type t, final String name, 
                                                   final Iterable<? extends Type> typeArgs, 
                                                   final Iterable<? extends Expression> args)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
//    System.out.println("\nLooking up static method " + name + " in type " + userRepresentation(t) +
//                       " with typeArgs (" + userRepresentation(typeArgs) + ") and args (" +
//                       userRepresentation(IterUtil.map(args, TYPE_OF_EXPRESSION)) + ")");
    class LookupMethod extends TypeAbstractVisitor<Iterable<StaticMethodInvocation>> {
      
      private Predicate<? super DJMethod> _matchMethod;
      
      public LookupMethod(final boolean includePrivate) {
        _matchMethod = new Predicate<DJMethod>() {
          public Boolean value(DJMethod m) {
            if (m.declaredName().equals(name)) {
              if (includePrivate) { return m.isStatic(); }
              else { return m.isStatic() && !m.accessibility().equals(Access.PRIVATE); }
            }
            else { return false; }
          }
        };
      }
      
      public Iterable<StaticMethodInvocation> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<StaticMethodInvocation> forSimpleClassType(SimpleClassType t) {
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            return makeChecker(m.declaredTypeParameters(), typeArgs, SymbolUtil.declaredParameterTypes(m), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation>() {
          public StaticMethodInvocation value(DJMethod m, SignatureChecker checker) {
            SubstitutionMap sigma = new SubstitutionMap(checker.typeParameters(), checker.typeArguments());
            Type returned = substitute(m.returnType(), sigma);
            // TODO: Handle the thrown types
            return new StaticMethodInvocation(m, returned, checker.typeArguments(), checker.arguments(),
                                              m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
      @Override public Iterable<StaticMethodInvocation> forRawClassType(RawClassType t) {
        // TODO: Handle raw member access warnings; make sure this is correct
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            return makeChecker(IterUtil.<VariableType>empty(), typeArgs, 
                               IterUtil.map(SymbolUtil.declaredParameterTypes(m), ERASE_LAMBDA), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation>() {
          public StaticMethodInvocation value(DJMethod m, SignatureChecker checker) {
            Type returned = erase(m.returnType());
            // TODO: Handle the thrown types
            return new StaticMethodInvocation(m, returned, checker.typeArguments(), checker.arguments(),
                                              m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
      @Override public Iterable<StaticMethodInvocation> forParameterizedClassType(ParameterizedClassType t) {
        final SubstitutionMap classSigma = 
          new SubstitutionMap(SymbolUtil.allTypeParameters(t.ofClass()), t.typeArguments());
        Iterable<DJMethod> methods = IterUtil.filter(t.ofClass().declaredMethods(), _matchMethod);
//        System.out.println("Matching methods in type " + userRepresentation(t) + ": " +
//                           IterUtil.multilineToString(matchingMethods));
        Lambda<DJMethod, SignatureChecker> makeChecker = new Lambda<DJMethod, SignatureChecker>() {
          public SignatureChecker value(DJMethod m) {
            // TODO: substitute out class type parameters from the method's parameters' bounds
            //       (how does the JLS handle this?)
            return makeChecker(m.declaredTypeParameters(), typeArgs, 
                               substitute(SymbolUtil.declaredParameterTypes(m), classSigma), args);
          }
        };
        Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation> makeResult = 
          new Lambda2<DJMethod, SignatureChecker, StaticMethodInvocation>() {
          public StaticMethodInvocation value(DJMethod m, SignatureChecker checker) {
            SubstitutionMap sigma = new SubstitutionMap(checker.typeParameters(), 
                                                        checker.typeArguments());
            Type rawReturned = m.returnType();
            Type returned = substitute(substitute(rawReturned, classSigma), sigma);
            // TODO: Handle the thrown types
            return new StaticMethodInvocation(m, returned, checker.typeArguments(), 
                                              checker.arguments(), m.thrownTypes());
          }
        };
        return findSignatureMatches(methods, makeChecker, makeResult);
      }
      
    }
    Iterable<? extends StaticMethodInvocation> results = lookupMember(t, new LookupMethod(true), 
                                                                      new LookupMethod(false));
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(results);
    if (matches != 1) { throw new UnmatchedLookupException(matches); }
    else { return IterUtil.first(results); }
  }
  
  
  public boolean containsField(Type t, String name) {
    return containsField(t, name, false);
  }
  
  public boolean containsStaticField(Type t, String name) {
    return containsField(t, name, true);
  }
  
  private boolean containsField(Type t, final String name, final boolean requireStatic) {
//    System.out.println("Testing if field " + name + " exists in type " + userRepresentation(t));
    class LookupField extends TypeAbstractVisitor<Iterable<Object>> {
      
      private boolean _includePrivate;
      
      public LookupField(boolean includePrivate) {
        _includePrivate = includePrivate;
      }
      
      private boolean validField(DJField f) { 
        return
          (_includePrivate || !f.accessibility().equals(Access.PRIVATE)) &&
          (!requireStatic || f.isStatic());
      }
      
      public Iterable<Object> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<Object> forClassType(ClassType t) {
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            return IterUtil.singleton(null);
          }
        }
        return IterUtil.empty();
      }
      
    }
    Iterable<? extends Object> results = lookupMember(t, new LookupField(true), new LookupField(false));
    return !IterUtil.isEmpty(results);
  }

  /**
   * Lookup the field with the given name in the given object.
   * @param object  A typed expression representing the object whose field is to be accessed.
   * @param name  The name of the field.
   * @return An {@link ObjectFieldReference} object representing the matched field.
   * @throws InvalidTargetException  If {@code object} cannot be used to access a field.
   * @throws UnmatchedLookupException  If 0 or more than 1 field matches the given name.
   */
  public ObjectFieldReference lookupField(final Expression object, final String name)
    throws InvalidTargetException, UnmatchedLookupException {
//    System.out.println("\nLooking up field " + name + " in type " + 
//                       userRepresentation(NodeProperties.getType(object)));
    class LookupField extends TypeAbstractVisitor<Iterable<ObjectFieldReference>> {
      
      private boolean _includePrivate;
      
      public LookupField(boolean includePrivate) {
        _includePrivate = includePrivate;
      }
      
      private boolean validField(DJField f) { 
        return _includePrivate || !f.accessibility().equals(Access.PRIVATE);
      }
      
      public Iterable<ObjectFieldReference> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<ObjectFieldReference> forSimpleClassType(SimpleClassType t) {
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            return IterUtil.make(new ObjectFieldReference(f, f.type(), makeCast(t, object)));
          }
        }
        return IterUtil.empty();
      }
      
      @Override public Iterable<ObjectFieldReference> forRawClassType(RawClassType t) {
        // TODO: Handle raw member access warnings
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            return IterUtil.make(new ObjectFieldReference(f, erase(f.type()), makeCast(t, object)));
          }
        }
        return IterUtil.empty();
      }
      
      @Override public Iterable<ObjectFieldReference> forParameterizedClassType(ParameterizedClassType t) {
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            Type fieldType = substitute(f.type(), SymbolUtil.allTypeParameters(t.ofClass()), 
                                        t.typeArguments());
            return IterUtil.make(new ObjectFieldReference(f, fieldType, makeCast(t, object)));
          }
        }
        return IterUtil.empty();
      }
      
    }
    Iterable<? extends ObjectFieldReference> results = lookupMember(NodeProperties.getType(object), 
                                                                    new LookupField(true), 
                                                                    new LookupField(false));
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(results);
    if (matches != 1) { throw new UnmatchedLookupException(matches); }
    else { return IterUtil.first(results); }
  }
  
  
  /**
   * Lookup the static field with the given name.
   * @param t  The type in which to search for a static field.
   * @param name  The name of the field.
   * @return A {@link StaticFieldReference} object representing the matched field.
   * @throws InvalidTargetException  If field access is not legal for the type {@code t}.
   * @throws UnmatchedLookupException  If 0 or more than 1 field matches the given name.
   */
  public StaticFieldReference lookupStaticField(Type t, final String name)
    throws InvalidTargetException, UnmatchedLookupException {
//    System.out.println("\nLooking up static field " + name + " in type " + userRepresentation(t));
    class LookupField extends TypeAbstractVisitor<Iterable<StaticFieldReference>> {
      
      private boolean _includePrivate;
      
      public LookupField(boolean includePrivate) {
        _includePrivate = includePrivate;
      }
      
      private boolean validField(DJField f) {
        if (_includePrivate) { return f.isStatic(); }
        else { return f.isStatic() && !f.accessibility().equals(Access.PRIVATE); }
      }
      
      public Iterable<StaticFieldReference> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<StaticFieldReference> forSimpleClassType(SimpleClassType t) {
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            return IterUtil.make(new StaticFieldReference(f, f.type()));
          }
        }
        return IterUtil.empty();
      }
      
      @Override public Iterable<StaticFieldReference> forRawClassType(RawClassType t) {
        // TODO: Handle raw member access warnings
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            return IterUtil.make(new StaticFieldReference(f, erase(f.type())));
          }
        }
        return IterUtil.empty();
      }
      
      @Override public Iterable<StaticFieldReference> forParameterizedClassType(ParameterizedClassType t) {
        for (DJField f : t.ofClass().declaredFields()) {
          if (f.declaredName().equals(name) && validField(f)) {
            Type fieldType = substitute(f.type(), SymbolUtil.allTypeParameters(t.ofClass()), 
                                        t.typeArguments());
            return IterUtil.make(new StaticFieldReference(f, fieldType));
          }
        }
        return IterUtil.empty();
      }
      
    }
    Iterable<? extends StaticFieldReference> results = lookupMember(t, new LookupField(true), 
                                                                    new LookupField(false));
    // TODO: provide more error-message information
    int matches = IterUtil.sizeOf(results);
    if (matches != 1) { throw new UnmatchedLookupException(matches); }
    else { return IterUtil.first(results); }
  }
  
  
  public boolean containsClass(Type t, final String name) {
//    System.out.println("Testing if class " + name + " exists in type " + userRepresentation(t));
    
    // TODO: We allow nonstatic classes and ambiguous references here.  Is that correct?
    Lambda<Boolean, Predicate<DJClass>> makePred = new Lambda<Boolean, Predicate<DJClass>>() {
      public Predicate<DJClass> value(final Boolean includePrivate) {
        return new Predicate<DJClass>() {
          public Boolean value(DJClass c) {
            if (c.declaredName().equals(name)) {
              return includePrivate || !c.accessibility().equals(Access.PRIVATE);
            }
            else { return false; }
          }
        };
      }
    };
    Iterable<? extends ClassType> classes = lookupClasses(t, makePred, EMPTY_TYPE_ITERABLE);
    return !IterUtil.isEmpty(classes);
  }
  
  public boolean containsStaticClass(Type t, final String name) {
    Lambda<Boolean, Predicate<DJClass>> makePred = new Lambda<Boolean, Predicate<DJClass>>() {
      public Predicate<DJClass> value(final Boolean includePrivate) {
        return new Predicate<DJClass>() {
          public Boolean value(DJClass c) {
            if (c.declaredName().equals(name)) {
              if (includePrivate) { return c.isStatic(); }
              else { return c.isStatic() && !c.accessibility().equals(Access.PRIVATE); }
            }
            else { return false; }
          }
        };
      }
    };
    Iterable<? extends ClassType> classes = lookupClasses(t, makePred, EMPTY_TYPE_ITERABLE);
    return !IterUtil.isEmpty(classes);
  }
  
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
  public ClassType lookupClass(Expression object, String name, Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
    return lookupClass(NodeProperties.getType(object), name, typeArgs);
  }
  
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
  public ClassType lookupClass(Type t, final String name, Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
    debug.logStart(new String[]{"t", "name", "typeArgs"}, t, name, typeArgs);
    try {
      Lambda<Boolean, Predicate<DJClass>> makePred = new Lambda<Boolean, Predicate<DJClass>>() {
        public Predicate<DJClass> value(final Boolean includePrivate) {
          return new Predicate<DJClass>() {
            public Boolean value(DJClass c) {
              if (c.declaredName().equals(name)) {
                return includePrivate || !c.accessibility().equals(Access.PRIVATE);
              }
              else { return false; }
            }
          };
        }
      };
      return lookupClass(t, makePred, typeArgs, name);
    }
    finally { debug.logEnd(); }
  }
  
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
  public ClassType lookupStaticClass(Type t, final String name, final Iterable<? extends Type> typeArgs)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
    debug.logStart(new String[]{"t", "name", "typeArgs"}, t, name, typeArgs);
    try {
      Lambda<Boolean, Predicate<DJClass>> makePred = new Lambda<Boolean, Predicate<DJClass>>() {
        public Predicate<DJClass> value(final Boolean includePrivate) {
          return new Predicate<DJClass>() {
            public Boolean value(DJClass c) {
              if (c.declaredName().equals(name)) {
                if (includePrivate) { return c.isStatic(); }
                else { return c.isStatic() && !c.accessibility().equals(Access.PRIVATE); }
              }
              else { return false; }
            }
          };
        }
      };
      return lookupClass(t, makePred, typeArgs, name);
    }
    finally { debug.logEnd(); }
  }
  
  /** Look up an inner class based on the given predicate. */
  private ClassType lookupClass(Type t, Lambda<? super Boolean, ? extends Predicate<? super DJClass>> makePred,
                                Iterable<? extends Type> typeArgs, String name)
    throws InvalidTargetException, InvalidTypeArgumentException, UnmatchedLookupException {
    Iterable<? extends ClassType> results = lookupClasses(t, makePred, typeArgs);
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
            return t; // TODO: Handle parameterized raw members (such as Foo.Bar<T> vs. Foo<X>.Bar<T>)
          }
          
          @Override public ClassType forParameterizedClassType(ParameterizedClassType t) {
            try {
              if (IterUtil.sizeOf(params) != IterUtil.sizeOf(t.typeArguments())) {
                throw new InvalidTypeArgumentException();
              }
              checkBounds(params, capture(t).typeArguments());
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
  }
  
  /**
   * Produces a list of all inner classes matching the given predicate in type {@code t}.  No
   * errors are thrown.  The given type arguments are applied to the result, but no checks are
   * made for their correctness.
   */
  private Iterable<? extends ClassType> 
    lookupClasses(Type t, Lambda<? super Boolean, ? extends Predicate<? super DJClass>> makePred,
                  final Iterable<? extends Type> typeArgs) {
    /** Produces a type for the inner class; note that no checks are made on the type arguments */
    class LookupClass extends TypeAbstractVisitor<Iterable<ClassType>> {
      
      private final Predicate<? super DJClass> _matchInner;
      
      public LookupClass(Predicate<? super DJClass> matchInner) {
        _matchInner = matchInner;
      }
      
      public Iterable<ClassType> defaultCase(Type t) { return IterUtil.empty(); }
      
      @Override public Iterable<ClassType> forSimpleClassType(SimpleClassType t) {
        Lambda<DJClass, ClassType> makeType;
        if (IterUtil.isEmpty(typeArgs)) {
          makeType = new Lambda<DJClass, ClassType>() {
            public ClassType value(DJClass c) { return new SimpleClassType(c); }
          };
        }
        else {
          makeType = new Lambda<DJClass, ClassType>() {
            public ClassType value(DJClass c) { return new ParameterizedClassType(c, typeArgs); }
          };
        }
        return IterUtil.mapSnapshot(IterUtil.filter(t.ofClass().declaredClasses(), _matchInner), makeType);
      }
      
      @Override public Iterable<ClassType> forRawClassType(RawClassType t) {
        // TODO: Handle raw member access warnings
        Lambda<DJClass, ClassType> makeType = new Lambda<DJClass, ClassType>() {
          public ClassType value(DJClass c) { return new RawClassType(c); }
        };
        return IterUtil.mapSnapshot(IterUtil.filter(t.ofClass().declaredClasses(), _matchInner), makeType);
      }
      
      @Override public Iterable<ClassType> forParameterizedClassType(final ParameterizedClassType t) {
        Lambda<DJClass, ClassType> makeType = new Lambda<DJClass, ClassType>() {
          public ClassType value(DJClass c) {
            return new ParameterizedClassType(c, IterUtil.compose(t.typeArguments(), typeArgs));
          }
        };
        return IterUtil.mapSnapshot(IterUtil.filter(t.ofClass().declaredClasses(), _matchInner), makeType);
      }
      
    }
    return lookupMember(t, new LookupClass(makePred.value(true)), new LookupClass(makePred.value(false)));
  }
  
  /**
   * Insure that the given arguments are within the bounds of the corresponding parameters.  Assumes
   * that {@code params} and {@code args} have matching arity.
   * 
   * @throw  InvalidTypeArgumentException  If some argument is not within its bound
   */
  private void checkBounds(Iterable<? extends VariableType> params, Iterable<? extends Type> args)
    throws InvalidTypeArgumentException {
    SubstitutionMap sigma = new SubstitutionMap(params, args);
    for (Pair<VariableType, Type> pair : IterUtil.zip(params, args)) {
      VariableType param = pair.first();
      Type arg = pair.second();
      if (!isSubtype(arg, substitute(param.symbol().upperBound(), sigma))) {
        throw new InvalidTypeArgumentException();
      }
      if (!isSubtype(substitute(param.symbol().lowerBound(), sigma), arg)) {
        throw new InvalidTypeArgumentException();
      }
    }
  }
  
  /**
   * Test whether the given arguments are within the bounds of the corresponding parameters.  Unlike 
   * {@link checkBounds}, no exception is thrown when a mismatch occurs.  Assumes that {@code params} 
   * and {@code args} have matching arity.
   * 
   * @return  {@code true} iff the given arguments are within the bounds of the given parameters
   */
  private boolean inBounds(Iterable<? extends VariableType> params, Iterable<? extends Type> args) {
    SubstitutionMap sigma = new SubstitutionMap(params, args);
    for (Pair<VariableType, Type> pair : IterUtil.zip(params, args)) {
      VariableType param = pair.first();
      Type arg = pair.second();
      if (!isSubtype(substitute(param.symbol().lowerBound(), sigma), arg)) { return false; }
      if (!isSubtype(arg, substitute(param.symbol().upperBound(), sigma))) { return false; }
    }
    return true;
  }
  
  /**
   * Provides the recursive framework for class member lookup.  {@code baseCase} and 
   * {@code recursiveBaseCase} are visitors that produce a list of members for the given type.
   * If the result of {@code t.apply(baseCase)} is nonempty, that result is returned.  Otherwise,
   * this method is recursively invoked on a class type's supertypes, an array type's interfaces
   * ({@code Serializable} and {@code Cloneable}), a variable's upper bound, or each of an 
   * intersection's members.
   */
  private <T> Iterable<? extends T> 
    lookupMember(Type t, TypeVisitor<? extends Iterable<? extends T>> baseCase, 
                 TypeVisitor<? extends Iterable<? extends T>> recursiveBaseCase) {
    return lookupMember(t, new LinkedList<Type>(), baseCase, recursiveBaseCase);
  }
  
  /**
   * Implements {@code lookupMember}, keeping track of types that have already been checked,
   * and returning an empty iterable in that case
   */
  private <T> Iterable<? extends T>
    lookupMember(Type t, final List<Type> alreadyChecked,
                 TypeVisitor<? extends Iterable<? extends T>> baseCase, 
                 final TypeVisitor<? extends Iterable<? extends T>> recursiveBaseCase) {
    for (Type checkedT : alreadyChecked) {
      // TODO: Improve the performance here (use a hash code consistent with isEqual)
      if (isEqual(t, checkedT)) { return IterUtil.empty(); }
    }
    
    final Iterable<? extends T> baseResult = t.apply(baseCase);
    alreadyChecked.add(t);
    if (!IterUtil.isEmpty(baseResult)) { return baseResult; }
    else {
      return t.apply(new TypeAbstractVisitor<Iterable<? extends T>>() {
        
        public Iterable<? extends T> defaultCase(Type t) { return baseResult; }
        
        @Override public Iterable<? extends T> forArrayType(ArrayType t) {
          return lookupMember(CLONEABLE_AND_SERIALIZABLE, alreadyChecked, recursiveBaseCase, recursiveBaseCase);
        }
        
        @Override public Iterable<? extends T> forSimpleClassType(SimpleClassType t) {
          Type superT = immediateSupertype(t);
          if (superT == null) { return baseResult; }
          else { return lookupMember(superT, alreadyChecked, recursiveBaseCase, recursiveBaseCase); }
        }
        
        @Override public Iterable<? extends T> forRawClassType(RawClassType t) {
          Type superT = immediateSupertype(t);
          if (superT == null) { return baseResult; }
          else { return lookupMember(superT, alreadyChecked, recursiveBaseCase, recursiveBaseCase); }
        }
        
        @Override public Iterable<? extends T> forParameterizedClassType(ParameterizedClassType t) {
          Type superT = immediateSupertype(t);
          if (superT == null) { return baseResult; }
          else { return lookupMember(superT, alreadyChecked, recursiveBaseCase, recursiveBaseCase); }
        }
        
        @Override public Iterable<? extends T> forVariableType(VariableType t) {
          return lookupMember(t.symbol().upperBound(), alreadyChecked, recursiveBaseCase, recursiveBaseCase);
        }
        
        @Override public Iterable<? extends T> forIntersectionType(IntersectionType t) {
          Iterable<? extends T> result = IterUtil.empty();
          for (Type tSup : t.ofTypes()) {
            Iterable<? extends T> forSup = lookupMember(tSup, alreadyChecked, recursiveBaseCase, 
                                                        recursiveBaseCase);
            result = IterUtil.compose(result, forSup);
          }
          return result;
        }
        
      });
    }
  }
  
}
