package edu.rice.cs.plt.tuple;

/**
 * An arbitrary 6-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Sextet<T1, T2, T3, T4, T5, T6> extends Quint<T1, T2, T3, T4, T5> {
  
  protected final T6 _sixth;
  
  public Sextet(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth) {
    super(first, second, third, fourth, fifth);
    _sixth = sixth;
  }
  
  public T6 sixth() { return _sixth; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ", " + _fourth + ", " + _fifth + ", " +
             _sixth + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Sextet<?, ?, ?, ?, ?, ?> cast = (Sextet<?, ?, ?, ?, ?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second) &&
        _third.equals(cast._third) &&
        _fourth.equals(cast._fourth) &&
        _fifth.equals(cast._fifth) &&
        _sixth.equals(cast._sixth);
    }
  }
  
  protected int generateHashCode() {
    return 
      _first.hashCode() ^ 
      (_second.hashCode() << 1) ^ 
      (_third.hashCode() << 2) ^ 
      (_fourth.hashCode() << 3) ^ 
      (_fifth.hashCode() << 4) ^ 
      (_sixth.hashCode() << 5) ^ 
      getClass().hashCode();
  }
    
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, T5, T6> Sextet<T1, T2, T3, T4, T5, T6> make(T1 first, T2 second, 
                                                                             T3 third, T4 fourth, 
                                                                             T5 fifth, T6 sixth) {
    return new Sextet<T1, T2, T3, T4, T5, T6>(first, second, third, fourth, fifth, sixth);
  }
  
}
