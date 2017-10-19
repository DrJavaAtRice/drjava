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
import java.io.Serializable;
import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.iter.SingletonIterator;

/** An immutable {@code PredicateSet} with a single element. */
public class SingletonSet<E> extends AbstractPredicateSet<E> implements Serializable {
  
  private final E _elt;
  
  public SingletonSet(E elt) { _elt = elt; }
  
  public boolean contains(Object o) { return (_elt == null) ? (o == null) : _elt.equals(o); }
  public Iterator<E> iterator() { return new SingletonIterator<E>(_elt); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return true; }
  public boolean isStatic() { return true; }
  
  @Override public int size() { return 1; }
  @Override public int size(int bound) { return (bound < 1) ? bound : 1; }
  @Override public boolean isEmpty() { return false; }
  @Override public Object[] toArray() { return new Object[]{ _elt }; }
  
  @Override public boolean add(E o) { throw new UnsupportedOperationException(); }
  @Override public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
  @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
  @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  @Override public void clear() { throw new UnsupportedOperationException(); }
  
  /** Call the constructor (allows {@code E} to be inferred) */
  public static <E> SingletonSet<E> make(E element) { 
    return new SingletonSet<E>(element);
  }
  
  @SuppressWarnings("unchecked") public static <T> Lambda<T, SingletonSet<T>> factory() {
    return (Factory<T>) Factory.INSTANCE;
  }
  
  private static final class Factory<T> implements Lambda<T, SingletonSet<T>>, Serializable {
    public static final Factory<Object> INSTANCE = new Factory<Object>();
    private Factory() {}
    public SingletonSet<T> value(T arg) { return new SingletonSet<T>(arg); }
  }
  
}
