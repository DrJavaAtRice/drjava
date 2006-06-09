package edu.rice.cs.plt.tuple;

/**
 * An arbitrary 7-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Septet<T1, T2, T3, T4, T5, T6, T7> extends Sextet<T1, T2, T3, T4, T5, T6> {
  
  protected final T7 _seventh;
  
  public Septet(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth, T7 seventh) {
    super(first, second, third, fourth, fifth, sixth);
    _seventh = seventh;
  }
  
  public T7 seventh() { return _seventh; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ", " + _fourth + ", " + _fifth + ", " +
             _sixth + ", " + _seventh + ")";
  }
  
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Septet<?, ?, ?, ?, ?, ?, ?> cast = (Septet<?, ?, ?, ?, ?, ?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second) &&
        _third.equals(cast._third) &&
        _fourth.equals(cast._fourth) &&
        _fifth.equals(cast._fifth) &&
        _sixth.equals(cast._sixth) &&
        _seventh.equals(cast._seventh);
    }
  }
  
  protected int generateHashCode() {
    return _first.hashCode() ^ (_second.hashCode() << 1) ^ (_third.hashCode() << 2) ^ 
           (_fourth.hashCode() << 3) ^ (_fifth.hashCode() << 4) ^ (_sixth.hashCode() << 5) ^ 
           (_seventh.hashCode() << 6);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, T5, T6, T7> 
    Septet<T1, T2, T3, T4, T5, T6, T7> make(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, 
                                            T6 sixth, T7 seventh) {
    return new Septet<T1, T2, T3, T4, T5, T6, T7>(first, second, third, fourth, fifth, sixth, seventh);
  }
  
}
