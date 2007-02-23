package edu.rice.cs.plt.iter;

import java.util.Iterator;

/**
 * Wraps an iterator in an immutable interface, preventing modifications to underlying data  
 * structures via {@link #remove()}.
 */
public class ImmutableIterator<T> extends ReadOnlyIterator<T> {
  
  private final Iterator<? extends T> _i;
  
  public ImmutableIterator(Iterator<? extends T> i) { _i = i; }
  public boolean hasNext() { return _i.hasNext(); }
  public T next() { return _i.next(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ImmutableIterator<T> make(Iterator<? extends T> i) {
    return new ImmutableIterator<T>(i);
  }
}
