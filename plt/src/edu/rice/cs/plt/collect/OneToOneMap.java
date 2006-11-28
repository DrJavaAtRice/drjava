package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.Map;

/**
 * A map that enforces a one-to-one relationship between keys
 * and values.
 */
public interface OneToOneMap<K, V> extends Map<K, V> {
  
  /**
   * Produce the value corresponding to the given key, or {@code null} if
   * {@code containsKey(key)} does not hold.  Note that this essentially duplicates
   * the {@link Map#get()} method, but provides a stronger type signature along with
   * a name consistent with {@link #getKey()}.
   */
  public V getValue(K key);
  
  /**
   * Produce the key corresponding to the given value, or {@code null} if
   * {@code containsValue(value)} does not hold.
   */
  public K getKey(V value);
  
  /**
   * Remove the mapping corresponding to the given key, if any.  Return the matching
   * value, or {@code null} if there is none.  Note that this essentially duplicates
   * the {@link Map#remove()} method, but provides a stronger type signature along
   * with a name consistent with {@link #removeValue()}.
   */
  public V removeKey(K key);
  
  /**
   * Remove the mapping corresponding to the given value, if any.  Return the matching
   * key, or {@code null} if there is none.
   */
  public K removeValue(V value);
  
  /**
   * Remove the given key-value pair if it exists.  This provides a safer alternative
   * to {@link #removeKey()} or {@link #removeValue()}, because it requires clients to
   * explicitly state the key-value pair, avoiding erroneous assumptions.
   */
  public boolean remove(K key, V value);
  
  /**
   * Put the given key-value pair in the map.  The previous value corresponding to
   * {@code key}, if any, will be returned (otherwise, the result is {@code null}).
   * The previous key corresponding to {@code value}, on the other hand, will be
   * lost.
   */
  public V put(K key, V value);
  
  /**
   * Add all mappings in the given map to this map.  If {@code map} does not
   * represent a one-to-one correspondence -- that is, it associates the same
   * value with at least two keys -- only one key related to a duplicate value
   * will be added.  The choice of <em>which</em> key-value pair to use is 
   * unspecified.  Clients, then, cannot assume afterwards that
   * {@code this.entrySet().containsAll(map.entrySet())} holds.
   */
  public void putAll(Map<? extends K, ? extends V> map);
  
  /**
   * Produce the set of values contained by this map (this alters the return type
   * of the method, which is specified as {@code Collection<V>} in the {@link Map}
   * interface).
   */
  public Set<V> values();
  
  /**
   * Provide a view of this map in which the values have become keys, and vice versa.
   * Changes to either the new map or the original must be reflected in the other.
   */
  public OneToOneMap<V, K> reverse();
}
