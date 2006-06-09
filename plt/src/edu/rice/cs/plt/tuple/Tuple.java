package edu.rice.cs.plt.tuple;

/** Abstract parent of all tuple classes; see the package documentation for general discussion */
public abstract class Tuple {

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
