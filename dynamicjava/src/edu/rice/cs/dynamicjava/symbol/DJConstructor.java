package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;

/** Represents a constructor declaration. */
public interface DJConstructor extends Function, Access.Limited {
  public Access accessibility();
  public Access.Module accessModule();
  /** Get the original declared constructor (prior to any substitutions, etc). */
  public DJConstructor declaredSignature();
  /** Invoke the constructor with the given parameters.  {@code outer} may be null. */
  public Object evaluate(Object outer, Iterable<Object> args, RuntimeBindings bindings, Options options) 
    throws EvaluatorException;
}
