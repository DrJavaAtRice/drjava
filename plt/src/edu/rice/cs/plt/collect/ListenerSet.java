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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Runnable1;

/**
 * A mutable set of listeners.  Each listener is a {@link Runnable1} and receives an argument
 * of type {@code T}.  In typical usage, a class supporting listeners has some number of
 * {@code ListenerSet<T>} fields and associated getters returning {@code ListenerSet<T>.Sink}s
 * (one for each different kind of event to be responded to).  Clients use expressions like
 * {@code foo.listeners().add(...)} to add listeners; the listener class then calls
 * {@code _listeners.run(...)} when an event occurs.
 */
public class ListenerSet<T> extends DelegatingSet<Runnable1<? super T>> implements Runnable1<T> {

  private final Sink _sink;
  
  /**
   * Create a ListenerSet backed by a {@link CopyOnWriteArraySet} (thread-safe and efficient for
   * frequent traversal, but slow when frequently mutated).  
   */
  public ListenerSet() { this(new CopyOnWriteArraySet<Runnable1<? super T>>()); }
  
  /** Create a ListenerSet backed by the given set. */
  public ListenerSet(Set<Runnable1<? super T>> delegate) {
    super(delegate);
    _sink = new Sink();
  }
  
  /**
   * Pass the given value to each of the listeners in turn.  Execution order is determined by
   * the backing set.  If an exception occurs in a listener, that exception is set aside until
   * all listeners can be run.  Upon completion, the first exception to occur (if any) is thrown.
   */ 
  public void run(T arg) {
    RuntimeException exception = null;
    for (Runnable1<? super T> l : _delegate) {
      try { l.run(arg); }
      catch (RuntimeException e) {
        if (exception == null) { exception = e; }
      }
    }
    if (exception != null) { throw exception; }
  }
  
  /**
   * Get a write-only sink view of the set.  Clients should generally use this interface to register
   * listeners.
   */
  public Sink sink() { return _sink; }
  
  
  /**
   * A write-only view of the set.  This interface ensures that clients act independently &mdash;
   * a client with only a reference to a certain listener cannot view, remove, or run other listeners.
   */
  public class Sink {
    
    /** Add the given listener to the set. */
    public boolean add(Runnable1<? super T> listener) {
      return _delegate.add(listener);
    }
    
    /** Add the given listener to the set, wrapped with {@link LambdaUtil#promote(Runnable)}. */
    public boolean add(Runnable listener) {
      return _delegate.add(LambdaUtil.promote(listener));
    }
    
    /** Add the given listeners to the set. */
    public boolean addAll(Iterable<Runnable1<? super T>> addList) {
      return CollectUtil.addAll(_delegate, addList);
    }
    
    /** Remove the given listener from the set. */
    public boolean remove(Runnable1<? super T> listener) {
      return _delegate.remove(listener);
    }
    
    /** Remove all of the given listeners from the set. */
    public boolean removeAll(Iterable<Runnable1<? super T>> removeList) {
      return CollectUtil.removeAll(_delegate, removeList);
    }
    
  }
  
  /** Call the constructor (allows {@code T} to be inferred). */
  public static <T> ListenerSet<T> make(Set<Runnable1<? super T>> delegate) {
    return new ListenerSet<T>(delegate);
  }
  
}
