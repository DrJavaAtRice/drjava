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
import java.util.*;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.CachedThunk;
import edu.rice.cs.plt.lambda.Thunk;

/**
 * A synchronized set like {@link Collections#synchronizedSet}, but one that returns a snapshot of
 * the set contents on invocations of {@code iterator()}.  In contrast to
 * {@link java.util.concurrent.CopyOnWriteArraySet}, copies are only made when needed for iteration; other
 * operations use locking to support concurrency.  The snapshot strategy has the following advantages over
 * {@link Collections#synchronizedSet}: 1) Thread safety during iteration is guaranteed; 2) the set is
 * interchangeable with other types of sets, even in contexts that perform iteration; 3) concurrent access to 
 * the set is not blocked during iteration; and 4) the set can be directly mutated by the iteration loop
 * (on the other hand, removing elements via the iterator is not supported).  Note, also, that operations on
 * this set cannot be blocked by synchronizing on the set itself.  To support these differences, the
 * implementation must make a copy whenever {@code iterator()} is invoked after the set has been mutated;
 * that copy is cached with the set (optimizing the performance of subsequent calls, but doubling the
 * set's memory footprint).
 */
public class SnapshotSynchronizedSet<E> extends DelegatingSet<E> {
  private final CachedThunk<Iterable<E>> _copy;
  
  public SnapshotSynchronizedSet(Set<E> delegate) {
    super(Collections.synchronizedSet(delegate));
    _copy = CachedThunk.make(new Thunk<Iterable<E>>() {
      public Iterable<E> value() {
        synchronized(_delegate) {
          return IterUtil.snapshot(_delegate.iterator());
        }
      }
    });
  }

  /**
   * Discard the cached copy of the set, if it exists.  This minimizes this set's memory footprint, but
   * forces the copy to be recalculated when {@link #iterator} is next invoked.  Has no effect if
   * {@code iterator()} has not been invoked since the last mutating operation.
   */
  public void discardSnapshot() { _copy.reset(); }
  
  /** Reset the copy thunk if {@code changed} is {@code true}; return {@code changed}. */
  private boolean reset(boolean changed) {
    if (changed) { _copy.reset(); }
    return changed;
  }
  
  @Override public Iterator<E> iterator() { return _copy.value().iterator(); }
  
  @Override public boolean add(E o) { return reset(_delegate.add(o)); }
  @Override public boolean addAll(Collection<? extends E> c) { return reset(_delegate.addAll(c)); }
  @Override public void clear() { _delegate.clear(); reset(true); }
  @Override public boolean remove(Object o) { return reset(_delegate.remove(o)); }
  @Override public boolean removeAll(Collection<?> c) { return reset(_delegate.removeAll(c)); }
  @Override public boolean retainAll(Collection<?> c) { return reset(_delegate.retainAll(c)); }
  
  /** Get a thunk that invokes the constructor with sets produced by the given factory. */
  public static <T> Thunk<Set<T>> factory(Thunk<? extends Set<T>> delegateFactory) {
    return new Factory<T>(delegateFactory);
  }
  
  private static final class Factory<T> implements Thunk<Set<T>>, Serializable {
    private final Thunk<? extends Set<T>> _delegateFactory;
    private Factory(Thunk<? extends Set<T>> delegateFactory) { _delegateFactory = delegateFactory; }
    public Set<T> value() { return new SnapshotSynchronizedSet<T>(_delegateFactory.value()); }
  }
  
  /** Call the constructor (allows {@code T} to be inferred). */
  public static <T> SnapshotSynchronizedSet<T> make(Set<T> delegate) {
    return new SnapshotSynchronizedSet<T>(delegate);
  }
  /** Call the constructor with an empty HashSet. */
  public static <T> SnapshotSynchronizedSet<T> makeHash() {
    return new SnapshotSynchronizedSet<T>(new HashSet<T>());
  }
  /** Call the constructor with an empty LinkedHashSet. */
  public static <T> SnapshotSynchronizedSet<T> makeLinkedHash() {
    return new SnapshotSynchronizedSet<T>(new LinkedHashSet<T>());
  }
  /** Call the constructor with an empty TreeSet. */
  public static <T> SnapshotSynchronizedSet<T> makeTree() {
    return new SnapshotSynchronizedSet<T>(new TreeSet<T>());
  }
  
}
