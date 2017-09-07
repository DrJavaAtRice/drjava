package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/** An abstract void visitor over Type that provides a Runnable1 run method;
  * all visit methods are left unimplemented. */
//@SuppressWarnings("unused")
public abstract class TypeVisitorRunnable1 implements edu.rice.cs.plt.lambda.Runnable1<Type>, TypeVisitor_void {

  public void run(Type that) {
    that.apply(this);
  }


  /** Process an instance of BooleanType. */
  public abstract void forBooleanType(BooleanType that);

  /** Process an instance of CharType. */
  public abstract void forCharType(CharType that);

  /** Process an instance of ByteType. */
  public abstract void forByteType(ByteType that);

  /** Process an instance of ShortType. */
  public abstract void forShortType(ShortType that);

  /** Process an instance of IntType. */
  public abstract void forIntType(IntType that);

  /** Process an instance of LongType. */
  public abstract void forLongType(LongType that);

  /** Process an instance of FloatType. */
  public abstract void forFloatType(FloatType that);

  /** Process an instance of DoubleType. */
  public abstract void forDoubleType(DoubleType that);

  /** Process an instance of NullType. */
  public abstract void forNullType(NullType that);

  /** Process an instance of SimpleArrayType. */
  public abstract void forSimpleArrayType(SimpleArrayType that);

  /** Process an instance of VarargArrayType. */
  public abstract void forVarargArrayType(VarargArrayType that);

  /** Process an instance of SimpleClassType. */
  public abstract void forSimpleClassType(SimpleClassType that);

  /** Process an instance of RawClassType. */
  public abstract void forRawClassType(RawClassType that);

  /** Process an instance of ParameterizedClassType. */
  public abstract void forParameterizedClassType(ParameterizedClassType that);

  /** Process an instance of IntersectionType. */
  public abstract void forIntersectionType(IntersectionType that);

  /** Process an instance of UnionType. */
  public abstract void forUnionType(UnionType that);

  /** Process an instance of VariableType. */
  public abstract void forVariableType(VariableType that);

  /** Process an instance of TopType. */
  public abstract void forTopType(TopType that);

  /** Process an instance of BottomType. */
  public abstract void forBottomType(BottomType that);

  /** Process an instance of VoidType. */
  public abstract void forVoidType(VoidType that);

  /** Process an instance of Wildcard. */
  public abstract void forWildcard(Wildcard that);
}
