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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.rice.cs.plt.lambda.CachedThunk;
import edu.rice.cs.plt.lambda.Thunk;

/**
 * A synchronized list like {@link Collections#synchronizedList}, but one that returns a snapshot of
 * the list contents on invocations of {@code iterator()}.  In contrast to
 * {@link java.util.concurrent.CopyOnWriteArrayList}, copies are only made when needed for iteration; other
 * operations use locking to support concurrency.  The snapshot strategy has the following advantages over
 * {@link Collections#synchronizedList}: 1) Thread safety during iteration is guaranteed; 2) the list is
 * interchangeable with other types of lists, even in contexts that perform iteration; 3) concurrent access to 
 * the list is not blocked during iteration; and 4) the list can be directly mutated by the iteration loop
 * (on the other hand, removing elements via the iterator is not supported).  Note, also, that operations on
 * this list cannot be blocked by synchronizing on the list itself.  To support these differences, the
 * implementation must make a copy whenever {@code iterator()} is invoked after the list has been mutated;
 * that copy is cached with the list (optimizing the performance of subsequent calls, but doubling the
 * list's memory footprint).
 */
public class SnapshotSynchronizedList<E> extends DelegatingList<E> {
  private final Object _lock;
  private final CachedThunk<List<E>> _copy;
  
  public SnapshotSynchronizedList(List<E> delegate) { this(Collections.synchronizedList(delegate), null); }
  
  /** The {@code lock} field may be null, indicating that {@code synchronziedDelegate} should be used. */ 
  private SnapshotSynchronizedList(List<E> synchronizedDelegate, Object lock) {
    super(synchronizedDelegate);
    _lock = (lock == null) ? synchronizedDelegate : lock;
    _copy = CachedThunk.make(new Thunk<List<E>>() {
      public List<E> value() {
        synchronized(_lock) { return new ArrayList<E>(_delegate); }
      }
    });
  }
  
  /**
   * Discard the cached copy of the list, if it exists.  This minimizes this list's memory footprint, but
   * forces the copy to be recalculated when {@link #iterator} is next invoked.  Has no effect if
   * {@code iterator()} has not been invoked since the last mutating operation.
   */
  public void discardSnapshot() {
    // to facilitate overridden behavior in subList(), this is the *only* code that should call _copy.reset()
    _copy.reset();
  }
  
  /** Reset the copy thunk if {@code changed} is {@code true}; return {@code changed}. */
  private boolean reset(boolean changed) {
    if (changed) { discardSnapshot(); }
    return changed;
  }
  
  @Override public Iterator<E> iterator() { return _copy.value().iterator(); }
  @Override public ListIterator<E> listIterator() { return _copy.value().listIterator(); }
  @Override public ListIterator<E> listIterator(int index) { return _copy.value().listIterator(index); }
  
  @Override public boolean add(E o) { return reset(_delegate.add(o)); }
  @Override public boolean addAll(Collection<? extends E> c) { return reset(_delegate.addAll(c)); }
  @Override public void clear() { _delegate.clear(); discardSnapshot(); }
  @Override public boolean remove(Object o) { return reset(_delegate.remove(o)); }
  @Override public boolean removeAll(Collection<?> c) { return reset(_delegate.removeAll(c)); }
  @Override public boolean retainAll(Collection<?> c) { return reset(_delegate.retainAll(c)); }
  
  @Override public void add(int index, E element) { _delegate.add(index, element); discardSnapshot(); }
  @Override public boolean addAll(int index, Collection<? extends E> c) { return reset(_delegate.addAll(index, c)); }

  @Override public E set(int index, E element) {
    E result = _delegate.set(index, element);
    discardSnapshot();
    return result;
  }
  
  @Override public E remove(int index) {
    E result = _delegate.remove(index);
    discardSnapshot();
    return result;
  }
  
  @Override public List<E> subList(final int from, int to) {
    return new SnapshotSynchronizedList<E>(_delegate.subList(from, to), _lock) {
      @Override public void discardSnapshot() {
        // discard both this cache and that of the outer list(s)
        SnapshotSynchronizedList.this.discardSnapshot();
        super.discardSnapshot();
      }
    };
  }
  
  /** Get a thunk that invokes the constructor with sets produced by the given factory. */
  public static <T> Thunk<List<T>> factory(Thunk<? extends List<T>> delegateFactory) {
    return new Factory<T>(delegateFactory);
  }
  
  private static final class Factory<T> implements Thunk<List<T>>, Serializable {
    private final Thunk<? extends List<T>> _delegateFactory;
    private Factory(Thunk<? extends List<T>> delegateFactory) { _delegateFactory = delegateFactory; }
    public List<T> value() { return new SnapshotSynchronizedList<T>(_delegateFactory.value()); }
  }
  
  
}
