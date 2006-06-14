package edu.rice.cs.plt.tuple;

/**
 * An arbitrary 8-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Octet<T1, T2, T3, T4, T5, T6, T7, T8> extends Septet<T1, T2, T3, T4, T5, T6, T7> {
  
  protected final T8 _eighth;
  
  public Octet(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth, T7 seventh, T8 eighth) {
    super(first, second, third, fourth, fifth, sixth, seventh);
    _eighth = eighth;
  }
  
  public T8 eighth() { return _eighth; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ", " + _fourth + ", " + _fifth + ", " +
             _sixth + ", " + _seventh + ", " + _eighth + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Octet<?, ?, ?, ?, ?, ?, ?, ?> cast = (Octet<?, ?, ?, ?, ?, ?, ?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second) &&
        _third.equals(cast._third) &&
        _fourth.equals(cast._fourth) &&
        _fifth.equals(cast._fifth) &&
        _sixth.equals(cast._sixth) &&
        _seventh.equals(cast._seventh) &&
        _eighth.equals(cast._eighth);
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
      (_seventh.hashCode() << 6) ^ 
      (_eighth.hashCode() << 7) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, T5, T6, T7, T8> 
    Octet<T1, T2, T3, T4, T5, T6, T7, T8> make(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, 
                                               T6 sixth, T7 seventh, T8 eighth) {
    return new Octet<T1, T2, T3, T4, T5, T6, T7, T8>(first, second, third, fourth, fifth, sixth, 
                                                     seventh, eighth);
  }
  
}
