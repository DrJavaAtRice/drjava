package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Truncates a given iterator to have at most {@code size} elements.
 */
public class TruncatedIterator<T> implements Iterator<T> {
  
  private final Iterator<? extends T> _iter;
  private int _size;
  
  public TruncatedIterator(Iterator<? extends T> iter, int size) {
    _iter = iter;
    _size = size;
  }
  
  public boolean hasNext() { return _size > 0 && _iter.hasNext(); }
  
  public T next() {
    if (_size <= 0) { throw new NoSuchElementException(); }
    _size--;
    return _iter.next();
  }
  
  public void remove() { _iter.remove(); }
  
}
