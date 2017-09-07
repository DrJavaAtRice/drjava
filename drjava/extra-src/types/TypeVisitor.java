package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** A parametric interface for visitors over Type that return a value. */
//@SuppressWarnings("unused")
public interface TypeVisitor<RetType> {

  /** Process an instance of BooleanType. */
  public RetType forBooleanType(BooleanType that);

  /** Process an instance of CharType. */
  public RetType forCharType(CharType that);

  /** Process an instance of ByteType. */
  public RetType forByteType(ByteType that);

  /** Process an instance of ShortType. */
  public RetType forShortType(ShortType that);

  /** Process an instance of IntType. */
  public RetType forIntType(IntType that);

  /** Process an instance of LongType. */
  public RetType forLongType(LongType that);

  /** Process an instance of FloatType. */
  public RetType forFloatType(FloatType that);

  /** Process an instance of DoubleType. */
  public RetType forDoubleType(DoubleType that);

  /** Process an instance of NullType. */
  public RetType forNullType(NullType that);

  /** Process an instance of SimpleArrayType. */
  public RetType forSimpleArrayType(SimpleArrayType that);

  /** Process an instance of VarargArrayType. */
  public RetType forVarargArrayType(VarargArrayType that);

  /** Process an instance of SimpleClassType. */
  public RetType forSimpleClassType(SimpleClassType that);

  /** Process an instance of RawClassType. */
  public RetType forRawClassType(RawClassType that);

  /** Process an instance of ParameterizedClassType. */
  public RetType forParameterizedClassType(ParameterizedClassType that);

  /** Process an instance of IntersectionType. */
  public RetType forIntersectionType(IntersectionType that);

  /** Process an instance of UnionType. */
  public RetType forUnionType(UnionType that);

  /** Process an instance of VariableType. */
  public RetType forVariableType(VariableType that);

  /** Process an instance of TopType. */
  public RetType forTopType(TopType that);

  /** Process an instance of BottomType. */
  public RetType forBottomType(BottomType that);

  /** Process an instance of VoidType. */
  public RetType forVoidType(VoidType that);

  /** Process an instance of Wildcard. */
  public RetType forWildcard(Wildcard that);
}
