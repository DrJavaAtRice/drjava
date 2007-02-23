package edu.rice.cs.plt.iter;

import java.io.Serializable;
import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * Collapses a list of lists into a single list.  Subsequent changes to the list or its sublists will be
 * reflected.
 */
public class CollapsedIterable<T> extends AbstractIterable<T> 
  implements SizedIterable<T>, OptimizedLastIterable<T>, Serializable {
  
  private final Iterable<? extends Iterable<? extends T>> _iters;
  
  public CollapsedIterable(Iterable<? extends Iterable<? extends T>> iters) { _iters = iters; }
    
  public CollapsedIterator<T> iterator() {
    Iterator<? extends Iterator<? extends T>> i =
      new MappedIterable<Iterable<? extends T>, Iterator<? extends T>>(_iters, GetIterator.<T>make()).iterator();
    return new CollapsedIterator<T>(i);
  }
  
  public int size() {
    int result = 0;
    for (Iterable<?> iter : _iters) {
      result += IterUtil.sizeOf(iter);
      if (result < 0) { result = Integer.MAX_VALUE; break; } // overflow
    }
    return result;
  }
  
  public int size(int bound) {
    int result = 0;
    for (Iterable<?> iter : _iters) {
      result += IterUtil.sizeOf(iter);
      if (result >= bound) { break; }
      else if (result < 0) { result = Integer.MAX_VALUE; break; } // overflow
    }
    return result <= bound ? result : bound;
  }
  
  public boolean isFixed() {
    if (!IterUtil.isFixed(_iters)) { return false; }
    for (Iterable<?> iter : _iters) {
      if (!IterUtil.isFixed(iter)) { return false; }
    }
    return true;
  }
  
  /**
   * Determine the last value in the iterable.  This implementation will usually be faster than
   * the general approach of iterating through the entire list, because it only iterates through
   * the top-level list to find the last non-empty nested list.
   */
  public T last() {
    Iterable<? extends T> lastNonEmpty = null;
    for (Iterable<? extends T> iter : _iters) {
      if (lastNonEmpty == null || !IterUtil.isEmpty(iter)) { lastNonEmpty = iter; }
    }
    return IterUtil.last(lastNonEmpty);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> CollapsedIterable<T> make(Iterable<? extends Iterable<? extends T>> iters) {
    return new CollapsedIterable<T>(iters);
  }
  
  private static final class GetIterator<T>
    implements Lambda<Iterable<? extends T>, Iterator<? extends T>>, Serializable {
    public static final GetIterator<Object> INSTANCE = new GetIterator<Object>();
    @SuppressWarnings("unchecked") public static <T> GetIterator<T> make() { return (GetIterator<T>) INSTANCE; }
    private GetIterator() {}
    public Iterator<? extends T> value(Iterable<? extends T> iter) { return iter.iterator(); }
  }
  
}
