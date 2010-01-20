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

import java.io.Serializable;
import java.util.Iterator;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.SingletonIterator;
import edu.rice.cs.plt.object.ObjectUtil;

/** An immutable {@code Relation} containing a single pair. */
public class SingletonRelation<T1, T2> extends AbstractOneToOneRelation<T1, T2> implements Serializable {
  
  private final T1 _first;
  private final T2 _second;
  
  public SingletonRelation(T1 first, T2 second) { _first = first; _second = second; }
  
  public SingletonRelation(Pair<? extends T1, ? extends T2> pair) {
    _first = pair.first();
    _second = pair.second();
  }
  
  @Override public boolean isEmpty() { return false; }
  @Override public int size() { return 1; }
  @Override public int size(int bound) { return (bound < 1) ? bound : 1; }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return true; }
  public boolean isStatic() { return true; }
  
  public boolean contains(T1 candidate1, T2 candidate2) {
    return ObjectUtil.equal(candidate1, _first) && ObjectUtil.equal(candidate2, _second);
  }
  
  public boolean contains(Object obj) {
    if (obj instanceof Pair<?, ?>) {
      Pair<?, ?> p = (Pair<?, ?>) obj;
      return ObjectUtil.equal(p.first(), _first) && ObjectUtil.equal(p.second(), _second);
    }
    else { return false; }
  }
  
  @Override public Iterator<Pair<T1, T2>> iterator() {
    return new SingletonIterator<Pair<T1, T2>>(Pair.make(_first, _second));
  }
  
  public LambdaMap<T1, T2> functionMap() { return new SingletonMap<T1, T2>(_first, _second); }
  public LambdaMap<T2, T1> injectionMap() { return new SingletonMap<T2, T1>(_second, _first); }
  
  @Override public PredicateSet<T1> firstSet() { return new SingletonSet<T1>(_first); }
  @Override public PredicateSet<T2> matchFirst(T1 match) {
    if ((_first == null) ? (match == null) : _first.equals(match)) {
      return new SingletonSet<T2>(_second);
    }
    else { return EmptySet.make(); }
  }
  
  @Override public PredicateSet<T2> secondSet() { return new SingletonSet<T2>(_second); }
  @Override public PredicateSet<T1> matchSecond(T2 match) {
    if ((_second == null) ? (match == null) : _second.equals(match)) {
      return new SingletonSet<T1>(_first);
    }
    else { return EmptySet.make(); }
  }
  
  @Override public OneToOneRelation<T2, T1> inverse() {
    return new SingletonRelation<T2, T1>(_second, _first);
  }
  
  /** Call the constructor (allows type arguments to be inferred) */
  public static <T1, T2> SingletonRelation<T1, T2> make(T1 first, T2 second) { 
    return new SingletonRelation<T1, T2>(first, second);
  }
  
  /** Call the constructor (allows type arguments to be inferred) */
  public static <T1, T2> SingletonRelation<T1, T2> make(Pair<? extends T1, ? extends T2> pair) { 
    return new SingletonRelation<T1, T2>(pair);
  }
  
}
