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

package edu.rice.cs.plt.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.rice.cs.plt.lambda.Condition;

/**
 * Provides a convenient facility for blocking until a boolean flag is signaled.  Typically, this is used 
 * for communication in which one thread must complete a task before other threads can proceed.
 */
public class CompletionMonitor implements Condition {
  private volatile boolean _signal;
  
  /** Create an unsignaled completion monitor. */
  public CompletionMonitor() { _signal = false; }
  
  /**
   * Create a completion monitor in the given initial state.  If signaled is {@code true}, invocations of
   * {@link #ensureSignaled} will not block until {@link #reset} is invoked.
   */
  public CompletionMonitor(boolean signaled) { _signal = signaled; }
  
  /** Returns whether the flag is currently set */
  public boolean isSignaled() { return _signal; }
  
  /** Returns whether the flag is currently set */
  public boolean isTrue() { return _signal; }
  
  /** Revert to the unsignaled state */
  public void reset() { _signal = false; }
  
  /** Sets the state to signaled and alerts all blocked threads */
  public void signal() {
    if (! _signal) {
      synchronized (this) {
        if (! _signal) {
          _signal = true;
          this.notifyAll();
        }
      }
    }
  }
  
  /** Ensures that the monitor has been signaled before continuing.  Blocks if necessary. */
  public void ensureSignaled() throws InterruptedException {
    if (! _signal) {
      synchronized (this) {
        while (!_signal) { this.wait(); }
      }
    }
  }
  
  /**
   * Ensures that the monitor has been signaled before continuing.  Blocks if necessary; fails if the
   * the timeout is reached.
   * @param timeout  Maximum wait time, in milliseconds.
   */
  public void ensureSignaled(long timeout) throws InterruptedException, TimeoutException {
    ensureSignaled(timeout, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Ensures that the monitor has been signaled before continuing.  Blocks if necessary; fails if the
   * the timeout is reached.
   * @param timeout  Maximum wait time, in {@code unit} units.
   * @param unit  Units for {@code timeout}.
   */
  public void ensureSignaled(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    if (! _signal) {
      long timeoutTime = ConcurrentUtil.futureTimeNanos(timeout, unit);
      synchronized (this) {
        while (!_signal) { ConcurrentUtil.waitUntilNanos(this, timeoutTime); }
      }
    }
  }
  
  /**
   * Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
   * is interrupted, returns {@code false}.
   */
  public boolean attemptEnsureSignaled() {
    try { ensureSignaled(); return true; }
    catch (InterruptedException e) { return _signal; }
  }
  
  /**
   * Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
   * is interrupted or the timeout is reached, returns {@code false}.
   * @param timeout  Maximum wait time, in milliseconds.
   */
  public boolean attemptEnsureSignaled(long timeout) {
    try { ensureSignaled(timeout, TimeUnit.MILLISECONDS); return true; }
    catch (InterruptedException e) { return _signal; }
    catch (TimeoutException e) { return _signal; }
  }
  
  /**
   * Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
   * is interrupted or the timeout is reached, returns {@code false}.
   * @param timeout  Maximum wait time, in {@code unit} units.
   * @param unit  Units for {@code timeout}.
   */
  public boolean attemptEnsureSignaled(long timeout, TimeUnit unit) {
    try { ensureSignaled(timeout, unit); return true; }
    catch (InterruptedException e) { return _signal; }
    catch (TimeoutException e) { return _signal; }
  }
  
}
