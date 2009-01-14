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

package edu.rice.cs.plt.swing;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.concurrent.TaskController;
import edu.rice.cs.plt.concurrent.TaskController.Status;
import edu.rice.cs.plt.lambda.Thunk;

/**
 * A utility class providing some of the functionality of {@code javax.swing.SwingWorker}
 * (first available in Java 6).  Allows a task to be separated into two parts:
 * a working portion that calculates a value in the background, and a GUI portion
 * that executes in the Swing event thread when the working portion is complete.
 */
public abstract class SwingWorker<T> {
  
  private final TaskController<T> _controller;
  private volatile boolean _cancelled;
  
  public SwingWorker() {
    Thunk<T> task = new Thunk<T>() {
      public T value() {
        try { return doInBackground(); }
        catch (Throwable t) { throw new WrappedException(t); }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { done(); }
          });
        }
      }
    };
    _controller = ConcurrentUtil.computeInProcess(task, false);
    _cancelled = false;
  }
  
  /** Work to be performed in a worker thread. */
  protected abstract T doInBackground() throws Exception;
  
  /** Action to be performed in the event thread when work has completed. */
  protected void done() {}
  
  /** Begin computation in the worker thread. */
  public final void execute() {  _controller.start(); }

  /**
   * Requests that the computation be cancelled.  If the worker thread is blocked,
   * it will be interrupted.  Workers that wish to support cancellation and that
   * do not periodically block should instead poll {@link #isCancelled()}.
   */
  public final void cancel() { _cancelled = true; _controller.cancel(); }
  
  /** Returns {@code true} iff {@link #cancel()} has been invoked. */
  public final boolean isCancelled() { return _cancelled; }
  
  /**
   * Returns {@code true} if worker thread has completed.  Note that there
   * may be a delay between when {@code isCancelled()} is {@code true} and
   * when {@code isDone()} is {@code true}.
   */
  public final boolean isDone() {
    Status status = _controller.status();
    return status.equals(Status.CANCELED) || status.equals(Status.FINISHED);
  }
  
  /**
   * Get the value of the computation.  Unless {@code isDone()} is {@code true},
   * blocks until the worker task completes.
   * @throws IllegalStateException  If the task was cancelled.
   * @throws InterruptedException  If this thread is interrupted while waiting for a result.
   * @throws InvocationTargetException  Wraps any exceptions thrown by {@link #doInBackground()}.
   */
  public final T get() throws InterruptedException, InvocationTargetException {
    if (_cancelled) { throw new IllegalStateException("Task was cancelled"); }
    try { return _controller.value(); }
    catch (WrappedException e) {
      Throwable cause = e.getCause();
      if (cause instanceof InterruptedException) {throw (InterruptedException) cause; }
      else if (cause instanceof InvocationTargetException && cause.getCause() != null) {
        // The task wrapped the exception, so we must unwrap it
        throw new InvocationTargetException(cause.getCause());
      }
      else { throw e; }
    }
  }
  
}
