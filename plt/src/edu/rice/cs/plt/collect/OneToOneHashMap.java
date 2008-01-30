/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

import java.util.AbstractMap;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

/**
 * An implementation of {@code OneToOneMap} based on {@link HashMap}s.  Both keys and
 * values are stored as keys in a hashtable, providing constant-time lookup in both 
 * directions.  (Clients should thus be aware that stored values must correctly implement
 * {@link Object#hashCode()} and be valid hash keys.)
 */
public class OneToOneHashMap<K, V> extends AbstractMap<K, V> implements OneToOneMap<K, V> {
  
  private final HashMap<K, V> _forward;
  private final HashMap<V, K> _backward;
  
  public OneToOneHashMap() {
    _forward = new HashMap<K, V>();
    _backward = new HashMap<V, K>();
  }
  
  private OneToOneHashMap(HashMap<K, V> forward, HashMap<V, K> backward) {
    _forward = forward;
    _backward = backward;
  }
  
  public int size() { return _forward.size(); }
  
  public boolean isEmpty() { return _forward.isEmpty(); }
  
  public boolean containsKey(Object key) { return _forward.containsKey(key); }
  
  public boolean containsValue(Object value) { return _backward.containsKey(value); }
  
  public V get(Object key) { return _forward.get(key); }
  
  public V getValue(K key) { return _forward.get(key); }
  
  public K getKey(V value) { return _backward.get(value); }
  
  public V put(K key, V value) {
    V result = _forward.get(key);
    if (_forward.containsKey(key)) { _backward.remove(result); }
    if (_backward.containsKey(value)) { _forward.remove(_backward.get(value)); }
    _forward.put(key, value);
    _backward.put(value, key);
    return result;
  }
  
  public V remove(Object key) {
    if (_forward.containsKey(key)) {
      V result = _forward.remove(key);
      _backward.remove(result);
      return result;
    }
    else { return null; }
  }
  
  public V removeKey(K key) { return remove(key); }
  
  public K removeValue(V value) {
    if (_backward.containsKey(value)) {
      K result = _backward.remove(value);
      _forward.remove(result);
      return result;
    }
    else { return null; }
  }
  
  public boolean remove(K key, V value) {
    if (_forward.containsKey(key) && _forward.get(key).equals(value)) {
      _forward.remove(key);
      _backward.remove(value);
      return true;
    }
    else { return false; }
  }
  
  public void clear() { _forward.clear(); _backward.clear(); }
  
  public Set<K> keySet() {
    return new DelegatedSet<K>(_forward.keySet()) {
      
      public Iterator<K> iterator() {
        final Iterator<K> forwardIter = _delegate.iterator();
        return new Iterator<K>() {
          private K _last = null;
          
          public boolean hasNext() { return forwardIter.hasNext(); }
          
          public K next() { _last = forwardIter.next(); return _last; }

          public void remove() {
            V val = _forward.get(_last);
            forwardIter.remove(); // ensures that _last is initialized and in the map
            _backward.remove(val);
          }
        };
      }
      
      public boolean remove(Object o) {
        if (_delegate.contains(o)) {
          // o is a valid key
          V val = _forward.get(o);
          _forward.remove(o);
          _backward.remove(val);
          return true;
        }
        else { return false; }
      }
      
      public boolean add(K o) { throw new UnsupportedOperationException(); }
      public boolean addAll(Collection<? extends K> c) {
        throw new UnsupportedOperationException();
      }
      public boolean retainAll(Collection<?> c) { return abstractSetRetainAll(c); }
      public boolean removeAll(Collection<?> c) { return abstractSetRemoveAll(c); }
      public void clear() { OneToOneHashMap.this.clear(); }
      
    };
  }
  
  public Set<V> values() { return reverse().keySet(); }
  
  /**
   * Provide access to the set of entries in this map.  Follows the contract of
   * {@link Map#entrySet}, and ensures that changes made to the set are reflected
   * consistently in this map.  Note that the {@link java.util.Map.Entry#setValue} method
   * is not supported.
   */
  public Set<Map.Entry<K, V>> entrySet() {
    return new DelegatedSet<Map.Entry<K, V>>(_forward.entrySet()) {

      public Iterator<Map.Entry<K, V>> iterator() {
        final Iterator<Map.Entry<K, V>> forwardIter = _delegate.iterator();
        return new Iterator<Map.Entry<K, V>>() {
          private Map.Entry<K, V> _last = null;
          
          public boolean hasNext() { return forwardIter.hasNext(); }
          
          public Map.Entry<K, V> next() {
            _last = new ImmutableMapEntry<K, V>(forwardIter.next());
            return _last;
          }
          
          public void remove() {
            forwardIter.remove(); // ensures that _last is initialized and in the map
            _backward.remove(_last.getValue());
          }
          
        };
      }
      
      public boolean remove(Object o) {
        if (_delegate.contains(o)) {
          // o must be a Map.Entry
          Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
          _forward.remove(entry.getKey());
          _backward.remove(entry.getValue());
          return true;
        }
        else { return false; }
      }
      
      public boolean add(Map.Entry<K, V> o) { throw new UnsupportedOperationException(); }
      public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        throw new UnsupportedOperationException();
      }
      public boolean retainAll(Collection<?> c) { return abstractSetRetainAll(c); }
      public boolean removeAll(Collection<?> c) { return abstractSetRemoveAll(c); }
      public void clear() { OneToOneHashMap.this.clear(); }
    };
  }
  
  public OneToOneMap<V, K> reverse() { return new OneToOneHashMap<V, K>(_backward, _forward); }
  
}
