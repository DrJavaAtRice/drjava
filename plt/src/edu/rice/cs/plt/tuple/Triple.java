package edu.rice.cs.plt.tuple;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda3;

/**
 * An arbitrary 3-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Triple<T1, T2, T3> extends Tuple {
  
  protected final T1 _first;
  protected final T2 _second;
  protected final T3 _third;
  
  public Triple(T1 first, T2 second, T3 third) { 
    _first = first;
    _second = second;
    _third = third;
  }
  
  public T1 first() { return _first; }
  public T2 second() { return _second; }
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
  
  /** Produce a lambda that invokes the constructor */
  @SuppressWarnings("unchecked") public static <T1, T2, T3> Lambda3<T1, T2, T3, Triple<T1, T2, T3>> factory() {
    return (Factory<T1, T2, T3>) Factory.INSTANCE;
  }
  
  private static final class Factory<T1, T2, T3> implements Lambda3<T1, T2, T3, Triple<T1, T2, T3>>, Serializable {
    public static final Factory<Object, Object, Object> INSTANCE = new Factory<Object, Object, Object>();
    private Factory() {}
    public Triple<T1, T2, T3> value(T1 first, T2 second, T3 third) {
      return new Triple<T1, T2, T3>(first, second, third);
    }
  }

}
