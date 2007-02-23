package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.io.Serializable;

/**
 * Creates an iterable based on the result of immediately traversing some other
 * iterable; generated iterators will traverse those same values in the same order.  
 * Changes to the wrapped iterable will <em>not</em> be reflected.
 */
public class SnapshotIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final SizedIterable<T> _values;
  
  public SnapshotIterable(Iterable<? extends T> iterable) {
    @SuppressWarnings("unchecked") SizedIterable<T> vals = (EmptyIterable<T>) EmptyIterable.INSTANCE;
    for (T e : iterable) { vals = new ComposedIterable<T>(vals, e); }
    _values = vals;
  }
  
  public SnapshotIterable(Iterator<? extends T> iterator) {
    @SuppressWarnings("unchecked") SizedIterable<T> vals = (EmptyIterable<T>) EmptyIterable.INSTANCE;
    while (iterator.hasNext()) { vals = new ComposedIterable<T>(vals, iterator.next()); }
    _values = vals;
  }
    
  public Iterator<T> iterator() { return _values.iterator(); }
  public int size() { return _values.size(); }
  public int size(int bound) { return _values.size(bound); }
  public boolean isFixed() { return true; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SnapshotIterable<T> make(Iterable<? extends T> iterable) {
    return new SnapshotIterable<T>(iterable);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SnapshotIterable<T> make(Iterator<? extends T> iterator) {
    return new SnapshotIterable<T>(iterator);
  }
}
