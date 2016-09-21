/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.collect;

import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.MappedIterator;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * An abstract parent class for {@code Map} implementations that defines its operations in terms
 * of the key-based methods {@code get()} and {@code keySet()}.  This is an alternative to 
 * {@link java.util.AbstractMap}, which defines its operations in terms of {@code entrySet()} (which is
 * generally more difficult to implement, while its use as a basic operation leads to inefficient behavior).
 * Subclasses must implement {@link #get} and {@link #keySet}; to support mutation, they must also
 * implement {@link #put}, {@link #remove}, and {@link #clear}.
 */
public abstract class AbstractKeyBasedMap<K, V> implements LambdaMap<K, V> {
  
  public abstract V get(Object key);
  public abstract PredicateSet<K> keySet();
  
  /** Returns {@code get(key)}. */
  public V value(K key) { return get(key); }
  /** Returns {@code keySet().size()}. */
  public int size() { return keySet().size(); }
  /** Returns {@code keySet().isEmpty()}. */
  public boolean isEmpty() { return keySet().isEmpty(); }
  /** Returns {@code keySet().contains(key)}. */
  public boolean containsKey(Object key) { return keySet().contains(key); }
  
  /** Returns {@code IterUtil.contains(IterUtil.map(keySet(), this), val)}. */
  public boolean containsValue(Object val) {
    return IterUtil.contains(IterUtil.map(keySet(), this), val);
  }
  
  /** Return a collection backed by {@code IterUtil.map(keySet(), this)}. */
  public Collection<V> values() {
    return new IterableCollection<V>(IterUtil.map(keySet(), this));
  }
  
  /** Returns an instance of {@link EntrySet}. */
  public Set<Entry<K, V>> entrySet() {
    return new EntrySet();
  }
  
  /** Throws an {@link UnsupportedOperationException}. */
  public V put(K key, V val) { throw new UnsupportedOperationException(); }
  /** Throws an {@link UnsupportedOperationException}. */
  public V remove(Object key) { throw new UnsupportedOperationException(); }
  /** Throws an {@link UnsupportedOperationException}. */
  public void clear() { throw new UnsupportedOperationException(); }
  
  /** Invokes {@link #put} for each element of the given map's entry set. */
  public void putAll(Map<? extends K, ? extends V> elts) {
    for (Entry<? extends K, ? extends V> entry : elts.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }
  
  public String toString() { return IterUtil.toString(entrySet(), "{", ", ", "}"); }
  
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (!(o instanceof Map<?, ?>)) { return false; }
    else { return entrySet().equals(((Map<?, ?>) o).entrySet()); }
  }
  
  public int hashCode() { return entrySet().hashCode(); }
    
  
  /** An entry set defined in terms of the enclosing map's other methods. */
  protected class EntrySet extends AbstractPredicateSet<Entry<K, V>> {
    
    /**
     * Test whether an entry is present by invoking {@code containsKey()} and
     * {@code get()}.
     */
    @Override public boolean contains(Object o) {
      if (o instanceof Entry<?, ?>) {
        Entry<?, ?> entry = (Entry<?, ?>) o;
        Object key = entry.getKey();
        if (containsKey(key)) {
          Object val = entry.getValue();
          Object mapVal = get(key);
          return (val == null) ? (mapVal == null) : val.equals(mapVal);
        }
      }
      return false;
    }
    
    /** Create an iterator based on {@link #mapEntryForKey}. */
    public Iterator<Entry<K, V>> iterator() {
      return MappedIterator.make(keySet().iterator(), new Lambda<K, Entry<K, V>>() {
        public Entry<K, V> value(K key) {
          return mapEntryForKey(AbstractKeyBasedMap.this, key);
        }
      });
    }
    
    /** Delegate to {@code keySet()}. */
    public boolean isInfinite() { return keySet().isInfinite(); }
    /** Delegate to {@code keySet()}. */
    public boolean hasFixedSize() { return keySet().hasFixedSize(); }
    /** False: can't guarantee that values won't change. */
    public boolean isStatic() { return false; }

    /** Delegate to the map's {@code isEmpty()} method. */
    @Override public boolean isEmpty() { return AbstractKeyBasedMap.this.isEmpty(); }
    /** Delegate to the map's {@code size()} method. */
    @Override public int size() { return AbstractKeyBasedMap.this.size(); }
    /** Delegate to {@code keySet()}. */
    @Override public int size(int bound) { return keySet().size(bound); }
    
    /** Delegate to the map's {@code put()} method. */
    @Override public boolean add(Entry<K, V> entry) {
      boolean present = contains(entry);
      AbstractKeyBasedMap.this.put(entry.getKey(), entry.getValue());
      return !present;
    }
    
    /** Delegate to the map's {@code remove()} method. */
    @Override public boolean remove(Object o) {
      if (o instanceof Entry<?, ?>) {
        Entry<?, ?> entry = (Entry<?, ?>) o;
        boolean present = containsKey(entry.getKey());
        AbstractKeyBasedMap.this.remove(entry.getKey());
        return present;
      }
      else { return false; }
    }
    
    /** Delegate to the map's {@code clear()} method. */
    @Override public void clear() {
      AbstractKeyBasedMap.this.clear();
    }
    
  }
  
  /**
   * Define a map entry in terms of a map's {@code get()} and {@code put()} methods.
   * For the result to be a valid entry in the given map, the map must contain the given key.
   */
  protected static <K, V> Entry<K, V> mapEntryForKey(final Map<K, V> map, final K key) {
    return new Entry<K, V>() {
      public K getKey() { return key; }
      public V getValue() { return map.get(key); }
      public V setValue(V value) { return map.put(key, value); }
      public boolean equals(Object o) {
        if (this == o) { return true; }
        else if (!(o instanceof Entry<?, ?>)) { return false; }
        else {
          Entry<?, ?> cast = (Entry<?, ?>) o;
          if (key == null ? cast.getKey() == null : key.equals(cast.getKey())) {
            V val = map.get(key);
            return val == null ? cast.getValue() == null : val.equals(cast.getValue());
          }
          else { return false; }
        }
      }
      public int hashCode() {
        V val = map.get(key);
        return (key == null ? 0 : key.hashCode()) ^ (val == null ? 0 : val.hashCode());
      }
    };
  }
  
}
