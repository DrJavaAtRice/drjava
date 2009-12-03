package edu.rice.cs.dynamicjava.symbol;

import java.util.*;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Triple;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Wrapper;
import edu.rice.cs.plt.recur.*;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.IndexedRelation;
import edu.rice.cs.plt.collect.Order;
import edu.rice.cs.plt.collect.PredicateSet;
import edu.rice.cs.plt.collect.Relation;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.type.*;

import static edu.rice.cs.plt.iter.IterUtil.mapSnapshot;
import static edu.rice.cs.plt.iter.IterUtil.sizeOf;
import static edu.rice.cs.plt.iter.IterUtil.zip;
import static edu.rice.cs.plt.iter.IterUtil.filter;
import static edu.rice.cs.plt.collect.CollectUtil.minList;
import static edu.rice.cs.plt.collect.CollectUtil.union;
import static edu.rice.cs.plt.collect.CollectUtil.asPredicateSet;
import static edu.rice.cs.plt.collect.CollectUtil.asLambdaMap;
import static edu.rice.cs.plt.lambda.LambdaUtil.bindFirst;
import static edu.rice.cs.plt.lambda.LambdaUtil.bindSecond;
import static edu.rice.cs.plt.lambda.LambdaUtil.asRunnable;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class JLSTypeSystem extends StandardTypeSystem {
  
  /**
   * Whether the inference algorithm and "join" should attempt to pack capture variables.
   * JLS does not specify this, but javac and Eclipse both do it.
   */
  private final boolean _packCaptureVars;
  /**
   * Whether the inference algorithm should use constraints inferred from arguments in all cases.
   * JLS is unclear, but the most straightforward interpretation is for this to be false.  javac uses true (?
   * I initially thought it used false, for some reason); Eclipse uses true. 
   */ 
  private final boolean _alwaysUseArgumentConstraints;
  /**
   * Whether the inference algorithm should only use declared parameter bounds as a last option (where
   * arguments and return type produce nothing).  JLS does not do this, but javac does.
   */
  private final boolean _waitToUseDeclaredBounds;

  public JLSTypeSystem(Options opt) { this(opt, true, true, true); }
  
  public JLSTypeSystem(Options opt, boolean packCaptureVars, boolean alwaysUseArgumentConstraints,
                        boolean waitToUseDeclaredBounds) {
    super(opt);
    _packCaptureVars = packCaptureVars;
    _alwaysUseArgumentConstraints = alwaysUseArgumentConstraints;
    _waitToUseDeclaredBounds = waitToUseDeclaredBounds;
  }
  
  /** Determine if the type is well-formed. */
  public boolean isWellFormed(Type t) { return t.apply(new WellFormedTester()); }
  
  private class WellFormedTester extends TypeAbstractVisitor<Boolean> implements Predicate<Type> {
    final RecursionStack<Type> _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
    Set<Wildcard> _visibleWildcards = new HashSet<Wildcard>();
    
    private Set<Wildcard> resetVisibleWildcards() {
      Set<Wildcard> result = _visibleWildcards;
      _visibleWildcards = new HashSet<Wildcard>();
      return result;
    }
    
    public boolean contains(Type t) { return t.apply(this); }
    
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
      boolean validArgs = true;
      for (Type arg : args) {
        validArgs &= arg.apply(new TypeAbstractVisitor<Boolean>() {
          @Override public Boolean defaultCase(Type t) {
            Set<Wildcard> old = resetVisibleWildcards();
            try { return t.apply(WellFormedTester.this); }
            finally { _visibleWildcards = old; }
          }
          @Override public Boolean forWildcard(Wildcard w) { return w.apply(WellFormedTester.this); }
        });
        if (!validArgs) { return false; }
      }
      // validArgs is true
      Iterable<VariableType> params = SymbolUtil.allTypeParameters(t.ofClass());
      if (sizeOf(params) == sizeOf(args)) {
        Iterable<Type> captArgs = captureTypeArgs(args, params);
        for (Pair<Type, Type> pair : zip(args, captArgs)) {
          if (pair.first() != pair.second()) {
            // must be a capture variable, so should be a VariableType
            // We can assume the bounds are well-formed  -- otherwise, the redundant well-formed check
            // may require a further capture, and never terminate (this implies the relevant class
            // declaration is assumed to be well-formed).
            // We can also assume that containsVar(lower, cvar) is false.
            VariableType cvar = (VariableType) pair.second();
            Type lower = cvar.symbol().lowerBound();
            Type upper = cvar.symbol().upperBound();
            if (!isSubtype(lower, upper)) { return false; }
          }
        }
        return inBounds(params, captArgs);
      }
      else { return false; }
    }
    
    @Override public Boolean forIntersectionType(IntersectionType t) {
      return IterUtil.and(t.ofTypes(), this);
    }
    
    @Override public Boolean forUnionType(UnionType t) { return false; }
    
    @Override public Boolean forVariableType(final VariableType t) {
      Set<Wildcard> old = resetVisibleWildcards();
      try {
        Thunk<Boolean> recur = new Thunk<Boolean>() {
          public Boolean value() {
            Type lower = t.symbol().lowerBound();
            Type upper = t.symbol().upperBound();
            return lower.apply(WellFormedTester.this) && upper.apply(WellFormedTester.this) &&
                    isSubtype(lower, upper) && !containsVar(lower, t);
          }
        };
        // assume it's well-formed if we're already checking it
        return _stack.apply(recur, true, t);
      }
      finally { _visibleWildcards = old; }
    }
    
    @Override public Boolean forWildcard(final Wildcard w) {
      if (_visibleWildcards.contains(w)) { return true; }
      else {
        Thunk<Boolean> recur = new Thunk<Boolean>() {
          public Boolean value() {
            Type lower = w.symbol().lowerBound();
            Type upper = w.symbol().upperBound();
            _visibleWildcards.add(w);
            try {
              if (upper.apply(WellFormedTester.this)) {
                Set<Wildcard> old = resetVisibleWildcards();
                try { return lower.apply(WellFormedTester.this) && isSubtype(lower, upper); }
                finally { _visibleWildcards = old; }
              }
              else { return false; }
            }
            finally { _visibleWildcards.remove(w); }
          }
        };
        // if we encounter a wildcard we've seen before that isn't visible, it's malformed
        return _stack.apply(recur, false, w);
      }
    }
  }
  
  /** Test whether a variable is reachable from a type. */
  private boolean containsVar(Type t, final VariableType var) {
    return containsAnyVar(t, Collections.singleton(var));
  }
  
  /** Test whether any of the given variables is reachable from a type. */
  private boolean containsAnyVar(Type t, final Set<? extends VariableType> vars) {
    return t.apply(new TypeAbstractVisitor<Boolean>() {
      private final RecursionStack<Type> _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
      public Boolean defaultCase(Type t) { return false; }
      @Override public Boolean forArrayType(ArrayType t) { return t.ofType().apply(this); }
      @Override public Boolean forParameterizedClassType(ParameterizedClassType t) {
        return checkList(t.typeArguments());
      }
      @Override public Boolean forBoundType(BoundType t) {  return checkList(t.ofTypes()); }
      @Override public Boolean forVariableType(VariableType t) {
        return vars.contains(t) || checkBoundedSymbol(t, t.symbol());
      }
      @Override public Boolean forWildcard(Wildcard w) { return checkBoundedSymbol(w, w.symbol()); } 
      
      private Boolean checkList(Iterable<? extends Type> types) {
        for (Type t : types) { 
          if (t.apply(this)) { return true; }
        }
        return false;
      }
      
      private Boolean checkBoundedSymbol(Type t, final BoundedSymbol s) {
        final TypeVisitor<Boolean> visitor = this; // handles this shadowing
        Thunk<Boolean> handleBounds = new Thunk<Boolean>() {
          public Boolean value() {
            return s.lowerBound().apply(visitor) || s.upperBound().apply(visitor);
          }
        };
        return _stack.apply(handleBounds, false, t);
      }
      
    });
  }
  
  
  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public boolean isEqual(Type t1, Type t2) {
    return new IsEqualTester().contains(t1, t2);
  }
  
  private class IsEqualTester implements Order<Type>, Lambda2<Type, Type, Boolean> {
    RecursionStack2<Type, Type> _stack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
    
    public Boolean value(Type subT, Type superT) { return contains(subT, superT); }
    
    public boolean contains(final Type t1, final Type t2) {
      return t1.apply(new TypeAbstractVisitor<Boolean>() {
        @Override public Boolean defaultCase(Type t1) { return t1.equals(t2); }

        @Override public Boolean forArrayType(ArrayType t1) {
          return (t2 instanceof ArrayType) && contains(t1.ofType(), ((ArrayType) t2).ofType());
        }
        
        @Override public Boolean forParameterizedClassType(ParameterizedClassType t1) {
          if (t2 instanceof ParameterizedClassType) {
            ParameterizedClassType t2Cast = (ParameterizedClassType) t2;
            if (t1.ofClass().equals(t2Cast.ofClass())) {
              if (sizeOf(t1.typeArguments()) == sizeOf(t2Cast.typeArguments())) {
                return IterUtil.and(t1.typeArguments(), t2Cast.typeArguments(), IsEqualTester.this);
              }
            }
          }
          return false;
        }
        
        @Override public Boolean forIntersectionType(IntersectionType t1) {
          return t2 instanceof IntersectionType &&
                 IterUtil.and(t1.ofTypes(), ((IntersectionType) t2).ofTypes(), IsEqualTester.this);
        }
        
        @Override public Boolean forWildcard(final Wildcard t1) {
          if (t2 instanceof Wildcard) {
            Thunk<Boolean> recur = new Thunk<Boolean>() {
              public Boolean value() {
                BoundedSymbol s1 = t1.symbol();
                BoundedSymbol s2 = ((Wildcard) t2).symbol();
                return IsEqualTester.this.contains(s1.upperBound(), s2.upperBound()) &&
                       IsEqualTester.this.contains(s1.lowerBound(), s2.lowerBound());
              }
            };
            return _stack.apply(recur, true, t1, t2);
          }
          else { return false; }
        }
        
      });
    }
  }
  
  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link #isEqual}), transitive relation.
   */
  public boolean isSubtype(Type subT, Type superT) { return new Subtyper().contains(subT, superT); }
  
  /**
   * Tests subtyping.  Due to its use of internal state, unrelated (and possibly parallel)
   * invocations should use distinct instances.
   */
  private class Subtyper implements Order<Type>, Lambda2<Type, Type, Boolean> {
    RecursionStack2<Type, Type> _stack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
    
    public Boolean value(Type subT, Type superT) { return contains(subT, superT); }
    
    public Predicate<Type> supertypes(Type sub) { return bindFirst((Order<Type>) this, sub); }
    
    public Predicate<Type> subtypes(Type sup) { return bindSecond((Order<Type>) this, sup); }
    
    public boolean contains(final Type subT, final Type superT) {
      debug.logStart(new String[]{"subT", "superT"}, wrap(subT), wrap(superT)); try {
              
      if (subT.equals(superT)) { return true; } // what follows assumes the types are not syntactically equal
      
      // Handle easy superT cases; return null if subT cases need to be considered, too
      Boolean result = superT.apply(new TypeAbstractVisitor<Boolean>() {
        public Boolean defaultCase(Type superT) { return null; }
        
        @Override public Boolean forVariableType(final VariableType superT) {
          return subT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(final Type subT) {
              Thunk<Boolean> checkLowerBound = new Thunk<Boolean>() {
                public Boolean value() {
                  return Subtyper.this.contains(subT, superT.symbol().lowerBound());
                }
              };
              Thunk<Boolean> checkInfinite = new Thunk<Boolean>() {
                public Boolean value() { return Subtyper.this.contains(subT, NULL); }
              };
              return _stack.apply(checkLowerBound, checkInfinite, subT, superT);
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
          else { return IterUtil.and(superT.ofTypes(), supertypes(subT)); }
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
              else { return Subtyper.this.contains(subT.ofType(), superT.ofType()); }
            }
            
            @Override public Boolean forClassType(ClassType superT) { 
              return Subtyper.this.contains(CLONEABLE_AND_SERIALIZABLE, superT);
            }
            
          });
        }
        
        @Override public Boolean forSimpleClassType(final SimpleClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forClassType(ClassType superT) {
              Type newSub = immediateSupertype(subT);
              if (newSub == null) { return false; }
              else { return Subtyper.this.contains(newSub, superT); }
            }
          });
        }
        
        @Override public Boolean forRawClassType(final RawClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forClassType(final ClassType superT) {
              Type newSub = immediateSupertype(subT);
              if (newSub == null) { return false; }
              else { return Subtyper.this.contains(newSub, superT); }
            }
            @Override public Boolean forParameterizedClassType(final ParameterizedClassType superT) {
              if (subT.ofClass().equals(superT.ofClass())) {
                return Subtyper.this.contains(parameterize(subT), superT) || forClassType(superT);
              }
              else { return forClassType(superT); }
            }
          });
        }
        
        public Boolean forParameterizedClassType(final ParameterizedClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type superT) { return false; }
            
            @Override public Boolean forClassType(ClassType superT) {
              Type newSub = immediateSupertype(subT);
              if (newSub == null) { return false; }
              else { return Subtyper.this.contains(newSub, superT); }
            }
            
            @Override public Boolean forParameterizedClassType(final ParameterizedClassType superT) {
              if (subT.ofClass().equals(superT.ofClass())) {
                boolean result = true;
                ParameterizedClassType subCapT = capture(subT);
                for (final Triple<Type, Type, Type> args : zip(subT.typeArguments(),
                                                               subCapT.typeArguments(), 
                                                               superT.typeArguments())) {
                  result &= args.third().apply(new TypeAbstractVisitor<Boolean>() {
                    public Boolean defaultCase(Type superArg) {
                      return isEqual(args.second(), superArg);
                    }
                    @Override public Boolean forWildcard(final Wildcard superArg) {
                      Thunk<Boolean> inBounds = new Thunk<Boolean>() {
                        public Boolean value() {
                          Type subArg = args.second();
                          return Subtyper.this.contains(superArg.symbol().lowerBound(), subArg) &&
                                 Subtyper.this.contains(subArg, superArg.symbol().upperBound());
                        }
                      };
                      // if we've seen this sub arg/super arg combo before, we can prove subtyping inductively
                      // (assuming superArg appears in a valid context -- checked by isWellFormed)
                      // Put the pre-capture sub arg on the stack, because post-capture it may be a fresh var
                      return _stack.apply(inBounds, true, args.first(), superArg);
                    }
                  });
                  if (!result) { break; }
                }
                return result || forClassType(superT);
              }
              else { return forClassType(superT); }
            }
            
            @Override public Boolean forRawClassType(RawClassType superT) {
              if (subT.ofClass().equals(superT.ofClass())) {
                return Subtyper.this.contains(erase(subT), superT);
              }
              else { return forClassType(superT); }
            }
            
          });
        }
        
        public Boolean forVariableType(final VariableType subT) {
          Thunk<Boolean> checkUpperBound = new Thunk<Boolean>() {
            public Boolean value() {
              return Subtyper.this.contains(subT.symbol().upperBound(), superT);
            }
          };
          Thunk<Boolean> checkInfinite = new Thunk<Boolean>() {
            public Boolean value() { return Subtyper.this.contains(OBJECT, superT); }
          };
          return _stack.apply(checkUpperBound, checkInfinite, subT, superT);
        }
        
        public Boolean forIntersectionType(IntersectionType subT) {
          return IterUtil.or(subT.ofTypes(), subtypes(superT)); 
        }
        
        public Boolean forBottomType(BottomType subT) { return true; }
      });
      } finally { debug.logEnd(); }
    }
  };
  
  
  /** Join implementation based on the JLS specification (15.12.2.7). */
  public Type join(Iterable<? extends Type> ts) {
    return join(ts, new PrecomputedRecursionStack<Set<Type>, Wildcard>(Wrapper.<Set<Type>>factory()));
  }
  
  private Type join(Iterable<? extends Type> ts, final PrecomputedRecursionStack<Set<Type>, Wildcard> joinStack) {
    if (_packCaptureVars) {
      ts = IterUtil.mapSnapshot(ts, new Lambda<Type, Type>() {
        public Type value(Type t) {
          while (t instanceof VariableType && ((VariableType) t).symbol().generated()) {
            t = ((VariableType) t).symbol().upperBound();
          }
          return t;
        }
      });
    }
    switch (sizeOf(ts, 2)) {
      case 0: return BOTTOM;
      case 1: return IterUtil.first(ts);
      default:
        boolean hasNonReference = false;
        boolean hasReference = false;
        for (Type t : ts) {
          if (isReference(t)) { hasReference = true; }
          else { hasNonReference = true; }
        }
        if (hasNonReference && hasReference) { return TOP; }
        else {
          final Set<Type> toJoin = CollectUtil.asSet(filter(ts, bindFirst(LambdaUtil.NOT_EQUAL, NULL)));
          switch (toJoin.size()) {
            case 0: return NULL;
            case 1: return IterUtil.first(toJoin);
            default:
              Set<Type> erasedTypes = IterUtil.first(toJoin).apply(new ErasedSuperAccumulator()).result();
              for (Type t : IterUtil.skipFirst(toJoin)) {
                erasedTypes.retainAll(t.apply(new ErasedSuperAccumulator()).result());
              }
              List<Type> minimalSupers = minList(erasedTypes, new Subtyper());
              Iterable<Type> conjuncts = mapSnapshot(minimalSupers, new Lambda<Type, Type>() {
                public Type value(Type erasedT) {
                  if (erasedT instanceof RawClassType) {
                    TypeArgumentMerger merger = new TypeArgumentMerger((RawClassType) erasedT);
                    IterUtil.run(toJoin, asRunnable(merger));
                    return merger.result(joinStack);
                  }
                  else { return erasedT; }
                }
              });
              return meet(conjuncts);
          }
        }
    }
  }
  
  /** Accumulates a set (ordered depth-first) of erased supertypes of the given type. */
  private class ErasedSuperAccumulator extends TypeAbstractVisitor<ErasedSuperAccumulator> {
    private final Set<Type> _result;
    private final RecursionStack<Type> _stack;
    
    public ErasedSuperAccumulator() {
      _result = new LinkedHashSet<Type>();
      _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
    }
    
    public Set<Type> result() { return _result; }
    
    @Override public ErasedSuperAccumulator forPrimitiveType(PrimitiveType t) {
      _result.add(t);
      return this;
    }
    
    @Override public ErasedSuperAccumulator forCharType(CharType t) {
      _result.addAll(Arrays.asList(CHAR, INT, LONG, FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forByteType(ByteType t) {
      _result.addAll(Arrays.asList(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forShortType(ShortType t) {
      _result.addAll(Arrays.asList(SHORT, INT, LONG, FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forIntType(IntType t) {
      _result.addAll(Arrays.asList(INT, LONG, FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forLongType(LongType t) {
      _result.addAll(Arrays.asList(LONG, FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forFloatType(FloatType t) {
      _result.addAll(Arrays.asList(FLOAT, DOUBLE));
      return this;
    }
    
    @Override public ErasedSuperAccumulator forNullType(NullType t) {
      _result.add(t);
      return this;
    }
    
    @Override public ErasedSuperAccumulator forArrayType(ArrayType t) {
      Set<Type> elementTypes = t.ofType().apply(new ErasedSuperAccumulator()).result();
      for (Type elt : elementTypes) {
        _result.add(new SimpleArrayType(elt));
      }
      CLONEABLE_AND_SERIALIZABLE.apply(this);
      return this;
    }
    
    @Override public ErasedSuperAccumulator forClassType(ClassType t) {
      _result.add(erase(t));
      Type sup = immediateSupertype(t);
      if (sup != null) { sup.apply(this); }
      return this;
    }
    
    @Override public ErasedSuperAccumulator forIntersectionType(IntersectionType t) {
      for (Type sup : t.ofTypes()) { sup.apply(this); }
      return this;
    }
    
    @Override public ErasedSuperAccumulator forVariableType(final VariableType t) {
      Runnable recurOnBound = asRunnable(bindFirst(this, t.symbol().upperBound()));
      Runnable recurOnObject = asRunnable(bindFirst(this, OBJECT));
      _stack.run(recurOnBound, recurOnObject, t);
      return this;
    }
    
  }
  
  /** 
   * Finds an instantiation of a raw class type that is a supertype of the visited types.
   * Visiting types that are not subtypes of the raw class has no effect on the result.
   */
  private class TypeArgumentMerger extends TypeAbstractVisitor<TypeArgumentMerger> {
    private final DJClass _c;
    private final RecursionStack<Type> _stack; 
    private final List<ArgSet> _args;
    private boolean _rawResult; // whether the result should be a raw type
    
    public TypeArgumentMerger(RawClassType erased) {
      _c = erased.ofClass();
      _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
      int params = sizeOf(SymbolUtil.allTypeParameters(_c)); 
      _args = new ArrayList<ArgSet>(params);
      for (int i = 0; i < params; i++) { _args.add(new ArgSet()); }
      _rawResult = false;
    }
    
    public ClassType result(final PrecomputedRecursionStack<Set<Type>, Wildcard> joinStack) {
      if (_rawResult) { return new RawClassType(_c); }
      else {
        return new ParameterizedClassType(_c, mapSnapshot(_args, new Lambda<ArgSet, Type>() {
          public Type value(ArgSet set) { return set.merge(joinStack); }
        }));
      }
    }
    
    @Override public TypeArgumentMerger defaultCase(Type t) { return this; } // ignore by default
      
    @Override public TypeArgumentMerger forArrayType(ArrayType t) {
      return CLONEABLE_AND_SERIALIZABLE.apply(this);
    }
    
    @Override public TypeArgumentMerger forClassType(ClassType t) {
      Type sup = immediateSupertype(t);
      if (sup != null) { sup.apply(this); }
      return this;
    }
    
    @Override public TypeArgumentMerger forRawClassType(RawClassType t) {
      if (t.ofClass().equals(_c)) { _rawResult = true; }
      else { forClassType(t); }
      return this;
    }
    
    @Override public TypeArgumentMerger forParameterizedClassType(ParameterizedClassType t) {
      if (t.ofClass().equals(_c)) {
        for (Pair<ArgSet, Type> p : zip(_args, t.typeArguments())) { p.first().add(p.second()); }
      }
      else { forClassType(t); }
      return this;
    }
    
    @Override public TypeArgumentMerger forIntersectionType(IntersectionType t) {
      // Only of the elements needs to be an instantiation of _c, but since we don't know
      // which one (and get no feedback), we'll just do them all.  If we have multiple-instantiation
      // inheritance (prohibited by JLS), we'll merge the arguments from all of them, but that's okay.
      for (Type sup : t.ofTypes()) { sup.apply(this); }
      return this;
    }
    
    @Override public TypeArgumentMerger forVariableType(VariableType t) {
      Runnable recurOnBound = asRunnable(bindFirst(this, t.symbol().upperBound()));
      Runnable recurOnObject = asRunnable(bindFirst(this, OBJECT));
      _stack.run(recurOnBound, recurOnObject, t);
      return this;
    }
    
    private class ArgSet {
      private final Set<Type> _types;
      private boolean _wildcardUpper;
      private boolean _wildcardLower;
      
      public ArgSet() {
        _types = new LinkedHashSet<Type>();
        _wildcardUpper = false;
        _wildcardLower = false;
      }
      
      public void add(Type t) {
        if (t instanceof Wildcard) {
          BoundedSymbol s = ((Wildcard) t).symbol();
          if (s.upperBound().equals(OBJECT)) {
            if (s.lowerBound().equals(NULL)) { _wildcardUpper = true; }
            else { _types.add(s.lowerBound()); _wildcardLower = true; }
          }
          else { _types.add(s.upperBound()); _wildcardUpper = true; }
        }
        else { _types.add(t); }
      }
      
      public Type merge(final PrecomputedRecursionStack<Set<Type>, Wildcard> joinStack) {
        if (_types.isEmpty() || (_wildcardUpper && _wildcardLower)) {
          return new Wildcard(new BoundedSymbol(new Object(), OBJECT, NULL));
        }
        else if (_wildcardLower) {
          return new Wildcard(new BoundedSymbol(new Object(), OBJECT, meet(_types)));
        }
        else if (_wildcardUpper || _types.size() > 1) {
          final Wildcard result = new Wildcard(new BoundedSymbol(new Object()));
          Thunk<Wildcard> recur = new Thunk<Wildcard>() {
            public Wildcard value() {
              result.symbol().initializeLowerBound(NULL);
              result.symbol().initializeUpperBound(join(_types, joinStack));
              return result;
            }
          };
          return joinStack.apply(recur, result, _types);
        }
        else { // no wildcards, size is 1
          return IterUtil.first(_types);
        }
      }
      
    }
  }
  
  
  /** Simple meet implementation: construct an intersection if there are more than two. */
  public Type meet(Iterable<? extends Type> ts) {
    Set<? extends Type> toMeet = CollectUtil.asSet(ts);
    switch (toMeet.size()) {
      case 0: return TOP;
      case 1: return IterUtil.first(toMeet);
      default: return new IntersectionType(IterUtil.snapshot(toMeet));
    }
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
    Set<VariableType> remainingParams = new HashSet<VariableType>();
    CollectUtil.addAll(remainingParams, params);
    while (!remainingParams.isEmpty()) {
      boolean changed = false;
      for (Triple<BoundedSymbol, Type, VariableType> triple : zip(captureVars, targs, params)) {
        VariableType param = triple.third();
        if (!containsAnyVar(param.symbol().lowerBound(), remainingParams)) {
          Type arg = triple.second();
          if (arg instanceof Wildcard) {
            Wildcard argW = (Wildcard) arg;
            Type wildU = argW.symbol().upperBound();
            Type paramU = substitute(param.symbol().upperBound(), sigma);
            Type wildL = argW.symbol().lowerBound();
            Type paramL = substitute(param.symbol().lowerBound(), sigma);
            
            Type captU = wildU.equals(OBJECT) ? paramU :
                         paramU.equals(OBJECT) ? wildU : new IntersectionType(IterUtil.make(wildU, paramU));
            triple.first().initializeUpperBound(captU);
            triple.first().initializeLowerBound(join(wildL, paramL));
          }
          remainingParams.remove(param);
          changed = true;
        }
      }
      if (!changed) { throw new IllegalArgumentException("Params have circular lower-bound dependency: " + params); }
    }
    return newArgs;
  }
  
  
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
    Inferencer inf = new Inferencer(CollectUtil.makeSet(tparams));
    
    // perform inference for args
    for (Pair<Type, Type> pair : zip(args, params)) {
      inf.subtype(pair.first(), pair.second());
    }
    
    ConstraintSet constraints = inf.constraints();
    Map<VariableType, Type> instantiations = new HashMap<VariableType, Type>();
    Iterable<VariableType> toInfer = filter(tparams, LambdaUtil.negate(asPredicateSet(instantiations.keySet())));
    
    // handle equal bounds
    for (VariableType var : toInfer) {
      Set<Type> eqBounds = constraints.equalBounds(var);
      if (!eqBounds.isEmpty()) { instantiations.put(var, IterUtil.first(eqBounds)); }
    }
    
    // handle lower bounds
    for (VariableType var : toInfer) {
      Set<Type> lowerBounds = constraints.lowerBounds(var);
      if (!lowerBounds.isEmpty()) { instantiations.put(var, join(lowerBounds)); }
    }
    
    if (!_alwaysUseArgumentConstraints) {
      inf = new Inferencer(CollectUtil.makeSet(toInfer));
      constraints = inf.constraints();
    }
    
    // perform inference for expected type
    if (expected.isSome()) {
      inf.supertype(expected.unwrap(), substitute(returned, new SubstitutionMap(instantiations)));
      for (VariableType var : toInfer) {
        Set<Type> eqBounds = constraints.equalBounds(var);
        if (!eqBounds.isEmpty()) { instantiations.put(var, IterUtil.first(eqBounds)); }
      }
    }
    
    // use upper bounds (may be inferred from args or expected, and may be declared)
    if (_waitToUseDeclaredBounds) {
      for (VariableType var : toInfer) {
        Set<Type> upperBounds = constraints.upperBounds(var);
        if (!upperBounds.isEmpty()) { instantiations.put(var, meet(upperBounds)); }
      }
      for (VariableType var : toInfer) {
        instantiations.put(var, substitute(var.symbol().upperBound(), new SubstitutionMap(instantiations)));
      }
    }
    else {
      for (VariableType var : toInfer) {
        Set<Type> upperBounds = constraints.upperBounds(var);
        Type declared = var.symbol().upperBound();
        if (!declared.equals(OBJECT)) {
          upperBounds = union(upperBounds, substitute(declared, new SubstitutionMap(instantiations)));
        }
        instantiations.put(var, meet(upperBounds));
      }
    }
    
    Iterable<Type> result = mapSnapshot(tparams, asLambdaMap(instantiations));
    SubstitutionMap sigma = new SubstitutionMap(tparams, result);
    boolean valid = inBounds(tparams, result);
    for (Pair<Type, Type> pair : zip(args, params)) {
      if (!valid) { break; }
      valid &= isSubtype(pair.first(), substitute(pair.second(), sigma));
    }
    return valid ? result : null;
  }
  
  private class ConstraintSet {
    private final Relation<VariableType, Type> _equalBounds;
    private final Relation<VariableType, Type> _upperBounds;
    private final Relation<VariableType, Type> _lowerBounds;
    
    public ConstraintSet() {
      Thunk<Map<VariableType, PredicateSet<Type>>> mapFactory = CollectUtil.hashMapFactory();
      Thunk<Set<Type>> setFactory = CollectUtil.linkedHashSetFactory();
      _equalBounds = new IndexedRelation<VariableType, Type>(mapFactory, setFactory);
      _upperBounds = new IndexedRelation<VariableType, Type>(mapFactory, setFactory);
      _lowerBounds = new IndexedRelation<VariableType, Type>(mapFactory, setFactory);
    }
    
    public void addEqualBound(VariableType var, Type t) { _equalBounds.add(var, t); }
    public void addUpperBound(VariableType var, Type t) {
      _upperBounds.add(var, t);
    }
    public void addLowerBound(VariableType var, Type t) {
      _lowerBounds.add(var, t);
    }
    
    public Set<Type> equalBounds(VariableType var) { return _equalBounds.matchFirst(var); }
    public Set<Type> upperBounds(VariableType var) { return _upperBounds.matchFirst(var); }
    public Set<Type> lowerBounds(VariableType var) { return _lowerBounds.matchFirst(var); }
  }
  
  private class Inferencer {
    private final Set<? extends VariableType> _vars;
    private final ConstraintSet _constraints;
    private final RecursionStack2<Type, Type> _subStack;
    private final RecursionStack2<Type, Type> _supStack;
    
    public Inferencer(Set<? extends VariableType> vars) {
      _vars = vars;
      _constraints = new ConstraintSet();
      _subStack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
      _supStack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
    }
    
    public ConstraintSet constraints() { return _constraints; }
    
    public void subtype(final Type arg, Type param) {
      if (param.apply(_containsVar) && !(arg instanceof NullType)) {
        param.apply(new TypeAbstractVisitor_void() {

          @Override public void forVariableType(VariableType param) {
            if (_vars.contains(param)) { _constraints.addLowerBound(param, arg); }
          }
          
          @Override public void forArrayType(final ArrayType param) {
            arg.apply(new TypeAbstractVisitor_void() {
              @Override public void forArrayType(ArrayType arg) { subtype(arg.ofType(), param.ofType()); }
              @Override public void forVariableType(VariableType arg) {
                Runnable recurOnSuper = bindFirst(this, arg.symbol().upperBound());
                Runnable recurOnObject = bindFirst(this, OBJECT);
                _subStack.run(recurOnSuper, recurOnObject, arg, param);
              }
            });
          }
          
          @Override public void forParameterizedClassType(final ParameterizedClassType param) {
            arg.apply(new TypeAbstractVisitor_void() {
              @Override public void forArrayType(ArrayType arg) { CLONEABLE_AND_SERIALIZABLE.apply(this); }
              @Override public void forClassType(ClassType arg) {
                Type argSup = immediateSupertype(arg);
                if (argSup != null) { argSup.apply(this); }
              }
              @Override public void forParameterizedClassType(ParameterizedClassType arg) {
                if (arg.ofClass().equals(param.ofClass())) {
                  for (final Pair<Type, Type> pair : zip(arg.typeArguments(), param.typeArguments())) {
                    pair.second().apply(new TypeAbstractVisitor_void() {
                      @Override public void forValidType(ValidType param) {
                        if (pair.first() instanceof ValidType) { equal(pair.first(), param); }
                      }
                      @Override public void forWildcard(final Wildcard param) {
                        Runnable recurOnWildcard = new Runnable() {
                          public void run() {
                            final Type paramUpper = param.symbol().upperBound();
                            final Type paramLower = param.symbol().lowerBound();
                            if (!paramUpper.equals(OBJECT)) {
                              pair.first().apply(new TypeAbstractVisitor_void() {
                                @Override public void forValidType(ValidType arg) { subtype(arg, paramUpper); }
                                @Override public void forWildcard(Wildcard arg) {
                                  Type argUpper = arg.symbol().upperBound();
                                  if (!argUpper.equals(OBJECT)) { subtype(argUpper, paramUpper); }
                                }
                              });
                            }
                            if (!paramLower.equals(NULL)) {
                              pair.first().apply(new TypeAbstractVisitor_void() {
                                @Override public void forValidType(ValidType arg) { supertype(arg, paramLower); }
                                @Override public void forWildcard(Wildcard arg) {
                                  Type argLower = arg.symbol().lowerBound();
                                  if (!argLower.equals(NULL)) { supertype(argLower, paramLower); }
                                }
                              });
                            }
                          }
                        };
                        _subStack.run(recurOnWildcard, pair.first(), param);
                      }
                    });
                  }
                }
                else { forClassType(arg); }
              }
              @Override public void forIntersectionType(IntersectionType arg) {
                for (Type argSup : arg.ofTypes()) { argSup.apply(this); }
              }
              @Override public void forVariableType(VariableType arg) {
                Runnable recurOnSuper = bindFirst(this, arg.symbol().upperBound());
                Runnable recurOnObject = bindFirst(this, OBJECT);
                _subStack.run(recurOnSuper, recurOnObject, arg, param);
              }
            });
          }
          
        });
      }
    }
    
    public void supertype(final Type arg, Type param) {
      if (param.apply(_containsVar) && !(arg instanceof NullType)) {
        param.apply(new TypeAbstractVisitor_void() {

          @Override public void forVariableType(VariableType param) {
            if (_vars.contains(param)) { _constraints.addUpperBound(param, arg); }
          }
          
          @Override public void forArrayType(final ArrayType param) {
            if (arg instanceof ArrayType) { supertype(((ArrayType) arg).ofType(), param.ofType()); }
          }
          
          @Override public void forParameterizedClassType(final ParameterizedClassType param) {
            TypeVisitorRunnable1 argVisitor = new TypeAbstractVisitor_void() {
              @Override public void forParameterizedClassType(ParameterizedClassType arg) {
                if (arg.ofClass().equals(param.ofClass())) {
                  for (final Pair<Type, Type> pair : zip(arg.typeArguments(), param.typeArguments())) {
                    pair.first().apply(new TypeAbstractVisitor_void() {
                      @Override public void forValidType(ValidType arg) {
                        if (pair.second() instanceof ValidType) { equal(arg, pair.second()); }
                      }
                      @Override public void forWildcard(final Wildcard arg) {
                        final Type argUpper = arg.symbol().upperBound();
                        final Type argLower = arg.symbol().lowerBound();
                        if (!argUpper.equals(OBJECT)) {
                          pair.second().apply(new TypeAbstractVisitor_void() {
                            @Override public void forValidType(ValidType param) { supertype(argUpper, param); }
                            @Override public void forWildcard(Wildcard param) {
                              Type paramUpper = param.symbol().upperBound();
                              if (!paramUpper.equals(OBJECT)) { supertype(argUpper, paramUpper); }
                            }
                          });
                        }
                        if (!argLower.equals(NULL)) {
                          pair.second().apply(new TypeAbstractVisitor_void() {
                            @Override public void forValidType(ValidType param) { subtype(argLower, param); }
                            @Override public void forWildcard(Wildcard param) {
                              Type paramLower = param.symbol().lowerBound();
                              if (!paramLower.equals(NULL)) { subtype(argLower, paramLower); }
                            }
                          });
                        }
                      }
                    });
                  }
                }
                else {
                  Type paramSup = immediateSupertype(param);
                  if (paramSup != null) { supertype(arg, paramSup); }
                }
              }
            };
            // avoid infinite recursion on recursive wildcards
            _supStack.run(bindFirst(argVisitor, arg), arg, param);
          }
          
          @Override public void forIntersectionType(IntersectionType param) {
            // this case is not specified, but is necessary in this implementation because immediateSupertype
            // may return an intersection
            for (Type paramSup : param.ofTypes()) { supertype(arg, paramSup); }
          }
        });
      }
    }
    
    public void equal(final Type arg, Type param) {
      if (param.apply(_containsVar) && !(arg instanceof NullType)) {
        param.apply(new TypeAbstractVisitor_void() {

          @Override public void forVariableType(VariableType param) {
            if (_vars.contains(param)) { _constraints.addEqualBound(param, arg); }
          }
          
          @Override public void forArrayType(final ArrayType param) {
            if (arg instanceof ArrayType) { equal(((ArrayType) arg).ofType(), param.ofType()); }
          }
          
          @Override public void forParameterizedClassType(final ParameterizedClassType param) {
            if (arg instanceof ParameterizedClassType) {
              for (Pair<Type, Type> pair : zip(((ParameterizedClassType) arg).typeArguments(), param.typeArguments())) {
                if (pair.first() instanceof ValidType && pair.second() instanceof ValidType) {
                  equal(pair.first(), pair.second());
                }
                else if (pair.first() instanceof Wildcard && pair.second() instanceof Wildcard) {
                  BoundedSymbol argBounds = ((Wildcard) pair.first()).symbol();
                  BoundedSymbol paramBounds = ((Wildcard) pair.second()).symbol();
                  if (!(argBounds.upperBound().equals(OBJECT) || paramBounds.upperBound().equals(OBJECT))) {
                    equal(argBounds.upperBound(), paramBounds.upperBound());
                  }
                  if (!(argBounds.lowerBound().equals(NULL) || paramBounds.lowerBound().equals(NULL))) {
                    equal(argBounds.lowerBound(), paramBounds.lowerBound());
                  }
                }
              }
            }
          }
            
        });
      }
    }
     
    private final TypeVisitorLambda<Boolean> _containsVar = new TypeAbstractVisitor<Boolean>() {
      private final RecursionStack<Type> _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
      public Boolean defaultCase(Type t) { return false; }
      @Override public Boolean forArrayType(ArrayType t) { return t.ofType().apply(this); }
      @Override public Boolean forParameterizedClassType(ParameterizedClassType t) {
        return checkList(t.typeArguments());
      }
      @Override public Boolean forBoundType(BoundType t) {  return checkList(t.ofTypes()); }
      @Override public Boolean forVariableType(VariableType t) {
        return _vars.contains(t) || checkBoundedSymbol(t, t.symbol());
      }
      @Override public Boolean forWildcard(Wildcard w) { return checkBoundedSymbol(w, w.symbol()); } 
      
      private Boolean checkList(Iterable<? extends Type> types) {
        for (Type t : types) { 
          if (t.apply(this)) { return true; }
        }
        return false;
      }
      
      private Boolean checkBoundedSymbol(Type t, final BoundedSymbol s) {
        final TypeVisitor<Boolean> visitor = this; // handles this shadowing
        Thunk<Boolean> handleBounds = new Thunk<Boolean>() {
          public Boolean value() {
            return s.lowerBound().apply(visitor) || s.upperBound().apply(visitor);
          }
        };
        return _stack.apply(handleBounds, false, t);
      }
      
    };
  }
        
}
