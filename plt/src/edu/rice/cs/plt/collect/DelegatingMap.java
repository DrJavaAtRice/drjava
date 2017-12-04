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
import java.util.Set;
import java.util.Collection;
import java.io.Serializable;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A map that delegates all operations to a wrapped map.  Subclasses can be defined
 * that override a few of the methods, while maintaining the default delegation behavior
 * in most cases.
 */
public class DelegatingMap<K, V> implements LambdaMap<K, V>, Composite, Serializable {
  
  protected Map<K, V> _delegate;
  
  public DelegatingMap(Map<K, V> delegate) { _delegate = delegate; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_delegate) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_delegate) + 1; }
  
  public int size() { return _delegate.size(); }
  public boolean isEmpty() { return _delegate.isEmpty(); }
  
  public V get(Object key) { return _delegate.get(key); }
  public V value(K key) { return _delegate.get(key); }
  
  public boolean containsKey(Object o) { return _delegate.containsKey(o); }
  public PredicateSet<K> keySet() { return CollectUtil.asPredicateSet(_delegate.keySet()); }
  
  public boolean containsValue(Object o) { return _delegate.containsValue(o); }
  public Collection<V> values() { return _delegate.values(); }
  
  public Set<Map.Entry<K, V>> entrySet() { return _delegate.entrySet(); }

  public V put(K key, V value) { return _delegate.put(key, value); }
  public void putAll(Map<? extends K, ? extends V> t) { _delegate.putAll(t); }
  public V remove(Object key) { return _delegate.remove(key); }
  public void clear() { _delegate.clear(); }
  
  public String toString() { return _delegate.toString(); }
  public boolean equals(Object o) { return _delegate.equals(o); }
  public int hashCode() { return _delegate.hashCode(); }
}
