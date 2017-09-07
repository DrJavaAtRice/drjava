package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** An abstract visitor over Type that provides a lambda value method;
  * all visit methods are left unimplemented. */
//@SuppressWarnings("unused")
public abstract class TypeVisitorLambda<RetType> implements edu.rice.cs.plt.lambda.Lambda<Type, RetType>, TypeVisitor<RetType> {

  public RetType value(Type that) {
    return that.apply(this);
  }


  /** Process an instance of BooleanType. */
  public abstract RetType forBooleanType(BooleanType that);

  /** Process an instance of CharType. */
  public abstract RetType forCharType(CharType that);

  /** Process an instance of ByteType. */
  public abstract RetType forByteType(ByteType that);

  /** Process an instance of ShortType. */
  public abstract RetType forShortType(ShortType that);

  /** Process an instance of IntType. */
  public abstract RetType forIntType(IntType that);

  /** Process an instance of LongType. */
  public abstract RetType forLongType(LongType that);

  /** Process an instance of FloatType. */
  public abstract RetType forFloatType(FloatType that);

  /** Process an instance of DoubleType. */
  public abstract RetType forDoubleType(DoubleType that);

  /** Process an instance of NullType. */
  public abstract RetType forNullType(NullType that);

  /** Process an instance of SimpleArrayType. */
  public abstract RetType forSimpleArrayType(SimpleArrayType that);

  /** Process an instance of VarargArrayType. */
  public abstract RetType forVarargArrayType(VarargArrayType that);

  /** Process an instance of SimpleClassType. */
  public abstract RetType forSimpleClassType(SimpleClassType that);

  /** Process an instance of RawClassType. */
  public abstract RetType forRawClassType(RawClassType that);

  /** Process an instance of ParameterizedClassType. */
  public abstract RetType forParameterizedClassType(ParameterizedClassType that);

  /** Process an instance of IntersectionType. */
  public abstract RetType forIntersectionType(IntersectionType that);

  /** Process an instance of UnionType. */
  public abstract RetType forUnionType(UnionType that);

  /** Process an instance of VariableType. */
  public abstract RetType forVariableType(VariableType that);

  /** Process an instance of TopType. */
  public abstract RetType forTopType(TopType that);

  /** Process an instance of BottomType. */
  public abstract RetType forBottomType(BottomType that);

  /** Process an instance of VoidType. */
  public abstract RetType forVoidType(VoidType that);

  /** Process an instance of Wildcard. */
  public abstract RetType forWildcard(Wildcard that);
}
