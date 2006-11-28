package edu.rice.cs.plt.tuple;

import edu.rice.cs.plt.lambda.Lambda2;

/**
 * A pair that defines {@link #equals} and {@link #hashCode} in terms of its elements' 
 * identity ({@code ==}) instead of equality (@code equals})
 */
public class IdentityPair<T1, T2> extends Pair<T1, T2> {
  
  public IdentityPair(T1 first, T2 second) { super(first, second); }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is identical (according to {@code ==})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Pair<?, ?> cast = (Pair<?, ?>) o;
      return 
        _first == cast._first &&
        _second == cast._second;
    }
  }
  
  protected int generateHashCode() {
    return
      System.identityHashCode(_first) ^ 
      (System.identityHashCode(_second) << 1) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2> IdentityPair<T1, T2> make(T1 first, T2 second) {
    return new IdentityPair<T1, T2>(first, second);
  }
  
  /** Produce a lambda that invokes the constructor */
  public static <T1, T2> Lambda2<T1, T2, Pair<T1, T2>> factory() {
    return new Lambda2<T1, T2, Pair<T1, T2>>() {
      public Pair<T1, T2> value(T1 first, T2 second) {
        return new IdentityPair<T1, T2>(first, second);
      }
    };
  }
  
}
