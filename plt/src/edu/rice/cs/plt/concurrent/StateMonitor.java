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

package edu.rice.cs.plt.concurrent;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import edu.rice.cs.plt.lambda.Box;

/**
 * <p>Provides a convenient facility for blocking until a change to an expected state explicitly occurs.
 * Each time the wrapped state value changes, the blocked threads check to see if the new state matches
 * the desired state (this test is performed with {@code equals()}).</p>
 * 
 * <p>Ideally, this class would extend {@link ConcurrentBox}.  Unfortunately, the methods of {@link AtomicReference}
 * are {@code final}, and so they can't be overridden here.  Also, since locking is necessary when performing thread
 * notification, these implementations simply use synchronization, rather than relying on built-in non-blocking
 * atomic primitives.</p>
 */ 
public class StateMonitor<T> implements Box<T>, Serializable {
  
  private volatile T _state;
  
  public StateMonitor(T state) { _state = state; }
  
  public T value() { return _state; }
  
  public synchronized void set(T newState) {
    _state = newState;
    this.notifyAll();
  }
  
  public synchronized T getAndSet(T state) {
    T result = _state;
    _state = state;
    this.notifyAll();
    return result;
  }
  
  public synchronized boolean compareAndSet(T expect, T update) {
    if (_state == expect) {
      _state = update;
      this.notifyAll();
      return true;
    }
    else { return false; }
  }
  
  /** Ensures that the state equals {@code expected} before continuing.  Blocks if necessary. */
  public synchronized void ensureState(T expected) throws InterruptedException {
    while ((expected == null) ? _state != null : !expected.equals(_state)) { this.wait(); }
  }
  
  /**
   * Ensures that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait is interrupted,
   * returns {@code false}.
   */
  public boolean attemptEnsureState(T expected) {
    try { ensureState(expected); return true; }
    catch (InterruptedException e) { return false; }
  }
  
}
