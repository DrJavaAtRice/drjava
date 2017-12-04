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
import edu.rice.cs.plt.iter.ReadOnlyIterator;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * The transitive composition of two relations, lazily constructed and dynamically-updated.  An entry 
 * {@code (x, y)} appears in the relation if and only if there is an entry {@code (x, z)} in the first
 * relation and {@code (z, y)} in the second.
 */
public class ComposedRelation<T1, T2, T3> extends AbstractRelation<T1, T3> implements Serializable {
  
  private final Relation<T1, T2> _rel1;
  private final Relation<T2, T3> _rel2;
  private final PredicateSet<T1> _firstSet;
  private final PredicateSet<T3> _secondSet;
  
  public ComposedRelation(Relation<T1, T2> rel1,
                          Relation<T2, T3> rel2) {
    _rel1 = rel1;
    _rel2 = rel2;
    _firstSet = new FilteredSet<T1>(rel1.firstSet(), new Predicate<T1>() {
      public boolean contains(T1 first) {
        for (T2 middle : _rel1.matchFirst(first)) {
          if (_rel2.containsFirst(middle)) { return true; }
        }
        return false;
      }
    });
    _secondSet = new FilteredSet<T3>(rel2.secondSet(), new Predicate<T3>() {
      public boolean contains(T3 second) {
        for (T2 middle : _rel2.matchSecond(second)) {
          if (_rel1.containsSecond(middle)) { return true; }
        }
        return false;
      }
    });
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_rel1, _rel2) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_rel1, _rel2) + 1; }
  
  @Override public boolean isEmpty() { return _firstSet.isEmpty(); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return _rel1.isStatic() && _rel2.isStatic(); }
  
  public boolean contains(T1 first, T3 second) {
    return matchFirst(first).contains(second);
  }
  
  public boolean contains(Object o) {
    if (o instanceof Pair<?, ?>) {
      Pair<?, ?> p = (Pair<?, ?>) o;
      Option<T1> first = CollectUtil.castIfContains(_firstSet, p.first());
      return first.isSome() && matchFirst(first.unwrap()).contains(p.second());
    }
    else { return false; }
  }
        
  /** For each element of {@code rel1.firstSet()}, iterate over all transitive matches. */
  public Iterator<Pair<T1, T3>> iterator() {
    return new ReadOnlyIterator<Pair<T1, T3>>() {
      private final Iterator<T1> _firsts = _firstSet.iterator();
      private T1 _currentFirst = null;
      private Iterator<T2> _currentMiddles = EmptyIterator.<T2>make();
      private T2 _currentMiddle = null;
      private Iterator<? extends T3> _currentSeconds = EmptyIterator.<T3>make();

      public boolean hasNext() {
        return _currentSeconds.hasNext() || _currentMiddles.hasNext() || _firsts.hasNext();
      }
      
      public Pair<T1, T3> next() {
        // the use of _firstSet for _firsts guarantees that none of these match sets is empty
        if (!_currentSeconds.hasNext()) {
          if (!_currentMiddles.hasNext()) {
            _currentFirst = _firsts.next();
            _currentMiddles = _rel1.matchFirst(_currentFirst).iterator();
          }
          _currentMiddle = _currentMiddles.next();
          _currentSeconds = _rel2.matchFirst(_currentMiddle).iterator();
        }
        return new Pair<T1, T3>(_currentFirst, _currentSeconds.next());
      }
    };
  }
  
  public PredicateSet<T1> firstSet() { return _firstSet; }

  public PredicateSet<T3> matchFirst(T1 first) {
    Iterable<PredicateSet<T3>> seconds =
      IterUtil.map(_rel1.matchFirst(first), new Lambda<T2, PredicateSet<T3>>() {
      public PredicateSet<T3> value(T2 middle) { return _rel2.matchFirst(middle); }
    });
    return new IterableSet<T3>(IterUtil.collapse(seconds));
  }
  
  public PredicateSet<T3> secondSet() { return _secondSet; }
  
  public PredicateSet<T1> matchSecond(T3 second) {
    Iterable<PredicateSet<T1>> firsts =
      IterUtil.map(_rel2.matchSecond(second), new Lambda<T2, PredicateSet<T1>>() {
      public PredicateSet<T1> value(T2 middle) { return _rel1.matchSecond(middle); }
    });
    return new IterableSet<T1>(IterUtil.collapse(firsts));
  }
  
}
