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

import java.io.Serializable;
import java.util.Date;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;

/** A serializable and immutable view of a Thread at a particular time. */
public class ThreadSnapshot implements Serializable {
  
  /** The number of stack frames used by the implementation of Thread.getStackTrace() */
  private static final int GET_STACK_TRACE_DEPTH;
  static {
    StackTraceElement[] s = Thread.currentThread().getStackTrace();
    int depth = 0;
    String name = ThreadSnapshot.class.getName();
    while (depth < s.length) {
      if (name.equals(s[depth].getClassName())) { break; }
      depth++;
    }
    GET_STACK_TRACE_DEPTH = depth;
  }
  
  private final String _name;
  private final long _id;
  private final boolean _daemon;
  private final int _priority;
  private final String _group;
  
  private final Date _time;
  
  private final SizedIterable<StackTraceElement> _stack;
  private final StackTraceElement _running;
  private final StackTraceElement _calling;
  
  private final Thread.State _state;
  private final boolean _alive;
  private final boolean _interrupted;
  
  public ThreadSnapshot() {
    this(Thread.currentThread(), true);
  }
  
  public ThreadSnapshot(Thread t) {
    this(t, t == Thread.currentThread());
  }
  
  /**
   * If {@code filterStack}, the top two stack locations are ignored: they correspond
   * to this constructor and the calling public constructor.
   */
  private ThreadSnapshot(Thread t, boolean filterStack) {
    _name = t.getName();
    _id = t.getId();
    _daemon = t.isDaemon();
    _priority = t.getPriority();
    ThreadGroup g = t.getThreadGroup();
    _group = (g == null) ? null : g.getName();
    
    _time = new Date();
    
    StackTraceElement[] s = t.getStackTrace();
    if (filterStack) {
      int offset = GET_STACK_TRACE_DEPTH + 2; // two extra for ThreadSnapshot constructors
      if (s.length > offset) { // at least one useful StackTraceElement is available
        _stack = IterUtil.arraySegment(s, offset);
        _running = s[offset];
        _calling = (s.length > offset+1) ? s[offset+1] : null;
      }
      else {
        _stack = IterUtil.empty();
        _running = null;
        _calling = null;
      }
    }
    else {
      _stack = IterUtil.asIterable(s);
      _running = (s.length >= 1) ? s[0] : null;
      _calling = (s.length >= 2) ? s[1] : null;
    }
    
    _state = t.getState();
    _alive = t.isAlive();
    _interrupted = t.isInterrupted();
  }
  
  /** The result of {@link Thread#getName()} at the snapshot time. */
  public String getName() { return _name; }
  /** The result of {@link Thread#getId()} at the snapshot time. */
  public long getId() { return _id; }
  /** The result of {@link Thread#isDaemon()} at the snapshot time. */
  public boolean isDaemon() { return _daemon; }
  /** The result of {@link Thread#getPriority()} at the snapshot time. */
  public int getPriority() { return _priority; }
  /**
   * The name of {@code t}'s ThreadGroup at the snapshot time, as returned by {@link Thread#getThreadGroup()},
   * or {@code null} if it did not have one.
   */
  public String getThreadGroup() { return _group; }

  /** The time at which the snapshot was taken. */
  public Date snapshotTime() { return _time; }

  /**
   * The stack trace at creation time, as returned by {@link Thread#getStackTrace()}.  If the thread was
   * used to take a snapshot of itself, the relevant ThreadSnapshot invocations are hidden in the result.
   * Note that {@code Thread.getStackTrace()} does not guarantee complete results, and the trace may even 
   * be empty. 
   */
  public SizedIterable<StackTraceElement> getStackTrace() { return _stack; }
  /**
   * The top of the stack at snapshot time, or {@code null} if unavailable.  If the thread was
   * used to take a snapshot of itself, the relevant ThreadSnapshot invocations are hidden in the result.
   */
  public StackTraceElement runningLocation() { return _running; }
  /**
   * The second stack element at snapshot time, or {@code null} if unavailable.  If the thread was
   * used to take a snapshot of itself, the relevant ThreadSnapshot invocations are hidden in the result.
   */
  public StackTraceElement callingLocation() { return _calling; }
  
  /** The result of {@link Thread#getState()} at the snapshot time. */
  public Thread.State getState() { return _state; }  
  /** The result of {@link Thread#isAlive()} at the snapshot time. */
  public boolean isAlive() { return _alive; }
  /** The result of {@link Thread#isInterrupted()} at the snapshot time. */
  public boolean isInterrupted() { return _interrupted; }

  /**
   * Produce a string representation of the thread, as would be returned by {@link Thread#toString} at
   * snapshot time.
   */
  public String toString() {
    return "Thread[" + _name + "," + _priority + "," + (_group == null ? "" : _group) + "]";
  }
  
}
