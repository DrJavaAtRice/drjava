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
import edu.rice.cs.plt.lambda.Thunk;

/** A implementation of OneToOneRelation based on indexing maps. */
public class IndexedOneToOneRelation<T1, T2> extends AbstractOneToOneRelation<T1, T2> implements Serializable {
  
  private Map<T1, T2> _firstMap;
  private LambdaMap<T1, T2> _functionMap;
  private Map<T2, T1> _secondMap;
  private LambdaMap<T2, T1> _injectionMap;
  
  /** Index using {@link java.util.HashMap}s. */
  public IndexedOneToOneRelation() {
    this(CollectUtil.<T1, T2>hashMapFactory(), CollectUtil.<T2, T1>hashMapFactory());
  }
  
  /** Create an IndexedOneToOneRelation using the given map factories to produce indices. */
  public IndexedOneToOneRelation(Thunk<Map<T1, T2>> firstIndexFactory,
                                 Thunk<Map<T2, T1>> secondIndexFactory) {
    _firstMap = firstIndexFactory.value();
    // TODO: support mutation:
    _functionMap = new ImmutableMap<T1, T2>(_firstMap);
    _secondMap = secondIndexFactory.value();
    // TODO: support mutation:
    _injectionMap = new ImmutableMap<T2, T1>(_secondMap);
  }
  
  public boolean isStatic() { return false; }
  public LambdaMap<T1, T2> functionMap() { return _functionMap; }
  public LambdaMap<T2, T1> injectionMap() { return _injectionMap; }
  
  @Override public boolean add(T1 first, T2 second) {
    boolean result = validateAdd(first, second);
    if (result) {
      _firstMap.put(first, second);
      _secondMap.put(second, first);
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
    else if (_secondMap.containsKey(second)) {
      throw new IllegalArgumentException("Relation already contains an entry for " + second);
    }
    else { return true; }
  }
  
  @Override public boolean remove(T1 first, T2 second) {
    boolean result = contains(first, second);
    if (result) {
      _firstMap.remove(first);
      _secondMap.remove(second);
    }
    return result;
  }
  
  @Override public void clear() {
    _firstMap.clear();
    _secondMap.clear();
  }
  
  /** Make an IndexedOneToOneRelation indexed by {@link java.util.HashMap}s. */
  public static <T1, T2> IndexedOneToOneRelation<T1, T2> makeHashBased() {
    return new IndexedOneToOneRelation<T1, T2>(CollectUtil.<T1, T2>hashMapFactory(),
                                               CollectUtil.<T2, T1>hashMapFactory());
  }
  
  /** Make an IndexedOneToOneRelation indexed by {@link java.util.LinkedHashMap}s. */
  public static <T1, T2> IndexedOneToOneRelation<T1, T2> makeLinkedHashBased() {
    return new IndexedOneToOneRelation<T1, T2>(CollectUtil.<T1, T2>linkedHashMapFactory(),
                                               CollectUtil.<T2, T1>linkedHashMapFactory());
  }
  
  /** Make an IndexedOneToOneRelation indexed by {@link java.util.TreeMap}s. */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
    IndexedOneToOneRelation<T1, T2> makeTreeBased() {
    return new IndexedOneToOneRelation<T1, T2>(CollectUtil.<T1, T2>treeMapFactory(),
                                               CollectUtil.<T2, T1>treeMapFactory());
  }
  
}
