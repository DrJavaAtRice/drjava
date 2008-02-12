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

import java.util.Dictionary;
import java.util.AbstractMap;
import java.util.Set;
import java.util.Map;
import java.util.Collections;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.ReadOnceIterable;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;

/**
 * A map wrapping a {@link Dictionary} object.  Defined for compatibility with legacy APIs.
 */
public class DictionaryMap<K, V> extends AbstractMap<K, V> {
  
  private final Dictionary<K, V> _d;
  private final Thunk<Set<K>> _keys;
  
  public DictionaryMap(Dictionary<K, V> d) {
    _d = d;
    _keys = LazyThunk.make(new Thunk<Set<K>>() {
      public Set<K> value() {
        Set<K> result = CollectUtil.asSet(ReadOnceIterable.make(IterUtil.asIterator(_d.keys())));
        return Collections.unmodifiableSet(result);
      }
    });
  }
  
  public int size() { return _d.size(); }
  
  public boolean isEmpty() { return _d.isEmpty(); }
  
  public boolean containsKey(Object key) { return _keys.value().contains(key); }

  // use default containsValue implementation
  
  public V get(Object key) { return _d.get(key); }
  
  public V put(K key, V value) { return _d.put(key, value); }
  
  public V remove(Object key) { return _d.remove(key); }
  
  // use default putAll implementation
  
  public void clear() {
    // make a snapshot to avoid concurrent modification
    Iterable<K> keys = IterUtil.snapshot(IterUtil.asIterator(_d.keys()));
    for (K key : keys) { _d.remove(key); }
  }
  
  public Set<K> keySet() { return _keys.value(); }
  
  // use default values() implementation
  
  public Set<Map.Entry<K, V>> entrySet() { return new KeyDrivenEntrySet<K, V>(this); }
  
}
