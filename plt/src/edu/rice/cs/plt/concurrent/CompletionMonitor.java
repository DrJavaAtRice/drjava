/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2017 JavaPLT group at Rice University
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

/** Provides a convenient facility for blocking until a boolean flag is true or signaled.  Typically, this is used 
  * for communication in which one thread must complete a task before other threads can proceed.  But it can also 
  * serve to block threads only when an alarm has been raised (the raise() operation.  In this case, the default flag
  * value is true and the flag becomes false only when a raise() operation is executed.  Once this operation has
  * been executed, the various blocking methods (ensure..., attempt...) will block until a signal or timeout happens.
  * At this point, the monitor is reset to its default value.
  */
public class CompletionMonitor implements Condition {
  private volatile boolean _signal;
  private boolean _default;
  
  /** Create an unsignaled completion monitor. */
  public CompletionMonitor() { this(false); }
  
  /** Create a completion monitor in the given initial state.  If signaled is {@code true}, invocations of
    * {@link #ensureSignaled} will not block until {@link #reset} is invoked.
    */
  public CompletionMonitor(boolean signaled) { 
    _default = signaled;
    _signal = _default;
  }
  
  /** Returns whether the flag is currently set */
  public boolean isSignaled() { return _signal; }
  
  /** Returns whether the flag is currently set */
  public boolean isTrue() { return _signal; }
  
  /** Revert to the default state */
  public void reset() { _signal = _default; }
  
  /** Gets value of _signal and executes reset(). */
  public boolean getSignalAndReset() {
    final boolean result = _signal;
    reset();
    return result;
  }
    
  /** Set the flag to false.  Only used when default state is true. */
  public void raise() { _signal = false; }
  
  /** Sets the state to _signal to true and alerts all blocked threads */
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
  
  /** Ensures that the monitor has been signaled before continuing.  Blocks if necessary; fails if the
    * the timeout is reached.
    * @param timeout  Maximum wait time, in milliseconds.
    */
  public void ensureSignaled(long timeout) throws InterruptedException, TimeoutException {
    ensureSignaled(timeout, TimeUnit.MILLISECONDS);
  }
  
  /** Ensures that the monitor has been signaled before continuing.  Blocks if necessary; fails if the
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
  
  /** Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
    * is interrupted, returns {@code _signal}.
    */
  public boolean attemptEnsureSignaled() {
    try { ensureSignaled(); return true; }
    catch (InterruptedException e) { return getSignalAndReset(); }
  }
  
  /** Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
    * is interrupted or the timeout is reached, returns {@code _signal}.
    * @param timeout  Maximum wait time, in milliseconds.
    */
  public boolean attemptEnsureSignaled(long timeout) {
    try { ensureSignaled(timeout, TimeUnit.MILLISECONDS); return getSignalAndReset(); }
    catch (InterruptedException e) { return getSignalAndReset(); }
    catch (TimeoutException e) { return getSignalAndReset(); }
  }
  
  /** Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
    * is interrupted or the timeout is reached, returns {@code _signal}.
    * @param timeout  Maximum wait time, in {@code unit} units.
    * @param unit  Units for {@code timeout}.
    */
  public boolean attemptEnsureSignaled(long timeout, TimeUnit unit) {
    try { ensureSignaled(timeout, unit); return true; }
    catch (InterruptedException e) { return getSignalAndReset(); }
    catch (TimeoutException e) { return getSignalAndReset(); }
  }
}
