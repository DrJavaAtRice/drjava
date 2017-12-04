package edu.rice.cs.plt.concurrent;

import java.util.concurrent.Executor;

import edu.rice.cs.plt.lambda.WrappedException;

/**
 * A TaskController for an IncrementalTask, which is scheduled for execution by an Executor.  To support
 * canceling, the task should respond to an interrupt by throwing an {@link InterruptedException}, wrapped by a
 * {@link WrappedException}.  The task is submitted (via {@link Executor#execute}) when {@code start()} is
 * invoked (if the executor blocks, so will {@code start()}); its status is changed to "running" when it
 * actually begins executing; if canceled in the interim, the status will still be "paused" until the
 * task begins its scheduled execution.
 */
public class ExecutorIncrementalTaskController<I, R> extends IncrementalTaskController<I, R> {
  // fields will be changed to null by discard(), but no need for volatile because it's only for garbage collection
  private Executor _executor;
  private IncrementalTask<? extends I, ? extends R> _task;
  private CompletionMonitor _continueMonitor;
  // volatile because it's uninitialized at first
  private volatile Thread _t;
  
  public ExecutorIncrementalTaskController(Executor executor, IncrementalTask<? extends I, ? extends R> task,
                                           boolean ignoreIntermediate) {
    super(ignoreIntermediate);
    _executor = executor;
    _task = task;
    _continueMonitor = new CompletionMonitor(false);
    _t = null;
  }
  
  protected void doStart() {
    _continueMonitor.signal();
    _executor.execute(new Runnable() {
      public void run() {
        _t = Thread.currentThread();
        started();
        try {
          while (!_task.isResolved()) {
            authorizeContinue();
            stepped(_task.step());
          }
          authorizeContinue();
          finishedCleanly(_task.value());
        }
        catch (WrappedException e) {
          if (e.getCause() instanceof InterruptedException) { stopped(); }
          else { finishedWithTaskException(e); }
        }
        catch (RuntimeException e) { finishedWithTaskException(e); }
        catch (InterruptedException e) { stopped(); }
        catch (Throwable t) { finishedWithImplementationException(new WrappedException(t)); }
      }
      private void authorizeContinue() throws InterruptedException {
        if (Thread.interrupted()) { throw new InterruptedException(); }
        if (!_continueMonitor.isSignaled()) {
          paused();
          _continueMonitor.ensureSignaled();
          started();
        }
      }
    });
  }

  protected void doPause() { _continueMonitor.reset(); }
  protected void doResume() { _continueMonitor.signal(); }
  protected void doStop() { _t.interrupt(); }
  protected void discard() { _executor = null; _task = null; _continueMonitor = null; _t = null; }
  
}
