package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** An abstract implementation of a visitor over Type that does not return a value.
 ** This visitor implements the visitor interface with methods that 
 ** first call forCASEDoFirst(), second visit the children, and finally 
 ** call forCASEOnly().  (CASE is replaced by the case name.)
 ** By default, each of forCASEDoFirst and forCASEOnly delegates
 ** to a more general case.  At the top of this delegation tree are
 ** defaultDoFirst() and defaultCase(), respectively, which (unless
 ** overridden) are no-ops.
 **/
//@SuppressWarnings("unused")
public class TypeDepthFirstVisitor_void extends TypeVisitorRunnable1 {
  /**
   * This method is run for all cases that are not handled elsewhere.
   * By default, it is a no-op; subclasses may override this behavior.
  **/
  public void defaultCase(Type that) {}

  /**
   * This method is run for all DoFirst cases that are not handled elsewhere.
   * By default, it is a no-op; subclasses may override this behavior.
  **/
  public void defaultDoFirst(Type that) {}

  /* Methods to handle a node before recursion. */
  public void forTypeDoFirst(Type that) {
    defaultDoFirst(that);
  }

  public void forValidTypeDoFirst(ValidType that) {
    forTypeDoFirst(that);
  }

  public void forPrimitiveTypeDoFirst(PrimitiveType that) {
    forValidTypeDoFirst(that);
  }

  public void forBooleanTypeDoFirst(BooleanType that) {
    forPrimitiveTypeDoFirst(that);
  }

  public void forNumericTypeDoFirst(NumericType that) {
    forPrimitiveTypeDoFirst(that);
  }

  public void forIntegralTypeDoFirst(IntegralType that) {
    forNumericTypeDoFirst(that);
  }

  public void forCharTypeDoFirst(CharType that) {
    forIntegralTypeDoFirst(that);
  }

  public void forIntegerTypeDoFirst(IntegerType that) {
    forIntegralTypeDoFirst(that);
  }

  public void forByteTypeDoFirst(ByteType that) {
    forIntegerTypeDoFirst(that);
  }

  public void forShortTypeDoFirst(ShortType that) {
    forIntegerTypeDoFirst(that);
  }

  public void forIntTypeDoFirst(IntType that) {
    forIntegerTypeDoFirst(that);
  }

  public void forLongTypeDoFirst(LongType that) {
    forIntegerTypeDoFirst(that);
  }

  public void forFloatingPointTypeDoFirst(FloatingPointType that) {
    forNumericTypeDoFirst(that);
  }

  public void forFloatTypeDoFirst(FloatType that) {
    forFloatingPointTypeDoFirst(that);
  }

  public void forDoubleTypeDoFirst(DoubleType that) {
    forFloatingPointTypeDoFirst(that);
  }

  public void forReferenceTypeDoFirst(ReferenceType that) {
    forValidTypeDoFirst(that);
  }

  public void forNullTypeDoFirst(NullType that) {
    forReferenceTypeDoFirst(that);
  }

  public void forArrayTypeDoFirst(ArrayType that) {
    forReferenceTypeDoFirst(that);
  }

  public void forSimpleArrayTypeDoFirst(SimpleArrayType that) {
    forArrayTypeDoFirst(that);
  }

  public void forVarargArrayTypeDoFirst(VarargArrayType that) {
    forArrayTypeDoFirst(that);
  }

  public void forClassTypeDoFirst(ClassType that) {
    forReferenceTypeDoFirst(that);
  }

  public void forSimpleClassTypeDoFirst(SimpleClassType that) {
    forClassTypeDoFirst(that);
  }

  public void forRawClassTypeDoFirst(RawClassType that) {
    forClassTypeDoFirst(that);
  }

  public void forParameterizedClassTypeDoFirst(ParameterizedClassType that) {
    forClassTypeDoFirst(that);
  }

  public void forBoundTypeDoFirst(BoundType that) {
    forValidTypeDoFirst(that);
  }

  public void forIntersectionTypeDoFirst(IntersectionType that) {
    forBoundTypeDoFirst(that);
  }

  public void forUnionTypeDoFirst(UnionType that) {
    forBoundTypeDoFirst(that);
  }

  public void forVariableTypeDoFirst(VariableType that) {
    forValidTypeDoFirst(that);
  }

  public void forTopTypeDoFirst(TopType that) {
    forValidTypeDoFirst(that);
  }

  public void forBottomTypeDoFirst(BottomType that) {
    forValidTypeDoFirst(that);
  }

  public void forVoidTypeDoFirst(VoidType that) {
    forTypeDoFirst(that);
  }

  public void forWildcardDoFirst(Wildcard that) {
    forTypeDoFirst(that);
  }

  /* Methods to handle a node after recursion. */
  public void forTypeOnly(Type that) {
    defaultCase(that);
  }

  public void forValidTypeOnly(ValidType that) {
    forTypeOnly(that);
  }

  public void forPrimitiveTypeOnly(PrimitiveType that) {
    forValidTypeOnly(that);
  }

  public void forBooleanTypeOnly(BooleanType that) {
    forPrimitiveTypeOnly(that);
  }

  public void forNumericTypeOnly(NumericType that) {
    forPrimitiveTypeOnly(that);
  }

  public void forIntegralTypeOnly(IntegralType that) {
    forNumericTypeOnly(that);
  }

  public void forCharTypeOnly(CharType that) {
    forIntegralTypeOnly(that);
  }

  public void forIntegerTypeOnly(IntegerType that) {
    forIntegralTypeOnly(that);
  }

  public void forByteTypeOnly(ByteType that) {
    forIntegerTypeOnly(that);
  }

  public void forShortTypeOnly(ShortType that) {
    forIntegerTypeOnly(that);
  }

  public void forIntTypeOnly(IntType that) {
    forIntegerTypeOnly(that);
  }

  public void forLongTypeOnly(LongType that) {
    forIntegerTypeOnly(that);
  }

  public void forFloatingPointTypeOnly(FloatingPointType that) {
    forNumericTypeOnly(that);
  }

  public void forFloatTypeOnly(FloatType that) {
    forFloatingPointTypeOnly(that);
  }

  public void forDoubleTypeOnly(DoubleType that) {
    forFloatingPointTypeOnly(that);
  }

  public void forReferenceTypeOnly(ReferenceType that) {
    forValidTypeOnly(that);
  }

  public void forNullTypeOnly(NullType that) {
    forReferenceTypeOnly(that);
  }

  public void forArrayTypeOnly(ArrayType that) {
    forReferenceTypeOnly(that);
  }

  public void forSimpleArrayTypeOnly(SimpleArrayType that) {
    forArrayTypeOnly(that);
  }

  public void forVarargArrayTypeOnly(VarargArrayType that) {
    forArrayTypeOnly(that);
  }

  public void forClassTypeOnly(ClassType that) {
    forReferenceTypeOnly(that);
  }

  public void forSimpleClassTypeOnly(SimpleClassType that) {
    forClassTypeOnly(that);
  }

  public void forRawClassTypeOnly(RawClassType that) {
    forClassTypeOnly(that);
  }

  public void forParameterizedClassTypeOnly(ParameterizedClassType that) {
    forClassTypeOnly(that);
  }

  public void forBoundTypeOnly(BoundType that) {
    forValidTypeOnly(that);
  }

  public void forIntersectionTypeOnly(IntersectionType that) {
    forBoundTypeOnly(that);
  }

  public void forUnionTypeOnly(UnionType that) {
    forBoundTypeOnly(that);
  }

  public void forVariableTypeOnly(VariableType that) {
    forValidTypeOnly(that);
  }

  public void forTopTypeOnly(TopType that) {
    forValidTypeOnly(that);
  }

  public void forBottomTypeOnly(BottomType that) {
    forValidTypeOnly(that);
  }

  public void forVoidTypeOnly(VoidType that) {
    forTypeOnly(that);
  }

  public void forWildcardOnly(Wildcard that) {
    forTypeOnly(that);
  }

  /* Methods to recur on each child. */
  public void forBooleanType(BooleanType that) {
    forBooleanTypeDoFirst(that);
    forBooleanTypeOnly(that);
  }

  public void forCharType(CharType that) {
    forCharTypeDoFirst(that);
    forCharTypeOnly(that);
  }

  public void forByteType(ByteType that) {
    forByteTypeDoFirst(that);
    forByteTypeOnly(that);
  }

  public void forShortType(ShortType that) {
    forShortTypeDoFirst(that);
    forShortTypeOnly(that);
  }

  public void forIntType(IntType that) {
    forIntTypeDoFirst(that);
    forIntTypeOnly(that);
  }

  public void forLongType(LongType that) {
    forLongTypeDoFirst(that);
    forLongTypeOnly(that);
  }

  public void forFloatType(FloatType that) {
    forFloatTypeDoFirst(that);
    forFloatTypeOnly(that);
  }

  public void forDoubleType(DoubleType that) {
    forDoubleTypeDoFirst(that);
    forDoubleTypeOnly(that);
  }

  public void forNullType(NullType that) {
    forNullTypeDoFirst(that);
    forNullTypeOnly(that);
  }

  public void forSimpleArrayType(SimpleArrayType that) {
    forSimpleArrayTypeDoFirst(that);
    recur(that.ofType());
    forSimpleArrayTypeOnly(that);
  }

  public void forVarargArrayType(VarargArrayType that) {
    forVarargArrayTypeDoFirst(that);
    recur(that.ofType());
    forVarargArrayTypeOnly(that);
  }

  public void forSimpleClassType(SimpleClassType that) {
    forSimpleClassTypeDoFirst(that);
    forSimpleClassTypeOnly(that);
  }

  public void forRawClassType(RawClassType that) {
    forRawClassTypeDoFirst(that);
    forRawClassTypeOnly(that);
  }

  public void forParameterizedClassType(ParameterizedClassType that) {
    forParameterizedClassTypeDoFirst(that);
    recurOnIterableOfWildcardExtendsType(that.typeArguments());
    forParameterizedClassTypeOnly(that);
  }

  public void forIntersectionType(IntersectionType that) {
    forIntersectionTypeDoFirst(that);
    recurOnIterableOfWildcardExtendsType(that.ofTypes());
    forIntersectionTypeOnly(that);
  }

  public void forUnionType(UnionType that) {
    forUnionTypeDoFirst(that);
    recurOnIterableOfWildcardExtendsType(that.ofTypes());
    forUnionTypeOnly(that);
  }

  public void forVariableType(VariableType that) {
    forVariableTypeDoFirst(that);
    forVariableTypeOnly(that);
  }

  public void forTopType(TopType that) {
    forTopTypeDoFirst(that);
    forTopTypeOnly(that);
  }

  public void forBottomType(BottomType that) {
    forBottomTypeDoFirst(that);
    forBottomTypeOnly(that);
  }

  public void forVoidType(VoidType that) {
    forVoidTypeDoFirst(that);
    forVoidTypeOnly(that);
  }

  public void forWildcard(Wildcard that) {
    forWildcardDoFirst(that);
    forWildcardOnly(that);
  }


  public void recur(Type that) {
    that.apply(this);
  }

  public void recurOnIterableOfWildcardExtendsType(Iterable<? extends Type> that) {
    for (Type elt : that) {
      recur(elt);
    }
  }
}
