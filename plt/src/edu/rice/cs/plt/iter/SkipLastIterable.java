package edu.rice.cs.plt.iter;

import java.io.Serializable;

/**
 * Contains all but the last element of a wrapped iterable.  (If the wrapped iterable is
 * empty, this is empty as well.)  Changes made to the underlying list are reflected here.
 * This provides a general, but clumsy, way to decompose arbitrary iterables.  Care should 
 * be taken in using this approach, however, as the iterator must, on creation, cache a value and
 * check that it is last.  Thus, an iterable composed of multiple nested {@code SkipLastIterable}s 
 * will have poor performance in comparison to other solutions.  For better performance or recursive 
 * list-decomposing algorithms, use a {@link edu.rice.cs.plt.collect.ConsList}.
 */
public class SkipLastIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final Iterable<? extends T> _iterable;
  
  public SkipLastIterable(Iterable<? extends T> iterable) { _iterable = iterable; }

  public SkipLastIterator<T> iterator() { return new SkipLastIterator<T>(_iterable.iterator()); }
  
  public int size() {
    int nestedSize = IterUtil.sizeOf(_iterable);
    if (nestedSize == 0) { return 0; }
    else if (nestedSize == Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
    else { return nestedSize - 1; }
  }
  
  public int size(int bound) {
    if (bound == Integer.MAX_VALUE) { return size(); }
    else {
      int nestedSize = IterUtil.sizeOf(_iterable, bound + 1);
      return (nestedSize == 0) ? 0 : nestedSize - 1;
    }
  }
  public boolean isFixed() { return IterUtil.isFixed(_iterable); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SkipLastIterable<T> make(Iterable<? extends T> iterable) {
    return new SkipLastIterable<T>(iterable);
  }
  
  /**
   * Create a {@code SkipLastIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate traversal of the list.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(Iterable<? extends T> iterable) { 
    return new SnapshotIterable<T>(new SkipLastIterable<T>(iterable));
  }
}
