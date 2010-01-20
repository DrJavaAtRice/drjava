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

import java.util.Map.Entry;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An implementation of {@link Entry} that does not support
 * the {@link #setValue} operation; all other methods are
 * delegated to a wrapped {@code Entry}.
 */
public class ImmutableMapEntry<K, V> implements Entry<K, V>, Composite, Serializable {
  
  protected Entry<? extends K, ? extends V> _delegate;
  
  public ImmutableMapEntry(Entry<? extends K, ? extends V> entry) { _delegate = entry; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_delegate) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_delegate) + 1; }
  
  public K getKey() { return _delegate.getKey(); }
  public V getValue() { return _delegate.getValue(); }
  public V setValue(V value) { throw new UnsupportedOperationException(); }
  
  public String toString() { return _delegate.toString(); }
  public boolean equals(Object o) { return _delegate.equals(o); }
  public int hashCode() { return _delegate.hashCode(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> ImmutableMapEntry<K, V> make(Entry<? extends K, ? extends V> entry) {
    return new ImmutableMapEntry<K, V>(entry);
  }
  
  @SuppressWarnings("unchecked") public static <K, V> Lambda<Entry<? extends K, ? extends V>, Entry<K, V>> factory() {
    return (Factory<K, V>) Factory.INSTANCE;
  }
  
  private static final class Factory<K, V> implements Lambda<Entry<? extends K, ? extends V>, Entry<K, V>>, Serializable {
    public static final Factory<Object, Object> INSTANCE = new Factory<Object, Object>();
    private Factory() {}
    public Entry<K, V> value(Entry<? extends K, ? extends V> arg) { return new ImmutableMapEntry<K, V>(arg); }
  }
  
}
