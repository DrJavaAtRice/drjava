package edu.rice.cs.dynamicjava.symbol;

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.recur.PrecomputedRecursionStack;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Wrapper;

import edu.rice.cs.dynamicjava.symbol.type.*;
import edu.rice.cs.dynamicjava.symbol.type.Type; // resolves ambiguity with java.lang.reflect.Type

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** DJClass implementation that wraps a Java 5 reflection Class object. To prevent linkage
  * errors, this class should only be dynamically loaded if it's possible that the target platform
  * does not support the Java 5 APIs.
  */
public class Java5Class extends JavaClass {
  
  public Java5Class(Class<?> c) { super(c); }
  
  @Override public String declaredName() {
    if (_c.isAnonymousClass()) {
      throw new IllegalArgumentException("Anonymous class has no declared name");
    }
    else { return _c.getSimpleName(); }
  }
  
  @Override public Access.Module accessModule() {
    Class<?> result = _c;
    Class<?> outer = result.getEnclosingClass();
    while (outer != null) { result = outer; outer = result.getEnclosingClass(); }
    return new Java5Class(result);
  }

  @Override public DJClass declaringClass() {
    Class<?> outer = _c.getDeclaringClass();
    return (outer == null) ? null : new Java5Class(outer);
  }
  
  /** List all type variables declared by this class (but not by its enclosing classes) */
  @Override public Iterable<VariableType> declaredTypeParameters() {
    return IterUtil.mapSnapshot(IterUtil.asIterable(_c.getTypeParameters()), CONVERT_VAR);
  }
  
  /** List the declared supertypes of this class */
  @Override public Iterable<Type> declaredSupertypes() {
    Type superC = immediateSuperclass();
    Iterable<Type> superIs = IterUtil.mapSnapshot(IterUtil.asIterable(_c.getGenericInterfaces()), CONVERT_TYPE);
    return superC == null ? superIs : IterUtil.compose(superC, superIs);
  }
  
  @Override public Iterable<DJField> declaredFields() {
    // CONVERT_FIELD is shadowed here to create a Java5Field
    return IterUtil.mapSnapshot(IterUtil.asIterable(_c.getDeclaredFields()), CONVERT_FIELD);
  }
  
  @Override public Iterable<DJConstructor> declaredConstructors() {
    // CONVERT_CONSTRUCTOR is shadowed here to create a Java5Constructor
    return IterUtil.mapSnapshot(IterUtil.asIterable(_c.getDeclaredConstructors()), CONVERT_CONSTRUCTOR);
  }
  
  @Override public Iterable<DJMethod> declaredMethods() {
    // CONVERT_METHOD is shadowed here to create a Java5Method
    Iterable<Method> ms = IterUtil.filter(IterUtil.asIterable(_c.getDeclaredMethods()), IS_NOT_BRIDGE);
    return IterUtil.mapSnapshot(ms, CONVERT_METHOD);
  }
  
  private static final Predicate<Method> IS_NOT_BRIDGE = new Predicate<Method>() {
    public boolean contains(Method m) { return !m.isBridge(); }
  };
  
  @Override public Iterable<DJClass> declaredClasses() {
    // CONVERT_CLASS is shadowed here to create a Java5Class
    return IterUtil.mapSnapshot(IterUtil.asIterable(_c.getDeclaredClasses()), CONVERT_CLASS);
  }
  

  /**
   * Return the type bound to {@code super} in the context of this class, or 
   * {@code null} if {@code super} is not defined
   */
  @Override public Type immediateSuperclass() {
    java.lang.reflect.Type superT = _c.getGenericSuperclass();
    return (superT == null) ? null : CONVERT_TYPE.value(superT);
  }
  
  @Override public String toString() { return "Java5Class(" + _c.getName() + ")"; }

  
  private static Type convertType(java.lang.reflect.Type refT, 
                                  PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack) {
    if (refT instanceof Class<?>) { return classAsType((Class<?>) refT); }
    else if (refT instanceof ParameterizedType) { 
      return convertParameterizedType((ParameterizedType) refT, stack);
    }
    else if (refT instanceof java.lang.reflect.TypeVariable<?>) {
      return convertTypeVariable((java.lang.reflect.TypeVariable<?>) refT, stack);
    }
    else if (refT instanceof java.lang.reflect.WildcardType) {
      return convertWildcard((java.lang.reflect.WildcardType) refT, stack);
    }
    else if (refT instanceof GenericArrayType) {
      Type elementType = convertType(((GenericArrayType) refT).getGenericComponentType(), stack);
      return new SimpleArrayType(elementType);
    }
    else { throw new IllegalArgumentException("Unrecognized java.lang.reflect.Type"); }
  }
  
  private static Iterable<Type> convertTypes(java.lang.reflect.Type[] refTs,
                                             final PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack) {
    return IterUtil.mapSnapshot(IterUtil.asIterable(refTs), new Lambda<java.lang.reflect.Type, Type>() {
      public Type value(java.lang.reflect.Type t) { return convertType(t, stack); }
    });
  }
  
  /** Convert a class object to a type */
  private static Type classAsType(Class<?> c) {
    if (c.isPrimitive()) { return SymbolUtil.typeOfPrimitiveClass(c); }
    else if (c.isArray()) { return new SimpleArrayType(classAsType(c.getComponentType())); }
    else {
      DJClass djc = new Java5Class(c);
      // This logic is performed here, rather than deferring to SymbolUtil.allTypeParameters(djc),
      // in order to avoid a potential infinite loop: to create a type for c, we would first have
      // to create the types of the parameters, which may in turn refer to c.
      Class<?> outer = c;
      boolean innerIsStatic = false;
      while (outer != null) {
        if (!innerIsStatic && outer.getTypeParameters().length > 0) { return new RawClassType(djc); }
        innerIsStatic = Modifier.isStatic(outer.getModifiers());
        outer = outer.getDeclaringClass();
      }
      // djc has no type parameters
      return new SimpleClassType(djc);
    }
  }  
    
  private static Type convertParameterizedType(ParameterizedType paramT, 
                                               final PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack) {
    // assumes getRawType and getOwnerType return class types
    ClassType rawT = (ClassType) convertType(paramT.getRawType(), stack);
    ClassType ownerT = (paramT.getOwnerType() == null) ? null : (ClassType) convertType(paramT.getOwnerType(), stack);
    ClassType enclosingT = rawT.ofClass().isStatic() ? SymbolUtil.dynamicOuterClassType(ownerT) : ownerT;
    
    Iterable<? extends Type> outerArgs = IterUtil.empty();
    boolean raw = false; // true iff an enclosing type is raw, and so this type must also be raw
    if (enclosingT != null) {
      Iterable<? extends Type> ts = enclosingT.apply(new TypeAbstractVisitor<Iterable<? extends Type>>() {
        @Override public Iterable<? extends Type> forSimpleClassType(SimpleClassType enclosingT) { 
          return IterUtil.empty();
        }
        @Override public Iterable<? extends Type> forRawClassType(RawClassType enclosingT) { return null; }
        @Override public Iterable<? extends Type> forParameterizedClassType(ParameterizedClassType enclosingT) { 
          return enclosingT.typeArguments();
        }
      });
      if (ts == null) { raw = true; }
      else { outerArgs = ts; }
    }
    
    Iterable<Type> directArgs = convertTypes(paramT.getActualTypeArguments(), stack);
    final Iterable<Type> targs = IterUtil.compose(outerArgs, directArgs);
    Type result = rawT.apply(new TypeAbstractVisitor<Type>() {
      
      public Type defaultCase(Type t) { 
        // We should do this check even if "raw" is true (meaning that we'll return "rawT", not "result")
        throw new IllegalArgumentException("Raw type for ParameterizedType must be a raw class type");
      }
      
      @Override public Type forRawClassType(RawClassType t) {
        return new ParameterizedClassType(t.ofClass(), targs);
      }
      
      @Override public Type forSimpleClassType(SimpleClassType t) {
        if (!IterUtil.isEmpty(targs)) {
          throw new IllegalArgumentException("Type arguments on ParameterizedType are not necessary");
        }
        return t;
      }
      
    });
    return raw ? rawT : result;
  }
  
  private static VariableType convertTypeVariable(final java.lang.reflect.TypeVariable<?> refV, 
                                                  final PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack) {
    final BoundedSymbol bounds = new BoundedSymbol(refV, refV.getName());
    final VariableType var = new VariableType(bounds);
    Thunk<VariableType> setBounds = new Thunk<VariableType>() {
      public VariableType value() {
        Type upper;
        Iterable<Type> uppers = convertTypes(refV.getBounds(), stack);
        if (IterUtil.isEmpty(uppers)) { upper = TypeSystem.OBJECT; }
        else if (IterUtil.sizeOf(uppers) == 1) { upper = IterUtil.first(uppers); }
        else { upper = new IntersectionType(uppers); }
        bounds.initializeUpperBound(upper);
        bounds.initializeLowerBound(TypeSystem.NULL);
        return var;
      }
    };
    return (VariableType) stack.apply(setBounds, var, refV);
  }
  
  private static Type convertWildcard(final java.lang.reflect.WildcardType refW, 
                                      final PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack) {
    final BoundedSymbol bounds = new BoundedSymbol(refW);
    final Wildcard wild = new Wildcard(bounds);
    Thunk<Type> setBounds = new Thunk<Type>() {
      public Type value() {
        Type upper;
        Iterable<Type> uppers = convertTypes(refW.getUpperBounds(), stack);
        if (IterUtil.isEmpty(uppers)) { upper = TypeSystem.OBJECT; }
        else if (IterUtil.sizeOf(uppers) == 1) { upper = IterUtil.first(uppers); }
        else { upper = new IntersectionType(uppers); }
        bounds.initializeUpperBound(upper);
        Type lower;
        Iterable<Type> lowers = convertTypes(refW.getLowerBounds(), stack);
        if (IterUtil.isEmpty(lowers)) { lower = TypeSystem.NULL; }
        else if (IterUtil.sizeOf(lowers) == 1) { lower = IterUtil.first(lowers); }
        else { throw new IllegalArgumentException("Wildcard with multiple lower bounds"); }
        bounds.initializeLowerBound(lower);
        return wild;
      }
    };
    return stack.apply(setBounds, wild, refW);
  }

  private static Lambda<java.lang.reflect.Type, Type> CONVERT_TYPE =
    new Lambda<java.lang.reflect.Type, Type>() {
    public Type value(java.lang.reflect.Type t) {
      // must use .equals, not ==: see java java.lang.reflect.TypeVariable javadocs
      PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack =
        PrecomputedRecursionStack.make(Wrapper.<java.lang.reflect.Type>factory());
      return convertType(t, stack);
    }
  };
  
  
  private static final Lambda<TypeVariable<?>, VariableType> CONVERT_VAR =
    new Lambda<TypeVariable<?>, VariableType>() {
    public VariableType value(TypeVariable<?> var) {
      // must use .equals, not ==: see java java.lang.reflect.TypeVariable javadocs
      PrecomputedRecursionStack<java.lang.reflect.Type, Type> stack =
        PrecomputedRecursionStack.make(Wrapper.<java.lang.reflect.Type>factory());
      return convertTypeVariable(var, stack);
    }
  };
  
  @SuppressWarnings("unchecked") // java.lang.Class methods return (raw) type Class[] in Java 5 (fixed in Java 6)
  private static final Lambda<Class, DJClass> CONVERT_CLASS = new Lambda<Class, DJClass>() {
    public DJClass value(Class c) { return new Java5Class(c); }
  };
  
  /** Non-static because Java5Field is non-static. */
  private final Lambda<Field, DJField> CONVERT_FIELD = new Lambda<Field, DJField>() {
    public DJField value(Field f) { return new Java5Field(f); }
  };
  
  /** Non-static because Java5Constructor is non-static. */
  @SuppressWarnings("unchecked") // java.lang.Class methods return (raw) type Constructor[] in Java 5 (fixed in Java 6)
  private final Lambda<Constructor, DJConstructor> CONVERT_CONSTRUCTOR =
    new Lambda<Constructor, DJConstructor>() {
    public DJConstructor value(Constructor k) { return new Java5Constructor(k); }
  };
  
  /** Non-static because Java5Method is non-static. */
  private final Lambda<Method, DJMethod> CONVERT_METHOD = new Lambda<Method, DJMethod>() {
    public DJMethod value(Method m) { return new Java5Method(m); }
  };

  private class Java5Field extends JavaField {
    public Java5Field(Field f) { super(f); }
    @Override public Type type() { return CONVERT_TYPE.value(_f.getGenericType()); }
    @Override public String toString() { return "Java5Field(" + declaredName() + ")"; }
  }
  
  private class Java5Constructor extends JavaConstructor {
    public Java5Constructor(Constructor<?> k) { super(k); }
    @Override public Iterable<VariableType> typeParameters() {
      return IterUtil.mapSnapshot(IterUtil.asIterable(_k.getTypeParameters()), CONVERT_VAR);
    }
    @Override public Iterable<Type> thrownTypes() {
      return IterUtil.mapSnapshot(IterUtil.asIterable(_k.getGenericExceptionTypes()), CONVERT_TYPE);
    }
    protected Thunk<Iterable<LocalVariable>> makeParamThunk() {
      return paramFactory(_k.getGenericParameterTypes(), _k.isVarArgs());
    }
    @Override public String toString() { return "Java5Constructor(" + declaredName() + ")"; }
  }

  private class Java5Method extends JavaMethod {
    public Java5Method(Method m) { super(m); }
    @Override public Type returnType() { return CONVERT_TYPE.value(_m.getGenericReturnType()); }
    @Override public Iterable<VariableType> typeParameters() {
      return IterUtil.mapSnapshot(IterUtil.asIterable(_m.getTypeParameters()), CONVERT_VAR);
    }
    @Override public Iterable<Type> thrownTypes() {
      return IterUtil.mapSnapshot(IterUtil.asIterable(_m.getGenericExceptionTypes()), CONVERT_TYPE);
    }
    protected Thunk<Iterable<LocalVariable>> makeParamThunk() {
      return paramFactory(_m.getGenericParameterTypes(), _m.isVarArgs());
    }
    @Override public String toString() { return "Java5Method(" + declaredName() + ")"; }
  }
  
  private static Thunk<Iterable<LocalVariable>> paramFactory(final java.lang.reflect.Type[] ts,
                                                             final boolean isVarargs) {
    return LazyThunk.make(new Thunk<Iterable<LocalVariable>>() {
      public Iterable<LocalVariable> value() {
        List<LocalVariable> result = new ArrayList<LocalVariable>(ts.length);
        // TODO: can we access better information about the parameters -- names, final declarations?
        for (int i = 0; i < ts.length; i++) {
          Type t = CONVERT_TYPE.value(ts[i]);
          if (isVarargs && i == ts.length-1 && t instanceof SimpleArrayType) {
            t = new VarargArrayType(((SimpleArrayType) t).ofType());
          }
          result.add(new LocalVariable("a" + (i+1), t, false));
        }
        // Must wrap result in something that implements Iterable for Retroweaver compatibility:
        // normally, it's not a problem; but erasure inserts casts to the thunk's parameter type,
        // is translated to the Retroweaver Iterable type.
        return IterUtil.asSizedIterable(result);
      }
    });
  }

}
