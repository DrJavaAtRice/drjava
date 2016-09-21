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
import java.util.HashMap;
import java.util.Set;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.Thunk;

/**
 * A hash code-based implementation of the InjectiveRelation interface.  By default, a hash-based index
 * is created mapping firsts to sets of seconds; this functionality may be turned off where it 
 * is not needed.
 */
public class IndexedInjectiveRelation<T1, T2> extends AbstractInjectiveRelation<T1, T2> implements Serializable {
  
  private RelationIndex<T1, T2> _firstIndex;
  private Map<T2, T1> _secondMap;
  private LambdaMap<T2, T1> _injectionMap;
  
  /** Index in both directions using {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public IndexedInjectiveRelation() {
    this(CollectUtil.<T2, T1>hashMapFactory(),
         CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
         CollectUtil.<T2>hashSetFactory(4));
  }
  
  /**
   * Index using {@link java.util.HashMap}s and {@link java.util.HashSet}s.  If {@code indexFirst}
   * is false, no first-to-second index is created.
   */
  public IndexedInjectiveRelation(boolean indexFirst) {
    if (indexFirst) {
      _secondMap = new HashMap<T2, T1>();
      _injectionMap = new ImmutableMap<T2, T1>(_secondMap);
      _firstIndex = makeFirstIndex(CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                   CollectUtil.<T2>hashSetFactory(4));
    }
    else {
      _secondMap = new HashMap<T2, T1>();
      _injectionMap = new ImmutableMap<T2, T1>(_secondMap);
      _firstIndex = new LazyRelationIndex<T1, T2>(this);
    }
  }
  
  /**
   * Create indices based on the given factories.
   * @param secondIndexFactory  Maps seconds to firsts.
   * @param firstIndexFactory  Maps firsts to sets of seconds.
   * @param firstIndexEntryFactory  Produces sets for the first-to-second index.
   */
  public IndexedInjectiveRelation(Thunk<Map<T2, T1>> secondIndexFactory,
                                   Thunk<Map<T1, PredicateSet<T2>>> firstIndexFactory,
                                   Thunk<Set<T2>> firstIndexEntryFactory) {
    _secondMap = secondIndexFactory.value();
    _injectionMap = new ImmutableMap<T2, T1>(_secondMap);
    _firstIndex = makeFirstIndex(firstIndexFactory, firstIndexEntryFactory);
  }
  
  /**
   * Create an index based on the given factory.  No first-to-second index is created.
   * @param secondIndexFactory  Maps seconds to firsts.
   */
  public IndexedInjectiveRelation(Thunk<Map<T2, T1>> secondIndexFactory) {
    _secondMap = secondIndexFactory.value();
    _injectionMap = new ImmutableMap<T2, T1>(_secondMap);
    _firstIndex = new LazyRelationIndex<T1, T2>(this);
  }
  
  private RelationIndex<T1, T2> makeFirstIndex(Thunk<Map<T1, PredicateSet<T2>>> mapFactory,
                                               Thunk<Set<T2>> setFactory) {
    return new ConcreteRelationIndex<T1, T2>(mapFactory, setFactory) {
      public void validateAdd(T1 first, T2 second) { IndexedInjectiveRelation.this.validateAdd(first, second); }
      public void addToRelation(T1 first, T2 second) { _secondMap.put(second, first); }
      public void removeFromRelation(T1 first, T2 second) { _secondMap.remove(second); }
      public void clearRelation() { _secondMap.clear(); }
    };
  }

  public boolean isStatic() { return false; }
  public LambdaMap<T2, T1> injectionMap() { return _injectionMap; }
  public PredicateSet<T1> firstSet() { return _firstIndex.keys(); }
  public PredicateSet<T2> matchFirst(T1 first) { return _firstIndex.match(first); }

  @Override public boolean add(T1 first, T2 second) {
    boolean result = validateAdd(first, second);
    if (result) {
      _secondMap.put(second, first);
      _firstIndex.added(first, second);
    }
    return result;
  }
  
  /**
   * Returns true if this pair is not already present and can be added.
   * Throws an exception if the pair violates integrity constraints.
   */
  private boolean validateAdd(T1 first, T2 second) {
    if (_secondMap.containsKey(second)) {
      T1 current = _secondMap.get(second);
      if ((current == null) ? (first == null) : current.equals(first)) {
        return false;
      }
      else {
        throw new IllegalArgumentException("Relation already contains an entry for " + second);
      }
    }
    else { return true; }
  }
  
  @Override public boolean remove(T1 first, T2 second) {
    boolean result = contains(first, second);
    if (result) {
      _secondMap.remove(second);
      _firstIndex.removed(first, second);
    }
    return result;
  }
  
  @Override public void clear() {
    _secondMap.clear();
    _firstIndex.cleared();
  }
    
  /** Make an IndexedInjectiveRelation indexed by {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public static <T1, T2> IndexedInjectiveRelation<T1, T2> makeHashBased() {
    return new IndexedInjectiveRelation<T1, T2>(CollectUtil.<T2, T1>hashMapFactory(),
                                                CollectUtil.<T1, PredicateSet<T2>>hashMapFactory(),
                                                CollectUtil.<T2>hashSetFactory(4));
  }
  
  /** Make an IndexedInjectiveRelation indexed by {@link java.util.LinkedHashMap}s and {@link java.util.LinkedHashSet}s. */
  public static <T1, T2> IndexedInjectiveRelation<T1, T2> makeLinkedHashBased() {
    return new IndexedInjectiveRelation<T1, T2>(CollectUtil.<T2, T1>linkedHashMapFactory(),
                                                CollectUtil.<T1, PredicateSet<T2>>linkedHashMapFactory(),
                                                CollectUtil.<T2>linkedHashSetFactory(4));
  }
  
  /** Make an IndexedInjectiveRelation indexed by {@link java.util.TreeMap}s and {@link java.util.TreeSet}s. */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
    IndexedInjectiveRelation<T1, T2> makeTreeBased() {
    return new IndexedInjectiveRelation<T1, T2>(CollectUtil.<T2, T1>treeMapFactory(),
                                                CollectUtil.<T1, PredicateSet<T2>>treeMapFactory(),
                                                CollectUtil.<T2>treeSetFactory());
  }
  
}
