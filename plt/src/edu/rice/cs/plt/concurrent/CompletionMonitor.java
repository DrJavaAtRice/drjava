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

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Enables threads to communicate with each other by signaling. Typically, this communication 
 * concerns a task which one thread must complete before other threads can proceed.
 */
public class CompletionMonitor {
  private volatile boolean _signal;
  
  /** Create an unsignalled completion monitor. */
  public CompletionMonitor() { _signal = false; }
  
  /**
   * Create a completion monitor in the given initial state.  If signalled is {@code true}, invocations of
   * {@link #ensureSignalled} will not block until {@link #reset} is invoked.
   */
  public CompletionMonitor(boolean signalled) { _signal = signalled; }
  
  /** Returns whether the flag is currently set */
  public boolean isSignalled() { return _signal; }
  
  /** Revert to the unsignalled state */
  public void reset() { _signal = false; }
  
  /** Sets the state to signalled and alerts all blocked threads */
  synchronized public void signal() {
    _signal = true;
    this.notifyAll();
  }
  
  /** Ensures that the monitor has been signalled before continuing.  Blocks if necessary. */
  synchronized public void ensureSignalled() throws InterruptedException {
    while (!_signal) { this.wait(); }
  }
  
  /**
   * Ensures that the monitor has been signalled before continuing.  Blocks if necessary.  If the wait is interrupted,
   * returns {@code false}.
   */
  public boolean attemptEnsureSignalled() {
    try { ensureSignalled(); return true; }
    catch (InterruptedException e) { return false; }
  }
  
}
