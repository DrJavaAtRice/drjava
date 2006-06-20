package edu.rice.cs.plt.iter;

import java.util.Iterator;

/**
 * An iterable that simply wraps an {@link Iterator}.  Unlike most iterables,
 * state changes on the wrapped iterator (such as advancing) are reflected across 
 * invocations of {@link #iterator}; once the iterator has been traversed,
 * this iterable is effectively empty.  While these semantics are inconvenient at
 * times, they also allow a very lightweight implementation.  Clients that need
 * to traverse an iterator multiple times (or that need a {@link SizedIterable})
 * can use a {@link SnapshotIterable} instead, at the expense of copying and
 * storing the entire contents of the iterator.
 */
public class ReadOnceIterable<T> implements Iterable<T> {
  
  private final Iterator<T> _iter;
  public ReadOnceIterable(Iterator<T> iter) { _iter = iter; }
  public Iterator<T> iterator() { return _iter; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ReadOnceIterable<T> make(Iterator<T> iter) { 
    return new ReadOnceIterable<T>(iter);
  }
  
}
