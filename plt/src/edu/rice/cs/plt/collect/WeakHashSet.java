package edu.rice.cs.plt.collect;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * A Set implementation that has similar semantics to {@link java.util.WeakHashMap}.
 * The elements in this set are weakly held, so when the element is not longer
 * strongly reachable, the element will be removed from the set.
 */
public class WeakHashSet<T> extends AbstractSet<T> {
  /** A non-null value used to keep track of whether items were already
    * in the set.
    */
  private static final Object NOT_NULL = new Object();
  
  /**
   * This set is implemented on top of a WeakHashMap. The elements of
   * the set are stored as the keys of the WeakHashMap, while the value
   * is set to {@link NOT_NULL}
   */
  private WeakHashMap<T, Object> _items;
  
  /**
   * Constructs a new {@code WeakHashSet}.
   */
  public WeakHashSet() {
    _items = new WeakHashMap<T, Object>();
  }
  
  /**
   * Returns the number of elements in the {@code WeakHashSet}.
   * Initially, the {@code WeakHashSet} is empty, so size returns {@code 0}.
   * 
   * @return The number of elements in the set
   */
  public int size() {
    return _items.size();
  }
  
  /**
   * Adds the given item to the set. If the item is already in the set,
   * it will not be added again.
   * 
   * @param item The item to add
   * @return {@code true} if the item was not already in the set, {@code false} otherwise
   */
  public boolean add(T item) {
    //if put returns null, item was not in the set before
    return _items.put(item, NOT_NULL) == null;
  }
  
  /**
   * Removes all items from the set. After calling {@code clear},
   * {@code size} will return {@code 0}.
   */
  public void clear() {
    _items.clear();
  }
  
  /**
   * Returns a boolean value indicating whether the given object is a
   * member of the set.
   * 
   * @param o The object to look for
   * @return {@code true} if the object exists in the set, {@code false} otherwise
   */
  public boolean contains(Object o) {
    return _items.containsKey(o);
  }
  
  /**
   * Removes the given item from the set, if it was in the set.
   * 
   * @param o The item to remove
   * @return {@code true} if the item was in the set and has been removed, otherwise (when the item was not in the set) {@code false}
   */
  public boolean remove(Object o) {
    //if remove returns null, o was not in the set
    return _items.remove(o) != null;
  }

  /**
   * Returns an iterator for the elements in the set. The iterator is
   * not guaranteed to be backed by the live set. However, not of the
   * elements will be {@code null}. The {@code remove} operation is
   * implemented.
   * 
   * @return An iterator for the elements of the set
   */
  public Iterator<T> iterator() {
    return _items.keySet().iterator();
  }
}
