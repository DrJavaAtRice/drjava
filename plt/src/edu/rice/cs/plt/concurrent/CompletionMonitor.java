package edu.rice.cs.plt.concurrent;

/**
 * Enables threads to communicate with each other by signaling. Typically, this communication 
 * concerns a task which one thread must complete before other threads can proceed.
 */
public class CompletionMonitor {
  private boolean _flag;
  
  public CompletionMonitor(boolean flag) { _flag = flag; }
  
  public CompletionMonitor() { this(false); }
  
  /** Returns whether the flag is currently set */
  public synchronized boolean isFlag() { return _flag; }
  
  /** Sets the state to signaled, indicating that waiting threads can continue */
  synchronized public void set() {
    _flag = true;
    this.notifyAll();
  }
  
  /** Sets the state to unsignaled */
  synchronized public void reset() { _flag = false; }
  
  /** Causes the calling thread to wait for the signal to be set before continuing
   *  If the signal is already set, it returns immediately
   * @return returns true, unless the waiting thread was interrupted */
  synchronized public boolean waitOne() {
    while (!_flag) {
      try { this.wait(); } 
      catch (InterruptedException e) { return false; }
    }
    return true;
  }
}
