package edu.rice.cs.plt.tuple;

/**
 * An arbitrary 4-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Quad<T1, T2, T3, T4> extends Triple<T1, T2, T3> {
  
  protected final T4 _fourth;
  
  public Quad(T1 first, T2 second, T3 third, T4 fourth) {
    super(first, second, third);
    _fourth = fourth;
  }
  
  public T4 fourth() { return _fourth; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ", " + _fourth + ")";
  }
  
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Quad<?, ?, ?, ?> cast = (Quad<?, ?, ?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second) &&
        _third.equals(cast._third) &&
        _fourth.equals(cast._fourth);
    }
  }
  
  protected int generateHashCode() {
    return _first.hashCode() ^ (_second.hashCode() << 1) ^ (_third.hashCode() << 2) ^ 
           (_fourth.hashCode() << 3);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4> Quad<T1, T2, T3, T4> make(T1 first, T2 second, T3 third, 
                                                           T4 fourth) {
    return new Quad<T1, T2, T3, T4>(first, second, third, fourth);
  }
  
}
