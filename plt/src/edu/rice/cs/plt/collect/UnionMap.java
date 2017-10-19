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
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A lazily-constructed and dynamically-updated union of two maps.  The first is designated
 * the <em>parent</em>, and the second the <em>child</em>.  Bindings for keys in the child shadow
 * those in the parent.
 */
public class UnionMap<K, V> extends AbstractKeyBasedMap<K, V> implements Composite, Serializable {
  private final Map<? extends K, ? extends V> _parent;
  private final Map<? extends K, ? extends V> _child;
  private final PredicateSet<K> _keys;
  
  public UnionMap(Map<? extends K, ? extends V> parent, Map<? extends K, ? extends V> child) {
    _parent = parent;
    _child = child;
    _keys = new UnionSet<K>(parent.keySet(), child.keySet());
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_parent, _child) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_parent, _child) + 1; }
  
  public V get(Object key) {
    if (_child.containsKey(key)) { return _child.get(key); }
    else { return _parent.get(key); }
  }
  
  public PredicateSet<K> keySet() { return _keys; }
  
}
