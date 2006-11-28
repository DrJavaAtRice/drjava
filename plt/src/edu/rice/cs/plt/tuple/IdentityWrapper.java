package edu.rice.cs.plt.tuple;

import edu.rice.cs.plt.lambda.Lambda;

/**
 * A wrapper that defines {@link #equals} and {@link #hashCode} in terms of its value's 
 * identity ({@code ==}) instead of equality (@code equals})
 */
public class IdentityWrapper<T> extends Wrapper<T> {
  
  public IdentityWrapper(T value) { super(value); }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and the
   *          wrapped values are identical (according to {@code ==})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Wrapper<?> cast = (Wrapper<?>) o;
      return _value == cast._value;
    }
  }
  
  protected int generateHashCode() { 
    return System.identityHashCode(_value) ^ getClass().hashCode();
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> IdentityWrapper<T> make(T value) { return new IdentityWrapper<T>(value); }
  
  /** Produce a lambda that invokes the constructor */
  public static <T> Lambda<T, Wrapper<T>> factory() {
    return new Lambda<T, Wrapper<T>>() {
      public Wrapper<T> value(T value) { return new IdentityWrapper<T>(value); }
    };
  }
  
}
