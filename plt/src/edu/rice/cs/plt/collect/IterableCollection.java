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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Collection;
import java.io.Serializable;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A Collection wrapping an Iterable.  Allows iterables to be viewed as collections without creating a
 * copy (which would require linear time).  Does not support mutation, but does reflect 
 * changes made to the underlying iterable.
 */
public class IterableCollection<E> extends AbstractCollection<E>
                                   implements SizedIterable<E>, Composite, Serializable {
  
  private final Iterable<? extends E> _iter;
  private final boolean _fixedSize;
  private int _size; // -1 when uninitialized
  
  public IterableCollection(Iterable<? extends E> iter) {
    _iter = iter;
    _fixedSize = IterUtil.hasFixedSize(iter);
    _size = -1;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_iter) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_iter) + 1; }
  
  @Override public boolean isEmpty() {
    if (_size == -1) { return IterUtil.isEmpty(_iter); }
    else { return _size == 0; }
  }
  
  public int size() {
    if (_fixedSize) {
      if (_size == -1) { _size = IterUtil.sizeOf(_iter); }
      return _size;
    }
    else { return IterUtil.sizeOf(_iter); }
  }
  
  public int size(int bound) {
    if (_fixedSize) {
      if (_size == -1) {
        int result = IterUtil.sizeOf(_iter, bound);
        if (result < bound) { _size = result; }
        return result;
      }
      else { return (_size < bound) ? _size : bound; }
    }
    else { return IterUtil.sizeOf(_iter, bound); }
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_iter); }
  public boolean hasFixedSize() { return _fixedSize; }
  public boolean isStatic() { return _fixedSize && IterUtil.isStatic(_iter); }
  
  @Override public boolean contains(Object o) {
    return IterUtil.contains(_iter, o);
  }
  
  public Iterator<E> iterator() { return new ImmutableIterator<E>(_iter.iterator()); }
  
  @Override public boolean add(E o) { throw new UnsupportedOperationException(); }
  @Override public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
  @Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
  @Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  @Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
  @Override public void clear() { throw new UnsupportedOperationException(); }
  
  public String toString() { return _iter.toString(); }
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (!(o instanceof IterableCollection<?>)) { return false; }
    else { return _iter.equals(((IterableCollection<?>) o)._iter); }
  }
  public int hashCode() { return IterableCollection.class.hashCode() ^ _iter.hashCode(); }
}
