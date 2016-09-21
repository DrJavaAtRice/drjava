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

import java.util.Collection;
import edu.rice.cs.plt.tuple.Pair;

/**
 * Wraps a relation in an immutable interface.  Analogous to {@link java.util.Collections#unmodifiableMap}.
 * Note that only only <em>this</em> interface with the data is immutable -- if the original data
 * structure is mutable, a client with direct access to that structure can still mutate it.
 * Subclasses can invoke the overridden methods in {@link java.util.AbstractCollection} to use the
 * default implementations there by invoking, for example, {@link #abstractCollectionAddAll}
 * (see {@link java.util.AbstractCollection} for details on the default implementations).
 */
public class ImmutableRelation<T1, T2> extends DelegatingRelation<T1, T2> {
  
  private ImmutableRelation<T2, T1> _inverse; // may be null if not yet created
  
  public ImmutableRelation(Relation<T1, T2> relation) { super(relation); _inverse = null; }
  
  private ImmutableRelation(Relation<T1, T2> relation, ImmutableRelation<T2, T1> inverse) {
    super(relation);
    _inverse = inverse;
  }
  
  public boolean add(Pair<T1, T2> o) { throw new UnsupportedOperationException(); }
  public boolean remove(Object o) { throw new UnsupportedOperationException(); }
  public boolean addAll(Collection<? extends Pair<T1, T2>> c) { throw new UnsupportedOperationException(); }
  public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  public void clear() { throw new UnsupportedOperationException(); }
  
  public boolean contains(T1 first, T2 second) {
    return _delegate.contains(first, second);
  }
  public boolean add(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  public boolean remove(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  
  public Relation<T2, T1> inverse() {
    if (_inverse == null) {
      _inverse = new ImmutableRelation<T2, T1>(_delegate.inverse(), this);
    }
    return _inverse;
  }
  
  public PredicateSet<T1> firstSet() {
    return new ImmutableSet<T1>(_delegate.firstSet());
  }
  public boolean containsFirst(T1 first) {
    return _delegate.containsFirst(first);
  }
  public PredicateSet<T2> matchFirst(T1 first) {
    return new ImmutableSet<T2>(_delegate.matchFirst(first));
  }
  public PredicateSet<T2> excludeFirsts() {
    return new ImmutableSet<T2>(_delegate.excludeFirsts());
  }

  public PredicateSet<T2> secondSet() {
    return new ImmutableSet<T2>(_delegate.secondSet());
  }
  public boolean containsSecond(T2 second) {
    return _delegate.containsSecond(second);
  }
  public PredicateSet<T1> matchSecond(T2 second) {
    return new ImmutableSet<T1>(_delegate.matchSecond(second));
  }
  public PredicateSet<T1> excludeSeconds() {
    return new ImmutableSet<T1>(_delegate.excludeSeconds());
  }
  
  /** Call the constructor (allows {@code T1} and {@code T2} to be inferred). */
  public static <T1, T2> ImmutableRelation<T1, T2> make(Relation<T1, T2> relation) {
    return new ImmutableRelation<T1, T2>(relation);
  }
  
}
