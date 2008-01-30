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

import java.io.Serializable;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.ImmutableIterator;

/**
 * Wraps a relation in an immutable interface.  Analogous to {@link java.util.Collections#unmodifiableMap}.
 * Note that only only <em>this</em> interface with the data is immutable --
 * if the original data structure is mutable, a client with direct access to that structure can
 * still mutate it.
 */
public class ImmutableRelation<T1, T2> extends AbstractSet<Pair<T1, T2>> implements Relation<T1, T2>, Serializable {
  
  private final Relation<T1, T2> _relation;
  
  public ImmutableRelation(Relation<T1, T2> relation) { _relation = relation; }
  
  public Iterator<Pair<T1, T2>> iterator() { return new ImmutableIterator<Pair<T1, T2>>(_relation.iterator()); }
  public int size() { return _relation.size(); }
  public boolean isEmpty() { return _relation.isEmpty(); }
  public boolean contains(Object o) { return _relation.contains(o); }
  public Object[] toArray() { return _relation.toArray(); }
  public <T> T[] toArray(T[] a) { return _relation.toArray(a); }
  public boolean containsAll(Collection<?> c) { return _relation.containsAll(c); }
  
  public boolean contains(T1 first, T2 second) { return _relation.contains(first, second); }
  public boolean add(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  public boolean remove(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  
  public Set<T1> firstSet() { return _relation.firstSet(); }
  public boolean containsFirst(T1 first) { return _relation.containsFirst(first); }
  public Set<T2> getSeconds(T1 first) { return _relation.getSeconds(first); }
  
  public Set<T2> secondSet() { return _relation.secondSet(); }
  public boolean containsSecond(T2 second) { return _relation.containsSecond(second); }
  public Set<T1> getFirsts(T2 second) { return _relation.getFirsts(second); }
  
  /** Call the constructor (allows {@code T1} and {@code T2} to be inferred) */
  public static <T1, T2> ImmutableRelation<T1, T2> make(Relation<T1, T2> relation) {
    return new ImmutableRelation<T1, T2>(relation);
  }
  
}
