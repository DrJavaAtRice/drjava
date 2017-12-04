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

package edu.rice.cs.plt.debug;

import java.util.List;
import java.util.LinkedList;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A simple timer based on {@link System#currentTimeMillis}.  To support better performance, this class 
 * is not thread-safe.
 */
public class Stopwatch {
  private final List<Long> _splits;
  private boolean _running;
  private long _start;
  
  /** Create a new stopwatch with no split times and in a stopped state. */
  public Stopwatch() {
    _running = false;
    _splits = new LinkedList<Long>();
  }
  
  /**
   * Create a new stopwatch with no split times; if {@code startImmediately} is {@code true},
   * start the timer before returning.
   */
  public Stopwatch(boolean startImmediately) {
    _running = false;
    _splits = new LinkedList<Long>();
    if (startImmediately) start();
  }
  
  /**
   * Start the timer.
   * @throws IllegalStateException  If the stopwatch is currently running.
    */
  public void start() {
    if (_running) { throw new IllegalStateException("Already running"); }
    _start = System.currentTimeMillis();
    _running = true;
  }
  
  /**
   * Record and return the number of milliseconds since {@code start()} was invoked.
   * @throws IllegalStateException  If the stopwatch is not currently running.
   */
  public long split() {
    if (!_running) { throw new IllegalStateException("Not running"); }
    long result = System.currentTimeMillis() - _start;
    _splits.add(result);
    return result;
  }
  
  /**
   * Stop the timer; record and return the number of milliseconds since {@code start()} was invoked.
   * @throws IllegalStateException  If the stopwatch is not currently running.
   */
  public long stop() {
    long result = split();
    _running = false;
    return result;
  }

  /**
   * Get a dynamically-updating view of all splits recorded by the stopwatch.
   * (Create a {@link IterUtil#snapshot} if the stopwatch is to be used while iterating over
   * this list.)
   */
  public Iterable<Long> splits() { return IterUtil.immutable(_splits); }
}
