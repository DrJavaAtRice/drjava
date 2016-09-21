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
import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.MappedIterator;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * Wraps a map in an immutable interface.  Similar to {@link java.util.Collections#unmodifiableMap},
 * but this class also implements {@link LambdaMap}.  Note that only only <em>this</em>
 * interface with the data is immutable -- if the original data structure is mutable, a client with direct
 * access to that structure can still mutate it.
 */
public class ImmutableMap<K, V> implements LambdaMap<K, V>, Composite, Serializable {
  
  // using wildcards here allows us to wrap more maps, but means we can't extend DelegatingMap
  protected Map<? extends K, ? extends V> _delegate;
  
  public ImmutableMap(Map<? extends K, ? extends V> map) { _delegate = map; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_delegate) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_delegate) + 1; }
  
  public int size() { return _delegate.size(); }
  public boolean isEmpty() { return _delegate.isEmpty(); }
  
  public V get(Object key) { return _delegate.get(key); }
  public V value(K key) { return _delegate.get(key); }
  
  public boolean containsKey(Object o) { return _delegate.containsKey(o); }
  public boolean containsValue(Object o) { return _delegate.containsValue(o); }
  
  public PredicateSet<K> keySet() { return new ImmutableSet<K>(_delegate.keySet()); }
  public Collection<V> values() { return Collections.unmodifiableCollection(_delegate.values()); }
  public Set<Entry<K, V>> entrySet() { return new EntrySet(); }

  public V put(K key, V value) { throw new UnsupportedOperationException(); }
  public void putAll(Map<? extends K, ? extends V> t) { throw new UnsupportedOperationException(); }
  public V remove(Object key) { throw new UnsupportedOperationException(); }
  public void clear() { throw new UnsupportedOperationException(); }
  
  public String toString() { return _delegate.toString(); }
  public boolean equals(Object o) { return _delegate.equals(o); }
  public int hashCode() { return _delegate.hashCode(); }

  /** Call the constructor (allows {@code K} and {@code V} to be inferred). */
  public static <K, V> ImmutableMap<K, V> make(Map<? extends K, ? extends V> map) {
    return new ImmutableMap<K, V>(map);
  }
  
  private final class EntrySet extends AbstractPredicateSet<Entry<K, V>> implements Serializable {
    @Override public boolean isEmpty() { return _delegate.isEmpty(); }
    @Override public int size() { return _delegate.size(); }
    @Override public int size(int bound) { int s = _delegate.size(); return s < bound ? s : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return false; }
    public boolean isStatic() { return false; }
    
    public boolean contains(Object o) { return _delegate.entrySet().contains(o); }
    
    public Iterator<Entry<K, V>> iterator() {
      Lambda<Entry<? extends K, ? extends V>, Entry<K, V>> factory = ImmutableMapEntry.factory();
      return ImmutableIterator.make(MappedIterator.make(_delegate.entrySet().iterator(), factory));
    }
    
    @Override public boolean add(Entry<K, V> o) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(Collection<? extends Entry<K, V>> c) { throw new UnsupportedOperationException(); }
    @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
    @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }
  }
  
}
