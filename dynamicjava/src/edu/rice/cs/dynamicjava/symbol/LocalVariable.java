package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.Type;

/** Represents a local variable declaration.  To allow usage as a key in maps, every instance is
  * considered unique, and clients should not create multiple instances to describe the same entity.
  */
public class LocalVariable implements Variable {
  private final String _name;
  private final Type _type;
  private final boolean _isFinal;
  
  public LocalVariable(String name, Type type, boolean isFinal) {
    _name = name;
    _type = type;
    _isFinal = isFinal;
  }
  
  public String declaredName() { return _name; }
  
  public Type type() { return _type; }
  
  public boolean isFinal() { return _isFinal; }
  
  public String toString() {
    return "LocalVariable(" + _name + ": " + _type + ")@" + Integer.toHexString(hashCode());
  }
}
