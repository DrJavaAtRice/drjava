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
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.ComposedIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * The union of two relations, lazily constructed and updated dynamically.
 */
public class UnionRelation<T1, T2> extends AbstractRelation<T1, T2> implements Composite, Serializable {
  private final Relation<T1, T2> _rel1;
  private final Relation<T1, T2> _rel2;
  private final Relation<T1, T2> _rel2Extras;
  
  /**
   * For best performance of {@link #size}, {@code rel2} should be the smaller of the two
   * relations (this is not handled automatically because calculating sizes may be expensive).
   */
  public UnionRelation(Relation<T1, T2> rel1, Relation<T1, T2> rel2) {
    _rel1 = rel1;
    _rel2 = rel2;
    _rel2Extras = new ComplementRelation<T1, T2>(rel2, rel1);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_rel1, _rel2) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_rel1, _rel2) + 1; }
  
  public boolean contains(T1 first, T2 second) {
    return _rel1.contains(first, second) || _rel2.contains(first, second);
  }
  
  public boolean contains(Object o) {
    return _rel1.contains(o) || _rel2.contains(o);
  }
  
  public Iterator<Pair<T1, T2>> iterator() {
    return ImmutableIterator.make(ComposedIterator.make(_rel1.iterator(), _rel2Extras.iterator()));
  }
  
  public PredicateSet<T1> firstSet() {
    return new UnionSet<T1>(_rel1.firstSet(), _rel2.firstSet());
  }
  
  public PredicateSet<T2> matchFirst(T1 first) {
    return new UnionSet<T2>(_rel1.matchFirst(first), _rel2.matchFirst(first));
  }
  
  public PredicateSet<T2> secondSet() {
    return new UnionSet<T2>(_rel1.secondSet(), _rel2.secondSet());
  }
  
  public PredicateSet<T1> matchSecond(T2 second) {
    return new UnionSet<T1>(_rel1.matchSecond(second), _rel2.matchSecond(second));
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_rel1) || IterUtil.isInfinite(_rel2); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_rel1) && IterUtil.hasFixedSize(_rel2); }
  public boolean isStatic() { return IterUtil.isStatic(_rel1) && IterUtil.isStatic(_rel2); }
  
  
  /** Linear in the size of {@code rel2}. */
  @Override public int size() {
    return _rel1.size() + _rel2Extras.size();
  }
  
  @Override public int size(int bound) {
    int size1 = IterUtil.sizeOf(_rel1, bound);
    int bound2 = bound - size1;
    int size2 = (bound2 > 0) ? IterUtil.sizeOf(_rel2Extras, bound) : 0;
    return size1 + size2;
  }
  
  @Override public boolean isEmpty() {
    return _rel1.isEmpty() && _rel2.isEmpty();
  }
  
}
