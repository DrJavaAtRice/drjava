package edu.rice.cs.plt.iter;

import java.io.Serializable;

/**
 * Contains, for some value {@code size}, the first {@code size} elements of a nested iterable.
 * (If the nested list has less than {@code size} elements, this iterable is identical.)
 * Changes made to the underlying list are reflected here.
 */
public class TruncatedIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final Iterable<? extends T> _iterable;
  protected final int _size;
  
  public TruncatedIterable(Iterable<? extends T> iterable, int size) {
    if (size < 0) { throw new IllegalArgumentException("size < 0"); }
    _iterable = iterable;
    _size = size;
  }

  public TruncatedIterator<T> iterator() {
    return new TruncatedIterator<T>(_iterable.iterator(), _size);
  }

  /**
   * @return  {@code size}, unless the nested iterable is smaller than {@code size}; in that
   *          case, returns the iterable's size
   */
  public int size() { return IterUtil.sizeOf(_iterable, _size); }
  
  public int size(int bound) { return IterUtil.sizeOf(_iterable, _size <= bound ? _size : bound); }
    
  public boolean isFixed() { return IterUtil.isFixed(_iterable); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> TruncatedIterable<T> make(Iterable<? extends T> iterable, int size) {
    return new TruncatedIterable<T>(iterable, size);
  }
  
  /**
   * Create a {@code TruncatedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate traversal of the list.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(Iterable<? extends T> iterable, int size) { 
    return new SnapshotIterable<T>(new TruncatedIterable<T>(iterable, size));
  }
}
