package edu.rice.cs.plt.collect;

import java.util.Map.Entry;

/**
 * An implementation of {@link Entry} that does not support
 * the {@link #setValue()} operation; all other methods are
 * delegated to a wrapped {@code Entry}.
 */
public class ImmutableMapEntry<K, V> implements Entry<K, V> {
  
  private Entry<K, V> _entry;
  
  public ImmutableMapEntry(Entry<K, V> entry) { _entry = entry; }
  
  public K getKey() { return _entry.getKey(); }
  public V getValue() { return _entry.getValue(); }
  public V setValue(V value) { throw new UnsupportedOperationException(); }
  public String toString() { return _entry.toString(); }
  public boolean equals(Object o) { return _entry.equals(o); }
  public int hashCode() { return _entry.hashCode(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> ImmutableMapEntry<K, V> make(Entry<K, V> entry) {
    return new ImmutableMapEntry<K, V>(entry);
  }
}
