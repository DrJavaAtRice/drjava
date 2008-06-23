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
import java.util.HashMap;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A hash code-based implementation of the FunctionalRelation interface.  By default, a hash-based index
 * is created mapping seconds to sets of firsts; this functionality may be turned off where it 
 * is not needed.
 */
public class HashFunctionalRelation<T1, T2> extends AbstractFunctionalRelation<T1, T2> implements Serializable {
  
  private HashMap<T1, T2> _firstMap;
  private LambdaMap<T1, T2> _functionMap;
  private RelationIndex<T2, T1> _secondIndex;
  
  /** Create a HashFunctionalRelation with indexing turned on. */
  public HashFunctionalRelation() { this(true); }
  
  /** Create a HashFunctionalRelation with indexing turned on and with the given initial values. */
  public HashFunctionalRelation(FunctionalRelation<T1, T2> copy) {
    this(true);
    addAll(copy);
  }
  
  /** Create a HashFunctionalRelation with indexing turned on and with the given initial values. */
  public HashFunctionalRelation(Map<T1, T2> copy) {
    this(true);
    for (Map.Entry<T1, T2> entry : copy.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
  }
  
  /**
   * Optionally create a HashInjectiveRelation without a second-to-firsts index.
   * @param indexSecond  Whether an index from seconds to sets of firsts should be maintained.
   */
  public HashFunctionalRelation(boolean indexSecond) {
    _firstMap = new HashMap<T1, T2>();
    _functionMap = new ImmutableMap<T1, T2>(_firstMap);
    if (indexSecond) {
      _secondIndex = new ConcreteRelationIndex<T2, T1>(CollectUtil.<T2, PredicateSet<T1>>hashMapFactory(),
                                                       CollectUtil.<T1>hashSetFactory(4)) {
        public void validateAdd(T2 second, T1 first) { HashFunctionalRelation.this.validateAdd(first, second); }
        public void addToRelation(T2 second, T1 first) { _firstMap.put(first, second); }
        public void removeFromRelation(T2 second, T1 first) { _firstMap.remove(first); }
        public void clearRelation() { _firstMap.clear(); }
      };
    }
    else {
      _secondIndex = new LazyRelationIndex<T2, T1>(IterUtil.map(this, Pair.<T1, T2>inverter()));
    }
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
    
}
