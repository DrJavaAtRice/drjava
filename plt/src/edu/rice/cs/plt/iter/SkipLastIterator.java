package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Predicate;

/**
 * An iterator that skips the last element of a nested iterator.
 * Since it must read ahead to check if the current element is the last,
 * {@code remove} is not supported.
 */
public class SkipLastIterator<T> extends ReadOnlyIterator<T> {
  
  private final Iterator<? extends T> _i;
  private T _lookahead;
  
  public SkipLastIterator(Iterator<? extends T> i) {
    _i = i;
    if (_i.hasNext()) { _lookahead = _i.next(); }
    else { _lookahead = null; }
  }
  
  public boolean hasNext() { return _i.hasNext(); }
  
  public T next() {
    T result = _lookahead;
    _lookahead = _i.next();
    return result;
  }
  
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SkipLastIterator<T> make(Iterator<? extends T> i) {
    return new SkipLastIterator<T>(i);
  }
  
}
