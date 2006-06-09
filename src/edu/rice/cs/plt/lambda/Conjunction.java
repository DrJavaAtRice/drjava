package edu.rice.cs.plt.lambda;

import edu.rice.cs.plt.iter.IterUtil;

/** 
 * A predicate whose value is the conjunction ({@code &&}) of an arbitrary number of 
 * nested predicates
 */
public class Conjunction<T> implements Predicate<T> {
  
  private final Iterable<? extends Predicate<? super T>> _predicates;
  
  public Conjunction(Iterable<? extends Predicate<? super T>> predicates) {
    _predicates = predicates;
  }
  
  public Conjunction(Predicate<? super T> p1, Predicate<? super T> p2) {
    _predicates = IterUtil.<Predicate<? super T>>makeIterable(p1, p2);
  }

  public Boolean value(T arg) {
    boolean result = true;
    for (Predicate<? super T> p : _predicates) {
      result = result && p.value(arg);
      if (result == false) { break; }
    }
    return result;
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Conjunction<T> make(Iterable<? extends Predicate<? super T>> predicates) {
    return new Conjunction<T>(predicates);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Conjunction<T> make(Predicate<? super T> p1, Predicate<? super T> p2) {
    return new Conjunction<T>(p1, p2);
  }
 
}
