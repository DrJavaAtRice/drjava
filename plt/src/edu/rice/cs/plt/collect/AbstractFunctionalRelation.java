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

import java.util.Iterator;
import java.util.Map;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.iter.MappedIterator;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.MutableSingletonIterator;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An abstract parent class for implementations of FunctionalRelation.  Subclasses must provide
 * {@link #isStatic}, {@link #functionMap}, {@link #secondSet}, and {@link #matchSecond}.
 * To support mutation, they must also override {@link #add(Object, Object)}
 * and {@link #remove(Object, Object)}.  For best performance, they may also override
 * {@link #clear}.
 */
public abstract class AbstractFunctionalRelation<T1, T2> extends AbstractRelation<T1, T2>
                                                         implements FunctionalRelation<T1, T2> {
  
  public abstract boolean isStatic();
  public abstract LambdaMap<T1, T2> functionMap();
  public abstract PredicateSet<T2> secondSet();
  public abstract PredicateSet<T1> matchSecond(T2 second);

  /** Returns {@code functionMap().isEmpty()}. */
  @Override public boolean isEmpty() { return functionMap().isEmpty(); }
  /** Returns {@code functionMap().size()}. */
  @Override public int size() { return functionMap().size(); }
  /** Returns {@code functionMap().keySet().size(bound)}. */
  @Override public int size(int bound) { return functionMap().keySet().size(bound); }
  /** Returns {@code functionMap().keySet().isInfinite()}. */
  public boolean isInfinite() { return functionMap().keySet().isInfinite(); }
  /** Returns {@code functionMap().keySet().hasFixedSize()}. */
  public boolean hasFixedSize() { return functionMap().keySet().hasFixedSize(); }
  
  /** Checks for the given entry in {@code functionMap()}. */
  public boolean contains(T1 first, T2 second) {
    LambdaMap<T1, T2> map = functionMap();
    return map.containsKey(first) && ObjectUtil.equal(map.get(first), second);
  }
  
  /** Checks for the given entry in {@code functionMap()}. */
  public boolean contains(Object obj) {
    if (obj instanceof Pair<?, ?>) {
      Pair<?, ?> p = (Pair<?, ?>) obj;
      LambdaMap<T1, T2> map = functionMap();
      return map.containsKey(p.first()) && ObjectUtil.equal(map.get(p.first()), p.second());
    }
    else { return false; }
  }
  
  /** Produces an iterator based on {@code functionMap().entrySet()}. */
  public Iterator<Pair<T1, T2>> iterator() {
    return MappedIterator.make(functionMap().entrySet().iterator(),
                               new Lambda<Map.Entry<T1, T2>, Pair<T1, T2>>() {
      public Pair<T1, T2> value(Map.Entry<T1, T2> entry) {
        return new Pair<T1, T2>(entry.getKey(), entry.getValue());
      }
    });
  }

  /** Returns {@code functionMap().keySet()}. */
  public PredicateSet<T1> firstSet() { return functionMap().keySet(); }

  /** Returns {@code functionMap().containsKey(first)}. */
  @Override public boolean containsFirst(T1 first) { return functionMap().containsKey(first); }

  /** Returns a set that queries and manipulates the mapping from {@code first} in {@code functionMap()}. */
  public PredicateSet<T2> matchFirst(T1 first) { return new MatchFirstSet(first); }
  
  /** Returns {@code functionMap().get(first)}. */
  public T2 value(T1 first) { return functionMap().get(first); }
  
  /** Returns an {@link InverseFunctionalRelation}. */
  @Override public Relation<T2, T1> inverse() { return new InverseFunctionalRelation(); }
  
  /**
   * A result of {@code matchFirst()} defined in terms of {@code functionMap()}.  The size and
   * contents are determined by the map's {@code containsKey()} and {@code get()} methods;
   * mutation delegates to {@code put()} and {@code remove()}.
   */
  private final class MatchFirstSet extends AbstractPredicateSet<T2> implements Serializable {
    private final T1 _key;

    public MatchFirstSet(T1 first) { _key = first; }

    @Override public boolean isEmpty() { return !functionMap().containsKey(_key); }
    @Override public int size() { return functionMap().containsKey(_key) ? 1 : 0; }
    @Override public int size(int bound) { return (bound == 0) ? 0 : size(); }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return AbstractFunctionalRelation.this.isStatic(); }
    public boolean isStatic() { return AbstractFunctionalRelation.this.isStatic(); }

    public boolean contains(Object val) {
      return AbstractFunctionalRelation.this.contains(Pair.make(_key, val));
    }

    public Iterator<T2> iterator() {
      final LambdaMap<T1, T2> map = functionMap();
      if (map.containsKey(_key)) {
        return new MutableSingletonIterator<T2>(map.get(_key), new Runnable1<T2>() {
          public void run(T2 val) { map.remove(_key); }
        });
      }
      else { return EmptyIterator.make(); }
    }
    
    @Override public boolean add(T2 val) {
      boolean result = !AbstractFunctionalRelation.this.contains(_key, val);
      if (result) { functionMap().put(_key, val); }
      return result;
    }
    
    @Override public boolean remove(Object val) {
      boolean result = AbstractFunctionalRelation.this.contains(Pair.make(_key, val));
      if (result) { functionMap().remove(_key); }
      return result;
    }
    
    @Override public void clear() { functionMap().remove(_key); }
  }
  
  /**
   * An inverse of the enclosing relation.  Extends {@link AbstractRelation.InverseRelation} with
   * the methods necessary to implement InjectiveRelation.
   */
  protected class InverseFunctionalRelation extends InverseRelation implements InjectiveRelation<T2, T1> {
    public T2 antecedent(T1 second) { return AbstractFunctionalRelation.this.value(second); }
    public LambdaMap<T1, T2> injectionMap() { return AbstractFunctionalRelation.this.functionMap(); }
    @Override public FunctionalRelation<T1, T2> inverse() { return AbstractFunctionalRelation.this; }
  }
  
}
