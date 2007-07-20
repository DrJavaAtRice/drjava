package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Collection;
import edu.rice.cs.plt.iter.FilteredIterator;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * The complement of a set {@code excluded} in a domain {@code domain} (alternatively,
 * {@code domain - excluded}), constructed lazily and updated dynamically.
 */
public class ComplementSet<E> extends AbstractSet<E> {
  
  private final Set<? extends E> _domain;
  private final Set<?> _excluded;
  
  public ComplementSet(Set<? extends E> domain, Set<?> excluded) {
    _domain = domain;
    _excluded = excluded;
  }
  
  /** Traversing is linear in the size of {@code domain}. */
  public Iterator<E> iterator() {
    Predicate<Object> filter = LambdaUtil.negate(CollectUtil.containsPredicate(_excluded));
    return new FilteredIterator<E>(_domain.iterator(), filter);
  }
  
  /** Linear in the size of {@code domain}. */
  public int size() {
    int result = 0;
    for (E elt : this) { result++; }
    return result;
  }
  
  /** Linear in the size of {@code domain}. */
  public boolean isEmpty() {
    if (_domain.isEmpty()) { return true; }
    else if (_excluded.isEmpty()) { return false; }
    else if (_domain == _excluded) { return true; }
    else { return _excluded.containsAll(_domain); }
  }
  
  public boolean contains(Object o) {
    return _domain.contains(o) && !(_excluded.contains(o));
  }
  
  public boolean containsAll(Collection<?> objs) {
    if (_domain.containsAll(objs)) {
      for (Object obj : objs) {
        if (_excluded.contains(obj)) { return false; }
      }
      return true;
    }
    else { return false; }
  }
  
}
