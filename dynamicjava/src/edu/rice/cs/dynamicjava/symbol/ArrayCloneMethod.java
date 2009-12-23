package edu.rice.cs.dynamicjava.symbol;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.symbol.type.ArrayType;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

/**
 * Provides a DJMethod interface for accessing an array's implicit "clone" method.
 * This overrides {@link Object#clone}, giving it public access, eliminating the
 * CloneNotSupported {@code throws} clause, and refining the return type.
 */
public class ArrayCloneMethod implements DJMethod {
  
  private final ArrayType _type;
  
  public ArrayCloneMethod(ArrayType t) {_type = t; }
  
  public String declaredName() { return "clone"; }
  public DJClass declaringClass() { return null; }
  
  public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
  public Iterable<LocalVariable> parameters() { return IterUtil.empty(); }
  public Type returnType() { return _type; }
  public Iterable<Type> thrownTypes() { return IterUtil.empty(); }
  
  public boolean isStatic() { return false; }
  public boolean isAbstract() { return false; }
  public boolean isFinal() { return false; }
  public Access accessibility() { return Access.PUBLIC; }
  public Access.Module accessModule() { return new TopLevelAccessModule("java.lang"); }
  
  public DJMethod declaredSignature() { return this; }
  public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, 
                         Options options) throws EvaluatorException {
    if (receiver == null) {
      throw new WrappedException(new EvaluatorException(new NullPointerException()));
    }
    try {
      Method clone = Object.class.getDeclaredMethod("clone");
      try { clone.setAccessible(true); /* override protected access */ }
      catch (SecurityException e) { debug.log(e); /* ignore -- we can't relax accessibility */ }
      return clone.invoke(receiver, IterUtil.toArray(args, Object.class));
    }
    catch (NoSuchMethodException e) { throw new RuntimeException(e); }
    catch (InvocationTargetException e) { throw new EvaluatorException(e.getCause(), EXTRA_STACK); }
    catch (IllegalAccessException e) { throw new RuntimeException(e); }
  }

  private static final String[] EXTRA_STACK =
    new String[] { "java.lang.reflect.Method.invoke",
                   "sun.reflect.DelegatingMethodAccessorImpl.invoke",
                   "sun.reflect.NativeMethodAccessorImpl.invoke",
                   "sun.reflect.NativeMethodAccessorImpl.invoke0" };
}
