package edu.rice.cs.plt.tuple;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda4;

/**
 * An arbitrary 4-tuple of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Quad<T1, T2, T3, T4> extends Tuple {
  
  protected final T1 _first;
  protected final T2 _second;
  protected final T3 _third;
  protected final T4 _fourth;
  
  public Quad(T1 first, T2 second, T3 third, T4 fourth) {
    _first = first;
    _second = second;
    _third = third;
    _fourth = fourth;
  }
  
  public T1 first() { return _first; }
  public T2 second() { return _second; }
  public T3 third() { return _third; }
  public T4 fourth() { return _fourth; }

  public String toString() {
    return "(" + _first + ", " + _second + ", " + _third + ", " + _fourth + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
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
    return 
      _first.hashCode() ^ 
      (_second.hashCode() << 1) ^ 
      (_third.hashCode() << 2) ^ 
      (_fourth.hashCode() << 3) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4> Quad<T1, T2, T3, T4> make(T1 first, T2 second, T3 third, 
                                                           T4 fourth) {
    return new Quad<T1, T2, T3, T4>(first, second, third, fourth);
  }
  
  /** Produce a lambda that invokes the constructor */
  @SuppressWarnings("unchecked") public static 
    <T1, T2, T3, T4> Lambda4<T1, T2, T3, T4, Quad<T1, T2, T3, T4>> factory() {
    return (Factory<T1, T2, T3, T4>) Factory.INSTANCE;
  }
   
  private static final class Factory<T1, T2, T3, T4> 
    implements Lambda4<T1, T2, T3, T4, Quad<T1, T2, T3, T4>>, Serializable {
    public static final Factory<Object, Object, Object, Object> INSTANCE = 
      new Factory<Object, Object, Object, Object>();
    private Factory() {}
    public Quad<T1, T2, T3, T4> value(T1 first, T2 second, T3 third, T4 fourth) {
      return new Quad<T1, T2, T3, T4>(first, second, third, fourth);
    }
  }
  
}
