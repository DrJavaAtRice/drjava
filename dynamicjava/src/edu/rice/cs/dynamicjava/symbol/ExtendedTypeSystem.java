package edu.rice.cs.dynamicjava.symbol;

import java.util.*;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Wrapper;
import edu.rice.cs.plt.recur.*;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.Order;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.type.*;

import static edu.rice.cs.plt.iter.IterUtil.map;
import static edu.rice.cs.plt.iter.IterUtil.singleton;
import static edu.rice.cs.plt.iter.IterUtil.collapse;
import static edu.rice.cs.plt.collect.CollectUtil.maxList;
import static edu.rice.cs.plt.collect.CollectUtil.minList;
import static edu.rice.cs.plt.collect.CollectUtil.composeMaxLists;
import static edu.rice.cs.plt.collect.CollectUtil.union;
import static edu.rice.cs.plt.collect.CollectUtil.intersection;
import static edu.rice.cs.plt.lambda.LambdaUtil.bindFirst;
import static edu.rice.cs.plt.lambda.LambdaUtil.bindSecond;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ExtendedTypeSystem extends StandardTypeSystem {
  
  /** Whether the inference algorithm should attempt to pack capture variables that appear as inference results. */
  private final boolean _packCaptureVars;
  
  public ExtendedTypeSystem(Options opt) { this(opt, true, true, true, true); }
  
  public ExtendedTypeSystem(Options opt, boolean packCaptureVars, boolean boxingInMostSpecific,
                             boolean useExplicitTypeArgs, boolean strictClassEquality) {
    super(opt, boxingInMostSpecific, useExplicitTypeArgs, strictClassEquality);
    _packCaptureVars = packCaptureVars;
  }
  
  /** Determine if the type is well-formed. */
  public boolean isWellFormed(Type t) {
    return new WellFormedChecker().contains(t);
  }
  
  /**
   * Tests well-formedness for normalized types.  Due to its use of internal state, unrelated (and possibly parallel)
   * invocations should use distinct instances.
   */
  private class WellFormedChecker extends TypeAbstractVisitor<Boolean> implements Predicate<Type> {
    RecursionStack<Type> _stack = new RecursionStack<Type>(Wrapper.<Type>factory());
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
      if (IterUtil.and(args, this)) {
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
    @Override public Boolean forBoundType(BoundType t) {
      return IterUtil.and(t.ofTypes(), this);
    }
    @Override public Boolean forVariableType(final VariableType t) {
      Thunk<Boolean> checkVar = new Thunk<Boolean>() {
        public Boolean value() { return checkBoundedSymbol(t.symbol()); }
      };
      return _stack.apply(checkVar, true, t);
    }
    @Override public Boolean forWildcard(Wildcard w) {
      return checkBoundedSymbol(w.symbol());
    }
    private boolean checkBoundedSymbol(BoundedSymbol s) {
      Type lower = s.lowerBound();
      Type upper = s.upperBound();
      return lower.apply(this) && upper.apply(this) && isSubtype(lower, upper);
    }
  }
  
  /** Determine if the given types may be treated as equal.  This is recursive, transitive, and symmetric. */
  public boolean isEqual(Type t1, Type t2) {
    //debug.logStart(new String[]{"t1","t2"}, wrap(t1), wrap(t2)); try {
      
    if (t1.equals(t2)) { return true; }
    else {
      NormSubtyper sub = new NormSubtyper();
      Normalizer norm = new Normalizer(sub);
      Type t1Norm = norm.value(t1);
      Type t2Norm = norm.value(t2);
      return sub.contains(t1Norm, t2Norm) && sub.contains(t2Norm, t1Norm);
    }
    
    //} finally { debug.logEnd(); }
  }
  
  /**
   * Determine if {@code subT} is a subtype of {@code superT}.  This is a recursive
   * (in terms of {@link #isEqual}), transitive relation.
   */
  public boolean isSubtype(Type subT, Type superT) {
    NormSubtyper sub = new NormSubtyper();
    Normalizer norm = new Normalizer(sub);
    return sub.contains(norm.value(subT), norm.value(superT));
  }
  
  /**
   * Tests subtyping for normalized types.  Due to its use of internal state, unrelated (and possibly parallel)
   * invocations should use distinct instances.
   */
  private class NormSubtyper implements Order<Type>, Lambda2<Type, Type, Boolean> {
    RecursionStack2<Type, Type> _stack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
    
    public Boolean value(Type subT, Type superT) { return contains(subT, superT); }
    
    public Predicate<Type> supertypes(Type sub) { return bindFirst((Order<Type>) this, sub); }
    
    public Predicate<Type> subtypes(Type sup) { return bindSecond((Order<Type>) this, sup); }
    
    public boolean contains(final Type subT, final Type superT) {
      //debug.logStart(new String[]{"subT", "superT"}, wrap(subT), wrap(superT)); try {
              
      if (subT.equals(superT)) { return true; } // what follows assumes the types are not syntactically equal
      
      // Handle easy superT cases; return null if subT cases need to be considered, too
      Boolean result = superT.apply(new TypeAbstractVisitor<Boolean>() {
        public Boolean defaultCase(Type superT) { return null; }
        
        @Override public Boolean forVariableType(final VariableType superT) {
          return subT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(final Type subT) {
              Thunk<Boolean> checkLowerBound = new Thunk<Boolean>() {
                public Boolean value() {
                  Type bound = new Normalizer(NormSubtyper.this).value(superT.symbol().lowerBound());
                  return NormSubtyper.this.contains(subT, bound);
                }
              };
              Thunk<Boolean> checkInfinite = new Thunk<Boolean>() {
                public Boolean value() { return NormSubtyper.this.contains(subT, NULL); }
              };
              return _stack.apply(checkLowerBound, checkInfinite, subT, superT);
            }
            @Override public Boolean forVariableType(VariableType subT) {
              return defaultCase(subT) ? true : null;
            }
            @Override public Boolean forIntersectionType(IntersectionType subT) {
              return defaultCase(subT) ? true : null;
            }
            @Override public Boolean forUnionType(UnionType subT) { return null; }
            @Override public Boolean forBottomType(BottomType subT) { return true; }
          });
        }
        
        @Override public Boolean forIntersectionType(final IntersectionType superT) {
          return subT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type subT) {
              return IterUtil.and(superT.ofTypes(), supertypes(subT));
            }
            @Override public Boolean forUnionType(UnionType subT) { return null; }
            @Override public Boolean forBottomType(BottomType subT) { return true; }
          });
        }
        
        @Override public Boolean forUnionType(final UnionType superT) {
          return subT.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type t) {
              return IterUtil.or(superT.ofTypes(), supertypes(subT)); 
            }
            @Override public Boolean forVariableType(VariableType t) { return defaultCase(subT) ? true : null; }
            @Override public Boolean forIntersectionType(IntersectionType t) { return null; }
            @Override public Boolean forUnionType(UnionType t) { return null; }
            @Override public Boolean forBottomType(BottomType t) { return true; }
          });
        }
        
        @Override public Boolean forTopType(TopType superT) { return true; }
      });
      
      if (result != null) { return result; }
      
      // Handle subT-based cases:
      return subT.apply(new TypeAbstractVisitor<Boolean>() {
        
        @Override public Boolean defaultCase(Type t) { return false; }
        
        @Override public Boolean forCharType(CharType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forCharType(CharType superT) { return true; }
            @Override public Boolean forIntType(IntType superT) { return true; }
            @Override public Boolean forLongType(LongType superT) { return true; }
            @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
          });
        }
        
        @Override public Boolean forByteType(ByteType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forIntegerType(IntegerType superT) { return true; }
            @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
          });
        }
        
        @Override public Boolean forShortType(ShortType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forShortType(ShortType superT) { return true; }
            @Override public Boolean forIntType(IntType superT) { return true; }
            @Override public Boolean forLongType(LongType superT) { return true; }
            @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
          });
        }
        
        @Override public Boolean forIntType(IntType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forIntType(IntType superT) { return true; }
            @Override public Boolean forLongType(LongType superT) { return true; }
            @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
          });
        }
        
        @Override public Boolean forLongType(LongType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forLongType(LongType superT) { return true; }
            @Override public Boolean forFloatingPointType(FloatingPointType superT) { return true; }
          });
        }
        
        @Override public Boolean forFloatType(FloatType subT) { return superT instanceof FloatingPointType; }
        
        @Override public Boolean forNullType(NullType subT) { return isReference(superT); }
        
        @Override public Boolean forSimpleArrayType(SimpleArrayType subT) { return handleArrayType(subT); }
        
        @Override public Boolean forVarargArrayType(VarargArrayType subT) { return handleArrayType(subT); }
        
        private Boolean handleArrayType(final ArrayType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            
            @Override public Boolean forArrayType(ArrayType superT) {
              if (isPrimitive(subT.ofType())) {
                // types may be inequal if one is vararg and the other is not
                return subT.ofType().equals(superT.ofType());
              }
              else { return NormSubtyper.this.contains(subT.ofType(), superT.ofType()); }
            }
            
            @Override public Boolean forClassType(ClassType superT) { 
              return NormSubtyper.this.contains(CLONEABLE_AND_SERIALIZABLE, superT);
            }
            
          });
        }
        
        /**
         * Recur on {@code newSub}, a class's parent type.  {@code newSub} may be null, as in
         * {@code immediateSupertype()}.
         */
        private Boolean recurOnClassParent(final Type newSub) {
          if (newSub == null) { return false; }
          else {
            Thunk<Boolean> recurOnParent = new Thunk<Boolean>() {
              public Boolean value() {
                Type newSubNorm = new Normalizer(NormSubtyper.this).value(newSub);
                return NormSubtyper.this.contains(newSubNorm, superT);
              }
            };
            return _stack.apply(recurOnParent, false, newSub, superT);
          }
        }
        
        @Override public Boolean forSimpleClassType(final SimpleClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forClassType(final ClassType superT) {
              return recurOnClassParent(immediateSupertype(subT));
            }
            @Override public Boolean forSimpleClassType(final SimpleClassType superT) {
              return sameClass(subT, superT) || forClassType(superT);
            }
          });
        }
        
        @Override public Boolean forRawClassType(final RawClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forClassType(final ClassType superT) {
              return recurOnClassParent(immediateSupertype(subT));
            }
            @Override public Boolean forRawClassType(final RawClassType superT) {
              return sameClass(subT, superT) || forClassType(superT);
            }
            @Override public Boolean forParameterizedClassType(final ParameterizedClassType superT) {
              if (sameClass(subT, superT)) {
                return recurOnClassParent(parameterize(subT)) || forClassType(superT);
              }
              else { return forClassType(superT); }
            }
          });
        }
        
        @Override public Boolean forParameterizedClassType(final ParameterizedClassType subT) {
          return superT.apply(new TypeAbstractVisitor<Boolean>() {
            public Boolean defaultCase(Type superT) { return false; }
            @Override public Boolean forClassType(ClassType superT) {
              return recurOnClassParent(immediateSupertype(subT)) || recurOnClassParent(erase(subT));
            }
            @Override public Boolean forParameterizedClassType(final ParameterizedClassType superT) {
              if (sameClass(subT, superT)) {
                boolean containedArgs = true;
                ParameterizedClassType subCapT = capture(subT);
                for (final Pair<Type, Type> args : IterUtil.zip(subCapT.typeArguments(), 
                                                                superT.typeArguments())) {
                  containedArgs &= args.second().apply(new TypeAbstractVisitor<Boolean>() {
                    public Boolean defaultCase(Type superArg) {
                      Type subArg = args.first();
                      return NormSubtyper.this.contains(subArg, superArg) &&
                             NormSubtyper.this.contains(superArg, subArg);
                    }
                    @Override public Boolean forWildcard(Wildcard superArg) {
                      Type subArg = args.first();
                      return NormSubtyper.this.contains(superArg.symbol().lowerBound(), subArg) &&
                             NormSubtyper.this.contains(subArg, superArg.symbol().upperBound());
                    }
                  });
                  if (!containedArgs) { break; }
                }
                return containedArgs || forClassType(superT);
              }
              else { return forClassType(superT); }
            }
          });
        }
        
        @Override public Boolean forVariableType(final VariableType subT) {
          Thunk<Boolean> checkUpperBound = new Thunk<Boolean>() {
            public Boolean value() {
              Type bound = new Normalizer(NormSubtyper.this).value(subT.symbol().upperBound());
              return NormSubtyper.this.contains(bound, superT);
            }
          };
          Thunk<Boolean> checkInfinite = new Thunk<Boolean>() {
            public Boolean value() { return NormSubtyper.this.contains(OBJECT, superT); }
          };
          return _stack.apply(checkUpperBound, checkInfinite, subT, superT);
        }
        
        @Override public Boolean forIntersectionType(IntersectionType subT) {
          return IterUtil.or(subT.ofTypes(), subtypes(superT)); 
        }
        
        @Override public Boolean forUnionType(UnionType subT) {
          return IterUtil.and(subT.ofTypes(), subtypes(superT)); 
        }
        
        public Boolean forBottomType(BottomType subT) { return true; }
      });
      
      //} finally { debug.logEnd(); }
    }
  };
  
  /**
   * Converts the type to a normalized form:<ul>
   * <li>Unions are minimal, have at least two elements, and contain no nested unions.</li>
   * <li>Intersections are minimal, have at least two elements, and contain no nested unions or intersections.</li>
   * <li>All component types are normalized.  (Wildcard bounds are "component types"; variable bounds and
   * class supertypes are not.)</li>
   */
  private final class Normalizer extends TypeUpdateVisitor {
    
    /**
     * Subtyper to preserve stack during circular dependencies between normalization and subtyping.
     * Note that the results from this subtyper may be different than the results from a fresh
     * subtyper, and any use should be an optimization, not something essential to correctness.
     */
    private final NormSubtyper _subtyper;
    public Normalizer(NormSubtyper subtyper) { _subtyper = subtyper; }
    
    @Override public Type forIntersectionTypeOnly(IntersectionType t, Iterable<? extends Type> normTypes) {
      //debug.logStart(new String[]{"t","normTypes"}, wrap(t), wrap(normTypes)); try {
      Type result = new NormMeeter(_subtyper).value(normTypes);
      return t.equals(result) ? t : result;
      //} finally { debug.logEnd(); }
    }
    @Override public Type forUnionTypeOnly(UnionType t, Iterable<? extends Type> normTypes) {
      Type result = new NormJoiner(_subtyper).value(normTypes);
      return t.equals(result) ? t : result;
    }
    @Override public Type forWildcardOnly(Wildcard w) {
      // we assume wildcards don't contain themselves in this type system
      BoundedSymbol b = w.symbol();
      Type newUpper = recur(b.upperBound());
      Type newLower = recur(b.lowerBound());
      if (newUpper == b.upperBound() && newLower == b.lowerBound()) { return w; }
      else { return new Wildcard(new BoundedSymbol(new Object(), newUpper, newLower)); }
    }
  };
  
  public Type join(Iterable<? extends Type> ts) {
    NormSubtyper sub = new NormSubtyper();
    return new NormJoiner(sub).value(map(ts, new Normalizer(sub)));
  }
  
  /** Produce the normalized union of normalized types (may return a union or some other form). */
  private class NormJoiner implements Lambda<Iterable<? extends Type>, Type> {
    /**
     * Subtyper to preserve stack during circular dependencies between normalization and subtyping.
     * Note that the results from this subtyper may be different than the results from a fresh
     * subtyper, and so any use should be an optimization, not something essential to correctness.
     */
    private final NormSubtyper _subtyper;
    public NormJoiner(NormSubtyper subtyper) { _subtyper = subtyper; }
    public Type value(Iterable<? extends Type> elements) {
      List<Type> disjuncts = maxList(collapse(map(elements, DISJUNCTS)), _subtyper);
      switch (disjuncts.size()) {
        case 0: return BOTTOM;
        case 1: return disjuncts.get(0);
        default: return new UnionType(disjuncts);
      }
    }
  };
  
  public Type meet(Iterable<? extends Type> ts) {
    NormSubtyper sub = new NormSubtyper();
    return new NormMeeter(sub).value(map(ts, new Normalizer(sub)));
  }
  
  /** Produce the normalized intersection of normalized types (may return a union, intersection, or some other form). */
  private class NormMeeter implements Lambda<Iterable<? extends Type>, Type> {
    /**
     * Subtyper to preserve stack during circular dependencies between normalization and subtyping.
     * Note that the results from this subtyper may be different than the results from a fresh
     * subtyper, and so any use should be an optimization, not something essential to correctness.
     */
    private final NormSubtyper _subtyper;
    public NormMeeter(NormSubtyper subtyper) { _subtyper = subtyper; }
    
    public Type value(Iterable<? extends Type> elements) {
      if (IterUtil.or(elements, bindSecond(LambdaUtil.INSTANCE_OF, UnionType.class))) {
        final NormJoiner joiner = new NormJoiner(_subtyper);
        // elements contain at least one union
        Iterable<Iterable<Type>> posElements = map(elements, new Lambda<Type, Iterable<Type>>() {
          public Iterable<Type> value(Type element) {
            // convert sum-of-products (normalized) form to product-of-sums
            // javac 1.5/1.6 requires explicit type args
            return IterUtil.<Type, Type, Type, Type, Iterable<Type>>
              distribute(element, DISJUNCTS, CONJUNCTS, joiner, LambdaUtil.<Iterable<Type>>identity());
          }
        });
       // each element of conjuncts is atomic or a union of atomics
        List<Type> conjuncts = minList(collapse(posElements), new NormSubtyper());
        // convert back to sum-of-products
        // javac 1.5/1.6 requires explicit type args
        return IterUtil.<Iterable<Type>, Type, Type, Type, Type>
          distribute(conjuncts, LambdaUtil.<Iterable<Type>>identity(), DISJUNCTS, _meetAtomic, joiner);
      }
      else { return _meetAtomic.value(collapse(map(elements, CONJUNCTS))); }
    }
    
    /** Produce the normalized intersection of atomic (not union or intersection) types. */ 
    private final Lambda<Iterable<? extends Type>, Type> _meetAtomic =
        new Lambda<Iterable<? extends Type>, Type>() {
      public Type value(Iterable<? extends Type> atoms) {
        List<Type> conjuncts = minList(atoms, _subtyper);
        switch (conjuncts.size()) {
          case 0: return TOP;
          case 1: return conjuncts.get(0);
          default: return new IntersectionType(conjuncts);
        }
      }
    };
  }
  
  private final TypeVisitorLambda<Iterable<? extends Type>> DISJUNCTS =
      new TypeAbstractVisitor<Iterable<? extends Type>>() {
    @Override public Iterable<? extends Type> forValidType(ValidType t) { return singleton(t); }
    @Override public Iterable<? extends Type> forUnionType(UnionType t) { return t.ofTypes(); }
  };

  private final TypeVisitorLambda<Iterable<? extends Type>> CONJUNCTS =
      new TypeAbstractVisitor<Iterable<? extends Type>>() {
    @Override public Iterable<? extends Type> forValidType(ValidType t) { return singleton(t); }
    @Override public Iterable<? extends Type> forIntersectionType(IntersectionType t) { return t.ofTypes(); }
  };

  protected Iterable<Type> captureTypeArgs(Iterable<? extends Type> targs,
                                           Iterable<? extends VariableType> params) {
    // Create uninitialized placeholders for capture variables and normalized capture variables
    List<VariableType> captureVars = new LinkedList<VariableType>();
    List<VariableType> normCaptureVars = new LinkedList<VariableType>();
    List<Type> newArgs = new LinkedList<Type>();
    List<Type> normNewArgs = new LinkedList<Type>();
    for (Type arg : targs) {
      if (arg instanceof Wildcard) {
        VariableType var = new VariableType(new BoundedSymbol(new Object()));
        VariableType normVar = new VariableType(new BoundedSymbol(new Object()));
        captureVars.add(var);
        newArgs.add(var);
        normCaptureVars.add(normVar);
        normNewArgs.add(normVar);
      }
      else { newArgs.add(arg); normNewArgs.add(arg); }
    }
    
    // Initialize bounds of captureVars
    final SubstitutionMap sigma = new SubstitutionMap(params, newArgs);
    Iterator<VariableType> captureVarsI = captureVars.iterator();
    for (Pair<VariableType, Type> p : IterUtil.zip(params, targs)) {
      Type arg = p.second();
      if (arg instanceof Wildcard) {
        Wildcard argW = (Wildcard) arg;
        Type argU = argW.symbol().upperBound();
        Type argL = argW.symbol().lowerBound();
        VariableType param = p.first();
        Type paramU = substitute(param.symbol().upperBound(), sigma);
        Type paramL = substitute(param.symbol().lowerBound(), sigma);
        Type captureU = new IntersectionType(IterUtil.make(argU, paramU));
        Type captureL = new UnionType(IterUtil.make(argL, paramL));
        VariableType captureVar = captureVarsI.next();
        captureVar.symbol().initializeUpperBound(captureU);
        captureVar.symbol().initializeLowerBound(captureL);
      }
    }
    
    // Initialize bounds of normCaptureVars by normalizing captureVars bounds (must be done
    // in a second stage because we can't perform subtype checks on uninstantiated variables).
    Normalizer norm = new Normalizer(new NormSubtyper());
    SubstitutionMap sigmaNorm = new SubstitutionMap(captureVars, normCaptureVars);
    for (Pair<VariableType, VariableType> p : IterUtil.zip(captureVars, normCaptureVars)) {
      Type upper = substitute(norm.value(p.first().symbol().upperBound()), sigmaNorm);
      Type lower = substitute(norm.value(p.first().symbol().lowerBound()), sigmaNorm);
      p.second().symbol().initializeUpperBound(upper);
      p.second().symbol().initializeLowerBound(lower);
    }
    
    return normNewArgs;
  }
  
  private abstract class ConstraintFormula {
    public abstract boolean isSatisfiable();
    public abstract boolean isEmpty();
    public abstract Iterable<ConstraintScenario> scenarios();
    public abstract ConstraintFormula and(ConstraintFormula that);
    
    public ConstraintFormula or(ConstraintFormula that) {
      List<ConstraintScenario> scenarios = composeMaxLists(scenarios(), that.scenarios(), SCENARIO_IMPLICATION);
      if (scenarios.isEmpty()) { return FALSE; }
      else if (scenarios.size() == 1) { return scenarios.get(0); }
      else { return new DisjunctiveConstraint(scenarios); }
    }
    
    
    public String toString() {
      if (isEmpty()) { return "{}"; }
      else if (!isSatisfiable()) { return "{ false }"; }
      else {
        TypePrinter printer = typePrinter();
        StringBuilder result = new StringBuilder();
        boolean firstScenario = true;
        for (ConstraintScenario s : scenarios()) {
          if (!firstScenario) { result.append(" | "); }
          firstScenario = false;
          result.append("{ ");
          boolean firstVar = true;
          for (VariableType var : s.boundVariables()) {
            if (!firstVar) { result.append(", "); }
            firstVar = false;
            result.append(printer.print(s.lowerBound(var)));
            result.append(" <: ");
            result.append(var.symbol().name());
            result.append(" <: ");
            result.append(printer.print(s.upperBound(var)));
          }
          result.append(" }");
        }
        return result.toString();
      }
    }
    
  }
  private class ConstraintScenario extends ConstraintFormula {
    // all bounds are normalized and within range null <: T <: Object
    private final Map<VariableType, Type> _lowerBounds;
    private final Map<VariableType, Type> _upperBounds;
    
    protected ConstraintScenario() {
      _lowerBounds = new HashMap<VariableType, Type>();
      _upperBounds = new HashMap<VariableType, Type>();
    }
    
    protected ConstraintScenario(VariableType var, Type upper) {
      this();
      _upperBounds.put(var, upper);
    }
    
    protected ConstraintScenario(Type lower, VariableType var) {
      this();
      _lowerBounds.put(var, lower);
    }
    
    public boolean isSatisfiable() { return true; }
    public boolean isEmpty() { return _lowerBounds.isEmpty() && _upperBounds.isEmpty(); }
    public Iterable<ConstraintScenario> scenarios() { return singleton(this); }
    
    public ConstraintFormula and(ConstraintFormula that) {
      ConstraintFormula result = FALSE;
      for (ConstraintScenario s : that.scenarios()) {
        result = result.or(Option.unwrap(this.and(s), FALSE));
      }
      return result;
    }
    
    public Option<ConstraintScenario> and(ConstraintScenario that) {
      ConstraintScenario result = new ConstraintScenario();
      NormSubtyper sub = new NormSubtyper();
      NormJoiner join = new NormJoiner(sub);
      NormMeeter meet = new NormMeeter(sub);
      for (VariableType var : union(_lowerBounds.keySet(), that._lowerBounds.keySet())) {
        result._lowerBounds.put(var, join.value(IterUtil.make(lowerBound(var), that.lowerBound(var))));
      }
      for (VariableType var : union(_upperBounds.keySet(), that._upperBounds.keySet())) {
        result._upperBounds.put(var, meet.value(IterUtil.make(upperBound(var), that.upperBound(var))));
      }
      return result.isWellFormed() ? Option.some(result) : Option.<ConstraintScenario>none(); 
    }
    
    public Set<VariableType> boundVariables() {
      return union(_lowerBounds.keySet(), _upperBounds.keySet());
    }
    
    public Type upperBound(VariableType var) {
      Type result = _upperBounds.get(var);
      return (result == null) ? OBJECT : result;
    }
    
    public Type lowerBound(VariableType var) {
      Type result = _lowerBounds.get(var);
      return (result == null) ? NULL : result;
    }
    
    /** Test whether all variables have compatible bounds. */
    protected boolean isWellFormed() {
      NormSubtyper sub = new NormSubtyper();
      for (VariableType var : intersection(_lowerBounds.keySet(), _upperBounds.keySet())) {
        if (!sub.contains(lowerBound(var), upperBound(var))) { return false; }
      }
      return true;
    }
  }
  
  private class DisjunctiveConstraint extends ConstraintFormula {
    private final Iterable<ConstraintScenario> _scenarios;
    protected DisjunctiveConstraint(Iterable<ConstraintScenario> scenarios) { _scenarios = scenarios; }
    
    public boolean isSatisfiable() { return true; }
    public boolean isEmpty() { return false; }
    public Iterable<ConstraintScenario> scenarios() { return _scenarios; }
    
    public ConstraintFormula and(ConstraintFormula that) {
      Lambda<ConstraintFormula, Iterable<ConstraintScenario>> scenarios =
          new Lambda<ConstraintFormula, Iterable<ConstraintScenario>>() {
        public Iterable<ConstraintScenario> value(ConstraintFormula f) { return f.scenarios(); } 
      };
      Lambda<Iterable<ConstraintScenario>, Option<ConstraintScenario>> conjunction =
          new Lambda<Iterable<ConstraintScenario>, Option<ConstraintScenario>>() {
        public Option<ConstraintScenario> value(Iterable<ConstraintScenario> scenarios) {
          Option<ConstraintScenario> result = Option.some(TRUE);
          for (ConstraintScenario s : scenarios) { // loop invariant: result is a some
            result = result.unwrap().and(s);
            if (result.isNone()) { break; }
          }
          return result;
        }
      };
      Iterable<Option<ConstraintScenario>> disjuncts =
          IterUtil.distribute(IterUtil.make(this, that), scenarios, conjunction);
      ConstraintFormula result = FALSE;
      for (Option<ConstraintScenario> s : disjuncts) {
        if (s.isSome()) { result = result.or(s.unwrap()); }
      }
      return result;
    }
    
  }
  
  // Constraint primitives: only the values/methods below should be used to create new base ConstraintFormulas.
  
  private ConstraintScenario TRUE = new ConstraintScenario();
  
  private ConstraintFormula FALSE = new ConstraintFormula() {
    public boolean isSatisfiable() { return false; }
    public boolean isEmpty() { return false; }
    public Iterable<ConstraintScenario> scenarios() { return IterUtil.empty(); }
    public ConstraintFormula and(ConstraintFormula that) { return this; }
    @Override public ConstraintFormula or(ConstraintFormula that) { return that; }
  };
  
  private ConstraintFormula lowerBound(VariableType var, Type lower) {
    NormSubtyper sub = new NormSubtyper();
    if (sub.contains(NULL, lower) && sub.contains(lower, OBJECT)) { return new ConstraintScenario(lower, var); }
    else { return FALSE; }
  }
  
  private ConstraintFormula upperBound(VariableType var, Type upper) {
    NormSubtyper sub = new NormSubtyper();
    if (sub.contains(NULL, upper) && sub.contains(upper, OBJECT)) { return new ConstraintScenario(var, upper); }
    else { return FALSE; }
  }
  
  /** True when one scenario implies another: any substitution satisfying the antecedent satisfies the consequent. */
  private Order<ConstraintScenario> SCENARIO_IMPLICATION = new Order<ConstraintScenario>() {
    public boolean contains(ConstraintScenario ant, ConstraintScenario cons) {
      NormSubtyper sub = new NormSubtyper();
      for (VariableType var : cons.boundVariables()) {
        if (!sub.contains(ant.upperBound(var), cons.upperBound(var))) { return false; }
        if (!sub.contains(cons.lowerBound(var), ant.lowerBound(var))) { return false; }
      }
      return true;
    }
  };
  
  
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
    
    // perform inference for args and returned
    ConstraintFormula constraints = TRUE;
    NormSubtyper sub = new NormSubtyper();
    Normalizer norm = new Normalizer(sub);
    for (Pair<Type, Type> pair : IterUtil.zip(IterUtil.map(args, norm), IterUtil.map(params, norm))) {
      constraints = constraints.and(inf.subtypeNorm(pair.first(), pair.second()));
      if (!constraints.isSatisfiable()) { break; }
    }
    if (expected.isSome() && constraints.isSatisfiable()) {
      constraints = constraints.and(inf.supertypeNorm(norm.value(expected.unwrap()), norm.value(returned)));
    }
    
    // transitivity constraints: inferred bounds must be sub/super-types of declared bounds
    // (used to improve results where the variable has a self-referencing bound)
    ConstraintFormula transConstraints = FALSE;
    for (ConstraintScenario s : constraints.scenarios()) {
      ConstraintFormula cf = s;
      for (VariableType param : tparams) {
        cf = cf.and(inf.subtypeNorm(s.lowerBound(param), norm.value(param.symbol().upperBound())));
        if (!cf.isSatisfiable()) { break; }
        cf = cf.and(inf.supertypeNorm(s.upperBound(param), norm.value(param.symbol().lowerBound())));
        if (!cf.isSatisfiable()) { break; }
      }
      transConstraints = transConstraints.or(cf);
      if (transConstraints.isEmpty()) { break; }
    }
    
    //debug.logValue("constraints", constraints);
    if (!transConstraints.isSatisfiable()) { return null; }
    
    final Set<VariableType> inputTParams = new HashSet<VariableType>();
    for (VariableType tparam : tparams) {
      for (Type t : params) {
        if (containsVar(t, tparam)) { inputTParams.add(tparam); break; }
      }
    }

    // try to use packed bounds
    if (_packCaptureVars) {
      for (final ConstraintScenario s : transConstraints.scenarios()) {
        Iterable<Type> result = IterUtil.mapSnapshot(tparams, new Lambda<VariableType, Type>() {
          public Type value(VariableType param) {
            Type result = s.lowerBound(param);
            // use upper bound for input variables with a null lower bound
            if (result.equals(NULL) && inputTParams.contains(param)) {
              result = s.upperBound(param);
              while (result instanceof VariableType && ((VariableType) result).symbol().generated()) {
                result = ((VariableType) result).symbol().lowerBound();
              }
            }
            else {
              while (result instanceof VariableType && ((VariableType) result).symbol().generated()) {
                result = ((VariableType) result).symbol().upperBound();
              }
            }
            return result;
          }
        });
        if (inBounds(tparams, result)) { return result; }
      }
    }
    
    // packed bounds don't work, try to use bounds
    for (final ConstraintScenario s : transConstraints.scenarios()) {
      Iterable<Type> result = IterUtil.mapSnapshot(tparams, new Lambda<VariableType, Type>() {
        public Type value(VariableType param) {
          Type result = s.lowerBound(param);
          // use upper bound for input variables with a null lower bound
          if (result.equals(NULL) && inputTParams.contains(param)) { result = s.upperBound(param); }
          return result;
        }
      });
      if (inBounds(tparams, result)) { return result; }
    }
    
    // bounds don't work, try to use capture variables
    for (ConstraintScenario s : transConstraints.scenarios()) {
      List<Wildcard> constraintWs = new LinkedList<Wildcard>();
      for (VariableType param : tparams) {
        BoundedSymbol sym = new BoundedSymbol(new Object(), s.upperBound(param), s.lowerBound(param));
        constraintWs.add(new Wildcard(sym));
      }
      Iterable<Type> result = captureTypeArgs(constraintWs, tparams);
      if (IterUtil.and(result, new WellFormedChecker())) { return result; }
    }
    
    // give up
    return null;
  }
  
  private class Inferencer {
    private final Set<? extends VariableType> _vars;
    private final RecursionStack2<Type, Type> _subStack;
    private final RecursionStack2<Type, Type> _supStack;
    private final NormSubtyper _subtyper;
    
    public Inferencer(Set<? extends VariableType> vars) {
      _vars = vars;
      _subStack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
      _supStack = new RecursionStack2<Type, Type>(Pair.<Type, Type>factory());
      _subtyper = new NormSubtyper();
    }
    
    public ConstraintFormula subtypeNorm(final Type arg, final Type param) {
      //debug.logValues(new String[]{ "arg", "param" }, wrap(arg), wrap(param));
      if (!param.apply(_containsVar)) { return _subtyper.contains(arg, param) ? TRUE : FALSE; }
      else {
        return param.apply(new TypeAbstractVisitor<ConstraintFormula>() {
          
          class ArgVisitor extends TypeAbstractVisitor<ConstraintFormula> {
            @Override public ConstraintFormula defaultCase(Type arg) { return FALSE; }
            @Override public ConstraintFormula forNullType(NullType arg) { return TRUE; }
            @Override public ConstraintFormula forBottomType(BottomType arg) { return TRUE; }
            
            @Override public ConstraintFormula forVariableType(final VariableType arg) {
              Thunk<ConstraintFormula> recurOnBound = new Thunk<ConstraintFormula>() {
                public ConstraintFormula value() {
                  return subtypeNorm(new Normalizer(_subtyper).value(arg.symbol().upperBound()), param);
                }
              };
              Thunk<ConstraintFormula> infiniteCase = new Thunk<ConstraintFormula>() {
                public ConstraintFormula value() { return subtypeNorm(OBJECT, param); }
              };
              return _subStack.apply(recurOnBound, infiniteCase, arg, param);
            }
            
            @Override public ConstraintFormula forIntersectionType(IntersectionType arg) {
              ConstraintFormula result = FALSE;
              for (Type supArg : arg.ofTypes()) {
                result = result.or(subtypeNorm(supArg, param));
                if (result.isEmpty()) { break; }
              }
              return result;
            }
            
            @Override public ConstraintFormula forUnionType(UnionType arg) {
              ConstraintFormula result = TRUE;
              for (Type subArg : arg.ofTypes()) {
                result = result.and(subtypeNorm(subArg, param));
                if (!result.isSatisfiable()) { break; }
              }
              return result;
            }
          }
          
          public ConstraintFormula defaultCase(Type param) { throw new IllegalArgumentException(); }
          
          @Override public ConstraintFormula forArrayType(final ArrayType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula forArrayType(ArrayType arg) {
                if (isPrimitive(arg.ofType())) { return equivalentNorm(arg.ofType(), param.ofType()); }
                else { return subtypeNorm(arg.ofType(), param.ofType()); }
              }
            });
          }
          
          @Override public ConstraintFormula forParameterizedClassType(final ParameterizedClassType param) {
            return arg.apply(new ArgVisitor() {
              
              @Override public ConstraintFormula forArrayType(ArrayType arg) {
                return subtypeNorm(CLONEABLE_AND_SERIALIZABLE, param);
              }
              
              @Override public ConstraintFormula forClassType(ClassType arg) {
                Type argSuper = immediateSupertype(arg);
                if (argSuper == null) { return FALSE; }
                else { return subtypeNorm(argSuper, param); }
              }
              
              @Override public ConstraintFormula forRawClassType(RawClassType arg) {
                if (sameClass(arg, param)) { return subtypeNorm(parameterize(arg), param); }
                else { return forClassType(arg); }
              }
              
              @Override public ConstraintFormula forParameterizedClassType(final ParameterizedClassType arg) {
                ConstraintFormula cf = FALSE;
                if (sameClass(param, arg)) {
                  Thunk<ConstraintFormula> recurOnTargs = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() {
                      ParameterizedClassType argCap = capture(arg);
                      ConstraintFormula result = TRUE;
                      for (Pair<Type, Type> pair : IterUtil.zip(argCap.typeArguments(), param.typeArguments())) {
                        final Type argArg = pair.first();
                        final Type paramArg = pair.second();
                        result = result.and(paramArg.apply(new TypeAbstractVisitor<ConstraintFormula>() {
                          public ConstraintFormula defaultCase(Type paramArg) { 
                            return equivalentNorm(argArg, paramArg);
                          }
                          @Override public ConstraintFormula forWildcard(Wildcard paramArg) {
                            ConstraintFormula wildResult = supertypeNorm(argArg, paramArg.symbol().lowerBound());
                            if (wildResult.isSatisfiable()) {
                              wildResult = wildResult.and(subtypeNorm(argArg, paramArg.symbol().upperBound()));
                            }
                            return wildResult;
                          }
                        }));
                        if (!result.isSatisfiable()) { break; }
                      }
                      return result;
                    }
                  };
                  cf = _subStack.apply(recurOnTargs, FALSE, arg, param);
                }
                if (!cf.isEmpty()) { cf = cf.or(forClassType(arg)); }
                return cf;
              }
            });
          }
          
          @Override public ConstraintFormula forVariableType(final VariableType param) {
            // Note that this might be a capture variable with an inference-variable bound
            if (_vars.contains(param)) { return lowerBound(param, arg); }
            else {
              return arg.apply(new ArgVisitor() {
                
                @Override public ConstraintFormula defaultCase(final Type arg) {
                  Thunk<ConstraintFormula> recurOnBound = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() {
                      return subtypeNorm(arg, new Normalizer(_subtyper).value(param.symbol().lowerBound()));
                    }
                  };
                  Thunk<ConstraintFormula> infiniteCase = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() { return subtypeNorm(arg, NULL); }
                  };
                  return _subStack.apply(recurOnBound, infiniteCase, arg, param);
                }
                
                @Override public ConstraintFormula forVariableType(VariableType arg) {
                  ConstraintFormula result = super.forVariableType(arg);
                  if (!result.isEmpty()) { result = result.or(defaultCase(arg)); }
                  return result;
                }
                
                @Override public ConstraintFormula forIntersectionType(IntersectionType arg) {
                  ConstraintFormula result = super.forIntersectionType(arg);
                  if (!result.isEmpty()) { result = result.or(defaultCase(arg)); }
                  return result;
                }
                
              });
            }
          }
          
          @Override public ConstraintFormula forIntersectionType(final IntersectionType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula defaultCase(Type arg) {
                ConstraintFormula result = TRUE;
                for (Type supParam : param.ofTypes()) {
                  result = result.and(subtypeNorm(arg, supParam));
                  if (!result.isSatisfiable()) { break; }
                }
                return result;
              }
              @Override public ConstraintFormula forVariableType(VariableType arg) { return defaultCase(arg); }
              @Override public ConstraintFormula forIntersectionType(IntersectionType arg) { return defaultCase(arg); }
            });
          }
          
          @Override public ConstraintFormula forUnionType(final UnionType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula defaultCase(Type arg) {
                ConstraintFormula result = FALSE;
                for (Type subParam : param.ofTypes()) {
                  result = result.or(subtypeNorm(arg, subParam));
                  if (result.isEmpty()) { break; }
                }
                return result;
              }
              @Override public ConstraintFormula forVariableType(VariableType arg) {
                ConstraintFormula result = super.forVariableType(arg);
                if (!result.isEmpty()) { result = result.or(defaultCase(arg)); }
                return result;
              }
              @Override public ConstraintFormula forIntersectionType(IntersectionType arg) { return defaultCase(arg); }
            });
          }
          
        });
      }
    }
    
    public ConstraintFormula supertypeNorm(final Type arg, final Type param) {
      //debug.logValues(new String[]{ "arg", "param" }, wrap(arg), wrap(param));
      if (!param.apply(_containsVar)) { return new NormSubtyper().contains(param, arg) ? TRUE : FALSE; }
      else {
        return param.apply(new TypeAbstractVisitor<ConstraintFormula>() {
          
          class ArgVisitor extends TypeAbstractVisitor<ConstraintFormula> {
            @Override public ConstraintFormula defaultCase(Type arg) { return FALSE; }
            @Override public ConstraintFormula forTopType(TopType arg) { return TRUE; }
            
            @Override public ConstraintFormula forVariableType(final VariableType arg) {
              Thunk<ConstraintFormula> recurOnBound = new Thunk<ConstraintFormula>() {
                public ConstraintFormula value() {
                  return supertypeNorm(new Normalizer(_subtyper).value(arg.symbol().lowerBound()), param);
                }
              };
              Thunk<ConstraintFormula> infiniteCase = new Thunk<ConstraintFormula>() {
                public ConstraintFormula value() { return supertypeNorm(NULL, param); }
              };
              return _subStack.apply(recurOnBound, infiniteCase, arg, param);
            }
            
            @Override public ConstraintFormula forIntersectionType(IntersectionType arg) {
              ConstraintFormula result = TRUE;
              for (Type supArg : arg.ofTypes()) {
                result = result.and(supertypeNorm(supArg, param));
                if (!result.isSatisfiable()) { break; }
              }
              return result;
            }
            
            @Override public ConstraintFormula forUnionType(UnionType arg) {
              ConstraintFormula result = FALSE;
              for (Type subArg : arg.ofTypes()) {
                result = result.or(supertypeNorm(subArg, param));
                if (result.isEmpty()) { break; }
              }
              return result;
            }
          }
          
          public ConstraintFormula defaultCase(Type param) { throw new IllegalArgumentException(); }
          
          @Override public ConstraintFormula forArrayType(final ArrayType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula forArrayType(ArrayType arg) {
                if (isPrimitive(arg.ofType())) { return equivalentNorm(arg.ofType(), param.ofType()); }
                else { return supertypeNorm(arg.ofType(), param.ofType()); }
              }
              @Override public ConstraintFormula forClassType(ClassType arg) {
                return supertypeNorm(arg, CLONEABLE_AND_SERIALIZABLE);
              }
            });
          }
          
          @Override public ConstraintFormula forParameterizedClassType(final ParameterizedClassType param) {
            return arg.apply(new ArgVisitor() {
              
              @Override public ConstraintFormula forClassType(ClassType arg) {
                Type paramSuper = immediateSupertype(param);
                if (paramSuper == null) { return FALSE; }
                else { return supertypeNorm(arg, paramSuper); }
              }
              
              @Override public ConstraintFormula forRawClassType(RawClassType arg) {
                if (sameClass(arg, param)) { return TRUE; }
                else { return forClassType(arg); }
              }
              
              @Override public ConstraintFormula forParameterizedClassType(final ParameterizedClassType arg) {
                ConstraintFormula cf = FALSE;
                if (sameClass(param, arg)) {
                  Thunk<ConstraintFormula> recurOnTargs = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() {
                      ParameterizedClassType paramCap = capture(param);
                      ConstraintFormula result = TRUE;
                      for (Pair<Type, Type> pair : IterUtil.zip(arg.typeArguments(), paramCap.typeArguments())) {
                        final Type argArg = pair.first();
                        final Type paramArg = pair.second();
                        result = result.and(argArg.apply(new TypeAbstractVisitor<ConstraintFormula>() {
                          public ConstraintFormula defaultCase(Type argArg) { 
                            return equivalentNorm(argArg, paramArg);
                          }
                          @Override public ConstraintFormula forWildcard(Wildcard argArg) {
                            ConstraintFormula wildResult = subtypeNorm(argArg.symbol().lowerBound(), paramArg);
                            if (wildResult.isSatisfiable()) {
                              wildResult = wildResult.and(supertypeNorm(argArg.symbol().upperBound(), paramArg));
                            }
                            return wildResult;
                          }
                        }));
                        if (!result.isSatisfiable()) { break; }
                      }
                      return result;
                    }
                  };
                  cf = _supStack.apply(recurOnTargs, FALSE, arg, param);
                }
                if (!cf.isEmpty()) { cf = cf.or(forClassType(arg)); }
                return cf;
              }

            });
          }
          
          @Override public ConstraintFormula forVariableType(final VariableType param) {
            // Note that this might be a capture variable with an inference-variable bound
            if (_vars.contains(param)) { return upperBound(param, arg); }
            else {
              return arg.apply(new ArgVisitor() {
                
                @Override public ConstraintFormula defaultCase(final Type arg) {
                  Thunk<ConstraintFormula> recurOnBound = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() {
                      return supertypeNorm(arg, new Normalizer(_subtyper).value(param.symbol().upperBound()));
                    }
                  };
                  Thunk<ConstraintFormula> infiniteCase = new Thunk<ConstraintFormula>() {
                    public ConstraintFormula value() { return supertypeNorm(arg, OBJECT); }
                  };
                  return _supStack.apply(recurOnBound, infiniteCase, arg, param);
                }
                
                @Override public ConstraintFormula forVariableType(VariableType arg) {
                  ConstraintFormula result = defaultCase(arg);
                  if (!result.isEmpty()) { result = result.or(super.forVariableType(arg)); }
                  return result;
                }
                
                @Override public ConstraintFormula forUnionType(UnionType arg) {
                  ConstraintFormula result = defaultCase(arg);
                  if (!result.isEmpty()) { result = result.or(super.forUnionType(arg)); }
                  return result;
                }
                
              });
            }
          }
          
          @Override public ConstraintFormula forIntersectionType(final IntersectionType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula defaultCase(Type arg) {
                ConstraintFormula result = FALSE;
                for (Type supParam : param.ofTypes()) {
                  result = result.or(supertypeNorm(arg, supParam));
                  if (result.isEmpty()) { break; }
                }
                return result;
              }
              @Override public ConstraintFormula forVariableType(VariableType arg) {
                ConstraintFormula result = defaultCase(arg);
                if (!result.isEmpty()) { result = result.or(super.forVariableType(arg)); }
                return result;
              }
              @Override public ConstraintFormula forUnionType(UnionType arg) { return defaultCase(arg); }
            });
          }
          
          @Override public ConstraintFormula forUnionType(final UnionType param) {
            return arg.apply(new ArgVisitor() {
              @Override public ConstraintFormula defaultCase(Type arg) {
                ConstraintFormula result = TRUE;
                for (Type subParam : param.ofTypes()) {
                  result = result.and(supertypeNorm(arg, subParam));
                  if (!result.isSatisfiable()) { break; }
                }
                return result;
              }
              @Override public ConstraintFormula forVariableType(VariableType arg) { return defaultCase(arg); }
              @Override public ConstraintFormula forIntersectionType(IntersectionType arg) { return defaultCase(arg); }
              @Override public ConstraintFormula forUnionType(UnionType arg) { return defaultCase(arg); }
            });
          }
          
        });
      }
    }
    
    public ConstraintFormula equivalentNorm(final Type arg, final Type param) {
      ConstraintFormula result = subtypeNorm(arg, param);
      if (result.isSatisfiable()) { result = result.and(supertypeNorm(arg, param)); }
      return result;
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
        // wildcards here aren't recursive, so don't need to be handled with a stack,
        // but it doesn't hurt to cover the more general case
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
