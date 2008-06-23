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

import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.RemoveNotificationIterator;
import edu.rice.cs.plt.lambda.Runnable1;

/**
 * A hash code-based implementation of the Relation interface.  By default, hash-based indices are
 * created mapping firsts to seconds and vice-versa; if not needed, the second index may be
 * turned off.
 */
public class HashRelation<T1, T2> extends AbstractRelation<T1, T2> implements Serializable {
  private final RelationIndex<T1, T2> _firstIndex;
  private final RelationIndex<T2, T1> _secondIndex;
  
  /** Create a HashRelation with indexes in both directions. */
  public HashRelation() { this(true); }
  
  public HashRelation(Set<? extends Pair<T1, T2>> pairs) {
    this(true);
    addAll(pairs);
  }
  
  /**
   * Optionally create a HashRelation without a seconds-to-firsts index.  If a relation is
   * needed with <em>only</em> a second-to-first index, create a HashRelation without a
   * second index and then invoke {@code inverse()}.
   * @param indexSecond  Whether an index from seconds to sets of firsts should be maintained.
   */
  public HashRelation(boolean indexSecond) {
    _firstIndex = new ConcreteRelationIndex<T1, T2>(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                                    CollectUtil.<T2>hashSetFactory(4)) {
      public void addToRelation(T1 first, T2 second) { _secondIndex.added(second, first); }
      public void removeFromRelation(T1 first, T2 second) { _secondIndex.removed(second, first); }
      public void clearRelation() { _secondIndex.cleared(); }
    };
    if (indexSecond) {
      _secondIndex = new ConcreteRelationIndex<T2, T1>(CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                                       CollectUtil.<T1>hashSetFactory(4)) {
        public void addToRelation(T2 second, T1 first) { _firstIndex.added(first, second); }
        public void removeFromRelation(T2 second, T1 first) { _firstIndex.removed(first, second); }
        public void clearRelation() { _firstIndex.cleared(); }
      };
    }
    else {
      _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(_firstIndex, Pair.<T1, T2>inverter()));
    }
  }
    
  @Override public boolean isEmpty() { return _firstIndex.isEmpty(); }
  @Override public int size() { return _firstIndex.size(); }
  @Override public int size(int bound) { return _firstIndex.size(bound); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return false; }
  
  protected boolean containsObjects(Object first, Object second) {
    return _firstIndex.contains(first, second);
  }
  
  public Iterator<Pair<T1, T2>> iterator() { return _firstIndex.iterator(); }
  
  public PredicateSet<T1> firstSet() { return _firstIndex.keys(); }
  public PredicateSet<T2> matchFirst(T1 first) { return _firstIndex.match(first); }
  public PredicateSet<T2> secondSet() { return _secondIndex.keys(); }
  public PredicateSet<T1> matchSecond(T2 second) { return _secondIndex.match(second); }
  
  @Override public boolean add(T1 first, T2 second) {
    boolean result = !_firstIndex.contains(first, second);
    if (result) {
      _firstIndex.added(first, second);
      _secondIndex.added(second, first);
    }
    return result;
  }
  
  @Override public boolean remove(T1 first, T2 second) {
    boolean result = _firstIndex.contains(first, second);
    if (result) {
      _firstIndex.removed(first, second);
      _secondIndex.removed(second, first);
    }
    return result;
  }
  
  @Override public void clear() {
    if (!_firstIndex.isEmpty()) {
      _firstIndex.cleared();
      _secondIndex.cleared();
    }
  }
  
}
