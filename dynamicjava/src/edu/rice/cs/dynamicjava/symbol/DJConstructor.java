package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;

/** Represents a constructor declaration. */
public interface DJConstructor extends Function, Access.Limited {
  public Access accessibility();
  public Access.Module accessModule();
  public Object evaluate(Object outer, Iterable<Object> args, RuntimeBindings bindings, Options options) 
    throws EvaluatorException;
}
