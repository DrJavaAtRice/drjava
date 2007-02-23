package edu.rice.cs.plt.tuple;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda2;

/**
 * An arbitrary pair of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Pair<T1, T2> extends Tuple {
  
  protected final T1 _first;
  protected final T2 _second;
  
  public Pair(T1 first, T2 second) {
    _first = first;
    _second = second;
  }
  
  public T1 first() { return _first; }
  public T2 second() { return _second; }
  
  public String toString() {
    return "(" + _first + ", " + _second + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Pair<?, ?> cast = (Pair<?, ?>) o;
      return 
        _first.equals(cast._first) &&
        _second.equals(cast._second);
    }
  }
  
  protected int generateHashCode() {
    return 
      _first.hashCode() ^ 
      (_second.hashCode() << 1) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2> Pair<T1, T2> make(T1 first, T2 second) {
    return new Pair<T1, T2>(first, second);
  }
  
  /** Produce a lambda that invokes the constructor */
  @SuppressWarnings("unchecked") public static <T1, T2> Lambda2<T1, T2, Pair<T1, T2>> factory() {
    return (Factory<T1, T2>) Factory.INSTANCE;
  }
  
  private static final class Factory<T1, T2> implements Lambda2<T1, T2, Pair<T1, T2>>, Serializable {
    public static final Factory<Object, Object> INSTANCE = new Factory<Object, Object>();
    private Factory() {}
    public Pair<T1, T2> value(T1 first, T2 second) { return new Pair<T1, T2>(first, second); }
  }
  
}
