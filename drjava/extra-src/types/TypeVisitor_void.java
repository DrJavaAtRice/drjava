package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** An interface for visitors over Type that do not return a value. */
//@SuppressWarnings("unused")
public interface TypeVisitor_void {

  /** Process an instance of BooleanType. */
  public void forBooleanType(BooleanType that);

  /** Process an instance of CharType. */
  public void forCharType(CharType that);

  /** Process an instance of ByteType. */
  public void forByteType(ByteType that);

  /** Process an instance of ShortType. */
  public void forShortType(ShortType that);

  /** Process an instance of IntType. */
  public void forIntType(IntType that);

  /** Process an instance of LongType. */
  public void forLongType(LongType that);

  /** Process an instance of FloatType. */
  public void forFloatType(FloatType that);

  /** Process an instance of DoubleType. */
  public void forDoubleType(DoubleType that);

  /** Process an instance of NullType. */
  public void forNullType(NullType that);

  /** Process an instance of SimpleArrayType. */
  public void forSimpleArrayType(SimpleArrayType that);

  /** Process an instance of VarargArrayType. */
  public void forVarargArrayType(VarargArrayType that);

  /** Process an instance of SimpleClassType. */
  public void forSimpleClassType(SimpleClassType that);

  /** Process an instance of RawClassType. */
  public void forRawClassType(RawClassType that);

  /** Process an instance of ParameterizedClassType. */
  public void forParameterizedClassType(ParameterizedClassType that);

  /** Process an instance of IntersectionType. */
  public void forIntersectionType(IntersectionType that);

  /** Process an instance of UnionType. */
  public void forUnionType(UnionType that);

  /** Process an instance of VariableType. */
  public void forVariableType(VariableType that);

  /** Process an instance of TopType. */
  public void forTopType(TopType that);

  /** Process an instance of BottomType. */
  public void forBottomType(BottomType that);

  /** Process an instance of VoidType. */
  public void forVoidType(VoidType that);

  /** Process an instance of Wildcard. */
  public void forWildcard(Wildcard that);
}
