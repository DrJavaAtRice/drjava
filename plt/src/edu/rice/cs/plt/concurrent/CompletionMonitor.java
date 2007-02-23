package edu.rice.cs.plt.concurrent;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Enables threads to communicate with each other by signaling. Typically, this communication 
 * concerns a task which one thread must complete before other threads can proceed.
 */
public class CompletionMonitor {
  private volatile boolean _signal;
  
  /** Create an unsignalled completion monitor. */
  public CompletionMonitor() { _signal = false; }
  
  /**
   * Create a completion monitor in the given initial state.  If signalled is {@code true}, invocations of
   * {@link #insureSignalled} will not block until {@link #reset} is invoked.
   */
  public CompletionMonitor(boolean signalled) { _signal = signalled; }
  
  /** Returns whether the flag is currently set */
  public boolean isSignalled() { return _signal; }
  
  /** Revert to the unsignalled state */
  public void reset() { _signal = false; }
  
  /** Sets the state to signalled and alerts all blocked threads */
  synchronized public void signal() {
    _signal = true;
    this.notifyAll();
  }
  
  /** Insures that the monitor has been signalled before continuing.  Blocks if necessary. */
  synchronized public void insureSignalled() throws InterruptedException {
    while (!_signal) { this.wait(); }
  }
  
  /**
   * Insures that the monitor has been signalled before continuing.  Blocks if necessary.  If the wait is interrupted,
   * returns {@code false}.
   */
  public boolean attemptInsureSignalled() {
    try { insureSignalled(); return true; }
    catch (InterruptedException e) { return false; }
  }
  
}
