package edu.rice.cs.dynamicjava.interpreter;

import java.lang.reflect.Array;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.iter.IterUtil;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.LocalVariable;

import static koala.dynamicjava.interpreter.NodeProperties.*;

/** Evaluates the given expression.  The expression is assumed to have been
  * checked without any errors.  Note that the result of a visitor method is not necessarily
  * the value of the expression -- for example, a primitive casting conversion might be required.
  * The {@link value} method contains additional checks and conversions, and should be used exclusively
  * by clients to evaluate an expression.
  */
public class ExpressionEvaluator extends AbstractVisitor<Object> implements Lambda<Node, Object> {
  
  private final RuntimeBindings _bindings;
  private final Options _options;

  public ExpressionEvaluator(RuntimeBindings bindings, Options options) {
    _bindings = bindings;
    _options = options;
  }
  
  public Object value(Node n) {
    Object result;
    if (hasValue(n)) { result = getValue(n); }
    else if (hasTranslation(n)) { result = value(getTranslation(n)); }
    else { result = n.acceptVisitor(this); }
    if (hasConvertedType(n)) { result = convert(result, getConvertedType(n).value()); }
    if (hasCheckedType(n)) {
      Class<?> expected = getCheckedType(n).value();
      if (!expected.isInstance(result)) {
        throw new WrappedException(new EvaluatorException(
            new ClassCastException("From " + result.getClass().getName() + " to " + expected.getName())));
      }
    }
    return result;
  }
  
  @Override public Object visit(Literal node) { return node.getValue(); }
  
  @Override public Object visit(VariableAccess node) { return new LValueVisitor().visit(node).value(); }

  @Override public Object visit(SimpleFieldAccess node) { return new LValueVisitor().visit(node).value(); }
  
  @Override public Object visit(ObjectFieldAccess node) { return new LValueVisitor().visit(node).value(); }
  
  @Override public Object visit(SuperFieldAccess node) { return new LValueVisitor().visit(node).value(); }

  @Override public Object visit(StaticFieldAccess node) { return new LValueVisitor().visit(node).value(); }
  
  @Override public Object visit(ThisExpression node) {
    return _bindings.getThis(getDJClass(node));
  }

  @Override public Object visit(SimpleMethodCall node) {
    if (hasDJClass(node)) {
      return handleMethodCall(node, _bindings.getThis(getDJClass(node)));
    }
    else {
      return handleMethodCall(node, null);
    }
  }
  
  @Override public Object visit(ObjectMethodCall node) {
    return handleMethodCall(node, value(node.getExpression()));
  }
  
  @Override public Object visit(SuperMethodCall node) {
    return handleMethodCall(node, _bindings.getThis(getDJClass(node)));
  }

  @Override public Object visit(StaticMethodCall node) {
    return handleMethodCall(node, null);
  }
  
  private Object handleMethodCall(MethodCall node, Object receiver) {
    Iterable<Object> args;
    if (node.getArguments() == null) { args = IterUtil.empty(); }
    else { args = IterUtil.mapSnapshot(node.getArguments(), this); }

    try { return getMethod(node).evaluate(receiver, args, _bindings, _options); }
    catch (EvaluatorException e) { throw new WrappedException(e); }
  }
  
    
  @Override public Object visit(SimpleAllocation node) {
    return handleConstructor(node, null, node.getArguments());
  }
  
  @Override public Object visit(AnonymousAllocation node) {
    return handleConstructor(node, null, null);
  }

  @Override public Object visit(InnerAllocation node) {
    return handleConstructor(node, node.getExpression(), node.getArguments());
  }
  
  @Override public Object visit(AnonymousInnerAllocation node) {
    return handleConstructor(node, null, null);
  }

  /**
   * @param args  May be null, meaning there are no arguments
   */
  private Object handleConstructor(Expression node, Expression outer, Iterable<Expression> args) {
    Object outerVal = (outer == null) ? null : value(outer);
    
    Iterable<Object> argVals;
    if (args == null) { argVals = IterUtil.empty(); }
    else { argVals = IterUtil.mapSnapshot(args, this); }

    try { return getConstructor(node).evaluate(outerVal, argVals, _bindings, _options); }
    catch (EvaluatorException e) { throw new WrappedException(e); }
  }
  
  
  @Override public Object visit(SimpleAssignExpression node) {
    Box<Object> left = node.getLeftExpression().acceptVisitor(new LValueVisitor());
    Object val = value(node.getRightExpression());
    left.set(val);
    return val;
  }

  @Override public Object visit(TypeExpression node) { return getErasedType(node.getType()).value(); }

  @Override public Object visit(ArrayAllocation node) {
    if (node.getInitialization() != null) {
      return node.getInitialization().acceptVisitor(this);
    }
    else {
      Class<?> component = getErasedType(node).value();
      int[] dims = new int[node.getSizes().size()];
      int i = 0;
      for (Object dim : IterUtil.map(node.getSizes(), this)) {
        dims[i] = (Integer) dim;
        component = component.getComponentType();
        i++;
      }
      return Array.newInstance(component, dims);
    }
  }

  @Override public Object visit(ArrayInitializer node) {
    Object result = Array.newInstance(getErasedType(node).value().getComponentType(),
                                      node.getCells().size());
    int i = 0;
    for (Object elt : IterUtil.map(node.getCells(), this)) {
      Array.set(result, i, elt);
      i++;
    }
    return result;
  }

  @Override public Object visit(ArrayAccess node) { return new LValueVisitor().visit(node).value(); }


  /* * * PRIMITIVE EXPRESSIONS * * */
  
  @Override public Object visit(NotExpression node) { return NOT.value(value(node.getExpression())); }
  @Override public Object visit(ComplementExpression node) { return COMPLEMENT.value(value(node.getExpression())); }
  @Override public Object visit(PlusExpression node) { return PLUS.value(value(node.getExpression())); }
  @Override public Object visit(MinusExpression node) { return MINUS.value(value(node.getExpression())); }

  @Override public Object visit(AddExpression node) {
    return getOperation(node).value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(AddAssignExpression node) {
    return handleOpAssignExpression(node, getOperation(node));
  }
  
  @Override public Object visit(SubtractExpression node) {
    return SUBTRACT.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(SubtractAssignExpression node) {
    return handleOpAssignExpression(node, SUBTRACT);
  }

  @Override public Object visit(MultiplyExpression node) {
    return MULTIPLY.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(MultiplyAssignExpression node) {
    return handleOpAssignExpression(node, MULTIPLY);
  }
  
  @Override public Object visit(DivideExpression node) {
    try { return DIVIDE.value(value(node.getLeftExpression()), value(node.getRightExpression())); }
    catch (ArithmeticException e) {
      throw new WrappedException(new EvaluatorException(e,
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$MatchingPrimitiveBinaryOperation.value",
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$15.value"));
    }
  }

  @Override public Object visit(DivideAssignExpression node) {
    return handleOpAssignExpression(node, DIVIDE);
  }

  @Override public Object visit(RemainderExpression node) {
    try { return REMAINDER.value(value(node.getLeftExpression()), value(node.getRightExpression())); }
    catch (ArithmeticException e) {
      throw new WrappedException(new EvaluatorException(e,
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$MatchingPrimitiveBinaryOperation.value",
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$16.value"));
    }
  }

  @Override public Object visit(RemainderAssignExpression node) {
    return handleOpAssignExpression(node, REMAINDER);
  }

  @Override public Object visit(EqualExpression node) {
    return getOperation(node).value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(NotEqualExpression node) {
    return getOperation(node).value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(LessExpression node) {
    return LESS.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(LessOrEqualExpression node) {
    return LESS_OR_EQUAL.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(GreaterExpression node) {
    return GREATER.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(GreaterOrEqualExpression node) {
    return GREATER_OR_EQUAL.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(BitAndExpression node) {
    return BIT_AND.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(BitAndAssignExpression node) {
    return handleOpAssignExpression(node, BIT_AND);
  }
  
  @Override public Object visit(ExclusiveOrExpression node) {
    return EXCLUSIVE_OR.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(ExclusiveOrAssignExpression node) {
    return handleOpAssignExpression(node, EXCLUSIVE_OR);
  }

  @Override public Object visit(BitOrExpression node) {
    return BIT_OR.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(BitOrAssignExpression node) {
    return handleOpAssignExpression(node, BIT_OR);
  }

  @Override public Object visit(ShiftLeftExpression node) {
    return SHIFT_LEFT.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(ShiftLeftAssignExpression node) {
    return handleOpAssignExpression(node, SHIFT_LEFT);
  }

  @Override public Object visit(ShiftRightExpression node) {
    return SHIFT_RIGHT.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(ShiftRightAssignExpression node) {
    return handleOpAssignExpression(node, SHIFT_RIGHT);
  }

  @Override public Object visit(UnsignedShiftRightExpression node) {
    return UNSIGNED_SHIFT_RIGHT.value(value(node.getLeftExpression()), value(node.getRightExpression()));
  }

  @Override public Object visit(UnsignedShiftRightAssignExpression node) {
    return handleOpAssignExpression(node, UNSIGNED_SHIFT_RIGHT);
  }

  
  /**
   * Evaluate the given operator-assignment expression by invoking the given operation and setting
   * the variable to its result
   */
  private Object handleOpAssignExpression(AssignExpression node, Lambda2<Object, Object, Object> op) {
    // TODO: This implementation incorrectly evaluates the left twice.  For example:
    // foo().x += 3
    // (Should call "foo" only once)
    // The problem arises from our current strategy of tagging node with a LEFT_EXPRESSION, 
    // which is node.leftExpression wrapped in any conversions.  We could either use a different
    // strategy or somehow detect on the first evaluation that the expression (possibly a nested
    // component of LEFT_EXPRESSION) needs to be tagged with VALUE for the second evaluation.
    Box<Object> setter = node.getLeftExpression().acceptVisitor(new LValueVisitor());
    Object left = value(getLeftExpression(node)); // not to be confused with node.getLeft...
    Object right = value(node.getRightExpression());
    try {
      Object result = op.value(left, right);
      // The result might need to be boxed, but the representation of boxed values
      // and primitive values is identical, so nothing needs to be done
      setter.set(result);
      return result;
    }
    catch (ArithmeticException e) {
      String[] divStack = new String[]{
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$MatchingPrimitiveBinaryOperation.value",
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$15.value"
      };
      String[] modStack = new String[]{
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$MatchingPrimitiveBinaryOperation.value",
        "edu.rice.cs.dynamicjava.interpreter.ExpressionEvaluator$16.value"
      };
      throw new WrappedException(new EvaluatorException(e, divStack, modStack));
    }
  }

  @Override public Object visit(AndExpression node) {
    return (Boolean) value(node.getLeftExpression()) && (Boolean) value(node.getRightExpression());
  }
  
  @Override public Object visit(OrExpression node) {
    return (Boolean) value(node.getLeftExpression()) || (Boolean) value(node.getRightExpression());
  }

  @Override public Object visit(InstanceOfExpression node) {
    Object v = value(node.getExpression());
    return getErasedType(node.getReferenceType()).value().isInstance(v);
  }

  @Override public Object visit(CastExpression node) {
    // cast checks/conversions are handled by value()
    return value(node.getExpression());
  }

  @Override public Object visit(ConditionalExpression node) {
    if ((Boolean) value(node.getConditionExpression())) { return value(node.getIfTrueExpression()); }
    else { return value(node.getIfFalseExpression()); }
  }
  
  @Override public Object visit(PostIncrement node) {
    Box<Object> setter = node.getExpression().acceptVisitor(new LValueVisitor());
    Object result = value(getLeftExpression(node)); // not to be confused with node.getLeft...
    // The result might need to be boxed, but the representation of boxed values
    // and primitive values is identical, so nothing needs to be done
    setter.set(INCREMENT.value(result));
    return result;
  }
  
  @Override public Object visit(PreIncrement node) {
    Box<Object> setter = node.getExpression().acceptVisitor(new LValueVisitor());
    Object val = value(getLeftExpression(node)); // not to be confused with node.getLeft...
    Object result = INCREMENT.value(val);
    // The result might need to be boxed, but the representation of boxed values
    // and primitive values is identical, so nothing needs to be done
    setter.set(result);
    return result;
  }

  @Override public Object visit(PostDecrement node) {
    Box<Object> setter = node.getExpression().acceptVisitor(new LValueVisitor());
    Object result = value(getLeftExpression(node)); // not to be confused with node.getLeft...
    // The result might need to be boxed, but the representation of boxed values
    // and primitive values is identical, so nothing needs to be done
    setter.set(DECREMENT.value(result));
    return result;
  }

  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  @Override public Object visit(PreDecrement node) {
    Box<Object> setter = node.getExpression().acceptVisitor(new LValueVisitor());
    Object val = value(getLeftExpression(node)); // not to be confused with node.getLeft...
    Object result = DECREMENT.value(val);
    // The result might need to be boxed, but the representation of boxed values
    // and primitive values is identical, so nothing needs to be done
    setter.set(result);
    return result;
  }
  

  /**
   * Evaluates a left-hand side of an assignment.  In the undefined cases, this returns null.  
   * (The type checker insures that such cases will never be called.)
   */
  private class LValueVisitor extends AbstractVisitor<Box<Object>> {
    
    @Override public Box<Object> visit(AmbiguousName node) {
      return getTranslation(node).acceptVisitor(this);
    }
    
    @Override public Box<Object> visit(VariableAccess node) {
      final LocalVariable var = getVariable(node);
      return new Box<Object>() {
        public Object value() { return _bindings.get(var); }
        public void set(Object val) { _bindings.set(var, val); }
      };
    }

    @Override public Box<Object> visit(SimpleFieldAccess node) {
      Object receiver = hasDJClass(node) ? _bindings.getThis(getDJClass(node)) : null;
      return getField(node).boxForReceiver(receiver);
    }
    
    @Override public Box<Object> visit(ObjectFieldAccess node) {
      if (hasTranslation(node)) { return getTranslation(node).acceptVisitor(this); }
      else { return getField(node).boxForReceiver(ExpressionEvaluator.this.value(node.getExpression())); }
    }
    
    @Override public Box<Object> visit(SuperFieldAccess node) {
      return getField(node).boxForReceiver(_bindings.getThis(getDJClass(node)));
    }
    
    @Override public Box<Object> visit(StaticFieldAccess node) {
      return getField(node).boxForReceiver(null);
    }
    
    @Override public Box<Object> visit(ArrayAccess node) {
      final Object array = ExpressionEvaluator.this.value(node.getExpression());
      final Integer index = (Integer) ExpressionEvaluator.this.value(node.getCellNumber());
      return new Box<Object>() {
        public Object value() {
          try { return Array.get(array, index); }
          catch (NullPointerException e) {
            throw new WrappedException(new EvaluatorException(e, "java.lang.reflect.Array.get"));
          }
          catch (ArrayIndexOutOfBoundsException e) {
            throw new WrappedException(new EvaluatorException(e, "java.lang.reflect.Array.get"));
          }
        }
        public void set(Object val) {
          try { Array.set(array, index, val); }
          catch (NullPointerException e) {
            throw new WrappedException(new EvaluatorException(e, "java.lang.reflect.Array.set"));
          }
          catch (ArrayIndexOutOfBoundsException e) {
            throw new WrappedException(new EvaluatorException(e, "java.lang.reflect.Array.set"));
          }
        }
      };
    }
    
  }
  
  
  /** Convert a primitive to the appropriate type */
  private static Object convert(Object obj, Class<?> target) {
    if (target.equals(boolean.class)) {
      if (obj instanceof Boolean) { return obj; }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(char.class)) {
      if (obj instanceof Character) { return obj; }
      else if (obj instanceof Number) { return (char) ((Number) obj).intValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(byte.class)) {
      if (obj instanceof Byte) { return obj; }
      else if (obj instanceof Character) { return (byte) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).byteValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(short.class)) {
      if (obj instanceof Short) { return obj; }
      else if (obj instanceof Character) { return (short) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).shortValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(int.class)) {
      if (obj instanceof Integer) { return obj; }
      else if (obj instanceof Character) { return (int) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).intValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(long.class)) {
      if (obj instanceof Long) { return obj; }
      else if (obj instanceof Character) { return (long) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).longValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(float.class)) {
      if (obj instanceof Float) { return obj; }
      else if (obj instanceof Character) { return (float) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).floatValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else if (target.equals(double.class)) {
      if (obj instanceof Double) { return obj; }
      else if (obj instanceof Character) { return (double) (char) (Character) obj; }
      else if (obj instanceof Number) { return ((Number) obj).doubleValue(); }
      else { throw new IllegalArgumentException(); }
    }
    else { throw new IllegalArgumentException(); }
  }

  /**
   * Parent class for primitive unary operations; the correct primitive {@code value()} method is
   * called based on the type of the arguments.  If an operation does not apply to
   * a certain primitive type, that version of the {@code value()} method made be left
   * out, and the default implementation will just throw an exception.
   */
  private static abstract class PrimitiveUnaryOperation implements Lambda<Object, Object> {
    public Object value(Object val) {
      if (val instanceof Boolean) { 
        return value((boolean)(Boolean) val);
      }
      else if (val instanceof Character) { 
        return value((char)(Character) val);
      }
      else if (val instanceof Byte) { 
        return value((byte)(Byte) val);
      }
      else if (val instanceof Short) { 
        return value((short)(Short) val);
      }
      else if (val instanceof Integer) { 
        return value((int)(Integer) val);
      }
      else if (val instanceof Long) { 
        return value((long)(Long) val);
      }
      else if (val instanceof Float) { 
        return value((float)(Float) val);
      }
      else if (val instanceof Double) { 
        return value((double)(Double) val);
      }
      else { throw new IllegalArgumentException(); }
    }
    
    public Object value(boolean val) { throw new IllegalArgumentException(); }
    public Object value(char val) { throw new IllegalArgumentException(); }
    public Object value(byte val) { throw new IllegalArgumentException(); }
    public Object value(short val) { throw new IllegalArgumentException(); }
    public Object value(int val) { throw new IllegalArgumentException(); }
    public Object value(long val) { throw new IllegalArgumentException(); }
    public Object value(float val) { throw new IllegalArgumentException(); }
    public Object value(double val) { throw new IllegalArgumentException(); }
  }
  
  /**
   * Parent class for primitive operations in which the types of {@code left} and
   * {@code right} are assumed to match; the correct primitive {@code value()} method is
   * called based on the type of the arguments.  If an operation does not apply to
   * a certain primitive type, that version of the {@code value()} method made be left
   * out, and the default implementation will just throw an exception.
   */
  private static abstract class MatchingPrimitiveBinaryOperation implements Lambda2<Object, Object, Object> {
    public Object value(Object left, Object right) {
      if (left instanceof Boolean) { 
        return value((boolean)(Boolean) left, (boolean)(Boolean) right);
      }
      else if (left instanceof Character) { 
        return value((char)(Character) left, (char)(Character) right);
      }
      else if (left instanceof Byte) { 
        return value((byte)(Byte) left, (byte)(Byte) right);
      }
      else if (left instanceof Short) { 
        return value((short)(Short) left, (short)(Short) right);
      }
      else if (left instanceof Integer) { 
        return value((int)(Integer) left, (int)(Integer) right);
      }
      else if (left instanceof Long) { 
        return value((long)(Long) left, (long)(Long) right);
      }
      else if (left instanceof Float) { 
        return value((float)(Float) left, (float)(Float) right);
      }
      else if (left instanceof Double) { 
        return value((double)(Double) left, (double)(Double) right);
      }
      else { throw new IllegalArgumentException(); }
    }
    
    public Object value(boolean left, boolean right) { throw new IllegalArgumentException(); }
    public Object value(char left, char right) { throw new IllegalArgumentException(); }
    public Object value(byte left, byte right) { throw new IllegalArgumentException(); }
    public Object value(short left, short right) { throw new IllegalArgumentException(); }
    public Object value(int left, int right) { throw new IllegalArgumentException(); }
    public Object value(long left, long right) { throw new IllegalArgumentException(); }
    public Object value(float left, float right) { throw new IllegalArgumentException(); }
    public Object value(double left, double right) { throw new IllegalArgumentException(); }
  }
  
  /**
   * Parent class for shift operations -- each operand is the result of *unary* numeric
   * promotion, so each may be an int or a long
   */
  private static abstract class ShiftOperation implements Lambda2<Object, Object, Object> {
    public Object value(Object left, Object right) {
      if (left instanceof Integer) {
        if (right instanceof Integer) { return value((int)(Integer) left, (int)(Integer) right); }
        else if (right instanceof Long) { return value((int)(Integer) left, (long)(Long) right); }
        else { throw new IllegalArgumentException(); }
      }
      else if (left instanceof Long) {
        if (right instanceof Integer) { return value((long)(Long) left, (int)(Integer) right); }
        else if (right instanceof Long) { return value((long)(Long) left, (long)(Long) right); }
        else { throw new IllegalArgumentException(); }
      }
      else { throw new IllegalArgumentException(); }
    }
    
    public Object value(int left, int right) { throw new IllegalArgumentException(); }
    public Object value(int left, long right) { throw new IllegalArgumentException(); }
    public Object value(long left, int right) { throw new IllegalArgumentException(); }
    public Object value(long left, long right) { throw new IllegalArgumentException(); }
  }
  
  
  
  public static final Lambda<Object, Object> NOT = new PrimitiveUnaryOperation() {
    @Override public Object value(boolean val) { return !val; }
  };
  
  public static final Lambda<Object, Object> COMPLEMENT = new PrimitiveUnaryOperation() {
    @Override public Object value(int val) { return ~val; }
    @Override public Object value(long val) { return ~val; }
  };
  
  public static final Lambda<Object, Object> PLUS = new PrimitiveUnaryOperation() {
    @Override public Object value(int val) { return +val; }
    @Override public Object value(long val) { return +val; }
    @Override public Object value(float val) { return +val; }
    @Override public Object value(double val) { return +val; }
  };
  
  public static final Lambda<Object, Object> MINUS = new PrimitiveUnaryOperation() {
    @Override public Object value(int val) { return -val; }
    @Override public Object value(long val) { return -val; }
    @Override public Object value(float val) { return -val; }
    @Override public Object value(double val) { return -val; }
  };
  
  public static final Lambda<Object, Object> INCREMENT = new PrimitiveUnaryOperation() {
    @Override public Object value(char val) { return ++val; }
    @Override public Object value(byte val) { return ++val; }
    @Override public Object value(short val) { return ++val; }
    @Override public Object value(int val) { return ++val; }
    @Override public Object value(long val) { return ++val; }
    @Override public Object value(float val) { return ++val; }
    @Override public Object value(double val) { return ++val; }
  };
  
  public static final Lambda<Object, Object> DECREMENT = new PrimitiveUnaryOperation() {
    @Override public Object value(char val) { return --val; }
    @Override public Object value(byte val) { return --val; }
    @Override public Object value(short val) { return --val; }
    @Override public Object value(int val) { return --val; }
    @Override public Object value(long val) { return --val; }
    @Override public Object value(float val) { return --val; }
    @Override public Object value(double val) { return --val; }
  };
  
  public static final Lambda2<Object, Object, Object> OBJECT_EQUAL = new Lambda2<Object, Object, Object>() {
    public Object value(Object left, Object right) { return left == right; }
  };
  
  public static final Lambda2<Object, Object, Object> PRIMITIVE_EQUAL = new Lambda2<Object, Object, Object>() {
    public Object value(Object left, Object right) { return left.equals(right); }
  };
  
  public static final Lambda2<Object, Object, Object> OBJECT_NOT_EQUAL = new Lambda2<Object, Object, Object>() {
    public Object value(Object left, Object right) { return left != right; }
  };
  
  public static final Lambda2<Object, Object, Object> PRIMITIVE_NOT_EQUAL = new Lambda2<Object, Object, Object>() {
    public Object value(Object left, Object right) { return !left.equals(right); }
  };
  
  public static final Lambda2<Object, Object, Object> CONCATENATE = new Lambda2<Object, Object, Object>() {
    public Object value(Object left, Object right) { 
      return
        (left == null ? "null" : left.toString()) + 
        (right == null ? "null" : right.toString());
    }
  };
  
  public static final Lambda2<Object, Object, Object> ADD = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l + r; }
    @Override public Object value(long l, long r) { return l + r; }
    @Override public Object value(float l, float r) { return l + r; }
    @Override public Object value(double l, double r) { return l + r; }
  };
  
  public static final Lambda2<Object, Object, Object> SUBTRACT = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l - r; }
    @Override public Object value(long l, long r) { return l - r; }
    @Override public Object value(float l, float r) { return l - r; }
    @Override public Object value(double l, double r) { return l - r; }
  };
  
  public static final Lambda2<Object, Object, Object> MULTIPLY = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l * r; }
    @Override public Object value(long l, long r) { return l * r; }
    @Override public Object value(float l, float r) { return l * r; }
    @Override public Object value(double l, double r) { return l * r; }
  };
  
  public static final Lambda2<Object, Object, Object> DIVIDE = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l / r; }
    @Override public Object value(long l, long r) { return l / r; }
    @Override public Object value(float l, float r) { return l / r; }
    @Override public Object value(double l, double r) { return l / r; }
  };
  
  public static final Lambda2<Object, Object, Object> REMAINDER = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l % r; }
    @Override public Object value(long l, long r) { return l % r; }
    @Override public Object value(float l, float r) { return l % r; }
    @Override public Object value(double l, double r) { return l % r; }
  };
  
  public static final Lambda2<Object, Object, Object> LESS = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l < r; }
    @Override public Object value(long l, long r) { return l < r; }
    @Override public Object value(float l, float r) { return l < r; }
    @Override public Object value(double l, double r) { return l < r; }
  };
  
  public static final Lambda2<Object, Object, Object> LESS_OR_EQUAL = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l <= r; }
    @Override public Object value(long l, long r) { return l <= r; }
    @Override public Object value(float l, float r) { return l <= r; }
    @Override public Object value(double l, double r) { return l <= r; }
  };
  
  public static final Lambda2<Object, Object, Object> GREATER = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l > r; }
    @Override public Object value(long l, long r) { return l > r; }
    @Override public Object value(float l, float r) { return l > r; }
    @Override public Object value(double l, double r) { return l > r; }
  };
  
  public static final Lambda2<Object, Object, Object> GREATER_OR_EQUAL = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(int l, int r) { return l >= r; }
    @Override public Object value(long l, long r) { return l >= r; }
    @Override public Object value(float l, float r) { return l >= r; }
    @Override public Object value(double l, double r) { return l >= r; }
  };
  
  public static final Lambda2<Object, Object, Object> BIT_AND = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(boolean l, boolean r) { return l & r; }
    @Override public Object value(int l, int r) { return l & r; }
    @Override public Object value(long l, long r) { return l & r; }
  };
  
  public static final Lambda2<Object, Object, Object> BIT_OR = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(boolean l, boolean r) { return l | r; }
    @Override public Object value(int l, int r) { return l | r; }
    @Override public Object value(long l, long r) { return l | r; }
  };
  
  public static final Lambda2<Object, Object, Object> EXCLUSIVE_OR = new MatchingPrimitiveBinaryOperation() {
    @Override public Object value(boolean l, boolean r) { return l ^ r; }
    @Override public Object value(int l, int r) { return l ^ r; }
    @Override public Object value(long l, long r) { return l ^ r; }
  };
  
  public static final Lambda2<Object, Object, Object> SHIFT_LEFT = new ShiftOperation() {
    @Override public Object value(int l, int r) { return l << r; }
    @Override public Object value(int l, long r) { return l << r; }
    @Override public Object value(long l, int r) { return l << r; }
    @Override public Object value(long l, long r) { return l << r; }
  };
  
  public static final Lambda2<Object, Object, Object> SHIFT_RIGHT = new ShiftOperation() {
    @Override public Object value(int l, int r) { return l >> r; }
    @Override public Object value(int l, long r) { return l >> r; }
    @Override public Object value(long l, int r) { return l >> r; }
    @Override public Object value(long l, long r) { return l >> r; }
  };
  
  public static final Lambda2<Object, Object, Object> UNSIGNED_SHIFT_RIGHT = new ShiftOperation() {
    @Override public Object value(int l, int r) { return l >>> r; }
    @Override public Object value(int l, long r) { return l >>> r; }
    @Override public Object value(long l, int r) { return l >>> r; }
    @Override public Object value(long l, long r) { return l >>> r; }
  };
  
}
