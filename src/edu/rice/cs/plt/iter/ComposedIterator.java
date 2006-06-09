package edu.rice.cs.plt.iter;

import java.util.Iterator;

/**
 * Defines an iterator by composing two other iterators.  Supports {@link #remove()}.
 */
public class ComposedIterator<T> implements Iterator<T> {
  
  private Iterator<? extends T> _i;
  private Iterator<? extends T> _rest;
  
  /** The result traverses {@code i1}, then {@code i2} */
  public ComposedIterator(Iterator<? extends T> i1, Iterator<? extends T> i2) {
    _i = i1;
    _rest = i2;
  }
  
  public boolean hasNext() { return _i.hasNext() || (_rest != null && _rest.hasNext()); }
  
  public T next() {
    if (_rest != null && !_i.hasNext()) { _i = _rest; _rest = null; }
    return _i.next();
  }
  
  public void remove() { _i.remove(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterator<T> make(Iterator<? extends T> i1, Iterator<? extends T> i2) {
    return new ComposedIterator<T>(i1, i2);
  }
    
}
