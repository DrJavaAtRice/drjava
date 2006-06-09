package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Creates an iterable of the input in reverse order.  Like {@link SnapshotIterable},
 * the original list is read immediately, and subsequent changes will not be reflected
 * here.
 */
public class ReverseIterable<T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private final SizedIterable<T> _values;
  
  public ReverseIterable(Iterable<? extends T> iterable) {
    SizedIterable<T> vals = EmptyIterable.make();
    for (T e : iterable) { vals = ComposedIterable.make(e, vals); }
    _values = vals;
  }
  
  public Iterator<T> iterator() { return _values.iterator(); }
  public int size() { return _values.size(); }
  public boolean isFixed() { return true; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ReverseIterable<T> make(Iterable<? extends T> iterable) {
    return new ReverseIterable<T>(iterable);
  }
}
