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

package edu.rice.cs.plt.swing;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingUtilities;

import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.concurrent.IncrementalTaskController;

/**
 * <p>A utility class providing the core functionality of {@code javax.swing.SwingWorker} (first available
 * in Java 6), in addition to supporting the IncrementalTaskController interface.  Allows a task to be
 * separated into two parts: a working portion that calculates a value (or sequence of intermediate values)
 * in the background, and a GUI portion that executes in the Swing event thread when the working portion is
 * complete.  Implementations should define {@link #doInBackground} and, optionally, {@link #process}
 * and {@link #done}.  The {@code doInBackground()} implementation may call {@link #publish}
 * and {@link #authorizeContinue}; other protected methods should generally be ignored as implementation
 * details.</p>
 * 
 * <p>Implementations should be able to migrate seamlessly to the Java 6 API version by simply changing
 * the parent class, as long as they stick to methods that are defined in both classes.  This version doesn't
 * support PropertyChangeListeners, a progress property, the Runnable interface, a {@code getState()} method
 * (although {@code status()} provides similar information), or an {@code isCancelled()} method that can be 
 * polled by {@code doInBackground()} ({@code isCancelled()} is defined, but its result is never 
 * {@code true} before {@code doInBackground()} returns; implementations should instead handle cancellation
 * by checking for an interrupt).  There is also (currently) no thread pooling &mdash;
 * each worker runs in a new thread.  This version <em>adds</em> the methods provided by
 * IncrementalTaskController, as well as {@link #authorizeContinue}, which supports the implementation
 * of {@link #pause}.</p>
 */
public abstract class SwingWorker<R, I> extends IncrementalTaskController<I, R> {
  
  private CompletionMonitor _continueMonitor;
  private Thread _workerThread;
  
  public SwingWorker() {
    super();
    _continueMonitor = new CompletionMonitor(false);
    _workerThread = new Thread("SwingWorker") {
      public void run() {
        started();
        try { finishedCleanly(doInBackground()); }
        catch (InterruptedException e) { stopped(); }
        catch (Exception e) { finishedWithTaskException(e); }
        catch (Throwable t) { finishedWithImplementationException(new WrappedException(t)); }
      }
    };
    finishListeners().add(new Runnable() {
      public void run() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { done(); }
        });
      }
    });
    // Defined as a stand-alone class in order to simplify self-references.
    class IntermediateListener implements Runnable1<I> {
      public void run(I val) {
        intermediateListeners().remove(this); // don't respond again until the Swing task runs
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            List<I> vals = new LinkedList<I>();
            // add listener before drain to ensure a concurrent write isn't missed
            intermediateListeners().add(IntermediateListener.this);
            intermediateQueue().drainTo(vals);
            process(vals);
          }
        });
      }
    }
    @SuppressWarnings("unchecked") // javac 6 bug (Eclipse and javac 5 are fine)
    Runnable1<I> listener = new IntermediateListener();
    intermediateListeners().add(listener);
  }
  
  
  /*=== Public SwingWorker interface. ===*/
  
  public final void execute() { start(); }
  
  
  /*=== Methods to be overridden by the subclass. ===*/ 
    
  /** Work to be performed in a worker thread. */
  protected abstract R doInBackground() throws Exception;
  
  /** Action to be performed in the event thread when intermediate results are available. */
  protected void process(List<I> chunks) {}
  
  /** Action to be performed in the event thread when work has completed. */
  protected void done() {}
  
  
  /*=== Methods that the subclass may call in doInBackground(). ===*/
  
  /** Called by {@link #doInBackground} when intermediate results are available. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected final void publish(I... chunks) {
    BlockingQueue<I> queue = intermediateQueue();
    try {
      for (I val : chunks) { queue.put(val); }
    }
    catch (InterruptedException e) { throw new WrappedException(e); }
  }
  
  /**
   * <p>Called by {@link #doInBackground} to ensure that the task has not been paused or canceled.
   * If paused, this method blocks until the task is restarted.  If canceled, throws an
   * InterruptedException.</p>
   * 
   * <p>Tasks that wish to maintain migration compatibility with {@code javax.swing.SwingWorker} cannot
   * call this method or support pausing.  Instead, they should simply respond to an interrupt when canceled
   * (checking, for example, via {@link Thread#interrupted}).</p> 
   */
  protected void authorizeContinue() throws InterruptedException {
    if (Thread.interrupted()) { throw new InterruptedException(); }
    if (!_continueMonitor.isSignaled()) {
      paused();
      _continueMonitor.ensureSignaled();
      started();
    }
  }
  
  
  /*=== Implementation of IncrementalTaskController. ===*/
  
  
  protected final void doStart() {
    _continueMonitor.signal();
    _workerThread.start();
  }
  protected final void doPause() { _continueMonitor.reset(); }
  protected final void doResume() { _continueMonitor.signal(); }
  protected final void doStop() { _workerThread.interrupt(); }
  
}
