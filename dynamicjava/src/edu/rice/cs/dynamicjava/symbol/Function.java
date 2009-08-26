package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

/** Represents a function -- a method, constructor, or local function. */
public interface Function {
  public String declaredName();
  public Iterable<VariableType> declaredTypeParameters();
  public Iterable<LocalVariable> declaredParameters();
  public Type returnType();
  public Iterable<Type> thrownTypes();
}
