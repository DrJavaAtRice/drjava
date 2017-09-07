package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** An abstract implementation of a visitor over Type that does not return a value.
 ** This visitor implements the visitor interface with methods that each 
 ** delegate to a case representing their superclass.  At the top of this
 ** delegation tree is the method defaultCase(), which (unless overridden)
 ** is a no-op.
 **/
//@SuppressWarnings("unused")
public class TypeAbstractVisitor_void extends TypeVisitorRunnable1 {
  /**
   * This method is run for all cases that are not handled elsewhere.
   * By default, it is a no-op; subclasses may override this behavior.
  **/
  public void defaultCase(Type that) {}

  /* Methods to visit an item. */
  public void forType(Type that) {
    defaultCase(that);
  }

  public void forValidType(ValidType that) {
    forType(that);
  }

  public void forPrimitiveType(PrimitiveType that) {
    forValidType(that);
  }

  public void forBooleanType(BooleanType that) {
    forPrimitiveType(that);
  }

  public void forNumericType(NumericType that) {
    forPrimitiveType(that);
  }

  public void forIntegralType(IntegralType that) {
    forNumericType(that);
  }

  public void forCharType(CharType that) {
    forIntegralType(that);
  }

  public void forIntegerType(IntegerType that) {
    forIntegralType(that);
  }

  public void forByteType(ByteType that) {
    forIntegerType(that);
  }

  public void forShortType(ShortType that) {
    forIntegerType(that);
  }

  public void forIntType(IntType that) {
    forIntegerType(that);
  }

  public void forLongType(LongType that) {
    forIntegerType(that);
  }

  public void forFloatingPointType(FloatingPointType that) {
    forNumericType(that);
  }

  public void forFloatType(FloatType that) {
    forFloatingPointType(that);
  }

  public void forDoubleType(DoubleType that) {
    forFloatingPointType(that);
  }

  public void forReferenceType(ReferenceType that) {
    forValidType(that);
  }

  public void forNullType(NullType that) {
    forReferenceType(that);
  }

  public void forArrayType(ArrayType that) {
    forReferenceType(that);
  }

  public void forSimpleArrayType(SimpleArrayType that) {
    forArrayType(that);
  }

  public void forVarargArrayType(VarargArrayType that) {
    forArrayType(that);
  }

  public void forClassType(ClassType that) {
    forReferenceType(that);
  }

  public void forSimpleClassType(SimpleClassType that) {
    forClassType(that);
  }

  public void forRawClassType(RawClassType that) {
    forClassType(that);
  }

  public void forParameterizedClassType(ParameterizedClassType that) {
    forClassType(that);
  }

  public void forBoundType(BoundType that) {
    forValidType(that);
  }

  public void forIntersectionType(IntersectionType that) {
    forBoundType(that);
  }

  public void forUnionType(UnionType that) {
    forBoundType(that);
  }

  public void forVariableType(VariableType that) {
    forValidType(that);
  }

  public void forTopType(TopType that) {
    forValidType(that);
  }

  public void forBottomType(BottomType that) {
    forValidType(that);
  }

  public void forVoidType(VoidType that) {
    forType(that);
  }

  public void forWildcard(Wildcard that) {
    forType(that);
  }

}
