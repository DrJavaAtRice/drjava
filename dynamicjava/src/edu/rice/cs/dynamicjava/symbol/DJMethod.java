package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.symbol.type.Type;

/** Represents a method declaration. */
public interface DJMethod extends Function {
  public String declaredName();
  public boolean isStatic();
  public boolean isAbstract();
  public boolean isFinal();
  public Access accessibility();
  public Type returnType();
  public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, Options options) 
    throws EvaluatorException;
}
