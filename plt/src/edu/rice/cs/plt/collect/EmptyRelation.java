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

import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.EmptyIterator;

public final class EmptyRelation<T1, T2> implements Relation<T1, T2> {

  public static final EmptyRelation<Object, Object> INSTANCE = new EmptyRelation<Object, Object>();

  @SuppressWarnings("unchecked") public static <T1, T2> EmptyRelation<T1, T2> make() {
    return (EmptyRelation<T1, T2>) INSTANCE;
  }

  private EmptyRelation() {}

  public int size() { return 0; }
  public boolean isEmpty() { return true; }
  public boolean contains(Object o) { return false; }
  public boolean contains(T1 first, T2 second) { return false; }
  public Iterator<Pair<T1, T2>> iterator() { return EmptyIterator.<Pair<T1, T2>>make(); }
  public Object[] toArray() { return new Object[0]; }
  public <T> T[] toArray(T[] a) {
    if (a.length > 0) { a[0] = null; }
    return a;
  }

  public boolean add(Pair<T1, T2> o) { throw new UnsupportedOperationException(); }
  public boolean add(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  public boolean remove(Object o) { return false; }
  public boolean remove(T1 first, T2 second) { return false; }

  public boolean containsAll(Collection<?> c) { return c.isEmpty(); }
  public boolean addAll(Collection<? extends Pair<T1, T2>> c) {
    if (c.isEmpty()) { return false; }
    else { throw new UnsupportedOperationException(); }
  }
  public boolean retainAll(Collection<?> c) { return false; }
  public boolean removeAll(Collection<?> c) { return false; }
  public void clear() {}
  
  public Set<T1> firstSet() { return CollectUtil.<T1>emptySet(); }
  public boolean containsFirst(T1 first) { return false; }
  public Set<T2> getSeconds(T1 first) { return CollectUtil.<T2>emptySet(); }
  public Set<T2> secondSet() { return CollectUtil.<T2>emptySet(); }
  public boolean containsSecond(T2 second) { return false; }
  public Set<T1> getFirsts(T2 second) { return CollectUtil.<T1>emptySet(); }

  public String toString() { return "[]"; }

  public boolean equals(Object o) {
    if (o instanceof Set<?>) { return ((Set<?>) o).isEmpty(); }
    else { return false; }
  }

  public int hashCode() { return 0; }

}

