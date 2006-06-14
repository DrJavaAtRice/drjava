package edu.rice.cs.plt.tuple;

/**
 * An arbitrary 3-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Triple<T1, T2, T3> extends Pair<T1, T2> {
  
  protected final T3 _third;
  
  public Triple(T1 first, T2 second, T3 third) { 
    super(first, second);
    _third = third;
  }
  
  public T3 third() { return _third; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Triple<?, ?, ?> cast = (Triple<?, ?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second) &&
        _third.equals(cast._third);
    }
  }
  
  protected int generateHashCode() {
    return 
      _first.hashCode() ^ 
      (_second.hashCode() << 1) ^ 
      (_third.hashCode() << 2) ^
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3> Triple<T1, T2, T3> make(T1 first, T2 second, T3 third) {
    return new Triple<T1, T2, T3>(first, second, third);
  }
  
}
