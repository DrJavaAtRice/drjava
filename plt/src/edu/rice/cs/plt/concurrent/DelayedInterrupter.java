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

/**
 * Sets a "time bomb" on a specific thread: if the {@link #abort} method is not invoked within a specified
 * amount of time (in milliseconds), the thread will be interrupted.
 */
public class DelayedInterrupter {
  
  private final Thread _worker;
  private final Thread _interrupter;
  
  /**
   * Create an interrupter for the current thread.
   * @param timeToInterrupt  Number of milliseconds to allow an abort before the thread will be interrupted.
   */
  public DelayedInterrupter(int timeToInterrupt) { this(Thread.currentThread(), timeToInterrupt); }
  
  /**
   * Create an interrupter for the specified thread.
   * @param timeToInterrupt  Number of milliseconds to allow an abort before the thread will be interrupted.
   */
  public DelayedInterrupter(Thread worker, final int timeToInterrupt) {
    _worker = worker;
    _interrupter = new Thread() {
      public void run() {
        try {
          sleep(timeToInterrupt);
          _worker.interrupt();
        }
        catch (InterruptedException e) { /* abort has occurred */ }
      }
    };
    _interrupter.start();
  }
  
  /** Abort the request to interrupt the thread.  Should be called from the worker thread. */
  public void abort() {
    _interrupter.interrupt();
    if (Thread.currentThread() == _worker) {
      Thread.interrupted(); // clear the interrupted status, in case it occurred but wasn't detected
    }
  }
  
}
