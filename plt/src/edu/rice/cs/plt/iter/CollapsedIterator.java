package edu.rice.cs.plt.iter;

import java.util.Iterator;

/** An iterator over an arbitrary number of iterators.  Supports {@link #remove()}. */
public class CollapsedIterator<T> implements Iterator<T> {
  
  private Iterator<? extends T> _i;
  private Iterator<? extends T> _last; // to support remove()
  private final Iterator<? extends Iterator<? extends T>> _rest;
  
  /** The result traverses {@code i1}, then {@code i2} */
  public CollapsedIterator(Iterator<? extends Iterator<? extends T>> iters) {
    _i = EmptyIterator.make();
    _rest = iters;
    advance();
  }
  
  public boolean hasNext() { return _i.hasNext(); }

  public T next() {
    T result = _i.next();
    _last = _i;
    advance();
    return result;
  }
  
  public void remove() { _last.remove(); }
  
  private void advance() {
    while (!_i.hasNext() && _rest.hasNext()) { _i = _rest.next(); }
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> CollapsedIterator<T> make(Iterator<? extends Iterator<? extends T>> iters) {
    return new CollapsedIterator<T>(iters);
  }
    
}
