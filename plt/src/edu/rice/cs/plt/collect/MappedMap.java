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
import java.io.Serializable;
import edu.rice.cs.plt.iter.MappedIterable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A map whose value set is translated by a mapping lambda.
 * @see MappedIterable
 * @see ComposedMap
 */
public class MappedMap<K, X, V> extends AbstractKeyBasedMap<K, V> implements Composite, Serializable {
  private final Map<K, ? extends X> _map;
  private final Lambda<? super X, ? extends V> _lambda;
  
  public MappedMap(Map<K, ? extends X> map, Lambda<? super X, ? extends V> lambda) {
    _map = map;
    _lambda = lambda;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_map, _lambda) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_map, _lambda) + 1; }
  
  public V get(Object key) {
    if (_map.containsKey(key)) { return _lambda.value(_map.get(key)); }
    else { return null; }
  }
  
  public PredicateSet<K> keySet() {
    return CollectUtil.asPredicateSet(_map.keySet());
  }
  
  @Override public V remove(Object key) {
    if (_map.containsKey(key)) {
      X resultX = _map.remove(key);
      return _lambda.value(resultX);
    }
    else { return null; }
  }
  
  @Override public void clear() { _map.clear(); }
  
  @Override public int size() { return _map.size(); }
  @Override public boolean isEmpty() { return _map.isEmpty(); }
  
  @Override public boolean containsKey(Object o) { return _map.containsKey(o); }

  @Override public Collection<V> values() {
    return new IterableCollection<V>(new MappedIterable<X, V>(_map.values(), _lambda));
  }
  
}
