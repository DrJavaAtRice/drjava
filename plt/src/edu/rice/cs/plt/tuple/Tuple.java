package edu.rice.cs.plt.tuple;

import java.io.Serializable;

/**
 * <p>Abstract parent of all tuple classes, providing lazy evaluation of the hash code.  See the 
 * package documentation for general discussion of the tuple classes.</p>
 * 
 * <p>As a wrapper for arbitrary objects, instances of this class will serialize without error
 * only if the wrapped objects are serializable.</p>
 */
public abstract class Tuple implements Serializable {

  private int _hashCode;
  private boolean _validHashCode;
  
  protected Tuple() {
    _validHashCode = false;
    // _hashCode's value doesn't matter yet
  }
  
  protected abstract int generateHashCode();
  
  public int hashCode() {
    if (!_validHashCode) { _hashCode = generateHashCode(); _validHashCode = true; }
    return _hashCode;
  }

}
