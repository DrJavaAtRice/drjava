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

import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.Predicate;

/**
 * <p>Provides a convenient facility for blocking until a state change explicitly occurs.
 * Each time the wrapped state value changes, the blocked threads check to see if the new state meets
 * a provided criteria.</p>
 * 
 * <p>Ideally, this class would extend {@link ConcurrentBox}.  Unfortunately, the methods of
 * {@link java.util.concurrent.atomic.AtomicReference} are {@code final}, and so they can't be overridden here.
 * Also, since locking is necessary when performing thread notification, these implementations simply use
 * locking, rather than relying on built-in non-blocking atomic primitives.</p>
 */ 
public class StateMonitor<T> implements Box<T>, Serializable {
  
  private volatile T _state;
  
  public StateMonitor(T state) { _state = state; }
  
  /**
   * Get the current state.  (No guarantee is made about how long this value is valid &mdash; a concurrent state
   * change may occur immediately.)
   */
  public T value() { return _state; }
  
  /** Change the state to {@code newState}. */
  public synchronized void set(T newState) {
    _state = newState;
    this.notifyAll();
  }
  
  /**
   * Change the state to {@code newState}; return the previous state.
   * @see java.util.concurrent.atomic.AtomicReference#getAndSet
   */  
  public synchronized T getAndSet(T newState) {
    T result = _state;
    _state = newState;
    this.notifyAll();
    return result;
  }
  
  /**
   * If the current state is {@code expect}, change it to {@code update}.  In this case, {@code true} is 
   * returned.  Otherwise, give up and return {@code false}.
   * @see java.util.concurrent.atomic.AtomicReference#compareAndSet
   */
  public synchronized boolean compareAndSet(T expect, T update) {
    if (_state == expect) {
      _state = update;
      this.notifyAll();
      return true;
    }
    else { return false; }
  }
  
  /**
   * Ensures that the state equals {@code expected} before continuing.  Blocks if necessary.
   * @return  The state at the time this method returns (always equal to {@code expected}).
   */
  public synchronized T ensureState(T expected) throws InterruptedException {
    while (!inState(expected)) { this.wait(); }
    return _state;
  }
  
  /**
   * Tries to ensure that the state equals {@code expected} before continuing.  Blocks if necessary.  Stops waiting
   * if interrupted.
   * @return  The state at the time this method returns (if not interrupted, this is equal to {@code expected}).
   */
  public synchronized T attemptEnsureState(T expected) {
    try { return ensureState(expected); }
    catch (InterruptedException e) { return _state; }
  }
  
  /**
   * Tries to ensure that the state equals {@code expected} before continuing.  Blocks if necessary.  If interrupted
   * or if the timeout is reached, stops waiting.
   * @param timeout  Maximum wait time, in milliseconds.  Must be positive or zero (where zero signals, as in
   *                 {@link Object#wait(long)}, that no timeout should be used).
   * @return  The state at the time this method returns (barring an interrupt or timeout, this is equal to
   *          {@code expected}).
   */
  public synchronized T attemptEnsureState(T expected, long timeout) {
    if (timeout == 0) { return attemptEnsureState(expected); }
    else if (inState(expected)) { return _state; }
    else {
      // must record expected wake-up time to account for spurious wake-ups
      long timeoutTime = System.currentTimeMillis() + timeout;
      try {
        do {
          this.wait(timeout);
          long currentTime = System.currentTimeMillis();
          if (currentTime >= timeoutTime) { break; } // timeout has been reached
          else { timeout = timeoutTime - currentTime; }
        } while (!inState(expected));
        return _state;
      }
      catch (InterruptedException e) { return _state; }
    }
  }
  
  /**
   * Ensures that the state does not equal {@code wrong} before continuing.  Blocks if necessary.
   * @return  The state at the time this method returns (never equal to {@code wrong}).
   */
  public synchronized T ensureNotState(T wrong) throws InterruptedException {
    while (inState(wrong)) { this.wait(); }
    return _state;
  }
  
  /**
   * Tries to ensure that the state does not equal {@code wrong} before continuing.  Blocks if necessary.
   * Stops waiting if interrupted.
   * @return  The state at the time this method returns (if not interrupted, this is not equal to {@code wrong}).
   */
  public synchronized T attemptEnsureNotState(T wrong) {
    try { return ensureNotState(wrong); }
    catch (InterruptedException e) { return _state; }
  }
  
  /**
   * Tries to ensure that the state does not equal {@code wrong} before continuing.  Blocks if necessary.
   * If interrupted or if the timeout is reached, stops waiting.
   * @param timeout  Maximum wait time, in milliseconds.  Must be positive or zero (where zero signals, as in
   *                 {@link Object#wait(long)}, that no timeout should be used).
   * @return  The state at the time this method returns (barring an interrupt or timeout, this is not equal to
   *          {@code wrong}).
   */
  public synchronized T attemptEnsureNotState(T wrong, long timeout) {
    if (timeout == 0) { return attemptEnsureNotState(wrong); }
    else if (!inState(wrong)) { return _state; }
    else {
      // must record expected wake-up time to account for spurious wake-ups
      long timeoutTime = System.currentTimeMillis() + timeout;
      try {
        do {
          this.wait(timeout);
          long currentTime = System.currentTimeMillis();
          if (currentTime >= timeoutTime) { break; } // timeout has been reached
          else { timeout = timeoutTime - currentTime; }
        } while (inState(wrong));
        return _state;
      }
      catch (InterruptedException e) { return _state; }
    }
  }
  
  /**
   * Ensures that {@code predicate} accepts the current state before continuing.  Blocks if necessary.
   * @return  The state at the time this method returns (always a state accepted by {@code predicate}).
   */
  public synchronized T ensurePredicate(Predicate<? super T> predicate) throws InterruptedException {
    while (!predicate.contains(_state)) { this.wait(); }
    return _state;
  }
  
  /**
   * Tries to ensure that {@code predicate} accepts the current state before continuing.  Blocks if necessary.
   * Stops waiting if interrupted.
   * @return  The state at the time this method returns (if not interrupted, this was accepted by {@code predicate}).
   */
  public synchronized T attemptEnsurePredicate(Predicate<? super T> predicate) {
    try { return ensurePredicate(predicate); }
    catch (InterruptedException e) { return _state; }
  }
  
  /**
   * Tries to ensure that {@code predicate} accepts the current state before continuing.  Blocks if necessary.
   * If interrupted or if the timeout is reached, stops waiting.
   * @param timeout  Maximum wait time, in milliseconds.  Must be positive or zero (where zero signals, as in
   *                 {@link Object#wait(long)}, that no timeout should be used).
   * @return  The state at the time this method returns (barring an interrupt or timeout, this was accepted by
   *          {@code predicate}).
   */
  public synchronized T attemptEnsurePredicate(Predicate<? super T> predicate, long timeout) {
    if (timeout == 0) { return attemptEnsurePredicate(predicate); }
    else if (predicate.contains(_state)) { return _state; }
    else {
      // must record expected wake-up time to account for spurious wake-ups
      long timeoutTime = System.currentTimeMillis() + timeout;
      try {
        do {
          this.wait(timeout);
          long currentTime = System.currentTimeMillis();
          if (currentTime >= timeoutTime) { break; } // timeout has been reached
          else { timeout = timeoutTime - currentTime; }
        } while (!predicate.contains(_state));
        return _state;
      }
      catch (InterruptedException e) { return _state; }
    }
  }
  
  /** Test whether the current state matches {@code expected}.  Helper method for performing null-aware comparison. */
  private boolean inState(T expected) {
    T current = _state; // only read once, to allow for concurrent changes
    return (current == null) ? (expected == null) : current.equals(expected);
  }
  
}
