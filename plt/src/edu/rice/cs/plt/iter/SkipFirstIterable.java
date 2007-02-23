package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.io.Serializable;

/**
 * Contains all but the first element of a wrapped iterable.  (If the wrapped iterable is
 * empty, this is empty as well.)  Changes made to the underlying list are reflected here.
 * This provides a general, but clumsy, way to decompose arbitrary iterables (that is, access 
 * the "rest" of some iterable).  Care should be taken in using this approach, however, as the 
 * first value is skipped on <em>every</em> invocation of {@code iterator}.  Thus, an iterable 
 * composed of multiple nested {@code SkipFirstIterable}s will have poor performance in 
 * comparison to other solutions.  For better performance or recursive list-decomposing 
 * algorithms, use a {@link edu.rice.cs.plt.collect.ConsList}.
 */
public class SkipFirstIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final Iterable<T> _iterable;
  
  public SkipFirstIterable(Iterable<T> iterable) { _iterable = iterable; }

  public Iterator<T> iterator() {
    Iterator<T> result = _iterable.iterator();
    if (result.hasNext()) { result.next(); }
    return result;
  }
  
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
  public static <T> SkipFirstIterable<T> make(Iterable<T> iterable) {
    return new SkipFirstIterable<T>(iterable);
  }
  
  /**
   * Create a {@code SkipFirstIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate traversal of the list.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(Iterable<T> iterable) { 
    return new SnapshotIterable<T>(new SkipFirstIterable<T>(iterable));
  }
}
