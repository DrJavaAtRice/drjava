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
import java.util.Iterator;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.SizedIterable;

/**
 * Abstract parent class of immutable empty collections.  Subclasses must implement
 * {@link #equals} and {@link #hashCode}.
 */
public abstract class EmptyCollection<T> implements Collection<T>, SizedIterable<T> {

  @Override public abstract boolean equals(Object o);
  @Override public abstract int hashCode();

  public int size() { return 0; }
  public int size(int bound) { return 0; }
  public boolean isEmpty() { return true; }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return true; }
  public boolean isStatic() { return true; }
  
  public boolean contains(Object o) { return false; }
  public boolean containsAll(Collection<?> c) { return c.isEmpty(); }
  
  public Iterator<T> iterator() { return EmptyIterator.make(); }
  
  public Object[] toArray() { return new Object[0]; }
  
  public <S> S[] toArray(S[] a) {
    if (a.length > 0) { a[0] = null; }
    return a;
  }
  
  /** Returns {@code "[]"}. */
  public String toString() { return "[]"; }

  public boolean add(T o) { throw new UnsupportedOperationException(); }
  public boolean addAll(Collection<? extends T> c) { throw new UnsupportedOperationException(); }
  public boolean remove(Object o) { throw new UnsupportedOperationException(); }
  public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  public void clear() { throw new UnsupportedOperationException(); }
  
}
