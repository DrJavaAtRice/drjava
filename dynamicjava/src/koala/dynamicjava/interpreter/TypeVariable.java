package koala.dynamicjava.interpreter;

import koala.dynamicjava.interpreter.type.Type;

/**
 * Represents a type variable.  For simplicity, two kinds of variables may be represented.
 * Declared type variables have a name; generated type variables are unnamed.  In both
 * cases, equality is defined in terms of identity, rather than by equating names or other
 * parameters.
 */
public class TypeVariable {
  
  private final boolean _generated;
  private final String _name;
  private final Type _upperBound;
  private final Type _lowerBound;

  public TypeVariable(Type upperBound, Type lowerBound) { 
    _generated = true;
    _name = null; 
    _upperBound = upperBound;
    _lowerBound = lowerBound;
  }
  
  public TypeVariable(String name, Type upperBound, Type lowerBound) { 
    _generated = false;
    _name = name; 
    _upperBound = upperBound;
    _lowerBound = lowerBound;
  }
  
  public boolean generated() { return _generated; }
  
  public String name() { 
    if (_generated) { throw new IllegalArgumentException("Variable is unnamed"); }
    else { return _name; }
  }
  
  public Type upperBound() { return _upperBound; }
  
  public Type lowerBound() { return _lowerBound; }

}
