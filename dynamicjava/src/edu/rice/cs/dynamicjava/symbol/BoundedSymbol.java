package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.plt.lambda.DelayedThunk;

/**
 * <p>Represents a type variable or wildcard.  For simplicity, two kinds of symbols may be 
 * represented.  Declared symbols have a name; generated symbols (including 
 * wildcards) are unnamed.  In both cases, equality is defined in terms of an id object, rather 
 * than by equating names or other parameters.  Clients are responsible for ensuring, where 
 * two symbols have the same {@code id} (according to {@code equals()}), that the other 
 * properties of the symbol are the same.</p>
 * 
 * <p>Each symbol has an upper and a lower bound.  These may be initialized after object creation,
 * but must be initialized exactly once before use, or an exception will occur.</p>
 */
public class BoundedSymbol {
  
  private final Object _id;
  private final boolean _generated;
  private final String _name;
  private final DelayedThunk<Type> _upperBound;
  private final DelayedThunk<Type> _lowerBound;

  public BoundedSymbol(Object id) {
    _id = id;
    _generated = true;
    _name = null;
    _upperBound = new DelayedThunk<Type>();
    _lowerBound = new DelayedThunk<Type>();
  }
  
  public BoundedSymbol(Object id, String name) {
    _id = id;
    _generated = false;
    _name = name; 
    _upperBound = new DelayedThunk<Type>();
    _lowerBound = new DelayedThunk<Type>();
  }
  
  public BoundedSymbol(Object id, Type upperBound, Type lowerBound) { 
    this(id);
    _upperBound.set(upperBound);
    _lowerBound.set(lowerBound);
  }
  
  public BoundedSymbol(Object id, String name, Type upperBound, Type lowerBound) {
    this(id, name);
    _upperBound.set(upperBound);
    _lowerBound.set(lowerBound);
  }
  
  /* This code is a bad idea.
  public TypeVariable changeBounds(Type upperBound, Type lowerBound) {
    // TODO: This allows two variables to be equal (same id), even though their
    // bounds are different.  How do we handle that?
    if (_generated) { return new TypeVariable(_id, upperBound, lowerBound); }
    else { return new TypeVariable(_id, _name, upperBound, lowerBound); }
  }
  */
  
  public boolean generated() { return _generated; }
  
  public String name() { 
    if (_generated) { throw new IllegalArgumentException("Symbol is unnamed"); }
    else { return _name; }
  }
  
  public void initializeUpperBound(Type t) { _upperBound.set(t); }
  
  public void initializeLowerBound(Type t) { _lowerBound.set(t); }
  
  public Type upperBound() { return _upperBound.value(); }
  
  public Type lowerBound() { return _lowerBound.value(); }
  
  public String toString() { return "symbol " + _id; }
  
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (!(o instanceof BoundedSymbol)) { return false; }
    else { return ((BoundedSymbol) o)._id.equals(_id); }
  }
  
  public int hashCode() {
    return getClass().hashCode() ^ _id.hashCode();
  }

}
