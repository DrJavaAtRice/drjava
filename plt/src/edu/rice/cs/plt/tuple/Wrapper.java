package edu.rice.cs.plt.tuple;

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * An arbitrary 1-tuple; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Wrapper<T> extends Option<T> implements Thunk<T> {
  
  protected final T _value;
  
  public Wrapper(T value) { _value = value; }
  
  public T value() { return _value; }
  
  public <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor) {
    return visitor.forSome(_value);
  }
  
  public String toString() { return "(" + _value + ")"; }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and the
   *          wrapped values are equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Wrapper<?> cast = (Wrapper<?>) o;
      return _value.equals(cast._value);
    }
  }
  
  protected int generateHashCode() { return _value.hashCode() ^ getClass().hashCode(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Wrapper<T> make(T value) { return new Wrapper<T>(value); }
  
  /** Produce a lambda that invokes the constructor */
  public static <T> Lambda<T, Wrapper<T>> factory() {
    return new Lambda<T, Wrapper<T>>() {
      public Wrapper<T> value(T value) { return new Wrapper<T>(value); }
    };
  }
  
}
