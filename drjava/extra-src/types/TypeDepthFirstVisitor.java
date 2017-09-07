package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** A parametric abstract implementation of a visitor over Type that returns a value.
 ** This visitor implements the visitor interface with methods that 
 ** first visit children, and then call forCASEOnly(), passing in 
 ** the values of the visits of the children. (CASE is replaced by the case name.)
 ** By default, each of forCASEOnly delegates to a more general case; at the
 ** top of this delegation tree is defaultCase(), which (unless overridden)
 ** throws an exception.
 **/
//@SuppressWarnings("unused")
public abstract class TypeDepthFirstVisitor<RetType> extends TypeVisitorLambda<RetType> {
  /**
   * This method is run for all cases that are not handled elsewhere.
   * By default, an exception is thrown; subclasses may override this behavior.
   * @throws IllegalArgumentException
  **/
  public RetType defaultCase(Type that) {
    throw new IllegalArgumentException("Visitor " + getClass().getName() + " does not support visiting values of type " + that.getClass().getName());
  }

  /* Methods to handle a node after recursion. */
  public RetType forTypeOnly(Type that) {
    return defaultCase(that);
  }

  public RetType forValidTypeOnly(ValidType that) {
    return forTypeOnly(that);
  }

  public RetType forPrimitiveTypeOnly(PrimitiveType that) {
    return forValidTypeOnly(that);
  }

  public RetType forBooleanTypeOnly(BooleanType that) {
    return forPrimitiveTypeOnly(that);
  }

  public RetType forNumericTypeOnly(NumericType that) {
    return forPrimitiveTypeOnly(that);
  }

  public RetType forIntegralTypeOnly(IntegralType that) {
    return forNumericTypeOnly(that);
  }

  public RetType forCharTypeOnly(CharType that) {
    return forIntegralTypeOnly(that);
  }

  public RetType forIntegerTypeOnly(IntegerType that) {
    return forIntegralTypeOnly(that);
  }

  public RetType forByteTypeOnly(ByteType that) {
    return forIntegerTypeOnly(that);
  }

  public RetType forShortTypeOnly(ShortType that) {
    return forIntegerTypeOnly(that);
  }

  public RetType forIntTypeOnly(IntType that) {
    return forIntegerTypeOnly(that);
  }

  public RetType forLongTypeOnly(LongType that) {
    return forIntegerTypeOnly(that);
  }

  public RetType forFloatingPointTypeOnly(FloatingPointType that) {
    return forNumericTypeOnly(that);
  }

  public RetType forFloatTypeOnly(FloatType that) {
    return forFloatingPointTypeOnly(that);
  }

  public RetType forDoubleTypeOnly(DoubleType that) {
    return forFloatingPointTypeOnly(that);
  }

  public RetType forReferenceTypeOnly(ReferenceType that) {
    return forValidTypeOnly(that);
  }

  public RetType forNullTypeOnly(NullType that) {
    return forReferenceTypeOnly(that);
  }

  public RetType forArrayTypeOnly(ArrayType that, RetType ofType_result) {
    return forReferenceTypeOnly(that);
  }

  public RetType forSimpleArrayTypeOnly(SimpleArrayType that, RetType ofType_result) {
    return forArrayTypeOnly(that, ofType_result);
  }

  public RetType forVarargArrayTypeOnly(VarargArrayType that, RetType ofType_result) {
    return forArrayTypeOnly(that, ofType_result);
  }

  public RetType forClassTypeOnly(ClassType that) {
    return forReferenceTypeOnly(that);
  }

  public RetType forSimpleClassTypeOnly(SimpleClassType that) {
    return forClassTypeOnly(that);
  }

  public RetType forRawClassTypeOnly(RawClassType that) {
    return forClassTypeOnly(that);
  }

  public RetType forParameterizedClassTypeOnly(ParameterizedClassType that, Iterable<RetType> typeArguments_result) {
    return forClassTypeOnly(that);
  }

  public RetType forBoundTypeOnly(BoundType that, Iterable<RetType> ofTypes_result) {
    return forValidTypeOnly(that);
  }

  public RetType forIntersectionTypeOnly(IntersectionType that, Iterable<RetType> ofTypes_result) {
    return forBoundTypeOnly(that, ofTypes_result);
  }

  public RetType forUnionTypeOnly(UnionType that, Iterable<RetType> ofTypes_result) {
    return forBoundTypeOnly(that, ofTypes_result);
  }

  public RetType forVariableTypeOnly(VariableType that) {
    return forValidTypeOnly(that);
  }

  public RetType forTopTypeOnly(TopType that) {
    return forValidTypeOnly(that);
  }

  public RetType forBottomTypeOnly(BottomType that) {
    return forValidTypeOnly(that);
  }

  public RetType forVoidTypeOnly(VoidType that) {
    return forTypeOnly(that);
  }

  public RetType forWildcardOnly(Wildcard that) {
    return forTypeOnly(that);
  }


  /** Methods to recur on each child. */
  public RetType forBooleanType(BooleanType that) {
    return forBooleanTypeOnly(that);
  }

  public RetType forCharType(CharType that) {
    return forCharTypeOnly(that);
  }

  public RetType forByteType(ByteType that) {
    return forByteTypeOnly(that);
  }

  public RetType forShortType(ShortType that) {
    return forShortTypeOnly(that);
  }

  public RetType forIntType(IntType that) {
    return forIntTypeOnly(that);
  }

  public RetType forLongType(LongType that) {
    return forLongTypeOnly(that);
  }

  public RetType forFloatType(FloatType that) {
    return forFloatTypeOnly(that);
  }

  public RetType forDoubleType(DoubleType that) {
    return forDoubleTypeOnly(that);
  }

  public RetType forNullType(NullType that) {
    return forNullTypeOnly(that);
  }

  public RetType forSimpleArrayType(SimpleArrayType that) {
    RetType ofType_result = recur(that.ofType());
    return forSimpleArrayTypeOnly(that, ofType_result);
  }

  public RetType forVarargArrayType(VarargArrayType that) {
    RetType ofType_result = recur(that.ofType());
    return forVarargArrayTypeOnly(that, ofType_result);
  }

  public RetType forSimpleClassType(SimpleClassType that) {
    return forSimpleClassTypeOnly(that);
  }

  public RetType forRawClassType(RawClassType that) {
    return forRawClassTypeOnly(that);
  }

  public RetType forParameterizedClassType(ParameterizedClassType that) {
    Iterable<RetType> typeArguments_result = recurOnIterableOfWildcardExtendsType(that.typeArguments());
    return forParameterizedClassTypeOnly(that, typeArguments_result);
  }

  public RetType forIntersectionType(IntersectionType that) {
    Iterable<RetType> ofTypes_result = recurOnIterableOfWildcardExtendsType(that.ofTypes());
    return forIntersectionTypeOnly(that, ofTypes_result);
  }

  public RetType forUnionType(UnionType that) {
    Iterable<RetType> ofTypes_result = recurOnIterableOfWildcardExtendsType(that.ofTypes());
    return forUnionTypeOnly(that, ofTypes_result);
  }

  public RetType forVariableType(VariableType that) {
    return forVariableTypeOnly(that);
  }

  public RetType forTopType(TopType that) {
    return forTopTypeOnly(that);
  }

  public RetType forBottomType(BottomType that) {
    return forBottomTypeOnly(that);
  }

  public RetType forVoidType(VoidType that) {
    return forVoidTypeOnly(that);
  }

  public RetType forWildcard(Wildcard that) {
    return forWildcardOnly(that);
  }


  public RetType recur(Type that) {
    return that.apply(this);
  }

  public Iterable<RetType> recurOnIterableOfWildcardExtendsType(Iterable<? extends Type> that) {
    java.util.ArrayList<RetType> accum = new java.util.ArrayList<RetType>();
    for (Type elt : that) {
      accum.add(recur(elt));
    }
    return accum;
  }
}
