package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** A parametric abstract implementation of a visitor over Type that return a value.
 ** This visitor implements the visitor interface with methods that each 
 ** delegate to a case representing their superclass.  At the top of this
 ** delegation tree is the method defaultCase(), which (unless overridden)
 ** throws an exception.
 **/
//@SuppressWarnings("unused")
public abstract class TypeAbstractVisitor<RetType>  extends TypeVisitorLambda<RetType> {
  /**
   * This method is run for all cases that are not handled elsewhere.
   * By default, an exception is thrown; subclasses may override this behavior.
   * @throws IllegalArgumentException
  **/
  public RetType defaultCase(Type that) {
    throw new IllegalArgumentException("Visitor " + getClass().getName() + " does not support visiting values of type " + that.getClass().getName());
  }

  /* Methods to visit an item. */
  public RetType forType(Type that) {
    return defaultCase(that);
  }

  public RetType forValidType(ValidType that) {
    return forType(that);
  }

  public RetType forPrimitiveType(PrimitiveType that) {
    return forValidType(that);
  }

  public RetType forBooleanType(BooleanType that) {
    return forPrimitiveType(that);
  }

  public RetType forNumericType(NumericType that) {
    return forPrimitiveType(that);
  }

  public RetType forIntegralType(IntegralType that) {
    return forNumericType(that);
  }

  public RetType forCharType(CharType that) {
    return forIntegralType(that);
  }

  public RetType forIntegerType(IntegerType that) {
    return forIntegralType(that);
  }

  public RetType forByteType(ByteType that) {
    return forIntegerType(that);
  }

  public RetType forShortType(ShortType that) {
    return forIntegerType(that);
  }

  public RetType forIntType(IntType that) {
    return forIntegerType(that);
  }

  public RetType forLongType(LongType that) {
    return forIntegerType(that);
  }

  public RetType forFloatingPointType(FloatingPointType that) {
    return forNumericType(that);
  }

  public RetType forFloatType(FloatType that) {
    return forFloatingPointType(that);
  }

  public RetType forDoubleType(DoubleType that) {
    return forFloatingPointType(that);
  }

  public RetType forReferenceType(ReferenceType that) {
    return forValidType(that);
  }

  public RetType forNullType(NullType that) {
    return forReferenceType(that);
  }

  public RetType forArrayType(ArrayType that) {
    return forReferenceType(that);
  }

  public RetType forSimpleArrayType(SimpleArrayType that) {
    return forArrayType(that);
  }

  public RetType forVarargArrayType(VarargArrayType that) {
    return forArrayType(that);
  }

  public RetType forClassType(ClassType that) {
    return forReferenceType(that);
  }

  public RetType forSimpleClassType(SimpleClassType that) {
    return forClassType(that);
  }

  public RetType forRawClassType(RawClassType that) {
    return forClassType(that);
  }

  public RetType forParameterizedClassType(ParameterizedClassType that) {
    return forClassType(that);
  }

  public RetType forBoundType(BoundType that) {
    return forValidType(that);
  }

  public RetType forIntersectionType(IntersectionType that) {
    return forBoundType(that);
  }

  public RetType forUnionType(UnionType that) {
    return forBoundType(that);
  }

  public RetType forVariableType(VariableType that) {
    return forValidType(that);
  }

  public RetType forTopType(TopType that) {
    return forValidType(that);
  }

  public RetType forBottomType(BottomType that) {
    return forValidType(that);
  }

  public RetType forVoidType(VoidType that) {
    return forType(that);
  }

  public RetType forWildcard(Wildcard that) {
    return forType(that);
  }


}
