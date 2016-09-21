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
import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Thunk;

/** An implementation of the Relation interface based on indexing maps. */
public class IndexedRelation<T1, T2> extends AbstractRelation<T1, T2> implements Serializable {
  private final RelationIndex<T1, T2> _firstIndex;
  private final RelationIndex<T2, T1> _secondIndex;
  
  /** Index in both directions using {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public IndexedRelation() {
    this(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
         CollectUtil.<T2>hashSetFactory(4),
         CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
         CollectUtil.<T1>hashSetFactory(4));
  }
  
  /**
   * Index using {@link java.util.HashMap}s and {@link java.util.HashSet}s.  If {@code indexSecond}
   * is false, no second-to-first index is created.
   */
  public IndexedRelation(boolean indexSecond) {
    if (indexSecond) {
      _firstIndex = makeFirstIndex(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                   CollectUtil.<T2>hashSetFactory(4));
      _secondIndex = makeSecondIndex(CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                     CollectUtil.<T1>hashSetFactory(4));
    }
    else {
      _firstIndex = makeFirstIndex(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                   CollectUtil.<T2>hashSetFactory(4));
      _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(_firstIndex, Pair.<T1, T2>inverter()));
    }
  }
  
  /**
   * Create indices based on the given factories.
   * @param firstIndexFactory  Maps firsts to sets of seconds.
   * @param firstIndexEntryFactory  Produces sets for the first-to-second index.
   * @param secondIndexFactory  Maps seconds to sets of firsts.
   * @param secondIndexEntryFactory  Produces sets for the second-to-first index.
   */
  public IndexedRelation(Thunk<Map<T1, PredicateSet<T2>>> firstIndexFactory,
                         Thunk<Set<T2>> firstIndexEntryFactory,
                         Thunk<Map<T2, PredicateSet<T1>>> secondIndexFactory,
                         Thunk<Set<T1>> secondIndexEntryFactory) {
    _firstIndex = makeFirstIndex(firstIndexFactory, firstIndexEntryFactory);
    _secondIndex = makeSecondIndex(secondIndexFactory, secondIndexEntryFactory);
  }
  
  /**
   * Create an index based on the given factories.  No second-to-first index is created.  (If
   * <em>only</em> a second-to-first index is wanted, invoke this constructor and then invert
   * the result.)
   * @param firstIndexFactory  Maps firsts to sets of seconds.
   * @param firstIndexEntryFactory  Produces sets for the first-to-second index.
   */
  public IndexedRelation(Thunk<Map<T1, PredicateSet<T2>>> firstIndexFactory,
                         Thunk<Set<T2>> firstIndexEntryFactory) {
    _firstIndex = makeFirstIndex(firstIndexFactory, firstIndexEntryFactory);
    _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(_firstIndex, Pair.<T1, T2>inverter()));
  }
  
  private RelationIndex<T1, T2> makeFirstIndex(Thunk<Map<T1, PredicateSet<T2>>> mapFactory,
                                               Thunk<Set<T2>> setFactory) {
    return new ConcreteRelationIndex<T1, T2>(mapFactory, setFactory) {
      public void addToRelation(T1 first, T2 second) { _secondIndex.added(second, first); }
      public void removeFromRelation(T1 first, T2 second) { _secondIndex.removed(second, first); }
      public void clearRelation() { _secondIndex.cleared(); }
    };
  }
  
  private RelationIndex<T2, T1> makeSecondIndex(Thunk<Map<T2, PredicateSet<T1>>> mapFactory,
                                                Thunk<Set<T1>> setFactory) {
    return new ConcreteRelationIndex<T2, T1>(mapFactory, setFactory) {
      public void addToRelation(T2 second, T1 first) { _firstIndex.added(first, second); }
      public void removeFromRelation(T2 second, T1 first) { _firstIndex.removed(first, second); }
      public void clearRelation() { _firstIndex.cleared(); }
    };
  }
  
  @Override public boolean isEmpty() { return _firstIndex.isEmpty(); }
  @Override public int size() { return _firstIndex.size(); }
  @Override public int size(int bound) { return _firstIndex.size(bound); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return false; }
  
  public boolean contains(T1 first, T2 second) {
    return _firstIndex.contains(first, second);
  }
  
  public boolean contains(Object obj) {
    if (obj instanceof Pair<?, ?>) {
      Pair<?, ?> p = (Pair<?, ?>) obj;
      return _firstIndex.contains(p.first(), p.second());
    }
    else { return false; }
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
  
  /** Make an IndexedRelation indexed by {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public static <T1, T2> IndexedRelation<T1, T2> makeHashBased() {
    return new IndexedRelation<T1, T2>(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                       CollectUtil.<T2>hashSetFactory(4),
                                       CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                       CollectUtil.<T1>hashSetFactory(4));
  }
  
  /** Make an IndexedRelation indexed by {@link java.util.LinkedHashMap}s and {@link java.util.LinkedHashSet}s. */
  public static <T1, T2> IndexedRelation<T1, T2> makeLinkedHashBased() {
    return new IndexedRelation<T1, T2>(CollectUtil.<T1, PredicateSet<T2>>linkedHashMapFactory(),
                                       CollectUtil.<T2>linkedHashSetFactory(4),
                                       CollectUtil.<T2, PredicateSet<T1>>linkedHashMapFactory(),
                                       CollectUtil.<T1>linkedHashSetFactory(4));
  }
  
  /** Make an IndexedRelation indexed by {@link java.util.TreeMap}s and {@link java.util.TreeSet}s. */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
    IndexedRelation<T1, T2> makeTreeBased() {
    return new IndexedRelation<T1, T2>(CollectUtil.<T1, PredicateSet<T2>>treeMapFactory(),
                                       CollectUtil.<T2>treeSetFactory(),
                                       CollectUtil.<T2, PredicateSet<T1>>treeMapFactory(),
                                       CollectUtil.<T1>treeSetFactory());
  }
  
}
