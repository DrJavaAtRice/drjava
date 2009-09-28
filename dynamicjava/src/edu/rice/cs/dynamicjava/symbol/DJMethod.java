package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.symbol.type.Type;

/** Represents a method declaration. */
public interface DJMethod extends Function, Access.Limited {
  /** The class declaring this method.  May be null for certain special methods. */
  public DJClass declaringClass();
  public boolean isStatic();
  public boolean isAbstract();
  public boolean isFinal();
  public Access accessibility();
  public Access.Module accessModule();
  /** Get the original declared method (prior to any substitutions, etc). */
  public DJMethod declaredSignature();
  /** Invoke the method with the given parameters. */
  public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, Options options) 
    throws EvaluatorException;
}
