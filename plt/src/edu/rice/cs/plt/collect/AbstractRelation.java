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
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.iter.MappedIterator;

/**
 * An abstract parent class for implementations of Relation.  Subclasses must provide
 * the size methods {@link #isInfinite}, {@link #hasFixedSize}, and {@link #isStatic},
 * and the query methods {@link #contains(Object, Object)}, {@link #contains(Object)},
 * {@link #iterator}, {@link #firstSet}, {@link #matchFirst}, {@link #secondSet},
 * and {@link #matchSecond}. To support mutation, they must also override
 * {@link #add(Object, Object)} and {@link #remove(Object, Object)}.  For best performance,
 * they may also override {@link #isEmpty}, {@link #size(int)} and {@link #clear}.
 */
public abstract class AbstractRelation<T1, T2> extends AbstractPredicateSet<Pair<T1, T2>>
                                               implements Relation<T1, T2> {
  
  public abstract boolean isInfinite();
  public abstract boolean hasFixedSize();
  public abstract boolean isStatic();
  
  /**
   * Tests whether the given objects appear as a pair in this relation.  Not guaranteed
   * to have types {@code T1} and {@code T2} because the {@link java.util.Collection#contains}
   * method allows arbitrary objects.
   */
  public abstract boolean contains(T1 first, T2 second);
  public abstract boolean contains(Object obj);
  public abstract Iterator<Pair<T1, T2>> iterator();
  public abstract PredicateSet<T1> firstSet();
  public abstract PredicateSet<T2> matchFirst(T1 first);
  public abstract PredicateSet<T2> secondSet();
  public abstract PredicateSet<T1> matchSecond(T2 second);
  
  public boolean add(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  public boolean remove(T1 first, T2 second) { throw new UnsupportedOperationException(); }
    
  /** Invokes {@link #add(Object, Object)}. */
  public boolean add(Pair<T1, T2> p) { return add(p.first(), p.second()); }
  
  /** Invokes {@link #remove(Object, Object)} if {@code contains(o)} is {@code true}. */
  public boolean remove(Object o) {
    Option<Pair<T1, T2>> cast = CollectUtil.castIfContains(this, o);
    if (cast.isSome()) { return remove(cast.unwrap().first(), cast.unwrap().second()); }
    else { return false; }
  }
  
  /** Returns an {@link InverseRelation}. */
  public Relation<T2, T1> inverse() { return new InverseRelation(); }
  
  /** Returns {@code firstSet().contains(first)}. */
  public boolean containsFirst(T1 first) { return firstSet().contains(first); }
  
  /** Returns {@code secondSet()}. */
  public PredicateSet<T2> excludeFirsts() { return secondSet(); }
  
  /** Returns {@code secondSet().contains(second)}. */
  public boolean containsSecond(T2 second) { return secondSet().contains(second); }
  
  /** Returns {@code firstSet()}. */
  public PredicateSet<T1> excludeSeconds() { return firstSet(); }
  
  /**
   * An inverse of the enclosing relation, defined in terms of {@link Pair#inverse}.  Mutation is
   * supported, with changes reflected in the enclosing relation.  Inherits the AbstractSet implementations
   * of {@link java.util.AbstractSet#toArray()}, {@link java.util.AbstractSet#toArray(Object[])},
   * {@link java.util.AbstractSet#containsAll}, {@link java.util.AbstractSet#addAll},
   * {@link java.util.AbstractSet#retainAll}, {@link java.util.AbstractSet#removeAll},
   * {@code java.util.AbstractSet#toString()}, {@code equals(Object)}, and {@code hashCode()}.  All other
   * methods delegate to their corresponding methods in the enclosing AbstractRelation (inverting pairs
   * as necessary).
   */
  protected class InverseRelation extends AbstractPredicateSet<Pair<T2, T1>>
                                  implements Relation<T2, T1>, Serializable {
    
    public int size() { return AbstractRelation.this.size(); }
    public int size(int bound) { return AbstractRelation.this.size(bound); }
    @Override public boolean isEmpty() { return AbstractRelation.this.isEmpty(); }
    public boolean isInfinite() { return AbstractRelation.this.isInfinite(); }
    public boolean hasFixedSize() { return AbstractRelation.this.hasFixedSize(); }
    public boolean isStatic() { return AbstractRelation.this.isStatic(); }
    
    @Override public boolean contains(Object o) {
      return (o instanceof Pair<?, ?>) &&
             AbstractRelation.this.contains(((Pair<?, ?>) o).inverse());
    }
    
    public Iterator<Pair<T2, T1>> iterator() {
      return new MappedIterator<Pair<T1, T2>, Pair<T2, T1>>(AbstractRelation.this.iterator(),
                                                            Pair.<T1, T2>inverter());
    }
    
    @Override public boolean add(Pair<T2, T1> pair) {
      return AbstractRelation.this.add(pair.inverse());
    }
    
    @Override public boolean remove(Object o) {
      return (o instanceof Pair<?, ?>) &&
             AbstractRelation.this.remove(((Pair<?, ?>) o).inverse());
    }
    
    @Override public void clear() { AbstractRelation.this.clear(); }
    
    public boolean contains(T2 f, T1 s) { return AbstractRelation.this.contains(s, f); }
    public boolean add(T2 f, T1 s) { return AbstractRelation.this.add(s, f); }
    public boolean remove(T2 f, T1 s) { return AbstractRelation.this.remove(s, f); }
    public Relation<T1, T2> inverse() { return AbstractRelation.this; }
    
    public PredicateSet<T2> firstSet() { return AbstractRelation.this.secondSet(); }
    public boolean containsFirst(T2 f) { return AbstractRelation.this.containsSecond(f); }
    public PredicateSet<T1> matchFirst(T2 f) { return AbstractRelation.this.matchSecond(f); }
    public PredicateSet<T1> excludeFirsts() { return AbstractRelation.this.excludeSeconds(); }
    
    public PredicateSet<T1> secondSet() { return AbstractRelation.this.firstSet(); }
    public boolean containsSecond(T1 s) { return AbstractRelation.this.containsFirst(s); }
    public PredicateSet<T2> matchSecond(T1 s) { return AbstractRelation.this.matchFirst(s); }
    public PredicateSet<T2> excludeSeconds() { return AbstractRelation.this.excludeFirsts(); }
  }
  
}
