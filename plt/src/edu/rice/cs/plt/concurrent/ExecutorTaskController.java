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

import java.util.concurrent.Executor;

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * A TaskController for a simple task, which is scheduled for execution by an Executor.  To support
 * canceling, the task should respond to an interrupt by throwing an {@link InterruptedException}, wrapped by a
 * {@link WrappedException}.  The task is submitted (via {@link Executor#execute}) when {@code start()} is
 * invoked (if the executor blocks, so will {@code start()}); its status is changed to "running" when it
 * actually begins executing; if canceled in the interim, the status will still be "paused" until the
 * task begins its scheduled execution.
 */
public class ExecutorTaskController<R> extends TaskController<R> {
  // fields will be changed to null by discard(), but no need for volatile because it's only for garbage collection
  private Executor _executor;
  private Thunk<? extends R> _task;
  // must be volatile because it starts uninitialized
  private volatile Thread _t;
  
  public ExecutorTaskController(Executor executor, Thunk<? extends R> task) {
    _executor = executor;
    _task = task;
    _t = null; // initialized immediately before started() is called
  }
  
  protected void doStart() {
    _executor.execute(new Runnable() {
      public void run() {
        _t = Thread.currentThread();
        started();
        try {
          // stop if the task was canceled before execution began
          if (Thread.interrupted()) { throw new InterruptedException(); }
          finishedCleanly(_task.value());
        }
        catch (InterruptedException e) { stopped(); }
        catch (WrappedException e) {
          if (e.getCause() instanceof InterruptedException) { stopped(); }
          else { finishedWithTaskException(e); }
        }
        catch (RuntimeException e) { finishedWithTaskException(e); }
      }
    });
  }
  
  protected void doStop() { _t.interrupt(); }
  protected void discard() { _executor = null; _task = null; _t = null; }
}
