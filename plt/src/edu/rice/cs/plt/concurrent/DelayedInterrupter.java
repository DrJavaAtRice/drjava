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
      Thread.interrupted(); // clear the interrupted status, in case it occured but wasn't detected
    }
  }
  
}
