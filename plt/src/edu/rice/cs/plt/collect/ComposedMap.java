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
