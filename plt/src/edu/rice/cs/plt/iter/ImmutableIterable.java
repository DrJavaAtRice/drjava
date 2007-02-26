package edu.rice.cs.plt.iter;

import java.io.Serializable;

/**
 * Wraps an iterable in an immutable interface, thus allowing internal data structures to be treated 
 * by clients as iterables without allowing access (via casting) to their mutating methods.  
 * Also ensures that the {@link java.util.Iterator#remove()} method of the provided Iterator is not 
 * supported.  Note that only only <em>this</em> interface with the data is immutable --
 * if the original data structure is mutable, a client with direct access to that structure can
 * still mutate it.  To guarantee that no mutation will take place, use a {@link SnapshotIterable} instead,
 * which makes an immutable copy.
 */
public class ImmutableIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final Iterable<? extends T> _iterable;
  
  public ImmutableIterable(Iterable<? extends T> iterable) { _iterable = iterable; }
  public ImmutableIterator<T> iterator() { return new ImmutableIterator<T>(_iterable.iterator()); }
  public int size() { return IterUtil.sizeOf(_iterable); }
  public int size(int bound) { return IterUtil.sizeOf(_iterable, bound); }
  public boolean isFixed() { return IterUtil.isFixed(_iterable); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ImmutableIterable<T> make(Iterable<? extends T> iterable) {
    return new ImmutableIterable<T>(iterable);
  }
  
}
