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

import edu.rice.cs.plt.lambda.Condition;

/**
 * Provides a convenient facility for blocking until an arbitrary condition is satisfied.
 * Each time {@link #check} is invoked, the blocked threads check to see if the condition 
 * has become {@code true}.  Note that there is no way for this to occur "automatically" &mdash;
 * the only way the monitor can be made aware of a possible change is by an explicit call to {@code check()}.
 */ 
public class ConditionMonitor implements Condition {
  private final Condition _condition;
  
  /** Create an unsignaled completion monitor. */
  public ConditionMonitor(Condition condition) { _condition = condition; }
  
  /** Returns whether the flag is currently set */
  public boolean isTrue() { return _condition.isTrue(); }
  
  /** Check the condition and, if it is now {@code true}, notify all blocked threads. */
  synchronized public void check() {
    // each thread will check the condition again, but we can optimize the case where the condition is false
    // by checking once here
    if (_condition.isTrue()) { this.notifyAll(); }
  }
  
  /** Ensures that the condition is true before continuing.  Blocks if necessary. */
  synchronized public void ensureTrue() throws InterruptedException {
    while (!_condition.isTrue()) { this.wait(); }
  }
  
  /**
   * Tries to ensure that the condition is true before continuing.  Blocks if necessary.  If the wait is
   * interrupted, returns {@code false}.
   */
  public boolean attemptEnsureTrue() {
    try { ensureTrue(); return true; }
    catch (InterruptedException e) { return _condition.isTrue(); }
  }
  
  /**
   * Tries to ensure that the monitor has been signaled before continuing.  Blocks if necessary.  If the wait
   * is interrupted or the timeout is reached, returns {@code false}.
   * @param timeout  Maximum wait time, in milliseconds.  Must be positive or zero (where zero signals, as in
   *                 {@link Object#wait(long)}, that no timeout should be used).
   */
  public synchronized boolean attemptEnsureTrue(long timeout) {
    if (timeout == 0) { return attemptEnsureTrue(); }
    else if (_condition.isTrue()) { return true; }
    else {
      // must record expected wake-up time to account for spurious wake-ups
      long timeoutTime = System.currentTimeMillis() + timeout;
      try {
        do {
          this.wait(timeout);
          long currentTime = System.currentTimeMillis();
          if (currentTime >= timeoutTime) { return _condition.isTrue(); } // timeout has been reached
          else { timeout = timeoutTime - currentTime; }
        } while (!_condition.isTrue());
        return true; // no need to invoke condition again -- it was true at least once
      }
      catch (InterruptedException e) { return _condition.isTrue(); } // check, just in case it became true
    }
  }
  
}
