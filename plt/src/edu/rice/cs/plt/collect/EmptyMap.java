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
import java.io.Serializable;

/** An empty {@code LambdaMap}. */
public class EmptyMap<K, V> implements LambdaMap<K, V>, Serializable {
  
  public static final EmptyMap<Object, Object> INSTANCE = new EmptyMap<Object, Object>();

  private EmptyMap() {}
  
  public V value(K key) { return null; }
  public V get(Object key) { return null; }
  
  public int size() { return 0; }
  public boolean isEmpty() { return true; }
  public boolean containsKey(Object o) { return false; }
  public boolean containsValue(Object o) { return false; }
  public EmptySet<K> keySet() { return EmptySet.make(); }
  public EmptySet<V> values() { return EmptySet.make(); }
  public EmptySet<Map.Entry<K, V>> entrySet() { return EmptySet.make(); }
  
  public V put(K key, V value) { throw new UnsupportedOperationException(); }
  public void putAll(Map<? extends K, ? extends V> t) { throw new UnsupportedOperationException(); }
  public V remove(Object key) { throw new UnsupportedOperationException(); }
  public void clear() { throw new UnsupportedOperationException(); }
  
  public String toString() { return "{}"; }
  
  public boolean equals(Object o) {
    return (this == o) || (o instanceof Map<?, ?> && ((Map<?, ?>) o).isEmpty());
  }
  
  public int hashCode() { return 0; }
  
  /** Return a singleton, cast to the appropriate type. */
  @SuppressWarnings("unchecked") public static <K, V> EmptyMap<K, V> make() {
    return (EmptyMap<K, V>) INSTANCE;
  }
  
}
