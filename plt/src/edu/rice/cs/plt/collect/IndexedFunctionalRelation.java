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
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Thunk;

/** An implementation of the FunctionalRelation interface based on indexing maps. */
public class IndexedFunctionalRelation<T1, T2> extends AbstractFunctionalRelation<T1, T2> implements Serializable {
  
  private Map<T1, T2> _firstMap;
  private LambdaMap<T1, T2> _functionMap;
  private RelationIndex<T2, T1> _secondIndex;
  
  /** Index in both directions using {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public IndexedFunctionalRelation() {
    this(CollectUtil.<T1, T2>hashMapFactory(),
         CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
         CollectUtil.<T1>hashSetFactory(4));
  }
  
  /**
   * Index using {@link java.util.HashMap}s and {@link java.util.HashSet}s.  If {@code indexSecond}
   * is false, no second-to-first index is created.
   */
  public IndexedFunctionalRelation(boolean indexSecond) {
    if (indexSecond) {
      _firstMap = new HashMap<T1, T2>();
      _functionMap = new ImmutableMap<T1, T2>(_firstMap);
      _secondIndex = makeSecondIndex(CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                     CollectUtil.<T1>hashSetFactory(4));
    }
    else {
      _firstMap = new HashMap<T1, T2>();
      _functionMap = new ImmutableMap<T1, T2>(_firstMap);
      _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(this, Pair.<T1, T2>inverter()));
    }
  }
  
  /**
   * Create indices based on the given factories.
   * @param firstIndexFactory  Maps firsts to seconds.
   * @param secondIndexFactory  Maps seconds to sets of firsts.
   * @param secondIndexEntryFactory  Produces sets for the second-to-first index.
   */
  public IndexedFunctionalRelation(Thunk<Map<T1, T2>> firstIndexFactory,
                                   Thunk<Map<T2, PredicateSet<T1>>> secondIndexFactory,
                                   Thunk<Set<T1>> secondIndexEntryFactory) {
    _firstMap = firstIndexFactory.value();
    _functionMap = new ImmutableMap<T1, T2>(_firstMap);
    _secondIndex = makeSecondIndex(secondIndexFactory, secondIndexEntryFactory);
  }
  
  /**
   * Create an index based on the given factory.  No second-to-first index is created.
   * @param firstIndexFactory  Maps firsts to seconds.
   */
  public IndexedFunctionalRelation(Thunk<Map<T1, T2>> firstIndexFactory) {
    _firstMap = firstIndexFactory.value();
    _functionMap = new ImmutableMap<T1, T2>(_firstMap);
    _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(this, Pair.<T1, T2>inverter()));
  }
    
  private RelationIndex<T2, T1> makeSecondIndex(Thunk<Map<T2, PredicateSet<T1>>> mapFactory,
                                                Thunk<Set<T1>> setFactory) {
    return new ConcreteRelationIndex<T2, T1>(mapFactory, setFactory) {
      public void validateAdd(T2 second, T1 first) { IndexedFunctionalRelation.this.validateAdd(first, second); }
      public void addToRelation(T2 second, T1 first) { _firstMap.put(first, second); }
      public void removeFromRelation(T2 second, T1 first) { _firstMap.remove(first); }
      public void clearRelation() { _firstMap.clear(); }
    };
  }
  
  public boolean isStatic() { return false; }
  public LambdaMap<T1, T2> functionMap() { return _functionMap; }
  public PredicateSet<T2> secondSet() { return _secondIndex.keys(); }
  public PredicateSet<T1> matchSecond(T2 second) { return _secondIndex.match(second); }

  @Override public boolean add(T1 first, T2 second) {
    boolean result = validateAdd(first, second);
    if (result) {
      _firstMap.put(first, second);
      _secondIndex.added(second, first);
    }
    return result;
  }
  
  /**
   * Returns true if this pair is not already present and can be added.
   * Throws an exception if the pair violates integrity constraints.
   */
  private boolean validateAdd(T1 first, T2 second) {
    if (_firstMap.containsKey(first)) {
      T2 current = _firstMap.get(first);
      if ((current == null) ? (second == null) : current.equals(second)) {
        return false;
      }
      else {
        throw new IllegalArgumentException("Relation already contains an entry for " + first);
      }
    }
    else { return true; }
  }
  
  @Override public boolean remove(T1 first, T2 second) {
    boolean result = contains(first, second);
    if (result) {
      _firstMap.remove(first);
      _secondIndex.removed(second, first);
    }
    return result;
  }
  
  @Override public void clear() {
    _firstMap.clear();
    _secondIndex.cleared();
  }
    
  /** Make an IndexedFunctionalRelation indexed by {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public static <T1, T2> IndexedFunctionalRelation<T1, T2> makeHashBased() {
    return new IndexedFunctionalRelation<T1, T2>(CollectUtil.<T1, T2>hashMapFactory(),
                                                 CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                                 CollectUtil.<T1>hashSetFactory(4));
  }
  
  /** Make an IndexedFunctionalRelation indexed by {@link java.util.LinkedHashMap}s and {@link java.util.LinkedHashSet}s. */
  public static <T1, T2> IndexedFunctionalRelation<T1, T2> makeLinkedHashBased() {
    return new IndexedFunctionalRelation<T1, T2>(CollectUtil.<T1, T2>linkedHashMapFactory(),
                                                 CollectUtil.<T2, PredicateSet<T1>>linkedHashMapFactory(),
                                                 CollectUtil.<T1>linkedHashSetFactory(4));
  }
  
  /** Make an IndexedFunctionalRelation indexed by {@link java.util.TreeMap}s and {@link java.util.TreeSet}s. */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
    IndexedFunctionalRelation<T1, T2> makeTreeBased() {
    return new IndexedFunctionalRelation<T1, T2>(CollectUtil.<T1, T2>treeMapFactory(),
                                                 CollectUtil.<T2, PredicateSet<T1>>treeMapFactory(),
                                                 CollectUtil.<T1>treeSetFactory());
  }
  
}
