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

import java.util.Set;
import java.util.Iterator;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;
import edu.rice.cs.plt.iter.CartesianIterator;
import edu.rice.cs.plt.iter.EmptyIterator;

/**
 * A Relation representing the cartesian (or cross) product of two sets.  Does not support mutation,
 * but the contents are updated dynamically as the given sets change.
 */
public class CartesianRelation<T1, T2> extends AbstractRelation<T1, T2> implements Composite, Serializable {
  
  private final PredicateSet<T1> _firstSet;
  private final PredicateSet<T2> _secondSet;
  
  public CartesianRelation(Set<? extends T1> firsts, Set<? extends T2> seconds) {
    _firstSet = new ImmutableSet<T1>(firsts);
    _secondSet = new ImmutableSet<T2>(seconds);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_firstSet, _secondSet) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_firstSet, _secondSet) + 1; }
  
  @Override public int size(int bound) {
    // copied from CartesianIterable
    int size1 = _firstSet.size(bound);
    if (size1 == 0) { return 0; }
    else {
      int bound2 = bound / size1;
      if (bound2 < Integer.MAX_VALUE) { bound2++; } // division must round up, not down
      int size2 = _secondSet.size(bound2);
      // if this overflows, it must be negative:
      // size1*size2 <= size1 * ((bound/size1)+1) = bound + size1
      int result = size1*size2;
      return (result > bound || result < 0) ? bound : result;
    }
  }
  
  @Override public boolean isEmpty() { return _firstSet.isEmpty() && _secondSet.isEmpty(); }
  public boolean isInfinite() { return _firstSet.isInfinite() || _secondSet.isInfinite(); }
  public boolean hasFixedSize() { return _firstSet.hasFixedSize() && _secondSet.hasFixedSize(); }
  public boolean isStatic() { return _firstSet.isStatic() && _secondSet.isStatic(); }
  
  public boolean contains(T1 first, T2 second) {
    return _firstSet.contains(first) && _secondSet.contains(second);
  }
  
  public boolean contains(Object o) {
    if (o instanceof Pair<?, ?>) {
      Pair<?, ?> p = (Pair<?, ?>) o;
      return _firstSet.contains(p.first()) && _secondSet.contains(p.second());
    }
    else { return false; }
  }
  
  public Iterator<Pair<T1, T2>> iterator() {
    return CartesianIterator.make(_firstSet.iterator(), _secondSet, Pair.<T1, T2>factory());
  }
  
  public PredicateSet<T1> firstSet() { return _firstSet; }

  public PredicateSet<T2> matchFirst(T1 first) {
    if (_firstSet.isStatic()) {
      return _firstSet.contains(first) ? _secondSet : CollectUtil.<T2>emptySet();
    }
    else { return new MatchSet<T1, T2>(first, _firstSet, _secondSet); }
  }

  public PredicateSet<T2> secondSet() { return _secondSet; }

  public PredicateSet<T1> matchSecond(T2 second) {
    if (_secondSet.isStatic()) {
      return _secondSet.contains(second) ? _firstSet : CollectUtil.<T1>emptySet();
    }
    else { return new MatchSet<T2, T1>(second, _secondSet, _firstSet); }
  }

  /**
   * The result of a "match" method that dynamically changes based on whether the given key
   * appears in a set of keys.  Does not support mutation.
   */
  private static class MatchSet<K, V> extends AbstractPredicateSet<V> {
    private final K _key;
    private final PredicateSet<K> _keys;
    private final PredicateSet<V> _vals;
    
    public MatchSet(K key, PredicateSet<K> keys, PredicateSet<V> vals) {
      _key = key;
      _keys = keys;
      _vals = vals;
    }
    
    public boolean contains(Object obj) {
      return _keys.contains(_key) ? _vals.contains(obj) : false;
    }
    public Iterator<V> iterator() {
      return _keys.contains(_key) ? _vals.iterator() : EmptyIterator.<V>make();
    }
    
    @Override public boolean isEmpty() { return !_keys.contains(_key) || _vals.isEmpty(); }
    @Override public int size() { return _keys.contains(_key) ? _vals.size() : 0; }
    @Override public int size(int bound) { return _keys.contains(_key) ? _vals.size(bound) : 0; }
    public boolean isInfinite() { return _keys.contains(_key) && _vals.isInfinite(); }
    public boolean hasFixedSize() { return _keys.isStatic() && _vals.hasFixedSize(); }
    public boolean isStatic() { return _keys.isStatic() && _vals.isStatic(); }
  }
  
}
