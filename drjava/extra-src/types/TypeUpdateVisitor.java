package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** 
 * A depth-first visitor that makes an updated copy as it visits (by default).
 * The type of the result is generally the same as that of the argument; where
 * automatic recursion on a field of type T occurs, this must be true for T.
 * Where no changes are made to a node, a new copy is not allocated.
 * This visitor implements the visitor interface with methods that 
 * first update the children, and then call forCASEOnly(), passing in 
 * the values of the updated children. (CASE is replaced by the case name.)
 * Override forCASE or forCASEOnly if you want to transform an AST subtree.
 * There is no automatic delegation to more general cases, because each concrete
 * case has a default implementation.
 */
//@SuppressWarnings("unused")
public abstract class TypeUpdateVisitor extends TypeVisitorLambda<Type> {

  /* Methods to handle a node after recursion. */

  public Type forBooleanTypeOnly(BooleanType that) {
    return that;
  }

  public Type forCharTypeOnly(CharType that) {
    return that;
  }

  public Type forByteTypeOnly(ByteType that) {
    return that;
  }

  public Type forShortTypeOnly(ShortType that) {
    return that;
  }

  public Type forIntTypeOnly(IntType that) {
    return that;
  }

  public Type forLongTypeOnly(LongType that) {
    return that;
  }

  public Type forFloatTypeOnly(FloatType that) {
    return that;
  }

  public Type forDoubleTypeOnly(DoubleType that) {
    return that;
  }

  public Type forNullTypeOnly(NullType that) {
    return that;
  }

  public Type forSimpleArrayTypeOnly(SimpleArrayType that, Type ofType_result) {
    if (that.ofType() == ofType_result) return that;
    else return new SimpleArrayType(ofType_result);
  }

  public Type forVarargArrayTypeOnly(VarargArrayType that, Type ofType_result) {
    if (that.ofType() == ofType_result) return that;
    else return new VarargArrayType(ofType_result);
  }

  public Type forSimpleClassTypeOnly(SimpleClassType that) {
    return that;
  }

  public Type forRawClassTypeOnly(RawClassType that) {
    return that;
  }

  public Type forParameterizedClassTypeOnly(ParameterizedClassType that, Iterable<? extends Type> typeArguments_result) {
    if (that.typeArguments() == typeArguments_result) return that;
    else return new ParameterizedClassType(that.ofClass(), typeArguments_result);
  }

  public Type forIntersectionTypeOnly(IntersectionType that, Iterable<? extends Type> ofTypes_result) {
    if (that.ofTypes() == ofTypes_result) return that;
    else return new IntersectionType(ofTypes_result);
  }

  public Type forUnionTypeOnly(UnionType that, Iterable<? extends Type> ofTypes_result) {
    if (that.ofTypes() == ofTypes_result) return that;
    else return new UnionType(ofTypes_result);
  }

  public Type forVariableTypeOnly(VariableType that) {
    return that;
  }

  public Type forTopTypeOnly(TopType that) {
    return that;
  }

  public Type forBottomTypeOnly(BottomType that) {
    return that;
  }

  public Type forVoidTypeOnly(VoidType that) {
    return that;
  }

  public Type forWildcardOnly(Wildcard that) {
    return that;
  }

  /** Methods to recur on each child. */

  public Type forBooleanType(BooleanType that) {
    return forBooleanTypeOnly(that);
  }


  public Type forCharType(CharType that) {
    return forCharTypeOnly(that);
  }


  public Type forByteType(ByteType that) {
    return forByteTypeOnly(that);
  }


  public Type forShortType(ShortType that) {
    return forShortTypeOnly(that);
  }


  public Type forIntType(IntType that) {
    return forIntTypeOnly(that);
  }


  public Type forLongType(LongType that) {
    return forLongTypeOnly(that);
  }


  public Type forFloatType(FloatType that) {
    return forFloatTypeOnly(that);
  }


  public Type forDoubleType(DoubleType that) {
    return forDoubleTypeOnly(that);
  }


  public Type forNullType(NullType that) {
    return forNullTypeOnly(that);
  }


  public Type forSimpleArrayType(SimpleArrayType that) {
    Type ofType_result = recur(that.ofType());
    return forSimpleArrayTypeOnly(that, ofType_result);
  }


  public Type forVarargArrayType(VarargArrayType that) {
    Type ofType_result = recur(that.ofType());
    return forVarargArrayTypeOnly(that, ofType_result);
  }


  public Type forSimpleClassType(SimpleClassType that) {
    return forSimpleClassTypeOnly(that);
  }


  public Type forRawClassType(RawClassType that) {
    return forRawClassTypeOnly(that);
  }


  public Type forParameterizedClassType(ParameterizedClassType that) {
    Iterable<? extends Type> typeArguments_result = recurOnIterableOfWildcardExtendsType(that.typeArguments());
    return forParameterizedClassTypeOnly(that, typeArguments_result);
  }


  public Type forIntersectionType(IntersectionType that) {
    Iterable<? extends Type> ofTypes_result = recurOnIterableOfWildcardExtendsType(that.ofTypes());
    return forIntersectionTypeOnly(that, ofTypes_result);
  }


  public Type forUnionType(UnionType that) {
    Iterable<? extends Type> ofTypes_result = recurOnIterableOfWildcardExtendsType(that.ofTypes());
    return forUnionTypeOnly(that, ofTypes_result);
  }


  public Type forVariableType(VariableType that) {
    return forVariableTypeOnly(that);
  }


  public Type forTopType(TopType that) {
    return forTopTypeOnly(that);
  }


  public Type forBottomType(BottomType that) {
    return forBottomTypeOnly(that);
  }


  public Type forVoidType(VoidType that) {
    return forVoidTypeOnly(that);
  }


  public Type forWildcard(Wildcard that) {
    return forWildcardOnly(that);
  }


  public Type recur(Type that) {
    return that.apply(this);
  }

  public Iterable<? extends Type> recurOnIterableOfWildcardExtendsType(Iterable<? extends Type> that) {
    java.util.ArrayList<Type> accum = new java.util.ArrayList<Type>();
    boolean unchanged = true;
    for (Type elt : that) {
      Type update_elt = recur(elt);
      unchanged &= (elt == update_elt);
      accum.add(update_elt);
    }
    return unchanged ? that : accum;
  }
}
