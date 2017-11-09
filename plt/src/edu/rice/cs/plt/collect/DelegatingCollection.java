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
import java.util.Collection;
import java.util.AbstractCollection;
import java.io.Serializable;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A collection that delegates all operations to a wrapped collection.  Subclasses can be defined
 * that override a few of the methods, while maintaining the default delegation behavior in most cases.
 * Since instances of this class must <em>not</em> be equal to some collections (such as {@code Set}s
 * and {@code List}s -- allowing the equivalence would violate symmetry), the {@code equals()} and
 * {@code hashCode()} methods are not implemented, and inherit the {@link Object} defaults.
 * Subclasses can also invoke the overridden methods in {@link AbstractCollection} to use the
 * default implementations there by invoking, for example, {@link #abstractCollectionAddAll}
 * (see {@link java.util.AbstractCollection} for details on the default implementations).
 */
public class DelegatingCollection<T> extends AbstractCollection<T> implements SizedIterable<T>, Composite, Serializable {
  
  protected Collection<T> _delegate;
  
  public DelegatingCollection(Collection<T> delegate) { _delegate = delegate; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_delegate) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_delegate) + 1; }
  
  public boolean isEmpty() { return _delegate.isEmpty(); }
  public int size() { return _delegate.size(); }
  public int size(int bound) { return IterUtil.sizeOf(_delegate, bound); }
  public boolean isInfinite() { return IterUtil.isInfinite(_delegate); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_delegate); }
  public boolean isStatic() { return IterUtil.isStatic(_delegate); }
  
  public boolean contains(Object o) { return _delegate.contains(o); }
  public boolean containsAll(Collection<?> c) { return _delegate.containsAll(c); }
  public Iterator<T> iterator() { return _delegate.iterator(); }
  public Object[] toArray() { return _delegate.toArray(); }
  public <S> S[] toArray(S[] a) { return _delegate.toArray(a); }
  
  public boolean add(T o) { return _delegate.add(o); }
  public boolean addAll(Collection<? extends T> c) { return _delegate.addAll(c); }
  public boolean remove(Object o) { return _delegate.remove(o); }
  public boolean retainAll(Collection<?> c) { return _delegate.retainAll(c); }
  public boolean removeAll(Collection<?> c) { return _delegate.removeAll(c); }
  public void clear() { _delegate.clear(); }
  
  public String toString() { return _delegate.toString(); }
  
  protected boolean abstractCollectionIsEmpty() { return super.isEmpty(); }
  protected boolean abstractCollectionContains(Object o) { return super.contains(o); }
  protected Object[] abstractCollectionToArray() { return super.toArray(); }
  protected <S> S[] abstractCollectionToArray(S[] a) { return super.toArray(a); }
  protected boolean abstractCollectionRemove(T o) { return super.remove(o); }
  protected boolean abstractCollectionContainsAll(Collection<?> c) { return super.containsAll(c); }
  protected boolean abstractCollectionAddAll(Collection<? extends T> c) { return super.addAll(c); }
  protected boolean abstractCollectionRetainAll(Collection<?> c) { return super.retainAll(c); }
  protected boolean abstractCollectionRemoveAll(Collection<?> c) { return super.removeAll(c); }
  protected void abstractCollectionClear() { super.clear(); }
  
}
