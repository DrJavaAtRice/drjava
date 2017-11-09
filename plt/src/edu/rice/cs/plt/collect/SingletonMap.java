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

import java.util.Collection;
import java.io.Serializable;

/** An immutable {@code LambdaMap} containing a single entry. */
public class SingletonMap<K, V> extends AbstractKeyBasedMap<K, V> implements Serializable {
  
  private final K _key;
  private final V _value;
  
  public SingletonMap(K key, V value) { _key = key; _value = value; }

  public V get(Object obj) {
    if ((_key == null) ? (obj == null) : _key.equals(obj)) { return _value; }
    else { return null; }
  }
  
  public PredicateSet<K> keySet() { return new SingletonSet<K>(_key); }
  
  @Override public int size() { return 1; }
  @Override public boolean isEmpty() { return false; }
  
  @Override public boolean containsKey(Object obj) {
    return (_key == null) ? (obj == null) : _key.equals(obj);
  }
  
  @Override public boolean containsValue(Object obj) {
    return (_value == null) ? (obj == null) : _value.equals(obj);
  }
  
  @Override public Collection<V> values() { return new SingletonSet<V>(_value); }
  
  /** Call the constructor (allows type arguments to be inferred) */
  public static <K, V> SingletonMap<K, V> make(K key, V value) {
    return new SingletonMap<K, V>(key, value);
  }
  
}
