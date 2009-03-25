package edu.rice.cs.dynamicjava.symbol;

import java.util.*;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Triple;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.recur.*;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.PermutationIterable;
import edu.rice.cs.plt.collect.CollectUtil;

import edu.rice.cs.dynamicjava.symbol.type.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ExtendedTypeSystem extends StandardTypeSystem {
  
  public static final ExtendedTypeSystem INSTANCE = new ExtendedTypeSystem();
  
  /** Determine if the type is well-formed. */
  public boolean isWellFormed(Type t) { return WELL_FORMED.contains(t); }
  
  private final Predicate<Type> WELL_FORMED = new Predicate<Type>() {
    public boolean contains(Type t) {
      return t.apply(new TypeAbstractVisitor<Boolean>() {
        @Override public Boolean defaultCase(Type t) { return true; }
        @Override public Boolean forArrayType(ArrayType t) { return t.ofType().apply(this); }
        @Override public Boolean forSimpleClassType(SimpleClassType t) {
          return IterUtil.isEmpty(SymbolUtil.allTypeParameters(t.ofClass()));
        }
        @Override public Boolean forRawClassType(RawClassType t) {
          return !IterUtil.isEmpty(SymbolUtil.allTypeParameters(t.ofClass()));
        }
        @Override public Boolean forParameterizedClassType(ParameterizedClassType t) {
          Iterable<? extends Type> args = t.typeArguments();
          if (IterUtil.and(args, WELL_FORMED)) {
            Iterable<VariableType> params = SymbolUtil.allTypeParameters(t.ofClass());
            if (IterUtil.sizeOf(params) == IterUtil.sizeOf(args)) {
              Iterable<Type> captArgs = captureTypeArgs(args, params);
              for (Pair<Type, Type> pair : IterUtil.zip(args, captArgs)) {
                if (pair.first() != pair.second() && !pair.second().apply(this)) { return false; }
              }
              return inBounds(params, captArgs);
            }
          }
          return false;
        }
        @Override public Boolean forVariableType(VariableType t) {
          Type lower = t.symbol().lowerBound();
          Type upper = t.symbol().upperBound();
          return lower.apply(this) && upper.apply(this) && isSubtype(lower, upper);
        }
        @Override public Boolean forBoundType(BoundType t) {
          return IterUtil.and(t.ofTypes(), WELL_FORMED);
        }
      });
    }
  };

  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public boolean isEqual(Type t1, Type t2) { return t1.equals(t2) || (isSubtype(t1, t2) && isSubtype(t2, t1)); }
  
  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link #isEqual}), transitive relation.
   */
  public boolean isSubtype(Type subT, Type superT) {
    //debug.logStart(new String[]{"subT", "superT"}, subT, superT);
    boolean result = isSubtype(subT, superT, new RecursionStack2<Type, Type>());
    //debug.logEnd("result", result);
    return result;
  }
  
  private boolean isSubtype(final Type subT, final Type superT, final RecursionStack2<Type, Type> stack) {
    //debug.logStart(new String[]{"subT", "superT"}, subT, superT); try {
            
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
            public boolean contains(Type t) { return isSubtype(subT, t, stack); }
          });
        }
      }
      
      @Override public Boolean forUnionType(final UnionType superT) {
        return subT.apply(new TypeAbstractVisitor<Boolean>() {
          @Override public Boolean defaultCase(Type t) {
            return IterUtil.or(superT.ofTypes(), new Predicate<Type>() {
              public boolean contains(Type t) { return isSubtype(subT, t, stack); }
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
            if (isPrimitive(subT.ofType())) {
              // types may be inequal if one is vararg and the other is not
              return subT.ofType().equals(superT.ofType());
            }
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
          public boolean contains(Type t) { return isSubtype(t, superT, stack); }
        });
      }
      
      public Boolean forUnionType(UnionType subT) {
        return IterUtil.and(subT.ofTypes(), new Predicate<Type>() {
          public boolean contains(Type t) { return isSubtype(t, superT, stack); }
        });
      }
      
      public Boolean forBottomType(BottomType subT) { return true; }
    });
    //} finally { debug.logEnd(); }
  }
  
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
  
  protected Iterable<Type> captureTypeArgs(Iterable<? extends Type> targs,
                                           Iterable<? extends VariableType> params) {
    List<BoundedSymbol> captureVars = new LinkedList<BoundedSymbol>();
    List<Type> newArgs = new LinkedList<Type>();
    for (Type arg : targs) {
      if (arg instanceof Wildcard) {
        BoundedSymbol s = new BoundedSymbol(new Object());
        captureVars.add(s);
        newArgs.add(new VariableType(s));
      }
      else { captureVars.add(null); newArgs.add(arg); }
    }
    
    final SubstitutionMap sigma = new SubstitutionMap(params, newArgs);
    for (Triple<BoundedSymbol, Type, VariableType> triple : IterUtil.zip(captureVars, targs, params)) {
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
    return newArgs;
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
   * ({@code args} and {@code params} are assumed to have the same length), and {@code returned} will
   * be returned where {@code expected} is expected.
   * 
   * @return  A set of inferred type arguments for {@code tparams}, or {@code null} if the parameters
   *          are over-constrained
   */
  protected Iterable<Type> inferTypeArguments(Iterable<? extends VariableType> tparams, 
                                            Iterable<? extends Type> params, Type returned,
                                            Iterable<? extends Type> args, Option<Type> expected) {
    //debug.logValues("Beginning inferTypeArguments",
    //                new String[]{ "tparams", "params", "returned", "args", "expected" },
    //                wrap(tparams), wrap(params), wrap(returned), wrap(args), wrap(expected));
    RecursionStack3<Type, Type, InferenceMode> stack = RecursionStack3.make();
    Set<? extends VariableType> tparamSet = CollectUtil.makeSet(tparams);
    
    ConstraintSet constraintsBuilder = EMPTY_CONSTRAINTS;
    for (Pair<Type, Type> pair : IterUtil.zip(args, params)) {
      constraintsBuilder = constraintsBuilder.and(inferFromSubtype(pair.first(), pair.second(), 
                                                                   tparamSet, stack));
      if (!constraintsBuilder.isSatisfiable()) { break; }
    }
    if (expected.isSome() && constraintsBuilder.isSatisfiable()) {
      constraintsBuilder = constraintsBuilder.and(inferFromSupertype(expected.unwrap(), returned,
                                                                     tparamSet, stack));
    }
    
    final ConstraintSet constraints = constraintsBuilder; // constraints must be redeclared as final
    //debug.logValue("constraints", constraints);
//    System.out.println("Final inference result: " + constraints);
    if (!constraints.isSatisfiable()) { return null; }

    Iterable<Type> result = IterUtil.mapSnapshot(tparams, new Lambda<VariableType, Type>() {
      public Type value(VariableType param) { return constraints.lowerBound(param); }
    });
    if (inBounds(tparams, result)) { return result; }
    
    List<Wildcard> constraintWs = new LinkedList<Wildcard>();
    for (VariableType param : tparams) {
      BoundedSymbol s = new BoundedSymbol(new Object(), constraints.upperBound(param), constraints.lowerBound(param));
      constraintWs.add(new Wildcard(s));
    }
    result = captureTypeArgs(constraintWs, tparams);
    if (IterUtil.and(result, WELL_FORMED)) { return result; }

    return null;
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
              if (paramSuper == null) { return UNSATISFIABLE_CONSTRAINTS; }
              else { return inferFromSupertype(arg, paramSuper, vars, stack); }
            }
            
            @Override public ConstraintSet forRawClassType(RawClassType arg) {
              Type paramSuper = immediateSupertype(param);
              if (paramSuper == null) { return UNSATISFIABLE_CONSTRAINTS; }
              else { return inferFromSupertype(arg, paramSuper, vars, stack); }
            }
            
            @Override public ConstraintSet forParameterizedClassType(final ParameterizedClassType arg) {
              ConstraintSet matchConstraints = UNSATISFIABLE_CONSTRAINTS;
              if (param.ofClass().equals(arg.ofClass())) {
                Thunk<ConstraintSet> recurOnTargs = new Thunk<ConstraintSet>() {
                  public ConstraintSet value() {
                    ParameterizedClassType paramCap = capture(param);
                    ConstraintSet result = EMPTY_CONSTRAINTS;
                    for (Pair<Type, Type> pair : IterUtil.zip(arg.typeArguments(), paramCap.typeArguments())) {
                      Type argArg = pair.first();
                      final Type paramArg = pair.second();
                      result = result.and(argArg.apply(new TypeAbstractVisitor<ConstraintSet>() {
                        public ConstraintSet defaultCase(Type argArg) {
                          ConstraintSet nonWildS = inferFromEqual(argArg, paramArg, vars, stack);
                          return nonWildS;
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
  
}
