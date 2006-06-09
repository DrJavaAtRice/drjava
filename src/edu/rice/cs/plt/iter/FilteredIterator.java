package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Predicate;

/**
 * An Iterator that only returns the values in another Iterator ({@code i}) for which some
 * predicate ({@code p}) holds.  Does not support {@link #remove()}.
 */
public class FilteredIterator<T> implements Iterator<T> {
  
  private final Predicate<? super T> _p;
  private final Iterator<? extends T> _i;
  private T _lookahead;
  
  public FilteredIterator(Iterator<? extends T> i, Predicate<? super T> p) {
    _p = p;
    _i = i;
    advanceLookahead();
  }
  
  public boolean hasNext() { return _lookahead != null; }
  
  public T next() {
    T result = _lookahead;
    advanceLookahead();
    return result;
  }
  
  public void remove() { throw new UnsupportedOperationException(); } 
  
  /**
   * Finds the next value in {@code _i} for which {@code _p} holds.
   * Ignores the previous value of {@code _lookahead}.  If a value is
   * found, sets {@code _lookahead} to that value; otherwise, sets it to 
   * {@code null}.
   */
  private void advanceLookahead() {
    _lookahead = null;
    while (_i.hasNext() && _lookahead == null) {
      T next = _i.next();
      if (_p.value(next)) { _lookahead = next; }
    }
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> FilteredIterator<T> make(Iterator<? extends T> i, Predicate<? super T> p) {
    return new FilteredIterator<T>(i, p);
  }
  
}
