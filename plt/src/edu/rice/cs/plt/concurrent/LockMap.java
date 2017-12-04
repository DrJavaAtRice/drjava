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

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import edu.rice.cs.plt.tuple.IdentityWrapper;

/**
 * A map for associating arbitrary objects with locks for the lifetime of the objects.  This allows, for example,
 * synchronization based on the arguments to a method (the alternative of directly locking on the arguments
 * themselves is dangerous, because the argument objects may be used for locking in a different context; two
 * threads locking on the same object for unrelated purposes may lead to deadlock).  Each unique key
 * (distinguished via {@code ==}) is associated with a different ReentrantLock.  Weak references are used to
 * allow keys to be freely garbage-collected.
 */
public class LockMap<T> {
  
  private final WeakHashMap<IdentityWrapper<T>, ReentrantLock> _map;
  
  public LockMap() { _map = new WeakHashMap<IdentityWrapper<T>, ReentrantLock>(); }

  public LockMap(int initialCapacity) {
    _map = new WeakHashMap<IdentityWrapper<T>, ReentrantLock>(initialCapacity);
  }
  
  /** Get the lock associated with the given value.  If necessary, allocate and cache a new lock. */
  public synchronized ReentrantLock get(T val) {
    IdentityWrapper<T> key = IdentityWrapper.make(val);
    if (!_map.containsKey(key)) { _map.put(key, new ReentrantLock()); }
    return _map.get(key);
  }
  
  /**
   * Acquire the lock associated with the given value, and return a Runnable for unlocking when the lock can
   * be released.  Clients should generally follow this invocation with a {@code try/finally} block that
   * guarantees execution of the resulting runnable.
   * @return  A Runnable for invoking {@code lock.unlock()}.  If invoked incorrectly, may throw an
   *          {@link IllegalMonitorStateException}.
   * @see ReentrantLock#lock
   * @see ReentrantLock#unlock
   */
  public Runnable lock(T val) {
    final ReentrantLock l = get(val);
    Runnable result = new Unlocker(l);
    l.lock();
    return result;
  }
  
  /**
   * Declared out of local scope to prevent accidental ties to local variables, potentially preventing
   * garbage collection.
   */
  private static class Unlocker implements Runnable {
    private final ReentrantLock _l;
    public Unlocker(ReentrantLock l) { _l = l; }
    public void run() { _l.unlock(); }
  }
  
}
