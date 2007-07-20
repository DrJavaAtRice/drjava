package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.ComposedIterator;

/**
 * The union of two sets, lazily constructed and updated dynamically.
 */
public class UnionSet<E> extends AbstractSet<E> {
  private final Set<? extends E> _set1;
  private final Set<? extends E> _set2;
  private final Set<? extends E> _set2Extras;
  
  /**
   * For best performance of {@link #size}, {@code set2} should be the smaller
   * of the two sets (this is not handled automatically because calculating sizes may be expensive).
   */
  public UnionSet(Set<? extends E> set1, Set<? extends E> set2) {
    _set1 = set1;
    _set2 = set2;
    _set2Extras = new ComplementSet<E>(set2, set1);
  }
  
  public Iterator<E> iterator() {
    return new ImmutableIterator<E>(new ComposedIterator<E>(_set1.iterator(), _set2Extras.iterator()));
  }
  
  /** Linear in the size of {@code set2}. */
  public int size() {
    return _set1.size() + _set2Extras.size();
  }
  
  public boolean isEmpty() {
    return _set1.isEmpty() && _set2.isEmpty();
  }
  
  public boolean contains(Object o) {
    return _set1.contains(o) || _set2.contains(o);
  }
  
  // inherit default implementation of containsAll
  
}
