package edu.rice.cs.dynamicjava.symbol;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;

/**
 * Abstract parent for special implicit methods.  The evaluation details are handled
 * here, allowing the implementation to be based on a method with a different signature.
 */
public abstract class SpecialMethod implements DJMethod {
  
  protected abstract Method implementation() throws ClassNotFoundException, NoSuchMethodException;
  
  public DJMethod declaredSignature() { return this; }
  public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, 
                         Options options) throws EvaluatorException {
    if (receiver == null) {
      throw new WrappedException(new EvaluatorException(new NullPointerException()));
    }
    try {
      Method m = implementation();
      try { m.setAccessible(true); /* override protected access */ }
      catch (SecurityException e) { debug.log(e); /* ignore -- we can't relax accessibility */ }
      return m.invoke(receiver, IterUtil.toArray(args, Object.class));
    }
    catch (ClassNotFoundException e) { throw new RuntimeException(e); }
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
