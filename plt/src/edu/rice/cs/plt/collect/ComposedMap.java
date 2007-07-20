package edu.rice.cs.plt.collect;

import java.util.Map;
import java.util.AbstractMap;
import java.util.Set;

/**
 * A lazily-constructed and dynamically-updated composition of two maps.  The first is designated
 * the <em>parent</em>, and the second the <em>child</em>.  Bindings for keys in the child shadow
 * those in the parent.
 */
public class ComposedMap<K, V> extends AbstractMap<K, V> {
  private final Map<? extends K, ? extends V> _parent;
  private final Map<? extends K, ? extends V> _child;
  private final Set<K> _keys;
  
  public ComposedMap(Map<? extends K, ? extends V> parent, Map<? extends K, ? extends V> child) {
    _parent = parent;
    _child = child;
    _keys = new UnionSet<K>(parent.keySet(), child.keySet());
  }
  
  public int size() { return _keys.size(); }
  
  public boolean isEmpty() { return _keys.isEmpty(); }
  
  public boolean containsKey(Object key) { return _keys.contains(key); }
  
  /**
   * In the worst case, takes as long as {@code parent.containsValue()} plus a linear
   * traversal of {@code child}.
   */
  public boolean containsValue(Object value) {
    if (_parent.containsValue(value)) { return true; }
    else {
      for (Map.Entry<? extends K, ? extends V> childEntry : _child.entrySet()) {
        Object childVal = childEntry.getValue();
        if (value == null ? childVal == null : value.equals(childVal)) {
          if (!_parent.containsKey(childEntry.getKey())) { return true; }
        }
      }
      return false;
    }
  }
  
  public V get(Object key) {
    if (_child.containsKey(key)) { return _child.get(key); }
    else { return _parent.get(key); }
  }
  
  public Set<K> keySet() { return _keys; }
  
  // inherit implementation of values
  
  public Set<Entry<K, V>> entrySet() { return new KeyDrivenEntrySet<K, V>(this); }
  
}
