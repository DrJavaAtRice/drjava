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

import java.util.*;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.IterUtil;

/** Maps from a key to a set of values; a key may be added multiple times with different values, and those values are 
  * collected in a set.  Each set is ordered according to some Comparable corresponding to each value.  The methods 
  * provided are modeled after the {@link java.util.Map} interface.  However, the container being modeled is not exactly
  * a map, and some of the Map methods do not make sense in this context.  For example, {@code put(key, value)} was 
  * replaced here by {@code put(key, value, orderBy)}.
  */
public class ExternallySortedMultiMap<K, V, C extends Comparable<? super C>> {
  
  /** Maps from a key to a <em>non-empty</em> set. */
  private final Map<K, ExternallySortedSet<V, C>> _map;

  /**
   * Caches the value to eliminate the need to traverse the map.
   * <em>Must</em> be updated whenever changes are made to _map (thus, providing
   * indirect access to mutation on _map is generally a bad idea).
   */
  private int _size;
  
  /** Create an empty map. */
  public ExternallySortedMultiMap() {
    _map = new HashMap<K, ExternallySortedSet<V, C>>();
    _size = 0;
  }
  
  /** @return  The current number of (key, value) pairs in the map. */
  public int size() { return _size; }
  
  /** @return  The current number of (key, value) pairs in the map, or {@code bound} if it is less. */
  public int size(int bound) { return _size <= bound ? _size : bound; }
  
  /** @return  {@code true} iff {@code size() == 0}. */
  public boolean isEmpty() { return _size == 0; }
  
  /** @return  {@code true} iff the specified key is mapped to at least 1 value. */
  public boolean containsKey(K key) { return _map.containsKey(key); }
  
  /** @return  {@code true} iff the specified value is associated with some key. */
  public boolean containsValue(V value) {
    for (ExternallySortedSet<V, C> set : _map.values()) {
      if (set.contains(value)) { return true; }
    }
    return false;
  }
  
  /** @return  {@code true} iff the specified (key, value) pair is in the map. */
  public boolean contains(K key, V value) {
    ExternallySortedSet<V, C> set = _map.get(key);
    return (set != null) && (set.contains(value));
  }
  
  /**
   * @return  A dynamically-updated view of the values associated with the given key, sorted
   *          by their corresponding {@code orderBy} values.  If the key maps to no values when 
   *          {@link Iterable#iterator()} is invoked, a 0-length iterator is returned.  
   *          {@link Iterator#remove()} is not supported.
   */
  public Iterable<V> get(final K key) {
    return new Iterable<V>() {
      public Iterator<V> iterator() {
        ExternallySortedSet<V, C> set = _map.get(key);
        if (set == null) { return EmptyIterator.make(); }
        else { return ImmutableIterator.make(set.iterator()); }
      }
    };
  }
  
  /**
   * Adds the (key, value) pair if it is not already present.
   * @return  {@code true} iff the (key, value) pair is not already present in the map.
   */
  public boolean put(K key, V value, C orderBy) {
    ExternallySortedSet<V, C> set = _map.get(key);
    if (set == null) { set = new ExternallySortedSet<V, C>(); _map.put(key, set); }
    if (set.add(value, orderBy)) { _size++; return true; }
    else { return false; }
  }
  
  /**
   * Removes the (key, value) pair if it is present.
   * @return  {@code true} iff the (key, value) pair was present in (and thus removed from) 
   *          the map.
   */
  public boolean remove(K key, V value) {
    ExternallySortedSet<V, C> set = _map.get(key);
    if (set == null) { return false; }
    else {
      if (set.remove(value)) {
        _size--;
        if (set.isEmpty()) { _map.remove(key); }
      }
      else {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Removes all values associated with the specified key.
   * @return  {@code true} iff the operation modifies the map.
   */
  public boolean removeKey(K key) {
    ExternallySortedSet<V, C> set = _map.get(key);
    if (set == null) { return false; }
    else { _map.remove(key); _size -= set.size(); return true; }
  }
  
  /** Adds all (key, value) pairs represented by {@code map} to this map.  If a mapping is already 
    * present, adding it makes no modifications; otherwise, the pair is added, sorted according 
    * to the {@code orderBy} value in {@code map}.
    * @return  {@code true} iff the operation modified the map.
    */
  public boolean putAll(ExternallySortedMultiMap<? extends K, ? extends V, ? extends C> map) {
    boolean result = false;
    for (Map.Entry<? extends K, ? extends ExternallySortedSet<? extends V, ? extends C>> e : 
           map._map.entrySet()) {
      ExternallySortedSet<V, C> set = _map.get(e.getKey());
      if (set == null) { set = new ExternallySortedSet<V, C>(); _map.put(e.getKey(), set); }
      _size -= set.size();
      
      // The following generates an incorrect type error in javac 5, fixed in javac 6:
      result = result | set.addAll(e.getValue()); // "|" instead of "||" to avoid short-circuit
      // The former workaround:
//      @SuppressWarnings("unchecked") ExternallySortedSet<V,C> s = e.getValue();
//      @SuppressWarnings("unchecked") boolean newResult = set.addAll(s);
//      result = result | newResult;      
      _size += set.size();
    }
    return result;
  }
  
  /** Removes all elements from the map. */
  public void clear() { _map.clear(); _size = 0; }
  
  /**
   * @return  A dynamically-updating iterable of all keys associated with at least one
   *          value in this map.  {@link Iterator#remove()} is not supported.
   */
  public Iterable<K> keys() {
    return IterUtil.immutable(_map.keySet());
  }
  
  /**
   * @return  A dynamically-updating iterable of all values associated with any key
   *          in this map.  {@link Iterator#remove()} is not supported.
   */
  public Iterable<V> values() { return IterUtil.immutable(IterUtil.collapse(_map.values())); }
  
}
