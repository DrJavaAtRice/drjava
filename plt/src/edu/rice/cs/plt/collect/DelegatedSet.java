package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Collection;

/**
 * An implementation of set that delegates all operations to a wrapped
 * set.  Subclasses can override a few of the methods, while maintaining
 * the default delegation behavior in most cases.  Subclasses can also
 * invoke the overridden methods in {@link AbstractSet} to use the
 * default implementations there by invoking, for example,
 * {@link #abstractSetAddAll} (see {@link AbstractSet} and
 * {@link java.util.AbstractCollection} for details on the default
 * implementations).
 */
public abstract class DelegatedSet<T> extends AbstractSet<T> {
  
  protected Set<T> _delegate;
  
  protected DelegatedSet(Set<T> delegate) { _delegate = delegate; }
  
  public int size() { return _delegate.size(); }
  public boolean isEmpty() { return _delegate.isEmpty(); }
  public boolean contains(Object o) { return _delegate.contains(o); }
  public Iterator<T> iterator() { return _delegate.iterator(); }
  public Object[] toArray() { return _delegate.toArray(); }
  public <T> T[] toArray(T[] a) { return _delegate.toArray(a); }
  public boolean add(T o) { return _delegate.add(o); }
  public boolean remove(Object o) { return _delegate.remove(o); }
  public boolean containsAll(Collection<?> c) { return _delegate.containsAll(c); }
  public boolean addAll(Collection<? extends T> c) { return _delegate.addAll(c); }
  public boolean retainAll(Collection<?> c) { return _delegate.retainAll(c); }
  public boolean removeAll(Collection<?> c) { return _delegate.removeAll(c); }
  public void clear() { _delegate.clear(); }
  public String toString() { return _delegate.toString(); }
  public boolean equals(Object o) { return _delegate.equals(o); }
  public int hashCode() { return _delegate.hashCode(); }
  
  protected boolean abstractSetIsEmpty() { return super.isEmpty(); }
  protected boolean abstractSetContains(Object o) { return super.contains(o); }
  protected Object[] abstractSetToArray() { return super.toArray(); }
  protected <T> T[] abstractSetToArray(T[] a) { return super.toArray(a); }
  protected boolean abstractSetRemove(T o) { return super.remove(o); }
  protected boolean abstractSetContainsAll(Collection<?> c) { return super.containsAll(c); }
  protected boolean abstractSetAddAll(Collection<? extends T> c) { return super.addAll(c); }
  protected boolean abstractSetRetainAll(Collection<?> c) { return super.retainAll(c); }
  protected boolean abstractSetRemoveAll(Collection<?> c) { return super.removeAll(c); }
  protected void abstractSetClear() { super.clear(); }
  protected String abstractSetToString() { return super.toString(); }
  protected boolean abstractSetEquals(Object o) { return super.equals(o); }
  protected int abstractSetHashCode() { return super.hashCode(); }
  
}
