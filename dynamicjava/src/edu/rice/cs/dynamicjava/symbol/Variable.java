package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.Type;

public interface Variable {
  public String declaredName();
  public Type type();
  public boolean isFinal();
}
