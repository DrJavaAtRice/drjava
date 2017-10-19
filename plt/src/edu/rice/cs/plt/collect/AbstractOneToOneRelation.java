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
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.MutableSingletonIterator;

/**
 * An abstract parent class for implementations of OneToOneRelation.  Subclasses must provide
 * {@link #isStatic}, {@link #functionMap}, and {@link #injectionMap}.  To support mutation,
 * they must also override {@link #add(Object, Object)} and {@link #remove(Object, Object)}.
 * For best performance, they may also override {@link #clear}.
 */
public abstract class AbstractOneToOneRelation<T1, T2> extends AbstractFunctionalRelation<T1, T2>
                                                       implements OneToOneRelation<T1, T2> {
  
  public abstract boolean isStatic();
  public abstract LambdaMap<T1, T2> functionMap();
  public abstract LambdaMap<T2, T1> injectionMap();
  
  // Code copied from AbstractInjectiveRelation because we can't have multiple inheritance:
  
  /** Returns {@code injectionMap().keySet()}. */
  public PredicateSet<T2> secondSet() { return injectionMap().keySet(); }

  /** Returns {@code injectionMap().containsKey(second)}. */
  @Override public boolean containsSecond(T2 second) { return injectionMap().containsKey(second); }

  /** Returns a set that queries and manipulates the mapping from {@code second} in {@code injectionMap()}. */
  public PredicateSet<T1> matchSecond(T2 second) { return new MatchSecondSet(second); }
  
  /** Returns {@code injectionMap().get(first)}. */
  public T1 antecedent(T2 second) { return injectionMap().get(second); }
  
  /** Returns an {@link InverseOneToOneRelation}. */
  @Override public OneToOneRelation<T2, T1> inverse() { return new InverseOneToOneRelation(); }
  
  /**
   * A result of {@code matchSecond()} defined in terms of {@code injectionMap()}.  The size and
   * contents are determined by the map's {@code containsKey()} and {@code get()} methods;
   * mutation delegates to {@code put()} and {@code remove()}.
   */
  private final class MatchSecondSet extends AbstractPredicateSet<T1> implements Serializable {
    private final T2 _key;

    public MatchSecondSet(T2 second) { _key = second; }

    @Override public boolean isEmpty() { return !injectionMap().containsKey(_key); }
    @Override public int size() { return injectionMap().containsKey(_key) ? 1 : 0; }
    @Override public int size(int bound) { return (bound == 0) ? 0 : size(); }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return AbstractOneToOneRelation.this.isStatic(); }
    public boolean isStatic() { return AbstractOneToOneRelation.this.isStatic(); }

    public boolean contains(Object val) {
      return AbstractOneToOneRelation.this.contains(Pair.make(val, _key));
    }

    public Iterator<T1> iterator() {
      final LambdaMap<T2, T1> map = injectionMap();
      if (map.containsKey(_key)) {
        return new MutableSingletonIterator<T1>(map.get(_key), new Runnable1<T1>() {
          public void run(T1 val) { map.remove(_key); }
        });
      }
      else { return EmptyIterator.make(); }
    }
    
    @Override public boolean add(T1 val) {
      boolean result = !AbstractOneToOneRelation.this.contains(val, _key);
      if (result) { injectionMap().put(_key, val); }
      return result;
    }
    
    @Override public boolean remove(Object val) {
      boolean result = AbstractOneToOneRelation.this.contains(Pair.make(val, _key));
      if (result) { injectionMap().remove(_key); }
      return result;
    }
    
    @Override public void clear() { injectionMap().remove(_key); }
  }
  
  /**
   * An inverse of the enclosing relation.  Extends {@link AbstractFunctionalRelation.InverseFunctionalRelation} with
   * the methods necessary to implement OneToOneRelation.
   */
  protected class InverseOneToOneRelation extends InverseFunctionalRelation implements OneToOneRelation<T2, T1> {
    public T1 value(T2 first) { return AbstractOneToOneRelation.this.antecedent(first); }
    public LambdaMap<T2, T1> functionMap() { return AbstractOneToOneRelation.this.injectionMap(); }
    @Override public OneToOneRelation<T1, T2> inverse() { return AbstractOneToOneRelation.this; }
  }
  
}
