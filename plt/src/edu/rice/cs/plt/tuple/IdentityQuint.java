package edu.rice.cs.plt.tuple;

/**
 * A quint that defines {@link #equals} and {@link #hashCode} in terms of its elements' 
 * identity ({@code ==}) instead of equality (@code equals})
 */
public class IdentityQuint<T1, T2, T3, T4, T5> extends Quint<T1, T2, T3, T4, T5> {

  public IdentityQuint(T1 first, T2 second, T3 third, T4 fourth, T5 fifth) {
    super(first, second, third, fourth, fifth);
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is identical (according to {@code ==})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (! getClass().equals(o.getClass())) { return false; }
    else {
      Quint<?, ?, ?, ?, ?> cast = (Quint<?, ?, ?, ?, ?>) o;
      return 
        _first == cast._first &&
        _second == cast._second &&
        _third == cast._third &&
        _fourth == cast._fourth &&
        _fifth == cast._fifth;
    }
  }
  
  protected int generateHashCode() {
    return 
      System.identityHashCode(_first) ^ 
      (System.identityHashCode(_second) << 1) ^ 
      (System.identityHashCode(_third) << 2) ^ 
      (System.identityHashCode(_fourth) << 3) ^ 
      (System.identityHashCode(_fifth) << 4) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, T5> 
    IdentityQuint<T1, T2, T3, T4, T5> make(T1 first, T2 second, T3 third, 
                                           T4 fourth, T5 fifth) {
    return new IdentityQuint<T1, T2, T3, T4, T5>(first, second, third, fourth, fifth);
  }
  
}
