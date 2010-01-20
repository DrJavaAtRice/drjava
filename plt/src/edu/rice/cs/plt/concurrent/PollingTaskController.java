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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.rice.cs.plt.collect.ListenerSet;
import edu.rice.cs.plt.lambda.LazyRunnable;

/**
 * <p>A TaskController for tasks that are run without any facility for executing code when the task
 * is complete &mdash; instead, the controller must either block, poll for the task's current status.
 * When most of the controller's methods are invoked, the current status is polled before proceeding;
 * operations depending on completion block ({@link #finishListeners()} are handled by blocking in a new
 * daemon thread).</p> 
 * 
 * <p>To implement a concrete instance, a subclass must provide {@link #doStart}, {@link #doStop},
 * {@link #update}, {@link #finish}, and, optionally, {@link #discard}.</p>
 */
public abstract class PollingTaskController<R> extends TaskController<R> {
  
  private final LazyRunnable _startDaemon;
  
  protected PollingTaskController() {
    // a daemon thread is started only if necessary -- if there are no listeners, we can just wait
    // for the user to check on the current status
    _startDaemon = new LazyRunnable(new Runnable() {
      public void run() {
        Thread t = new Thread("PollingTaskController daemon") {
          public void run() {
            try { finish(); }
            catch (InterruptedException e) { /* if interrupted, just stop */ }
          }
        };
        t.setDaemon(true);
        t.start();
      }
    });
  }
  
  /**
   * Access the ListenerSet responding to the completion of computation.  The first time this method
   * is invoked, a daemon thread is started that will wait for computation to complete (otherwise,
   * any registered listeners will not run until the task is manually polled).
   */
  public ListenerSet<R>.Sink finishListeners() {
    _startDaemon.run();
    return super.finishListeners();
  }
  
 
  
  /**
   * Check the current status and call the appropriate method if the task is complete.  This method
   * is invoked frequently and should not block.
   */
  protected abstract void update();
  
  /**
   * Block until the task is complete or this thread is interrupted.  Must not return until after one
   * of the completion methods are called.
   */
  protected abstract void finish() throws InterruptedException;
  
  /**
   * Block until the task is complete, this thread is interrupted, or the given timeout is reached.  Must
   * not return until after one of the completion methods are called.
   */
  protected abstract void finish(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
  
  protected RunningState runningState() { return new PollingRunningState(); }
  
  /**
   * RunningState variant based on a PollingRunner.  Extends RunningState to allow it to be
   * recognized as that type.
   */
  protected class PollingRunningState extends RunningState {
    public Status status() {
      update();
      return (state.get() == this) ? Status.RUNNING : state.get().status();
    }
    public void start() {
      update();
      if (state.get() != this) { state.get().start(); }
    }
    public R get() throws InterruptedException, ExecutionException {
      finish();
      if (!isDone()) { throw new IllegalStateException("PollingTaskController returned without finishing"); }
      return state.get().get();
    }
    public R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException  {
      finish(timeout, u);
      if (!isDone()) { throw new IllegalStateException("PollingTaskController returned without finishing"); }
      return state.get().get();
    }
    public boolean cancel(boolean stopRunning) {
      update();
      if (state.get() == this) {
        if (stopRunning) {
          if (state.compareAndSet(this, new PollingCancelingState())) { doStop(); return true; }
          else { return state.get().cancel(stopRunning); }
        }
        else { return false; }
      }
      else { return state.get().cancel(stopRunning); }
    }
  }
  
  /**
   * CancelingState variant based on a PollingRunner.  Extends Canceling to allow it to be
   * recognized as that type.  (A lot of code is duplicated from PollingRunningState, but that
   * seems inevitable without overhauling the design to use interfaces to identify different
   * kinds of states.)
   */
  protected class PollingCancelingState extends CancelingState {
    public void start() {
      update();
      if (state.get() != this) { state.get().start(); }
    }
    public Status status() {
      update();
      return (state.get() == this) ? Status.RUNNING : state.get().status();
    }
    public boolean cancel(boolean stopRunning) {
      update();
      return (state.get() == this) ? stopRunning : state.get().cancel(stopRunning);
    }
    public R get() throws InterruptedException, ExecutionException {
      finish();
      if (!isDone()) { throw new IllegalStateException("PollingRunner returned without finishing"); }
      return state.get().get();
    }
    public R get(long timeout, TimeUnit u) throws InterruptedException, ExecutionException, TimeoutException  {
      finish(timeout, u);
      if (!isDone()) { throw new IllegalStateException("PollingRunner returned without finishing"); }
      return state.get().get();
    }
  }
  
}
