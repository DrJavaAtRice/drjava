package edu.rice.cs.plt.lambda;

import edu.rice.cs.plt.iter.IterUtil;

/** 
 * A predicate whose value is the disjunction ({@code ||}) of an arbitrary number of 
 * nested predicates
 */
public class Disjunction<T> implements Predicate<T> {
  
  private final Iterable<? extends Predicate<? super T>> _predicates;
  
  public Disjunction(Iterable<? extends Predicate<? super T>> predicates) {
    _predicates = predicates;
  }
  
  public Disjunction(Predicate<? super T> p1, Predicate<? super T> p2) {
    // A type checker bug won't allow the following:
    //_predicates = IterUtil.makeIterable(p1, p2);
    _predicates = IterUtil.<Predicate<? super T>>makeIterable(p1, p2);
  }
  
  public Boolean value(T arg) {
    boolean result = false;
    for (Predicate<? super T> p : _predicates) {
      result = result || p.value(arg);
      if (result == true) { break; }
    }
    return result;
  }

  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Disjunction<T> make(Iterable<? extends Predicate<? super T>> predicates) {
    return new Disjunction<T>(predicates);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Disjunction<T> make(Predicate<? super T> p1, Predicate<? super T> p2) {
    return new Disjunction<T>(p1, p2);
  }

}
